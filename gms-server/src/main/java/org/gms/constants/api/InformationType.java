package org.gms.constants.api;

import lombok.Getter;

@Getter
public enum InformationType {
    CASH("cash"),
    CONSUME("consume"),
    EQP("eqp"),
    ETC("etc"),
    INS("ins"),
    MAP("map"),
    MOB("mob"),
    NPC("npc"),
    PET("pet"),
    SKILL("skill"),
    ;

    private final String type;

    InformationType(final String type) {
        this.type = type;
    }

    public static InformationType ofType(final String type) {
        for (InformationType value : values()) {
            if (value.type.equals(type)) {
                return value;
            }
        }
        return null;
    }
}
