package scripting;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.script.ScriptEngine;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ScriptEvaluationTest {
    private AbstractScriptManager scriptManager = new AbstractScriptManager() {};

    @BeforeAll
    static void muteGraal() {
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
    }

    private static List<String> npcScriptFilePaths() throws IOException {
        return getScriptFilePaths("npc");
    }

    private static List<String> eventScriptFilePaths() throws IOException {
        return getScriptFilePaths("event");
    }

    private static List<String> getScriptFilePaths(final String scriptsSubdirectory) throws IOException {
        Path scriptDirectory = Path.of("scripts", scriptsSubdirectory);
        try (Stream<Path> pathStream = Files.walk(scriptDirectory)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .map(path -> "%s/%s".formatted(scriptsSubdirectory, path.getFileName().toString()))
                    .toList();
        }
    }

    @ParameterizedTest
    @MethodSource("eventScriptFilePaths")
    void eventScriptShouldEvaluate(String eventScriptPath) {
        ScriptEngine scriptEngine = scriptManager.getInvocableScriptEngine(eventScriptPath);

        assertNotNull(scriptEngine);
    }

    @ParameterizedTest
    @MethodSource("npcScriptFilePaths")
    void npcScriptShouldEvaluate(String npcScriptPath) {
        ScriptEngine scriptEngine = scriptManager.getInvocableScriptEngine(npcScriptPath);

        assertNotNull(scriptEngine);
    }
}
