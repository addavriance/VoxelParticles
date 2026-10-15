package me.adda.voxelparticles.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import me.adda.voxelparticles.VoxelParticles;
import me.adda.voxelparticles.gui.VoxelConfigScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;

@Mod(VoxelParticles.MOD_ID)
public final class VoxelParticlesMod {
	private static final KeyMapping TOGGLE = new KeyMapping(
		"key.voxelparticles.toggle", InputConstants.UNKNOWN.getValue(), KeyMapping.CATEGORY_MISC);

	public VoxelParticlesMod() {
		VoxelParticles.init(FMLPaths.CONFIGDIR.get());

		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addListener(VoxelParticlesMod::registerKeys);
		NeoForge.EVENT_BUS.addListener(VoxelParticlesMod::onClientTick);
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
			() -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new VoxelConfigScreen(parent)));
	}

	private static void registerKeys(RegisterKeyMappingsEvent event) {
		event.register(TOGGLE);
	}

	private static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}

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
