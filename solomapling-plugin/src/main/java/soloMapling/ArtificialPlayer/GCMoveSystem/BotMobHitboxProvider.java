package soloMapling.ArtificialPlayer.GCMoveSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.gms.provider.wz.WZFiles;
import org.gms.server.life.Monster;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static soloMapling.ArtificialPlayer.GCMoveSystem.BotWzXml.findNamedChild;
import static soloMapling.ArtificialPlayer.GCMoveSystem.BotWzXml.getIntAttribute;
import static soloMapling.ArtificialPlayer.GCMoveSystem.BotWzXml.getIntValue;

// Loads a mob's body hitbox (lt/rb bounds) from its WZ Mob/%07d.img.xml and maps it to world
// coordinates around the mob's position (mirrored when facing left). One rectangle is cached per
// distinct mob id - bounded by monster species, not by mob/bot count - and the cache is cleared if
// the WZ root path changes. Used by the bot contact-damage overlap check.
// Extracted from GreenCatMS and converted from a getInstance() singleton to all-static. Credit: NutNNut.
final class BotMobHitboxProvider {
    private static final Logger log = LoggerFactory.getLogger(BotMobHitboxProvider.class);

    // Sentinel stored when a mob has no usable hitbox frame, so we don't re-parse/re-log every tick
    // for mobs we already know are unresolvable.
    private static final Rectangle UNRESOLVED_BOUNDS = new Rectangle(Integer.MIN_VALUE, 0, 0, 0);

    // Body-pose frame groups only. Attack/hit frames can embed weapon-swing extents that would
    // inflate the body rect; flying mobs expose fly/0 instead of stand/0.
    private static final String[] FRAME_GROUP_FALLBACK = {"stand", "move", "fly"};

    private static final Map<Integer, Rectangle> boundsByMobId = new ConcurrentHashMap<>();
    private static volatile Path cachedMobRoot = null;

    private BotMobHitboxProvider() {
    }

    static Rectangle getMobBounds(Monster mob) {
        if (mob == null) {
            return null;
        }

        return getMobBounds(mob.getId(), mob.getPosition(), mob.isFacingLeft());
    }

    static Rectangle getMobBounds(int mobId, Point position, boolean facingLeft) {
        ensureCurrentMobRoot();
        Rectangle modelBounds = boundsByMobId.computeIfAbsent(mobId, BotMobHitboxProvider::loadMobBounds);
        if (modelBounds == UNRESOLVED_BOUNDS) {
            return null;
        }

        return calculateWorldBounds(modelBounds, position, facingLeft);
    }

    private static void ensureCurrentMobRoot() {
        Path currentMobRoot = WZFiles.MOB.getFile();
        Path previousMobRoot = cachedMobRoot;
        if (previousMobRoot != null && previousMobRoot.equals(currentMobRoot)) {
            return;
        }

        synchronized (boundsByMobId) {
            if (cachedMobRoot != null && cachedMobRoot.equals(currentMobRoot)) {
                return;
            }
            boundsByMobId.clear();
            cachedMobRoot = currentMobRoot;
        }
    }

    private static Rectangle loadMobBounds(int mobId) {
        Path mobFile = WZFiles.MOB.getFile().resolve(String.format("%07d.img.xml", mobId));
        if (!Files.isRegularFile(mobFile)) {
            log.debug("Bot mob hitbox: no WZ file for mob {} - caching miss", mobId);
            return UNRESOLVED_BOUNDS;
        }

        Document document = parseXmlDocument(mobFile);
        if (document == null) {
            return UNRESOLVED_BOUNDS;
        }

        Rectangle bounds = loadFrameBounds(document.getDocumentElement());
        if (bounds == null) {
            log.debug("Bot mob hitbox: no lt/rb bounds on any of {} for mob {} - caching miss",
                    String.join(",", FRAME_GROUP_FALLBACK), mobId);
            return UNRESOLVED_BOUNDS;
        }
        return bounds;
    }

    private static Rectangle loadFrameBounds(Element root) {
        Element linkedRoot = resolveLinkedRoot(root);
        for (String frameGroup : FRAME_GROUP_FALLBACK) {
            Element group = findNamedChild(linkedRoot, frameGroup);
            if (group == null) {
                continue;
            }
            Element frame = findNamedChild(group, "0");
            if (frame == null) {
                continue;
            }
            Rectangle bounds = toBounds(findNamedChild(frame, "lt"), findNamedChild(frame, "rb"));
            if (bounds != null) {
                return bounds;
            }
        }
        return null;
    }

    private static Element resolveLinkedRoot(Element root) {
        Element info = findNamedChild(root, "info");
        int linkedMobId = getIntValue(findNamedChild(info, "link"), 0);
        if (linkedMobId <= 0) {
            return root;
        }

        Path linkedFile = WZFiles.MOB.getFile().resolve(String.format("%07d.img.xml", linkedMobId));
        if (!Files.isRegularFile(linkedFile)) {
            return root;
        }

        Document linkedDocument = parseXmlDocument(linkedFile);
        return linkedDocument != null ? linkedDocument.getDocumentElement() : root;
    }

    private static Rectangle calculateWorldBounds(Rectangle modelBounds, Point origin, boolean facingLeft) {
        int left = modelBounds.x;
        int right = modelBounds.x + modelBounds.width;
        if (facingLeft) {
            int originalLeft = left;
            left = -right;
            right = -originalLeft;
        }

        return new Rectangle(origin.x + left, origin.y + modelBounds.y, right - left, modelBounds.height);
    }

    private static Rectangle toBounds(Element lt, Element rb) {
        if (lt == null || rb == null) {
            return null;
        }

        int left = Math.min(getIntAttribute(lt, "x", 0), getIntAttribute(rb, "x", 0));
        int right = Math.max(getIntAttribute(lt, "x", 0), getIntAttribute(rb, "x", 0));
        int top = Math.min(getIntAttribute(lt, "y", 0), getIntAttribute(rb, "y", 0));
        int bottom = Math.max(getIntAttribute(lt, "y", 0), getIntAttribute(rb, "y", 0));
        if (left >= right || top >= bottom) {
            return null;
        }

        return new Rectangle(left, top, right - left, bottom - top);
    }

    private static Document parseXmlDocument(Path path) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(path.toFile());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.warn("Failed to load bot mob hitbox data from {}", path, e);
            return null;
        }
    }

}
