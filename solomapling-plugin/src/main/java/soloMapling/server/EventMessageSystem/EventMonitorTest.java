//package soloMapling.server.EventMessageSystem;
//
//import javax.swing.*;
//import java.awt.*;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Random;
//import java.util.Timer;
//import java.util.TimerTask;
//
///*
//For Debugging purposes. UI screen to show event bus in real time for testing purposes
// */
//
//public class EventMonitorTest extends JFrame implements EventSubscriber {
//    private JTextArea eventDisplay;
//    private JButton generateEventButton;
//    private JButton clearButton;
//    private JCheckBox autoGenerateCheckbox;
//    private Timer autoGenerator;
//    private Random random = new Random();
//    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
//
//    // Mock objects for testing
////    private MapleMap mockMap = new MapleMap(100001, "Henesys");
////    private Item mockItem = new Item(4001126, "Maple Leaf");
//
//    public EventMonitorTest() {
//        setTitle("Event System Monitor");
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setLayout(new BorderLayout());
//
//        // Create components
//        setupUI();
//
//        // Subscribe to ALL event types
//        subscribeToAllEvents();
//
//        // Window settings
//        setSize(800, 600);
//        setLocationRelativeTo(null);
//        setVisible(true);
//
//        log("=== Event Monitor Started ===");
//    }
//
//    private void setupUI() {
//        // Top panel with controls
//        JPanel controlPanel = new JPanel(new FlowLayout());
//
////        generateEventButton = new JButton("Generate Random Event");
////        generateEventButton.addActionListener(e -> generateRandomEvent());
//
//        clearButton = new JButton("Clear Log");
//        clearButton.addActionListener(e -> eventDisplay.setText(""));
//
////        autoGenerateCheckbox = new JCheckBox("Auto-Generate Events");
////        autoGenerateCheckbox.addActionListener(e -> toggleAutoGenerate());
//
//        JButton queryStoreButton = new JButton("Query Event Store");
//        queryStoreButton.addActionListener(e -> queryEventStore());
//
////        controlPanel.add(generateEventButton);
////        controlPanel.add(autoGenerateCheckbox);
//        controlPanel.add(queryStoreButton);
//        controlPanel.add(clearButton);
//
//        // Center - event display
//        eventDisplay = new JTextArea();
//        eventDisplay.setEditable(false);
//        eventDisplay.setFont(new Font("Monospaced", Font.PLAIN, 12));
//        JScrollPane scrollPane = new JScrollPane(eventDisplay);
//        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//
//        // Bottom panel - statistics
//        JPanel statsPanel = new JPanel(new FlowLayout());
//        JLabel statsLabel = new JLabel("Events in Store: 0");
//        statsPanel.add(statsLabel);
//
//        // Update stats periodically
//        Timer statsTimer = new Timer();
//        statsTimer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                SwingUtilities.invokeLater(() -> {
//                    int eventCount = EventBus.getInstance().getEventStore()
//                            .getRecentEvents(Integer.MAX_VALUE).size();
//                    statsLabel.setText("Events in Store: " + eventCount);
//                });
//            }
//        }, 0, 1000);
//
//        add(controlPanel, BorderLayout.NORTH);
//        add(scrollPane, BorderLayout.CENTER);
//        add(statsPanel, BorderLayout.SOUTH);
//    }
//
//    private void subscribeToAllEvents() {
//        EventBus bus = EventBus.getInstance();
//        for (EventType type : EventType.values()) {
//            bus.subscribe(type, this);
//        }
//    }
//
//    @Override
//    public void onEvent(GameEvent event) {
//        SwingUtilities.invokeLater(() -> {
//            String eventInfo = formatEvent(event);
//            log("[EVENT RECEIVED] " + eventInfo);
//        });
//    }
//
//    @Override
//    public boolean matchesFilter(GameEvent event) {
//        return true; // Accept all events for monitoring
//    }
//
////    private void generateRandomEvent() {
////        EventType[] types = EventType.values();
////        EventType randomType = types[random.nextInt(types.length)];
////
////        GameEvent event = createMockEvent(randomType);
////
////        log("[PUBLISHING] " + formatEvent(event));
////        EventBus.getInstance().publish(event);
////    }
//
////    private GameEvent createMockEvent(EventType type) {
////        String playerName = "Player" + random.nextInt(100);
////        int playerId = 1000 + random.nextInt(9000);
////        int world = random.nextInt(3);
////        int channel = random.nextInt(20);
////
////        switch (type) {
////            case GACHAPON_REWARD:
////                return EventFactory.createGachaponEvent(
////                        world, channel, mockMap, playerName, playerId, mockItem
////                );
////
////            case LEVEL_UP:
////                return EventFactory.createLevelUpEvent(
////                        world, channel, mockMap, playerName, playerId,
////                        30 + random.nextInt(170)
////                );
////
////            case UPGRADE_RESULT:
////                return EventFactory.createUpgradeEvent(
////                        world, channel, mockMap, playerName, playerId,
////                        mockItem, random.nextBoolean()
////                );
////
////            case ITEM_MEGAPHONE:
////                return EventFactory.createItemMegaphoneEvent(
////                        world, channel, mockMap, playerName, playerId, mockItem
////                );
////
////            case CHAT_MEGAPHONE:
////                return EventFactory.createChatMegaphoneEvent(
////                        world, channel, mockMap, playerName, playerId,
////                        "Test message " + random.nextInt(1000)
////                );
////
////            default:
////                return new GameEvent(
////                        world, channel, mockMap, playerName, playerId,
////                        type, null, null
////                );
////        }
////    }
//
////    private void toggleAutoGenerate() {
////        if (autoGenerateCheckbox.isSelected()) {
////            autoGenerator = new Timer();
////            autoGenerator.scheduleAtFixedRate(new TimerTask() {
////                @Override
////                public void run() {
////                    generateRandomEvent();
////                }
////            }, 0, 2000); // Generate every 2 seconds
////        } else {
////            if (autoGenerator != null) {
////                autoGenerator.cancel();
////                autoGenerator = null;
////            }
////        }
////    }
//
//    private void queryEventStore() {
//        EventStore store = EventBus.getInstance().getEventStore();
//
//        // Create dialog for query options
//        String[] options = {"Recent Events", "By Type", "By Location", "Time Range"};
//        String choice = (String) JOptionPane.showInputDialog(
//                this, "Select query type:", "Query Event Store",
//                JOptionPane.QUESTION_MESSAGE, null, options, options[0]
//        );
//
//        if (choice == null) return;
//
//        switch (choice) {
//            case "Recent Events":
//                int count = Integer.parseInt(
//                        JOptionPane.showInputDialog(this, "How many recent events?", "10")
//                );
//                store.getRecentEvents(count).forEach(event ->
//                        log("[STORE QUERY] " + formatEvent(event))
//                );
//                break;
//
//            case "By Type":
//                EventType type = (EventType) JOptionPane.showInputDialog(
//                        this, "Select event type:", "Query by Type",
//                        JOptionPane.QUESTION_MESSAGE, null,
//                        EventType.values(), EventType.GACHAPON_REWARD
//                );
//                if (type != null) {
//                    store.getEventsByType(type).forEach(event ->
//                            log("[STORE QUERY] " + formatEvent(event))
//                    );
//                }
//                break;
//
//            case "By Location":
//                String worldStr = JOptionPane.showInputDialog(this, "World (0-2):", "0");
//                String channelStr = JOptionPane.showInputDialog(this, "Channel (0-19):", "0");
//                int world = Integer.parseInt(worldStr);
//                int channel = Integer.parseInt(channelStr);
//                store.getEventsByLocation(world, channel, null).forEach(event ->
//                        log("[STORE QUERY] " + formatEvent(event))
//                );
//                break;
//
//            case "Time Range":
//                long now = System.currentTimeMillis();
//                long fiveMinAgo = now - (5 * 60 * 1000);
//                store.getEventsInTimeRange(fiveMinAgo, now).forEach(event ->
//                        log("[STORE QUERY - Last 5 min] " + formatEvent(event))
//                );
//                break;
//        }
//    }
//
//    private String formatEvent(GameEvent event) {
//        StringBuilder sb = new StringBuilder();
//        sb.append(String.format("ID:%d | ", event.getId()));
//        sb.append(String.format("Time:%s | ", timeFormat.format(new Date(event.getTimestamp()))));
//        sb.append(String.format("Type:%s | ", event.getType()));
//        sb.append(String.format("W%d-C%d | ", event.getWorld(), event.getChannel()));
//        sb.append(String.format("Player:%s(%d) | ", event.getPlayerName(), event.getPlayerId()));
//        sb.append(String.format("Map:%s", event.getMap().getId()));
//
//        if (event.getItem() != null) {
//            sb.append(String.format(" | Item:%s", event.getItem().getItemId()));
//        }
//        if (event.getPass() != null) {
//            sb.append(String.format(" | Success:%s", event.getPass()));
//        }
//
//        return sb.toString();
//    }
//
//    private void log(String message) {
//        eventDisplay.append(timeFormat.format(new Date()) + " - " + message + "\n");
//        eventDisplay.setCaretPosition(eventDisplay.getDocument().getLength());
//    }
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> new EventMonitorTest());
//    }
//
////    // Mock classes for testing
////    static class MapleMap {
////        private int id;
////        private String name;
////
////        public MapleMap(int id, String name) {
////            this.id = id;
////            this.name = name;
////        }
////
////        public int getId() { return id; }
////        public String getName() { return name; }
////    }
////
////    static class Item {
////        private int id;
////        private String name;
////
////        public Item(int id, String name) {
////            this.id = id;
////            this.name = name;
////        }
////
////        public int getId() { return id; }
////        public String getName() { return name; }
////    }
//}