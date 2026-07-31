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

package org.gms.server.partyquest;

import org.gms.client.Character;
import org.gms.constants.id.ItemId;
import org.gms.constants.id.MapId;
import org.gms.net.server.world.Party;
import org.gms.server.ItemInformationProvider;
import org.gms.server.TimerManager;
import org.gms.server.quest.Quest;
import org.gms.util.PacketCreator;

import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author kevintjuh93
 */
public class Pyramid extends PartyQuest {
    private static final int PROTECTOR_OF_PHARAOH_QUEST = 29932;
    private static final int PROTECTOR_OF_PHARAOH_INFO = 7760;
    private static final int PROTECTOR_OF_PHARAOH_NPC = 9000066;
    private static final int PROTECTOR_OF_PHARAOH_REQUIRED_KILLS = 50000;

    public enum PyramidMode {
        EASY(0), NORMAL(1), HARD(2), HELL(3);
        int mode;

        PyramidMode(int mode) {
            this.mode = mode;
        }

        public int getMode() {
            return mode;
        }
    }

    int kill = 0, miss = 0, cool = 0, exp = 0, map, count, skill = 0;
    byte coolAdd = 5, missSub = 4, decrease = 1;//hmmm
    short gauge;
    byte rank, stage = 0, buffcount = 0;//buffcount includes buffs + skills
    PyramidMode mode;

    ScheduledFuture<?> timer = null;
    ScheduledFuture<?> gaugeSchedule = null;

    public Pyramid(Party party, PyramidMode mode, int mapid) {
        super(party);
        this.mode = mode;
        this.map = mapid;
        for (Character chr : getParticipants()) {
            chr.setPartyQuest(this);
        }

        byte plus = (byte) mode.getMode();
        coolAdd += plus;
        missSub += plus;
        switch (plus) {
            case 0:
                decrease = 1;
                break;
            case 1:
            case 2:
                decrease = 2;
                break;
            case 3:
                decrease = 3;
                break;
        }
    }

    public void startGaugeSchedule() {
        if (gaugeSchedule == null) {
            gauge = 100;
            count = 0;
            gaugeSchedule = TimerManager.getInstance().register(() -> {
                gauge -= decrease;
                if (gauge <= 0) {
                    warp(MapId.NETTS_PYRAMID);
                }

            }, 1000);
        }
    }

    public void kill() {
        kill++;
        recordProtectorOfPharaohKill();
        if (gauge < 100) {
            count++;
        }
        gauge++;
        broadcastInfo("hit", kill);
        if (gauge >= 100) {
            gauge = 100;
        }
        checkBuffs();
    }

    public void cool() {
        cool++;
        recordProtectorOfPharaohKill();
        int plus = coolAdd;
        if ((gauge + coolAdd) > 100) {
            plus -= ((gauge + coolAdd) - 100);
        }
        gauge += plus;
        count += plus;
        if (gauge >= 100) {
            gauge = 100;
        }
        broadcastInfo("cool", cool);
        checkBuffs();

    }

    public void miss() {
        miss++;
        count -= missSub;
        gauge -= missSub;
        broadcastInfo("miss", miss);
    }

    private void recordProtectorOfPharaohKill() {
        for (Character chr : getParticipants()) {
            if (chr.getQuestStatus(PROTECTOR_OF_PHARAOH_QUEST) == 2) {
                continue;
            }
            if (chr.getQuestStatus(PROTECTOR_OF_PHARAOH_QUEST) != 1) {
                Quest.getInstance(PROTECTOR_OF_PHARAOH_QUEST).forceStart(chr, PROTECTOR_OF_PHARAOH_NPC);
            }

            int progress;
            try {
                progress = Integer.parseInt(chr.getQuest(Quest.getInstance(PROTECTOR_OF_PHARAOH_INFO)).getProgress(0));
            } catch (NumberFormatException nfe) {
                progress = 0;
            }
            if (progress < PROTECTOR_OF_PHARAOH_REQUIRED_KILLS) {
                chr.setQuestProgress(PROTECTOR_OF_PHARAOH_QUEST, PROTECTOR_OF_PHARAOH_INFO, Integer.toString(progress + 1));
            }
        }
    }

    public int timer() {
        stopTimer();

        int value;
        if (stage > 0) {
            value = 180;
        } else {
            value = 120;
        }

        timer = TimerManager.getInstance().schedule(() -> {
            byte nextStage = (byte) (stage + 1);
            if (nextStage >= 5) {
                stage = 5;
                warp(MapId.NETTS_PYRAMID);
            } else {
                warp(map + (nextStage * 100));//Should work :D
            }
        }, SECONDS.toMillis(value));//, 4000
        broadcastInfo("party", getParticipants().size() > 1 ? 1 : 0);
        broadcastInfo("hit", kill);
        broadcastInfo("miss", miss);
        broadcastInfo("cool", cool);
        broadcastInfo("skill", skill);
        broadcastInfo("laststage", stage);
        startGaugeSchedule();
        for (Character chr : getParticipants()) {
            chr.sendPacket(PacketCreator.getClock(value));
        }
        return value;
    }

    public void warp(int mapid) {
        stopTimer();
        int stageOffset = mapid - map;
        boolean nextStageMap = stageOffset >= 0 && stageOffset <= 400 && stageOffset % 100 == 0;
        for (Character chr : getParticipants()) {
            chr.changeMap(mapid, 0);
            if (!nextStageMap) {
                chr.setPartyQuest(null);
            }
        }
        if (nextStageMap) {
            stage = (byte) (stageOffset / 100);
            timer();
        } else {
            stopGaugeSchedule();
        }
    }

    public void leave(int mapid) {
        stopTimer();
        stopGaugeSchedule();
        for (Character chr : getParticipants()) {
            chr.setPartyQuest(null);
            chr.changeMap(mapid, 0);
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel(false);
            timer = null;
        }
    }

    private void stopGaugeSchedule() {
        if (gaugeSchedule != null) {
            gaugeSchedule.cancel(false);
            gaugeSchedule = null;
        }
    }

    public void broadcastInfo(String info, int amount) {
        for (Character chr : getParticipants()) {
            chr.sendPacket(PacketCreator.getEnergy("massacre_" + info, amount));
            chr.sendPacket(PacketCreator.pyramidGauge(count));
        }
    }

    public boolean useSkill() {
        if (skill < 1) {
            return false;
        }

        skill--;
        broadcastInfo("skill", skill);
        return true;
    }

    public void checkBuffs() {
        int total = (kill + cool);
        if (buffcount == 0 && total >= 250) {
            buffcount++;
            ItemInformationProvider ii = ItemInformationProvider.getInstance();
            for (Character chr : getParticipants()) {
                ii.getItemEffect(ItemId.PHARAOHS_BLESSING_1).applyTo(chr);
            }

        } else if (buffcount == 1 && total >= 500) {
            buffcount++;
            skill++;
            ItemInformationProvider ii = ItemInformationProvider.getInstance();
            for (Character chr : getParticipants()) {
                chr.sendPacket(PacketCreator.getEnergy("massacre_skill", skill));
                ii.getItemEffect(ItemId.PHARAOHS_BLESSING_2).applyTo(chr);
            }
        } else if (buffcount == 2 && total >= 1000) {
            buffcount++;
            skill++;
            ItemInformationProvider ii = ItemInformationProvider.getInstance();
            for (Character chr : getParticipants()) {
                chr.sendPacket(PacketCreator.getEnergy("massacre_skill", skill));
                ii.getItemEffect(ItemId.PHARAOHS_BLESSING_3).applyTo(chr);
            }
        } else if (buffcount == 3 && total >= 1500) {
            buffcount++;
            skill++;
            broadcastInfo("skill", skill);
        } else if (buffcount == 4 && total >= 2000) {
            buffcount++;
            skill++;
            ItemInformationProvider ii = ItemInformationProvider.getInstance();
            for (Character chr : getParticipants()) {
                chr.sendPacket(PacketCreator.getEnergy("massacre_skill", skill));
                ii.getItemEffect(ItemId.PHARAOHS_BLESSING_4).applyTo(chr);
            }
        } else if (buffcount == 5 && total >= 2500) {
            buffcount++;
            skill++;
            broadcastInfo("skill", skill);
        } else if (buffcount == 6 && total >= 3000) {
            buffcount++;
            skill++;
            broadcastInfo("skill", skill);
        }
    }

    public void sendScore(Character chr) {
        if (exp == 0) {
            int totalkills = (kill + cool);
            if (stage == 5) {
                if (totalkills >= 3000) {
                    rank = 0;
                } else if (totalkills >= 2000) {
                    rank = 1;
                } else if (totalkills >= 1500) {
                    rank = 2;
                } else if (totalkills >= 500) {
                    rank = 3;
                } else {
                    rank = 4;
                }
            } else {
                if (totalkills >= 2000) {
                    rank = 3;
                } else {
                    rank = 4;
                }
            }

            if (rank == 0) {
                exp = (60500 + (5500 * mode.getMode()));
            } else if (rank == 1) {
                exp = (55000 + (5000 * mode.getMode()));
            } else if (rank == 2) {
                exp = (46750 + (4250 * mode.getMode()));
            } else if (rank == 3) {
                exp = (22000 + (2000 * mode.getMode()));
            }

            exp += ((kill * 2) + (cool * 10));
        }
        chr.sendPacket(PacketCreator.pyramidScore(rank, exp));
        chr.gainExp(exp, true, true);
    }
}


