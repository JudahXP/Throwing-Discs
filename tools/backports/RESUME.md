# Work checkpoint

Active task: finish all 18 Fabric backports, preserve 1.4.1 behavior, copper only on 1.21.9/10. Publish final mod/source ZIPs and merge work to main after all target checks pass. User authorized pushes. Do not change the six existing modern projects.

Repository: /workspace/Throwing-Discs, branch fabric-backports (pushed). Original main is 24f015d. All 18 backport sources are now committed. If the workspace is replaced, clone this branch; do not reconstruct from main again.

Build tools under /workspace/work/toolchains; setup_toolchains.py downloads JDK21/25. build.py takes version arguments, uses /workspace/work/gradle-cache, writes /workspace/work/backport-VERSION.log. Default tasks build runGameTest; BUILD_TASKS may override. It prepares target-specific vanilla enchantment JSON before Gradle. Do not run the same target concurrently. ActiveProcessorCount=2 avoids excess GC workers; machine has two CPUs. Max 3–4 build runners, fewer preferred.

Current runners at checkpoint:
- newer: 1.21.10 and 1.21.9 passed and packaged/pushed. Running 1.21.8, then1.21.7,1.21.6,1.21.5. Status work/newer-build-status.log. Session99062.
- middle: 1.21.4 first full run compiled main/client/test but failed shield setup (tick vs doTick) and saving empty carried ItemStack after immediate catch. Both fixed for1.21–1.21.4. Needs full rerun. Currently1.21.3 then1.21.2; statuswork/middle-build-status.log,session89089.
- legacy:1.20.1 running, main compiled. Session48101;work/legacy-build-status.log. Upon success, run1.20,1.20.2,1.20.3,1.20.4.
- transition:1.20.5,then1.20.6,1.21,1.21.1 running; session58166,work/transition-build-status.log.

package.py VERSION validates successful logs, unit XML, JAR metadata/class target/textures/copper/test exclusion, updates README+VALIDATION and packages artifacts under downloads/backports. It supports 11 units and different game test counts (legacy9, newer34, intermediate33). Run only after successful full build. Commit/push passed target artifacts promptly. cleanup_cache.py removes only raw/mapped/.gradle caches for packaged versions, keeping sources/build reports/artifacts. 1.21.9/10 caches already cleaned.

bundle.py must run only when all18 validation JSONs exist. Produces final mod ZIP and complete-source ZIP in downloads/ and adds README links for main. Then commit, update main without overwriting remote changes, push, and verify raw GitHub download SHA256. Existing six release downloads stay unchanged. No graphical playtesting claims.

Important compatibility findings:
- Older game test servers disable PvP. Enable setPvpAllowed(true) in fake-player helper for1.21–1.21.8 to test shields. Production player damage correctly obeys PvP.
- Pre1.21.5 shields need six ServerPlayer.doTick() calls (not tick()) to raise. Test fixes applied.
- CompoundTag ItemStack.save(registryAccess()) rejects empty stacks; omit Disc key when empty. Applied1.21–1.21.4 and1.20.5/6.
- 1.20.2–1.20.4 test connections require EmbeddedChannel attrs SERVERBOUND_PROTOCOL and CLIENTBOUND_PROTOCOL set to ConnectionProtocol.PLAY.codec(flow).
- 1.20.5/6 static enchantments use EnchantmentDefinition and ItemEnchantments, unlike1.20.1 map API. FLAME replaces FLAMING_ARROWS there.
- 1.21.2/3 rendering uses render states but no ItemModelResolver; custom renderer uses ItemRenderer.renderStatic. 1.21.4+ resolver;1.21.9+ submit API.

prepare/adapt scripts are one-shot reconstruction helpers; do not blindly rerun them on existing edited sources. Actual per-version source files are authoritative. The old /workspace/work/github-release and old checkpoint/files no longer exist after the workspace reset.
