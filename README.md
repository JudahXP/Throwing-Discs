Judah's Throwing Discs 1.3.1 — JudahXP

Install only the JAR matching your Minecraft version, on both client and server. Replace the previous throwing_disc mod. Fabric Loader 0.19.5+ and matching Fabric API are required. Minecraft 26.x requires Java 25; 1.21.11 requires Java 21 to play.

Changes:
- Fix hovering at the outbound range boundary and return when outbound velocity stops.
- Restore the original held pose.
- Make Curve and Homing mutually exclusive.
- Cap Keen Edge at III.
- Replace Flame with Fire Aspect I–II without a burning projectile effect.
- Retire Ricochet enchantment; all discs gain 17.5% speed per bounce, capped at 3 blocks/tick.
- Reduce melee damage by 2.5 per tier: copper 1, iron 2, gold 0, diamond 3, netherite 4. Thrown base damage is unchanged.
- Automatically migrate existing discs: remove Ricochet, cap Keen Edge, convert Flame, keep Homing when both steering enchantments exist. A hidden inert Ricochet definition preserves decoding of older saves.

Textures, crafting, durability, and the 12-block base range from 1.3.0 are preserved.

Validation:
Minecraft 1.21.11: clean build, 9 unit tests, 28 required game tests passed.
Minecraft 26.1: clean build, 9 unit tests, 28 required game tests passed.
Minecraft 26.1.1: clean build, 9 unit tests, 28 required game tests passed.
Minecraft 26.1.2: clean build, 9 unit tests, 28 required game tests passed.
Minecraft 26.2: clean build, 9 unit tests, 28 required game tests passed.
Minecraft 26.3: clean build, 9 unit tests, 28 required game tests passed.

Graphical playtesting was not performed.
