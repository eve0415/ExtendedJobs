package net.eve0415.extendedJobs.events

import com.gamingmesh.jobs.Jobs
import net.eve0415.extendedJobs.ExtendedJobs
import org.bukkit.entity.Player
import org.bukkit.event.Listener

open class EventListener(plugin: ExtendedJobs) : Listener {
    val config = plugin.config.getConfigurationSection("restriction")
    val jobs = config?.getKeys(false)

    fun getJobs(player: Player): Set<String> {
        val jobs = Jobs.getPlayerManager().getJobsPlayer(player).jobProgression
        if (jobs.isEmpty()) return emptySet()

        return Jobs.getPlayerManager().getJobsPlayer(player).jobProgression.map { it.job.name }.toSet()
    }
}
