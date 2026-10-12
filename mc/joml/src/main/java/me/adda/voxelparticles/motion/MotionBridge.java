package me.adda.voxelparticles.motion;

import me.adda.voxelparticles.config.VoxelConfig;
import org.joml.Quaternionf;

public final class MotionBridge {
	private static final Quat Q = new Quat();

	private MotionBridge() {}

	public static void apply(Quaternionf rotation, VoxelConfig.Settings settings, int seed, float time) {
		Q.set(rotation.x, rotation.y, rotation.z, rotation.w);

		ParticleMotion.apply(Q, settings, seed, time);
		rotation.set(Q.x, Q.y, Q.z, Q.w);
	}

	public static float applyDebris(Quaternionf rotation, VoxelConfig.Settings settings, int seed, float time, float landedAt) {
		Q.set(rotation.x, rotation.y, rotation.z, rotation.w);

		float settled = ParticleMotion.applyDebris(Q, settings, seed, time, landedAt);
		rotation.set(Q.x, Q.y, Q.z, Q.w);

		return settled;
	}
}