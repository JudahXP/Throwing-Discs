# 1.3.1 validation — Minecraft 26.1.1

Clean release build passed with 9 JUnit tests and 28 required server game tests (27 mod tests and the Fabric framework check).

The regression tests cover return at a rounded range boundary, return from zero outbound velocity, Curve/Homing exclusivity in Survival and Creative anvils, legacy enchantment migration, Keen Edge III limits, Fire Aspect without projectile fire visuals, and melee damage reduced by 2.5. Existing tests cover custody, returns blocked by walls, durability, repairs, recipes, combat, steering and texture fidelity.

Release JAR checked for version/author metadata, Java 25 class target, absence of test classes, enchantment resources, original generated-item held transforms, and byte-identical item textures.

Graphical playtesting was not performed. Held poses and flight visuals still need in-game review. High-latency multiplayer and every lifecycle transition have not been exhaustively tested.

Reproduce with Java 25 running Gradle: `./gradlew clean build runGameTest`. For 1.21.11 also install Java 21 and, if Gradle does not discover it, set `JDK_21=/path/to/jdk-21` in the build environment.
