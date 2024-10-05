package net.eve0415.extendedJobs.events

import net.eve0415.extendedJobs.ExtendedJobs
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockPlaceEvent

class BlockEventListener(plugin: ExtendedJobs) : EventListener(plugin) {
    val whitelistedPlace = getRawWhitelistedPlace().filter { !it.contains("*") }
    val whiteListedPlaceWildcard =
        getRawWhitelistedPlace().filter { it.contains("*") }.map { Regex(it.replace("*", ".*")) }

    val errorMessage = lazy {
        TextComponent("You are not eligible to place this block/item.").apply {
            color = ChatColor.RED
        }
    }

    @EventHandler
    fun onEvent(event: BlockPlaceEvent) {
        val block = event.block.type.key.toString()

        // If the block is not whitelisted in any job, anyone can place it
        if (!whitelistedPlace.contains(block) && !whiteListedPlaceWildcard.any { it.matches(block) }) return

        val jobs = getJobs(event.player)
        if (jobs.isEmpty()) {
            event.isCancelled = true
            event.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, errorMessage.value)
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
        event.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, errorMessage.value)
    }

    private fun getRawWhitelistedPlace(): Set<String> {
        return jobs?.flatMap { config?.getStringList("$it.place") ?: emptyList() }?.toSet()
            ?: emptySet()
    }
}
