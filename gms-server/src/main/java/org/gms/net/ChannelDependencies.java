package org.gms.net;

import org.gms.client.processor.npc.FredrickProcessor;
import org.gms.service.NoteService;

import java.util.Objects;

public record ChannelDependencies(NoteService noteService, FredrickProcessor fredrickProcessor) {

    public ChannelDependencies {
        Objects.requireNonNull(noteService);
        Objects.requireNonNull(fredrickProcessor);
    }
}
