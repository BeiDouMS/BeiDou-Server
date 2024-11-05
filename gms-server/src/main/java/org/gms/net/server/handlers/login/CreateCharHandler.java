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
package org.gms.net.server.handlers.login;

import lombok.extern.slf4j.Slf4j;
import org.gms.client.Client;
import org.gms.client.creator.novice.BeginnerCreator;
import org.gms.client.creator.novice.LegendCreator;
import org.gms.client.creator.novice.NoblesseCreator;
import org.gms.config.GameConfig;
import org.gms.constants.inventory.ItemConstants;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.util.I18nUtil;
import org.gms.util.PacketCreator;

@Slf4j
public final class CreateCharHandler extends AbstractPacketHandler {

    @Override
    public void handlePacket(InPacket p, Client c) {
        String name = p.readString();
        int job = p.readInt();
        int face = p.readInt();

        int hair = p.readInt();
        int hairColor = p.readInt();
        int skinColor = p.readInt();

        int top = p.readInt();
        int bottom = p.readInt();
        int shoes = p.readInt();
        int weapon = p.readInt();
        int gender = p.readByte();

        if (!ItemConstants.isNewCharDefaultFace(job, gender, face)) {
            log.warn("非法创建角色，使用了非默认参数 职业 {} 性别 {} 脸型 {}", job, gender, face);
            c.sendPacket(PacketCreator.deleteCharResponse(0, 9));
            return;
        }
        if (!ItemConstants.isNewCharDefaultHair(gender, hair)) {
            log.warn("非法创建角色，使用了非默认参数 性别 {} 发型 {}", gender, hair);
            c.sendPacket(PacketCreator.deleteCharResponse(0, 9));
            return;
        }
        if (!ItemConstants.isNewCharDefaultHairColor(hairColor)) {
            log.warn("非法创建角色，使用了非默认参数 发色 {}", hairColor);
            c.sendPacket(PacketCreator.deleteCharResponse(0, 9));
            return;
        }
        if (!ItemConstants.isNewCharDefaultSkinColor(skinColor)) {
            log.warn("非法创建角色，使用了非默认参数 肤色 {}", skinColor);
            c.sendPacket(PacketCreator.deleteCharResponse(0, 9));
            return;
        }
        if (!ItemConstants.isNewCharDefaultTop(job, gender, top)) {
            log.warn("非法创建角色，使用了非默认参数 职业 {} 性别 {} 衣服 {}", job, gender, top);
            c.sendPacket(PacketCreator.deleteCharResponse(0, 9));
            return;
        }
        if (!ItemConstants.isNewCharDefaultBottom(job, gender, bottom)) {
            log.warn("非法创建角色，使用了非默认参数 职业 {} 性别 {} 裙裤 {}", job, gender, bottom);
            c.sendPacket(PacketCreator.deleteCharResponse(0, 9));
            return;
        }
        if (!ItemConstants.isNewCharDefaultShoes(job, shoes)) {
            log.warn("非法创建角色，使用了非默认参数 职业 {} 鞋子 {}", job, shoes);
            c.sendPacket(PacketCreator.deleteCharResponse(0, 9));
            return;
        }
        if (!ItemConstants.isNewCharDefaultWeapon(job, weapon)) {
            log.warn("非法创建角色，使用了非默认参数 职业 {} 武器 {}", job, weapon);
            c.sendPacket(PacketCreator.deleteCharResponse(0, 9));
            return;
        }

        int status;
        /**
         * 创建角色职业
         * 将禁止创建指定职业群的判断挪到此处进行统一判断，并且向客户端发出禁止创建的提示信息
         */
        switch (job) {
            case 0: // Knights of Cygnus #骑士团
                //先判断是否禁止创建该职业，再进行角色创建
                status = !GameConfig.getServerBoolean("enable_knights_of_cygnus") ? -3 : NoblesseCreator.createCharacter(c, name, face, hair + hairColor, skinColor, top, bottom, shoes, weapon, gender);
                break;
            case 1: // Adventurer #冒险家
                status = !GameConfig.getServerBoolean("enable_adventurers") ? -3 : BeginnerCreator.createCharacter(c, name, face, hair + hairColor, skinColor, top, bottom, shoes, weapon, gender);
                break;
            case 2: // Aran #战神
                status = !GameConfig.getServerBoolean("enable_the_lord_of_war") ? -3 : LegendCreator.createCharacter(c, name, face, hair + hairColor, skinColor, top, bottom, shoes, weapon, gender);
                break;
            default:
                c.sendPacket(PacketCreator.deleteCharResponse(0, 9));
                return;
        }

        if(status == -3) {
            String jobname = I18nUtil.getMessage("CreateCharHandler.handlePacket.job." + job );
            String message = I18nUtil.getMessage("CreateCharHandler.handlePacket.serverNotice.disableJob", jobname);
            c.sendPacket(PacketCreator.serverNotice(1,message));    //由于未找到不弹窗结束客户端请求等待，所以先发出未知错误的提示，再发送弹窗提示，这样不会被未知错误窗口挡住
            c.sendPacket(PacketCreator.getLoginFailed(1));       //断开客户端请求，避免客户端假死
        } else if(status != 0) {
            c.sendPacket(PacketCreator.deleteCharResponse(0, 9));       //发送未知错误的弹窗提示
        }
    }
}