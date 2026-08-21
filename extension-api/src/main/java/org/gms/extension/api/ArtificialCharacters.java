package org.gms.extension.api;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Host-side registry of {@link CharacterClassifier}s. Engine hooks must call
 * {@link #isArtificial(int)} instead of importing any plugin package.
 *
 * <p>Plugins register classifiers in {@code ServerExtension#onLoad}; the host clears
 * them on unload.
 */
public final class ArtificialCharacters {

    private static final CopyOnWriteArrayList<CharacterClassifier> CLASSIFIERS = new CopyOnWriteArrayList<>();

    private ArtificialCharacters() {
    }

    public static void register(CharacterClassifier classifier) {
        if (classifier != null) {
            CLASSIFIERS.addIfAbsent(classifier);
        }
    }

    public static void unregister(CharacterClassifier classifier) {
        CLASSIFIERS.remove(classifier);
    }

    public static void clear() {
        CLASSIFIERS.clear();
    }

    /** Snapshot for tests / diagnostics. */
    public static List<CharacterClassifier> classifiers() {
        return List.copyOf(CLASSIFIERS);
    }

    public static boolean isArtificial(int characterId) {
        for (CharacterClassifier classifier : CLASSIFIERS) {
            if (classifier.isArtificial(characterId)) {
                return true;
            }
        }
        return false;
    }
}
