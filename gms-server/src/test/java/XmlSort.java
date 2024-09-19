import org.gms.util.StringUtil;
import org.junit.jupiter.api.Test;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

public class XmlSort {

    @Test
    public void xmlSort() throws Exception {
        // 源文件路径，支持文件夹
        String src = "wz/Item.wz/Cash/0501.img.xml";
        // 输出路径，只能是文件夹，否则就生成到桌面
        String dst = "";

        Path srcPath = Path.of(src);
        if (!Files.exists(srcPath)) {
            throw new RuntimeException("源文件或源文件夹不存在");
        }
        if (dst.isEmpty()) {
            dst = System.getProperty("user.home") + "/Desktop/";
        }
        Path dstPath = Path.of(dst);
        if (!Files.exists(dstPath) || !Files.isDirectory(dstPath)) {
            throw new RuntimeException("导出文件夹不存在");
        }
        resolvePath(srcPath, dstPath);
        System.out.println("导出完成：" + dstPath.toAbsolutePath());
    }

    private void resolvePath(Path srcPath, Path dstPath) throws Exception {
        if (Files.isDirectory(srcPath)) {
            try (Stream<Path> list = Files.list(srcPath);) {
                list.forEach(path -> {
                    try {
                        Path resolved = dstPath.resolve(path.getFileName());
                        resolvePath(path, resolved);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } else {
            if (!Files.exists(dstPath.getParent())) {
                Files.createDirectories(dstPath.getParent());
            }
            if (srcPath.toString().endsWith(".xml") && !dstPath.toString().endsWith(".xml")) {
                resolveFile(srcPath, dstPath.resolve(srcPath.getFileName()));
            } else {
                resolveFile(srcPath, dstPath);
            }
        }
    }

    private void resolveFile(Path srcPath, Path dstPath) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(srcPath.toFile());
        XmlNode.XmlParent xmlParent = new XmlNode.XmlParent();
        resolveParent(document, xmlParent);
        sortXml(xmlParent);
        writeToXml(xmlParent, builder, dstPath);
    }

    private void sortXml(XmlNode.XmlParent xmlParent) {
        if (xmlParent.getChildren() != null && !xmlParent.getChildren().isEmpty()) {
            xmlParent.getChildren().sort(Comparator.comparing(XmlNode.XmlChild::getName, xmlComparator));
        }
        if (xmlParent.getParents() != null && !xmlParent.getParents().isEmpty()) {
            xmlParent.getParents().sort(Comparator.comparing(XmlNode.XmlParent::getName));
            xmlParent.getParents().forEach(this::sortXml);
        }
    }

    private void writeToXml(XmlNode.XmlParent xmlParent, DocumentBuilder builder, Path dstPath) throws Exception {
        Document doc = builder.newDocument();
        addParentElement(doc, null, xmlParent);

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

        try (FileWriter fw = new FileWriter(dstPath.toFile())) {
            fw.write(xmlContent);
        }
    }

    private void addParentElement(Document doc, Element root, XmlNode.XmlParent xmlParent) {
        Element element = doc.createElement(xmlParent.getType().getType());
        element.setAttribute("name", xmlParent.getName());
        if (xmlParent.getType().hasWH()) {
            element.setAttribute("width", xmlParent.getWidth());
            element.setAttribute("height", xmlParent.getHeight());
        }
        if (xmlParent.getChildren() != null && !xmlParent.getChildren().isEmpty()) {
            xmlParent.getChildren().forEach(child -> addChildElement(doc, element, child));
        }
        if (xmlParent.getParents() != null && !xmlParent.getParents().isEmpty()) {
            xmlParent.getParents().forEach(parent -> addParentElement(doc, element, parent));
        }
        Objects.requireNonNullElse(root, doc).appendChild(element);
    }

    private void addChildElement(Document doc, Element root, XmlNode.XmlChild xmlChild) {
        Element element = doc.createElement(xmlChild.getType().getType());
        if (xmlChild.getType().hasXY()) {
            element.setAttribute("x", xmlChild.getX());
            element.setAttribute("y", xmlChild.getY());
        } else if (!xmlChild.getType().onlyName()) {
            element.setAttribute("value", xmlChild.getValue());
        }
        element.setAttribute("name", xmlChild.getName());
        if (root != null) {
            root.appendChild(element);
        }
    }

    private void resolveParent(Node parent, XmlNode.XmlParent xmlParent) {
        if (parent.getNodeType() == Node.DOCUMENT_NODE) {
            NodeList childNodes = parent.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                resolveParent(item, xmlParent);
            }
            return;
        }
        XmlNode.NodeType parentType = XmlNode.NodeType.getByType(parent.getNodeName());
        if (parentType == null) {
            System.out.println("Error type: " + parent.getNodeName());
            return;
        }
        if (parentType.isParent()) {
            xmlParent.setType(parentType);
            resolveParentData(parent, xmlParent);
            NodeList childNodes = parent.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                XmlNode.NodeType itemType = XmlNode.NodeType.getByType(item.getNodeName());
                if (itemType == null) {
                    continue;
                }
                if (itemType.isParent()) {
                    if (xmlParent.getParents() == null) {
                        xmlParent.setParents(new ArrayList<>());
                    }
                    XmlNode.XmlParent itemParent = new XmlNode.XmlParent();
                    xmlParent.getParents().add(itemParent);
                    resolveParent(item, itemParent);
                } else {
                    resolveChild(item, xmlParent);
                }
            }
        } else {
            resolveChild(parent, xmlParent);
        }
    }

    private void resolveChild(Node child, XmlNode.XmlParent xmlParent) {
        if (xmlParent.getChildren() == null) {
            xmlParent.setChildren(new ArrayList<>());
        }
        XmlNode.XmlChild xmlChild = new XmlNode.XmlChild();
        xmlParent.getChildren().add(xmlChild);
        XmlNode.NodeType childType = XmlNode.NodeType.getByType(child.getNodeName());
        xmlChild.setType(childType);
        resolveChildData(child, xmlChild);
    }

    private void resolveParentData(Node parent, XmlNode.XmlParent xmlParent) {
        NamedNodeMap attributes = parent.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node item = attributes.item(i);
            if (item.getNodeType() != Node.ATTRIBUTE_NODE) {
                continue;
            }
            if ("name".equals(item.getNodeName())) {
                xmlParent.setName(item.getNodeValue());
                if (xmlParent.getType().onlyName()) {
                    break;
                }
            }
            if (xmlParent.getType().hasWH()) {
                if ("height".equals(item.getNodeName())) {
                    xmlParent.setHeight(item.getNodeValue());
                } else if ("width".equals(item.getNodeName())) {
                    xmlParent.setWidth(item.getNodeValue());
                }
            }
        }
    }

    private void resolveChildData(Node child, XmlNode.XmlChild xmlChild) {
        NamedNodeMap attributes = child.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node item = attributes.item(i);
            if (item.getNodeType() != Node.ATTRIBUTE_NODE) {
                continue;
            }
            if ("name".equals(item.getNodeName())) {
                xmlChild.setName(item.getNodeValue());
                if (xmlChild.getType().onlyName()) {
                    break;
                }
            }
            if (xmlChild.getType().hasXY()) {
                if ("x".equals(item.getNodeName())) {
                    xmlChild.setX(item.getNodeValue());
                } else if ("y".equals(item.getNodeName())) {
                    xmlChild.setY(item.getNodeValue());
                }
            } else if (!xmlChild.getType().onlyName()) {
                xmlChild.setValue(item.getNodeValue());
            }
        }
    }

    Comparator<String> xmlComparator = (o1, o2) -> {
        // name优先排最前
        if ("name".equals(o1)) return -1;
        if ("name".equals(o2)) return 1;
        // id优先排最前
        if ("id".equals(o1)) return -1;
        if ("id".equals(o2)) return 1;
        // start优先排最前
        if ("start".equals(o1)) return -1;
        if ("start".equals(o2)) return 1;
        // end优先排最前
        if ("end".equals(o1)) return -1;
        if ("end".equals(o2)) return 1;
        // SN优先排最前
        if ("SN".equals(o1)) return -1;
        if ("SN".equals(o2)) return 1;
        // ItemId优先排最前
        if ("ItemId".equals(o1)) return -1;
        if ("ItemId".equals(o2)) return 1;
        // 数字按大小排序
        if (StringUtil.isNumeric(o1) && StringUtil.isNumeric(o2)) {
            return Long.valueOf(o1).compareTo(Long.valueOf(o2));
        }
        return o1.compareTo(o2);
    };
}
