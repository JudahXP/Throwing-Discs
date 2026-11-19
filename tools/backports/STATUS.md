# Fabric backports — work in progress

Target: all 18 released versions from Minecraft 1.20 through 1.21.10. Copper is omitted before 1.21.9. Preserve release 1.4.1 flight, Harvest, dispenser reuse, enchantments, and reference tier damage/range.

The previous workspace was lost before its backports were published. This branch reconstructs those changes and reruns verification. No backport download on this branch is a finished release until its VALIDATION.md records a successful build and runtime tests.

All 18 target projects have been reconstructed. Fifteen targets have passed and are packaged under downloads/backports. Final verification is running for 1.20.5, 1.21, and 1.21.1. Individual VALIDATION.md reports identify completed targets.

Build tools: JDK 25 for Gradle/Loom, Java 21 compiler/runtime toolchain. Early 1.20 ports target Java 17 class files. Scripts in this folder support reconstruction and verification.
