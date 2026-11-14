package com.crystalmacro;

import com.crystalmacro.config.ConfigManager;
import com.crystalmacro.gui.CrystalConfigScreen;
import com.crystalmacro.handler.MacroHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class CrystalMacroMod implements ClientModInitializer {
    public static final String MOD_ID = "crystalmacro";
    public static KeyBinding guiKeyBinding;

    @Override
    public void onInitializeClient() {
        // Load configuration
        ConfigManager.load();

        // Register Page Down keybind for configuration GUI
        guiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.crystalmacro.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_PAGE_DOWN,
                "category.crystalmacro"
        ));

        // Register client tick listener for GUI toggle and macro execution
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (guiKeyBinding.wasPressed()) {
                client.setScreen(new CrystalConfigScreen(client.currentScreen));
            }
            MacroHandler.onTick(client);
        });
    }
}
