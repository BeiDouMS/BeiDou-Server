package soloMapling.ArtificialPlayer.BotGrindSystem;

// How a grinding bot moves toward mobs on a map. Drives GrindBrain's approach leg + engage flair so
// class-appropriate locomotion reads authentically. Ours (not a GreenCat extraction); the actual blink/dash
// packets live in GCMoveSystem/GCMovementSkills.
public enum MovementStyle {
    PLANTED,     // walk + plant to swing (default; low-level bots stay slow = organic)
    JUMP_ATTACK, // hop toward mobs and swing mid-arc (haste thieves: bandit/CB/shadower, optionally warriors)
    FLASH_JUMP,  // air-dash toward mobs (Hermit / Night Lord)
    TELEPORT,    // blink toward mobs (mage 2nd job and above)
    RANGED       // hold distance and kite: step back when a mob closes inside min-range (bowmen 2nd job+)
}
