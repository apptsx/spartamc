package com.minecraft.core.bukkit.command;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.account.context.objects.toggle.Toggle;
import com.minecraft.core.bukkit.api.option.ServerOptions;
import com.minecraft.core.bukkit.api.vanish.Vanish;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.bukkit.menu.server.report.ReportMenu;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

public class ModeratorCommands implements CommandInheritor {

    @Command(name = "reports", rank = RankType.MOD)
    public void reports(BukkitCommandContext context) {
        new ReportMenu(context.getPlayer()).handle();
    }

    private ItemStack createCustomSkull(String texture, String name, int amount) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, amount, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        if (name != null && !name.isEmpty()) {
            meta.setDisplayName(name);
        }

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", texture));

        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        skull.setItemMeta(meta);
        return skull;
    }

    @Command(name = "gamemode", aliases = {"gm"}, rank = RankType.HELPER)
    public void gameMode(BukkitCommandContext context) {
        Player player = context.getPlayer();

        String[] args = context.getArgs();

        if (args.length == 0) {
            player.sendMessage("§cUso /" + context.getLabel() + " (modo).");
            return;
        }

        GameMode mode = getGameModeByString(args[0]);

        if (mode == null) {
            player.sendMessage("§cO modo de jogo solicitado não foi encontrado.");
            return;
        }

        String modeName = getGameModeName(mode);

        if (args.length == 2) {
            Player target = context.getPlayer(args[1]);

            if (target == null) {
                player.sendMessage(TARGET_NOT_FOUND);
                return;
            }

            if (target.equals(player)) {
                player.sendMessage(SAME_PLAYER);
                return;
            }

            if (target.getGameMode() == mode) {
                player.sendMessage("§cO jogador " + target.getName() + " já está no " + modeName + ".");
                return;
            }

            target.setGameMode(mode);
            player.sendMessage("§eVocê alterou o modo de jogo de §6" + target.getName() + "§e para §6" + modeName + "§e.");

            log(context.getSender(), player.getName() + " atualizou o modo de jogo de " + target.getName() + " para " + modeName);
            return;
        }

        if (player.getGameMode() == mode) {
            player.sendMessage("§cO modo de jogo solicitado já foi selecionado.");
            return;
        }

        player.setGameMode(mode);
        player.sendMessage("§eVocê entrou no modo §6" + modeName + "§e.");

        log(context.getSender(), player.getName() + " entrou no modo " + modeName);
    }

    private GameMode getGameModeByString(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        // Tentar por nome (SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR)
        try {
            return GameMode.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Ignorar e tentar por alias
        }

        // Tentar por alias comum e valor numérico
        String lower = input.toLowerCase();
        if (lower.equals("0") || lower.equals("s") || lower.equals("survival") || lower.equals("sobrevivencia")) {
            return GameMode.SURVIVAL;
        } else if (lower.equals("1") || lower.equals("c") || lower.equals("creative") || lower.equals("criativo")) {
            return GameMode.CREATIVE;
        } else if (lower.equals("2") || lower.equals("a") || lower.equals("adventure") || lower.equals("aventura")) {
            return GameMode.ADVENTURE;
        } else if (lower.equals("3") || lower.equals("sp") || lower.equals("spectator") || lower.equals("espectador")) {
            return GameMode.SPECTATOR;
        }

        return null;
    }

    private String getGameModeName(GameMode mode) {
        switch (mode) {
            case SURVIVAL:
                return "Sobrevivência";
            case CREATIVE:
                return "Criativo";
            case ADVENTURE:
                return "Aventura";
            case SPECTATOR:
                return "Espectador";
            default:
                return mode.name();
        }
    }

    @Command(name = "vanish", aliases = {"admin", "v"}, rank = RankType.HELPER)
    public void vanish(BukkitCommandContext context) {
        Vanish.handle(context.getPlayer());
    }

    @Command(name = "chat", rank = RankType.MOD, onlyPlayer = false)
    public void chat(BukkitCommandContext context) {
        String label = "/" + context.getLabel();

        context.getSender().send("§eComandos de chat:",
                "§e* " + label + " stats §7- Ativar/desativar chat");
    }

    @Command(name = "chat.stats", rank = RankType.MOD, onlyPlayer = false)
    public void chatStats(BukkitCommandContext context) {
        ServerOptions.CHAT_ENABLED = !ServerOptions.CHAT_ENABLED;

        Core.getAccountController().send(ServerOptions.CHAT_ENABLED ? "§aO chat foi ativado." : "§cO chat foi desativado.");
    }

    @Command(name = "build", rank = RankType.MODPLUS)
    public void build(BukkitCommandContext context) {
        Account account = context.getAccount();

        Toggle toggle = account.getToggle();

        toggle.setAllowBuild(!toggle.isAllowBuild());
        account.setToggle(toggle);

        account.send(toggle.isAllowBuild() ? "§aO seu modo de construção foi ativado." : "§cO seu modo de construção foi desativado.");
    }

    @Command(name = "teleport", aliases = {"tp"}, rank = RankType.HELPER)
    public void teleport(BukkitCommandContext context) {
        Player player = context.getPlayer();

        String[] args = context.getArgs();

        if (args.length == 0) {
            player.sendMessage("§cUtilize /" + context.getLabel() + " [jogador].");
            return;
        }

        Player target = context.getPlayer(args[0]);

        if (target == null) {
            player.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        player.teleport(target);
    }

    @Command(name = "give", rank = RankType.HELPER, onlyPlayer = false)
    public void give(BukkitCommandContext context) {
        String[] args = context.getArgs();

        if (args.length < 2) {
            context.getSender().send("§cUso: /" + context.getLabel() + " [jogador] (item) [quantidade] [jogador_skull]");
            context.getSender().send("§cExemplo: /" + context.getLabel() + " Notch diamond 64");
            context.getSender().send("§cExemplo: /" + context.getLabel() + " Notch skull Notch");
            context.getSender().send("§ePara cabeças customizadas use: §6/giveskull");
            return;
        }

        Player target = context.getPlayer(args[0]);

        if (target == null) {
            context.getSender().send(TARGET_NOT_FOUND);
            return;
        }

        String itemName = args[1].toUpperCase();
        int amount = 1;

        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 1 || amount > 64) {
                    context.getSender().send("§cA quantidade deve estar entre 1 e 64.");
                    return;
                }
            } catch (NumberFormatException e) {
                context.getSender().send("§cQuantidade inválida.");
                return;
            }
        }

        ItemStack item;

        // Suporte para cabeças de jogador
        if (itemName.equals("SKULL") || itemName.equals("PLAYER_HEAD") || itemName.equals("SKULL_ITEM")) {
            item = new ItemStack(Material.SKULL_ITEM, amount, (short) 3);
            
            if (args.length >= 4) {
                String skullOwner = args[3];
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                meta.setOwner(skullOwner);
                item.setItemMeta(meta);
                
                target.getInventory().addItem(item);
                context.getSender().send("§eVocê deu §6" + amount + "x cabeça de " + skullOwner + "§e para §6" + target.getName() + "§e.");
                
                target.sendMessage("§eVocê recebeu §6" + amount + "x cabeça de " + skullOwner + "§e.");
                
                log(context.getSender(), context.getSender().getName() + " deu " + amount + "x cabeça de " + skullOwner + " para " + target.getName());
                return;
            } else {
                target.getInventory().addItem(item);
                context.getSender().send("§eVocê deu §6" + amount + "x cabeça§e para §6" + target.getName() + "§e.");
                
                target.sendMessage("§eVocê recebeu §6" + amount + "x cabeça§e.");
                
                log(context.getSender(), context.getSender().getName() + " deu " + amount + "x cabeça para " + target.getName());
                return;
            }
        }

        // Itens normais
        Material material = Material.getMaterial(itemName);

        if (material == null) {
            context.getSender().send("§cO item '" + itemName + "' não foi encontrado.");
            return;
        }

        item = new ItemStack(material, amount);
        target.getInventory().addItem(item);

        String materialName = material.name().toLowerCase().replace("_", " ");
        context.getSender().send("§eVocê deu §6" + amount + "x " + materialName + "§e para §6" + target.getName() + "§e.");

        target.sendMessage("§eVocê recebeu §6" + amount + "x " + materialName + "§e.");

        log(context.getSender(), context.getSender().getName() + " deu " + amount + "x " + materialName + " para " + target.getName());
    }

    @Command(name = "giveskull", aliases = {"gskull"}, rank = RankType.HELPER, onlyPlayer = false)
    public void giveSkull(BukkitCommandContext context) {
        String[] args = context.getArgs();

        if (args.length < 2) {
            context.getSender().send("§cUso: /" + context.getLabel() + " [jogador] [textura_base64] [quantidade] [nome].");
            return;
        }

        Player target = context.getPlayer(args[0]);

        if (target == null) {
            context.getSender().send(TARGET_NOT_FOUND);
            return;
        }

        String texture = args[1];
        int amount = 1;
        String skullName = null;

        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 1 || amount > 64) {
                    context.getSender().send("§cA quantidade deve estar entre 1 e 64.");
                    return;
                }
            } catch (NumberFormatException e) {
                context.getSender().send("§cQuantidade inválida.");
                return;
            }
        }

        if (args.length >= 4) {
            StringBuilder nameBuilder = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                nameBuilder.append(args[i]).append(" ");
            }
            skullName = nameBuilder.toString().trim().replace("&", "§");
        }

        try {
            ItemStack skull = createCustomSkull(texture, skullName, amount);
            target.getInventory().addItem(skull);

            String skullDisplayName = skullName != null ? skullName : "cabeça customizada";
            context.getSender().send("§eVocê deu §6" + amount + "x " + skullDisplayName + "§e para §6" + target.getName() + "§e.");

            target.sendMessage("§eVocê recebeu §6" + amount + "x " + skullDisplayName + "§e.");

            log(context.getSender(), context.getSender().getName() + " deu " + amount + "x cabeça customizada para " + target.getName());
        } catch (Exception e) {
            context.getSender().send("§cErro ao criar a cabeça customizada. Verifique se a textura está correta.");
            e.printStackTrace();
        }
    }

    @Command(name = "invsee", aliases = {"inventorysee", "inv"}, rank = RankType.HELPER)
    public void inventorySee(BukkitCommandContext context) {
        Player player = context.getPlayer();

        String[] args = context.getArgs();

        if (args.length == 0) {
            player.sendMessage("§cUso: /" + context.getLabel() + " [jogador].");
            return;
        }

        Player target = context.getPlayer(args[0]);

        if (target == null) {
            player.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        if (player.equals(target)) {
            player.sendMessage(SAME_PLAYER);
            return;
        }

        player.openInventory(target.getInventory());

        log(context.getSender(), player.getName() + " abriu o inventário de " + target.getName());
    }
}
