#!/usr/bin/env python3
"""
MCP stdio client for batch IMG-to-XML conversion.
Launches WzImgMCP.exe in stdio mode and calls get_tree_structure for each .img file,
then converts the JSON tree to server-compatible XML.

Usage:
    python mcp_batch_convert.py <category> <mcp_exe_path> [wz_zh_cn_dir]
"""

import json
import os
import subprocess
import sys
import time

# Add scripts dir to path to import the XML converter
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))


class McpStdioClient:
    """Simple MCP JSON-RPC client over stdio."""

    def __init__(self, exe_path: str):
        self.process = subprocess.Popen(
            [exe_path, "--stdio"],
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            bufsize=1,
        )
        self.request_id = 0
        self._initialize()

    def _send(self, msg: dict):
        """Send a JSON-RPC message to the server."""
        line = json.dumps(msg, ensure_ascii=False)
        self.process.stdin.write(line + "\n")
        self.process.stdin.flush()

    def _recv(self) -> dict:
        """Receive a JSON-RPC response from the server."""
        while True:
            line = self.process.stdout.readline()
            if not line:
                raise EOFError("MCP server process ended unexpectedly")
            line = line.strip()
            if not line:
                continue
            return json.loads(line)

    def _call(self, method: str, params: dict = None) -> dict:
        """Make a JSON-RPC call and return the result."""
        self.request_id += 1
        msg = {
            "jsonrpc": "2.0",
            "id": self.request_id,
            "method": method,
            "params": params or {},
        }
        self._send(msg)
        return self._recv()

    def _notify(self, method: str, params: dict = None):
        """Send a JSON-RPC notification (no response expected)."""
        msg = {
            "jsonrpc": "2.0",
            "method": method,
            "params": params or {},
        }
        self._send(msg)

    def _initialize(self):
        """Perform MCP initialization handshake."""
        init_result = self._call("initialize", {
            "protocolVersion": "2024-11-05",
            "capabilities": {},
            "clientInfo": {"name": "batch-converter", "version": "1.0.0"},
        })
        if "error" in init_result:
            raise RuntimeError(f"MCP initialize failed: {init_result['error']}")
        self._notify("notifications/initialized")
        # Read the response (some servers send acknowledgment)
        time.sleep(0.1)

    def call_tool(self, tool_name: str, arguments: dict) -> dict:
        """Call an MCP tool and return the result content."""
        result = self._call("tools/call", {
            "name": tool_name,
            "arguments": arguments,
        })
        if "error" in result:
            return {"error": result["error"]}
        return result.get("result", {})

    def close(self):
        """Terminate the MCP server process."""
        try:
            self.process.stdin.close()
        except:
            pass
        try:
            self.process.terminate()
            self.process.wait(timeout=5)
        except:
            self.process.kill()


def convert_category(
    category: str,
    mcp_exe: str,
    wz_dir: str = r"E:\pro\BeiDou-Server\gms-server\wz-zh-CN",
    data_dir: str = r"E:\mxd_soft\2.客户端\083\BeiDou-Client\Data",
):
    """Convert all IMG files in a category to XML."""
    from md_to_xml import convert_tree_to_xml

    category_map = {
        'base': 'Base.wz', 'character': 'Character.wz', 'effect': 'Effect.wz',
        'etc': 'Etc.wz', 'item': 'Item.wz', 'map': 'Map.wz',
        'mob': 'Mob.wz', 'morph': 'Morph.wz', 'npc': 'Npc.wz',
        'quest': 'Quest.wz', 'reactor': 'Reactor.wz', 'skill': 'Skill.wz',
        'sound': 'Sound.wz', 'string': 'String.wz', 'tamingmob': 'TamingMob.wz',
        'ui': 'UI.wz',
    }

    wz_name = category_map.get(category.lower())
    if not wz_name:
        print(f"Unknown category: {category}")
        return

    output_dir = os.path.join(wz_dir, wz_name)
    os.makedirs(output_dir, exist_ok=True)

    print(f"Starting MCP server for category: {category} ({wz_name})")

    client = McpStdioClient(mcp_exe)

    try:
        # Initialize data source
        print("Initializing data source...")
        result = client.call_tool("init_data_source", {"basePath": data_dir})
        content = result.get("content", [{}])
        text = content[0].get("text", "") if content else ""
        print(f"Data source initialized: {text[:200]}")

        # List images in category
        print(f"Listing images in {category}...")
        result = client.call_tool("list_images_in_category", {"category": category})
        content = result.get("content", [{}])
        text = content[0].get("text", "") if content else ""

        # Parse image list from text response
        # The response is YAML-like, extract image names
        images = _parse_image_list(text)
        print(f"Found {len(images)} images")

        count = 0
        errors = []
        truncated = []

        for img_name in images:
            try:
                print(f"  [{count + 1}/{len(images)}] Processing {img_name}...")

                # Get tree structure
                result = client.call_tool("get_tree_structure", {
                    "category": category,
                    "image": img_name,
                    "path": "",
                    "depth": 10,
                    "maxChildrenPerNode": 200,
                })
                content = result.get("content", [{}])
                tree_text = content[0].get("text", "") if content else ""

                if not tree_text:
                    errors.append((img_name, "Empty response"))
                    continue

                # Parse the YAML tree into a JSON dict
                tree = _parse_tree_yaml(tree_text)
                if not tree:
                    errors.append((img_name, "Failed to parse tree"))
                    continue

                # Convert to XML
                xml_str = convert_tree_to_xml(tree)

                # Write output
                xml_name = img_name.replace('.img', '.img.xml')
                if not xml_name.endswith('.xml'):
                    xml_name = img_name + '.xml'
                output_path = os.path.join(output_dir, xml_name)
                with open(output_path, 'w', encoding='utf-8') as f:
                    f.write(xml_str)

                count += 1

                # Check for truncation
                if _check_truncation(tree):
                    truncated.append(img_name)

            except Exception as e:
                errors.append((img_name, str(e)))
                print(f"    ERROR: {e}")

    finally:
        client.close()

    print(f"\nCategory '{category}' ({wz_name}): {count}/{len(images)} files converted")
    if truncated:
        print(f"  WARNING: {len(truncated)} files were truncated:")
        for t in truncated:
            print(f"    - {t}")
    if errors:
        print(f"  ERROR: {len(errors)} files failed:")
        for f, e in errors:
            print(f"    - {f}: {e}")

    return count, truncated, errors


def _parse_image_list(text: str) -> list:
    """Parse image names from list_images_in_category text output."""
    import re
    images = []
    for match in re.finditer(r'name:\s+(\S+\.img)', text):
        images.append(match.group(1))
    return images


def _parse_tree_yaml(text: str) -> dict:
    """Parse get_tree_structure YAML output into a JSON tree."""
    # The text is a YAML-like structure. We need to convert it to the
    # format expected by our XML converter.
    # For now, save the YAML and use the md_to_xml parser
    # This is a placeholder - actual parsing depends on the exact format
    pass


def _check_truncation(tree: dict) -> bool:
    """Check if a tree has truncated children."""
    def _check(node):
        if node.get('truncated_children', 0) > 0:
            return True
        for child in node.get('children', []):
            if _check(child):
                return True
        return False
    return _check(tree)


if __name__ == '__main__':
    if len(sys.argv) < 3:
        print(__doc__)
        sys.exit(1)

    category = sys.argv[1]
    mcp_exe = sys.argv[2]
    wz_dir = sys.argv[3] if len(sys.argv) > 3 else r'E:\pro\BeiDou-Server\gms-server\wz-zh-CN'
    convert_category(category, mcp_exe, wz_dir)
