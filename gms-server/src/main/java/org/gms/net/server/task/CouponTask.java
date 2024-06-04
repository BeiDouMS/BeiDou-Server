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
package org.gms.net.server.task;

import org.gms.net.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ronan
 * @info Thread responsible for maintaining coupons EXP & DROP effects active
 */
public class CouponTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(CouponTask.class);

    @Override
    public void run() {
        try {
            Server.getInstance().updateActiveCoupons();
            Server.getInstance().commitActiveCoupons();
        } catch (Exception e) {
            log.error("Error updating coupon effects", e);
        }
    }
}
