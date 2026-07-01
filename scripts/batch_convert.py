#!/usr/bin/env python3
"""
Batch convert all exported MD files to XML and copy to wz-zh-CN.

Usage: python batch_convert.py <category_name> <md_dir> [wz_zh_cn_dir]
"""

import os
import sys
import shutil

# Add scripts dir to path for import
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from md_to_xml import convert_file


CATEGORY_MAP = {
    'base': 'Base.wz', 'character': 'Character.wz', 'effect': 'Effect.wz',
    'etc': 'Etc.wz', 'item': 'Item.wz', 'map': 'Map.wz',
    'mob': 'Mob.wz', 'morph': 'Morph.wz', 'npc': 'Npc.wz',
    'quest': 'Quest.wz', 'reactor': 'Reactor.wz', 'skill': 'Skill.wz',
    'sound': 'Sound.wz', 'string': 'String.wz', 'tamingmob': 'TamingMob.wz',
    'ui': 'UI.wz',
}


def batch_convert(category: str, md_dir: str, wz_dir: str):
    wz_name = CATEGORY_MAP.get(category.lower())
    if not wz_name:
        print(f"Unknown category: {category}")
        return

    output_dir = os.path.join(wz_dir, wz_name)
    os.makedirs(output_dir, exist_ok=True)

    count = 0
    errors = []
    truncated = []

    for filename in sorted(os.listdir(md_dir)):
        if not filename.endswith('.md'):
            continue

        input_path = os.path.join(md_dir, filename)
        xml_name = filename.replace('.md', '.xml')
        output_path = os.path.join(output_dir, xml_name)

        try:
            convert_file(input_path, output_path)
            count += 1
        except Exception as e:
            errors.append((filename, str(e)))

    print(f"Category '{category}' ({wz_name}): {count} files converted")
    if truncated:
        print(f"  WARNING: {len(truncated)} files were truncated (5000 node limit):")
        for t in truncated:
            print(f"    - {t}")
    if errors:
        print(f"  ERROR: {len(errors)} files failed:")
        for f, e in errors:
            print(f"    - {f}: {e}")

    return count, truncated, errors


if __name__ == '__main__':
    if len(sys.argv) < 3:
        print(__doc__)
        sys.exit(1)

    category = sys.argv[1]
    md_dir = sys.argv[2]
    wz_dir = sys.argv[3] if len(sys.argv) > 3 else r'E:\pro\BeiDou-Server\gms-server\wz-zh-CN'
    batch_convert(category, md_dir, wz_dir)
