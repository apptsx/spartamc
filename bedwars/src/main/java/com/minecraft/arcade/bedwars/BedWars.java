package com.minecraft.arcade.bedwars;

import com.minecraft.arcade.bedwars.packet.SoundPacket;
import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.option.ServerOptions;
import com.minecraft.core.bukkit.command.structure.BukkitCommandHandler;
import com.minecraft.core.bukkit.listener.handler.ListenerHandler;
import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.BedCollectibleController;
import com.minecraft.core.server.type.ServerType;
import org.bukkit.Material;

import java.util.Arrays;

public class BedWars extends BukkitCore {

    @Override
    public void onLoad() {
        super.onLoad();

        getLogger().info("Iniciando BedWars...");

        saveDefaultConfig();

        /* Configurações de Servidor */
        ServerOptions.BLOCK_PLACE = true;
        ServerOptions.BLOCK_BREAK = true;

        ServerOptions.DEFAULT_CHAT = false;
        ServerOptions.DAMAGE_ENABLED = true;

        ServerOptions.DROP_ITEM_ENABLED = true;
        ServerOptions.AUTO_CLEAR_DROPS = false;

        ServerOptions.CHANGE_SKIN_AND_TAG_IN_FAKE = false;

        ServerOptions.BLOCK_INTERACTION_ENABLED = true;
        ServerOptions.NON_INTERACTIVE_BLOCKS = Arrays.asList(Material.BED, Material.BED_BLOCK);

        ServerOptions.SPAWN_CREATURES = false;

        Core.setServerType(ServerType.BEDWARS);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        /* Iniciando jogos */
        getManager().getArcade().handle(this, Constant.SOURCE_DIR + ".arcade.bedwars.arcade.list");

        BedCollectibleController.handle(this);

        /* Iniciando comandos & eventos */
        new BukkitCommandHandler(this).handle(Constant.SOURCE_DIR + ".arcade.bedwars.command");
        new ListenerHandler(this).handle(Constant.SOURCE_DIR + ".arcade.bedwars.listener");

        // Packets
        getManager().getProtocol().addPacketListener(new SoundPacket(this));

        getLogger().info("BedWars iniciado com sucesso.");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        getLogger().info("BedWars desligado com sucesso.");
    }
}
