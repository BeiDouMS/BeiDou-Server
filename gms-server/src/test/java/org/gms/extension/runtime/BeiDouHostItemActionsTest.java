package org.gms.extension.runtime;

import org.gms.constants.inventory.ItemConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BeiDouHostItemActionsTest {

    @Test
    void directedDropPolicyRejectsRestrictedAndBoundItems() {
        assertEquals("CASH_ITEM",
                BeiDouHostItemActions.dropRestriction(true, false, false, false, (short) 0));
        assertEquals("DROP_RESTRICTED",
                BeiDouHostItemActions.dropRestriction(false, true, false, false, (short) 0));
        assertEquals("PET_ITEM",
                BeiDouHostItemActions.dropRestriction(false, false, true, false, (short) 0));
        assertEquals("BOUND_ITEM",
                BeiDouHostItemActions.dropRestriction(false, false, false, true, (short) 0));
        assertEquals("BOUND_ITEM",
                BeiDouHostItemActions.dropRestriction(
                        false, false, false, false, ItemConstants.ACCOUNT_SHARING));
        assertEquals(null,
                BeiDouHostItemActions.dropRestriction(false, false, false, false, (short) 0));
    }
}
