package org.gms.provider;

import lombok.Getter;
import org.gms.property.ServiceProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Centralized server resource path resolver.
 * <p>
 * Auto-detects the {@code dataHome} directory (where {@code wz/}, {@code scripts/} etc. live)
 * so that WZ data and game scripts can be found regardless of JVM working directory —
 * whether running from IDE (project root or gms-server/), or as a packaged JAR.
 * </p>
 *
 * <h3>Auto-detection priority</h3>
 * <ol>
 *   <li>{@code gms.service.data-home} config property — explicit override</li>
 *   <li>{@code ./wz-zh-CN} or {@code ./wz} exists → {@code dataHome = ./}</li>
 *   <li>{@code ./gms-server/wz-zh-CN} or {@code ./gms-server/wz} exists → {@code dataHome = ./gms-server}</li>
 *   <li>{@code user.dir} — fallback</li>
 * </ol>
 */
@Component
public class ServerResourceResolver {

    private static final Logger log = LoggerFactory.getLogger(ServerResourceResolver.class);

    /** Detected server data home directory. */
    @Getter
    private final Path dataHome;

    public ServerResourceResolver(ServiceProperty serviceProperty) {
        this.dataHome = resolveDataHome(serviceProperty.getDataHome());
        log.info("Server data home: {}", dataHome.toAbsolutePath().normalize());
    }

    // ── Public API ──────────────────────────────────────────────────────

    /**
     * Resolve a relative path under the data home directory.
     *
     * @param first first path segment (e.g. {@code "wz-zh-CN"} or {@code "scripts"})
     * @param more  additional path segments (e.g. {@code "Quest.wz"})
     * @return the resolved path (may not exist; caller handles missing files)
     */
    public Path resolveDataPath(String first, String... more) {
        return dataHome.resolve(Path.of(first, more));
    }

    // ── Detection logic ─────────────────────────────────────────────────

    private static Path resolveDataHome(String configured) {
        // 1. Explicit configuration — highest priority
        if (configured != null && !configured.isBlank()) {
            Path path = Path.of(configured).toAbsolutePath().normalize();
            log.info("Using configured data-home: {}", path);
            return path;
        }

        // 2. Check if current working directory contains the data dirs
        Path cwd = Path.of("").toAbsolutePath().normalize();
        if (hasDataDirs(cwd)) {
            return cwd;
        }

        // 3. Check if gms-server/ subdirectory contains the data dirs (IDE from project root)
        Path gmsServer = cwd.resolve("gms-server");
        if (Files.isDirectory(gmsServer) && hasDataDirs(gmsServer)) {
            return gmsServer;
        }

        // 4. Fall back to working directory (may fail later with clear errors)
        log.warn("No wz/ or wz-zh-CN/ found in {} or {}/gms-server/, using working directory as fallback",
                cwd, cwd);
        return cwd;
    }

    private static boolean hasDataDirs(Path base) {
        return Files.isDirectory(base.resolve("wz"))
                || Files.isDirectory(base.resolve("wz-zh-CN"));
    }
}
