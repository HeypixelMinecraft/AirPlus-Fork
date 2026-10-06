/*
 * AirPlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/lmx0721/AirPlus
 */
package net.airplus.api

import net.airplus.AirPlus
import net.airplus.utils.client.ClientUtils.LOGGER
import java.util.Properties

object ClientUpdate {

    val gitInfo = Properties().also {
        val inputStream = AirPlus::class.java.classLoader.getResourceAsStream("git.properties")

        if (inputStream != null) {
            it.load(inputStream)
        }
    }

    fun reloadNewestVersion() {
        try {
            newestVersion = ClientApi.getNewestRelease()
        } catch (e: Exception) {
            // Non-fatal: only the update checker depends on this.
            // Note: GitHub's /releases/latest returns 404 when the repository
            // has no published (non-draft, non-prerelease) release yet.
            LOGGER.warn("Unable to receive update information: ${e.message}")
        }
    }

    var newestVersion: GitHubRelease? = null
        private set

    fun hasUpdate(): Boolean {
        try {
            val newestVersion = newestVersion ?: return false
            if (newestVersion.draft) return false

            return compareVersions(newestVersion.tagName, AirPlus.clientVersionText) > 0
        } catch (e: Exception) {
            LOGGER.error("Unable to check for update", e)
            return false
        }
    }

    /**
     * 比较两个点分版本号（支持 tag 前缀 v/V），返回 >0 表示 a 更新
     */
    private fun compareVersions(a: String, b: String): Int {
        fun parse(version: String) =
            version.trim().removePrefix("v").removePrefix("V")
                .split('.').map { it.trim().toIntOrNull() ?: 0 }

        val pa = parse(a)
        val pb = parse(b)

        for (i in 0 until maxOf(pa.size, pb.size)) {
            val x = pa.getOrElse(i) { 0 }
            val y = pb.getOrElse(i) { 0 }
            if (x != y) return x - y
        }

        return 0
    }

}
