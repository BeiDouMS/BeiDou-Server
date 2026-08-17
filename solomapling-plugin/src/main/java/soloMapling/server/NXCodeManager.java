package soloMapling.server;

import org.gms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static soloMapling.server.SoloMaplingUtilities.random;

public class NXCodeManager {

    /**
     * Creates a new NX code with the specified code string
     *
     * @param code The code string to insert
     * @return The generated ID of the new code entry
     */
    public static int createNXCode(String code) {
        int generatedId = -1;

        try (Connection con = DatabaseConnection.getConnection()) {
            // Get the next available ID
            int nextId = getNextNXCodeId(con);

            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO nxcode (id, code, retriever, expiration) VALUES (?, ?, ?, ?)")) {

                ps.setInt(1, nextId);
                ps.setString(2, code);
                ps.setNull(3, java.sql.Types.VARCHAR); // completely empty/null
                ps.setLong(4, 8901234567890L); // big expiration time

                ps.executeUpdate();
                generatedId = nextId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return generatedId;
    }

    /**
     * Creates a new NX code item entry linked to an existing code
     *
     * @param codeId   The ID of the nxcode entry to link to
     * @param quantity The quantity of NX (default 10000 for 10k NX)
     * @return The generated ID of the new code item entry
     */
    public static int createNXCodeItem(int codeId, int quantity) {
        int generatedId = -1;

        try (Connection con = DatabaseConnection.getConnection()) {
            // Get the next available ID
            int nextId = getNextNXCodeItemId(con);

            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO nxcode_items (id, codeid, type, item, quantity) VALUES (?, ?, ?, ?, ?)")) {

                ps.setInt(1, nextId);
                ps.setInt(2, codeId);
                ps.setInt(3, 4); // type 4 for prepaid nx
                ps.setInt(4, 0); // item id (not used for NX)
                ps.setInt(5, quantity);

                ps.executeUpdate();
                generatedId = nextId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return generatedId;
    }

    /**
     * Convenience method to create both nxcode and nxcode_items entries in one call
     *
     * @param code     The code string
     * @param quantity The quantity of NX (default 10000 for 10k NX)
     * @return The ID used for both entries
     */
    public static int createCompleteNXCode(String code, int quantity) {
        code = code.replace("-", "");
        int codeId = createNXCode(code);
        if (codeId > 0) {
            createNXCodeItem(codeId, quantity);
        }
        return codeId;
    }

    /**
     * Overloaded convenience method with default 10k NX quantity
     *
     * @param code The code string
     * @return The ID used for both entries
     */
    public static int createCompleteNXCode(String code) {
        return createCompleteNXCode(code, 10000);
    }

    /**
     * Helper method to get the next available ID for nxcode table
     */
    private static int getNextNXCodeId(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT MAX(id) FROM nxcode")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) + 1;
                }
            }
        }
        return 1; // Start from 1 if table is empty
    }

    /**
     * Helper method to get the next available ID for nxcode_items table
     */
    private static int getNextNXCodeItemId(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT MAX(id) FROM nxcode_items")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) + 1;
                }
            }
        }
        return 1; // Start from 1 if table is empty
    }

    /**
     * Generates a random gift card code in the format: XXXXXX-XXXXXX-XXXXXX
     * Uses only unambiguous characters (excludes 0, O, I, 1, L etc.)
     *
     * @return String of 20 characters total (18 alphanumeric + 2 dashes)
     */
    public static String generateGiftCardCode() {
        String characters = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
        StringBuilder codeBuilder = new StringBuilder();

        for (int section = 0; section < 3; section++) {
            if (section > 0) {
                codeBuilder.append("-");
            }

            for (int i = 0; i < 5; i++) {
                int randomIndex = random.nextInt(characters.length());
                codeBuilder.append(characters.charAt(randomIndex));
            }
        }
        return codeBuilder.toString();
    }

}