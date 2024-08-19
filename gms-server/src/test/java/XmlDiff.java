import org.junit.jupiter.api.Test;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class XmlDiff {
    @Test
    public void stringDiff() throws Exception {
        // Comparer1
        final String comparerPath1 = "wz/String.wz/ToolTipHelp.img.xml";
        // Comparer2
        final String comparerPath2 = "wz-zh-CN/String.wz/ToolTipHelp.img.xml";
        // Writer
        final String writeName = "ToolTipHelp.img.xml";

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document parsed1 = builder.parse(new File(comparerPath1));
        Map<String, Object> result1 = new HashMap<>();
        resolveElement(result1, parsed1);
//        System.out.println(result1);

//        System.out.println();
        Document parsed2 = builder.parse(new File(comparerPath2));
        Map<String, Object> result2 = new HashMap<>();
        resolveElement(result2, parsed2);
//        System.out.println(result2);

        System.out.println(comparerPath1 + "存在，" + comparerPath2 + "不存在：");
        compare(new StringBuilder(), result1, result2);
        System.out.println();
        System.out.println(comparerPath2 + "存在，" + comparerPath1 + "不存在：");
        compare(new StringBuilder(), result2, result1);

        TreeMap<String, Object> treeMap = new TreeMap<>(nameDescComparator);
        merge(result1, result2, treeMap);
//        System.out.println();
//        System.out.println(result1);

        writeToXml(treeMap, builder, writeName);
    }

    private void resolveElement(Map<String, Object> inMap, Node root) throws Exception {
        NodeList childNodes = root.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                resolveAttribute(inMap, item);
            }
        }
    }

    private void resolveAttribute(Map<String, Object> inMap, Node root) throws Exception {
        NamedNodeMap attributes = root.getAttributes();
        if ("imgdir".equals(root.getNodeName())) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node item = attributes.item(i);
                if (item.getNodeType() == Node.ATTRIBUTE_NODE) {
                    if ("name".equals(item.getNodeName())) {
                        Map<String, Object> childMap = new HashMap<>();
                        inMap.put(item.getNodeValue(), childMap);
                        resolveElement(childMap, root);
                    }
                }
            }
        } else if ("string".equals(root.getNodeName())) {
            String key = null;
            String value = null;
            for (int i = 0; i < attributes.getLength(); i++) {
                Node item = attributes.item(i);
                if (item.getNodeType() == Node.ATTRIBUTE_NODE) {
                    if ("name".equals(item.getNodeName())) {
                        key = item.getNodeValue();
                    } else if ("value".equals(item.getNodeName())) {
                        value = item.getNodeValue();
                    }
                }
            }
            if (key != null) {
                inMap.put(key, value);
            }
        }
    }

    private void compare(StringBuilder pathBuilder, Map<String, Object> c1, Map<String, Object> c2) throws Exception {
        c1.forEach((k, v) -> {
            StringBuilder newPathBuilder = new StringBuilder(pathBuilder).append("/").append(k);
            Object o = c2.get(k);
            if (o == null) {
                System.out.println(newPathBuilder);
                return;
            }
            if (o instanceof Map) {
                try {
                    compare(newPathBuilder, (Map<String, Object>) v, (Map<String, Object>) o);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void merge(Map<String, Object> base, Map<String, Object> append, TreeMap<String, Object> treeMap) {
        for (Map.Entry<String, Object> entry : base.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();
            Object appendVal = append.get(key);
            if (appendVal == null) {
                treeMap.put(key, val);
                continue;
            }
            if (appendVal instanceof Map) {
                TreeMap<String, Object> childMap = new TreeMap<>(nameDescComparator);
                treeMap.put(key, childMap);
                merge((Map<String, Object>) val, (Map<String, Object>) appendVal, childMap);
            } else {
                treeMap.put(key, appendVal);
            }
        }
    }

    private void writeToXml(Map<String, Object> base, DocumentBuilder builder, String writeName) throws Exception {
        Document doc = builder.newDocument();
        Element root = doc.createElement("imgdir");
        String rootKey = base.keySet().iterator().next();
        root.setAttribute("name", rootKey);
        doc.appendChild(root);

        addMapToElement(doc, root, base.get(rootKey));

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 4);
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        // 这么写不知道为什么写不进去？
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");

        StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        String xmlContent = sw.toString();

        // 手动添加 XML 声明，确保 standalone="yes"
        String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
        xmlContent = xmlHeader + xmlContent.substring(xmlContent.indexOf("?>") + 2);

        String dest = System.getProperty("user.home") + "/Desktop/" + writeName;
        try (FileWriter fw = new FileWriter(dest)) {
            fw.write(xmlContent);
        }
    }

    private static void addMapToElement(Document document, Element parent, Object data) {
        if (data instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) data;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Element child;
                if (entry.getValue() instanceof Map) {
                    child = document.createElement("imgdir");
                    child.setAttribute("name", entry.getKey());
                } else {
                    child = document.createElement("string");
                    child.setAttribute("name", entry.getKey());
                    child.setAttribute("value", entry.getValue().toString());
                }
                parent.appendChild(child);
                addMapToElement(document, child, entry.getValue());
            }
        }
    }

    private static String toSafeStr(String str) {
        // 转义特殊字符
        return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    Comparator<String> nameDescComparator = (key1, key2) -> {
        // 定义特定的排序规则
        if ("name".equals(key1)) return -1; // "name" 排在前面
        if ("name".equals(key2)) return 1;
        if ("desc".equals(key1)) return 1;  // "desc" 排在后面
        if ("desc".equals(key2)) return -1;
        return key1.compareTo(key2);       // 其他键按照自然顺序排序
    };
}
