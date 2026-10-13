package me.adda.voxelparticles.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import me.adda.voxelparticles.VoxelParticles;
import me.adda.voxelparticles.gui.VoxelConfigScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = VoxelParticles.MOD_ID, dist = Dist.CLIENT)
public final class VoxelParticlesMod {
	private static final KeyMapping TOGGLE = new KeyMapping(
		"key.voxelparticles.toggle", InputConstants.UNKNOWN.getValue(), KeyMapping.Category.MISC);

	public VoxelParticlesMod(IEventBus modBus, ModContainer container) {
		VoxelParticles.init(FMLPaths.CONFIGDIR.get());

		modBus.addListener(VoxelParticlesMod::registerKeys);
		NeoForge.EVENT_BUS.addListener(VoxelParticlesMod::onClientTick);
		container.registerExtensionPoint(IConfigScreenFactory.class,
			(modContainer, parent) -> new VoxelConfigScreen(parent));
	}

	private static void registerKeys(RegisterKeyMappingsEvent event) {
		event.register(TOGGLE);
	}

	private static void onClientTick(ClientTickEvent.Post event) {
		while (TOGGLE.consumeClick()) {
			VoxelParticles.toggle();

			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.player != null) {
				minecraft.player.displayClientMessage(Component.translatable(
					VoxelParticles.config().enabled ? "voxelparticles.toggle.on" : "voxelparticles.toggle.off"), true);
			}
		}
	}
}
