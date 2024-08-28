package utils;

import tools.StringUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class CreateXmlUtil {
    private final PrintWriter pw;
    private int indent;

    /**
     * @param outputDirectory 输出目录，如：wz/String.wz
     * @param imgName         文件名，如：Npc（保存为Npc.img.xml）
     */
    public CreateXmlUtil(String outputDirectory, String imgName) {
        try {
            Path OUTPUT_DIRECTORY = Path.of(outputDirectory);
            Path path = OUTPUT_DIRECTORY.resolve(imgName + ".img.xml");
            pw = new PrintWriter(Files.newOutputStream(path));
            printXmlHead(imgName + ".img");
            indent = 1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printXmlHead(String imgName) {
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        pw.println("<imgdir name=\"" + imgName + "\">");
    }

    private void printXmlData(String context) {
        pw.println("    ".repeat(Math.max(0, indent)) + context);
    }

    /**
     * 插入目录开始符
     *
     * @param name 目录名
     */
    public void insertImgdir(String name) {
        printXmlData("<imgdir name=\"" + name + "\"/>");
        indent++;
    }

    /**
     * 插入目录结束符
     */
    public void closeImgdir() {
        printXmlData("</imgdir>");
        indent--;
    }

    /**
     * 插入一条数据
     *
     * @param name    名称
     * @param type    数据类型，常见的有：int、string、null、short
     * @param context 数据内容
     */
    public void insertData(String name, String type, String context) {
        printXmlData("<" + type + " name=\"" + name + "\" value=\"" + StringUtil.covertHtml(context) + "\"/>");
    }

    /**
     * 关闭并保存文件
     */
    public void closeAndSave() {
        pw.println("</imgdir>");
        pw.close();
    }
}
