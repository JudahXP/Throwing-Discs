# Judah’s Throwing Discs 1.4.1

## Fabric backports: Minecraft 1.20–1.21.10

[Download backport JARs](https://github.com/JudahXP/Throwing-Discs/raw/refs/heads/main/downloads/judahs-throwing-discs-1.4.1-fabric-1.20-to-1.21.10.zip) · [Download complete backport source](https://github.com/JudahXP/Throwing-Discs/raw/refs/heads/main/downloads/judahs-throwing-discs-1.4.1-fabric-1.20-to-1.21.10-sources.zip)

All 18 targets passed their builds and automated unit/server tests. Use the JAR for your exact Minecraft version. Copper discs are included in every supported version. Java 17+ for 1.20–1.20.4; Java 21+ for 1.20.5 onward. Fabric Loader 0.19.5+ and matching Fabric API are required. Graphical playtesting has not been performed.

## Newer versions: Minecraft 1.21.11 and 26.x

[Download newer-version JARs](https://github.com/JudahXP/Throwing-Discs/raw/refs/heads/main/downloads/judahs-throwing-discs-1.4.1-all-versions.zip) · [Download newer-version source](https://github.com/JudahXP/Throwing-Discs/raw/refs/heads/main/downloads/judahs-throwing-discs-1.4.1-all-sources.zip)

Judah's Throwing Discs 1.4.1 — JudahXP

Supports Minecraft 1.21.11, 26.1, 26.1.1, 26.1.2, 26.2 and 26.3. Install only the JAR matching your version, replacing the old mod on both client and server. Fabric Loader 0.19.5+ and matching Fabric API required. Java 21 for 1.21.11; Java 25 for 26.x.

Smooth direct return flight without a forced arc; no bounce-count limit and no total piercing limit; block phasing only while returning and within eight blocks of the destination; Harvest I; reusable dispenser throws; complete Ricochet enchantment removal. The 1.4.0 throw damage, melee damage and base ranges are unchanged. Piercing can hit each target once per outbound/return pass. Other tier values and Range enchantment bonuses are unchanged.

Tier: thrown damage / melee damage / base range
Copper: 5 / 3 / 14
Iron: 6 / 4 / 16
Gold: 5 / 3 / 18
Diamond: 7.5 / 5 / 19
Netherite: 8.5 / 6 / 21

Harvest breaks mature wheat, carrots, potatoes, beetroot, nether wart, cocoa, melons and pumpkins, plus upper sugar cane and bamboo. Young crops and the bottom cane/bamboo block remain. Vanilla drops; no automatic replanting. Dispensers recover returning discs in an empty slot for the next redstone activation. Missing/full dispensers drop the item; throws consume durability.

UPGRADING: Back up your world. Let version 1.3.1 remove retired Ricochet from carried discs before installing 1.4.1; remove old Ricochet books. This version has no Ricochet registry entry, so stacks still referencing it may fail to decode correctly.

All six clean builds passed 11 unit tests and 34 required game tests each. Visual playtesting was not performed.

Complete per-version projects, instructions and validation reports are in `versions/`.

## Forge ports

Install only the JAR matching your Minecraft version, with Forge. Do not install the Fabric edition alongside it.
Includes all five disc tiers and the existing 1.4.1 behavior: smooth direct return, return-only phasing within eight blocks, unlimited bounce count, Harvest, reusable dispenser throws, and no Ricochet enchantment.

Minecraft 1.20.5 and 1.21.2 are omitted because Forge does not publish releases for them.
Validation was deliberately limited to compilation and JAR inspection. No game-test suites or gameplay tests were run for these Forge ports.

[Download Forge mods](downloads/judahs-throwing-discs-1.4.1-forge-all-versions.zip) · [Download Forge source](downloads/judahs-throwing-discs-1.4.1-forge-all-versions-sources.zip)
