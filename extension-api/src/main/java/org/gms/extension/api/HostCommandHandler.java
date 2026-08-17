package org.gms.extension.api;

/**
 * Command callback. Character identity is passed as id to avoid leaking engine types into the API jar.
 */
@FunctionalInterface
public interface HostCommandHandler {

    void handle(int characterId, String[] args);
}
