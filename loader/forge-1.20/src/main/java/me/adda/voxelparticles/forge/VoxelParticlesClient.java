package me.adda.voxelparticles.forge;

import me.adda.voxelparticles.VoxelParticles;
import me.adda.voxelparticles.gui.VoxelConfigScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// Kept apart from the @Mod class so a dedicated server never loads the client classes referenced here
final class VoxelParticlesClient {
	// InputConstants.UNKNOWN.getValue(); the field itself is not found at runtime on NeoForge 1.20.1
	private static final int UNBOUND = -1;
	private static final KeyMapping TOGGLE = new KeyMapping(
		"key.voxelparticles.toggle", UNBOUND, KeyMapping.CATEGORY_MISC);

	private VoxelParticlesClient() {
	}

	static void init() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addListener(VoxelParticlesClient::registerKeys);
		MinecraftForge.EVENT_BUS.addListener(VoxelParticlesClient::onClientTick);
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
