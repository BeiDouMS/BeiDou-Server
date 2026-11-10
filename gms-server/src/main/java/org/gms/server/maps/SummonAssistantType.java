package org.gms.server.maps;

import org.gms.client.SkillSummonNode;

public enum SummonAssistantType {
    NONE(0),
    ATTACK(1),
    HEALORBUFF(2);

    private final int val;

    SummonAssistantType(int val) {
        this.val = val;
    }

    public int getValue() {
        return val;
    }

    public static SummonAssistantType getFromSummonData(SkillSummonNode node) {
        if (node.hasAttackNode()) {
            return SummonAssistantType.ATTACK;
        } else if (node.hasSkillNode()) {
            return SummonAssistantType.HEALORBUFF;
        } else {
            return SummonAssistantType.NONE;
        }
    }
}
