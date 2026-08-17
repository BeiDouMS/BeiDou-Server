package soloMapling.ArtificialPlayer.BotGrindSystem;

// The map-axis grind archetype: HOW a bot works a map once it's there. Orthogonal to MovementStyle
// (the class-axis locomotion flavor) — a map picks the archetype, the class picks how the bot moves
// inside it. Resolved once per grind episode by GrindStylePolicy; each style is its own GrindStrategy
// so archetypes are tuned (and GM-forced, for testing) independently. Ours (SoloMapling).
public enum GrindStyle {
    CAMP,   // plant on a spawn-dense spot; FIGHT <-> WAIT through respawn lulls (dense single-ledge maps)
    PATROL, // hold a ring of 2-4 spots; rotate to the next on dry instead of waiting (spread maps)
    ROAM,   // anchor-free seek across ledges (un-campable tiny-platform / jumpy-mob maps)
    STACK   // vertical tether across stacked ledges (trees / towers / subway)
}
