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
package server.life;

import client.Character;
import client.Disease;
import client.status.MonsterStatus;
import constants.id.MapId;
import constants.id.MobId;
import net.server.services.task.channel.OverallService;
import net.server.services.type.ChannelServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.maps.MapObject;
import server.maps.MapObjectType;
import server.maps.MapleMap;
import server.maps.Mist;
import tools.ArrayMap;
import tools.Randomizer;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * @author Danny (Leifde)
 */
public class MobSkill {
    private static final Logger log = LoggerFactory.getLogger(MobSkill.class);

    private final MobSkillType type;
    private final int skillLevel;
    private int mpCon;
    private final List<Integer> toSummon = new ArrayList<>();
    private int spawnEffect, hp, x, y, count;
    private long duration, cooltime;
    private float prop;
    private Point lt, rb;
    private int limit;

    public MobSkill(MobSkillType type, int level) {
        this.type = type;
        this.skillLevel = level;
    }

    public void setMpCon(int mpCon) {
        this.mpCon = mpCon;
    }

    public void addSummons(List<Integer> toSummon) {
        this.toSummon.addAll(toSummon);
    }

    public void setSpawnEffect(int spawnEffect) {
        this.spawnEffect = spawnEffect;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setCoolTime(long cooltime) {
        this.cooltime = cooltime;
    }

    public void setProp(float prop) {
        this.prop = prop;
    }

    public void setLtRb(Point lt, Point rb) {
        this.lt = lt;
        this.rb = rb;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void applyDelayedEffect(final Character player, final Monster monster, final boolean skill, int animationTime) {
        Runnable toRun = () -> {
            if (monster.isAlive()) {
                applyEffect(player, monster, skill, null);
            }
        };

        OverallService service = (OverallService) monster.getMap().getChannelServer().getServiceAccess(ChannelServices.OVERALL);
        service.registerOverallAction(monster.getMap().getId(), toRun, animationTime);
    }

    public void applyEffect(Monster monster) {
        applyEffect(null, monster, false, Collections.emptyList());
    }

    public void applyEffect(Character player, Monster monster, boolean skill, List<Character> banishPlayers) {
        Disease disease = null;
        Map<MonsterStatus, Integer> stats = new ArrayMap<>();
        List<Integer> reflection = new LinkedList<>();
        switch (type) {
            case ATTACK_UP, ATTACK_UP_M, PAD -> stats.put(MonsterStatus.WEAPON_ATTACK_UP, x);
            case MAGIC_ATTACK_UP, MAGIC_ATTACK_UP_M, MAD -> stats.put(MonsterStatus.MAGIC_ATTACK_UP, x);
            case DEFENSE_UP, DEFENSE_UP_M, PDR -> stats.put(MonsterStatus.WEAPON_DEFENSE_UP, x);
            case MAGIC_DEFENSE_UP, MAGIC_DEFENSE_UP_M, MDR -> stats.put(MonsterStatus.MAGIC_DEFENSE_UP, x);
            case HEAL_M -> {
                if (lt != null && rb != null && skill) {
                    List<MapObject> objects = getObjectsInRange(monster, MapObjectType.MONSTER);
                    final int hps = (getX() / 1000) * (int) (950 + 1050 * Math.random());
                    for (MapObject mons : objects) {
                        ((Monster) mons).heal(hps, getY());
                    }
                } else {
                    monster.heal(getX(), getY());
                }
            }
            case SEAL -> disease = Disease.SEAL;
            case DARKNESS -> disease = Disease.DARKNESS;
            case WEAKNESS -> disease = Disease.WEAKEN;
            case STUN -> disease = Disease.STUN;
            case CURSE -> disease = Disease.CURSE;
            case POISON -> disease = Disease.POISON;
            case SLOW -> disease = Disease.SLOW;
            case DISPEL -> {
                if (lt != null && rb != null && skill) {
                    for (Character character : getPlayersInRange(monster)) {
                        character.dispel();
                    }
                } else {
                    player.dispel();
                }
            }
            case SEDUCE -> disease = Disease.SEDUCE;
            case BANISH -> {
                if (lt != null && rb != null && skill) {
                    banishPlayers.addAll(getPlayersInRange(monster));
                } else {
                    banishPlayers.add(player);
                }
            }
            case AREA_POISON -> {
                monster.getMap().spawnMist(new Mist(calculateBoundingBox(monster.getPosition()), monster, this), x * 100, false, false, false);
            }
            case REVERSE_INPUT -> disease = Disease.CONFUSE;
            case UNDEAD -> disease = Disease.ZOMBIFY;
            case PHYSICAL_IMMUNE -> {
                if (makeChanceResult() && !monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) {
                    stats.put(MonsterStatus.WEAPON_IMMUNITY, x);
                }
            }
            case MAGIC_IMMUNE -> {
                if (makeChanceResult() && !monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY)) {
                    stats.put(MonsterStatus.MAGIC_IMMUNITY, x);
                }
            }
            case PHYSICAL_COUNTER -> {
                stats.put(MonsterStatus.WEAPON_REFLECT, 10);
                stats.put(MonsterStatus.WEAPON_IMMUNITY, 10);
                reflection.add(x);
            }
            case MAGIC_COUNTER -> {
                stats.put(MonsterStatus.MAGIC_REFLECT, 10);
                stats.put(MonsterStatus.MAGIC_IMMUNITY, 10);
                reflection.add(x);
            }
            case PHYSICAL_AND_MAGIC_COUNTER -> {
                stats.put(MonsterStatus.WEAPON_REFLECT, 10);
                stats.put(MonsterStatus.WEAPON_IMMUNITY, 10);
                stats.put(MonsterStatus.MAGIC_REFLECT, 10);
                stats.put(MonsterStatus.MAGIC_IMMUNITY, 10);
                reflection.add(x);
            }
            case ACC -> stats.put(MonsterStatus.ACC, x);
            case EVA -> stats.put(MonsterStatus.AVOID, x);
            case SPEED -> stats.put(MonsterStatus.SPEED, x);
            case SEAL_SKILL -> stats.put(MonsterStatus.SEAL_SKILL, x);
            case SUMMON -> {
                int skillLimit = this.getLimit();
                MapleMap map = monster.getMap();

                if (MapId.isDojo(map.getId())) {  // spawns in dojo should be unlimited
                    skillLimit = Integer.MAX_VALUE;
                }

                if (map.getSpawnedMonstersOnMap() < 80) {
                    List<Integer> summons = getSummons();
                    int summonLimit = monster.countAvailableMobSummons(summons.size(), skillLimit);
                    if (summonLimit >= 1) {
                        boolean bossRushMap = MapId.isBossRush(map.getId());

                        Collections.shuffle(summons);
                        for (Integer mobId : summons.subList(0, summonLimit)) {
                            Monster toSpawn = LifeFactory.getMonster(mobId);
                            if (toSpawn != null) {
                                if (bossRushMap) {
                                    toSpawn.disableDrops();  // no littering on BRPQ pls
                                }
                                toSpawn.setPosition(monster.getPosition());
                                int ypos, xpos;
                                xpos = (int) monster.getPosition().getX();
                                ypos = (int) monster.getPosition().getY();
                                switch (mobId) {
                                    case MobId.HIGH_DARKSTAR: // Pap bomb high
                                        toSpawn.setFh((int) Math.ceil(Math.random() * 19.0));
                                        ypos = -590;
                                        break;
                                    case MobId.LOW_DARKSTAR: // Pap bomb
                                        xpos = (int) (monster.getPosition().getX() + Randomizer.nextInt(1000) - 500);
                                        if (ypos != -590) {
                                            ypos = (int) monster.getPosition().getY();
                                        }
                                        break;
                                    case MobId.BLOODY_BOOM: //Pianus bomb
                                        if (Math.ceil(Math.random() * 5) == 1) {
                                            ypos = 78;
                                            xpos = Randomizer.nextInt(5) + (Randomizer.nextInt(2) == 1 ? 180 : 0);
                                        } else {
                                            xpos = (int) (monster.getPosition().getX() + Randomizer.nextInt(1000) - 500);
                                        }
                                        break;
                                }
                                switch (map.getId()) {
                                    case MapId.ORIGIN_OF_CLOCKTOWER: //Pap map
                                        if (xpos < -890) {
                                            xpos = (int) (Math.ceil(Math.random() * 150) - 890);
                                        } else if (xpos > 230) {
                                            xpos = (int) (230 - Math.ceil(Math.random() * 150));
                                        }
                                        break;
                                    case MapId.CAVE_OF_PIANUS: // Pianus map
                                        if (xpos < -239) {
                                            xpos = (int) (Math.ceil(Math.random() * 150) - 239);
                                        } else if (xpos > 371) {
                                            xpos = (int) (371 - Math.ceil(Math.random() * 150));
                                        }
                                        break;
                                }
                                toSpawn.setPosition(new Point(xpos, ypos));
                                if (toSpawn.getId() == MobId.LOW_DARKSTAR) {
                                    map.spawnFakeMonster(toSpawn);
                                } else {
                                    map.spawnMonsterWithEffect(toSpawn, getSpawnEffect(), toSpawn.getPosition());
                                }
                                monster.addSummonedMob(toSpawn);
                            }
                        }
                    }
                }
            }
        }
        if (stats.size() > 0) {
            if (lt != null && rb != null && skill) {
                for (MapObject mons : getObjectsInRange(monster, MapObjectType.MONSTER)) {
                    ((Monster) mons).applyMonsterBuff(stats, getX(), type.getId(), getDuration(), this, reflection);
                }
            } else {
                monster.applyMonsterBuff(stats, getX(), type.getId(), getDuration(), this, reflection);
            }
        }
        if (disease != null) {
            if (lt != null && rb != null && skill) {
                int i = 0;
                for (Character character : getPlayersInRange(monster)) {
                    if (!character.hasActiveBuff(2321005)) {  // holy shield
                        if (disease.equals(Disease.SEDUCE)) {
                            if (i < count) {
                                character.giveDebuff(Disease.SEDUCE, this);
                                i++;
                            }
                        } else {
                            character.giveDebuff(disease, this);
                        }
                    }
                }
            } else {
                player.giveDebuff(disease, this);
            }
        }
    }

    private List<Character> getPlayersInRange(Monster monster) {
        return monster.getMap().getPlayersInRange(calculateBoundingBox(monster.getPosition()));
    }

    public MobSkillType getType() {
        return type;
    }

    public int getSkillId() {
        return type.getId();
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public int getMpCon() {
        return mpCon;
    }

    public List<Integer> getSummons() {
        return new ArrayList<>(toSummon);
    }

    public int getSpawnEffect() {
        return spawnEffect;
    }

    public int getHP() {
        return hp;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public long getDuration() {
        return duration;
    }

    public long getCoolTime() {
        return cooltime;
    }

    public Point getLt() {
        return lt;
    }

    public Point getRb() {
        return rb;
    }

    public int getLimit() {
        return limit;
    }

    public boolean makeChanceResult() {
        return prop == 1.0 || Math.random() < prop;
    }

    private Rectangle calculateBoundingBox(Point posFrom) {
        Point mylt = new Point(lt.x + posFrom.x, lt.y + posFrom.y);
        Point myrb = new Point(rb.x + posFrom.x, rb.y + posFrom.y);
        Rectangle bounds = new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
        return bounds;
    }

    private List<MapObject> getObjectsInRange(Monster monster, MapObjectType objectType) {
        return monster.getMap().getMapObjectsInBox(calculateBoundingBox(monster.getPosition()), Collections.singletonList(objectType));
    }
}
