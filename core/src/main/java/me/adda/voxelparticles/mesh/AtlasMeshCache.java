package me.adda.voxelparticles.mesh;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.adda.voxelparticles.atlas.AtlasView;
import me.adda.voxelparticles.config.VoxelConfig;
import org.jetbrains.annotations.Nullable;

public final class AtlasMeshCache {
	private static final int ALPHA_THRESHOLD = 26;
	private static final VoxelMesh NONE = new VoxelMesh(new float[0], new byte[0], 0, 1);
	private static final int MAX_ENTRIES = 16384;
	/** At most this much mesh building per window (about one frame); the rest stays flat until later. */
	private static final long BUILD_BUDGET_NS = 1_500_000L;
	private static final long BUDGET_WINDOW_NS = 16_000_000L;
	private static long budgetWindowStart;
	private static long budgetSpent;

	private final AtlasView atlas;
	private final Long2ObjectOpenHashMap<VoxelMesh> meshes = new Long2ObjectOpenHashMap<>();
	private final VoxelMeshBuilder builder = new VoxelMeshBuilder();

	public AtlasMeshCache(AtlasView atlas) {
		this.atlas = atlas;
	}

	// Texels a vanilla-sized sprite would use for this window, so denser resource packs keep the vanilla thickness and big sprites stay capped
	public float referenceResolution(int w, int h, VoxelConfig config) {
		return Math.max(1F, Math.min(Math.max(w, h) / atlas.resolutionScale(), config.maxResolution));
	}

	// here null means the particle should stay a flat quad
	@Nullable
	public VoxelMesh meshFor(float u0, float u1, float v0, float v1, VoxelConfig config) {
		int aw = atlas.width(), ah = atlas.height();
		float tx0 = u0 * aw, tx1 = u1 * aw, ty0 = v0 * ah, ty1 = v1 * ah;

		boolean flipU = tx1 < tx0, flipV = ty1 < ty0;

		int left = Math.round(Math.min(tx0, tx1)), right = Math.round(Math.max(tx0, tx1));
		int top = Math.round(Math.min(ty0, ty1)), bottom = Math.round(Math.max(ty0, ty1));
		int w = right - left, h = bottom - top;

		if (w <= 0 || h <= 0 || w > 255 || h > 255 || left < 0 || top < 0 || left >= 16384 || top >= 16384) {
			return null;
		}

		long key = left | (long) top << 14 | (long) w << 28 | (long) h << 36 | (flipU ? 1L << 44 : 0) | (flipV ? 1L << 45 : 0);
		VoxelMesh mesh = meshes.get(key);

		if (mesh == null) {
			long now = System.nanoTime();
			if (now - budgetWindowStart > BUDGET_WINDOW_NS) {
				budgetWindowStart = now;
				budgetSpent = 0;
			}

			if (budgetSpent > BUILD_BUDGET_NS) {
				return null;
			}

			if (meshes.size() >= MAX_ENTRIES) {
				meshes.clear();
			}

			mesh = build(left, top, w, h, flipU, flipV, aw, ah, config);

			meshes.put(key, mesh);
			budgetSpent += System.nanoTime() - now;
		}
		return mesh == NONE ? null : mesh;
	}

	private VoxelMesh build(int left, int top, int w, int h, boolean flipU, boolean flipV, int aw, int ah, VoxelConfig config) {
		AtlasView.SpriteView sprite = atlas.spriteAt(left + w / 2, top + h / 2);
		if (sprite == null || config.isExcluded(sprite.id())) {
			return NONE;
		}

		int res = config.maxResolution;
		int gw = w, gh = h;

		if (Math.max(w, h) > res) {
			float f = (float) res / Math.max(w, h);

			gw = Math.max(1, Math.round(w * f));
			gh = Math.max(1, Math.round(h * f));
		}

		boolean[] solid = new boolean[gw * gh];
		int solidCount = 0;

		for (int r = 0; r < gh; r++) {
			int ar = flipV ? gh - 1 - r : r;
			int ty = top + (int) ((ar + 0.5F) * h / gh) - sprite.y();

			for (int c = 0; c < gw; c++) {
				int ac = flipU ? gw - 1 - c : c;
				int tx = left + (int) ((ac + 0.5F) * w / gw) - sprite.x();

				if (tx < 0 || ty < 0 || tx >= sprite.width() || ty >= sprite.height()) {
					continue;
				}

				if (sprite.alpha(tx, ty) >= ALPHA_THRESHOLD) {
					solid[r * gw + c] = true;
					solidCount++;
				}
			}
		}
		if (solidCount == 0) {
			return NONE;
		}

		float uL = (flipU ? left + w : left) / (float) aw, uR = (flipU ? left : left + w) / (float) aw;
		float vT = (flipV ? top + h : top) / (float) ah, vB = (flipV ? top : top + h) / (float) ah;

		float depth = config.depth * Math.max(gw, gh) / referenceResolution(w, h, config);
		return builder.build(gw, gh, solid, uL, uR, vT, vB, depth);
	}
}
