package org.gms.extension.runtime;

import org.gms.util.DatabaseConnection;

import java.sql.Connection;

/** Small injectable transaction boundary used by host extension capabilities. */
public final class JdbcTransactionCoordinator {

    @FunctionalInterface
    public interface ConnectionProvider {
        Connection open() throws Exception;
    }

    @FunctionalInterface
    public interface Work<T> {
        T execute(Connection connection) throws Exception;
    }

    private final ConnectionProvider connections;

    public JdbcTransactionCoordinator() {
        this(DatabaseConnection::getConnection);
    }

    public JdbcTransactionCoordinator(ConnectionProvider connections) {
        this.connections = connections;
    }

    public <T> T execute(Work<T> work) throws Exception {
        try (Connection connection = connections.open()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            int originalIsolation = connection.getTransactionIsolation();
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            try {
                T result = work.execute(connection);
                connection.commit();
                return result;
            } catch (Exception | Error failure) {
                try {
                    connection.rollback();
                } catch (Exception rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                }
                throw failure;
            } finally {
                connection.setTransactionIsolation(originalIsolation);
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }
}
