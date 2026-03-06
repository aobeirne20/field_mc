package com.gptwe;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class GptWeClient implements ClientModInitializer {
	private static KeyMapping openKey;
	private static final KeyMapping.Category CATEGORY =
			KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath("gptwe", "general"));

	@Override
	public void onInitializeClient() {
		openKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.gptwe.open",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_P,
				CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openKey.consumeClick()) {
				Minecraft.getInstance().setScreen(new GptWeScreen());
			}
		});
	}
}