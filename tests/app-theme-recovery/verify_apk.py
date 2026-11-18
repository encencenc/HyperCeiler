#!/usr/bin/env python3
"""Check the built APK's theme attributes in both day and night mode (requires androguard)."""

import argparse

from androguard.core.apk import APK
from androguard.core.axml import TYPE_REFERENCE
from loguru import logger


def resolve_attribute(resources, style_id, attribute_id, night):
    seen = set()
    while style_id and style_id not in seen:
        seen.add(style_id)
        entries = {config.get_qualifier(): entry for config, entry in resources.get_res_configs(style_id)}
        entry = entries.get("night" if night else "", entries.get(""))
        if entry is None:
            return None
        items = dict(entry.item.items)
        if attribute_id in items:
            return items[attribute_id]
        style_id = entry.item.id_parent
    return None


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("apk")
    args = parser.parse_args()
    logger.disable("androguard")
    apk = APK(args.apk)
    resources = apk.get_android_resources()
    package = apk.get_package()
    expected = {
        "AppTheme": ("actionBarSplitBackground",),
        "SubSettingsTheme": ("actionBarSplitBackground",),
        "LogViewerTheme": ("actionBarSplitBackground",),
        "CrashTheme": ("textColorList", "textColorListSecondary"),
    }
    failed = []
    for theme, attributes in expected.items():
        style_id = resources.get_res_id_by_key(package, "style", theme)
        for name in attributes:
            attribute_id = resources.get_res_id_by_key(package, "attr", name)
            for night in (False, True):
                value = resolve_attribute(resources, style_id, attribute_id, night)
                if value is None or value.get_data_type() != TYPE_REFERENCE:
                    failed.append(f"{theme} ({'night' if night else 'day'}): unresolved {name}")
    if failed:
        raise SystemExit("\n".join(failed))
    print("Settings, crash report, and log viewer theme attributes resolve in day and night mode")


if __name__ == "__main__":
    main()
