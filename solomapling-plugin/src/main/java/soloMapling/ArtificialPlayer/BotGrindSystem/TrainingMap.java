package soloMapling.ArtificialPlayer.BotGrindSystem;

// A candidate training map: its id, its representative mob level, and its hop distance from the bot's
// spawn town (1 = adjacent). Produced by TrainingMapFinder.
public record TrainingMap(int mapId, int mobLevel, int hops) {
}
