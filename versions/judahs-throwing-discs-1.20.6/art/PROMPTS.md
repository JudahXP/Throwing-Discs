# Version 1.1.0 artwork

The current mod uses the five replacement WEBP sprites supplied by JudahXP and the supplied iron/diamond composite icon. No AI generation was used for the new sprites or icon. WEBP sprites were losslessly decoded to PNG, then sampled at native pixel centers into 16×16 textures. The mod icon is copied without modification.

The historical generator outputs below are no longer used by the mod.

# Texture provenance and exact grid

Initial reconstruction mode: built-in imagegen, one generated transparent image per tier. References: the user's screenshot of all five dropped discs and screenshot with the frontal copper/diamond discs. No official source textures were supplied.

Prompt template used for each material:

> Create one Minecraft {tier} throwing disc item texture matching the provided screenshot references as closely as possible. References only; reconstruct an unlit front-on flat sprite, no perspective. Single thick octagonal circular ring with small irregular octagonal center hole. Authentic 16x16 Minecraft pixel art grid, nearest-neighbor enlarged to square canvas, one pixel equals 1/16 canvas width. Ring occupies 14x14 logical pixels centered with 1-pixel transparent border. Outer edge stepped octagon, hole roughly 4x5 pixels. {palette}. Thick face with banded pixel highlights top left and lower rim, beveled inner hole, no spokes, no handles, no ornament. Entire background and central hole truly transparent. No text, labels, shadows, scenery, smoothing or antialiasing. Asset type: game item sprite. Use case: stylized-concept.

Palette substitutions:

- Copper: rust brown outline, burnt copper orange face, muted pink pale highlights
- Iron: dark charcoal gray outline, silver gray face, pale cool silver highlights
- Gold: dark brown outline, saturated golden ochre face, pale yellow highlights
- Diamond: deep teal blue outline, bright cyan turquoise face, pale cyan highlights
- Netherite: near-black purple outline, dark desaturated purple gray face, muted lavender gray highlights

## Final revision requested by the user

All five shipped images use the copper disc's layout. The copper reconstruction was sampled to a native 16×16 grid and its colors consolidated into seven flat material roles. That grid is now explicit in `tools/PackTextures.java`; all other tiers use exactly the same pixel indices with different palettes. The generator's large images are retained for provenance only and are not loaded by Minecraft.

The final PNGs are exactly 16×16 pixels. Alpha is either 0 or 255. Each tier has the same transparency mask and highlight positions. No antialiasing, gradients, fractional pixels, or image resizing occurs in the final generation step. The preview scales those pixels by an exact integer factor.
