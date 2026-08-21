package org.gms.extension.runtime;

import org.gms.extension.api.HostCommandRegistry;
import org.gms.extension.api.HostConfig;
import org.gms.extension.api.HostEventBus;
import org.gms.extension.api.HostRuntime;

/**
 * BeiDou implementation of {@link HostRuntime}. Constructed once per server process.
 */
public final class BeiDouHostRuntime implements HostRuntime {

    private final HostConfig config;
    private final HostEventBus events;
    private final HostCommandRegistry commands;

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
}
