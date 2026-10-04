package net.airplus.ui.client.sodium.gui;

import net.airplus.ui.client.sodium.gui.options.Option;
import net.airplus.ui.client.sodium.gui.options.OptionFlag;
import net.airplus.ui.client.sodium.gui.options.OptionImpact;
import net.airplus.ui.client.sodium.gui.options.OptionGroup;
import net.airplus.ui.client.sodium.gui.options.OptionImpl;
import net.airplus.ui.client.sodium.gui.options.OptionPage;
import net.airplus.ui.client.sodium.gui.options.control.ControlValueFormatter;
import net.airplus.ui.client.sodium.gui.options.control.CyclingControl;
import net.airplus.ui.client.sodium.gui.options.control.SliderControl;
import net.airplus.ui.client.sodium.gui.options.control.TickBoxControl;
import net.airplus.ui.client.sodium.gui.options.storage.FPSBoostStorage;
import net.airplus.ui.client.sodium.gui.options.storage.MinecraftOptionsStorage;
import net.airplus.features.module.modules.render.FPSBoost;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Port of me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages (Sodium 0.4.1).
 * The option definitions are rewritten for the 1.8.9 GameSettings; the page/group/builder
 * structure is kept identical to upstream. Sodium-specific options are dropped.
 */
public class SodiumGameOptionPages {
    private static final MinecraftOptionsStorage vanillaOpts = new MinecraftOptionsStorage();
    private static final FPSBoostStorage fpsBoostOpts = new FPSBoostStorage();

    public enum GraphicsMode {
        FAST, FANCY
    }

    public enum SmoothLightingMode {
        OFF, MINIMUM, MAXIMUM
    }

    public enum ParticlesMode {
        ALL, DECREASED, MINIMAL
    }

    public enum CloudsMode {
        FANCY, FAST, OFF
    }

    public static OptionPage general() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName("Render Distance")
                        .setTooltip("The number of chunk sections to render away from the player. Higher values look better but can greatly reduce frame rates.")
                        .setControl(option -> new SliderControl(option, 2, 32, 1, ControlValueFormatter.quantity("chunks")))
                        .setBinding((options, value) -> options.renderDistanceChunks = value, options -> options.renderDistanceChunks)
                        .setImpact(OptionImpact.HIGH)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName("Brightness")
                        .setTooltip("Controls the brightness of the game. Dark values can make caves easier to see in.")
                        .setControl(opt -> new SliderControl(opt, 0, 100, 1, ControlValueFormatter.brightness()))
                        .setBinding((opts, value) -> opts.gammaSetting = value * 0.01F, (opts) -> (int) (opts.gammaSetting / 0.01F))
                        .build())
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName("GUI Scale")
                        .setTooltip("Controls the size of the user interface. Auto picks the largest scale that fits your window.")
                        .setControl(option -> new SliderControl(option, 0, 4, 1, ControlValueFormatter.guiScale()))
                        .setBinding((opts, value) -> {
                            opts.guiScale = value;

                            // Re-run initGui so the open screen adapts to the new scale (like Sodium's onResolutionChanged)
                            Minecraft client = Minecraft.getMinecraft();
                            GuiScreen screen = client.currentScreen;
                            if (screen != null) {
                                net.minecraft.client.gui.ScaledResolution scaledResolution = new net.minecraft.client.gui.ScaledResolution(client);
                                screen.setWorldAndResolution(client, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
                            }
                        }, opts -> opts.guiScale)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                        .setName("Fullscreen")
                        .setTooltip("Toggles whether the game runs in fullscreen mode.")
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> {
                            opts.fullScreen = value;

                            Minecraft client = Minecraft.getMinecraft();
                            if (client.isFullScreen() != opts.fullScreen) {
                                client.toggleFullscreen();

                                // The client might not be able to enter full-screen mode
                                opts.fullScreen = client.isFullScreen();
                            }
                        }, (opts) -> opts.fullScreen)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                        .setName("VSync")
                        .setTooltip("Synchronizes the game's frame rate with your monitor's refresh rate. Reduces screen tearing but can degrade performance.")
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.enableVsync = value, opts -> opts.enableVsync)
                        .setImpact(OptionImpact.VARIES)
                        .build())
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName("Max Framerate")
                        .setTooltip("Limits the maximum number of frames per second. Lower values produce less heat and save power.")
                        .setControl(option -> new SliderControl(option, 5, 260, 5, ControlValueFormatter.fpsLimit()))
                        .setBinding((opts, value) -> opts.limitFramerate = value, opts -> opts.limitFramerate)
                        .build())
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                        .setName("View Bobbing")
                        .setTooltip("Toggles the camera swaying as the player moves.")
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.viewBobbing = value, opts -> opts.viewBobbing)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                        .setName("Smooth Camera")
                        .setTooltip("Toggles the cinematic camera smoothing used in spectator-like third person modes.")
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.smoothCamera = value, opts -> opts.smoothCamera)
                        .build())
                .build());

        return new OptionPage("General", ImmutableList.copyOf(groups));
    }

    public static OptionPage quality() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(GraphicsMode.class, vanillaOpts)
                        .setName("Graphics")
                        .setTooltip("Controls the graphical quality of the game. Fancy enables extra visual details but can reduce performance.")
                        .setControl(option -> new CyclingControl<>(option, GraphicsMode.class, new String[] {"Fast", "Fancy"}))
                        .setBinding((opts, value) -> opts.fancyGraphics = value == GraphicsMode.FANCY, opts -> opts.fancyGraphics ? GraphicsMode.FANCY : GraphicsMode.FAST)
                        .setImpact(OptionImpact.HIGH)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(SmoothLightingMode.class, vanillaOpts)
                        .setName("Smooth Lighting")
                        .setTooltip("Controls the smooth shading of blocks. Makes lighting look more realistic but slightly reduces performance.")
                        .setControl(option -> new CyclingControl<>(option, SmoothLightingMode.class, new String[] {"Off", "Minimum", "Maximum"}))
                        .setBinding((opts, value) -> opts.ambientOcclusion = value.ordinal(), opts -> SmoothLightingMode.values()[Math.min(2, Math.max(0, opts.ambientOcclusion))])
                        .setImpact(OptionImpact.LOW)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                .add(OptionImpl.createBuilder(ParticlesMode.class, vanillaOpts)
                        .setName("Particles")
                        .setTooltip("Controls the number of particles such as explosions, flames and water drips.")
                        .setControl(option -> new CyclingControl<>(option, ParticlesMode.class, new String[] {"All", "Decreased", "Minimal"}))
                        .setBinding((opts, value) -> opts.particleSetting = value.ordinal(), opts -> ParticlesMode.values()[Math.min(2, Math.max(0, opts.particleSetting))])
                        .setImpact(OptionImpact.MEDIUM)
                        .build())
                .add(OptionImpl.createBuilder(CloudsMode.class, vanillaOpts)
                        .setName("Clouds")
                        .setTooltip("Controls the rendering of clouds. Turning this off can improve performance on some machines.")
                        .setControl(option -> new CyclingControl<>(option, CloudsMode.class, new String[] {"Fancy", "Fast", "Off"}))
                        .setBinding((opts, value) -> opts.clouds = value.ordinal(), opts -> CloudsMode.values()[Math.min(2, Math.max(0, opts.clouds))])
                        .setImpact(OptionImpact.LOW)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                        .setName("Entity Shadows")
                        .setTooltip("Toggles the simple shadow rendered underneath entities.")
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.entityShadows = value, opts -> opts.entityShadows)
                        .setImpact(OptionImpact.LOW)
                        .build())
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(int.class, vanillaOpts)
                        .setName("Mipmap Levels")
                        .setTooltip("Reduces texture noise at a distance by pre-generating smaller texture variants. Requires a resource reload when changed.")
                        .setControl(option -> new SliderControl(option, 0, 4, 1, ControlValueFormatter.multiplier()))
                        .setBinding((opts, value) -> opts.mipmapLevels = value, opts -> opts.mipmapLevels)
                        .setImpact(OptionImpact.MEDIUM)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                        .setName("Alternate Blocks")
                        .setTooltip("Toggles the use of alternate block models for blocks such as grass and stone.")
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.allowBlockAlternatives = value, opts -> opts.allowBlockAlternatives)
                        .build())
                .build());

        return new OptionPage("Quality", ImmutableList.copyOf(groups));
    }

    public static OptionPage performance() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(int.class, fpsBoostOpts)
                        .setName("Chunk Builders")
                        .setTooltip("The number of worker threads used to rebuild chunk geometry. More threads reduce stutter while moving, but takes effect after rejoining a world.")
                        .setControl(o -> new SliderControl(o, 2, 16, 1, ControlValueFormatter.quantity("threads")))
                        .setBinding((opts, value) -> opts.getChunkWorkersSetting().set(value, true), FPSBoost::getChunkWorkers)
                        .setImpact(OptionImpact.HIGH)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                        .setName("Use VBOs")
                        .setTooltip("Renders chunk geometry using vertex buffer objects, which can improve performance on some hardware.")
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.useVbo = value, opts -> opts.useVbo)
                        .setImpact(OptionImpact.MEDIUM)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build())
                .build());

        return new OptionPage("Performance", ImmutableList.copyOf(groups));
    }
}
