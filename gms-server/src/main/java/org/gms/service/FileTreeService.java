package org.gms.service;

import lombok.extern.slf4j.Slf4j;
import org.gms.exception.BizException;
import org.gms.model.dto.FileTreeNodeDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class FileTreeService {

    private static final String BASE_DIR = System.getProperty("user.dir");
    private static final String FILE_TREE_KEY_DELIMITER = "-";
    private static final boolean PATH_STRICT_MODE = true;

    public String readFile(String currentKey, String filename) {
        File file = resolveByTreeKey(currentKey);
        if (!filename.equals(file.getName())) throw new BizException("文件目录发生变动，请重新读取");
        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (MalformedInputException e) {
            log.error("file {} is not using utf8", filename);
            try {
                return Files.readString(file.toPath(), StandardCharsets.ISO_8859_1);
            } catch (IOException ex) {
                log.error("io error", ex);
                throw new BizException("读取文件异常");
            }
        } catch (IOException e) {
            log.error("io error", e);
            throw new BizException("读取文件异常");
        }
    }

    public void writeFile(String currentKey, String filename, String content) {
        File file = resolveByTreeKey(currentKey);
        if (!filename.equals(file.getName())) throw new BizException("文件目录发生变动，请重新写入");
        try {
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("io error", e);
            throw new BizException("读取文件异常");
        }
    }


    public List<FileTreeNodeDTO> tree(String currentKey) {
        // 入参为空表示在根目录
        boolean root = !StringUtils.hasText(currentKey);
        File current = root ? new File(BASE_DIR) : resolveByTreeKey(currentKey);

        if (!current.isDirectory()) throw new BizException("请输入文件夹");

        File[] listFiles = current.listFiles();
        if (listFiles == null) {
            return Collections.emptyList();
        }

        List<FileTreeNodeDTO> nodes = new ArrayList<>();
        for (int i = 0; i < listFiles.length; i++) {
            String childKey = String.valueOf(i);
            String key = root ? childKey : String.join(FILE_TREE_KEY_DELIMITER, currentKey, childKey);
            nodes.add(new FileTreeNodeDTO(listFiles[i], key));
        }

        return nodes;
    }

    public File resolveByTreeKey(String currentKey) {
        File base = new File(BASE_DIR);
        File current = base;
        String[] keyArray = currentKey.split(FILE_TREE_KEY_DELIMITER);
        try {
            for (int i = 0; i < keyArray.length; i++) {
                int key = Integer.parseInt(keyArray[i]);
                current = Objects.requireNonNull(current.listFiles())[key];
            }
        } catch (NullPointerException | NumberFormatException e) {
            log.error("file not exists", e);
            throw new BizException("文件/文件夹不存在");
        }

        Path basePath = base.toPath().toAbsolutePath();
        Path userPath = current.toPath().toAbsolutePath().normalize();

        if (PATH_STRICT_MODE) {
            if (!userPath.startsWith(basePath)) {
                log.error("file escape : {}", userPath);
                throw new BizException("检测到路径逃逸尝试");
            }
        }

        return current;
    }

}

