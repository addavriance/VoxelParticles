package me.adda.voxelparticles.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import me.adda.voxelparticles.VoxelParticles;
import me.adda.voxelparticles.config.VoxelConfig;
import me.adda.voxelparticles.motion.MotionBridge;
import me.adda.voxelparticles.render.ParticleVoxelizer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SingleQuadParticle.class, priority = 900)
public abstract class SingleQuadParticleMixin extends Particle {
	@Unique
	private static final Quaternionf voxelparticles$ROTATION = new Quaternionf();

	@Unique
	private float voxelparticles$landedAt = -1F;

	protected SingleQuadParticleMixin(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z);
	}

	@Shadow
	public abstract SingleQuadParticle.FacingCameraMode getFacingCameraMode();

	@Shadow
	public abstract float getQuadSize(float partialTick);

	@Shadow
	protected abstract float getU0();

	@Shadow
	protected abstract float getU1();

	@Shadow
	protected abstract float getV0();

	@Shadow
	protected abstract float getV1();

	@Unique
	private @Nullable TextureAtlasSprite voxelparticles$sprite() {
		Object self = this;
		return self instanceof TextureSheetParticle sheet ? ((TextureSheetParticleAccessor) sheet).voxelparticles$getSprite() : null;
	}

	// Sodium cancels render() and the camera overload of renderRotatedQuad before they write the quad, so voxels are emitted from here
	@Inject(method = "render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V", at = @At("HEAD"), cancellable = true)
	private void voxelparticles$render(VertexConsumer consumer, Camera camera, float partialTick, CallbackInfo ci) {
		VoxelConfig cfg = VoxelParticles.config();
		TextureAtlasSprite sprite = voxelparticles$sprite();
		if (!cfg.enabled || sprite == null) {
			return;
		}

		SingleQuadParticle.FacingCameraMode facing = getFacingCameraMode();
		Quaternionf rotation = voxelparticles$ROTATION.identity();
		facing.setRotation(rotation, camera, partialTick);
		if (this.roll != 0.0F) {
			rotation.rotateZ(Mth.lerp(partialTick, this.oRoll, this.roll));
		}

		Vec3 cam = camera.getPosition();
		float px = (float) (Mth.lerp(partialTick, this.xo, this.x) - cam.x());
		float py = (float) (Mth.lerp(partialTick, this.yo, this.y) - cam.y());
		float pz = (float) (Mth.lerp(partialTick, this.zo, this.z) - cam.z());

		if (
				cfg.motion != VoxelConfig.Motion.BILLBOARD &&
				ParticleVoxelizer.allowsMotion(sprite) &&
				facing == SingleQuadParticle.FacingCameraMode.LOOKAT_XYZ // Particles with their own orientation (flat ripples, vertical streaks) keep it
		) {
			int seed = System.identityHashCode(this) * 0x9E3779B9;
			float time = this.age + partialTick;

			Object particle = this;
			if (cfg.tumbleDebris && (particle instanceof TerrainParticle || particle instanceof BreakingItemParticle)) {
				if (this.onGround && this.voxelparticles$landedAt < 0) {
					this.voxelparticles$landedAt = time;
				}

				float settled = MotionBridge.applyDebris(rotation, seed, time, this.voxelparticles$landedAt);
				if (settled > 0) {
					// Crumbs use a quarter of their sprite; lift them by half a voxel so they rest on the ground
					int res = Math.max(1, Math.min(sprite.contents().width() / 4, cfg.maxResolution));
					py += settled * getQuadSize(partialTick) * cfg.depth / res;
				}
			} else {
				MotionBridge.apply(rotation, seed, time);
			}
		}

		if (voxelparticles$draw(consumer, sprite, rotation, px, py, pz, partialTick)) {
			ci.cancel();
		}
	}

	// Covers particles that override render() but still call renderRotatedQuad themselves
	@Inject(method = "renderRotatedQuad(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lorg/joml/Quaternionf;FFFF)V", at = @At("HEAD"), cancellable = true)
	private void voxelparticles$renderVoxels(
			VertexConsumer consumer, Quaternionf rotation, float x, float y, float z,
			float partialTick, CallbackInfo ci
	) {
		if (voxelparticles$draw(consumer, voxelparticles$sprite(), rotation, x, y, z, partialTick)) {
			ci.cancel();
		}
	}

	@Unique
	private boolean voxelparticles$draw(
			VertexConsumer consumer, @Nullable TextureAtlasSprite sprite, Quaternionf rotation,
			float x, float y, float z, float partialTick
	) {
		Object particle = this;
		ParticleVoxelizer.Kind kind =
				particle instanceof TerrainParticle ? ParticleVoxelizer.Kind.BLOCK :
				particle instanceof BreakingItemParticle ? ParticleVoxelizer.Kind.ITEM :
				ParticleVoxelizer.Kind.PARTICLE;

		int color = voxelparticles$channel(this.alpha) << 24 | voxelparticles$channel(this.rCol) << 16
			| voxelparticles$channel(this.gCol) << 8 | voxelparticles$channel(this.bCol);

		return ParticleVoxelizer.render(consumer, sprite, kind, x, y, z,
			rotation.x, rotation.y, rotation.z, rotation.w, getQuadSize(partialTick),
			getU0(), getU1(), getV0(), getV1(), color, getLightColor(partialTick));
	}

	@Unique
	private static int voxelparticles$channel(float value) {
		return Mth.clamp((int) (value * 255.0F), 0, 255);
	}
}
