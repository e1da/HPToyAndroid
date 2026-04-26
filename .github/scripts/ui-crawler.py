#!/usr/bin/env python3
import os
import re
import subprocess
import time
import xml.etree.ElementTree as ET


PACKAGE_NAME = os.environ.get("PACKAGE_NAME", "com.hptoy")
MAX_STEPS = int(os.environ.get("UI_CRAWL_STEPS", "24"))
WAIT_AFTER_TAP_SECONDS = float(os.environ.get("UI_CRAWL_WAIT_AFTER_TAP_SECONDS", "1.0"))
BOUNDS_RE = re.compile(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]")


def run_adb(*args, check=True):
    return subprocess.run(
        ["adb", *args],
        check=check,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    ).stdout


def check_no_crash():
    logcat = run_adb("logcat", "-d", "-v", "brief", check=False)
    crash_lines = [
        line
        for line in logcat.splitlines()
        if ("FATAL EXCEPTION" in line or "AndroidRuntime" in line or f"Process: {PACKAGE_NAME}" in line)
    ]
    if any(PACKAGE_NAME in line for line in crash_lines):
        print("\n".join(crash_lines))
        raise SystemExit(1)


def package_is_running():
    return subprocess.run(
        ["adb", "shell", "pidof", PACKAGE_NAME],
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    ).returncode == 0


def start_app():
    run_adb("shell", "monkey", "-p", PACKAGE_NAME, "-c", "android.intent.category.LAUNCHER", "1")
    time.sleep(2)


def dump_ui_xml():
    run_adb("shell", "uiautomator", "dump", "/sdcard/window.xml")
    xml = run_adb("exec-out", "cat", "/sdcard/window.xml", check=False)
    return xml.strip()


def node_bounds(node):
    match = BOUNDS_RE.match(node.attrib.get("bounds", ""))
    if not match:
        return None

    left, top, right, bottom = [int(value) for value in match.groups()]
    if right <= left or bottom <= top:
        return None

    return left, top, right, bottom


def node_label(node):
    values = [
        node.attrib.get("resource-id", ""),
        node.attrib.get("text", ""),
        node.attrib.get("content-desc", ""),
        node.attrib.get("class", ""),
    ]
    return "|".join(values)


def clickable_nodes(xml):
    root = ET.fromstring(xml)
    nodes = []

    for node in root.iter("node"):
        if node.attrib.get("clickable") != "true":
            continue
        if node.attrib.get("enabled") != "true":
            continue

        bounds = node_bounds(node)
        if bounds is None:
            continue

        label = node_label(node)
        if not label.strip("|"):
            continue

        left, top, right, bottom = bounds
        nodes.append(
            {
                "label": label,
                "bounds": bounds,
                "center": ((left + right) // 2, (top + bottom) // 2),
                "sort_key": (top, left, bottom - top, right - left),
            }
        )

    return sorted(nodes, key=lambda item: item["sort_key"])


def crawl():
    visited = set()
    back_count = 0

    for step in range(1, MAX_STEPS + 1):
        if not package_is_running():
            print(f"{PACKAGE_NAME} is not running, relaunching")
            start_app()

        check_no_crash()

        try:
            xml = dump_ui_xml()
            nodes = clickable_nodes(xml)
        except Exception as error:
            print(f"Could not dump UI on step {step}: {error}")
            run_adb("shell", "input", "keyevent", "4", check=False)
            time.sleep(WAIT_AFTER_TAP_SECONDS)
            continue

        selected = None
        for node in nodes:
            key = f"{node['label']}|{node['bounds']}"
            if key not in visited:
                selected = node
                visited.add(key)
                break

        if selected is None:
            back_count += 1
            print(f"UI crawl step {step}: no new clickable node, pressing back")
            run_adb("shell", "input", "keyevent", "4", check=False)
            time.sleep(WAIT_AFTER_TAP_SECONDS)
            if back_count >= 4:
                start_app()
                back_count = 0
            continue

        back_count = 0
        x, y = selected["center"]
        print(f"UI crawl step {step}: tap {selected['label']} at {x},{y}")
        run_adb("shell", "input", "tap", str(x), str(y))
        time.sleep(WAIT_AFTER_TAP_SECONDS)
        check_no_crash()

    print(f"UI crawl finished: {len(visited)} unique clickable nodes touched")


if __name__ == "__main__":
    crawl()
