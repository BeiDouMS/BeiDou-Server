#!/usr/bin/env python3
"""
Convert wzimg MCP get_tree_structure JSON to server-compatible .img.xml format.

Usage:
    python img_tree_to_xml.py <input_json_file> [output_xml_file]
    python img_tree_to_xml.py --batch <json_dir> <output_dir>
"""

import json
import os
import sys
import xml.sax.saxutils as saxutils
from xml.dom import minidom


def escape_xml(value: str) -> str:
    """Escape special XML characters."""
    return saxutils.escape(str(value))


def node_to_xml(node: dict, indent: int = 0) -> str:
    """Convert a single tree node to XML string."""
    node_type = node.get("type", "SubProperty")
    name = node.get("name", "")
    indent_str = " " * indent

    # SubProperty / Image / imgdir container
    if node_type in ("SubProperty", "Image"):
        tag_name = "imgdir"
        children = node.get("children", [])
        if not children:
            return f'{indent_str}<{tag_name} name="{escape_xml(name)}"/>\n'
        inner = "".join(node_to_xml(child, indent + 4) for child in children)
        return f'{indent_str}<{tag_name} name="{escape_xml(name)}">\n{inner}{indent_str}</{tag_name}>\n'

    # Integer types
    elif node_type == "Int":
        value = node.get("value", 0)
        return f'{indent_str}<int name="{escape_xml(name)}" value="{value}"/>\n'

    elif node_type == "Short":
        value = node.get("value", 0)
        return f'{indent_str}<short name="{escape_xml(name)}" value="{value}"/>\n'

    elif node_type == "Long":
        value = node.get("value", 0)
        return f'{indent_str}<long name="{escape_xml(name)}" value="{value}"/>\n'

    # String
    elif node_type == "String":
        value = node.get("value", "")
        return f'{indent_str}<string name="{escape_xml(name)}" value="{escape_xml(value)}"/>\n'

    # Float types
    elif node_type == "Float":
        value = node.get("value", 0.0)
        return f'{indent_str}<float name="{escape_xml(name)}" value="{value}"/>\n'

    elif node_type == "Double":
        value = node.get("value", 0.0)
        return f'{indent_str}<double name="{escape_xml(name)}" value="{value}"/>\n'

    # Vector (2D point)
    elif node_type == "Vector":
        v = node.get("value", {})
        x = v.get("x", 0)
        y = v.get("y", 0)
        return f'{indent_str}<vector name="{escape_xml(name)}" x="{x}" y="{y}"/>\n'

    # Canvas (image with metadata, no binary)
    elif node_type == "Canvas":
        v = node.get("value", {})
        width = v.get("width", 0)
        height = v.get("height", 0)
        children = node.get("children", [])
        if not children:
            return f'{indent_str}<canvas name="{escape_xml(name)}" width="{width}" height="{height}"/>\n'
        inner = "".join(node_to_xml(child, indent + 4) for child in children)
        return f'{indent_str}<canvas name="{escape_xml(name)}" width="{width}" height="{height}">\n{inner}{indent_str}</canvas>\n'

    # Sound (audio metadata only, no binary)
    elif node_type == "Sound":
        children = node.get("children", [])
        if not children:
            return f'{indent_str}<sound name="{escape_xml(name)}"/>\n'
        inner = "".join(node_to_xml(child, indent + 4) for child in children)
        return f'{indent_str}<sound name="{escape_xml(name)}">\n{inner}{indent_str}</sound>\n'

    # UOL (link/reference)
    elif node_type == "UOL":
        value = node.get("value", "")
        return f'{indent_str}<uol name="{escape_xml(name)}" value="{escape_xml(value)}"/>\n'

    # Null
    elif node_type == "Null":
        return f'{indent_str}<null name="{escape_xml(name)}"/>\n'

    else:
        # Unknown type, treat as imgdir for safety
        children = node.get("children", [])
        if not children:
            return f'{indent_str}<imgdir name="{escape_xml(name)}"/>\n'
        inner = "".join(node_to_xml(child, indent + 4) for child in children)
        return f'{indent_str}<imgdir name="{escape_xml(name)}">\n{inner}{indent_str}</imgdir>\n'


def convert_tree_to_xml(tree: dict) -> str:
    """Convert a full tree structure to a complete XML document."""
    header = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'
    body = node_to_xml(tree, 0)
    return header + body


def convert_file(input_path: str, output_path: str = None):
    """Convert a single JSON tree file to XML."""
    with open(input_path, 'r', encoding='utf-8') as f:
        tree = json.load(f)

    xml_str = convert_tree_to_xml(tree)

    if output_path:
        os.makedirs(os.path.dirname(output_path), exist_ok=True)
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(xml_str)
        print(f"Written: {output_path}")
    else:
        print(xml_str)

    return xml_str


def convert_batch(json_dir: str, output_dir: str):
    """Convert all JSON tree files in a directory to XML."""
    if not os.path.isdir(json_dir):
        print(f"Error: {json_dir} is not a directory")
        return

    count = 0
    for filename in os.listdir(json_dir):
        if filename.endswith('.json'):
            input_path = os.path.join(json_dir, filename)
            # Convert .img.json → .img.xml
            xml_name = filename.replace('.json', '.xml')
            if not xml_name.endswith('.img.xml'):
                # Ensure correct extension
                base = os.path.splitext(filename)[0]
                xml_name = base + '.xml'
            output_path = os.path.join(output_dir, xml_name)
            convert_file(input_path, output_path)
            count += 1

    print(f"Converted {count} files: {json_dir} → {output_dir}")


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)

    if sys.argv[1] == '--batch':
        if len(sys.argv) != 4:
            print("Usage: img_tree_to_xml.py --batch <json_dir> <output_dir>")
            sys.exit(1)
        convert_batch(sys.argv[2], sys.argv[3])
    else:
        input_path = sys.argv[1]
        output_path = sys.argv[2] if len(sys.argv) > 2 else None
        convert_file(input_path, output_path)
