package me.adda.voxelparticles;

import me.adda.voxelparticles.config.VoxelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class VoxelParticles {
	public static final String MOD_ID = "voxelparticles";
	public static final Logger LOG = LoggerFactory.getLogger("VoxelParticles");

	private static VoxelConfig config = new VoxelConfig();
	private static Path configPath;
	private static int shapeGeneration;
	private static String shapeKey = "";

	private VoxelParticles() {
	}

	public static void init(Path configDir) {
		configPath = configDir.resolve(MOD_ID + ".json");
		config = VoxelConfig.load(configPath);
		shapeKey = config.shapeKey();
		LOG.info("Voxel Particles loaded");
	}

	public static VoxelConfig config() {
		return config;
	}

	public static int shapeGeneration() {
		return shapeGeneration;
	}

	public static void onConfigChanged() {
		config.sanitize();
		String key = config.shapeKey();
		if (!key.equals(shapeKey)) {
			shapeKey = key;
			shapeGeneration++;
		}
	}

	public static void saveConfig() {
		if (configPath != null) {
			config.save(configPath);
		}
	}

	public static void toggle() {
		config.enabled = !config.enabled;
		onConfigChanged();
		saveConfig();
	}
}
