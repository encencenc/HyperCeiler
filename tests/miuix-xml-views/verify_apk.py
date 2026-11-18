#!/usr/bin/env python3
"""Check that compiled Miuix AndroidBaseWidget layouts can find their XML constructors."""

import argparse
from xml.etree import ElementTree

from androguard.core.apk import APK
from androguard.core.axml import AXMLPrinter
from androguard.core.dex import DEX
from loguru import logger


PREFIXES = ("fan.androidbase.widget.", "fan.androidbasewidget.widget.")
XML_CONSTRUCTOR = "(Landroid/content/Context;Landroid/util/AttributeSet;)V"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("apk")
    args = parser.parse_args()
    logger.disable("androguard")
    apk = APK(args.apk)
    resources = apk.get_android_resources()
    layout_ids = ElementTree.fromstring(resources.get_public_resources(apk.get_package()))
    layouts = {
        path
        for item in layout_ids.findall("public[@type='layout']")
        for _, path in resources.get_resolved_res_configs(int(item.attrib["id"], 16))
        if isinstance(path, str) and path.startswith("res/") and path.endswith(".xml")
    }
    references = {}
    for file in layouts:
        root = AXMLPrinter(apk.get_file(file)).get_xml_obj()
        if root is None:
            continue
        for node in root.iter():
            name = node.get("class") if node.tag == "view" else node.tag
            if isinstance(name, str) and name.startswith(PREFIXES):
                references.setdefault(name, set()).add(file)

    if not any(name.endswith(".ClearableEditText") for name in references):
        raise SystemExit("No ClearableEditText XML reference found")

    classes = {cls.get_name(): cls for dex in apk.get_all_dex() for cls in DEX(dex).get_classes()}
    failures = []
    for name, files in sorted(references.items()):
        cls = classes.get("L" + name.replace(".", "/") + ";")
        if cls is None:
            failures.append(f"Missing {name} used by {', '.join(sorted(files))}")
        elif not cls.get_access_flags() & 1 or not any(
            method.get_name() == "<init>"
            and method.get_descriptor().replace(" ", "") == XML_CONSTRUCTOR
            and method.get_access_flags() & 1
            for method in cls.get_methods()
        ):
            failures.append(f"No public XML constructor on {name}")
    if failures:
        raise SystemExit("\n".join(failures))
    print(f"All {len(references)} AndroidBaseWidget XML view classes and constructors are present")


if __name__ == "__main__":
    main()
