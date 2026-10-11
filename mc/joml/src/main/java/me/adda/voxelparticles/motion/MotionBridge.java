package me.adda.voxelparticles.motion;

import org.joml.Quaternionf;

public final class MotionBridge {
	private static final Quat Q = new Quat();

	private MotionBridge() {}

	public static void apply(Quaternionf rotation, int seed, float time) {
		Q.set(rotation.x, rotation.y, rotation.z, rotation.w);

		ParticleMotion.apply(Q, seed, time);
		rotation.set(Q.x, Q.y, Q.z, Q.w);
	}

	public static float applyDebris(Quaternionf rotation, int seed, float time, float landedAt) {
		Q.set(rotation.x, rotation.y, rotation.z, rotation.w);

		float settled = ParticleMotion.applyDebris(Q, seed, time, landedAt);
		rotation.set(Q.x, Q.y, Q.z, Q.w);

		return settled;
	}
}