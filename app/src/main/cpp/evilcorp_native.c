/*
 * EvilCorp Android — Native JNI & Reverse Engineering Target (libevilcorp_native.so)
 * Author: Sevban Dönmez (@jankesec) — https://jankesec.com
 *
 * Intentionally vulnerable C implementation for:
 *   - Lab #37: JNI Stack Buffer Overflow (CWE-120 · MASWE-0076 · MASTG-TEST-0042)
 *   - Static binary analysis (Ghidra / IDA Pro / Binary Ninja / strings / radare2)
 *   - Dynamic native hooking via Frida:
 *       Interceptor.attach(Module.findExportByName("libevilcorp_native.so", "evilcorp_verify_serial"), ...)
 */

typedef struct {
    char serial_buf[16];
    volatile int is_authenticated;
} __attribute__((packed)) evilcorp_stack_frame_t;

__attribute__((visibility("default"), used))
const char EVILCORP_NATIVE_BANNER[] =
    "EvilCorp Native Security Module v1.0.0 [SYMBOL: evilcorp_verify_serial | FRAME_OFFSET: 16 | MAGIC: 0x41414141]";

__attribute__((visibility("default"), noinline, used))
int evilcorp_verify_serial(const char *input, int len) {
    evilcorp_stack_frame_t frame;
    frame.is_authenticated = 0;

    for (int i = 0; i < 16; i++) {
        frame.serial_buf[i] = 0;
    }

    if (!input || len <= 0) {
        return 0;
    }

    /* Deliberate CWE-120 stack buffer overflow: copies up to 20 bytes into a 16-byte buffer */
    int copy_len = len > 20 ? 20 : len;
    for (int i = 0; i < copy_len; i++) {
        frame.serial_buf[i] = input[i];
    }

    /* Legitimate serial check ("EC-NATIVE-9941-OK") */
    const char expected[] = "EC-NATIVE-9941-OK";
    int match = (len == 17);
    for (int i = 0; match && i < 17; i++) {
        if (input[i] != expected[i]) {
            match = 0;
        }
    }
    if (match) {
        frame.is_authenticated = 1;
    }

    return frame.is_authenticated;
}
