package org.gms.extension.api;

/** Atomic host-owned creation of an account and a native character. */
@FunctionalInterface
public interface HostCharacterProvisioner {

    HostCharacterProvisionResult provision(HostCharacterProvisionRequest request) throws Exception;
}
