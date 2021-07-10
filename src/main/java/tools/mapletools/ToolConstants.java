package tools.mapletools;

import java.io.File;

public class ToolConstants {
    public static final File INPUT_DIRECTORY = new File("tools/input");
    public static final File OUTPUT_DIRECTORY = new File("tools/output");
    public static final String SCRIPTS_PATH = "scripts";

    public static File getInputFile(String fileName) {
        return new File(INPUT_DIRECTORY, fileName);
    }

    public static File getOutputFile(String fileName) {
        return new File(OUTPUT_DIRECTORY, fileName);
    }
}
