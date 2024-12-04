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
package org.gms.client;

import lombok.Getter;
import lombok.Setter;
import org.gms.config.GameConfig;
import org.gms.constants.game.GameConstants;
import org.gms.server.maps.AbstractAnimatedMapObject;
import org.gms.server.maps.MapleMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author RonanLana
 */
public abstract class AbstractCharacterObject extends AbstractAnimatedMapObject {
    @Setter
    @Getter
    protected MapleMap map;
    protected int attrStr;
    protected int attrDex;
    protected int attrLuk;
    protected int attrInt;
    protected int hp;
    protected int maxHp;
    protected int mp;
    protected int maxMp;
    @Setter
    protected int hpMpApUsed;
    @Setter
    protected int remainingAp;
    protected int[] remainingSp = new int[10];
    @Getter
    protected transient int clientMaxHp;
    @Getter
    protected transient int clientMaxMp;
    protected transient int localMaxHp = 50;
    protected transient int localMaxMp = 5;
    protected float transientHp = Float.NEGATIVE_INFINITY;
    protected float transientMp = Float.NEGATIVE_INFINITY;

    private AbstractCharacterListener listener = null;
    protected Map<Stat, Integer> statUpdates = new HashMap<>();

    protected final Lock effLock = new ReentrantLock(true);
    protected final Lock statRlock;
    protected final Lock statWlock;

    protected AbstractCharacterObject() {
        ReadWriteLock statLock = new ReentrantReadWriteLock(true);
        this.statRlock = statLock.readLock();
        this.statWlock = statLock.writeLock();
        Arrays.fill(remainingSp, 0);
    }

    protected void setListener(AbstractCharacterListener listener) {
        this.listener = listener;
    }

    public int getStr() {
        statRlock.lock();
        try {
            return attrStr;
        } finally {
            statRlock.unlock();
        }
    }

    public int getDex() {
        statRlock.lock();
        try {
            return attrDex;
        } finally {
            statRlock.unlock();
        }
    }

    public int getInt() {
        statRlock.lock();
        try {
            return attrInt;
        } finally {
            statRlock.unlock();
        }
    }

    public int getLuk() {
        statRlock.lock();
        try {
            return attrLuk;
        } finally {
            statRlock.unlock();
        }
    }

    public int getRemainingAp() {
        statRlock.lock();
        try {
            return remainingAp;
        } finally {
            statRlock.unlock();
        }
    }

    protected int getRemainingSp(int jobid) {
        statRlock.lock();
        try {
            return remainingSp[GameConstants.getSkillBook(jobid)];
        } finally {
            statRlock.unlock();
        }
    }

    public int[] getRemainingSps() {
        statRlock.lock();
        try {
            return Arrays.copyOf(remainingSp, remainingSp.length);
        } finally {
            statRlock.unlock();
        }
    }

    public int getHpMpApUsed() {
        statRlock.lock();
        try {
            return hpMpApUsed;
        } finally {
            statRlock.unlock();
        }
    }

    public boolean isAlive() {
        statRlock.lock();
        try {
            return hp > 0;
        } finally {
            statRlock.unlock();
        }
    }

    public int getHp() {
        statRlock.lock();
        try {
            return hp;
        } finally {
            statRlock.unlock();
        }
    }

    public int getMp() {
        statRlock.lock();
        try {
            return mp;
        } finally {
            statRlock.unlock();
        }
    }

    public int getMaxHp() {
        statRlock.lock();
        try {
            return maxHp;
        } finally {
            statRlock.unlock();
        }
    }

    public int getMaxMp() {
        statRlock.lock();
        try {
            return maxMp;
        } finally {
            statRlock.unlock();
        }
    }

    public int getCurrentMaxHp() {
        return localMaxHp;
    }

    public int getCurrentMaxMp() {
        return localMaxMp;
    }

    private void dispatchHpChanged(final int oldHp) {
        listener.onHpChanged(oldHp);
    }

    private void dispatchHpMpPoolUpdated() {
        listener.onHpMpPoolUpdate();
    }

    private void dispatchStatUpdated() {
        listener.onStatUpdate();
    }

    private void dispatchStatPoolUpdateAnnounced() {
        listener.onAnnounceStatPoolUpdate();
    }

    protected void setHp(int newHp) {
        int oldHp = hp;

        int thp = newHp;
        if (thp < 0) {
            thp = 0;
        } else if (thp > localMaxHp) {
            thp = localMaxHp;
        }

        if (this.hp != thp) {
            this.transientHp = Float.NEGATIVE_INFINITY;
        }
        this.hp = thp;

        dispatchHpChanged(oldHp);
    }

    protected void setMp(int newMp) {
        int tmp = newMp;
        if (tmp < 0) {
            tmp = 0;
        } else if (tmp > localMaxMp) {
            tmp = localMaxMp;
        }

        if (this.mp != tmp) {
            this.transientMp = Float.NEGATIVE_INFINITY;
        }
        this.mp = tmp;
    }

    public void setRemainingSp(int remainingSp, int skillbook) {
        this.remainingSp[skillbook] = remainingSp;
    }

    protected void setMaxHp(int hp_) {
        if (this.maxHp < hp_) {
            this.transientHp = Float.NEGATIVE_INFINITY;
        }
        this.maxHp = hp_;
        this.clientMaxHp = Math.min(30000, hp_);
    }

    protected void setMaxMp(int mp_) {
        if (this.maxMp < mp_) {
            this.transientMp = Float.NEGATIVE_INFINITY;
        }
        this.maxMp = mp_;
        this.clientMaxMp = Math.min(30000, mp_);
    }

    private static long clampStat(int v, int min, int max) {
        return (v < min) ? min : ((v > max) ? max : v);
    }

    private static long calcStatPoolNode(Integer v, int displacement) {
        long r;
        if (v == null) {
            r = -32768;
        } else {
            r = clampStat(v, -32767, 32767);
        }

        return ((r & 0x0FFFF) << displacement);
    }

    private static long calcStatPoolLong(Integer v1, Integer v2, Integer v3, Integer v4) {
        long ret = 0;

        ret |= calcStatPoolNode(v1, 48);
        ret |= calcStatPoolNode(v2, 32);
        ret |= calcStatPoolNode(v3, 16);
        ret |= calcStatPoolNode(v4, 0);

        return ret;
    }

    private void changeStatPool(Long hpMpPool, Long strDexIntLuk, Long newSp, int newAp, boolean silent) {
        effLock.lock();
        statWlock.lock();
        try {
            statUpdates.clear();
            boolean poolUpdate = false;
            boolean statUpdate = false;

            if (hpMpPool != null) {
                short newHp = (short) (hpMpPool >> 48);
                short newMp = (short) (hpMpPool >> 32);
                short newMaxHp = (short) (hpMpPool >> 16);
                short newMaxMp = hpMpPool.shortValue();

                if (newMaxHp != Short.MIN_VALUE) {
                    if (newMaxHp < 50) {
                        newMaxHp = 50;
                    }

                    poolUpdate = true;
                    setMaxHp(newMaxHp);
                    statUpdates.put(Stat.MAXHP, clientMaxHp);
                    statUpdates.put(Stat.HP, hp);
                }

                if (newHp != Short.MIN_VALUE) {
                    setHp(newHp);
                    statUpdates.put(Stat.HP, hp);
                }

                if (newMaxMp != Short.MIN_VALUE) {
                    if (newMaxMp < 5) {
                        newMaxMp = 5;
                    }

                    poolUpdate = true;
                    setMaxMp(newMaxMp);
                    statUpdates.put(Stat.MAXMP, clientMaxMp);
                    statUpdates.put(Stat.MP, mp);
                }

                if (newMp != Short.MIN_VALUE) {
                    setMp(newMp);
                    statUpdates.put(Stat.MP, mp);
                }
            }

            if (strDexIntLuk != null) {
                short newStr = (short) (strDexIntLuk >> 48);
                short newDex = (short) (strDexIntLuk >> 32);
                short newInt = (short) (strDexIntLuk >> 16);
                short newLuk = strDexIntLuk.shortValue();

                if (newStr >= 4) {
                    setStr(newStr);
                    statUpdates.put(Stat.STR, attrStr);
                }

                if (newDex >= 4) {
                    setDex(newDex);
                    statUpdates.put(Stat.DEX, attrDex);
                }

                if (newInt >= 4) {
                    setInt(newInt);
                    statUpdates.put(Stat.INT, attrInt);
                }

                if (newLuk >= 4) {
                    setLuk(newLuk);
                    statUpdates.put(Stat.LUK, attrLuk);
                }

                if (newAp >= 0) {
                    setRemainingAp(newAp);
                    statUpdates.put(Stat.AVAILABLEAP, remainingAp);
                }

                statUpdate = true;
            }

            if (newSp != null) {
                short sp = (short) (newSp >> 16);
                short skillbook = newSp.shortValue();

                setRemainingSp(sp, skillbook);
                statUpdates.put(Stat.AVAILABLESP, remainingSp[skillbook]);
            }

            if (!statUpdates.isEmpty()) {
                if (poolUpdate) {
                    dispatchHpMpPoolUpdated();
                }

                if (statUpdate) {
                    dispatchStatUpdated();
                }

                if (!silent) {
                    dispatchStatPoolUpdateAnnounced();
                }
            }
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    public void healHpMp() {
        updateHpMp(30000);
    }

    public void updateHpMp(int x) {
        updateHpMp(x, x);
    }

    public void updateHpMp(int newhp, int newmp) {
        changeHpMp(newhp, newmp, false);
    }

    public void changeHpMp(int newhp, int newmp, boolean silent) {
        changeHpMpPool(newhp, newmp, null, null, silent);
    }

    private void changeHpMpPool(Integer hp, Integer mp, Integer maxhp, Integer maxmp, boolean silent) {
        long hpMpPool = calcStatPoolLong(hp, mp, maxhp, maxmp);
        changeStatPool(hpMpPool, null, null, -1, silent);
    }

    public void updateHp(int hp) {
        updateHpMaxHp(hp, null);
    }

    public void updateMaxHp(int maxhp) {
        updateHpMaxHp(null, maxhp);
    }

    public void updateHpMaxHp(int hp, int maxhp) {
        updateHpMaxHp(Integer.valueOf(hp), Integer.valueOf(maxhp));
    }

    private void updateHpMaxHp(Integer hp, Integer maxhp) {
        changeHpMpPool(hp, null, maxhp, null, false);
    }

    public void updateMp(int mp) {
        updateMpMaxMp(mp, null);
    }

    public void updateMaxMp(int maxmp) {
        updateMpMaxMp(null, maxmp);
    }

    public void updateMpMaxMp(int mp, int maxmp) {
        updateMpMaxMp(Integer.valueOf(mp), Integer.valueOf(maxmp));
    }

    private void updateMpMaxMp(Integer mp, Integer maxmp) {
        changeHpMpPool(null, mp, null, maxmp, false);
    }

    public void updateMaxHpMaxMp(int maxhp, int maxmp) {
        changeHpMpPool(null, null, maxhp, maxmp, false);
    }

    protected void enforceMaxHpMp() {
        effLock.lock();
        statWlock.lock();
        try {
            if (mp > localMaxMp || hp > localMaxHp) {
                changeHpMp(hp, mp, false);
            }
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    public int safeAddHP(int delta) {
        effLock.lock();
        statWlock.lock();
        try {
            if (hp + delta <= 0) {
                delta = -hp + 1;
            }

            addHP(delta);
            return delta;
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    public void addHP(int delta) {
        effLock.lock();
        statWlock.lock();
        try {
            updateHp(hp + delta);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    public void addMP(int delta) {
        effLock.lock();
        statWlock.lock();
        try {
            updateMp(mp + delta);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    public void addMPHP(int hpDelta, int mpDelta) {
        effLock.lock();
        statWlock.lock();
        try {
            updateHpMp(hp + hpDelta, mp + mpDelta);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    protected void addMaxMPMaxHP(int hpdelta, int mpdelta, boolean silent) {
        effLock.lock();
        statWlock.lock();
        try {
            changeHpMpPool(null, null, maxHp + hpdelta, maxMp + mpdelta, silent);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    public void addMaxHP(int delta) {
        effLock.lock();
        statWlock.lock();
        try {
            updateMaxHp(maxHp + delta);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    public void addMaxMP(int delta) {
        effLock.lock();
        statWlock.lock();
        try {
            updateMaxMp(maxMp + delta);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    public void setStr(int str) {
        this.attrStr = str;
    }

    public void setDex(int dex) {
        this.attrDex = dex;
    }

    public void setInt(int int_) {
        this.attrInt = int_;
    }

    public void setLuk(int luk) {
        this.attrLuk = luk;
    }

    public boolean assignStr(int x) {
        return assignStrDexIntLuk(x, null, null, null);
    }

    public boolean assignDex(int x) {
        return assignStrDexIntLuk(null, x, null, null);
    }

    public boolean assignInt(int x) {
        return assignStrDexIntLuk(null, null, x, null);
    }

    public boolean assignLuk(int x) {
        return assignStrDexIntLuk(null, null, null, x);
    }

    public boolean assignHP(int deltaHP, int deltaAp) {
        effLock.lock();
        statWlock.lock();
        try {
            if (remainingAp - deltaAp < 0 || hpMpApUsed + deltaAp < 0 || maxHp >= 30000) {
                return false;
            }

            long hpMpPool = calcStatPoolLong(null, null, maxHp + deltaHP, maxMp);
            long strDexIntLuk = calcStatPoolLong(attrStr, attrDex, attrInt, attrLuk);

            changeStatPool(hpMpPool, strDexIntLuk, null, remainingAp - deltaAp, false);
            setHpMpApUsed(hpMpApUsed + deltaAp);
            return true;
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    public boolean assignMP(int deltaMP, int deltaAp) {
        effLock.lock();
        statWlock.lock();
        try {
            if (remainingAp - deltaAp < 0 || hpMpApUsed + deltaAp < 0 || maxMp >= 30000) {
                return false;
            }

            long hpMpPool = calcStatPoolLong(null, null, maxHp, maxMp + deltaMP);
            long strDexIntLuk = calcStatPoolLong(attrStr, attrDex, attrInt, attrLuk);

            changeStatPool(hpMpPool, strDexIntLuk, null, remainingAp - deltaAp, false);
            setHpMpApUsed(hpMpApUsed + deltaAp);
            return true;
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    private static int apAssigned(Integer x) {
        return x != null ? x : 0;
    }

    public boolean assignStrDexIntLuk(int deltaStr, int deltaDex, int deltaInt, int deltaLuk) {
        return assignStrDexIntLuk(Integer.valueOf(deltaStr), Integer.valueOf(deltaDex), Integer.valueOf(deltaInt), Integer.valueOf(deltaLuk));
    }

    private boolean assignStrDexIntLuk(Integer deltaStr, Integer deltaDex, Integer deltaInt, Integer deltaLuk) {
        effLock.lock();
        statWlock.lock();
        try {
            int apUsed = apAssigned(deltaStr) + apAssigned(deltaDex) + apAssigned(deltaInt) + apAssigned(deltaLuk);
            if (apUsed > remainingAp) {
                return false;
            }

            int newStr = attrStr, newDex = attrDex, newInt = attrInt, newLuk = attrLuk;
            if (deltaStr != null) {
                newStr += deltaStr;   // thanks Rohenn for noticing an NPE case after "null" started being used
            }
            if (deltaDex != null) {
                newDex += deltaDex;
            }
            if (deltaInt != null) {
                newInt += deltaInt;
            }
            if (deltaLuk != null) {
                newLuk += deltaLuk;
            }

            if (newStr < 4 || newStr > GameConfig.getServerInt("max_ap")) {
                return false;
            }

            if (newDex < 4 || newDex > GameConfig.getServerInt("max_ap")) {
                return false;
            }

            if (newInt < 4 || newInt > GameConfig.getServerInt("max_ap")) {
                return false;
            }

            if (newLuk < 4 || newLuk > GameConfig.getServerInt("max_ap")) {
                return false;
            }

            int newAp = remainingAp - apUsed;
            updateStrDexIntLuk(newStr, newDex, newInt, newLuk, newAp);
            return true;
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    public void updateStrDexIntLuk(int x) {
        updateStrDexIntLuk(x, x, x, x, -1);
    }

    public void changeRemainingAp(int x, boolean silent) {
        effLock.lock();
        statWlock.lock();
        try {
            changeStrDexIntLuk(attrStr, attrDex, attrInt, attrLuk, x, silent);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    public void gainAp(int deltaAp, boolean silent) {
        effLock.lock();
        statWlock.lock();
        try {
            changeRemainingAp(Math.max(0, remainingAp + deltaAp), silent);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    protected void updateStrDexIntLuk(int str, int dex, int int_, int luk, int remainingAp) {
        changeStrDexIntLuk(str, dex, int_, luk, remainingAp, false);
    }

    private void changeStrDexIntLuk(Integer str, Integer dex, Integer int_, Integer luk, int remainingAp, boolean silent) {
        long strDexIntLuk = calcStatPoolLong(str, dex, int_, luk);
        changeStatPool(null, strDexIntLuk, null, remainingAp, silent);
    }

    private void changeStrDexIntLukSp(Integer str, Integer dex, Integer int_, Integer luk, int remainingAp, int remainingSp, int skillbook, boolean silent) {
        long strDexIntLuk = calcStatPoolLong(str, dex, int_, luk);
        long sp = calcStatPoolLong(0, 0, remainingSp, skillbook);
        changeStatPool(null, strDexIntLuk, sp, remainingAp, silent);
    }

    protected void updateStrDexIntLukSp(int str, int dex, int int_, int luk, int remainingAp, int remainingSp, int skillbook) {
        changeStrDexIntLukSp(str, dex, int_, luk, remainingAp, remainingSp, skillbook, false);
    }

    protected void setRemainingSp(int[] sps) {
        effLock.lock();
        statWlock.lock();
        try {
            System.arraycopy(sps, 0, remainingSp, 0, sps.length);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }

    protected void updateRemainingSp(int remainingSp, int skillbook) {
        changeRemainingSp(remainingSp, skillbook, false);
    }

    protected void changeRemainingSp(int remainingSp, int skillbook, boolean silent) {
        long sp = calcStatPoolLong(0, 0, remainingSp, skillbook);
        changeStatPool(null, null, sp, Short.MIN_VALUE, silent);
    }

    public void gainSp(int deltaSp, int skillbook, boolean silent) {
        effLock.lock();
        statWlock.lock();
        try {
            changeRemainingSp(Math.max(0, remainingSp[skillbook] + deltaSp), skillbook, silent);
        } finally {
            statWlock.unlock();
            effLock.unlock();
        }
    }
}
