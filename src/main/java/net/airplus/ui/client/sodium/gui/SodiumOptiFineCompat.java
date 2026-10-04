package net.airplus.ui.client.sodium.gui;

import net.airplus.ui.client.sodium.gui.options.OptionFlag;
import net.airplus.ui.client.sodium.gui.options.OptionGroup;
import net.airplus.ui.client.sodium.gui.options.OptionImpact;
import net.airplus.ui.client.sodium.gui.options.OptionImpl;
import net.airplus.ui.client.sodium.gui.options.OptionPage;
import net.airplus.ui.client.sodium.gui.options.control.ControlValueFormatter;
import net.airplus.ui.client.sodium.gui.options.control.CyclingControl;
import net.airplus.ui.client.sodium.gui.options.control.SliderControl;
import net.airplus.ui.client.sodium.gui.options.control.TickBoxControl;
import net.airplus.ui.client.sodium.gui.options.storage.MinecraftOptionsStorage;
import net.minecraft.client.settings.GameSettings;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OptiFine compat for the Sodium options GUI.
 *
 * When OptiFine is installed it replaces GameSettings with a patched subclass
 * carrying public "ofXxx" fields. This class mirrors a curated set of those
 * options into a dedicated "OptiFine" page of the Sodium GUI, using reflection
 * exclusively (OptiFine is not a compile-time dependency).
 *
 * Every option is added only if its backing field exists, so version differences
 * degrade to fewer rows instead of crashes. A page with no usable fields is not
 * created at all.
 *
 * Saving goes through the vanilla MinecraftOptionsStorage: OptiFine patches
 * GameSettings.saveOptions() to persist the ofXxx fields into options.txt.
 */
public class SodiumOptiFineCompat {
    private static final MinecraftOptionsStorage vanillaOpts = new MinecraftOptionsStorage();

    private static Boolean available;
    private static final Map<String, Field> fields = new HashMap<>();

    /** Tri-state used by several OptiFine detail options (0=Default, 1=Fast, 2=Fancy). */
    public enum TriState {
        DEFAULT, FAST, FANCY
    }

    /** Quad-state used by Rain & Snow (0=Default, 1=Fast, 2=Fastest, 3=Off). */
    public enum QuadState {
        DEFAULT, FAST, FASTEST, OFF
    }

    public static boolean isOptiFineAvailable() {
        if (available == null) {
            // 1.8.9-era OptiFine puts Config in the DEFAULT package (class name "Config",
            // see ReplayMod's OptifineReflection); newer versions use "optifine.Config".
            available = classExists("Config") || classExists("optifine.Config");
        }

        return available;
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name, false, SodiumOptiFineCompat.class.getClassLoader());

            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean hasField(String name) {
        if (fields.containsKey(name)) {
            return true;
        }

        try {
            fields.put(name, GameSettings.class.getField(name));

            return true;
        } catch (Throwable ignored) {
            // Fall through: some OptiFine builds keep the fields non-public
        }

        try {
            Field field = GameSettings.class.getDeclaredField(name);
            field.setAccessible(true);
            fields.put(name, field);

            return true;
        } catch (Throwable t) {
            // Field renamed or missing in this OptiFine version -> skip the option
            return false;
        }
    }

    private static Object getFieldValue(GameSettings opts, String name) {
        try {
            return fields.get(name).get(opts);
        } catch (Throwable t) {
            t.printStackTrace();

            return null;
        }
    }

    private static void setFieldValue(GameSettings opts, String name, Object value) {
        try {
            fields.get(name).set(opts, value);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static OptionPage createPage() {
        if (!isOptiFineAvailable()) {
            return null;
        }

        List<OptionGroup> groups = new ArrayList<>();

        OptionGroup.Builder performance = OptionGroup.createBuilder();
        int performanceCount = 0;
        performanceCount += addBool(performance, "Smooth FPS", "ofSmoothFps",
                "Stabilizes the frame rate by flushing the graphics driver buffers. Can introduce slight input latency.");
        performanceCount += addBool(performance, "Smooth World", "ofSmoothWorld",
                "Removes lag spikes caused by chunk updates while moving in singleplayer.");
        performanceCount += addBool(performance, "Fast Render", "ofFastRender",
                "Uses optimized rendering algorithms for a large FPS boost. Requires a game restart to take full effect.");
        performanceCount += addBool(performance, "Lazy Chunk Loading", "ofLazyChunkLoading",
                "Loads chunks more evenly to smooth out world loading in singleplayer.");
        performanceCount += addBool(performance, "Fast Math", "ofFastMath",
                "Uses faster trigonometric functions. Hardware dependent, can improve FPS on weaker GPUs.");
        performanceCount += addSlider(performance, "Chunk Updates", "ofChunkUpdates",
                "The number of chunk geometry rebuilds allowed per frame. Higher values load the world faster but can reduce FPS.");

        if (performanceCount > 0) {
            groups.add(performance.build());
        }

        OptionGroup.Builder details = OptionGroup.createBuilder();
        int detailsCount = 0;
        detailsCount += addBool(details, "Sky", "ofSky",
                "Toggles the rendering of the sky. Turning this off can improve performance.", OptionFlag.REQUIRES_RENDERER_RELOAD);
        detailsCount += addBool(details, "Sun & Moon", "ofSunMoon",
                "Toggles the rendering of the sun and moon.", OptionFlag.REQUIRES_RENDERER_RELOAD);
        detailsCount += addBool(details, "Stars", "ofStars",
                "Toggles the rendering of stars at night.", OptionFlag.REQUIRES_RENDERER_RELOAD);
        detailsCount += addBool(details, "Weather", "ofWeather",
                "Toggles rain and snow effects. Only works in local worlds.");
        detailsCount += addBool(details, "Void Fog", "ofVoidFog",
                "Toggles the fog that appears near the void at low heights.", OptionFlag.REQUIRES_RENDERER_RELOAD);
        detailsCount += addCycle(details, "Vignette", "ofVignette", TriState.class,
                new String[] {"Default", "Fast", "Fancy"},
                "Controls the subtle darkening effect around the screen edges.");
        detailsCount += addCycle(details, "Rain & Snow", "ofRain", QuadState.class,
                new String[] {"Default", "Fast", "Fastest", "Off"},
                "Controls the visual quality of rain and snow.");
        detailsCount += addBool(details, "Clear Water", "ofClearWater",
                "Makes water transparent with better underwater visibility.", OptionFlag.REQUIRES_RENDERER_RELOAD);
        detailsCount += addBool(details, "Swamp Colors", "ofSwampColors",
                "Toggles the darker plant and water colors in swamps.", OptionFlag.REQUIRES_RENDERER_RELOAD);
        detailsCount += addCycle(details, "Dropped Items", "ofDroppedItems", TriState.class,
                new String[] {"Default", "Fast", "Fancy"},
                "Controls how dropped items are rendered.");

        if (detailsCount > 0) {
            groups.add(details.build());
        }

        if (groups.isEmpty()) {
            // No OptiFine fields found -> don't show an empty page
            return null;
        }

        return new OptionPage("OptiFine", com.google.common.collect.ImmutableList.copyOf(groups));
    }

    private static int addBool(OptionGroup.Builder group, String name, String field, String tooltip, OptionFlag... flags) {
        if (!hasField(field)) {
            return 0;
        }

        OptionImpl.Builder<GameSettings, Boolean> builder = OptionImpl.createBuilder(boolean.class, vanillaOpts)
                .setName(name)
                .setTooltip(tooltip)
                .setControl(TickBoxControl::new)
                .setBinding((opts, value) -> setFieldValue(opts, field, value), opts -> (Boolean) getFieldValue(opts, field));

        if (flags.length > 0) {
            builder.setFlags(flags);
        }

        group.add(builder.build());

        return 1;
    }

    private static <E extends Enum<E>> int addCycle(OptionGroup.Builder group, String name, String field,
                                                    Class<E> enumType, String[] labels, String tooltip) {
        if (!hasField(field)) {
            return 0;
        }

        E[] values = enumType.getEnumConstants();

        group.add(OptionImpl.createBuilder(enumType, vanillaOpts)
                .setName(name)
                .setTooltip(tooltip)
                .setControl(option -> new CyclingControl<>(option, enumType, labels))
                .setBinding((opts, value) -> setFieldValue(opts, field, value.ordinal()),
                        opts -> {
                            int raw = ((Number) getFieldValue(opts, field)).intValue();
                            return values[Math.max(0, Math.min(values.length - 1, raw))];
                        })
                .build());

        return 1;
    }

    private static int addSlider(OptionGroup.Builder group, String name, String field, String tooltip) {
        if (!hasField(field)) {
            return 0;
        }

        group.add(OptionImpl.createBuilder(int.class, vanillaOpts)
                .setName(name)
                .setTooltip(tooltip)
                .setControl(option -> new SliderControl(option, 1, 5, 1, ControlValueFormatter.quantity("updates")))
                .setBinding((opts, value) -> setFieldValue(opts, field, value),
                        opts -> ((Number) getFieldValue(opts, field)).intValue())
                .build());

        return 1;
    }
}
