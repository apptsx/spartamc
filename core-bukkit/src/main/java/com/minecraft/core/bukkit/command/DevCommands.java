package com.minecraft.core.bukkit.command;

import com.minecraft.core.Core;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.serialization.Serialization;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;

public class DevCommands implements CommandInheritor {

    @Command(name = "sinv", rank = RankType.ADMIN)
    public void serializeInv(BukkitCommandContext context) {
        Player player = context.getPlayer();

        player.sendMessage("§aInventário serializado!");

        Core.getLogger().info(Serialization.serializeInventoryOfPlayer(player));
    }

    @Command(name = "iunb", rank = RankType.ADMIN)
    public void itemUnbreakable(BukkitCommandContext context) {
        Player player = context.getPlayer();

        ItemStack hand = player.getItemInHand();

        if (hand == null || hand.getType().equals(Material.AIR)) {
            player.sendMessage("§cVocê não está segurando nenhum item.");
            return;
        }

        ItemMeta meta = hand.getItemMeta();

        meta.spigot().setUnbreakable(true);
        hand.setItemMeta(meta);

        player.setItemInHand(hand);
        player.sendMessage("§aO item " + hand.getType().name() + " se tornou inquebrável.");
    }

    @Command(name = "cmloc", rank = RankType.MODPLUS)
    public void customLocation(BukkitCommandContext context) {
        Player player = context.getPlayer();

        Location location = player.getLocation();

        String[] args = context.getArgs();

        TextComponent message = new TextComponent(String.format("§8[§aX: §f%s §aY: §f%s §aZ: §f%s §aYaw: §f%s §aPitch: §f%s§8]",
                Util.formatDouble(location.getX()), Util.formatDouble(location.getY()), Util.formatDouble(location.getZ()), Util.formatDouble(location.getYaw()),
                Util.formatDouble(location.getPitch())));

        if (args.length == 0) {
            player.sendMessage(message);
            return;
        }

        String name = args[0];

        String jsonFormat = "{ \"name\": \"%s\", \"x\": %s, \"y\": %s, \"z\": %s, \"yaw\": %s, \"pitch\": %s" + " }";

        TextComponent copy = new TextComponent(" §6§lCOPIAR JSON");

        copy.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Clique para copiar o Json.")));
        copy.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format(jsonFormat,
                name, Util.formatDouble(location.getX()), Util.formatDouble(location.getY()), Util.formatDouble(location.getZ()), Util.formatDouble(location.getYaw()),
                Util.formatDouble(location.getPitch()))));

        message.addExtra(copy);

        player.sendMessage(message);
    }

    @Command(name = "vblock", rank = RankType.MODPLUS)
    public void visionBlockLocation(BukkitCommandContext context) {
        Player player = context.getPlayer();

        Block viewBlock = player.getTargetBlock((HashSet<Material>) null, 5);

        if (viewBlock == null || viewBlock.isEmpty()) {
            player.sendMessage("§cVocê não está olhando para nenhum bloco.");
            return;
        }

        Location location = viewBlock.getLocation();

        BlockFace face = viewBlock.getFace(viewBlock);

        String[] args = context.getArgs();

        TextComponent message = new TextComponent("§aTipo de bloco: §f" + viewBlock.getType());

        message.addExtra("\n");
        message.addExtra(String.format("§aLocalização: §f%s, %s, %s, %s, %s, %s",
                Util.formatDouble(location.getX()),
                Util.formatDouble(location.getY()),
                Util.formatDouble(location.getZ()),
                Util.formatDouble(location.getYaw()),
                Util.formatDouble(location.getPitch()),
                face.name()));

        if (args.length == 0) {
            player.sendMessage(message);
            return;
        }

        String name = args[0];

        String jsonFormat = "{ \"name\": \"%s\", \"x\": %s, \"y\": %s, \"z\": %s, \"yaw\": %s, \"pitch\": %s, \"face\": %s" + " }";

        TextComponent copy = new TextComponent(" §eClique para copiar.");

        copy.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Clique para copiar o Json.")));
        copy.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format(jsonFormat,
                name,
                Util.formatDouble(location.getX()),
                Util.formatDouble(location.getY()),
                Util.formatDouble(location.getZ()),
                Util.formatDouble(location.getYaw()),
                Util.formatDouble(location.getPitch()),
                face.name())));

        message.addExtra(copy);

        player.sendMessage(message);
    }
}
