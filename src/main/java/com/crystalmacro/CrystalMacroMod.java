package com.crystalmacro;

import com.crystalmacro.config.ConfigManager;
import com.crystalmacro.gui.CrystalConfigScreen;
import com.crystalmacro.handler.MacroHandler;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class CrystalMacroMod implements ClientModInitializer {
    public static final String MOD_ID = "crystalmacro";
    public static KeyMapping guiKeyBinding;

    @Override
    public void onInitializeClient() {
        ConfigManager.load();

        guiKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.crystalmacro.open_gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_PAGE_DOWN,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (guiKeyBinding.consumeClick()) {
                client.gui.setScreen(new CrystalConfigScreen(client.gui.screen));
            }
            MacroHandler.onTick(client);
        });
    }
}
