package com.crystalmacro.gui;

import com.crystalmacro.CrystalMacroMod;
import com.crystalmacro.config.ConfigManager;
import com.crystalmacro.config.MacroConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class CrystalConfigScreen extends Screen {
    private final Screen parent;
    private final MacroConfig config;
    private ButtonWidget toggleButton;
    private ButtonWidget keybindButton;
    private boolean listeningForKey = false;

    public CrystalConfigScreen(Screen parent) {
        super(Text.literal("Crystal Macro Settings"));
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
        toggleButton = ButtonWidget.builder(getToggleText(), button -> {
            config.enabled = !config.enabled;
            button.setMessage(getToggleText());
        }).dimensions(centerX - (widgetWidth / 2), startY, widgetWidth, widgetHeight).build();
        this.addDrawableChild(toggleButton);

        // 2. Activation Keybind Button
        keybindButton = ButtonWidget.builder(getKeybindText(), button -> {
            listeningForKey = true;
            button.setMessage(Text.literal("> Press Key or Mouse <").formatted(Formatting.YELLOW));
        }).dimensions(centerX - (widgetWidth / 2), startY + spacing, widgetWidth, widgetHeight).build();
        this.addDrawableChild(keybindButton);

        // 3. Target CPS Slider (1 to 20 CPS)
        this.addDrawableChild(new DoubleSliderWidget(
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
        this.addDrawableChild(new DoubleSliderWidget(
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
        this.addDrawableChild(new DoubleSliderWidget(
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
        ButtonWidget doneButton = ButtonWidget.builder(Text.literal("Done"), button -> {
            this.close();
        }).dimensions(centerX - (widgetWidth / 2), startY + spacing * 6, widgetWidth, widgetHeight).build();
        this.addDrawableChild(doneButton);
    }

    private Text getToggleText() {
        return Text.literal("Macro: ").append(
                config.enabled
                        ? Text.literal("ENABLED").formatted(Formatting.GREEN)
                        : Text.literal("DISABLED").formatted(Formatting.RED)
        );
    }

    private Text getKeybindText() {
        return Text.literal("Hold Key: ").append(
                Text.literal(config.activationKeyName).formatted(Formatting.AQUA)
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

            String name = InputUtil.fromKeyCode(keyCode, scanCode).getLocalizedText().getString();
            if (name == null || name.isEmpty() || name.equalsIgnoreCase("null")) {
                name = "Key " + keyCode;
            }
            config.activationKeyName = name.toUpperCase();

            listeningForKey = false;
            keybindButton.setMessage(getKeybindText());
            return true;
        }

        // Allow toggling closed with the Page Down key
        if (CrystalMacroMod.guiKeyBinding.matchesKey(keyCode, scanCode)) {
            this.close();
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFF);

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Macro only runs while holding an End Crystal in hand").formatted(Formatting.GRAY),
                this.width / 2,
                40 + (26 * 5) + 6,
                0xAAAAAA
        );

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        ConfigManager.save();
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
