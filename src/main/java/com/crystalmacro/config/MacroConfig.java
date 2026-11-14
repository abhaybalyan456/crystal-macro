package com.crystalmacro.config;

import org.lwjgl.glfw.GLFW;

public class MacroConfig {
    public boolean enabled = true;
    public int activationKeyCode = GLFW.GLFW_KEY_SPACE;
    public boolean isActivationMouse = false;
    public String activationKeyName = "SPACE";

    // Timing parameters
    public int targetCPS = 12;
    public int placeDelayMs = 50;
    public int breakDelayMs = 50;
}
