package org.gms.extension.api;

/**
 * Decides whether a character id is owned by a plugin (bots, NPCs-as-characters, etc.).
 * Registered by plugins via {@link ArtificialCharacters#register(CharacterClassifier)}.
 */
@FunctionalInterface
public interface CharacterClassifier {

    boolean isArtificial(int characterId);
}
