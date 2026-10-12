package me.adda.voxelparticles.atlas;

import org.jetbrains.annotations.Nullable;

public interface AtlasView {
	int width();

	int height();

	/** How many times larger this atlas' typical sprite is than the vanilla one (1 = vanilla). */
	float resolutionScale();

	/** Sprite covering the given atlas texel, or null. */
	@Nullable
	SpriteView spriteAt(int texelX, int texelY);

	interface SpriteView {
		String id();

		/** Atlas-space bounds of the sprite (first animation frame). */
		int x();

		int y();

		int width();

		int height();

		/** Alpha 0..255 of a texel in sprite-local coordinates. */
		int alpha(int localX, int localY);
	}
}
