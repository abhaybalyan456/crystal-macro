package com.crystalmacro.gui;

import com.crystalmacro.CrystalMacroMod;
import com.crystalmacro.config.ConfigManager;
import com.crystalmacro.config.MacroConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class CrystalConfigScreen extends Screen {
    private final Screen parent;
    private final MacroConfig config;
    private Button toggleButton;
    private Button keybindButton;
    private boolean listeningForKey = false;

    public CrystalConfigScreen(Screen parent) {
        super(Component.literal("Crystal Macro Settings"));
        this.parent = parent;
        this.config = ConfigManager.getConfig();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;
        int spacing = 26;
        int widgetWidth = 220;
        int widgetHeight = 20;

        // 1. Master Toggle Button
        toggleButton = Button.builder(getToggleText(), button -> {
            config.enabled = !config.enabled;
            button.setMessage(getToggleText());
        }).bounds(centerX - (widgetWidth / 2), startY, widgetWidth, widgetHeight).build();
        this.addRenderableWidget(toggleButton);

        // 2. Activation Keybind Button
        keybindButton = Button.builder(getKeybindText(), button -> {
            listeningForKey = true;
            button.setMessage(Component.literal("> Press Key or Mouse <").withStyle(ChatFormatting.YELLOW));
        }).bounds(centerX - (widgetWidth / 2), startY + spacing, widgetWidth, widgetHeight).build();
        this.addRenderableWidget(keybindButton);

        // 3. Target CPS Slider (1 to 20 CPS)
        this.addRenderableWidget(new DoubleSliderWidget(
                centerX - (widgetWidth / 2),
                startY + spacing * 2,
                widgetWidth,
                widgetHeight,
                "Target CPS",
                "CPS",
                1.0,
                20.0,
                config.targetCPS,
                true,
                val -> config.targetCPS = val.intValue()
        ));

        // 4. Place Delay Slider (0ms to 500ms)
        this.addRenderableWidget(new DoubleSliderWidget(
                centerX - (widgetWidth / 2),
                startY + spacing * 3,
                widgetWidth,
                widgetHeight,
                "Place Delay",
                "ms",
                0.0,
                500.0,
                config.placeDelayMs,
                true,
                val -> config.placeDelayMs = val.intValue()
        ));

        // 5. Break Delay Slider (0ms to 500ms)
        this.addRenderableWidget(new DoubleSliderWidget(
                centerX - (widgetWidth / 2),
                startY + spacing * 4,
                widgetWidth,
                widgetHeight,
                "Break Delay",
                "ms",
                0.0,
                500.0,
                config.breakDelayMs,
                true,
                val -> config.breakDelayMs = val.intValue()
        ));

        // 6. Done Button
        Button doneButton = Button.builder(Component.literal("Done"), button -> {
            this.onClose();
        }).bounds(centerX - (widgetWidth / 2), startY + spacing * 6, widgetWidth, widgetHeight).build();
        this.addRenderableWidget(doneButton);
    }

    private Component getToggleText() {
        return Component.literal("Macro: ").append(
                config.enabled
                        ? Component.literal("ENABLED").withStyle(ChatFormatting.GREEN)
                        : Component.literal("DISABLED").withStyle(ChatFormatting.RED)
        );
    }

    private Component getKeybindText() {
        return Component.literal("Hold Key: ").append(
                Component.literal(config.activationKeyName).withStyle(ChatFormatting.AQUA)
        );
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningForKey) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                listeningForKey = false;
                keybindButton.setMessage(getKeybindText());
                return true;
            }

            config.activationKeyCode = keyCode;
            config.isActivationMouse = false;

            String name = InputConstants.getKey(keyCode, scanCode).getDisplayName().getString();
            if (name == null || name.isEmpty() || name.equalsIgnoreCase("null")) {
                name = "Key " + keyCode;
            }
            config.activationKeyName = name.toUpperCase();

            listeningForKey = false;
            keybindButton.setMessage(getKeybindText());
            return true;
        }

        if (CrystalMacroMod.guiKeyBinding.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (listeningForKey) {
            config.activationKeyCode = button;
            config.isActivationMouse = true;
            config.activationKeyName = "Mouse " + (button + 1);
            listeningForKey = false;
            keybindButton.setMessage(getKeybindText());
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 0xFFFFFF);

        guiGraphics.drawCenteredString(
                this.font,
                Component.literal("Macro only runs while holding an End Crystal in hand").withStyle(ChatFormatting.GRAY),
                this.width / 2,
                40 + (26 * 5) + 6,
                0xAAAAAA
        );

        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        ConfigManager.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
