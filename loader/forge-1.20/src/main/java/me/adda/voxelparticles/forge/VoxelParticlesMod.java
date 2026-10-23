package me.adda.voxelparticles.forge;

import me.adda.voxelparticles.VoxelParticles;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(VoxelParticles.MOD_ID)
public final class VoxelParticlesMod {
	public VoxelParticlesMod() {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			VoxelParticles.init(FMLPaths.CONFIGDIR.get());
			VoxelParticlesClient.init();
		}
	}
}
