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

/* @Author Lerk
 * Armor Statue - Sharenian: Hall of the Knight (990000400)
 * Guild Quest Stage 2 Info
 */

function start() {
    cm.sendOk("这块牌匾的翻译如下：\r\n“沙连尼亚的骑士是自豪的战士。他们的朗基努斯长矛既是强大的武器，也是城堡防御的关键：通过从大厅最高处的平台上移除它们，他们可以封锁入侵者的入口。”\r\n似乎有些东西用英文刻在侧面上，几乎看不清：\r\n“邪恶偷走了长矛，被锁在障碍物后面。返回到塔顶。大长矛，从更高处抓取。”\r\n……显然，找到答案的人没有太多时间活下去。可怜的家伙。");
    cm.dispose();
}