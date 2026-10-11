package me.adda.voxelparticles.atlas;

import com.mojang.blaze3d.platform.NativeImage;

final class Pixels {
	private Pixels() {}

	static int alpha(NativeImage image, int x, int y) {
		return image.getPixel(x, y) >>> 24;
	}
}