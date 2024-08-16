import org.junit.jupiter.api.Test;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class XmlDiff {
    @Test
    public void stringDiff() throws Exception {
        // Comparer1
        final String comparerPath1 = "wz/String.wz/Eqp.img.xml";
        // Comparer2
        final String comparerPath2 = "wz-zh-CN/String.wz/Eqp.img.xml";

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
                        key = item.getNodeValue();;
                    } else if ("value".equals(item.getNodeName())) {
                        value = item.getNodeValue();;
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
}
