package org.gms.server.life;

import org.gms.client.Character;

public interface MonsterListener {

    void monsterKilled(int aniTime);
    void monsterDamaged(Character from, int trueDmg);
    void monsterHealed(int trueHeal);
}
