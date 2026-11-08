GRADLE ?= ./gradlew
PKG    := com.evilcorp.android
APK    := app/build/outputs/apk/debug/app-debug.apk

.PHONY: build test check install run uninstall clean release

build:      ## Assemble the debug APK
	$(GRADLE) assembleDebug

test:       ## Run JVM unit tests (catalog, scoring, flag invariants)
	$(GRADLE) testDebugUnitTest

check: build test  ## Everything CI runs

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
