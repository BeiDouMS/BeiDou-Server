package soloMapling.itemPool;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ItemDataParserXML {

    private static final String ROOT_PATH = "wz/Character.wz";
    private static final Map<Integer, Map<String, String>> itemCache = new ConcurrentHashMap<>();

    public static int getValue(int itemId, String attributeName) {
        Map<String, String> data = itemCache.computeIfAbsent(itemId, ItemDataParserXML::loadItemData);
        try {
            return Integer.parseInt(data.getOrDefault(attributeName, "-1"));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static Map<String, String> loadItemData(int itemId) {
        String fileName = String.format("%08d.img.xml", itemId);
        Path basePath = Paths.get(ROOT_PATH);
        Map<String, String> data = new HashMap<>();

        try {
            Optional<Path> filePath = Files.walk(basePath)
                    .filter(p -> p.getFileName().toString().equals(fileName))
                    .findFirst();

            if (filePath.isEmpty()) {
                throw new FileNotFoundException("Item XML not found: " + itemId);
            }

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(filePath.get().toFile());

            NodeList infoNodes = doc.getElementsByTagName("imgdir");

            for (int i = 0; i < infoNodes.getLength(); i++) {
                Element elem = (Element) infoNodes.item(i);
                if ("info".equals(elem.getAttribute("name"))) {
                    NodeList children = elem.getChildNodes();
                    for (int j = 0; j < children.getLength(); j++) {
                        Node child = children.item(j);
                        if (child.getNodeType() == Node.ELEMENT_NODE) {
                            Element e = (Element) child;
                            String name = e.getAttribute("name");
                            String value = e.getAttribute("value");
                            if (!name.isEmpty() && !value.isEmpty()) {
                                data.put(name, value);
                            }
                        }
                    }
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to parse item " + itemId + ": " + e.getMessage());
        }

        return data;
    }

    private static void test() {
        int itemId = 1002357;

        System.out.println("reqJob: " + ItemDataParserXML.getValue(itemId, "reqJob"));
        System.out.println("reqLevel: " + ItemDataParserXML.getValue(itemId, "reqLevel"));
        System.out.println("price: " + ItemDataParserXML.getValue(itemId, "price"));
        System.out.println("cash: " + ItemDataParserXML.getValue(itemId, "cash"));
        System.out.println("incSTR: " + ItemDataParserXML.getValue(itemId, "incSTR"));
        System.out.println("tradeBlock: " + ItemDataParserXML.getValue(itemId, "tradeBlock"));

    }

    public static void main(String[] args) {
        test();
    }

}
