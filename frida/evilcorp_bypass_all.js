/**
 * EvilCorp Android — Frida instrumentation & bypass suite (instructor reference)
 * Author: Sevban Dönmez (@jankesec) — https://jankesec.com
 *
 * Neutralises the client-side checks of the Resilience/Auth labs in com.evilcorp.android only.
 *
 * Usage (Frida 17+; the frida CLI bundles the Java bridge):
 *   frida -U -f com.evilcorp.android -l frida/evilcorp_bypass_all.js
 * When loading through frida-compile or the Python/Node API instead, add
 *   import Java from "frida-java-bridge";
 */

Java.perform(function () {
    console.log("[+] EvilCorp Android bypass suite loaded");

    function hookAll(className, methodName, result) {
        try {
            var clazz = Java.use(className);
            clazz[methodName].overloads.forEach(function (overload) {
                overload.implementation = function () {
                    var args = Array.prototype.slice.call(arguments).map(String).join(", ");
                    console.log("[bypass] " + className.split(".").pop() + "." + methodName + "(" + args + ") -> " + result);
                    return result;
                };
            });
            console.log("  [ok] hooked " + className + "." + methodName);
        } catch (e) {
            console.log("  [!] skipped " + className + "." + methodName + ": " + e.message);
        }
    }

    // #10 Root Detection Bypass (MASWE-0051)
    hookAll("com.evilcorp.android.labs.RootChecker", "isRooted", false);

    // #19 SSL Pinning Bypass (MASWE-0028)
    hookAll("com.evilcorp.android.labs.SslPinValidator", "verifyCertificatePin", true);

    // #21 Biometric Auth Bypass (MASWE-0020)
    hookAll("com.evilcorp.android.labs.BiometricGate", "isBiometricVerified", true);

    // #22 Debugger Detection (MASWE-0064)
    hookAll("com.evilcorp.android.labs.AntiDebugCheck", "isBeingDebugged", false);

    // #24 Bytecode Tampering & Licensing (MASWE-0058)
    hookAll("com.evilcorp.android.labs.IntegrityGate", "verifyLicense", true);

    // #38 Emulator Detection Bypass (MASWE-0053)
    hookAll("com.evilcorp.android.labs.EmulatorDetector", "isEmulator", false);

    // #39 APK Signature & Installer Check (MASWE-0056)
    hookAll("com.evilcorp.android.labs.ApkSignatureVerifier", "isOfficialBuild", true);
});
