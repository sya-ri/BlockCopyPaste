package com.github.syari.blockcopypaste

import com.github.syari.spigot.api.command.command
import com.github.syari.spigot.api.event.events
import com.github.syari.spigot.api.item.customModelData
import com.github.syari.spigot.api.item.itemStack
import com.github.syari.spigot.api.string.toColor
import com.github.syari.spigot.api.uuid.UUIDPlayer
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    private val brushItem = itemStack(
        Material.STICK,
        "&a&l&nコピー&6&l & &a&l&nペースト&6&l ブラシ",
        listOf(
            "&b▶ &7ブロックを &b右クリック &7するとブロックをコピーします",
            "&b◀ &7ブロックを &b左クリック &7するとコピーしたブロックを貼り付けます"
        )
    ) {
        customModelData = 1
    }

    private val copyBlockData = mutableMapOf<UUIDPlayer, BlockData>()

    override fun onEnable() {
        command("brush") {
            execute {
                val player = sender as? Player ?: return@execute sender.sendMessage("&cプレイヤーからのみ実行できます".toColor())
                player.inventory.addItem(brushItem)
            }
        }
        events {
            event<PlayerInteractEvent> {
                val item = it.item ?: return@event
                if (item.isSimilar(brushItem)) {
                    val block = it.clickedBlock ?: return@event
                    val player = it.player
                    val uuidPlayer = UUIDPlayer.from(player)
                    when (it.action) {
                        Action.RIGHT_CLICK_BLOCK -> {
                            copyBlockData[uuidPlayer] = block.blockData
                            player.sendActionBarMessage("&6&lブロックをコピーしました")
                        }
                        Action.LEFT_CLICK_BLOCK -> {
                            if (player.level < 1) return@event player.sendActionBarMessage("&c&l貼り付けるのに必要な経験値がありません")
                            player.level --
                            block.blockData = copyBlockData[uuidPlayer] ?: return@event player.sendActionBarMessage("&c&lブロックをコピーしてください")
                            player.sendActionBarMessage("&a&lブロックを貼り付けました")
                        }
                        else -> return@event
                    }
                    it.isCancelled = true
                }
            }
        }
    }

    private fun Player.sendActionBarMessage(message: String) {
        spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(message.toColor()))
    }
}
