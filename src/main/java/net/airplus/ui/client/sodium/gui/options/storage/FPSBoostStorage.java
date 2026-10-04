package net.airplus.ui.client.sodium.gui.options.storage;

import net.airplus.features.module.modules.render.FPSBoost;

/**
 * Storage which binds options to the FPSBoost module settings (AirPlus-specific replacement
 * for Sodium's SodiumOptionsStorage). FPSBoost values persist themselves on set(), so save() is a no-op.
 */
public class FPSBoostStorage implements OptionStorage<FPSBoost> {
    @Override
    public FPSBoost getData() {
        return FPSBoost.INSTANCE;
    }

    @Override
    public void save() {
        // FPSBoost values save their configuration immediately on set()
    }
}
