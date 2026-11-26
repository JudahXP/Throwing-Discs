# NeoForge port status

23 source targets are prepared, including legacy 1.20.1, 1.20.5 and 1.21.2. Minecraft 1.20 has no NeoForge release.

11 builds are packaged and published: 26.1, 26.1.1, 26.1.2, 26.2, 26.3 and 1.21.6 through 1.21.11. Remaining compilation is in progress. Validation is compilation and JAR inspection only; no gameplay testing is claimed.

Queue output: /workspace/work/neoforge-queue-next.log. This active queue captured the matrix before 1.20.1 was added; run queue.py again after it finishes to include that target and retry failures. Packaged targets are skipped.

Do not regenerate projects blindly: compatibility fixes exist outside generate.py. The 26.x projects compile mixins separately to Java 21 bytecode. Cache cleanup runs sequentially between builds to keep disk usage bounded.
