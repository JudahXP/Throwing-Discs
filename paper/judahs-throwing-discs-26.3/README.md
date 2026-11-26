# Paper 26.3 port — development build

Targets Paper 26.3 build 263 and Java 25. This is an unfinished plugin, not a parity release. Compilation passes; no live-server or gameplay test has been performed.

Implemented: five identified disc tiers, original resource-pack assets and item models, copper/iron/gold/diamond recipes, admin `/discs <tier>` command, right-click throws using an ItemDisplay, direct smooth return, return-only phasing with both step endpoints within eight blocks, block reflections, one target hit per outbound/return pass, basic durability and recovery on logout/shutdown. Discs are removed from the inventory while in flight.

Remaining: enchantment registration and mechanics; Harvest; dispenser custody/reuse; netherite smithing; repair and vanilla durability-enchantment handling; melee attributes; exact combat/shield behavior and curve/homing; save/restart persistence; protection-plugin integration; visual orientation and latency checks; resource-pack hosting/configuration and distribution.

Vanilla clients need the resource pack for disc visuals. Paper cannot extend the vanilla creative inventory with native modded registry entries. The admin command is currently the development inventory entry point. Do not deploy this development build on a production world.
