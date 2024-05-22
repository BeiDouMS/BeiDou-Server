package org.gms.net.packet.out;

import org.gms.net.opcodes.SendOpcode;
import org.gms.net.packet.ByteBufOutPacket;

public final class SendNoteSuccessPacket extends ByteBufOutPacket {

    public SendNoteSuccessPacket() {
        super(SendOpcode.MEMO_RESULT);

        writeByte(4);
    }
}
