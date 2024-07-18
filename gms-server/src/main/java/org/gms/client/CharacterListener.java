package org.gms.client;

import org.gms.util.PacketCreator;
import org.gms.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CharacterListener implements AbstractCharacterListener {
    private final Character character;
    public CharacterListener(Character character) {
        this.character = character;
    }

    @Override
    public void onHpChanged(int oldHp) {
        character.hpChangeAction(oldHp);
    }

    @Override
    public void onHpMpPoolUpdate() {
        List<Pair<Stat, Integer>> hpmpupdate = character.recalcLocalStats();
        for (Pair<Stat, Integer> p : hpmpupdate) {
            character.statUpdates.put(p.getLeft(), p.getRight());
        }

        if (character.hp > character.localMaxHp) {
            character.setHp(character.localMaxHp);
            character.statUpdates.put(Stat.HP, character.hp);
        }

        if (character.mp > character.localMaxMp) {
            character.setMp(character.localMaxMp);
            character.statUpdates.put(Stat.MP, character.mp);
        }
    }

    @Override
    public void onStatUpdate() {
        character.recalcLocalStats();
    }

    @Override
    public void onAnnounceStatPoolUpdate() {
        List<Pair<Stat, Integer>> statup = new ArrayList<>(8);
        for (Map.Entry<Stat, Integer> s : character.statUpdates.entrySet()) {
            statup.add(new Pair<>(s.getKey(), s.getValue()));
        }

        character.sendPacket(PacketCreator.updatePlayerStats(statup, true, character));
    }
}
