GRADLE ?= ./gradlew
PKG    := com.evilcorp.android
APK    := app/build/outputs/apk/debug/app-debug.apk

ifeq ($(origin JAVA_HOME), undefined)
  ifneq ($(wildcard /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home),)
    export JAVA_HOME := /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
  endif
endif

.PHONY: build test flags check sast verify-labs install run uninstall clean release

build:      ## Assemble the debug APK
	$(GRADLE) assembleDebug

test:       ## Run JVM unit tests (catalog, scoring, flag invariants)
	$(GRADLE) testDebugUnitTest

flags:      ## Fail if any CTF flag is stored as plaintext in the debug APK
	$(GRADLE) verifyNoPlaintextFlags

check: build test flags  ## Everything CI runs

sast:       ## Run the Semgrep benchmark rules (no metrics sent)
	semgrep --config rules/semgrep-evilcorp.yml --metrics=off app/src/main/

verify-labs:  ## Check every adb-solvable lab on the connected emulator
	./scripts/adb_exploits.sh

install: build  ## Install on the connected emulator/device
	adb install -r $(APK)

run: install    ## Install and launch
	adb shell am start -n $(PKG)/.MainActivity

uninstall:
	adb uninstall $(PKG)

release:    ## Build the unsigned release APK + SHA-256
	$(GRADLE) assembleRelease
	cd app/build/outputs/apk/release && shasum -a 256 *.apk > SHA256SUMS

clean:
	$(GRADLE) clean
