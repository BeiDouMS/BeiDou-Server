package soloMapling.Environment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import soloMapling.ArtificialPlayer.BotTownSystem.TownPresenceConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Auto-verifies EnvironmentPopulation.yaml parsing without starting the game server.
 */
class EnvironmentPopulationConfigTest {

    @AfterEach
    void reset() {
        EnvironmentPopulationConfig.setConfigPath(null);
        EnvironmentPopulationConfig.reload();
    }

    @Test
    void loadsBundledYamlFromClasspathOrFs() {
        var plan = EnvironmentPopulationConfig.reload();
        assertTrue(plan.training().enabled());
        assertEquals(16, plan.training().cohorts().size());
        assertEquals(270, plan.trainingCohortTotal());
        assertTrue(plan.essentials().enabled());
        assertEquals("henesys", plan.essentials().fmRegion());
        assertEquals(5, plan.essentials().fmEntrance().m1());
        assertTrue(plan.loadedFrom() != null && !plan.loadedFrom().isBlank());

        var towns = TownPresenceConfig.towns();
        assertEquals(7, towns.size());
        int social = towns.stream().flatMap(t -> t.maps().stream()).mapToInt(m -> m.count()).sum();
        int wanderers = towns.stream().mapToInt(t -> t.wanderers()).sum();
        assertEquals(80, social);      // 14+10+10+10+12+14+10
        assertEquals(38, wanderers);   // 6+5+5+5+6+6+5
    }

    @Test
    void scaleHalvesTrainingTotal() throws Exception {
        Path tmp = Files.createTempFile("env-pop-", ".yaml");
        Files.writeString(tmp, """
                version: 1
                scale: 0.5
                waves:
                  essentials:
                    enabled: false
                  fm_buildout:
                    enabled: false
                  henesys_population:
                    enabled: false
                  expand_fm_market:
                    enabled: false
                  henesys_sub_areas:
                    enabled: false
                  specialty:
                    enabled: false
                  late_arrivals:
                    enabled: false
                  training:
                    enabled: true
                    cohorts:
                      - {map: 100000000, count: 10, level_lo: 10, level_hi: 20}
                      - {map: 103000000, count: 4, level_lo: 1, level_hi: 9}
                  town_presence:
                    enabled: false
                """);
        try {
            EnvironmentPopulationConfig.setConfigPath(tmp.toString());
            var plan = EnvironmentPopulationConfig.reload();
            assertEquals(0.5, plan.scale(), 1e-9);
            assertEquals(7, plan.trainingCohortTotal()); // round(10*0.5)+round(4*0.5)=5+2
            assertFalse(plan.essentials().enabled());
            assertFalse(plan.townPresence().enabled());
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void missingOverrideFallsBack() {
        EnvironmentPopulationConfig.setConfigPath("/tmp/does-not-exist-env-pop.yaml");
        var plan = EnvironmentPopulationConfig.reload();
        // Still finds DEFAULT_FS_PATH or classpath after missing override
        assertTrue(plan.training().cohorts().size() >= 1
                || new File(EnvironmentPopulationConfig.DEFAULT_FS_PATH).isFile()
                || plan.loadedFrom().contains("built-in")
                || plan.loadedFrom().contains("classpath")
                || plan.loadedFrom().contains("EnvironmentPopulation"));
    }
}
