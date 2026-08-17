package soloMapling.ArtificialPlayer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Manages a block list for bots that are trading.
 * When a bot declines a trade, the trade partner is added to this block list with an expiration time.
 */
public class BotBlockList {

    /**
     * Represents an entry in the block list.
     */
    public static class BlockEntry {
        private final Integer botId;
        private final Integer traderId;
        private final Instant blockTime;
        private final Instant expirationTime;

        /**
         * Creates a new block entry.
         *
         * @param botId          The ID of the bot that created the block
         * @param traderId       The ID of the trader being blocked
         * @param blockTime      The time when the block was created
         * @param expirationTime The time when the block will expire
         */
        public BlockEntry(Integer botId, Integer traderId, Instant blockTime, Instant expirationTime) {
            this.botId = botId;
            this.traderId = traderId;
            this.blockTime = blockTime;
            this.expirationTime = expirationTime;
        }

        // Getters
        public Integer getBotId() {
            return botId;
        }

        public Integer getTraderId() {
            return traderId;
        }

        public Instant getBlockTime() {
            return blockTime;
        }

        public Instant getExpirationTime() {
            return expirationTime;
        }

        /**
         * Checks if this block entry has expired.
         *
         * @param currentTime The current time to check against
         * @return true if the block has expired, false otherwise
         */
        public boolean isExpired(Instant currentTime) {
            return currentTime.isAfter(expirationTime);
        }

        @Override
        public String toString() {
            return "BlockEntry{" +
                    "botId='" + botId + '\'' +
                    ", traderId='" + traderId + '\'' +
                    ", blockTime=" + blockTime +
                    ", expirationTime=" + expirationTime +
                    '}';
        }
    }

    private static final BotBlockList botBlockList = new BotBlockList();
    private final List<BlockEntry> blockList;

    public BotBlockList() {
        this.blockList = new ArrayList<>();
    }

    public static BotBlockList getInstance() {
        return botBlockList;
    }

    /**
     * Adds a trader to the block list for a specific bot.
     *
     * @param botId                The ID of the bot creating the block
     * @param traderId             The ID of the trader to block
     * @param blockDurationSeconds How long the block should last in seconds
     * @return The created block entry
     */
    public BlockEntry addToBlockList(Integer botId, Integer traderId, long blockDurationSeconds) {
        Instant now = Instant.now();
        Instant expirationTime = now.plusSeconds(blockDurationSeconds);

        BlockEntry entry = new BlockEntry(botId, traderId, now, expirationTime);
        blockList.add(entry);

        return entry;
    }

    public BlockEntry addToBlockList(Integer botId, Integer traderId) {
        return addToBlockList(botId, traderId, 300);
    }

    /**
     * Checks if a trader is currently blocked by a specific bot.
     *
     * @param botId    The ID of the bot
     * @param traderId The ID of the trader
     * @return true if the trader is blocked by the bot, false otherwise
     */
    public boolean isBlocked(Integer botId, Integer traderId) {
        Instant now = Instant.now();

        // Remove expired entries first
        removeExpiredEntries();

        // Check if trader is blocked
        return blockList.stream()
                .anyMatch(entry -> entry.getBotId().equals(botId)
                        && entry.getTraderId().equals(traderId));
    }

    /**
     * Gets the block entry for a specific bot and trader if it exists.
     *
     * @param botId    The ID of the bot
     * @param traderId The ID of the trader
     * @return Optional containing the block entry if found, empty otherwise
     */
    public Optional<BlockEntry> getBlockEntry(Integer botId, Integer traderId) {
        // Remove expired entries first
        removeExpiredEntries();

        return blockList.stream()
                .filter(entry -> entry.getBotId().equals(botId)
                        && entry.getTraderId().equals(traderId))
                .findFirst();
    }

    /**
     * Removes all expired block entries from the list.
     */
    public void removeExpiredEntries() {
        Instant now = Instant.now();

        Iterator<BlockEntry> iterator = blockList.iterator();
        while (iterator.hasNext()) {
            BlockEntry entry = iterator.next();
            if (entry.isExpired(now)) {
                iterator.remove();
            }
        }
    }

    /**
     * Manually removes a block entry for a specific bot and trader.
     *
     * @param botId    The ID of the bot
     * @param traderId The ID of the trader
     * @return true if an entry was removed, false otherwise
     */
    public boolean removeBlock(Integer botId, Integer traderId) {
        return blockList.removeIf(entry ->
                entry.getBotId().equals(botId) && entry.getTraderId().equals(traderId));
    }

    /**
     * Gets all current block entries (expired entries are removed first).
     *
     * @return A list of all active block entries
     */
    public List<BlockEntry> getBlockList() {
        removeExpiredEntries();
        return new ArrayList<>(blockList);
    }

    /**
     * Gets all block entries for a specific bot.
     *
     * @param botId The ID of the bot
     * @return A list of all active block entries for the bot
     */
    public List<BlockEntry> getBlocksForBot(Integer botId) {
        removeExpiredEntries();

        return blockList.stream()
                .filter(entry -> entry.getBotId().equals(botId))
                .toList();
    }

    /**
     * Gets all traders blocked by a specific bot.
     *
     * @param botId The ID of the bot
     * @return A list of trader IDs blocked by the bot
     */
    public List<Integer> getBlockedTraders(Integer botId) {
        removeExpiredEntries();

        return blockList.stream()
                .filter(entry -> entry.getBotId().equals(botId))
                .map(BlockEntry::getTraderId)
                .toList();
    }

    /**
     * Clears all block entries.
     */
    public void clearBlockList() {
        blockList.clear();
    }

    /**
     * Gets the total number of active blocks.
     *
     * @return The count of non-expired block entries
     */
    public int getActiveBlockCount() {
        removeExpiredEntries();
        return blockList.size();
    }
}