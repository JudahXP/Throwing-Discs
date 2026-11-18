# Judah's Throwing Discs 1.4.0 — Minecraft 26.2

By **JudahXP**. A Vanilla friendly recreation of the recently announced Throwing Disc!

Install only `judahs-throwing-discs-1.4.0-mc26.2.jar`, replacing the previous mod on client and server. Requires Fabric Loader 0.19.5+ and Fabric API 0.161.0+26.2. Use Java 25 to play.

## Changes

- Return flight turns in a smooth arc, retaining forward movement rather than stopping and reversing. It slows gradually when approaching its destination.
- Block phasing is restricted to returning discs. Both ends of a movement step must be within eight blocks of the return destination. Outbound discs always collide, even near their owner. Returning discs outside that radius still collide.
- Harvest I breaks mature wheat, carrots, potatoes, beetroot, nether wart and cocoa, plus melons and pumpkins. It cuts upper sugar cane and bamboo while preserving the bottom block. Drops use vanilla block drops. Young crops remain planted; harvested mature crops are not automatically replanted. Harvest works on outbound and returning flight, including dispenser throws.
- Dispensers throw discs in their facing direction. A returning disc goes into its original empty slot, or another empty slot. If the dispenser is full or missing, it drops as an item. Each successful dispenser throw costs one durability, with vanilla Unbreaking handling. No manual reload is required after a successful return; each throw still needs a redstone activation.
- Ricochet is completely removed from the enchantment registry, commands and books. The existing 17.5% bounce acceleration remains standard for all discs.

## Tier values

| Tier | Thrown base damage | Total melee damage | Base outbound range |
|---|---:|---:|---:|
| Copper | 5 | 3 | 14 |
| Iron | 6 | 4 | 16 |
| Gold | 5 | 3 | 18 |
| Diamond | 7.5 | 5 | 19 |
| Netherite | 8.5 | 6 | 21 |

Only these three tier values were changed from the supplied reference. Durability remains 95/125/16/780/1015; melee attack speed remains -3. Range I–V still adds 3/7/12/17/22 blocks. Bounce range bonus remains 12 blocks, with eight bounces initiating return. Thrown speed bonus remains capped at 40%, and bounce speed at 3 blocks/tick. A flight may cross its range threshold during a tick before beginning its smooth return arc.

## Controls and enchantments

Right-click throws; sneak-use selects the off-hand when both hands hold discs. The real item transfers to the projectile and returns to its original slot, an empty inventory slot, or drops when full. Each entity may be hit once outbound and once returning. Return timeout is 320 ticks.

Homing I–IV and Curve I–II are mutually exclusive. Keen Edge I–III, Range I–V, Harvest I, Fire Aspect I–II, Knockback I–II, Unbreaking, Mending and Curse of Vanishing remain supported. Fire Aspect ignites hit targets without a fire overlay on the projectile. Example: `/enchant @s throwing_disc:harvest 1`.

## Existing worlds

Back up your world before updating. Version 1.3.1 removes retired Ricochet from carried discs as they tick; do that before installing 1.4.0 and remove retired Ricochet books. Version 1.4.0 intentionally has no legacy Ricochet registry entry, so stacks still referencing that removed enchantment may fail to load correctly. Older Keen Edge IV/V and Flame on otherwise valid discs are still normalized.

## Build

Run `./gradlew clean build runGameTest` with Java 25 running Gradle. The 1.21.11 project also needs Java 21; set `JDK_21=/path/to/jdk-21`. See VALIDATION.md for the actual test results.

Original textures and artwork are preserved. Graphical playtesting has not been performed. Not an official Minecraft product; not approved by or associated with Mojang or Microsoft.
