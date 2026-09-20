package org.gms.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 运行时资源基准目录解析器。
 *
 * <p>WZ 与脚本目录（wz/、wz-语言、scripts/、scripts-语言）原先使用 {@code Path.of("wz", ...)} 完全依赖
 * 当前工作目录（CWD）解析。开发态 IDE 把 Working directory 设为 gms-server 时能命中，但 jar 部署时在任意
 * 目录 {@code java -jar} 会导致目录找不到、{@code getData} 返回 null 进而 NPE。
 *
 * <p>本工具按以下顺序探测真实目录，彻底消除对启动 CWD 的依赖：
 * <ol>
 *     <li>显式覆盖：系统属性 {@code beidou.home} 或环境变量 {@code BEIDOU_HOME}；</li>
 *     <li>jar 位置候选根：基于启动 jar / classpath 入口目录，向上逐层探测；</li>
 *     <li>当前工作目录及其上级目录；</li>
 *     <li>均不存在时回退到 CWD 下的目录并打印告警日志，保持开发态原行为。</li>
 * </ol>
 *
 * <p>探测结果缓存为 volatile 单例，仅在首次访问时遍历一次文件系统。
 */
@Slf4j
public final class RuntimePaths {
    private static volatile Path wzHome;
    private static volatile Path scriptsHome;

    private RuntimePaths() {
    }

    public static Path getWzHome() {
        return getWzHome(null);
    }

    public static Path getWzHome(String language) {
        Path base = resolveWzHome();
        return withLanguage(base, language);
    }

    public static Path getScriptsHome() {
        return resolveScriptsHome();
    }

    public static Path getScriptsHome(String language) {
        Path base = resolveScriptsHome();
        return withLanguage(base, language);
    }

    private static Path withLanguage(Path base, String language) {
        if (language == null || language.isEmpty()) {
            return base;
        }
        return base.resolveSibling(base.getFileName() + "-" + language);
    }

    private static Path resolveWzHome() {
        Path cached = wzHome;
        if (cached != null) {
            return cached;
        }
        synchronized (RuntimePaths.class) {
            if (wzHome != null) {
                return wzHome;
            }
            wzHome = detectHome("wz");
            return wzHome;
        }
    }

    private static Path resolveScriptsHome() {
        Path cached = scriptsHome;
        if (cached != null) {
            return cached;
        }
        synchronized (RuntimePaths.class) {
            if (scriptsHome != null) {
                return scriptsHome;
            }
            scriptsHome = detectHome("scripts");
            return scriptsHome;
        }
    }

    private static Path detectHome(String dirName) {
        List<Path> candidates = new ArrayList<>();

        // 1. 显式覆盖：系统属性 beidou.home 或环境变量 BEIDOU_HOME
        String override = System.getProperty("beidou.home");
        if (override == null || override.isEmpty()) {
            override = System.getenv("BEIDOU_HOME");
        }
        if (override != null && !override.isEmpty()) {
            candidates.add(Path.of(override).resolve(dirName));
        }

        // 2. 基于启动 jar / classpath 入口目录，向上逐层探测
        Path jarDir = getLaunchDirectory();
        if (jarDir != null) {
            candidates.add(jarDir.resolve(dirName));
            Path parent = jarDir.getParent();
            if (parent != null) {
                candidates.add(parent.resolve(dirName));
                Path grandParent = parent.getParent();
                if (grandParent != null) {
                    candidates.add(grandParent.resolve(dirName));
                }
            }
        }

        // 3. 当前工作目录及其上级
        Path cwd = Path.of("").toAbsolutePath();
        candidates.add(cwd.resolve(dirName));
        Path cwdParent = cwd.getParent();
        if (cwdParent != null) {
            candidates.add(cwdParent.resolve(dirName));
        }

        for (Path candidate : candidates) {
            if (Files.isDirectory(candidate)) {
                log.info(I18nUtil.getLogMessage("RuntimePaths.info.resolve", dirName, candidate.toString()));
                return candidate;
            }
        }

        // 4. 均未命中：回退 CWD，并告警，保持开发态原行为
        Path fallback = cwd.resolve(dirName);
        log.warn(I18nUtil.getLogMessage("RuntimePaths.warn.notFound", dirName, fallback.toString()));
        return fallback;
    }

    /**
     * 获取启动入口（jar 或 classes 目录）所在目录，用于在 jar 部署时定位资源。
     */
    private static Path getLaunchDirectory() {
        // java -jar 启动时 java.class.path 首项即为 jar 路径，最可靠
        String classpath = System.getProperty("java.class.path");
        if (classpath != null && !classpath.isEmpty()) {
            String separator = System.getProperty("path.separator", File.pathSeparator);
            String first = classpath.split(separator, 2)[0];
            if (!first.isEmpty()) {
                Path cp = Path.of(first);
                if (Files.isRegularFile(cp) && cp.toString().endsWith(".jar")) {
                    return cp.getParent();
                }
                if (Files.isDirectory(cp)) {
                    return cp.toAbsolutePath();
                }
            }
        }
        return null;
    }
}
