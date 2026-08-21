package org.gms.extension.runtime;

import org.gms.client.Character;
import org.gms.client.creator.CharacterFactoryRecipe;
import org.gms.client.creator.novice.BeginnerCreator;
import org.gms.extension.api.HostCharacterProvisionRequest;
import org.gms.extension.api.HostCharacterProvisionResult;
import org.gms.extension.api.HostCharacterProvisioner;
import org.gms.net.server.Server;
import org.gms.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Objects;

/**
 * BeiDou's native atomic account/character provisioning implementation.
 */
public final class BeiDouHostCharacterProvisioner implements HostCharacterProvisioner {

    private static final Logger log =
            LoggerFactory.getLogger(BeiDouHostCharacterProvisioner.class);

    private final AccountService accounts;
    private final JdbcTransactionCoordinator transactions;

    public BeiDouHostCharacterProvisioner(AccountService accounts) {
        this(accounts, new JdbcTransactionCoordinator());
    }

    BeiDouHostCharacterProvisioner(
            AccountService accounts,
            JdbcTransactionCoordinator transactions
    ) {
        this.accounts = Objects.requireNonNull(accounts, "accounts");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
    }

    @Override
    public HostCharacterProvisionResult provision(HostCharacterProvisionRequest request) throws Exception {
        Objects.requireNonNull(request, "request");
        validateLengths(request);

        String encodedPassword = accounts.encryptPassword(request.credential());
        Provisioned provisioned = transactions.execute(connection -> {
            ensureAvailable(connection, "accounts", "name", request.accountName());
            ensureAvailable(connection, "characters", "name", request.characterName());

            int accountId = insertAccount(connection, request.accountName(), encodedPassword);
            Character character = BeginnerCreator.prepareProvisionedCharacter(
                    accountId, request.worldId(), request.characterName());
            if (character == null) {
                throw new IllegalArgumentException("host rejected default beginner character");
            }

            CharacterFactoryRecipe recipe = BeginnerCreator.createDefaultRecipe();
            if (!character.insertNewChar(recipe, connection)) {
                throw new IllegalStateException("native character insertion failed");
            }

            return new Provisioned(character, new HostCharacterProvisionResult(
                    character.getId(), accountId, request.characterName()));
        });

        // The login-server view must never expose an uncommitted character.
        // This is deliberately best-effort after commit: a cache publication
        // failure must not turn a durable provisioning success into a false
        // failure response that encourages the caller to create a duplicate.
        publishPostCommit(
                provisioned.character().getId(),
                provisioned.character().getName(),
                () -> Server.getInstance().createCharacterEntry(provisioned.character()));
        return provisioned.result();
    }

    static boolean publishPostCommit(
            int characterId,
            String characterName,
            Runnable publisher
    ) {
        Objects.requireNonNull(characterName, "characterName");
        Objects.requireNonNull(publisher, "publisher");
        try {
            publisher.run();
            return true;
        } catch (RuntimeException failure) {
            log.warn(
                    "Provisioned character {} ({}) committed, but login cache publication failed; "
                            + "the next server restart will rebuild the cache",
                    characterName, characterId, failure);
            return false;
        }
    }

    private static int insertAccount(Connection connection, String accountName, String encodedPassword)
            throws Exception {
        String sql = "INSERT INTO accounts (name, password, birthday, tempban) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, accountName);
            statement.setString(2, encodedPassword);
            statement.setDate(3, Date.valueOf(LocalDate.of(2005, 5, 11)));
            statement.setTimestamp(4, Timestamp.valueOf("2005-05-11 00:00:00"));
            if (statement.executeUpdate() != 1) {
                throw new IllegalStateException("native account insertion failed");
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException("native account id was not generated");
                }
                return keys.getInt(1);
            }
        }
    }

    private static void ensureAvailable(
            Connection connection,
            String table,
            String column,
            String value
    ) throws Exception {
        try (PreparedStatement statement =
                     connection.prepareStatement("SELECT 1 FROM " + table + " WHERE " + column + " = ?")) {
            statement.setString(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    throw new IllegalArgumentException(column + " is already in use");
                }
            }
        }
    }

    private static void validateLengths(HostCharacterProvisionRequest request) {
        if (request.accountName().length() > 13) {
            throw new IllegalArgumentException("accountName exceeds host limit");
        }
        if (request.characterName().length() > 13) {
            throw new IllegalArgumentException("characterName exceeds host limit");
        }
    }

    private record Provisioned(Character character, HostCharacterProvisionResult result) {
    }
}
