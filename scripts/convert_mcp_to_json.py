#!/usr/bin/env python3
"""
Convert MCP get_tree_structure YAML-like output to JSON for img_tree_to_xml.py.
Usage:
    python convert_mcp_to_json.py <mcp_output.txt> <output.json>
"""
import json
import re
import sys


def parse_primitive(text):
    """Parse a scalar value from the MCP output."""
    if text is None or text == '':
        return None
    text = text.strip()
    if text.startswith('"') and text.endswith('"'):
        return text[1:-1]
    if text.startswith("'") and text.endswith("'"):
        return text[1:-1]
    try:
        if '.' in text:
            return float(text)
        return int(text)
    except ValueError:
        return text


def count_indent(line):
    """Count leading space characters."""
    return len(line) - len(line.lstrip(' '))


def parse_mcp_output(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        raw_lines = f.readlines()

    # Convert lines to (type, indent, key, value) tuples
    # type: 'item' (bare dash), 'kv' (key:value pair)
    parsed = []
    for line in raw_lines:
        stripped = line.rstrip('\r\n')
        if not stripped:
            continue
        indent = count_indent(stripped)
        content = stripped.lstrip(' ')

        if content == '-':
            parsed.append(('item', indent, None, None))
        elif content.startswith('- '):
            rest = content[2:]
            if ':' in rest:
                key, _, value = rest.partition(':')
                parsed.append(('kv', indent, key.strip(), value.strip()))
            else:
                parsed.append(('kv', indent, rest.strip(), None))
        # Ignore lines not starting with `- `

    # Find the `- tree:` entry -- tree content starts right after it
    tree_start = 0
    for i, (typ, indent, key, val) in enumerate(parsed):
        if typ == 'kv' and key == 'tree':
            tree_start = i + 1
            break

    tree_entries = parsed[tree_start:]

    # The tree root has:
    #   - children: at some indent R
    #   - name: at indent R
    #   - type: at indent R
    # Children items are bare `-` at indent R+1

    pos = [0]  # mutable position tracker

    def parse_children(level_indent):
        """Parse bare `-` items at level_indent and return list of child nodes."""
        children = []
        while pos[0] < len(tree_entries):
            typ, indent, key, val = tree_entries[pos[0]]
            if indent < level_indent:
                break
            if typ == 'item' and indent == level_indent:
                pos[0] += 1  # skip the bare `-`
                node = parse_node_attrs(level_indent)
                if node:
                    children.append(node)
            else:
                pos[0] += 1
        return children

    def parse_value_subattrs(parent_indent):
        """Parse value sub-attributes (child_count, width, height, etc.) at parent_indent+1."""
        result = {}
        sub_level = parent_indent + 2
        while pos[0] < len(tree_entries):
            typ, indent, key, val = tree_entries[pos[0]]
            if indent < sub_level:
                break
            if typ == 'kv':
                if key == 'child_count':
                    result['child_count'] = int(val)
                elif key == 'width':
                    result['width'] = int(val)
                elif key == 'height':
                    result['height'] = int(val)
                elif key == 'has_children':
                    result['has_children'] = val.lower() == 'true'
            pos[0] += 1
        return result

    def parse_node_attrs(item_level):
        """
        Parse a node's attributes at indent item_level+2.
        The node may have `- children:` (marker for child items at item_level+4),
        then `- name:`, `- type:`, `- value:` (with possible sub-attrs).
        Returns a node dict or None.
        """
        node_children = None
        node_name = None
        node_type = None
        node_value = None
        has_value = False  # flag to distinguish "no value" from "value=None"

        attr_level = item_level + 2

        while pos[0] < len(tree_entries):
            typ, indent, key, val = tree_entries[pos[0]]

            # Stop if we left this node's attribute indentation zone
            if indent < attr_level:
                break

            # Another bare dash at attr_level shouldn't happen inside a node
            if typ == 'item' and indent == attr_level:
                break

            if typ == 'kv' and indent == attr_level:
                if key == 'children':
                    # Children items are at attr_level + 2
                    pos[0] += 1
                    node_children = parse_children(attr_level + 2)
                    continue  # skip the pos+=1 at loop end
                elif key == 'name':
                    node_name = parse_primitive(val)
                elif key == 'type':
                    node_type = val
                elif key == 'value':
                    has_value = True
                    if val:
                        node_value = parse_primitive(val)
                    else:
                        # Complex value -- read sub-attrs
                        pos[0] += 1  # Advance past the 'value:' line
                        node_value = parse_value_subattrs(indent)
                        continue
                # skip truncated_children, success, etc.

            pos[0] += 1

        # Build node dict
        node = {}
        if node_name is not None:
            node['name'] = node_name
        if node_type is not None:
            node['type'] = node_type
        if has_value:
            node['value'] = node_value
        if node_children is not None and len(node_children) > 0:
            node['children'] = node_children

        return node if node_name is not None or node_type is not None else None

    # --- Parse the root node ---
    # The first tree entry is `- children:` at some indent R.
    # Root's children items are at R+1.
    # Root's own attrs (name, type) follow at indent R.
    root_children = None
    root_name = None
    root_type = None

    while pos[0] < len(tree_entries):
        typ, indent, key, val = tree_entries[pos[0]]

        if typ == 'kv' and key == 'success':
            break

        if typ == 'kv' and key == 'children':
            pos[0] += 1
            root_children = parse_children(indent + 2)
            continue
        elif typ == 'kv':
            if key == 'name':
                root_name = parse_primitive(val)
            elif key == 'type':
                root_type = val
            # skip truncated_children

        pos[0] += 1

    root = {
        'name': root_name or 'Check.img',
        'type': root_type or 'Image',
        'children': root_children or []
    }

    return root


if __name__ == '__main__':
    if len(sys.argv) != 3:
        print(f"Usage: {sys.argv[0]} <mcp_output.txt> <output.json>")
        sys.exit(1)

    tree = parse_mcp_output(sys.argv[1])

    with open(sys.argv[2], 'w', encoding='utf-8') as f:
        json.dump(tree, f, indent=2, ensure_ascii=False)

    # Count total nodes for info
    def count_nodes(node):
        cnt = 1
        for child in node.get('children', []):
            cnt += count_nodes(child)
        return cnt

    total = count_nodes(tree)
    print(f"Converted: {sys.argv[1]} -> {sys.argv[2]}")
    print(f"Root: name={tree['name']}, type={tree['type']}, "
          f"children={len(tree['children'])}")
    print(f"Total nodes: {total}")
