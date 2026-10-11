package me.adda.voxelparticles.render;

public interface VertexSink {
	default void begin(int vertexCount) {
	}

	void vertex(float x, float y, float z, float u, float v, int argb, int light);
}
