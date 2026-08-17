package soloMapling.server.EventMessageSystem;

public interface EventSubscriber {
    void onEvent(GameEvent event);

    boolean matchesFilter(GameEvent event);
}
