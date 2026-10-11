package me.adda.voxelparticles.render;

import me.adda.voxelparticles.mesh.VoxelMesh;

public final class VoxelEmitter {
	private final int[] faceArgb = new int[6];
	private final boolean[] faceVisible = new boolean[6];

	public int emit(
			VoxelMesh mesh, VertexSink sink, float px, float py, float pz, float qx,
			float qy, float qz, float qw, float scale, int argb, int light, float shadingStrength
	) {
		float xx = qx * qx, yy = qy * qy, zz = qz * qz;
		float xy = qx * qy, xz = qx * qz, yz = qy * qz;
		float wx = qw * qx, wy = qw * qy, wz = qw * qz;

		float m00 = 1 - 2 * (yy + zz), m01 = 2 * (xy - wz), m02 = 2 * (xz + wy);
		float m10 = 2 * (xy + wz), m11 = 1 - 2 * (xx + zz), m12 = 2 * (yz - wx);
		float m20 = 2 * (xz - wy), m21 = 2 * (yz + wx), m22 = 1 - 2 * (xx + yy);

		float front = worldShade(m02, m12, m22);
		int a = argb >>> 24, r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;

		for (int f = 0; f < 6; f++) {
			float[] n = VoxelMesh.Face.NORMALS[f];

			float nx = m00 * n[0] + m01 * n[1] + m02 * n[2];
			float ny = m10 * n[0] + m11 * n[1] + m12 * n[2];
			float nz = m20 * n[0] + m21 * n[1] + m22 * n[2];
			faceVisible[f] = -(nx * px + ny * py + nz * pz) > -scale;

			float rel = f >= VoxelMesh.Face.POS_Z ? 1F : Math.min(1F, worldShade(nx, ny, nz) / front * 0.88F);
			float s = 1F - shadingStrength * (1F - rel);
			faceArgb[f] = a << 24 | (int) (r * s) << 16 | (int) (g * s) << 8 | (int) (b * s);
		}

		float s00 = m00 * scale, s01 = m01 * scale, s02 = m02 * scale;
		float s10 = m10 * scale, s11 = m11 * scale, s12 = m12 * scale;
		float s20 = m20 * scale, s21 = m21 * scale, s22 = m22 * scale;

		float[] d = mesh.data;
		byte[] faces = mesh.faces;
		int visible = 0;

		for (int q = 0; q < mesh.quadCount; q++) {
			if (faceVisible[faces[q]]) {
				visible++;
			}
		}

		if (visible == 0) {
			return 0;
		}

		sink.begin(visible * 4);

		int written = 0;
		for (int q = 0; q < mesh.quadCount; q++) {
			byte face = faces[q];

			if (!faceVisible[face]) {
				continue;
			}

			int color = faceArgb[face];
			int i = q * VoxelMesh.FLOATS_PER_QUAD;

			for (int k = 0; k < 4; k++, i += VoxelMesh.FLOATS_PER_VERTEX) {
				float lx = d[i], ly = d[i + 1], lz = d[i + 2];

				sink.vertex(
					px + s00 * lx + s01 * ly + s02 * lz,
					py + s10 * lx + s11 * ly + s12 * lz,
					pz + s20 * lx + s21 * ly + s22 * lz,
					d[i + 3], d[i + 4], color, light
				);
			}
			written++;
		}
		return written;
	}

	private static float worldShade(float nx, float ny, float nz) {
		return nx * nx * 0.6F + nz * nz * 0.8F + ny * ny * (ny > 0 ? 1.0F : 0.5F);
	}
}
