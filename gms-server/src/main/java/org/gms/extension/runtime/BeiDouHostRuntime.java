package org.gms.extension.runtime;

import dev.maple.extension.api.CharacterClassifier;
import dev.maple.extension.api.HostCommandRegistry;
import dev.maple.extension.api.HostConfig;
import dev.maple.extension.api.HostEventBus;
import dev.maple.extension.api.HostRuntime;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * BeiDou implementation of {@link HostRuntime}. Constructed once per server process.
 */
public final class BeiDouHostRuntime implements HostRuntime {

    private final HostConfig config;
    private final HostEventBus events;
    private final HostCommandRegistry commands;
    private final List<CharacterClassifier> classifiers = new CopyOnWriteArrayList<>();

    public BeiDouHostRuntime(HostConfig config, HostEventBus events, HostCommandRegistry commands) {
        this.config = config;
        this.events = events;
        this.commands = commands;
    }

    @Override
    public HostConfig config() {
        return config;
    }

    @Override
    public HostEventBus events() {
        return events;
    }

    @Override
    public HostCommandRegistry commands() {
        return commands;
    }

    @Override
    public String hostId() {
        return "beidou";
    }

    @Override
    public void addCharacterClassifier(CharacterClassifier classifier) {
        if (classifier != null) {
            classifiers.add(classifier);
        }
    }

    @Override
    public void removeCharacterClassifier(CharacterClassifier classifier) {
        classifiers.remove(classifier);
    }

    @Override
    public boolean isArtificialCharacter(int characterId) {
        for (CharacterClassifier classifier : classifiers) {
            if (classifier.test(characterId)) {
                return true;
            }
        }
        return false;
    }
}
