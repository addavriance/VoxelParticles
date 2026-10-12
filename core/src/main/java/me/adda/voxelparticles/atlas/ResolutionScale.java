package me.adda.voxelparticles.atlas;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public final class ResolutionScale {
	private static final float MIN = 0.25F;
	private static final float MAX = 8F;

	private final Int2IntOpenHashMap counts = new Int2IntOpenHashMap();

	public void add(int spriteWidth) {
		counts.addTo(spriteWidth, 1);
	}

	// How many times larger the atlas' most common sprite is than the vanilla one, so a resource pack keeps the vanilla thickness
	public float relativeTo(int vanillaWidth) {
		int common = 0;
		int best = 0;

		for (Int2IntMap.Entry entry : counts.int2IntEntrySet()) {
			if (entry.getIntValue() > best || (entry.getIntValue() == best && entry.getIntKey() > common)) {
				common = entry.getIntKey();
				best = entry.getIntValue();
			}
		}

		if (common <= 0) {
			return 1F;
		}

		return Math.max(MIN, Math.min(MAX, common / (float) vanillaWidth));
	}
}
