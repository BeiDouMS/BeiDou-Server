package soloMapling.ArtificialPlayer.BotMovementSystem;

import org.gms.client.Character;
import org.gms.client.Job;

import java.awt.*;

/**
 * Lightweight snapshot of a real player's visible info at the moment a bot "notices" them.
 * Intentionally passive data — no game logic, just what the bot observed.
 */
public class PlayerSnapshot {

    private final int id;
    private final String name;
    private final int level;
    private final Job job;
    private final Point position;

    public PlayerSnapshot(Character player) {
        this.id = player.getId();
        this.name = player.getName();
        this.level = player.getLevel();
        this.job = player.getJob();
        this.position = new Point(player.getPosition());
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getLevel() { return level; }
    public Job getJob() { return job; }
    public Point getPosition() { return position; }

    @Override
    public String toString() {
        return "PlayerSnapshot{" + name + ", Lv" + level + ", " + job + "}";
    }
}
