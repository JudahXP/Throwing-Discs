# Judah’s Throwing Discs 1.4.1 — Minecraft 1.20.5 (Fabric)

Install only the matching version JAR on client and server. Requires Fabric Loader 0.19.5+, matching Fabric API, and Java 21+.

Includes copper, iron, golden, diamond and netherite discs.

Preserves 1.4.1 smooth direct return, unlimited bounce count, no total piercing cap, Harvest I, reusable dispenser throws, and complete Ricochet removal. Phasing is allowed only on return and only when the whole movement step remains within eight blocks of the destination. Each target can be hit once per outbound/return pass. Uses the available trident impact sound.

Throw / melee / base range: iron 6 / 4 / 16; gold 5 / 3 / 18; diamond 7.5 / 5 / 19; netherite 8.5 / 6 / 21. Copper 5 / 3 / 14.

Harvest breaks mature crops, melons/pumpkins and upper sugar cane/bamboo, with vanilla drops and no automatic replanting. Young crops and the bottom cane/bamboo block remain. Dispensers recover the actual disc in an empty slot or drop it when missing/full. Throws use durability.

Build with JDK 25 for Gradle/Loom and a Java 21 compiler toolchain: `./gradlew build runGameTest`. Output targets Java 21. See VALIDATION.md.
