package com.crystalmacro;

import com.crystalmacro.config.ConfigManager;
import com.crystalmacro.gui.CrystalConfigScreen;
import com.crystalmacro.handler.MacroHandler;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class CrystalMacroMod implements ClientModInitializer {
    public static final String MOD_ID = "crystalmacro";
    public static KeyMapping guiKeyBinding;

    @Override
    public void onInitializeClient() {
        ConfigManager.load();

        guiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.crystalmacro.open_gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_PAGE_DOWN,
                "category.crystalmacro"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (guiKeyBinding.consumeClick()) {
                client.setScreen(new CrystalConfigScreen(client.screen));
            }
            MacroHandler.onTick(client);
        });
    }
}
