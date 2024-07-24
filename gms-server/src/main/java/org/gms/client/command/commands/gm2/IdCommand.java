package org.gms.client.command.commands.gm2;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.constants.game.NpcChat;
import org.gms.constants.id.NpcId;
import org.gms.server.ThreadManager;
import org.gms.exception.IdTypeNotSupportedException;
import org.gms.util.I18nUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class IdCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("IdCommand.message1"));
    }
    private final static int MAX_SEARCH_HITS = 100;
    private final Map<String, String> handbookDirectory = typeFilePaths();
    private final Map<String, HandbookFileItems> typeItems = new ConcurrentHashMap<>();

    private Map<String, String> typeFilePaths() {
        return Map.ofEntries(
                Map.entry("map", "handbook/Map.txt"),
                Map.entry("etc", "handbook/Etc.txt"),
                Map.entry("npc", "handbook/NPC.txt"),
                Map.entry("use", "handbook/Use.txt"),
                Map.entry("weapon", "handbook/Equip/Weapon.txt") // TODO add more into this
        );
    }

    private static class HandbookFileItems {
        private final List<HandbookItem> items;

        public HandbookFileItems(List<String> fileLines) {
            this.items = fileLines.stream()
                    .map(this::parseLine)
                    .filter(Predicate.not(Objects::isNull))
                    .toList();
        }

        private HandbookItem parseLine(String line) {
            if (line == null) {
                return null;
            }

            String[] splitLine = line.split(" - ", 2);
            if (splitLine.length < 2 || splitLine[1].isBlank()) {
                return null;
            }
            return new HandbookItem(splitLine[0], splitLine[1]);
        }

        public List<HandbookItem> search(String query) {
            if (query == null || query.isBlank()) {
                return Collections.emptyList();
            }
            return items.stream()
                    .filter(item -> item.matches(query))
                    .toList();
        }
    }

    private record HandbookItem(String id, String name) {

        public HandbookItem {
            Objects.requireNonNull(id);
            Objects.requireNonNull(name);
        }

        public boolean matches(String query) {
            if (query == null) {
                return false;
            }
            return this.name.toLowerCase().contains(query.toLowerCase());
        }
    }

    @Override
    public void execute(Client client, final String[] params) {
        final Character chr = client.getPlayer();
        if (params.length < 2) {
            chr.yellowMessage(I18nUtil.getMessage("IdCommand.message2"));
            return;
        }
        final String type = params[0].toLowerCase();
        final String[] queryItems = Arrays.copyOfRange(params, 1, params.length);
        final String query = String.join(" ", queryItems);
        chr.yellowMessage(I18nUtil.getMessage("IdCommand.message3"));
        Runnable queryRunnable = () -> {
            try {
                populateIdMap(type);

                final HandbookFileItems handbookFileItems = typeItems.get(type);
                if (handbookFileItems == null) {
                    return;
                }
                final List<HandbookItem> searchHits = handbookFileItems.search(query);

                if (!searchHits.isEmpty()) {
                    String searchHitsText = searchHits.stream()
                            .limit(MAX_SEARCH_HITS)
                            .map(item -> I18nUtil.getMessage("IdCommand.message4", item.id, item.name))
                            .collect(Collectors.joining(NpcChat.NEW_LINE));
                    int hitsCount = Math.min(searchHits.size(), MAX_SEARCH_HITS);
                    String summaryText = I18nUtil.getMessage("IdCommand.message5", searchHits.size(), hitsCount);
                    String fullText = searchHitsText + NpcChat.NEW_LINE + summaryText;
                    chr.getAbstractPlayerInteraction().npcTalk(NpcId.MAPLE_ADMINISTRATOR, fullText);
                } else {
                    chr.yellowMessage(I18nUtil.getMessage("IdCommand.message6", query, type));
                }
            } catch (IdTypeNotSupportedException e) {
                chr.yellowMessage(I18nUtil.getMessage("IdCommand.message7"));
            } catch (IOException e) {
                chr.yellowMessage(I18nUtil.getMessage("IdCommand.message8"));
            }
        };

        ThreadManager.getInstance().newTask(queryRunnable);
    }

    private void populateIdMap(String type) throws IdTypeNotSupportedException, IOException {
        final String filePath = handbookDirectory.get(type);
        if (filePath == null) {
            throw new IdTypeNotSupportedException();
        }
        if (typeItems.containsKey(type)) {
            return;
        }

        final List<String> fileLines = Files.readAllLines(Path.of(filePath));
        typeItems.put(type, new HandbookFileItems(fileLines));
    }
}
