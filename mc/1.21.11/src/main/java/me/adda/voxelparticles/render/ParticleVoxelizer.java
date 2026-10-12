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
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

public final class ParticleVoxelizer {
	private static final AtlasCaches<Identifier> CACHES = new AtlasCaches<>();
	private static final Map<TextureAtlasSprite, Boolean> EXCLUDED = new IdentityHashMap<>();
	private static final VoxelEmitter EMITTER = new VoxelEmitter();
	private static final Sink SINK = new Sink();
	private static final FastParticleSink FAST = new FastParticleSink();
	private static final float SUBPIXEL_ANGLE = 0.0011F;
	private static int cachedGeneration = -1;

	private static @Nullable AtlasMeshCache current;

	private ParticleVoxelizer() {
	}

	private static @Nullable AtlasView createView(Identifier location) {
		AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(location);
		return texture instanceof TextureAtlas atlas ? new McAtlasView(atlas) : null;
	}

	public static void invalidate(Identifier atlas) {
		CACHES.invalidate(atlas);
		EXCLUDED.clear();
	}

	private static void checkGeneration() {
		if (cachedGeneration != VoxelParticles.shapeGeneration()) {
			cachedGeneration = VoxelParticles.shapeGeneration();
			EXCLUDED.clear();
		}
	}

	public static void beginLayer(SingleQuadParticle.Layer layer) {
		RenderGuard.enter();
		current = null;

		VoxelConfig cfg = VoxelParticles.config();
		if (!cfg.enabled) {
			return;
		}

		checkGeneration();
		Identifier location = layer.textureAtlasLocation();
		boolean blocks = location.equals(TextureAtlas.LOCATION_BLOCKS);
		boolean items = location.equals(TextureAtlas.LOCATION_ITEMS);

		if ((blocks && !cfg.blockParticles) || (items && !cfg.itemParticles)) {
			return;
		}

		current = CACHES.get(location, ParticleVoxelizer::createView);
	}

	public static void endLayer() {
		current = null;
		SINK.target = null;
		RenderGuard.exit();
	}

	public static boolean render(
			VertexConsumer builder, float x, float y, float z,
			float qx, float qy, float qz, float qw, float scale,
			float u0, float u1, float v0, float v1, int color, int light
	) {
		AtlasMeshCache cache = current;
		if (cache == null || RenderGuard.tripped()) {
			return false;
		}

		VoxelConfig cfg = VoxelParticles.config();
		float dist2 = x * x + y * y + z * z;
		float lod = cfg.lodDistance;

		if (lod > 0 && dist2 > lod * lod) {
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
	}

	public static float resolutionScale(TextureAtlasSprite sprite) {
		AtlasMeshCache cache = CACHES.get(sprite.atlasLocation(), ParticleVoxelizer::createView);
		return cache != null ? cache.resolutionScale() : 1F;
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
