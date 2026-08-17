package soloMapling.ArtificialPlayer;

import org.gms.client.Character;
import org.gms.client.inventory.Item;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Pet;
import org.gms.constants.game.GameConstants;
import org.gms.constants.inventory.ItemConstants;
import org.gms.net.server.guild.Guild;
import org.gms.server.ItemInformationProvider;
import org.gms.server.life.Monster;
import org.gms.server.life.MonsterDropEntry;
import org.gms.server.life.MonsterInformationProvider;
import org.gms.server.maps.MapleMap;
import soloMapling.ArtificialPlayer.BotGrindSystem.MapMobIndex;
import soloMapling.itemPool.DesirableEquipList;
import soloMapling.itemPool.ItemInformationProviderUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static soloMapling.server.SoloMaplingUtilities.random;

// Resolves {TOKEN} placeholders in a dialogue line against live game-state.
//
// Two subjects:
//   - SELF (the speaking bot): {MAP} {REGION} {MOB} {DROP} {JOB} {LEVEL} {WEAPON}
//   - PLAYER (a character the bot reacts to): {PLAYER_NAME} {PLAYER_LEVEL} {PLAYER_JOB}
//     {PLAYER_FAME} {PLAYER_WEAPON} {PLAYER_GEAR} {PLAYER_PET} {PLAYER_GUILD} {PLAYER_NX}
//
// Every resolver returns Optional; empty means "can't resolve here", which the caller treats as
// "don't speak this line" so a raw {TOKEN} never leaks into chat (e.g. {PLAYER_PET} on a petless
// player simply drops the line).
public final class DialogueContextResolver {

    private DialogueContextResolver() {
    }

    private static final Pattern TOKEN = Pattern.compile("\\{([A-Z_]+)\\}");

    // Cash-overlay equipped items sit at slot <= this; regular visible equips are between this and 0.
    private static final int CASH_SLOT_CEILING = -100;

    public static boolean hasTokens(String line) {
        return line != null && TOKEN.matcher(line).find();
    }

    public static Optional<String> fill(String line, Character self) {
        return fill(line, self, null);
    }

    // Fills every token in the line against self (the bot) and player (nullable). Returns empty if
    // ANY token cannot be resolved, so the caller drops the line rather than speaking a half-filled
    // template.
    public static Optional<String> fill(String line, Character self, Character player) {
        if (line == null) {
            return Optional.empty();
        }
        Matcher m = TOKEN.matcher(line);
        if (!m.find()) {
            return Optional.of(line);
        }
        Resolution res = new Resolution(self, player);
        StringBuffer sb = new StringBuffer();
        m.reset();
        while (m.find()) {
            Optional<String> val = res.resolve(m.group(1));
            if (val.isEmpty() || val.get().isBlank()) {
                return Optional.empty();
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(val.get()));
        }
        m.appendTail(sb);
        return Optional.of(sb.toString());
    }

    // Per-fill resolver. Caches the focus mob so {MOB} and {DROP} in the same line agree on one mob.
    private static final class Resolution {
        private final Character self;
        private final Character player;
        private boolean mobResolved;
        private Integer focusMobId;

        Resolution(Character self, Character player) {
            this.self = self;
            this.player = player;
        }

        Optional<String> resolve(String token) {
            try {
                return switch (token) {
                    // self (the speaking bot)
                    case "MAP" -> mapName(self);
                    case "REGION" -> region(self);
                    case "JOB" -> jobName(self);
                    case "LEVEL" -> levelStr(self);
                    case "WEAPON" -> weaponName(self);
                    case "MOB" -> mobName();
                    case "DROP" -> dropName();
                    // player (the character the bot reacts to)
                    case "PLAYER_NAME" -> player == null ? Optional.empty() : nonBlank(player.getName());
                    case "PLAYER_LEVEL" -> player == null ? Optional.empty() : levelStr(player);
                    case "PLAYER_JOB" -> player == null ? Optional.empty() : jobName(player);
                    case "PLAYER_FAME" -> player == null ? Optional.empty() : Optional.of(String.valueOf(player.getFame()));
                    case "PLAYER_WEAPON" -> player == null ? Optional.empty() : weaponName(player);
                    case "PLAYER_GEAR" -> player == null ? Optional.empty() : notableGear(player);
                    case "PLAYER_PET" -> player == null ? Optional.empty() : petName(player);
                    case "PLAYER_GUILD" -> player == null ? Optional.empty() : guildName(player);
                    case "PLAYER_NX" -> player == null ? Optional.empty() : cashItem(player);
                    default -> Optional.empty();
                };
            } catch (RuntimeException e) {
                return Optional.empty();
            }
        }

        private Optional<String> mobName() {
            OptionalInt id = focusMob();
            if (id.isEmpty()) {
                return Optional.empty();
            }
            return nonBlank(MonsterInformationProvider.getInstance().getMobNameFromId(id.getAsInt()));
        }

        private Optional<String> dropName() {
            OptionalInt mobId = focusMob();
            if (mobId.isEmpty()) {
                return Optional.empty();
            }
            OptionalInt dropId = pickNotableDrop(mobId.getAsInt(), self);
            if (dropId.isEmpty()) {
                return Optional.empty();
            }
            return nonBlank(ItemInformationProvider.getInstance().getName(dropId.getAsInt()));
        }

        private OptionalInt focusMob() {
            if (!mobResolved) {
                OptionalInt id = representativeMobId(self);
                focusMobId = id.isPresent() ? id.getAsInt() : null;
                mobResolved = true;
            }
            return focusMobId == null ? OptionalInt.empty() : OptionalInt.of(focusMobId);
        }
    }

    // ── Subject-agnostic field resolvers (work for the bot or a player) ──

    private static Optional<String> mapName(Character c) {
        MapleMap map = c.getMap();
        return map == null ? Optional.empty() : nonBlank(map.getMapName());
    }

    private static Optional<String> region(Character c) {
        MapleMap map = c.getMap();
        if (map == null) {
            return Optional.empty();
        }
        Optional<String> street = nonBlank(map.getStreetName());
        return street.isPresent() ? street : nonBlank(map.getMapName());
    }

    private static Optional<String> jobName(Character c) {
        return nonBlank(GameConstants.getJobName(c.getJob().getId()));
    }

    private static Optional<String> levelStr(Character c) {
        return Optional.of(String.valueOf(c.getLevel()));
    }

    private static Optional<String> weaponName(Character c) {
        Item w = c.getInventory(InventoryType.EQUIPPED).getItem((short) -11);
        if (w == null) {
            return Optional.empty();
        }
        return nonBlank(ItemInformationProvider.getInstance().getName(w.getItemId()));
    }

    private static Optional<String> petName(Character c) {
        Pet[] pets = c.getPets();
        if (pets == null) {
            return Optional.empty();
        }
        for (Pet p : pets) {
            if (p != null) {
                Optional<String> name = nonBlank(p.getName());
                if (name.isPresent()) {
                    return name;
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<String> guildName(Character c) {
        Guild g = c.getGuild();
        return g == null ? Optional.empty() : nonBlank(g.getName());
    }

    // A notable visible equip the player wears: prefer one on the desirable/iconic whitelist, else
    // any regular (non-cash) equip. Lets a bot compliment genuinely good gear, not random socks.
    private static Optional<String> notableGear(Character c) {
        List<Integer> desirable = new ArrayList<>();
        List<Integer> any = new ArrayList<>();
        for (Item it : c.getInventory(InventoryType.EQUIPPED).list()) {
            if (it.getPosition() <= CASH_SLOT_CEILING) {
                continue; // skip cash overlay; that's {PLAYER_NX}
            }
            int id = it.getItemId();
            any.add(id);
            if (DesirableEquipList.isDesirable(id)) {
                desirable.add(id);
            }
        }
        List<Integer> tier = !desirable.isEmpty() ? desirable : any;
        if (tier.isEmpty()) {
            return Optional.empty();
        }
        return nonBlank(ItemInformationProvider.getInstance().getName(tier.get(random.nextInt(tier.size()))));
    }

    // A worn Cash Shop cosmetic (cash overlay slots), for "where'd u get that NX?" flavor.
    private static Optional<String> cashItem(Character c) {
        List<Integer> cash = new ArrayList<>();
        for (Item it : c.getInventory(InventoryType.EQUIPPED).list()) {
            if (it.getPosition() <= CASH_SLOT_CEILING) {
                cash.add(it.getItemId());
            }
        }
        if (cash.isEmpty()) {
            return Optional.empty();
        }
        return nonBlank(ItemInformationProvider.getInstance().getName(cash.get(random.nextInt(cash.size()))));
    }

    // ── Mob / drop helpers (bot's map) ──

    // A representative mob the subject is among: a random live hostile mob on the map, else (none
    // currently spawned) a random mob the map is defined to spawn (WZ data via MapMobIndex).
    private static OptionalInt representativeMobId(Character chr) {
        MapleMap map = chr.getMap();
        if (map == null) {
            return OptionalInt.empty();
        }
        List<Monster> live = new ArrayList<>();
        for (Monster m : map.getAllMonsters()) {
            if (isHostile(m)) {
                live.add(m);
            }
        }
        if (!live.isEmpty()) {
            return OptionalInt.of(live.get(random.nextInt(live.size())).getId());
        }
        List<Integer> wz = MapMobIndex.mobIds(map.getId());
        if (wz != null && !wz.isEmpty()) {
            return OptionalInt.of(wz.get(random.nextInt(wz.size())));
        }
        return OptionalInt.empty();
    }

    private static boolean isHostile(Monster m) {
        return m != null && m.isAlive() && (m.getStats() == null || !m.getStats().isFriendly());
    }

    // Picks a notable EQUIP drop from a mob, ranked: desirable & class-matched > desirable >
    // class-matched > any equip. Scrolls / use / etc and quest drops are excluded (equips only) so
    // the line references real gear, not generic consumables.
    private static OptionalInt pickNotableDrop(int mobId, Character chr) {
        List<MonsterDropEntry> drops = MonsterInformationProvider.getInstance().retrieveEffectiveDrop(mobId);
        if (drops == null || drops.isEmpty()) {
            return OptionalInt.empty();
        }
        int botJobBit = ItemInformationProviderUtilities.getReqJobViaJobStyle(chr.getJobStyle());

        List<Integer> desirableMatched = new ArrayList<>();
        List<Integer> desirable = new ArrayList<>();
        List<Integer> classMatched = new ArrayList<>();
        List<Integer> anyEquip = new ArrayList<>();

        for (MonsterDropEntry d : drops) {
            int item = d.itemId;
            if (d.questid != 0 || !ItemConstants.isEquipment(item)) {
                continue;
            }
            anyEquip.add(item);
            boolean matched = jobAllows(item, botJobBit);
            boolean hot = DesirableEquipList.isDesirable(item);
            if (hot && matched) {
                desirableMatched.add(item);
            } else if (hot) {
                desirable.add(item);
            } else if (matched) {
                classMatched.add(item);
            }
        }

        List<Integer> tier =
                !desirableMatched.isEmpty() ? desirableMatched
                : !desirable.isEmpty() ? desirable
                : !classMatched.isEmpty() ? classMatched
                : anyEquip;
        if (tier.isEmpty()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(tier.get(random.nextInt(tier.size())));
    }

    // Usable by the bot's class? True when the equip has no job restriction, or its restriction
    // bitmask includes the bot's class (reqJob: 1=Warrior 2=Mage 4=Bowman 8=Thief 16=Pirate).
    private static boolean jobAllows(int itemId, int botJobBit) {
        Map<String, Integer> stats = ItemInformationProvider.getInstance().getEquipStats(itemId);
        Integer reqJob = stats == null ? null : stats.get("reqJob");
        if (reqJob == null || reqJob == 0) {
            return true;
        }
        return botJobBit != 0 && (reqJob & botJobBit) != 0;
    }

    private static Optional<String> nonBlank(String s) {
        return (s == null || s.isBlank()) ? Optional.empty() : Optional.of(s);
    }
}
