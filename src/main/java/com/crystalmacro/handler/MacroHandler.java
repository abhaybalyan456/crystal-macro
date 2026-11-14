package com.crystalmacro.handler;

import com.crystalmacro.config.ConfigManager;
import com.crystalmacro.config.MacroConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

public class MacroHandler {
    private static long lastActionTime = 0;
    private static long lastPlaceTime = 0;
    private static long lastBreakTime = 0;

    public static void onTick(Minecraft client) {
        MacroConfig config = ConfigManager.getConfig();

        // 1. Master toggle check
        if (!config.enabled) {
            return;
        }

        // 2. GUI check: Do not execute while any screen is open
        if (client.screen != null) {
            return;
        }

        // 3. Client state verification
        if (client.player == null || client.level == null || client.gameMode == null) {
            return;
        }

        // 4. Hold-to-Activate check: Only execute while key/mouse button is actively held down
        if (!isActivationKeyDown(client, config)) {
            return;
        }

        // 5. Slot Safety Check: Must be holding an End Crystal in main hand or off-hand
        ItemStack mainHand = client.player.getMainHandItem();
        ItemStack offHand = client.player.getOffhandItem();
        InteractionHand crystalHand = null;

        if (mainHand.is(Items.END_CRYSTAL)) {
            crystalHand = InteractionHand.MAIN_HAND;
        } else if (offHand.is(Items.END_CRYSTAL)) {
            crystalHand = InteractionHand.OFF_HAND;
        }

        if (crystalHand == null) {
            return;
        }

        // 6. Timing & Rate Limiting Check
        long now = System.currentTimeMillis();
        long minActionInterval = (long) (1000.0 / Math.max(1, config.targetCPS));
        if (now - lastActionTime < minActionInterval) {
            return;
        }

        // 7. Crosshair Target Evaluation
        HitResult target = client.hitResult;
        if (target == null) {
            return;
        }

        if (target.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) target;
            Entity entity = entityHit.getEntity();

            if (entity instanceof EndCrystal crystal && crystal.isAlive()) {
                if (now - lastBreakTime >= config.breakDelayMs) {
                    client.gameMode.attack(client.player, crystal);
                    client.player.swing(InteractionHand.MAIN_HAND);
                    lastBreakTime = now;
                    lastActionTime = now;
                }
            }
        } else if (target.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) target;
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = client.level.getBlockState(pos);

            if (state.is(Blocks.OBSIDIAN) || state.is(Blocks.BEDROCK)) {
                if (now - lastPlaceTime >= config.placeDelayMs) {
                    InteractionResult result = client.gameMode.useItemOn(client.player, crystalHand, blockHit);
                    if (result.consumesAction()) {
                        client.player.swing(crystalHand);
                        lastPlaceTime = now;
                        lastActionTime = now;
                    }
                }
            }
        }
    }

    private static boolean isActivationKeyDown(Minecraft client, MacroConfig config) {
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
