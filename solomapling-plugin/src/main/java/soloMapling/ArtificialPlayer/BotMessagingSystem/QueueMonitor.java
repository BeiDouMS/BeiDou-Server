package soloMapling.ArtificialPlayer.BotMessagingSystem;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.*;

import static soloMapling.ArtificialPlayer.BotMessagingSystem.MessageQueue.getInstance;

public class QueueMonitor extends JFrame {
    private JTextArea queueContents;
    private MessageQueue messageQueue = getInstance();

    public QueueMonitor() {

        this.messageQueue = messageQueue;

        setTitle("Queue Monitor");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // Changed this line

        queueContents = new JTextArea();
        queueContents.setEditable(false);

        // Set a larger font size (adjust the size as needed)
        Font largerFont = new Font(queueContents.getFont().getName(), Font.PLAIN, 32);
        queueContents.setFont(largerFont);

        add(new JScrollPane(queueContents), BorderLayout.CENTER);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::updateContents, 0, 1, TimeUnit.SECONDS);

        // Add a window listener to stop the executor when the window is closed
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                executor.shutdown();
            }
        });

    }

    private void updateContents() {
        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder();

            BlockingQueue<ChatMessage> queue1 = this.messageQueue.getQueue("primary");
            sb.append("Queue 1 contents:\n");
            for (ChatMessage message : queue1) {
                sb.append("Primary: " + message.getSender().getName() + ": " + message.getContent() + ", Timestamp: " + message.getTimestamp() + "\n");
            }
            sb.append("\n==========================================\n");
            BlockingQueue<ChatMessage> queue2 = this.messageQueue.getQueue("secondary");
            sb.append("Queue 2 contents:\n");
            for (ChatMessage message : queue2) {
                sb.append("Secondary: " + message.getSender().getName() + ": " + message.getContent() + ", Timestamp: " + message.getTimestamp() + "\n");
            }

            sb.append("\n==========================================\n");
            BlockingQueue<ChatMessage> queue3 = this.messageQueue.getQueue("tertiary");
            sb.append("Queue 3 contents:\n");
            for (ChatMessage message : queue3) {
                sb.append("Tertiary: " + message.getSender().getName() + ": " + message.getContent() + ", Timestamp: " + message.getTimestamp() + "\n");
            }

            queueContents.setText(sb.toString());
        });
    }

    public void run() {
        SwingUtilities.invokeLater(() -> {
            new QueueMonitor().setVisible(true);
        });
    }
}