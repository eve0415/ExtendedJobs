package net.eve0415.extendedJobs.events

import net.eve0415.extendedJobs.ExtendedJobs
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.PrepareItemCraftEvent

class CraftEventListener(plugin: ExtendedJobs) : EventListener(plugin) {
    val whitelistedCrafting = getRawWhitelistedCrafting().filter { !it.contains("*") }
    val whiteListedCraftingWildcard =
        getRawWhitelistedCrafting().filter { it.contains("*") }.map { Regex(it.replace("*", ".*")) }

    @EventHandler
    fun onEvent(event: PrepareItemCraftEvent) {
        val result = event.recipe?.result?.type?.key?.toString() ?: return

        // If the result is not whitelisted in any job, anyone can craft it
        if (!whitelistedCrafting.contains(result) && !whiteListedCraftingWildcard.any { it.matches(result) }) return

        val player = event.viewers[0]
        if (player !is Player) return

        val jobs = getJobs(player)
        if (jobs.isEmpty()) {
            event.inventory.result = null
            return
        }

        jobs.forEach {
            val whitelist = config?.getStringList("$it.craft")
            if (whitelist.isNullOrEmpty()) return@forEach
            if (whitelist.contains(result)) return
            if (
                whitelist
                    .filter { it.contains("*") }
                    .map { Regex(it.replace("*", ".*")) }
                    .any { it.matches(result) }
            ) return
        }

        event.inventory.result = null
    }

    private fun getRawWhitelistedCrafting(): Set<String> {
        return jobs?.flatMap { config?.getStringList("$it.craft") ?: emptyList() }?.toSet()
            ?: emptySet()
    }
}
