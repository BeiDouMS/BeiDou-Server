package org.gms.extension.runtime;

import org.gms.extension.api.HostCommandRegistry;
import org.gms.extension.api.HostCharacterProvisioner;
import org.gms.extension.api.HostConfig;
import org.gms.extension.api.HostEventBus;
import org.gms.extension.api.HostItemActions;
import org.gms.extension.api.HostMonsterDrops;
import org.gms.extension.api.HostRuntime;

import java.util.Optional;

/**
 * BeiDou implementation of {@link HostRuntime}. Constructed once per server process.
 */
public final class BeiDouHostRuntime implements HostRuntime {

    private final HostConfig config;
    private final HostEventBus events;
    private final HostCommandRegistry commands;
    private final HostCharacterProvisioner characterProvisioner;
    private final HostItemActions itemActions;
    private final HostMonsterDrops monsterDrops;

    public BeiDouHostRuntime(HostConfig config, HostEventBus events, HostCommandRegistry commands) {
        this(config, events, commands, null);
    }

    public BeiDouHostRuntime(
            HostConfig config,
            HostEventBus events,
            HostCommandRegistry commands,
            HostCharacterProvisioner characterProvisioner
    ) {
        this(config, events, commands, characterProvisioner, null, null);
    }

    public BeiDouHostRuntime(
            HostConfig config,
            HostEventBus events,
            HostCommandRegistry commands,
            HostCharacterProvisioner characterProvisioner,
            HostItemActions itemActions,
            HostMonsterDrops monsterDrops
    ) {
        this.config = config;
        this.events = events;
        this.commands = commands;
        this.characterProvisioner = characterProvisioner;
        this.itemActions = itemActions;
        this.monsterDrops = monsterDrops;
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
    public Optional<HostCharacterProvisioner> characterProvisioner() {
        return Optional.ofNullable(characterProvisioner);
    }

    @Override
    public Optional<HostItemActions> itemActions() {
        return Optional.ofNullable(itemActions);
    }

    @Override
    public Optional<HostMonsterDrops> monsterDrops() {
        return Optional.ofNullable(monsterDrops);
    }

    @Override
    public String hostId() {
        return "beidou";
    }
}
