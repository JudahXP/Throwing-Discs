# NeoForge port status

Work in progress; no NeoForge release artifacts have been published.

The initial 20 projects adapt the Forge sources to NeoForge registration, lifecycle and rendering APIs. Copper-first creative ordering, icon metadata and pack metadata are retained. Compilation is pending. Validation is limited to compilation and JAR inspection, as requested.

Additional targets to implement: 1.20.1 (legacy NeoForge 47.1.106), 1.20.5 and 1.21.2 (use the corresponding Fabric gameplay sources because Forge skipped these releases). Minecraft 1.20 has no NeoForge release.

Do not regenerate projects blindly: later compatibility fixes may exist outside generate.py. The 26.x projects compile mixins separately to Java 21 bytecode.
