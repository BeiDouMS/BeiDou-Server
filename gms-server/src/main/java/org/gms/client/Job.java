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
package org.gms.client;

import lombok.Getter;
import org.gms.util.I18nUtil;


public enum Job {
    BEGINNER(0, I18nUtil.getMessage("job.name.0")),
    WARRIOR(100, I18nUtil.getMessage("job.name.100")),
    FIGHTER(110, I18nUtil.getMessage("job.name.110")), CRUSADER(111, I18nUtil.getMessage("job.name.111")), HERO(112, I18nUtil.getMessage("job.name.112")),
    PAGE(120, I18nUtil.getMessage("job.name.120")), WHITEKNIGHT(121, I18nUtil.getMessage("job.name.121")), PALADIN(122,  I18nUtil.getMessage("job.name.122")),
    SPEARMAN(130,  I18nUtil.getMessage("job.name.130")), DRAGONKNIGHT(131,  I18nUtil.getMessage("job.name.131")), DARKKNIGHT(132, I18nUtil.getMessage("job.name.132")),

    MAGICIAN(200, I18nUtil.getMessage("job.name.200")),
    FP_WIZARD(210, I18nUtil.getMessage("job.name.210")), FP_MAGE(211, I18nUtil.getMessage("job.name.211")), FP_ARCHMAGE(212, I18nUtil.getMessage("job.name.212")),
    IL_WIZARD(220, I18nUtil.getMessage("job.name.220")), IL_MAGE(221, I18nUtil.getMessage("job.name.221")), IL_ARCHMAGE(222, I18nUtil.getMessage("job.name.222")),
    CLERIC(230, I18nUtil.getMessage("job.name.230")), PRIEST(231, I18nUtil.getMessage("job.name.231")), BISHOP(232, I18nUtil.getMessage("job.name.232")),

    BOWMAN(300, I18nUtil.getMessage("job.name.300")),
    HUNTER(310, I18nUtil.getMessage("job.name.310")), RANGER(311, I18nUtil.getMessage("job.name.311")), BOWMASTER(312, I18nUtil.getMessage("job.name.312")),
    CROSSBOWMAN(320, I18nUtil.getMessage("job.name.320")), SNIPER(321, I18nUtil.getMessage("job.name.321")), MARKSMAN(322, I18nUtil.getMessage("job.name.322")),

    THIEF(400, I18nUtil.getMessage("job.name.400")),
    ASSASSIN(410,I18nUtil.getMessage("job.name.410")), HERMIT(411, I18nUtil.getMessage("job.name.411")), NIGHTLORD(412, I18nUtil.getMessage("job.name.412")),
    BANDIT(420, I18nUtil.getMessage("job.name.420")), CHIEFBANDIT(421, I18nUtil.getMessage("job.name.421")), SHADOWER(422, I18nUtil.getMessage("job.name.422")),

    PIRATE(500, I18nUtil.getMessage("job.name.500")),
    BRAWLER(510, I18nUtil.getMessage("job.name.510")), MARAUDER(511, I18nUtil.getMessage("job.name.511")), BUCCANEER(512, I18nUtil.getMessage("job.name.512")),
    GUNSLINGER(520, I18nUtil.getMessage("job.name.520")), OUTLAW(521, I18nUtil.getMessage("job.name.521")), CORSAIR(522, I18nUtil.getMessage("job.name.522")),

    MAPLELEAF_BRIGADIER(800, I18nUtil.getMessage("job.name.800")),
    GM(900, I18nUtil.getMessage("job.name.900")), SUPERGM(910, I18nUtil.getMessage("job.name.910")),

    NOBLESSE(1000, I18nUtil.getMessage("job.name.1000")),
    DAWNWARRIOR1(1100, I18nUtil.getMessage("job.name.1100")), DAWNWARRIOR2(1110, I18nUtil.getMessage("job.name.1110")), DAWNWARRIOR3(1111, I18nUtil.getMessage("job.name.1111")), DAWNWARRIOR4(1112, I18nUtil.getMessage("job.name.1112")),
    BLAZEWIZARD1(1200, I18nUtil.getMessage("job.name.1200")), BLAZEWIZARD2(1210, I18nUtil.getMessage("job.name.1210")), BLAZEWIZARD3(1211,I18nUtil.getMessage("job.name.1211")), BLAZEWIZARD4(1212,I18nUtil.getMessage("job.name.1212")),
    WINDARCHER1(1300,I18nUtil.getMessage("job.name.1300")), WINDARCHER2(1310, I18nUtil.getMessage("job.name.1310")), WINDARCHER3(1311, I18nUtil.getMessage("job.name.1311")), WINDARCHER4(1312, I18nUtil.getMessage("job.name.1312")),
    NIGHTWALKER1(1400,I18nUtil.getMessage("job.name.1400")), NIGHTWALKER2(1410,I18nUtil.getMessage("job.name.1410")), NIGHTWALKER3(1411,I18nUtil.getMessage("job.name.1411")), NIGHTWALKER4(1412,I18nUtil.getMessage("job.name.1412")),
    THUNDERBREAKER1(1500,I18nUtil.getMessage("job.name.1500")), THUNDERBREAKER2(1510,I18nUtil.getMessage("job.name.1510")), THUNDERBREAKER3(1511,I18nUtil.getMessage("job.name.1511")), THUNDERBREAKER4(1512,I18nUtil.getMessage("job.name.1512")),

    LEGEND(2000, I18nUtil.getMessage("job.name.2000")), EVAN(2001, I18nUtil.getMessage("job.name.2001")),
    ARAN1(2100, I18nUtil.getMessage("job.name.2100")), ARAN2(2110, I18nUtil.getMessage("job.name.2110")), ARAN3(2111, I18nUtil.getMessage("job.name.2111")), ARAN4(2112, I18nUtil.getMessage("job.name.2112")),

    EVAN1(2200,I18nUtil.getMessage("job.name.2200")), EVAN2(2210, I18nUtil.getMessage("job.name.2210")), EVAN3(2211, I18nUtil.getMessage("job.name.2211")), EVAN4(2212, I18nUtil.getMessage("job.name.2212")), EVAN5(2213, I18nUtil.getMessage("job.name.2213")), EVAN6(2214, I18nUtil.getMessage("job.name.2214")),
    EVAN7(2215, I18nUtil.getMessage("job.name.2215")), EVAN8(2216, I18nUtil.getMessage("job.name.2216")), EVAN9(2217, I18nUtil.getMessage("job.name.2217")), EVAN10(2218, I18nUtil.getMessage("job.name.2218"));

    @Getter
    private final int id;
    @Getter
    private final String name;

    final static int maxId = 22;    // maxId = (EVAN / 100);

    Job(int id, String name) {
        this.id = id;
        this.name = name;
    }


    public static int getMax() {
        return maxId;
    }

    public static Job getById(int id) {
        for (Job l : Job.values()) {
            if (l.getId() == id) {
                return l;
            }
        }
        return BEGINNER;
    }

    public static Job getBy5ByteEncoding(int encoded) {
        return switch (encoded) {
            case 2 -> WARRIOR;
            case 4 -> MAGICIAN;
            case 8 -> BOWMAN;
            case 16 -> THIEF;
            case 32 -> PIRATE;
            case 1024 -> NOBLESSE;
            case 2048 -> DAWNWARRIOR1;
            case 4096 -> BLAZEWIZARD1;
            case 8192 -> WINDARCHER1;
            case 16384 -> NIGHTWALKER1;
            case 32768 -> THUNDERBREAKER1;
            default -> BEGINNER;
        };
    }

    public boolean isA(Job basejob) {  // thanks Steve (kaito1410) for pointing out an improvement here
        int basebranch = basejob.getId() / 10;
        return (getId() / 10 == basebranch && getId() >= basejob.getId()) || (basebranch % 10 == 0 && getId() / 100 == basejob.getId() / 100);
    }

    public int getJobNiche() {
        return (id / 100) % 10;
        
        /*
        case 0: BEGINNER;
        case 1: WARRIOR;
        case 2: MAGICIAN;
        case 3: BOWMAN;  
        case 4: THIEF;
        case 5: PIRATE;
        */
    }

    public static Job getJobStyleInternal(int jobid, byte opt) {
        int jobtype = jobid / 100;

        if (jobtype == WARRIOR.getId() / 100 || jobtype == DAWNWARRIOR1.getId() / 100 || jobtype == ARAN1.getId() / 100) {
            return WARRIOR;
        } else if (jobtype == MAGICIAN.getId() / 100 || jobtype == BLAZEWIZARD1.getId() / 100 || jobtype == EVAN1.getId() / 100) {
            return MAGICIAN;
        } else if (jobtype == BOWMAN.getId() / 100 || jobtype == WINDARCHER1.getId() / 100) {
            if (jobid / 10 == CROSSBOWMAN.getId() / 10) {
                return CROSSBOWMAN;
            } else {
                return BOWMAN;
            }
        } else if (jobtype == THIEF.getId() / 100 || jobtype == NIGHTWALKER1.getId() / 100) {
            return THIEF;
        } else if (jobtype == PIRATE.getId() / 100 || jobtype == THUNDERBREAKER1.getId() / 100) {
            if (opt == (byte) 0x80) {
                return BRAWLER;
            } else {
                return GUNSLINGER;
            }
        }

        return BEGINNER;
    }
}
