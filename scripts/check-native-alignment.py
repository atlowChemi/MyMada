#!/usr/bin/env python3
"""Check packaged 64-bit ELF LOAD alignment; does not replace a 16 KB device test.

Usage: python3 scripts/check-native-alignment.py path/to/app.apk [path/to/app.aab]
Also run SDK zipalign -c -P 16 4 on APKs to check ZIP entry alignment.
"""

import struct
import sys
import zipfile


def check(path):
    count = 0
    with zipfile.ZipFile(path) as archive:
        for name in archive.namelist():
            if not name.endswith(".so"):
                continue
            data = archive.read(name)
            assert data[:4] == b"\x7fELF", f"Not ELF: {name}"
            if data[4] != 2:  # Play's 16 KB requirement concerns 64-bit binaries.
                continue
            endian = "<" if data[5] == 1 else ">"
            offset = struct.unpack_from(endian + "Q", data, 32)[0]
            size, number = struct.unpack_from(endian + "HH", data, 54)
            loads = []
            for index in range(number):
                header = struct.unpack_from(
                    endian + "IIQQQQQQ", data, offset + index * size
                )
                if header[0] == 1:  # PT_LOAD
                    loads.append(header)
                    assert header[7] >= 16384, f"Unaligned LOAD: {name} ({header[7]})"
                    assert (header[3] - header[2]) % 16384 == 0, (
                        f"Unaligned offset: {name}"
                    )
            assert loads, f"No LOAD segments: {name}"
            count += 1
            print(f"PASS {path}: {name} ({len(loads)} LOAD segments)")
    print(f"Checked {count} 64-bit shared libraries in {path}")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    for path in sys.argv[1:]:
        check(path)
