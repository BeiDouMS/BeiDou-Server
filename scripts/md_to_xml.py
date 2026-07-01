#!/usr/bin/env python3
"""
Convert wzimg MCP export_to_md YAML output to server-compatible .img.xml format.

Usage:
    python md_to_xml.py <input_md_file> [output_xml_file]
    python md_to_xml.py --batch <md_dir> <output_dir>
"""

import os
import re
import sys
from xml.sax.saxutils import escape as _xml_escape

def xml_escape(value: str) -> str:
    """Escape XML special chars including quotes for attribute values."""
    return _xml_escape(str(value), {'"': '&quot;'})


def parse_md_to_tree(filepath: str) -> dict:
    """Parse a wzimg export_to_md YAML file into a tree structure."""
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    # The YAML format uses:
    # - Lines with `- key: value` for leaf properties
    # - Lines with `- key:` followed by indented children
    # - 2-space indentation per level

    def get_indent(line: str) -> int:
        """Return indentation level (number of leading spaces)."""
        return len(line) - len(line.lstrip(' '))

    def parse_value_block(start_idx: int, parent_indent: int) -> tuple:
        """Parse a _value block, return (value_dict, next_idx)."""
        values = {}
        idx = start_idx
        while idx < len(lines):
            line = lines[idx]
            if not line.strip():
                idx += 1
                continue
            indent = get_indent(line)
            # Exit if we're back at or above the parent _value: indent
            if indent <= parent_indent:
                break
            stripped = line.strip()
            if stripped.startswith('- '):
                content = stripped[2:]
                if ': ' in content:
                    key, val = content.split(': ', 1)
                    key = key.strip()
                    val = val.strip()
                    # Try to convert to number
                    if val == 'true':
                        val = True
                    elif val == 'false':
                        val = False
                    else:
                        try:
                            if '.' in val:
                                val = float(val)
                            else:
                                val = int(val)
                        except ValueError:
                            pass
                    values[key] = val
                idx += 1
            else:
                break
        return values, idx

    def parse_children_block(start_idx: int, base_indent: int) -> tuple:
        """Parse a _children block, return (children_list, next_idx)."""
        children = []
        idx = start_idx
        while idx < len(lines):
            line = lines[idx]
            if not line.strip():
                idx += 1
                continue
            indent = get_indent(line)
            if indent <= base_indent:
                break
            # Children are structured as:
            #   - childName:
            #     - _name: childName
            #     ...
            stripped = line.strip()
            if stripped.startswith('- ') and stripped.endswith(':') and not ': ' in stripped[2:-1]:
                child_name = stripped[2:-1].strip()
                child, idx = parse_node(idx, indent)
                child['name'] = child.get('name', child_name)
                children.append(child)
            else:
                idx += 1
        return children, idx

    def parse_node(start_idx: int, expected_indent: int = 0) -> tuple:
        """Parse a single node, return (node_dict, next_idx)."""
        node = {}
        idx = start_idx
        while idx < len(lines):
            line = lines[idx]
            if not line.strip():
                idx += 1
                continue
            indent = get_indent(line)
            if indent < expected_indent + 2 and node:
                break

            stripped = line.strip()
            if not stripped.startswith('- '):
                idx += 1
                continue

            content = stripped[2:]  # Remove '- '

            if ': ' in content:
                key, val_str = content.split(': ', 1)
                key = key.strip()
                val = val_str.strip()

                if key == '_name':
                    node['name'] = val
                elif key == '_type':
                    node['type'] = val
                elif key == '_width':
                    node['width'] = int(val)
                elif key == '_height':
                    node['height'] = int(val)
                elif key == '_value':
                    # Scalar value (Int, String, Float, UOL, etc.)
                    if val == 'true':
                        node['value'] = True
                    elif val == 'false':
                        node['value'] = False
                    else:
                        try:
                            if '.' in val:
                                node['value'] = float(val)
                            else:
                                node['value'] = int(val)
                        except ValueError:
                            node['value'] = val
                idx += 1

            elif content.endswith(':'):
                key = content[:-1].strip()

                if key == '_value':
                    value_dict, idx = parse_value_block(idx + 1, indent)
                    node['value'] = value_dict
                elif key == '_children':
                    # Children start at next line with deeper indentation
                    children, idx = parse_children_block(idx + 1, indent)
                    node['children'] = children
                else:
                    idx += 1
            else:
                idx += 1

        return node, idx

    tree, _ = parse_node(0, -2)
    return tree


def to_xml(node: dict, indent: int = 0) -> str:
    """Convert a parsed tree node to server-format XML string."""
    indent_str = " " * indent
    node_type = node.get('type', 'SubProperty')
    name = xml_escape(str(node.get('name', '')))
    value = node.get('value', {})

    # Container types (Image, SubProperty, WzImage)
    if node_type in ('SubProperty', 'Image', 'WzImage'):
        children = node.get('children', [])
        if not children:
            return f'{indent_str}<imgdir name="{name}"/>\n'
        inner = ''.join(to_xml(child, indent + 4) for child in children)
        return f'{indent_str}<imgdir name="{name}">\n{inner}{indent_str}</imgdir>\n'

    # Integer types
    elif node_type == 'Int':
        val = value if isinstance(value, (int, float)) else 0
        return f'{indent_str}<int name="{name}" value="{val}"/>\n'

    elif node_type == 'Short':
        val = value if isinstance(value, (int, float)) else 0
        return f'{indent_str}<short name="{name}" value="{val}"/>\n'

    elif node_type == 'Long':
        # Server XMLDomMapleData has no <long> handler; map to <int>
        # All v83 Long values fit within int32 range
        val = value if isinstance(value, (int, float)) else 0
        return f'{indent_str}<int name="{name}" value="{val}"/>\n'

    # String
    elif node_type == 'String':
        val = xml_escape(str(value)) if not isinstance(value, dict) else ''
        return f'{indent_str}<string name="{name}" value="{val}"/>\n'

    # Float types
    elif node_type == 'Float':
        val = value if isinstance(value, (int, float)) else 0.0
        return f'{indent_str}<float name="{name}" value="{val}"/>\n'

    elif node_type == 'Double':
        val = value if isinstance(value, (int, float)) else 0.0
        return f'{indent_str}<double name="{name}" value="{val}"/>\n'

    # Vector
    elif node_type == 'Vector':
        x = value.get('x', 0) if isinstance(value, dict) else 0
        y = value.get('y', 0) if isinstance(value, dict) else 0
        return f'{indent_str}<vector name="{name}" x="{x}" y="{y}"/>\n'

    # Canvas
    elif node_type == 'Canvas':
        width = node.get('width', value.get('width', 0) if isinstance(value, dict) else 0)
        height = node.get('height', value.get('height', 0) if isinstance(value, dict) else 0)
        children = node.get('children', [])
        if not children:
            return f'{indent_str}<canvas name="{name}" width="{width}" height="{height}"/>\n'
        inner = ''.join(to_xml(child, indent + 4) for child in children)
        return f'{indent_str}<canvas name="{name}" width="{width}" height="{height}">\n{inner}{indent_str}</canvas>\n'

    # Sound
    elif node_type == 'Sound':
        children = node.get('children', [])
        if not children:
            return f'{indent_str}<sound name="{name}"/>\n'
        inner = ''.join(to_xml(child, indent + 4) for child in children)
        return f'{indent_str}<sound name="{name}">\n{inner}{indent_str}</sound>\n'

    # UOL
    elif node_type == 'UOL':
        val = xml_escape(str(value)) if not isinstance(value, dict) else ''
        return f'{indent_str}<uol name="{name}" value="{val}"/>\n'

    # Null
    elif node_type == 'Null':
        return f'{indent_str}<null name="{name}"/>\n'

    # Convex (like SubProperty, with children)
    elif node_type == 'Convex':
        children = node.get('children', [])
        if not children:
            return f'{indent_str}<convex name="{name}"/>\n'
        inner = ''.join(to_xml(child, indent + 4) for child in children)
        return f'{indent_str}<convex name="{name}">\n{inner}{indent_str}</convex>\n'

    # Default
    else:
        children = node.get('children', [])
        if not children:
            return f'{indent_str}<imgdir name="{name}"/>\n'
        inner = ''.join(to_xml(child, indent + 4) for child in children)
        return f'{indent_str}<imgdir name="{name}">\n{inner}{indent_str}</imgdir>\n'


def convert_file(input_path: str, output_path: str = None) -> str:
    """Convert a single MD/YAML file to XML."""
    tree = parse_md_to_tree(input_path)
    header = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'
    xml_str = header + to_xml(tree)

    if output_path:
        os.makedirs(os.path.dirname(output_path) if os.path.dirname(output_path) else '.', exist_ok=True)
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(xml_str)
        print(f"Written: {output_path}")

    return xml_str


def convert_batch(md_dir: str, output_dir: str):
    """Convert all .md files in a directory to XML."""
    if not os.path.isdir(md_dir):
        print(f"Error: {md_dir} is not a directory")
        return

    count = 0
    for filename in sorted(os.listdir(md_dir)):
        if filename.endswith('.md'):
            input_path = os.path.join(md_dir, filename)
            xml_name = filename.replace('.md', '.xml')
            output_path = os.path.join(output_dir, xml_name)
            try:
                convert_file(input_path, output_path)
                count += 1
            except Exception as e:
                print(f"Error converting {filename}: {e}")

    print(f"Converted {count} files: {md_dir} -> {output_dir}")


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)

    if sys.argv[1] == '--batch':
        if len(sys.argv) != 4:
            print("Usage: md_to_xml.py --batch <md_dir> <output_dir>")
            sys.exit(1)
        convert_batch(sys.argv[2], sys.argv[3])
    else:
        input_path = sys.argv[1]
        output_path = sys.argv[2] if len(sys.argv) > 2 else None
        xml_str = convert_file(input_path, output_path)
        if not output_path:
            print(xml_str)
