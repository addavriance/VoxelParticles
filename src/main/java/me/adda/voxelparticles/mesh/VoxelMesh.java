package me.adda.voxelparticles.mesh;

public final class VoxelMesh {
	public static final int FLOATS_PER_VERTEX = 5;
	public static final int FLOATS_PER_QUAD = FLOATS_PER_VERTEX * 4;

	public final float[] data;
	public final byte[] faces;
	public final int quadCount;
	public final int resolution;

	VoxelMesh(float[] data, byte[] faces, int quadCount, int resolution) {
		this.data = data;
		this.faces = faces;
		this.quadCount = quadCount;
		this.resolution = resolution;
	}

	public static final class Face {
		public static final byte POS_X = 0, NEG_X = 1, POS_Y = 2, NEG_Y = 3, POS_Z = 4, NEG_Z = 5;
		public static final float[][] NORMALS = {
			{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}
		};

		private Face() {}
	}
}
