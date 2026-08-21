package org.gms.constants.skills;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WarriorSkillConstantsTest {

    @Test
    void activeFirstJobSkillsUseTheIdsPresentInV83Wz() {
        assertEquals(1_001_004, Warrior.POWER_STRIKE);
        assertEquals(1_001_005, Warrior.SLASH_BLAST);
    }
}
