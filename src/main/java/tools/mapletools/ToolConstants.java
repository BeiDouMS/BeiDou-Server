package tools.mapletools;

import java.io.File;

public class ToolConstants {
    public static final File INPUT_DIRECTORY = new File("tools/input");
    public static final File OUTPUT_DIRECTORY = new File("tools/output");

    public static File getOutputFile(String fileName) {
        return new File(OUTPUT_DIRECTORY, fileName);
    }
}
