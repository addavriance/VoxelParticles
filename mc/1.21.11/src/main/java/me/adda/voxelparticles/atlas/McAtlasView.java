package me.adda.voxelparticles.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.adda.voxelparticles.mixin.SpriteContentsAccessor;
import me.adda.voxelparticles.mixin.TextureAtlasAccessor;
import me.adda.voxelparticles.mixin.TextureAtlasSpriteAccessor;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class McAtlasView implements AtlasView {
	private static final int CELL = 32;

	private final int width;
	private final int height;
	private final int cellsX;
	private final float resolutionScale;
	private final Int2ObjectOpenHashMap<List<Sprite>> cells = new Int2ObjectOpenHashMap<>();

	public McAtlasView(TextureAtlas atlas) {
		TextureAtlasAccessor accessor = (TextureAtlasAccessor) atlas;

		this.width = accessor.voxelparticles$getWidth();
		this.height = accessor.voxelparticles$getHeight();
		this.cellsX = (width + CELL - 1) / CELL;

		ResolutionScale scale = new ResolutionScale();

		for (TextureAtlasSprite s : accessor.voxelparticles$getSprites()) {
			Sprite sprite = new Sprite(s);
			scale.add(sprite.w);
			for (int cy = sprite.y / CELL; cy <= (sprite.y + sprite.h - 1) / CELL; cy++) {
				for (int cx = sprite.x / CELL; cx <= (sprite.x + sprite.w - 1) / CELL; cx++) {
					cells.computeIfAbsent(cy * cellsX + cx, k -> new ArrayList<>(2)).add(sprite);
				}
			}
		}

		// Vanilla particle sprites are mostly 8px, blocks and items 16px
		this.resolutionScale = scale.relativeTo(atlas.location().getPath().contains("particles") ? 8 : 16);
	}

	@Override
	public float resolutionScale() {
		return resolutionScale;
	}

	@Override
	public int width() {
		return width;
	}

	@Override
	public int height() {
		return height;
	}

	@Override
	public @Nullable SpriteView spriteAt(int texelX, int texelY) {
		List<Sprite> list = cells.get((texelY / CELL) * cellsX + texelX / CELL);
		if (list == null) {
			return null;
		}
		for (Sprite s : list) {
			if (texelX >= s.x && texelY >= s.y && texelX < s.x + s.w && texelY < s.y + s.h) {
				return s;
			}
		}
		return null;
	}

	private static final class Sprite implements SpriteView {
		final TextureAtlasSprite sprite;
		final int x, y, w, h;

		Sprite(TextureAtlasSprite sprite) {
			// getX/getY point at the stitching slot, the pixels (and UVs) start after the padding
			int padding = ((TextureAtlasSpriteAccessor) sprite).voxelparticles$getPadding();

			this.sprite = sprite;
			this.x = sprite.getX() + padding;
			this.y = sprite.getY() + padding;
			this.w = sprite.contents().width();
			this.h = sprite.contents().height();
		}

		@Override
		public String id() {
			return sprite.contents().name().toString();
		}

		@Override
		public int x() {
			return x;
		}

		@Override
		public int y() {
			return y;
		}

		@Override
		public int width() {
			return w;
		}

		@Override
		public int height() {
			return h;
		}

		@Override
		public int alpha(int localX, int localY) {
			NativeImage image = ((SpriteContentsAccessor) sprite.contents()).voxelparticles$getOriginalImage();

			if (localX >= image.getWidth() || localY >= image.getHeight()) {
				return 0;
			}

			return image.getPixel(localX, localY) >>> 24;
		}
	}
}
