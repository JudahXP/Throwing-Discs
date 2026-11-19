# Fabric backports — completed

All 18 Minecraft targets from 1.20 through 1.21.10 passed build, unit and dedicated-server game tests. Copper is omitted before 1.21.9. Release 1.4.1 flight, Harvest, dispenser reuse, enchantments and tier damage/range are preserved.

Validation totals: 198 unit tests and 432 required server game tests across all targets. Individual reports are in each version project and downloads/backports/validation-VERSION.json. No graphical playtesting or multiplayer latency testing was performed.

Combined mod/source ZIPs and checksums are in downloads/; README.md links the downloads. Existing 1.21.11 and 26.x projects and release bundles remain unchanged.

Build tools: JDK 25 for Gradle/Loom, Java 21 compiler/runtime toolchain. Early 1.20 ports emit Java 17 class files. Build per-version projects with `./gradlew build runGameTest`. The prepare/adapt scripts are historical one-shot reconstruction helpers; do not rerun them over maintained source files.
