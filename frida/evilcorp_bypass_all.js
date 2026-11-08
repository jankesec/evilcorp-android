/**
 * EvilCorp Android — Universal Frida Instrumentation & Bypass Suite
 * Author: Sevban Dönmez (@jankesec) — https://jankesec.com
 *
 * Usage:
 *   frida -U -f com.evilcorp.android -l frida/evilcorp_bypass_all.js
 */

Java.perform(function () {
    console.log("[+] EvilCorp Android Universal Frida Suite loaded");

    function hookMethod(className, methodName, callback) {
        try {
            var clazz = Java.use(className);
            clazz[methodName].overloads.forEach(function (overload) {
                overload.implementation = function () {
                    return callback.apply(this, arguments);
                };
            });
            console.log("  [✓] Hooked " + className + "." + methodName);
        } catch (e) {
            console.log("  [!] Skipped " + className + "." + methodName + ": " + e.message);
        }
    }

    // #10 Root Detection Bypass (MASWE-0097)
    hookMethod("com.evilcorp.android.labs.RootChecker", "isRooted", function (_ctx) {
        console.log("[Bypass] RootChecker.isRooted() -> false");
        return false;
    });

    // #19 SSL Certificate Pinning Bypass (MASWE-0047)
    hookMethod("com.evilcorp.android.labs.SslPinValidator", "verifyCertificatePin", function (pin) {
        console.log("[Bypass] SslPinValidator.verifyCertificatePin('" + pin + "') -> true");
        return true;
    });

    // #21 Biometric Auth Bypass (MASWE-0033)
    hookMethod("com.evilcorp.android.labs.BiometricGate", "isBiometricVerified", function () {
        console.log("[Bypass] BiometricGate.isBiometricVerified() -> true");
        return true;
    });

    // #22 Debugger & Frida Detection Bypass (MASWE-0104)
    hookMethod("com.evilcorp.android.labs.AntiDebugCheck", "isBeingDebugged", function (_ctx) {
        console.log("[Bypass] AntiDebugCheck.isBeingDebugged() -> false");
        return false;
    });

    // #24 Bytecode Tampering & Licensing Bypass (MASWE-0100)
    hookMethod("com.evilcorp.android.labs.IntegrityGate", "verifyLicense", function (key) {
        console.log("[Bypass] IntegrityGate.verifyLicense('" + key + "') -> true");
        return true;
    });

    // #38 Emulator Detection Bypass (MASWE-0099)
    hookMethod("com.evilcorp.android.labs.EmulatorDetector", "isEmulator", function (hw) {
        console.log("[Bypass] EmulatorDetector.isEmulator('" + hw + "') -> false");
        return false;
    });

    // #39 APK Signature & Installer Verification Bypass (MASWE-0102)
    hookMethod("com.evilcorp.android.labs.ApkSignatureVerifier", "isOfficialBuild", function (cert, pkg) {
        console.log("[Bypass] ApkSignatureVerifier.isOfficialBuild('" + cert + "', '" + pkg + "') -> true");
        return true;
    });
});
