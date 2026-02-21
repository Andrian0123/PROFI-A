#!/usr/bin/env python3
"""Checks translation key parity: values vs values-en and vs all locales (de, ro, uz, tg, kk)."""

from __future__ import annotations

import pathlib
import re
import sys


STRING_RE = re.compile(r'<string\s+name="([^"]+)"')

RES_DIR = pathlib.Path("app") / "src" / "main" / "res"
DEFAULT_VALUES = "values"
LOCALES = ["values-en", "values-de", "values-ro", "values-uz", "values-tg", "values-kk"]


def extract_keys(path: pathlib.Path) -> set[str]:
    content = path.read_text(encoding="utf-8")
    return set(STRING_RE.findall(content))


def main() -> int:
    root = pathlib.Path(__file__).resolve().parents[1]
    res_root = root / RES_DIR
    default_file = res_root / DEFAULT_VALUES / "strings.xml"

    if not default_file.exists():
        print("ERROR: values/strings.xml not found")
        return 1

    default_keys = extract_keys(default_file)
    failed = False

    for locale in LOCALES:
        path = res_root / locale / "strings.xml"
        if not path.exists():
            print(f"WARN: {locale}/strings.xml not found, skip")
            continue
        loc_keys = extract_keys(path)
        missing = sorted(default_keys - loc_keys)
        extra = sorted(loc_keys - default_keys)

        if missing:
            print(f"Missing in {locale}/strings.xml ({len(missing)}):")
            for key in missing:
                print(f"  - {key}")
            failed = True
        if extra:
            print(f"Extra in {locale}/strings.xml ({len(extra)}):")
            for key in extra[:20]:
                print(f"  - {key}")
            if len(extra) > 20:
                print(f"  ... and {len(extra) - 20} more")
            # extra keys do not fail the check
        if not missing and not extra:
            print(f"OK: {locale} ({len(loc_keys)} keys)")
        elif not missing:
            print(f"OK: {locale} ({len(loc_keys)} keys, {len(extra)} extra)")

    if failed:
        print("\nFAILED: i18n parity check (missing keys in some locales)")
        return 1

    print(f"\nOK: i18n parity check passed for all locales (reference: {len(default_keys)} keys)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
