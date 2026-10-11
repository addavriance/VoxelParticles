package me.adda.voxelparticles.atlas;

import me.adda.voxelparticles.VoxelParticles;
import me.adda.voxelparticles.mesh.AtlasMeshCache;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class AtlasCaches<K> {
	private final Map<K, AtlasView> views = new HashMap<>();
	private final Map<K, AtlasMeshCache> meshes = new HashMap<>();
	private int shapeGeneration = -1;

	public @Nullable AtlasMeshCache get(K atlas, Function<K, @Nullable AtlasView> factory) {
		if (shapeGeneration != VoxelParticles.shapeGeneration()) {
			shapeGeneration = VoxelParticles.shapeGeneration();
			meshes.clear();
		}

		AtlasMeshCache cache = meshes.get(atlas);

		if (cache == null) {
			AtlasView view = views.computeIfAbsent(atlas, factory);

			if (view == null) {
				return null;
			}

			cache = new AtlasMeshCache(view);
			meshes.put(atlas, cache);
		}
		return cache;
	}

	public void invalidate(K atlas) {
		views.remove(atlas);
		meshes.remove(atlas);
	}
}
