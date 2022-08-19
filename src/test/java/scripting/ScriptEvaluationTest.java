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

    public static List<String> npcScriptFiles() throws IOException {
        Path npcScriptDirectory = Path.of("scripts", "npc");
        try (Stream<Path> pathStream = Files.walk(npcScriptDirectory)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .map(ScriptEvaluationTest::mapToNpcScriptPath)
                    .toList();
        }
    }

    private static String mapToNpcScriptPath(Path path) {
        return "npc/%s".formatted(path.getFileName().toString());
    }

    @ParameterizedTest
    @MethodSource("npcScriptFiles")
    void npcScriptShouldEvaluate(String npcScriptPath) {
        ScriptEngine scriptEngine = scriptManager.getInvocableScriptEngine(npcScriptPath);

        assertNotNull(scriptEngine);
    }
}
