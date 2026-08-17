package soloMapling.command;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import soloMapling.ArtificialPlayer.BotHelpers;
import soloMapling.ArtificialPlayer.BotTypes.TrainingBot;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;
import soloMapling.server.BotTiming;

import java.awt.Point;

/*
 * !gcmove — GreenCat dynamic (calculation-based) movement test/control.
 * Bot-targeting subcommands take an explicit bot id (the bot's character id), matching
 * ArtificialPlayerCommand.
 */
public class GCMoveCommand extends Command {
    {
        setDescription("GreenCat dynamic movement (GCMoveSystem) test/control.");
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (params.length == 0 || params[0].equalsIgnoreCase("help")) {
            help(player);
            return;
        }

        String sub = params[0].toLowerCase();

        // 'bake' acts on the GM's own map and needs no bot.
        if (sub.equals("bake")) {
            player.dropMessage("Baking nav graph for map " + player.getMapId() + " ...");
            player.dropMessage(GCMovement.bakeReport(player));
            return;
        }

        // 'ropecheck' inspects the GM's own map ropes against the top-exit probe — no bot.
        if (sub.equals("ropecheck")) {
            GCMovement.ropeCheckReport(player).forEach(player::dropMessage);
            return;
        }

        // 'lod' is the LOD measurement tooling (M0) — load/unload/stats, no specific bot.
        if (sub.equals("lod")) {
            handleLod(player, params);
            return;
        }

        // Every other subcommand targets a specific bot by id: !gcmove <sub> <botId> [args]
        if (params.length < 2) {
            player.dropMessage("Usage: !gcmove " + sub + " <botId> ...   (see !gcmove help)");
            return;
        }
        Character bot = resolveBot(player, params[1]);
        if (bot == null) {
            return; // resolveBot already messaged the failure
        }

        switch (sub) {
            case "move" -> {
                if (params.length < 4) {
                    player.dropMessage("Usage: !gcmove move <botId> <x> <y>");
                    return;
                }
                try {
                    int x = Integer.parseInt(params[2]);
                    int y = Integer.parseInt(params[3]);
                    GCMovement.move(bot, x, y, () -> player.dropMessage(bot.getName() + " arrived at (" + x + "," + y + ")"));
                    player.dropMessage(bot.getName() + " moving to (" + x + "," + y + ")");
                } catch (NumberFormatException e) {
                    player.dropMessage("Usage: !gcmove move <botId> <x> <y>");
                }
            }
            case "here" -> {
                Point p = player.getPosition();
                GCMovement.move(bot, p.x, p.y, () -> player.dropMessage(bot.getName() + " reached you"));
                player.dropMessage(bot.getName() + " moving to you (" + p.x + "," + p.y + ")");
            }
            case "come" -> {
                Point p = player.getPosition();
                int mapId = player.getMapId();
                GCMovement.travelTo(bot, mapId, p.x, p.y, ok -> player.dropMessage(
                        bot.getName() + (ok ? " reached you" : " couldn't reach you")));
                player.dropMessage(bot.getName() + " coming to you (map " + mapId + " @ " + p.x + "," + p.y + ") ...");
            }
            case "follow" -> {
                GCMovement.follow(bot, player);
                player.dropMessage(bot.getName() + " now dynamically following you.");
            }
            case "stop" -> {
                GCMovement.stop(bot);
                player.dropMessage(bot.getName() + " stopped.");
            }
            case "jump" -> {
                GCMovement.jumpInPlace(bot);
                player.dropMessage(bot.getName() + " jumps.");
            }
            case "fj" -> {
                // Calibration hook: fire one flash jump at an exact scale (bypasses tier cap + platform
                // fit) and report the measured ground travel — the numbers feed GCMovementSkills.FJ_TRAVEL_PX.
                float scale = 1f;
                if (params.length >= 3) {
                    try {
                        scale = Float.parseFloat(params[2]);
                    } catch (NumberFormatException e) {
                        player.dropMessage("Usage: !gcmove fj <botId> [scale 0.2-1.0] — bad scale, using 1.0");
                    }
                }
                Point start = bot.getPosition();
                boolean facingLeft = Boolean.TRUE.equals(GCMovement.isFacingLeft(bot));
                int dir = facingLeft ? -1 : 1;
                if (start == null || !GCMovement.debugFlashJump(bot, dir, scale)) {
                    player.dropMessage(bot.getName() + " can't flash-jump right now (airborne/climbing/not GC-driven).");
                    return;
                }
                final float usedScale = scale;
                player.dropMessage(bot.getName() + " flash jump: scale " + usedScale + ", dir " + (dir > 0 ? "right" : "left") + " ...");
                BotTiming.after(1_600, () -> { // arc lasts well under a second; measure after it settles
                    Point end = bot.getPosition();
                    if (end != null) {
                        player.dropMessage(String.format("fj scale %.2f -> travel dx=%d px (dy=%d)",
                                usedScale, Math.abs(end.x - start.x), end.y - start.y));
                    }
                });
            }
            case "turn" -> {
                GCMovement.turnAround(bot);
                player.dropMessage(bot.getName() + " turns around.");
            }
            case "duck" -> {
                GCMovement.duck(bot, 1500);
                player.dropMessage(bot.getName() + " ducks.");
            }
            case "fidget" -> {
                boolean on = params.length < 3 || !params[2].equalsIgnoreCase("off");
                GCMovement.setFidget(bot, on);
                player.dropMessage(bot.getName() + " idle fidget " + (on ? "ON" : "OFF") + ".");
            }
            case "travel" -> {
                if (params.length < 3) {
                    player.dropMessage("Usage: !gcmove travel <botId> <mapId>");
                    return;
                }
                try {
                    int mapId = Integer.parseInt(params[2]);
                    GCMovement.travel(bot, mapId, ok -> player.dropMessage(
                            bot.getName() + (ok ? " arrived at map " + mapId : " could not reach map " + mapId)));
                    player.dropMessage(bot.getName() + " traveling to map " + mapId + " ...");
                } catch (NumberFormatException e) {
                    player.dropMessage("Usage: !gcmove travel <botId> <mapId>");
                }
            }
            case "route" -> {
                if (params.length < 3) {
                    player.dropMessage("Usage: !gcmove route <botId> <mapId>");
                    return;
                }
                try {
                    player.dropMessage(GCMovement.routeReport(bot, Integer.parseInt(params[2])));
                } catch (NumberFormatException e) {
                    player.dropMessage("Usage: !gcmove route <botId> <mapId>");
                }
            }
            case "off" -> {
                GCMovement.disable(bot);
                player.dropMessage(bot.getName() + " removed from dynamic control.");
            }
            case "status" -> player.dropMessage(bot.getName()
                    + ": enabled=" + GCMovement.isEnabled(bot)
                    + " moving=" + GCMovement.isMoving(bot)
                    + " traveling=" + GCMovement.isTraveling(bot)
                    + " following=" + GCMovement.isFollowing(bot));
            default -> help(player);
        }
    }

    // !gcmove lod [stats|load <n>|unload] — LOD measurement tooling (M0).
    private static void handleLod(Character player, String[] params) {
        String op = params.length >= 2 ? params[1].toLowerCase() : "stats";
        switch (op) {
            case "load" -> {
                int n = 0;
                if (params.length >= 3) {
                    try {
                        n = Integer.parseInt(params[2]);
                    } catch (NumberFormatException ignored) {
                        // fall back to the default load size
                    }
                }
                int enabled = GCMovement.lodLoad(n);
                player.dropMessage("GCMove LOD: enabled dynamic movement on " + enabled + " bot(s). "
                        + "Watch CPU, then !gcmove lod stats / unload.");
            }
            case "unload" -> {
                int released = GCMovement.lodUnload();
                player.dropMessage("GCMove LOD: released " + released + " load-test bot(s).");
            }
            case "train" -> TrainingBot.lodDiagnostic().forEach(player::dropMessage);
            default -> GCMovement.lodStats().forEach(player::dropMessage);
        }
    }

    // Resolve idArg (a bot's character id) to its live Character, or message + null.
    private static Character resolveBot(Character player, String idArg) {
        int botId;
        try {
            botId = Integer.parseInt(idArg);
        } catch (NumberFormatException e) {
            player.dropMessage("GCMove: invalid bot id '" + idArg + "'.");
            return null;
        }
        Character bot = BotHelpers.getCharFromChannelStorage(botId);
        if (bot == null || !BotHelpers.isBot(bot)) {
            player.dropMessage("GCMove: no bot found with id " + botId + ".");
            return null;
        }
        return bot;
    }

    private static void help(Character player) {
        player.dropMessage("=== !gcmove (GCMoveSystem dynamic movement) ===");
        player.dropMessage("!gcmove bake                   - bake + report nav graph for your map (proof point)");
        player.dropMessage("!gcmove ropecheck              - per-rope top-exit probe (old vs widened) + failure mode, your map");
        player.dropMessage("!gcmove lod [stats]            - report dynamic-movement load (bots/maps/job state)");
        player.dropMessage("!gcmove lod load <n>           - enable+fidget up to n idle bots (LOD load test)");
        player.dropMessage("!gcmove lod unload             - release the lod load-test bots");
        player.dropMessage("!gcmove lod train              - training-bot LOD overview (tier/phase counts + full/halo maps)");
        player.dropMessage("!gcmove move <botId> <x> <y>   - bot walks/jumps/climbs to (x,y)");
        player.dropMessage("!gcmove here <botId>           - bot walks to you (same map)");
        player.dropMessage("!gcmove come <botId>           - bot travels across maps to your spot");
        player.dropMessage("!gcmove follow <botId>         - bot dynamically follows you");
        player.dropMessage("!gcmove travel <botId> <map>   - bot travels to another map (chain hops)");
        player.dropMessage("!gcmove route <botId> <map>    - print the portal-hop route (no movement)");
        player.dropMessage("!gcmove jump <botId>           - bot hops in place");
        player.dropMessage("!gcmove fj <botId> [scale]     - fire one flash jump at exact scale + measure travel (calibration)");
        player.dropMessage("!gcmove turn <botId>           - bot turns around");
        player.dropMessage("!gcmove duck <botId>           - bot crouches briefly");
        player.dropMessage("!gcmove fidget <botId> [off]   - toggle idle auto-fidget");
        player.dropMessage("!gcmove stop <botId>           - stop the bot");
        player.dropMessage("!gcmove off <botId>            - remove the bot from dynamic control");
        player.dropMessage("!gcmove status <botId>         - dynamic state of the bot");
    }
}
