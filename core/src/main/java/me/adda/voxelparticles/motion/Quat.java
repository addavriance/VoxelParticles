package me.adda.voxelparticles.motion;

public final class Quat {
	public float x, y, z, w = 1;

	public Quat set(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}

	public Quat premulRotationY(float angle) {
		float s = (float) Math.sin(angle * 0.5), c = (float) Math.cos(angle * 0.5);

		return set(c * x + s * z, c * y + s * w, c * z - s * x, c * w - s * y);
	}

	public Quat rotateAxis(float angle, float ax, float ay, float az) {
		float s = (float) Math.sin(angle * 0.5), c = (float) Math.cos(angle * 0.5);
		float bx = ax * s, by = ay * s, bz = az * s;

		return set(
			w * bx + x * c + y * bz - z * by,
			w * by - x * bz + y * c + z * bx,
			w * bz + x * by - y * bx + z * c,
			w * c - x * bx - y * by - z * bz
		);
	}

	public Quat rotationYX(float yaw, float pitch) {
		float sy = (float) Math.sin(yaw * 0.5), cy = (float) Math.cos(yaw * 0.5);
		float sp = (float) Math.sin(pitch * 0.5), cp = (float) Math.cos(pitch * 0.5);
		return set(cy * sp, sy * cp, -sy * sp, cy * cp);
	}

	public Quat slerp(Quat target, float t) {
		float tx = target.x, ty = target.y, tz = target.z, tw = target.w;
		float dot = x * tx + y * ty + z * tz + w * tw;

		if (dot < 0) {
			dot = -dot;
			tx = -tx;
			ty = -ty;
			tz = -tz;
			tw = -tw;
		}

		float a, b;

		if (dot > 0.9995F) {
			a = 1 - t;
			b = t;
		} else {
			float theta = (float) Math.acos(dot), sin = (float) Math.sin(theta);
			a = (float) Math.sin((1 - t) * theta) / sin;
			b = (float) Math.sin(t * theta) / sin;
		}

		set(a * x + b * tx, a * y + b * ty, a * z + b * tz, a * w + b * tw);
		float len = (float) Math.sqrt(x * x + y * y + z * z + w * w);

		return set(x / len, y / len, z / len, w / len);
	}
}
