package me.adda.voxelparticles.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import me.adda.voxelparticles.VoxelParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public final class VoxelParticlesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		VoxelParticles.init(FabricLoader.getInstance().getConfigDir());

		KeyMapping toggle = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.voxelparticles.toggle", InputConstants.UNKNOWN.getValue(), KeyMapping.CATEGORY_MISC));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggle.consumeClick()) {
				VoxelParticles.toggle();
				if (client.player != null) {
					client.player.displayClientMessage(Component.translatable(
						VoxelParticles.config().enabled ? "voxelparticles.toggle.on" : "voxelparticles.toggle.off"), true);
				}
			}
		});
	}
}
