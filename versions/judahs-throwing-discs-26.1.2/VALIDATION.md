# 1.4.0 validation — Minecraft 26.1.2

`./gradlew clean build runGameTest` passed: 11 unit tests and 33 required game tests (32 mod tests plus the Fabric framework check).

New checks cover smooth turning without an abrupt reversal, convergence from 72 nearby distance/heading combinations, outbound collisions within eight blocks, return collisions outside eight blocks, nearby return phasing through a pane, Harvest mature wheat drops and young-crop preservation, registered dispenser behavior with two consecutive throws and durability use, absence of the Ricochet registry entry, and the new melee attributes. Existing coverage includes enchantments/anvils, save/load, item custody, recipes, durability, mending, combat and textures.

Release JAR metadata, Java class target, absence of test classes and Ricochet definition, Harvest level, and original texture bytes were checked. Graphical playtesting has not been performed; visual smoothness needs in-game review. Multiplayer latency and all possible lifecycle transitions have not been exhaustively tested.
