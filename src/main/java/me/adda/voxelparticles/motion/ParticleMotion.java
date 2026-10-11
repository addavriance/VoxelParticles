package me.adda.voxelparticles.motion;

import me.adda.voxelparticles.VoxelParticles;
import me.adda.voxelparticles.config.VoxelConfig;

public final class ParticleMotion {
	private static final Quat FLAT = new Quat();
	private static final float[] AXIS = new float[3];

	private ParticleMotion() {}

	public static float applyDebris(Quat rotation, int seed, float time, float landedAt) {
		float phase = phase(seed);
		float[] axis = randomAxis(seed);
		float spinTime = landedAt >= 0 ? Math.min(time, landedAt) : time;

		rotation.rotateAxis(phase + spinTime * 0.45F * VoxelParticles.config().motionSpeed, axis[0], axis[1], axis[2]);

		if (landedAt < 0) {
			return 0F;
		}

		float t = Math.min(1F, (time - landedAt) / 2F);
		t = t * t * (3 - 2 * t);

		FLAT.rotationYX(phase, (float) (-Math.PI / 2));
		rotation.slerp(FLAT, t);

		return t;
	}

	public static void apply(Quat rotation, int seed, float time) {
		VoxelConfig cfg = VoxelParticles.config();
		if (!cfg.enabled) {
			return;
		}

		float phase = phase(seed);
		float speed = cfg.motionSpeed;

		switch (cfg.motion) {
			case BILLBOARD -> {
			}
			case SWAY -> {
				float angle = (float) Math.sin(time * 0.09F * speed + phase) * 0.65F;
				rotation.premulRotationY(angle);
			}
			case SPIN -> {
				float dir = (seed & 0x10000) != 0 ? 1F : -1F;
				rotation.premulRotationY(phase + dir * time * 0.12F * speed);
			}
			case TUMBLE -> {
				float[] axis = randomAxis(seed);
				rotation.rotateAxis(phase + time * 0.14F * speed, axis[0], axis[1], axis[2]);
			}
		}
	}

	private static float phase(int seed) {
		return (seed & 0xFFFF) / 65536F * (float) (Math.PI * 2);
	}
	private static float[] randomAxis(int seed) {
		float x = ((seed >>> 8) & 0xFF) / 127.5F - 1F;
		float y = ((seed >>> 16) & 0xFF) / 127.5F - 1F;
		float z = ((seed >>> 24) & 0xFF) / 127.5F - 1F;
		float len = (float) Math.sqrt(x * x + y * y + z * z);

		if (len < 1.0E-3F) {
			AXIS[0] = 0;
			AXIS[1] = 1;
			AXIS[2] = 0;
		} else {
			AXIS[0] = x / len;
			AXIS[1] = y / len;
			AXIS[2] = z / len;
		}

		return AXIS;
	}
}
