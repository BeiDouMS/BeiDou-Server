package org.gms.extension.api;

/** Committed native identity returned by a host provisioner. */
public record HostCharacterProvisionResult(int characterId, int accountId, String characterName) {
    public HostCharacterProvisionResult {
        if (characterId <= 0 || accountId <= 0) {
            throw new IllegalArgumentException("provisioned ids must be positive");
        }
        if (characterName == null || characterName.isBlank()) {
            throw new IllegalArgumentException("characterName must not be blank");
        }
    }
}
