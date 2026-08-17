package soloMapling.ArtificialPlayer.BotFlavorSystem;

import org.gms.client.Character;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;
import soloMapling.server.BotTiming;
import soloMapling.server.EventMessageSystem.EventType;
import soloMapling.server.EventMessageSystem.GameEvent;

import java.util.concurrent.ThreadLocalRandom;

import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotEmote;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotSpeak;
import static soloMapling.ArtificialPlayer.BotDialogueHandler.getRandomResolvedLine;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.botFaceTowardsPoint;

// Shared level-up congratulations. A bot near a character (player OR bot) that just levelled reacts a
// few seconds later: faces them, speaks a congrats line, emotes. Both SocialBot and TownWandererBot
// delegate here from handleEvent on a LEVEL_UP event. Best-effort ambient - any gate failing just drops
// it. Anti-spam is probability + a staggered delay (no server-wide responder cap): with a modest per-bot
// chance and 5-9s jitter, a crowd yields a few scattered "grats", not a synchronized shout. The reacting
// bot must be available (not mid-conversation / mid-travel) both when the event arrives AND when the line
// fires, and the map must still be observed at fire time (the player may have walked off).
public final class LevelUpCongrats {

    private LevelUpCongrats() {}

    // Tuning knobs (public volatile for a future !env flavor readout).
    public static volatile double CONGRATS_CHANCE = 0.30;     // per nearby bot, per level-up
    public static volatile int CONGRATS_RADIUS = 350;         // px; only bots this close react
    public static volatile long CONGRATS_DELAY_MIN_MS = 5000;
    public static volatile long CONGRATS_DELAY_MAX_MS = 9000;

    private static final String NODE = "CongratsLevelUp";
    private static final int[] CONGRATS_EMOTES = {2, 6};

    // Decide whether THIS bot congratulates the leveler, and if so schedule the staggered reaction.
    public static void react(BotSM bot, GameEvent event) {
        if (bot == null || event == null || event.getType() != EventType.LEVEL_UP) {
            return;
        }
        Character me = bot.getChr();
        Character leveler = event.getMapleCharacter();
        if (me == null || me.getMap() == null || leveler == null) {
            return;
        }
        if (leveler.getId() == me.getId()) {
            return; // never congratulate yourself (also stops a bot reacting to its own level-up)
        }
        if (!bot.isAvailableForAmbientActions()) {
            return; // busy - mid-conversation / mid map-hop
        }
        if (leveler.getMap() != me.getMap()) {
            return; // different map instance (matchesFilter usually catches this already)
        }
        double radius = CONGRATS_RADIUS;
        if (me.getPosition().distanceSq(leveler.getPosition()) > radius * radius) {
            return; // too far to plausibly notice
        }
        if (ThreadLocalRandom.current().nextDouble() >= CONGRATS_CHANCE) {
            return; // this bot stays quiet this time
        }
        BotTiming.afterRandom(CONGRATS_DELAY_MIN_MS, CONGRATS_DELAY_MAX_MS, () -> fire(bot, leveler));
    }

    private static void fire(BotSM bot, Character leveler) {
        Character me = bot.getChr();
        if (me == null || me.getMap() == null || leveler == null) {
            return;
        }
        if (!bot.isAvailableForAmbientActions()) {
            return; // got busy during the stagger window
        }
        if (leveler.getMap() != me.getMap()) {
            return; // leveler left the map
        }
        if (!GCMovement.isMapObserved(me.getMapId())) {
            return; // nobody's around to see it now
        }
        String line;
        try {
            // Resolve {PLAYER_NAME}/{PLAYER_LEVEL} against the leveler (works whether it's a real
            // player or a bot - both are Characters). Unresolvable tokens drop the line to null.
            line = getRandomResolvedLine(bot, NODE, leveler);
        } catch (Exception e) {
            line = null;
        }
        final String spoken = line;
        final int emote = CONGRATS_EMOTES[ThreadLocalRandom.current().nextInt(CONGRATS_EMOTES.length)];
        BotTiming.chain()
                .stopUnless(() -> bot.isAvailableForAmbientActions() && leveler.getMap() == me.getMap())
                .run(() -> botFaceTowardsPoint(me, leveler.getPosition()))
                .pauseRandom(300, 700)
                .run(() -> {
                    if (spoken != null) {
                        BotSpeak(me, spoken);
                    }
                })
                .pause(400)
                .run(() -> BotEmote(me, emote))
                .start();
    }
}
