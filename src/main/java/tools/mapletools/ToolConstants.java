package tools.mapletools;

import java.io.File;

class ToolConstants {
    static final File INPUT_DIRECTORY = new File("tools/input");
    static final File OUTPUT_DIRECTORY = new File("tools/output");
    static final String SCRIPTS_PATH = "scripts";
    static final String HANDBOOK_PATH = "handbook";

    static File getInputFile(String fileName) {
        return new File(INPUT_DIRECTORY, fileName);
    }

    static File getOutputFile(String fileName) {
        return new File(OUTPUT_DIRECTORY, fileName);
    }
}
