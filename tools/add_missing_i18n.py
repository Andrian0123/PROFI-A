#!/usr/bin/env python3
"""Add missing string keys to locale files from values-en. Keys from values/ are the source of truth."""
from __future__ import annotations

import re
import sys
from pathlib import Path


def get_keys(path: Path) -> set[str]:
    text = path.read_text(encoding="utf-8")
    return set(re.findall(r'<string name="([^"]+)"', text))


def get_key_values(path: Path) -> dict[str, str]:
    text = path.read_text(encoding="utf-8")
    # Match <string name="key">value</string>; value may contain newlines
    pattern = re.compile(r'<string name="([^"]+)">(.*?)</string>', re.DOTALL)
    out = {}
    for m in pattern.finditer(text):
        key, val = m.group(1), m.group(2)
        out[key] = val.strip()
    return out


def main() -> int:
    root = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res"
    default_keys = get_keys(root / "values" / "strings.xml")
    en_kv = get_key_values(root / "values-en" / "strings.xml")
    locales = ["values-de", "values-ro", "values-uz", "values-tg", "values-kk"]

    for loc in locales:
        path = root / loc / "strings.xml"
        loc_keys = get_keys(path)
        missing = sorted(default_keys - loc_keys)
        if not missing:
            print(f"{loc}: OK, no missing keys")
            continue
        # Keys missing in en: use key as placeholder
        to_add = []
        for k in missing:
            val = en_kv.get(k, k.replace("_", " "))
            to_add.append(f'    <string name="{k}">{val}</string>')
        block = "\n".join(to_add)
        content = path.read_text(encoding="utf-8")
        if not content.strip().endswith("</resources>"):
            print(f"{loc}: unexpected end of file")
            return 1
        new_content = content.replace("</resources>", "\n\n    <!-- Added for parity -->\n" + block + "\n</resources>")
        path.write_text(new_content, encoding="utf-8")
        print(f"{loc}: added {len(missing)} keys")
    return 0


if __name__ == "__main__":
    sys.exit(main())
