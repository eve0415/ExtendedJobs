package net.eve0415.extendedJobs.events

import net.eve0415.extendedJobs.ExtendedJobs
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class InteractEventListener(plugin: ExtendedJobs) : EventListener(plugin) {
    val whitelistedInteract = getRawWhitelistedInteract().filter { !it.contains("*") }
    val whiteListedInteractWildcard =
        getRawWhitelistedInteract().filter { it.contains("*") }.map { Regex(it.replace("*", ".*")) }

    val errorMessage = lazy {
        TextComponent("You are not eligible to interact with this block.").apply {
            color = ChatColor.RED
        }
    }

    @EventHandler
    fun onEvent(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val clicked = event.clickedBlock?.type ?: return

        val isLog = clicked.toString().contains("LOG")
        val usingAxe = event.player.inventory.itemInMainHand.toString().contains("AXE") ||
                event.player.inventory.itemInOffHand.toString().contains("AXE")

        // Ignore if the block is not interactable (does not open any GUI) and the player is not trying to strip the log
        if (!clicked.isInteractable && !(isLog && usingAxe)) return

        val block = clicked.key.toString()

        // If the result is not whitelisted in any job, anyone can craft it
        if (!whitelistedInteract.contains(block) && !whiteListedInteractWildcard.any { it.matches(block) }) return

        val jobs = getJobs(event.player)
        if (jobs.isEmpty()) {
            event.isCancelled = true
            event.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, errorMessage.value)
            return
        }

        jobs.forEach {
            val whitelist = config?.getStringList("$it.interact")
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

    private fun getRawWhitelistedInteract(): Set<String> {
        return jobs?.flatMap { config?.getStringList("$it.interact") ?: emptyList() }?.toSet()
            ?: emptySet()
    }
}
