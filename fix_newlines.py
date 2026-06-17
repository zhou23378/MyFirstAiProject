import os
import sys

root = r"c:\Users\E023378\Desktop\MyFirstAiProject\backend\src\main\java"

fixed = 0
for dirpath, dirnames, filenames in os.walk(root):
    for fn in filenames:
        if not fn.endswith(".java"):
            continue
        fullpath = os.path.join(dirpath, fn)
        with open(fullpath, "rb") as f:
            content = f.read()
        if not content.endswith(b"\n"):
            with open(fullpath, "ab") as f:
                f.write(b"\n")
            fixed += 1
            print(f"  + newline: {fullpath}")

print(f"\nFixed {fixed} files.")