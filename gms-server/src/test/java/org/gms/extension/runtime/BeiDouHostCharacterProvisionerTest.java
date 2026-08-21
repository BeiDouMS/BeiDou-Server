package org.gms.extension.runtime;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeiDouHostCharacterProvisionerTest {

    @Test
    void postCommitCacheFailureDoesNotBecomeProvisioningFailure() {
        boolean published = BeiDouHostCharacterProvisioner.publishPostCommit(
                5,
                "luna",
                () -> {
                    throw new IllegalStateException("synthetic cache failure");
                });

        assertFalse(published);
    }

    @Test
    void successfulPostCommitCachePublicationIsReported() {
        AtomicInteger publications = new AtomicInteger();

        assertTrue(BeiDouHostCharacterProvisioner.publishPostCommit(
                5, "luna", publications::incrementAndGet));
        assertEquals(1, publications.get());
    }
}
