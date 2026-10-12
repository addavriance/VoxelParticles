package me.adda.voxelparticles.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SingleQuadParticle.class)
public abstract class SingleQuadParticleMixin extends Particle {
	@Shadow
	protected TextureAtlasSprite sprite;

	@Unique
	private float voxelparticles$landedAt = -1F;

	protected SingleQuadParticleMixin(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z);
	}

	@Shadow
	protected abstract void extractRotatedQuad(QuadParticleRenderState state, Quaternionf rotation, float x, float y, float z, float partialTick);

	@WrapOperation(
			method = "extract",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/SingleQuadParticle;extractRotatedQuad(Lnet/minecraft/client/renderer/state/QuadParticleRenderState;Lnet/minecraft/client/Camera;Lorg/joml/Quaternionf;F)V")
	)
	private void voxelparticles$applyMotion(
			SingleQuadParticle self, QuadParticleRenderState state, Camera camera,
			Quaternionf rotation, float partialTick, Operation<Void> original
	) {
		VoxelConfig cfg = VoxelParticles.config();
		if (
				cfg.enabled && cfg.motion != VoxelConfig.Motion.BILLBOARD &&
				ParticleVoxelizer.allowsMotion(this.sprite) &&
				self.getFacingCameraMode() == SingleQuadParticle.FacingCameraMode.LOOKAT_XYZ // Particles with their own orientation (flat ripples, vertical streaks) keep it
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
					int res = Math.max(1, Math.min(this.sprite.contents().width() / 4, cfg.maxResolution));
					float lift = settled * self.getQuadSize(partialTick) * cfg.depth / res;
					Vec3 cam = camera.position();

					this.extractRotatedQuad(state, rotation,
						(float) (Mth.lerp(partialTick, this.xo, this.x) - cam.x()),
						(float) (Mth.lerp(partialTick, this.yo, this.y) - cam.y()) + lift,
						(float) (Mth.lerp(partialTick, this.zo, this.z) - cam.z()),
						partialTick);

					return;
				}
			} else {
				MotionBridge.apply(rotation, seed, time);
			}
		}

		original.call(self, state, camera, rotation, partialTick);
	}
}
