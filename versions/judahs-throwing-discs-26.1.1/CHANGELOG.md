# 1.4.1

Restored direct homing with bounded acceleration for a smooth reversal; removed the bounce-count limit. Piercing remains unlimited across distinct targets. Tier ranges and eight-block return-only phasing are unchanged.

# 1.4.0

Smooth return arcs; eight-block return-only phasing; Harvest I; reusable dispenser throws; complete Ricochet enchantment removal; reference throw/melee/base-range values.

# 1.3.1

Fixed rounding-related outbound hovering; restored original held poses; exclusive Curve/Homing; Keen Edge III cap; Fire Aspect replaces Flame without projectile fire visuals; retired Ricochet enchantment and made 17.5% bounce acceleration default; reduced melee damage by 2.5 per tier; automatic legacy disc enchantment migration.

## Combat and steering additions

Curve I–II bends the outbound disc toward your sideways movement at the instant of throwing (1.5 or 3 degrees per tick). Stand still or move straight forward to throw without a sideways arc. Curve and Homing can coexist; Curve supplies the arc and Homing gently corrects toward visible forward mobs. The flight renderer pitches with vertical travel and banks during horizontal turns.

Keen Edge I–V adds one damage point per level to thrown and melee attacks. Thrown speed scaling applies after this bonus and remains capped at +40%. Armor and Protection still apply; this enchantment does not bypass them. Unenchanted melee damage and attack speed match the corresponding material shovel.

A thrown disc reverses ghast fireballs and assigns their ownership to the thrower. It disables a facing, raised shield for three seconds; the shield can still block the triggering hit. Entity impacts and melee hits use vanilla spear-hit sounds. Block impacts and returns retain their trident sounds.

Flame I ignites the projectile with vanilla fire rendering and ignites successfully damaged mobs/players for five seconds. Water can extinguish it. Thrown hits have a small knockback strength of 0.15; Knockback I–II adds 0.2 per level, capped at 0.55. Vanilla resistance still applies.

Books accepted by anvils: Homing I–IV, Ricochet I–III, Range I–V, Curve I–II, Keen Edge I–V, Mending I, Unbreaking I–III, Flame I, and Knockback I–II. They can coexist on one disc. Incompatible books are rejected even in Creative mode. A mixed book containing an incompatible enchantment is rejected as a whole. The mod extends the vanilla Flame and Knockback enchantment definitions with disc support while retaining their vanilla effects and existing supported items; datapacks overriding those definitions can change compatibility.

## 1.3.0 update

- Base outbound range is now exactly 12 blocks. Range I–V adds 3, 7, 12, 17 or 22 blocks; existing bounce distance bonuses still apply.
- Homing I–IV gently curves toward the nearest visible, unhit mob within ten blocks and a forward cone. It never targets players, tamed pets, allies, or mobs behind walls. Higher levels increase steering without adding speed. Return flight remains directed toward the owner.
- Ricochet I–III increases bounce acceleration from 15% to 17.5%, 20% or 22.5%. All levels retain the three-block-per-tick speed cap.
- Faster impacts deal up to 40% additional damage. Base-speed impacts retain their original tier damage.
- Homing, Ricochet and Range are compatible vanilla data-driven enchantments, available on discs and enchanted books through enchanting, normal book loot/trades, and anvils. Mending and Unbreaking remain supported.
- Thrown discs shatter decorated pots on outbound and return flight using vanilla projectile interaction, drops, and protection/game-rule checks. They continue through a shattered pot; other solid blocks still ricochet.
- The flying disc tilts upward/downward with its travel direction and casts a small vanilla entity shadow (requires entity shadows enabled). Both held poses use an upright grip with the hand near the center hole, based on the supplied teaser image.

To test enchantments while holding a disc, use `/enchant @s throwing_disc:homing 4`, `/enchant @s throwing_disc:ricochet 3`, and `/enchant @s throwing_disc:range 5`. Apply enchanted books at an anvil as usual. These three enchantments are custom balancing additions, not claimed official mechanics.

# Minecraft 26.1.1 port — 1.2.0+26.1.1

Ported entity interpolation, pose rotation, and recipe-book advancements to this exact Minecraft release. Gameplay, textures, sounds, durability, and credits match the 1.2.0 release. Requires Java 25 and Fabric Loader 0.19.5 or newer.

# 1.2.0 — Judah's Throwing Discs

- Base range reduced to 18 blocks; eight ricochets and accelerating bounces retained.
- Author/credit: JudahXP.
- Description: A Vanilla friendly recreation of the recently announced Throwing Disc!
- Durability halved, rounding down: copper 95, iron 125, gold 16, diamond 780, netherite 1015. Mending and Unbreaking remain supported.
- Entity impacts use trident hit; block impacts use trident hit ground; return/catch use trident return. Throw audio is unchanged.
- Return flight collides with blocks and ricochets. A clear path is required to catch, including through thin obstacles within pickup radius.
- Timeout, dimension change and unloaded-boundary recovery drop the disc at its current location instead of teleporting it through terrain. Blocked disconnect/shutdown recovery also drops it.

Replace the older JAR; do not install multiple versions together. Mod and item IDs are unchanged.

# 1.1.0 — Judah's Throwing Discs

- Renamed the display name; credits: JudahXP. Kept mod and item IDs for save compatibility.
- Added the supplied iron/diamond mod icon and replaced every item texture with the supplied sprite, packed at 16×16.
- Increased base travel allowance 16 → 28 blocks, bounce allowance 3 → 8, extra distance per bounce 6 → 12 blocks, outbound safety limit 80 → 160 ticks, and overall safety timeout 200 → 320 ticks.
- Ricochets now gain 15% speed instead of losing speed, capped at 3 blocks/tick.
- Added native tool-material durability, one point per throw, vanilla Unbreaking behavior, Mending compatibility and material-based anvil repair.
- Confirmed crafting and smithing recipes match the supplied screenshot.
- Shortened the action-bar text to “Disc in flight”.

Replace the old JAR with this version; do not install both versions together.
