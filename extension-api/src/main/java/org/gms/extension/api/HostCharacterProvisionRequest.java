package org.gms.extension.api;

import java.util.Objects;

/**
 * Host-neutral input for creating a dedicated account and a native beginner
 * character. The credential is only valid for the duration of the synchronous
 * provisioning call and must never be retained or logged.
 */
public record HostCharacterProvisionRequest(
        String accountName,
        char[] credential,
        String characterName,
        int worldId
) {
    public HostCharacterProvisionRequest {
        accountName = requireText(accountName, "accountName");
        characterName = requireText(characterName, "characterName");
        Objects.requireNonNull(credential, "credential");
        if (credential.length == 0) {
            throw new IllegalArgumentException("credential must not be empty");
        }
        if (worldId < 0) {
            throw new IllegalArgumentException("worldId must not be negative");
        }
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
