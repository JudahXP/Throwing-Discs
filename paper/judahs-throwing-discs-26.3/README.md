# Paper 26.3 port — in development

This is not a finished or installable plugin. No Paper release has been published.

Target: Paper 26.3 build 263, Java 25. Original item assets are included for the companion resource pack. The tier values and pure flight math are ported; gameplay integration is pending.

Parity checklist: item identity and crafting; item-display projectile rendering; direct homing return; return-only phasing with both movement endpoints within eight blocks; bounce and pierce behavior; enchantment registration and restrictions; mature-crop Harvest and drops; dispenser item custody and recovery; durability and repair; melee attributes; owner disconnect/server shutdown recovery; resource-pack distribution; permission and protection-plugin event compatibility.

Paper cannot add native client item registry entries. Vanilla clients need the resource pack for disc visuals, and the creative inventory cannot be extended like a client mod. These differences must be documented in the final release rather than described as perfect parity.
