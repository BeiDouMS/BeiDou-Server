/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gms.net.server.channel.handlers;

import org.gms.client.Character;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.server.maps.AnimatedMapObject;
import org.gms.server.movement.AbsoluteLifeMovement;
import org.gms.server.movement.ChangeEquip;
import org.gms.server.movement.JumpDownMovement;
import org.gms.server.movement.LifeMovementFragment;
import org.gms.server.movement.RelativeLifeMovement;
import org.gms.server.movement.TeleportMovement;
import org.gms.exception.EmptyMovementException;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMovementPacketHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(AbstractMovementPacketHandler.class);

    protected List<LifeMovementFragment> parseMovement(InPacket p) throws EmptyMovementException {
        List<LifeMovementFragment> res = new ArrayList<>();
        byte numCommands = p.readByte();
        if (numCommands < 1) {
            throw new EmptyMovementException(p);
        }
        for (byte i = 0; i < numCommands; i++) {
            byte command = p.readByte();
            switch (command) {
                case 0: // normal move
                case 5:
                case 17: { // Float
                    short xpos = p.readShort();
                    short ypos = p.readShort();
                    short xwobble = p.readShort();
                    short ywobble = p.readShort();
                    short fh = p.readShort();
                    byte newstate = p.readByte();
                    short duration = p.readShort();
                    AbsoluteLifeMovement alm = new AbsoluteLifeMovement(command, new Point(xpos, ypos), duration, newstate);
                    alm.setFh(fh);
                    alm.setPixelsPerSecond(new Point(xwobble, ywobble));
                    res.add(alm);
                    break;
                }
                case 1: // jump
                case 2: // knockback
                case 6: // fj
                case 12:
                case 13: // Shot-jump-back thing
                case 16: // Float
                case 18:
                case 19: // Springs on maps
                case 20: // Aran Combat Step
                case 22: {
                    short xpos = p.readShort();
                    short ypos = p.readShort();
                    byte newstate = p.readByte();
                    short duration = p.readShort();
                    RelativeLifeMovement rlm = new RelativeLifeMovement(command, new Point(xpos, ypos), duration, newstate);
                    res.add(rlm);
                    break;
                }
                case 3: // teleport disappear
                case 4: // teleport appear
                case 7: // assaulter
                case 8: // assassinate
                case 9: // rush
                case 11: //chair
                {
//                case 14: {
                    short xpos = p.readShort();
                    short ypos = p.readShort();
                    short xwobble = p.readShort();
                    short ywobble = p.readShort();
                    byte newstate = p.readByte();
                    TeleportMovement tm = new TeleportMovement(command, new Point(xpos, ypos), newstate);
                    tm.setPixelsPerSecond(new Point(xwobble, ywobble));
                    res.add(tm);
                    break;
                }
                case 14:
                    p.skip(9); // jump down (?)
                    break;
                case 10: // Change Equip
                    res.add(new ChangeEquip(p.readByte()));
                    break;
                /*case 11: { // Chair
                    short xpos = lea.readShort();
                    short ypos = lea.readShort();
                    short fh = lea.readShort();
                    byte newstate = lea.readByte();
                    short duration = lea.readShort();
                    ChairMovement cm = new ChairMovement(command, new Point(xpos, ypos), duration, newstate);
                    cm.setFh(fh);
                    res.add(cm);
                    break;
                }*/
                case 15: {
                    short xpos = p.readShort();
                    short ypos = p.readShort();
                    short xwobble = p.readShort();
                    short ywobble = p.readShort();
                    short fh = p.readShort();
                    short ofh = p.readShort();
                    byte newstate = p.readByte();
                    short duration = p.readShort();
                    JumpDownMovement jdm = new JumpDownMovement(command, new Point(xpos, ypos), duration, newstate);
                    jdm.setFh(fh);
                    jdm.setPixelsPerSecond(new Point(xwobble, ywobble));
                    jdm.setOriginFh(ofh);
                    res.add(jdm);
                    break;
                }
                case 21: {//Causes aran to do weird stuff when attacking o.o
                    /*byte newstate = lea.readByte();
                     short unk = lea.readShort();
                     AranMovement am = new AranMovement(command, null, unk, newstate);
                     res.add(am);*/
                    p.skip(3);
                    break;
                }
                default:
                    log.warn("Unhandled case: {}", command);
                    throw new EmptyMovementException(p);
            }
        }

        if (res.isEmpty()) {
            throw new EmptyMovementException(p);
        }
        return res;
    }

    protected void updatePosition(InPacket p, AnimatedMapObject target, int yOffset) throws EmptyMovementException {

        byte numCommands = p.readByte();
        if (numCommands < 1) {
            throw new EmptyMovementException(p);
        }
        for (byte i = 0; i < numCommands; i++) {
            byte command = p.readByte();
            switch (command) {
                case 0: // normal move
                case 5:
                case 17: { // Float
                    //Absolute movement - only this is important for the server, other movement can be passed to the client
                    Point beforePos = snapshotPosition(target);
                    short xpos = p.readShort(); //is signed fine here?
                    short ypos = p.readShort();
                    Point afterPos = new Point(xpos, ypos + yOffset);
                    target.setPosition(afterPos);
                    p.skip(6); //xwobble = lea.readShort(); ywobble = lea.readShort(); fh = lea.readShort();
                    byte newstate = p.readByte();
                    target.setStance(newstate);
                    p.readShort(); //duration
                    recordRegularMove(target, beforePos, afterPos);
                    break;
                }
                case 1:
                case 2:
                case 6: // fj
                case 12:
                case 13: // Shot-jump-back thing
                case 16: // Float
                case 18:
                case 19: // Springs on maps
                case 20: // Aran Combat Step
                case 22: {
                    //Relative movement - server only cares about stance
                    p.skip(4); //xpos = lea.readShort(); ypos = lea.readShort();
                    byte newstate = p.readByte();
                    target.setStance(newstate);
                    p.readShort(); //duration
                    break;
                }
                case 3:
                case 4: { // teleport disappear/appear
                    handleTeleportMove(p, target, yOffset);
                    break;
                }
                case 7: // assaulter
                case 8: // assassinate
                case 9: { // rush
                    handleDashLikeMove(p, target, yOffset);
                    break;
                }
                case 11: //chair
                {
                    handleChairMove(p, target);
                    break;
                }
                case 14:
                    p.skip(9); // jump down (?)
                    break;
                case 10: // Change Equip
                    //ignored server-side
                    p.readByte();
                    break;
                /*case 11: { // Chair
                    short xpos = lea.readShort();
                    short ypos = lea.readShort();
                    short fh = lea.readShort();
                    byte newstate = lea.readByte();
                    short duration = lea.readShort();
                    ChairMovement cm = new ChairMovement(command, new Point(xpos, ypos), duration, newstate);
                    cm.setFh(fh);
                    res.add(cm);
                    break;
                }*/
                case 15: {
                    handleJumpDownMove(p, target, yOffset);
                    break;
                }
                case 21: {//Causes aran to do weird stuff when attacking o.o
                    /*byte newstate = lea.readByte();
                     short unk = lea.readShort();
                     AranMovement am = new AranMovement(command, null, unk, newstate);
                     res.add(am);*/
                    p.skip(3);
                    break;
                }
                default:
                    log.warn("Unhandled Case: {}", command);
                    throw new EmptyMovementException(p);
            }
        }
    }

    /**
     * 处理瞬移动作（3/4）：同步坐标并在玩家对象上记录传送前后坐标。
     */
    private static void handleTeleportMove(InPacket p, AnimatedMapObject target, int yOffset) {
        Point beforePos = snapshotPosition(target);
        Point afterPos = readPositionWithOffset(p, yOffset);
        p.skip(4); // xwobble / ywobble
        byte newstate = p.readByte();
        applyPositionAndStance(target, afterPos, newstate);

        // 仅玩家记录“瞬移前后坐标”，供攻击距离双坐标校验使用
        if (target instanceof Character chr) {
            chr.markTeleportLikeMove(beforePos, afterPos);
        }
    }

    /**
     * 处理突进类位移（7/8/9）：同步服务端坐标，降低位移后首包攻击误判概率。
     */
    private static void handleDashLikeMove(InPacket p, AnimatedMapObject target, int yOffset) {
        Point afterPos = readPositionWithOffset(p, yOffset);
        p.skip(4); // xwobble / ywobble
        byte newstate = p.readByte();
        applyPositionAndStance(target, afterPos, newstate);
    }

    /**
     * 椅子动作（11）：保持原逻辑，仅同步姿态不覆盖坐标。
     */
    private static void handleChairMove(InPacket p, AnimatedMapObject target) {
        p.skip(8); // xpos / ypos / xwobble / ywobble
        byte newstate = p.readByte();
        target.setStance(newstate);
    }

    /**
     * 处理 jump down（15）：同步坐标，其余字段按协议顺序跳过。
     */
    private static void handleJumpDownMove(InPacket p, AnimatedMapObject target, int yOffset) {
        Point beforePos = snapshotPosition(target);
        Point afterPos = readPositionWithOffset(p, yOffset);
        target.setPosition(afterPos);
        p.skip(8); // xwobble / ywobble / fh / ofh
        byte newstate = p.readByte();
        target.setStance(newstate);
        p.readShort(); // duration
        recordRegularMove(target, beforePos, afterPos);
    }

    private static Point snapshotPosition(AnimatedMapObject target) {
        Point currentPos = target.getPosition();
        return currentPos != null ? new Point(currentPos) : null;
    }

    private static Point readPositionWithOffset(InPacket p, int yOffset) {
        short xpos = p.readShort();
        short ypos = p.readShort();
        return new Point(xpos, ypos + yOffset);
    }

    private static void applyPositionAndStance(AnimatedMapObject target, Point position, byte newstate) {
        target.setPosition(position);
        target.setStance(newstate);
    }

    private static void recordRegularMove(AnimatedMapObject target, Point beforePos, Point afterPos) {
        if (target instanceof Character chr) {
            chr.markRegularMove(beforePos, afterPos);
        }
    }
}
