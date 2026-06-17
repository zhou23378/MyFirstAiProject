import os
import re

root = r"c:\Users\E023378\Desktop\MyFirstAiProject\backend\src\main\java"

# Files missing ErrorCode import
files_to_fix = [
    os.path.join(root, "com/salon/queue/service/impl/ServiceQueueServiceImpl.java"),
    os.path.join(root, "com/salon/consumption/service/impl/ServiceCardServiceImpl.java"),
    os.path.join(root, "com/salon/schedule/service/impl/AttendanceServiceImpl.java"),
    os.path.join(root, "com/salon/technician/service/impl/TechnicianStatusServiceImpl.java"),
    os.path.join(root, "com/salon/coupon/service/impl/CouponServiceImpl.java"),
    os.path.join(root, "com/salon/inventory/service/impl/StockRecordServiceImpl.java"),
]

for fp in files_to_fix:
    with open(fp, "r", encoding="utf-8") as f:
        content = f.read()
    
    # Check if ErrorCode import already exists
    if "import com.salon.common.exception.ErrorCode" in content:
        print(f"  SKIP (already imported): {fp}")
        continue
    
    # Find existing import from com.salon.common.exception
    existing_import = re.search(r'^import com\.salon\.common\.exception\.', content, re.MULTILINE)
    if existing_import:
        # Add after the last com.salon.common.exception import
        lines = content.split('\n')
        last_common_exception_idx = -1
        for i, line in enumerate(lines):
            if line.startswith('import com.salon.common.exception.'):
                last_common_exception_idx = i
        
        if last_common_exception_idx >= 0:
            lines.insert(last_common_exception_idx + 1, 'import com.salon.common.exception.ErrorCode;')
            content = '\n'.join(lines)
        else:
            # Insert after package statement
            pkg_match = re.search(r'^(package .+;)$', content, re.MULTILINE)
            if pkg_match:
                insert_pos = pkg_match.end()
                content = content[:insert_pos] + '\n\nimport com.salon.common.exception.ErrorCode;' + content[insert_pos:]
    else:
        # No com.salon.common.exception imports at all - add after package
        pkg_match = re.search(r'^(package .+;)$', content, re.MULTILINE)
        if pkg_match:
            insert_pos = pkg_match.end()
            content = content[:insert_pos] + '\n\nimport com.salon.common.exception.ErrorCode;' + content[insert_pos:]
    
    # Ensure file ends with newline
    if not content.endswith('\n'):
        content += '\n'
    
    with open(fp, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"  + import ErrorCode: {fp}")

print("Done.")