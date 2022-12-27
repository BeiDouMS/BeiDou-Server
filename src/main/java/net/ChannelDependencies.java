package net;

import service.NoteService;

import java.util.Objects;

public record ChannelDependencies(NoteService noteService) {

    public ChannelDependencies {
        Objects.requireNonNull(noteService);
    }
}
