package soloMapling.ArtificialPlayer.BotMovementSystem;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.gms.net.packet.ByteBufInPacket;
import org.gms.net.packet.InPacket;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementPacket;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementPacketRaw;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.SingleMoveCommand;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementRecording;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementRecordingRaw;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static soloMapling.DebugUtilities.debugprint;

public class InPacketReader {

    private static final String movementDataPacketsPath = "src/main/java/soloMapling/ArtificialPlayer/BotMovementSystem/movementDataPackets/";

    public static boolean boolRecordMovementData = false;
    public static String movementDataRecordingName = "default_movement_recording";
    public static int movementRecordingMap = 0;

    public static MovementRecording getMovementRecording(int mapId, String recordingName) {
        String binFile = getMovementPacketBinaryFileName(mapId, recordingName);
        return new MovementRecording(mapId, recordingName, readPacketsFromFile(binFile));
    }

    public static MovementRecordingRaw getMovementRecordingRaw(int mapId, String recordingName) {
        String csvFile = getMovementPacketCsvFileName(mapId, recordingName);
        return new MovementRecordingRaw(mapId, recordingName, readRawPacketsFromFile(csvFile));
    }

    public static List<MovementPacket> readPacketsFromFile(String binaryFileName) {
        List<MovementPacket> packets = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(new FileInputStream(binaryFileName))) {
            while (dis.available() > 0) {
                packets.add(readSinglePacket(dis));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading packets from file: " + binaryFileName, e);
        }
        return packets;
    }

    public static List<MovementPacketRaw> readRawPacketsFromFile(String csvFileName) {
        List<MovementPacketRaw> packetList = new LinkedList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFileName))) {
            LineReader lineReader = new LineReader(reader);
            String line;

            while ((line = lineReader.readLine()) != null) {
                // Read A Single Raw Packet
                long timestamp = parseTimestamp(line);

                line = lineReader.readLine();
                if (line == null) break;
                int numCommands = parseNumCommands(line);

                List<SingleMoveCommand> packets = readMovementPackets(lineReader, numCommands);
                packetList.add(new MovementPacketRaw(timestamp, numCommands, packets));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading packets from file: " + csvFileName, e);
        }

        return packetList;
    }

    private static long parseTimestamp(String line) {
        try {
            return Long.parseLong(line.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid timestamp format: " + line, e);
        }
    }

    private static int parseNumCommands(String line) {
        try {
            return Integer.parseInt(line.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number of commands format: " + line, e);
        }
    }

    private static List<SingleMoveCommand> readMovementPackets(LineReader lineReader, int numCommands) throws IOException {
        List<SingleMoveCommand> packets = new ArrayList<>();

        for (int i = 0; i < numCommands; i++) {
            String line = lineReader.readLine();
            if (line == null) break;
            if (!line.contains(",")) {
                lineReader.goBack(line);
                break; // sometimes numCommands might be too large due to incomplete parser
            }

            SingleMoveCommand packet = parseMovementPacket(line);
            if (packet != null) {
                packets.add(packet);
            }
        }
        return packets;
    }

    private static SingleMoveCommand parseMovementPacket(String line) {
        String[] parts = line.split(",");

        if (parts.length != 8) {
            throw new IllegalArgumentException("Invalid packet format: " + line);
        }

        try {
            byte command = Byte.parseByte(parts[0]);
            short xpos = Short.parseShort(parts[1]);
            short ypos = Short.parseShort(parts[2]);
            short xwobble = Short.parseShort(parts[3]);
            short ywobble = Short.parseShort(parts[4]);
            short fh = Short.parseShort(parts[5]);
            byte newState = Byte.parseByte(parts[6]);
            short duration = Short.parseShort(parts[7]);

            return new SingleMoveCommand(command, xpos, ypos, xwobble, ywobble, fh, newState, duration);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid packet data: " + line, e);
        }
    }

    private static MovementPacket readSinglePacket(DataInputStream dis) throws IOException {
        long timestamp = dis.readLong(); // Read the timestamp
        int length = dis.readInt(); // Read the length of the binary data
        byte[] data = new byte[length];
        dis.readFully(data); // Read the binary data
        return new MovementPacket(timestamp, byteArrayToInPacket(data));
    }

    public static void recordMovementInPacketToBinaryAndCSV(InPacket p) {
        long timestamp = (System.currentTimeMillis());
        InPacket packet = p.copy();
        MovementPacket mp = new MovementPacket(timestamp, packet);
        MovementPacket mp2 = new MovementPacket(timestamp, packet);
        try {
            debugprint(mp2.toString());
            appendMovePacketDataToBinary(mp);
            appendRawDataToCsv(mp2);

        } catch (IOException e) {
            System.out.println("Unable to write MovementPacket to binary or csv");
        }
    }

    public static String getPacketRecordingFileNameBinary() {
        int mapId = getMovementDataRecordingMapId();
        String recordingName = getMovementDataRecordingName();
        return getMovementPacketBinaryFileName(mapId, recordingName);
    }

    public static String getPacketRecordingFileNameCsv() {
        int mapId = getMovementDataRecordingMapId();
        String recordingName = getMovementDataRecordingName();
        return getMovementPacketCsvFileName(mapId, recordingName);
    }

    public static String getMovementPacketBinaryFileName(int mapId, String fileName) {
        return getMovementPacketFileName(mapId, fileName, "bin");
    }

    public static String getMovementPacketCsvFileName(int mapId, String fileName) {
        return getMovementPacketFileName(mapId, fileName, "csv");
    }

    public static String getMovementPacketFileName(int mapId, String fileName, String extension) {
        return String.format("%smap%d/%s.%s", movementDataPacketsPath, mapId, fileName, extension);
    }

    public static void appendMovePacketDataToBinary(MovementPacket packet) throws IOException {
        writePacketToFile(getPacketRecordingFileNameBinary(), packet);
    }

    // Method to write packets to a binary file
    public static void writePacketToFile(String fullFileName, MovementPacket packet) throws IOException {
        ensureDirectoryExists(fullFileName);
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(fullFileName, true))) {
            dos.writeLong(packet.getTimestamp()); // Write the timestamp (8 bytes)
            byte[] data = packet.getPacket().getBytes();
            dos.writeInt(data.length); // Write the length of the binary data (4 bytes)
            dos.write(data); // Write the binary data
        }
    }

    public static InPacket byteArrayToInPacket(byte[] fullData) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(fullData);
        InPacket inPacket = new ByteBufInPacket(byteBuf);
        return inPacket;
    }

    public static void appendRawDataToCsv(MovementPacket movementPacket) {
        Object[] parsedData = MovementPacket.parseNumCommandMovementPacketRecFromPacket(movementPacket);
        long timestamp = movementPacket.getTimestamp();
        int numCommands = ((Byte) parsedData[0]).intValue();
        List<SingleMoveCommand> recordList = (List<SingleMoveCommand>) parsedData[1];
        MovementPacketRaw mpr = new MovementPacketRaw(timestamp, numCommands, recordList);
        writeRawDataToCsv(mpr);
    }

    public static void writeRawDataToCsv(MovementPacketRaw mpr) {
        String fileName = getPacketRecordingFileNameCsv();
        ensureDirectoryExists(fileName);
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writeLine(writer, String.valueOf(mpr.getTimestamp()));
            writeLine(writer, String.valueOf(mpr.getNumCommands()));

            for (SingleMoveCommand record : mpr.getRecordList()) {
                writeLine(writer, buildCsvLine(record));
            }
        } catch (IOException e) {
            System.out.println("Error while writing to CSV file: " + e.getMessage());
        }
    }

    private static void writeLine(FileWriter writer, String line) throws IOException {
        writer.write(line + "\n");
    }

    private static String buildCsvLine(SingleMoveCommand record) {
        String rawMoveData = String.join(",",
                Byte.toString(record.getCommand()),
                Short.toString(record.getXpos()),
                Short.toString(record.getYpos()),
                Short.toString(record.getXwobble()),
                Short.toString(record.getYwobble()),
                Short.toString(record.getFh()),
                Byte.toString(record.getNewstate()),
                Short.toString(record.getDuration())
        );
        debugprint(rawMoveData);
        return rawMoveData;
    }

    public static void ensureDirectoryExists(String fullFileName) {
        try {
            // Extract the directory path from the full file path
            Path directoryPath = Paths.get(fullFileName).getParent();

            if (directoryPath != null && !Files.exists(directoryPath)) {
                // Create the directory if it doesn't exist
                Files.createDirectories(directoryPath);
                System.out.println("Directory created: " + directoryPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to create directory for: " + fullFileName);
        }
    }

    public static void setMoveDataRecording(boolean rec) {
        boolRecordMovementData = rec;
    }

    public static boolean getMoveDataRecording() {
        return boolRecordMovementData;
    }

    public static void setMovementDataRecordingName(String recName) {
        movementDataRecordingName = recName;
    }

    public static String getMovementDataRecordingName() {
        return movementDataRecordingName;
    }

    public static void setMovementDataRecordingMapId(int mapId) {
        movementRecordingMap = mapId;
    }

    public static int getMovementDataRecordingMapId() {
        return movementRecordingMap;
    }

    public static class LineReader {
        private BufferedReader reader;
        private String previousLine;

        public LineReader(BufferedReader reader) {
            this.reader = reader;
            this.previousLine = null;
        }

        public String readLine() throws IOException {
            String line;
            if (previousLine != null) {
                line = previousLine;
                previousLine = null; // Reset after "going back"
            } else {
                line = reader.readLine();
            }
            return line;
        }

        public void goBack(String line) {
            this.previousLine = line; // Save the line to "go back"
        }
    }

    public static void test() {
        MovementRecordingRaw mcr = getMovementRecordingRaw(910000001, "bintest1");
        System.out.println(mcr.getMapId());
        System.out.println(mcr.getRecordingName());
        List<MovementPacketRaw> mpr = mcr.getMovementPacketList();
        for (MovementPacketRaw mpr_ : mpr) {
            System.out.println(mpr_.getTimestamp());
            System.out.println(mpr_.getNumCommands());
            List<SingleMoveCommand> mpr__ = mpr_.getRecordList();
            for (SingleMoveCommand mprec : mpr__) {
                System.out.println(Arrays.toString(mprec.getFieldValues()));
            }
        }
    }

    public static void main(String[] args) {
        test();
    }

}