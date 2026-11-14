package com.crystalmacro.handler;

import com.crystalmacro.config.ConfigManager;
import com.crystalmacro.config.MacroConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public class MacroHandler {
    private static long lastActionTime = 0;
    private static long lastPlaceTime = 0;
    private static long lastBreakTime = 0;

    public static void onTick(MinecraftClient client) {
        MacroConfig config = ConfigManager.getConfig();

        // 1. Master toggle check
        if (!config.enabled) {
            return;
        }

        // 2. GUI / Chat check: Do not execute while any screen or chat is open
        if (client.currentScreen != null) {
            return;
        }

        // 3. Client state verification
        if (client.player == null || client.world == null || client.interactionManager == null) {
            return;
        }

        // 4. Hold-to-Activate check: Only execute while key/mouse button is actively held down
        if (!isActivationKeyDown(client, config)) {
            return;
        }

        // 5. Slot Safety Check: Must be holding an End Crystal in main hand or off-hand
        ItemStack mainHand = client.player.getMainHandStack();
        ItemStack offHand = client.player.getOffHandStack();
        Hand crystalHand = null;

        if (mainHand.isOf(Items.END_CRYSTAL)) {
            crystalHand = Hand.MAIN_HAND;
        } else if (offHand.isOf(Items.END_CRYSTAL)) {
            crystalHand = Hand.OFF_HAND;
        }

        if (crystalHand == null) {
            return; // Not holding End Crystal in either hand
        }

        // 6. Timing & Rate Limiting Check
        long now = System.currentTimeMillis();
        long minActionInterval = (long) (1000.0 / Math.max(1, config.targetCPS));
        if (now - lastActionTime < minActionInterval) {
            return;
        }

        // 7. Crosshair Target Evaluation (Vanilla Reach)
        HitResult target = client.crosshairTarget;
        if (target == null) {
            return;
        }

        if (target.getType() == HitResult.Type.ENTITY) {
            // Detonation logic: targeted End Crystal entity
            EntityHitResult entityHit = (EntityHitResult) target;
            Entity entity = entityHit.getEntity();

            if (entity instanceof EndCrystalEntity crystal && crystal.isAlive()) {
                if (now - lastBreakTime >= config.breakDelayMs) {
                    client.interactionManager.attackEntity(client.player, crystal);
                    client.player.swingHand(Hand.MAIN_HAND);
                    lastBreakTime = now;
                    lastActionTime = now;
                }
            }
        } else if (target.getType() == HitResult.Type.BLOCK) {
            // Placement logic: looking at Obsidian or Bedrock
            BlockHitResult blockHit = (BlockHitResult) target;
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = client.world.getBlockState(pos);

            if (state.isOf(Blocks.OBSIDIAN) || state.isOf(Blocks.BEDROCK)) {
                if (now - lastPlaceTime >= config.placeDelayMs) {
                    ActionResult result = client.interactionManager.interactBlock(client.player, crystalHand, blockHit);
                    if (result.isAccepted()) {
                        client.player.swingHand(crystalHand);
                        lastPlaceTime = now;
                        lastActionTime = now;
                    }
                }
            }
        }
    }

    private static boolean isActivationKeyDown(MinecraftClient client, MacroConfig config) {
        long windowHandle = client.getWindow().getHandle();
        if (windowHandle == 0) {
            return false;
        }

        if (config.isActivationMouse) {
            return GLFW.glfwGetMouseButton(windowHandle, config.activationKeyCode) == GLFW.GLFW_PRESS;
        } else {
            if (config.activationKeyCode <= 0) {
                return false;
            }
            return GLFW.glfwGetKey(windowHandle, config.activationKeyCode) == GLFW.GLFW_PRESS;
        }
    }
}
