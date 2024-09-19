import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

public class XmlNode {
    @Getter
    public enum NodeType {
        IMG_DIR("imgdir"),
        CANVAS("canvas"),
        SOUND("sound"),
        UOL("uol"),
        DOUBLE("double"),
        FLOAT("float"),
        INT("int"),
        SHORT("short"),
        STRING("string"),
        VECTOR("vector"),
        NULL("null"),
        ;

        private final String type;

        NodeType(String type) {
            this.type = type;
        }

        public static NodeType getByType(String type) {
            for (NodeType value : NodeType.values()) {
                if (value.type.equals(type)) {
                    return value;
                }
            }
            return null;
        }

        public boolean isParent() {
            return Objects.equals(type, IMG_DIR.type) || Objects.equals(type, CANVAS.type);
        }

        public boolean hasWH() {
            return Objects.equals(type, CANVAS.type);
        }

        public boolean onlyName() {
            return Objects.equals(type, IMG_DIR.type) || Objects.equals(type, SOUND.type) || Objects.equals(type, NULL.type);
        }

        public boolean hasXY() {
            return Objects.equals(type, VECTOR.type);
        }
    }

    @Data
    public static class XmlParent {
        private List<XmlParent> parents;
        private List<XmlChild> children;
        private NodeType type;
        private String name;
        private String width;
        private String height;
    }

    @Data
    public static class XmlChild {
        private NodeType type;
        private String name;
        private String value;
        private String x;
        private String y;
    }
}
