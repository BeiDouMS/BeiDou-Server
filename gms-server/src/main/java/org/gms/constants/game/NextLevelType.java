package org.gms.constants.game;

import lombok.Getter;

@Getter
public enum NextLevelType {
    SEND_NEXT("sendNextLevel"),
    SEND_LAST("sendLastLevel"),
    SEND_LAST_NEXT("sendLastNextLevel"),
    SEND_OK("sendOkLevel"),
    SEND_SELECT("sendSelectLevel"),
    SEND_NEXT_SELECT("sendNextSelectLevel"),
    GET_INPUT_NUMBER("getInputNumberLevel"),
    GET_INPUT_TEXT("getInputTextLevel"),
    SEND_ACCEPT_DECLINE("sendAcceptDeclineLevel"),
    SEND_YES_NO("sendYesNoLevel"),
    ;

    private final String type;

    NextLevelType(String type) {
        this.type = type;
    }
}
