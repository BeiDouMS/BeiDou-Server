package org.gms.net.server.channel.handlers;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.manager.ServerManager;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.service.HpMpAlertService;

/**
 * @author lee
 */
public class SetHpMpAlertHandler extends AbstractPacketHandler {
    /**
     * 客户端上报 0-19 共 20 个挡位；服务端按 /20 换算比例（最大 95%）。
     */
    private static final int MAX_ALERT_STEP = 19;

    @Override
    public void handlePacket(InPacket p, Client c) {
        HpMpAlertService hpMpAlertService = ServerManager.getApplicationContext().getBean(HpMpAlertService.class);
        Character chr = c.getPlayer();

        if (chr == null) {
            return;
        }

        // 客户端上报阈值挡位：0-19；此处按无符号字节解析并限幅，避免异常值影响自动吃药逻辑。
        int hpStep = Math.min(MAX_ALERT_STEP, Math.max(0, Byte.toUnsignedInt(p.readByte())));
        int mpStep = Math.min(MAX_ALERT_STEP, Math.max(0, Byte.toUnsignedInt(p.readByte())));
        hpMpAlertService.setHpAlert(chr.getId(), (byte) hpStep);
        hpMpAlertService.setMpAlert(chr.getId(), (byte) mpStep);

    }
}
