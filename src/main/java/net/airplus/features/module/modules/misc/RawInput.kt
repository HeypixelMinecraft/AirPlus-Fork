/*skid gold bounce
 *https://github.com/bzym2/GoldBounce/
 */
package net.airplus.features.module.modules.misc

import net.airplus.features.module.Category
import net.airplus.features.module.Module
import net.airplus.utils.client.chat
import net.airplus.utils.client.SysUtils
import net.airplus.utils.skid.fpsmaster.RawInputMod

object RawInput : Module("RawInput", Category.CLIENT) {
    override fun onEnable(){
        if(SysUtils().isAndroid()){
            chat("警告: RawInput模块在安卓上无法使用，可能会导致视角无法转动!")
            RawInputMod().stop()
            return
        } else if (SysUtils().isLinux()){
            chat("警告: RawInput模块在Linux上无法使用，可能会导致视角无法转动!")
            RawInputMod().stop()
            return
        }
        RawInputMod().start()
    }
    override fun onDisable(){
        RawInputMod().stop()
    }
}
