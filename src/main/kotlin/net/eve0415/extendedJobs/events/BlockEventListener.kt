package net.eve0415.extendedJobs.events

import net.eve0415.extendedJobs.ExtendedJobs
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockPlaceEvent

data class BlockEventListener(private val plugin: ExtendedJobs) : EventListener(plugin) {
    val whitelistedPlace = getRawWhitelistedPlace().filter { !it.contains("*") }
    val whiteListedPlaceWildcard =
        getRawWhitelistedPlace().filter { it.contains("*") }.map { Regex(it.replace("*", ".*")) }

    @EventHandler
    fun onEvent(event: BlockPlaceEvent) {
        val block = event.block.type.key.toString()

        // If the block is not whitelisted in any job, anyone can place it
        if (!whitelistedPlace.contains(block) && !whiteListedPlaceWildcard.any { it.matches(block) }) return

        val jobs = getJobs(event.player)
        if (jobs.isEmpty()) {
            event.isCancelled = true
            return
        }

        jobs.forEach {
            val whitelist = config?.getStringList("$it.place")
            if (whitelist.isNullOrEmpty()) return@forEach
            if (whitelist.contains(block)) return
            if (
                whitelist
                    .filter { it.contains("*") }
                    .map { Regex(it.replace("*", ".*")) }
                    .any { it.matches(block) }
            ) return
        }

        event.isCancelled = true
    }

    private fun getRawWhitelistedPlace(): Set<String> {
        return jobs?.flatMap { config?.getStringList("$it.place") ?: emptyList() }?.toSet()
            ?: emptySet()
    }
}
