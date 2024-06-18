package constants.id;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemIdTest {

    @Test
    void isCashPackage() {
        assertTrue(ItemId.isCashPackage(9102237));
    }

    @Test
    void isNotCashPackage() {
        assertFalse(ItemId.isCashPackage(4000000));
    }
}
