/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
    Copyleft (L) 2016 - 2019 RonanLana

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.gms.client.command.commands.gm0;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.server.life.Monster;
import org.gms.server.maps.MapObject;
import org.gms.server.maps.MapObjectType;
import org.gms.util.I18nUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Point;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MobVacCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(MobVacCommand.class);
    private static final Timer mobVacTimer = new Timer("MobVacTimer");
    private static TimerTask currentTask = null;
    private static Character currentPlayer = null;
    
    private static Point vacPosition = null;
    
    {
        setDescription(I18nUtil.getMessage("MobVacCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        
        if (currentTask != null && currentPlayer != null && currentPlayer.getId() == player.getId()) {
            // Disable
            currentTask.cancel();
            currentTask = null;
            currentPlayer = null;
            vacPosition = null;
            player.toggleMobVac();
            player.dropMessage(5, "MobVac disabled!");
            log.info("[MobVac] Disabled for player {}", player.getName());
        } else {
            // Enable - cancel any existing
            if (currentTask != null) {
                currentTask.cancel();
            }
            
            player.toggleMobVac();
            currentPlayer = player;
            
            // Save the position where mobvac was activated
            vacPosition = new Point(player.getPosition());
            
            currentTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (currentPlayer == null || !currentPlayer.isLoggedIn() || 
                            !currentPlayer.isMobVacEnabled() || currentPlayer.getMap() == null || vacPosition == null) {
                            cancel();
                            currentTask = null;
                            currentPlayer = null;
                            vacPosition = null;
                            return;
                        }
                        
                        executeMobVac(currentPlayer, vacPosition);
                    } catch (Exception e) {
                        log.error("[MobVac] Error", e);
                    }
                }
            };
            
            mobVacTimer.scheduleAtFixedRate(currentTask, 0, 2000);
            player.dropMessage(5, "MobVac enabled!");
            log.info("[MobVac] Enabled for player {} at position {}", player.getName(), vacPosition);
        }
    }
    
    private void executeMobVac(Character player, Point targetPos) {
        if (player.getMap() == null) return;
        
        // Get ALL monsters on the map
        List<MapObject> monsters = player.getMap().getMapObjectsInRange(
            targetPos, Double.POSITIVE_INFINITY, Arrays.asList(MapObjectType.MONSTER));
        
        if (monsters.isEmpty()) return;
        
        int count = 0;
        
        for (MapObject obj : monsters) {
            if (!(obj instanceof Monster)) continue;
            
            Monster monster = (Monster) obj;
            if (!monster.isAlive()) continue;
            
            monster.lockMonster();
            try {
                // Use resetMobPosition which broadcasts the position to clients
                monster.resetMobPosition(targetPos);
                // Then remove controller to stop AI movement
                monster.aggroRemoveController();
                count++;
            } catch (Exception e) {
                log.error("[MobVac] Error moving monster", e);
            } finally {
                monster.unlockMonster();
            }
        }
        
        if (count > 0) {
            player.dropMessage(5, "Holding " + count + " monsters at fixed position");
        }
    }
}
