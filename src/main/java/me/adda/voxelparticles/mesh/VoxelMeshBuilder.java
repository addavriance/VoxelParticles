package me.adda.voxelparticles.mesh;

import java.util.Arrays;

public final class VoxelMeshBuilder {
	private static final float EDGE_INSET = 0.02F;

	private float[] data = new float[VoxelMesh.FLOATS_PER_QUAD * 64];
	private byte[] faces = new byte[64];
	private int quads;

	private int gw, gh;
	private boolean[] solid;
	private float u0, u1, v0, v1, halfDepth;

	public VoxelMesh build(int gw, int gh, boolean[] solid, float u0, float u1, float v0, float v1, float depth) {
		this.gw = gw;
		this.gh = gh;
		this.solid = solid;
		this.u0 = u0;
		this.u1 = u1;
		this.v0 = v0;
		this.v1 = v1;
		this.halfDepth = depth / Math.max(gw, gh);
		this.quads = 0;

		float hd = halfDepth;

		// Front (+z) and back (-z): the full sprite, alpha test cuts out the silhouette.
		quad(VoxelMesh.Face.POS_Z,
			1, -1, hd, u1, v1,
			1, 1, hd, u1, v0,
			-1, 1, hd, u0, v0,
			-1, -1, hd, u0, v1
		);

		quad(VoxelMesh.Face.NEG_Z,
			-1, -1, -hd, u0, v1,
			-1, 1, -hd, u0, v0,
			1, 1, -hd, u1, v0,
			1, -1, -hd, u1, v1
		);

		// Vertical runs of +x / -x side faces, merged along each column boundary.
		for (int c = 0; c < gw; c++) {
			sideColumn(c, true);
			sideColumn(c, false);
		}
		// Horizontal runs of +y / -y side faces, merged along each row boundary.
		for (int r = 0; r < gh; r++) {
			sideRow(r, true);
			sideRow(r, false);
		}

		return new VoxelMesh(Arrays.copyOf(data, quads * VoxelMesh.FLOATS_PER_QUAD), Arrays.copyOf(faces, quads), quads, Math.max(gw, gh));
	}

	private boolean solidAt(int c, int r) {
		return c >= 0 && r >= 0 && c < gw && r < gh && solid[r * gw + c];
	}

	private boolean exposed(int c, int r, int dc, int dr) {
		return solidAt(c, r) && !solidAt(c + dc, r + dr);
	}

	private float x(float col) {
		return -1F + 2F * col / gw;
	}

	private float y(float row) {
		return 1F - 2F * row / gh;
	}

	private float u(float col) {
		return u0 + (u1 - u0) * col / gw;
	}

	private float v(float row) {
		return v0 + (v1 - v0) * row / gh;
	}

	private void sideColumn(int c, boolean positive) {
		int dc = positive ? 1 : -1;
		float xe = x(positive ? c + 1 : c);
		float uc = u(c + 0.5F);
		float hd = halfDepth;
		int r = 0;

		while (r < gh) {
			if (!exposed(c, r, dc, 0)) {
				r++;
				continue;
			}

			int start = r;
			while (r < gh && exposed(c, r, dc, 0)) {
				r++;
			}

			float yt = y(start), yb = y(r);
			float vt = v(start + EDGE_INSET), vb = v(r - EDGE_INSET);
			quad(positive ? VoxelMesh.Face.POS_X : VoxelMesh.Face.NEG_X,
				xe, yt, hd, uc, vt,
				xe, yt, -hd, uc, vt,
				xe, yb, -hd, uc, vb,
				xe, yb, hd, uc, vb
			);
		}
	}

	private void sideRow(int r, boolean up) {
		int dr = up ? -1 : 1;
		float ye = y(up ? r : r + 1);
		float vc = v(r + 0.5F);
		float hd = halfDepth;
		int c = 0;
		while (c < gw) {
			if (!exposed(c, r, 0, dr)) {
				c++;
				continue;
			}
			int start = c;
			while (c < gw && exposed(c, r, 0, dr)) {
				c++;
			}
			float xl = x(start), xr = x(c);
			float ul = u(start + EDGE_INSET), ur = u(c - EDGE_INSET);
			quad(up ? VoxelMesh.Face.POS_Y : VoxelMesh.Face.NEG_Y,
				xl, ye, hd, ul, vc,
				xr, ye, hd, ur, vc,
				xr, ye, -hd, ur, vc,
				xl, ye, -hd, ul, vc);
		}
	}

	/** Adds a quad, fixing the winding so it is counter-clockwise when seen from outside. */
	private void quad(byte face,
					  float x0, float y0, float z0, float s0, float t0,
					  float x1, float y1, float z1, float s1, float t1,
					  float x2, float y2, float z2, float s2, float t2,
					  float x3, float y3, float z3, float s3, float t3) {
		float[] n = VoxelMesh.Face.NORMALS[face];
		float ax = x1 - x0, ay = y1 - y0, az = z1 - z0;
		float bx = x2 - x0, by = y2 - y0, bz = z2 - z0;
		float cx = ay * bz - az * by, cy = az * bx - ax * bz, cz = ax * by - ay * bx;
		boolean flip = cx * n[0] + cy * n[1] + cz * n[2] < 0;

		ensureCapacity();
		int i = quads * VoxelMesh.FLOATS_PER_QUAD;
		if (!flip) {
			i = put(i, x0, y0, z0, s0, t0);
			i = put(i, x1, y1, z1, s1, t1);
			i = put(i, x2, y2, z2, s2, t2);
			put(i, x3, y3, z3, s3, t3);
		} else {
			i = put(i, x3, y3, z3, s3, t3);
			i = put(i, x2, y2, z2, s2, t2);
			i = put(i, x1, y1, z1, s1, t1);
			put(i, x0, y0, z0, s0, t0);
		}
		faces[quads++] = face;
	}

	private int put(int i, float x, float y, float z, float u, float v) {
		data[i] = x;
		data[i + 1] = y;
		data[i + 2] = z;
		data[i + 3] = u;
		data[i + 4] = v;
		return i + 5;
	}

	private void ensureCapacity() {
		if (quads >= faces.length) {
			faces = Arrays.copyOf(faces, faces.length * 2);
			data = Arrays.copyOf(data, faces.length * VoxelMesh.FLOATS_PER_QUAD);
		}
	}
}
