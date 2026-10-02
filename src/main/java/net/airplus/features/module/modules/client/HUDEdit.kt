package net.airplus.features.module.modules.client

import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.ui.client.hud.designer.GuiHudDesigner

object HUDEdit : Module("HUDEdit", Category.CLIENT, canBeEnabled = false) {

    override fun onEnable() {
        super.onEnable()
        mc.displayGuiScreen(GuiHudDesigner())
    }
}
