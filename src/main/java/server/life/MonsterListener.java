package server.life;
import client.MapleCharacter;

public interface MonsterListener {
    
    void monsterKilled(int aniTime);
    void monsterDamaged(MapleCharacter from, int trueDmg);
    void monsterHealed(int trueHeal);
}
