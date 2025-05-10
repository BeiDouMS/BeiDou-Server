/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
function enter(pi) {
    const exitStatus = pi.getEventInstance().getIntProperty("canLeave");

    switch(exitStatus) {
        case 0: // 限制离开状态
            pi.message("请等待1分钟后再尝试离开该区域");
            return false;

        case 2: // 特殊传送条件
            pi.playPortalSound();
            pi.warp(912010200); // 传送到特殊地图
            return true;

        default: // 常规传送
            pi.playPortalSound();
            pi.warp(120000101); // 返回主城
            return true;
    }
}