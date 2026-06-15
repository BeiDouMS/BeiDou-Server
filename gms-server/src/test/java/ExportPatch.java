import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * wz 补丁导出工具。
 * <p>
 * 读取 git 提交记录，将指定提交（不含该提交本身）之后 {@code gms-server/wz} 和
 * {@code gms-server/wz-zh-CN} 目录下新增/修改的文件按目录结构复制到输出路径，
 * 删除的文件输出到控制台。
 * </p>
 *
 * <h3>常量可修改项：</h3>
 * <ul>
 *   <li>{@link #FROM_COMMIT} 起始提交 hash（不含该提交，导出它之后的所有变更）</li>
 *   <li>{@link #OUT_BASE}    输出根路径，null 则默认 桌面/upgrade_yyyyMMdd</li>
 *   <li>{@link #PREFIXES}    需要扫描的目录前缀</li>
 * </ul>
 *
 * <h3>用法：</h3>
 * <pre>
 * // 方式1：直接运行 main()
 * java ExportPatch
 *
 * // 方式2：IDE 中右键运行 {@link #export()} 测试方法
 * </pre>
 */
public class ExportPatch {

    /** 起始提交 hash（不含该提交）。修改此值来改变导出范围。 */
    private static final String FROM_COMMIT = "27529d68";

    /** 输出根路径，{@code null} 表示使用默认值：桌面/upgrade_yyyyMMdd */
    private static final String OUT_BASE = null;

    /** 需要扫描的目录前缀（相对仓库根） */
    private static final String[] PREFIXES = {"gms-server/wz", "gms-server/wz-zh-CN"};

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    // ---- 主入口 ----

    /**
     * 直接运行即可导出补丁。
     * <p>
     * 默认参数同上方常量，也可通过命令行指定：
     * <pre>
     *   java ExportPatch 27529d68 /d/my_patch
     * </pre>
     */
    public static void main(String[] args) {
        String fromCommit = args.length > 0 ? args[0] : FROM_COMMIT;
        String outBase = args.length > 1 ? args[1] : resolveOutBase();
        export(fromCommit, outBase);
    }

    /** JUnit5 入口，IDE 中右键直接运行此方法。 */
    @Test
    void export() {
        export(FROM_COMMIT, resolveOutBase());
    }

    // ---- 核心逻辑 ----

    static void export(String fromCommit, String outBase) {
        System.out.println("==========================================");
        System.out.println("  补丁导出工具");
        System.out.println("  起始提交: " + fromCommit + " (不含该提交)");
        System.out.println("  输出路径: " + outBase);
        System.out.println("==========================================");

        Path gitDir = findRepoRoot(Path.of("").toAbsolutePath().normalize());
        System.out.println("  仓库路径: " + gitDir);
        System.out.println();

        // 1. 先删除整个输出根目录
        Path outPath = Path.of(outBase);
        deleteDirIfExists(outPath);

        for (String prefix : PREFIXES) {
            System.out.println(">>> " + prefix + "/");
            System.out.println();

            // 输出目录取前缀最后一段，例如 "gms-server/wz" -> "wz"
            // 避免输出时出现 gms-server/wz 嵌套
            String shortName = lastSegment(prefix);
            Path targetDir = outPath.resolve(shortName);

            // 2. 收集新增/修改文件并复制
            List<String> changedFiles = gitChangedFiles(fromCommit, prefix, gitDir);

            if (changedFiles.isEmpty()) {
                System.out.println("  (无新增/修改)");
            } else {
                for (String file : changedFiles) {
                    Path src = gitDir.resolve(file);
                    if (Files.isRegularFile(src)) {
                        // 去掉前缀，例如 "gms-server/wz/Quest.wz/Act.img.xml" -> "Quest.wz/Act.img.xml"
                        Path dst = targetDir.resolve(file.substring(prefix.length() + 1));
                        try {
                            Files.createDirectories(dst.getParent());
                            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("  + " + file);
                        } catch (IOException e) {
                            System.err.println("  ! 复制失败: " + file + " - " + e.getMessage());
                        }
                    } else {
                        System.out.println("  ! 文件不存在(跳过): " + file);
                    }
                }
            }

            // 3. 列出删除文件
            List<String> deletedFiles = gitDeletedFiles(fromCommit, prefix, gitDir);
            System.out.println();
            System.out.println("  [删除文件]");
            if (deletedFiles.isEmpty()) {
                System.out.println("    (无)");
            } else {
                for (String file : deletedFiles) {
                    System.out.println("    [DEL] " + file);
                }
            }
            System.out.println();
        }

        // 摘要
        System.out.println("==========================================");
        System.out.println("  导出完成");
        for (String prefix : PREFIXES) {
            int added = gitChangedFiles(fromCommit, prefix, gitDir).size();
            int deleted = gitDeletedFiles(fromCommit, prefix, gitDir).size();
            System.out.println("  " + prefix + "/  新增/修改: " + added + "  删除: " + deleted);
        }
        System.out.println("  输出路径: " + outBase);
        System.out.println("==========================================");
    }

    // ---- Git 操作 ----

    /** 获取指定前缀下新增/修改的文件列表 */
    private static List<String> gitChangedFiles(String fromCommit, String prefix, Path gitDir) {
        return gitLog(fromCommit, prefix, "ACMR", gitDir);
    }

    /** 获取指定前缀下删除的文件列表 */
    private static List<String> gitDeletedFiles(String fromCommit, String prefix, Path gitDir) {
        return gitLog(fromCommit, prefix, "D", gitDir);
    }

    private static List<String> gitLog(String fromCommit, String prefix, String diffFilter, Path gitDir) {
        List<String> files = new ArrayList<>();
        try {
            Process process = new ProcessBuilder(
                    "git", "-c", "core.quotePath=false", "log",
                    fromCommit + "..HEAD",
                    "--diff-filter=" + diffFilter,
                    "--name-only",
                    "--pretty=format:",
                    "--",
                    prefix
            )
                    .directory(gitDir.toFile())
                    .redirectErrorStream(true)
                    .start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    // git 路径含中文等特殊字符时会加双引号包裹，需要去掉
                    if (line.length() >= 2 && line.startsWith("\"") && line.endsWith("\"")) {
                        line = line.substring(1, line.length() - 1);
                    }
                    if (!line.isEmpty()) {
                        files.add(line);
                    }
                }
            }
            process.waitFor();
        } catch (IOException e) {
            throw new UncheckedIOException("Git command failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // 去重保持顺序
        return files.stream().distinct().toList();
    }

    // ---- 工具方法 ----

    /** 取路径最后一段，例如 "gms-server/wz" -> "wz" */
    private static String lastSegment(String prefix) {
        int idx = prefix.lastIndexOf('/');
        return idx < 0 ? prefix : prefix.substring(idx + 1);
    }

    /** 向上查找仓库根（含 .git 目录的目录） */
    private static Path findRepoRoot(Path start) {
        Path current = start;
        while (current != null) {
            if (Files.isDirectory(current.resolve(".git"))) {
                return current;
            }
            current = current.getParent();
        }
        return start;
    }

    private static String resolveOutBase() {
        if (OUT_BASE != null) {
            return OUT_BASE;
        }
        String home = System.getProperty("user.home");
        String dateStr = LocalDate.now().format(DATE_FMT);
        return home + "/Desktop/upgrade_" + dateStr;
    }

    private static void deleteDirIfExists(Path dir) {
        if (Files.exists(dir)) {
            try (Stream<Path> walk = Files.walk(dir)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                System.err.println("  ! 删除失败: " + path + " - " + e.getMessage());
                            }
                        });
            } catch (IOException e) {
                System.err.println("  ! 无法删除目录: " + dir + " - " + e.getMessage());
            }
        }
    }
}
