# Judah's Throwing Discs 1.3.1 — Minecraft 26.2

By **JudahXP**.

A Vanilla friendly recreation of the recently announced Throwing Disc!

## Installation

Use Minecraft **26.2**, Java **25**, Fabric Loader **0.19.5+**, and Fabric API **0.161.0+26.2**. Replace the previous throwing_disc JAR with `judahs-throwing-discs-1.3.1-mc26.2.jar` on client and server. Install only one version of this mod.

## Changes in 1.3.1

- Fixed a floating-point range-boundary stall: a disc within 0.000001 blocks of its outbound limit starts returning immediately. A stopped outbound disc also starts returning instead of waiting for the timeout.
- Restored the original vanilla generated-item held poses. Flight tilt, bank and small entity shadow remain.
- Curve and Homing are mutually exclusive in enchanting and anvils, including Creative anvils.
- Keen Edge is capped at III, adding one damage per level to melee and thrown attacks.
- Fire Aspect I–II replaces Flame support. Hits ignite targets for four/eight seconds. The thrown disc has no fire overlay or burning flight effect.
- Removed the obtainable Ricochet enchantment. Every disc now gains 17.5% speed per bounce (the old Ricochet I setting), capped at three blocks per tick.
- Reduced every tier's melee damage by 2.5 points. Unenchanted fully charged totals: copper 1, iron 2, gold 0, diamond 3, netherite 4. Attack speed remains shovel speed. Thrown base damage is unchanged.

## Upgrading existing discs

Carried and thrown discs automatically remove old Ricochet, cap old Keen Edge IV/V to III, and convert Flame to Fire Aspect I (preserving a higher existing Fire Aspect level). A legacy disc containing both Curve and Homing keeps Homing and loses Curve. Other components, durability and names are preserved. A hidden, inert Ricochet registry definition remains only so older saved enchanted stacks can still decode safely; it is absent from enchanting, loot/trades and creative book selections and cannot be applied to a disc. Existing retired Ricochet books have no effect.

## Controls and enchantments

Right-click throws from the main hand, or from the off-hand when the main hand does not consume use. Sneak-use selects the off-hand when both hands contain discs. Real item custody transfers to the projectile, preventing duplicate throws. The disc returns to its original slot; occupied slots fall back to inventory, then a dropped item if full.

- Homing I–IV gently steers toward the nearest visible, unhit mob ahead within ten blocks. It ignores players, tamed pets and allies.
- Curve I–II curves left/right according to sideways movement when thrown, at 1.5/3 degrees per tick. Standing still or moving straight ahead gives no curve. Choose Curve **or** Homing.
- Range I–V adds 3, 7, 12, 17 or 22 blocks to the 12-block base outbound range.
- Keen Edge I–III adds 1/2/3 damage before the bounded thrown-speed bonus.
- Mending, Unbreaking I–III, Fire Aspect I–II, Knockback I–II and the compatible vanilla Curse of Vanishing remain supported. Incompatible books, including Flame, armor and unrelated tool enchantments, are rejected even in Creative. Mixed books with an unsupported enchantment are rejected as a whole.

Anvils apply compatible books normally. Example commands while holding a disc: `/enchant @s throwing_disc:homing 4`, `/enchant @s throwing_disc:range 5`, `/enchant @s throwing_disc:keen_edge 3`, `/enchant @s minecraft:fire_aspect 2`.

## Combat and flight

Launch speed is 1.25 blocks/tick. Thrown base damage: copper 4, iron 5, gold 4, diamond 7, netherite 8. Speed adds at most 40% damage after Keen Edge; armor and Protection still apply. Each bounce adds twelve blocks of range; eight bounces initiate return. Each entity can be hit once outbound and once returning. Knockback strength is 0.15 plus 0.2 per Knockback level, up to 0.55, subject to vanilla resistance.

Thrown discs shatter decorated pots using vanilla interaction permissions and drops, continuing past broken pots. They reverse ghast fireballs and credit their thrower. A facing raised shield can block the first hit and is then disabled for three seconds; player-combat restrictions are respected. Entity and melee impacts use spear-hit sounds, while throws, block impacts and returns retain trident sounds.

Return movement collides with blocks and cannot catch through walls. A blocked disc can bounce while seeking its owner. If it cannot return within 320 ticks, it drops as a recoverable item at its position. Missing/dead owners, dimension changes and unloaded boundaries also use recovery/drop paths instead of phasing through blocks. The server owns movement and damage; the client interpolates and renders the flight state.

## Crafting and durability

Copper, iron, gold and diamond each use four ingots/gems in a cross around an empty center. Netherite uses a diamond disc, Netherite Upgrade template and netherite ingot in a smithing table.

Durabilities remain copper 95, iron 125, gold 16, diamond 780 and netherite 1015. Throws consume one durability through vanilla Unbreaking handling; Creative throws are exempt. Mending works when held, and anvils repair with the matching material.

## Build and validation

Run `./gradlew clean build runGameTest` using Java 25 for Gradle. The 1.21.11 project also requires an installed Java 21 toolchain; set `JDK_21=/path/to/jdk-21` in the build environment if needed. Minecraft 26.x uses Java 25 and unobfuscated Loom; 1.21.11 uses Java 21 output and official Mojang mappings with remapping. See `build.gradle`, `gradle.properties` and `VALIDATION.md` for the exact setup and checks. Visuals need in-game review; this update's tests are headless.

The five exact 16×16 item textures and supplied iron/diamond icon are unchanged. `art/supplied` and `tools/PackTextures.java` preserve the texture sources and packing method. The mod's tuning and added enchantments are custom choices, not claimed official numerical specifications.

Not an official Minecraft product. Not approved by or associated with Mojang or Microsoft.
