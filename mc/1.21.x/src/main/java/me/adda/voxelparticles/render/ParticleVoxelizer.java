package me.adda.voxelparticles.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import me.adda.voxelparticles.VoxelParticles;
import me.adda.voxelparticles.atlas.AtlasCaches;
import me.adda.voxelparticles.atlas.AtlasView;
import me.adda.voxelparticles.atlas.McAtlasView;
import me.adda.voxelparticles.config.VoxelConfig;
import me.adda.voxelparticles.mesh.AtlasMeshCache;
import me.adda.voxelparticles.mesh.VoxelMesh;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

public final class ParticleVoxelizer {
	public enum Kind { PARTICLE, BLOCK, ITEM }

	private static final AtlasCaches<ResourceLocation> CACHES = new AtlasCaches<>();
	private static final Map<TextureAtlasSprite, Boolean> EXCLUDED = new IdentityHashMap<>();
	private static final VoxelEmitter EMITTER = new VoxelEmitter();
	private static final Sink SINK = new Sink();
	private static final FastParticleSink FAST = new FastParticleSink();
	private static final float SUBPIXEL_ANGLE = 0.0011F;
	private static int cachedGeneration = -1;

	private ParticleVoxelizer() {
	}

	private static @Nullable AtlasView createView(ResourceLocation location) {
		AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(location);
		return texture instanceof TextureAtlas atlas ? new McAtlasView(atlas) : null;
	}

	public static void invalidate(ResourceLocation atlas) {
		CACHES.invalidate(atlas);
		EXCLUDED.clear();
	}

	private static void checkGeneration() {
		if (cachedGeneration != VoxelParticles.shapeGeneration()) {
			cachedGeneration = VoxelParticles.shapeGeneration();
			EXCLUDED.clear();
		}
	}

	private static @Nullable AtlasMeshCache cacheFor(ResourceLocation location) {
		return CACHES.get(location, ParticleVoxelizer::createView);
	}

	public static boolean render(
			VertexConsumer builder, @Nullable TextureAtlasSprite sprite, Kind kind,
			float x, float y, float z, float qx, float qy, float qz, float qw, float scale,
			float u0, float u1, float v0, float v1, int color, int light
	) {
		VoxelConfig cfg = VoxelParticles.config();
		if (!cfg.enabled || sprite == null || RenderGuard.tripped()) {
			return false;
		}

		if ((kind == Kind.BLOCK && !cfg.blockParticles) || (kind == Kind.ITEM && !cfg.itemParticles)) {
			return false;
		}

		RenderGuard.enter();
		try {
			checkGeneration();
			float dist2 = x * x + y * y + z * z;
			float lod = cfg.lodDistance;

			if (lod > 0 && dist2 > lod * lod) {
				return false;
			}

			AtlasMeshCache cache = cacheFor(sprite.atlasLocation());
			if (cache == null) {
				return false;
			}

			VoxelMesh mesh = cache.meshFor(u0, u1, v0, v1, cfg);
			if (mesh == null) {
				return false;
			}

			float voxel = 2F * scale / mesh.resolution;
			if (voxel * voxel < dist2 * SUBPIXEL_ANGLE * SUBPIXEL_ANGLE) {
				return false;
			}

			VertexSink sink;
			if (FAST.bind(builder)) {
				sink = FAST;
			} else {
				SINK.target = builder;
				sink = SINK;
			}

			EMITTER.emit(mesh, sink, x, y, z, qx, qy, qz, qw, scale, color, light, cfg.shading);
			return true;
		} finally {
			RenderGuard.exit();
		}
	}

	public static float referenceResolution(TextureAtlasSprite sprite, int window) {
		AtlasMeshCache cache = cacheFor(sprite.atlasLocation());
		return cache != null ? cache.referenceResolution(window, window, VoxelParticles.config()) : window;
	}

	public static boolean allowsMotion(@Nullable TextureAtlasSprite sprite) {
		if (sprite == null) {
			return false;
		}

		checkGeneration();
		Boolean excluded = EXCLUDED.get(sprite);

		if (excluded == null) {
			excluded = VoxelParticles.config().isExcluded(sprite.contents().name().toString());
			EXCLUDED.put(sprite, excluded);
		}

		return !excluded;
	}

	private static final class Sink implements VertexSink {
		@Nullable VertexConsumer target;

		@Override
		public void vertex(float x, float y, float z, float u, float v, int argb, int light) {
			target.addVertex(x, y, z).setUv(u, v).setColor(argb).setLight(light);
		}
	}
}
