# Crystal Macro (Fabric 26.2)

A client-side Fabric mod for Minecraft 26.2 providing a customizable hold-to-activate macro with an in-game configuration GUI.

## Features
- **Hold-to-Activate Mode**: Only activates while holding down the configured hold key/button.
- **Slot Safety Check**: Only triggers when holding an End Crystal in main hand or off-hand.
- **In-Game GUI**: Press `Page Down` to configure settings (Master Toggle, Keybind Selector, Target CPS, Place Delay, Break Delay).
- **Persistent Config**: Automatically saves to `.minecraft/config/crystalmacro.json`.
- **Automated Cloud CI**: Pre-configured with GitHub Actions to automatically compile the `.jar` on every push using JDK 21.

## How to Get the Compiled Jar
1. Go to the **Actions** tab in this GitHub repository.
2. Select the latest **Build Mod** workflow run.
3. Download the `crystalmacro-26.2-jar` artifact under the **Artifacts** section at the bottom.
4. Place the `.jar` into your `.minecraft/mods/` folder along with Fabric API for Minecraft 26.2.
