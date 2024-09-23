package net.eve0415.extendedJobs

import net.eve0415.extendedJobs.events.BlockEventListener
import net.eve0415.extendedJobs.events.CraftEventListener
import net.eve0415.extendedJobs.events.InteractEventListener
import org.bukkit.plugin.java.JavaPlugin

class ExtendedJobs : JavaPlugin() {
    override fun onEnable() {
        logger.info("Starting up ExtendedJobs")

        saveDefaultConfig()

        server.pluginManager.registerEvents(CraftEventListener(this), this)
        server.pluginManager.registerEvents((InteractEventListener(this)), this)
        server.pluginManager.registerEvents(BlockEventListener(this), this)
    }
}
