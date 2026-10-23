import struct
import sys

PT_LOAD = 1
PT_GNU_RELRO = 0x6474E552
PAGE = 16384

failed = False
for path in sys.argv[1:]:
    data = open(path, "rb").read()
    is64 = data[4] == 2
    if is64:
        phoff, = struct.unpack_from("<Q", data, 0x20)
        phentsize, phnum = struct.unpack_from("<HH", data, 0x36)
    else:
        phoff, = struct.unpack_from("<I", data, 0x1C)
        phentsize, phnum = struct.unpack_from("<HH", data, 0x2A)
    for i in range(phnum):
        offset = phoff + i * phentsize
        kind, = struct.unpack_from("<I", data, offset)
        if is64:
            vaddr, = struct.unpack_from("<Q", data, offset + 16)
            memsz, align = struct.unpack_from("<QQ", data, offset + 40)
        else:
            vaddr, = struct.unpack_from("<I", data, offset + 8)
            memsz, = struct.unpack_from("<I", data, offset + 20)
            align, = struct.unpack_from("<I", data, offset + 28)
        if kind == PT_LOAD and align < PAGE:
            print(f"{path}: LOAD segment aligned to {align:#x}")
            failed = True
        if kind == PT_GNU_RELRO and (vaddr + memsz) % PAGE:
            print(f"{path}: RELRO ends at {vaddr + memsz:#x}")
            failed = True

sys.exit(1 if failed else 0)
