package com.minecraft.lobby.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.core.command.CommandInheritor;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.menu.patch.PatchLogStorage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class AddPatchLogCommand implements CommandInheritor {

    @Command(name = "addpatchlog", aliases = {"addpatch"}, rank = RankType.CHEFE)
    public void addpatchlog(BukkitCommandContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            context.getPlayer().sendMessage("§cApenas jogadores podem usar este comando.");
            return;
        }

        Account account = context.getAccount();
        if (!account.hasRank(RankType.CHEFE)) {
            player.sendMessage("§cApenas Chefes podem usar este comando.");
            return;
        }

        giveBookAndWait(player);
    }

    private void giveBookAndWait(Player player) {
        player.sendMessage("§e§l=== NOVA PATCH LOG ===");
        player.sendMessage("§7Um livro foi dado em sua mão.");
        player.sendMessage("§7§nEscreva sua patch log no livro§7§n.");
        player.sendMessage("§7Clique em §a§lEDIT BOOK§f para adicionar mais linhas.");
        player.sendMessage("§7Clique em §a§lSIGN BOOK§f quando terminar.");
        player.sendMessage("§7§oO comando detectará automaticamente quando assinar.");

        ItemStack book = new ItemStack(Material.BOOK_AND_QUILL);
        player.getInventory().setItemInHand(book);

        new BukkitRunnable() {
            private List<String> patchContent = new ArrayList<>();
            private boolean waitingForDate = false;
            private int ticks = 0;
            
            @Override
            public void run() {
                ticks++;
                if (ticks > 1200 || !player.isOnline()) {
                    cancel();
                    return;
                }

                ItemStack item = player.getInventory().getItemInHand();
                if (item == null) {
                    cancel();
                    return;
                }

                if (item.getType() == Material.WRITTEN_BOOK) {
                    BookMeta meta = (BookMeta) item.getItemMeta();
                    List<String> pages = meta.getPages();
                    
                    if (pages != null && !pages.isEmpty() && pages.get(0) != null && !pages.get(0).isEmpty()) {
                        if (!waitingForDate) {
                            patchContent = new ArrayList<>(pages);
                            patchContent.removeIf(p -> p == null || p.trim().isEmpty());
                            waitingForDate = true;
                            player.sendMessage("§a§lPatch log salva! Agora §nclique em EDIT BOOK§n§a§l e escreva a DATA§n (ex: 18/04/2026)§n§a§l, depois §nSIGN BOOK§n§a§l.");
                        } else {
                            String date = pages.get(0).trim();
                            PatchLogStorage.getInstance().addPatchLog(new PatchLogStorage.PatchLog(date, patchContent));
                            player.sendMessage("§a§lPATCH LOG ADICIONADA!");
                            player.sendMessage("§7Data: §e" + date);
                            player.sendMessage("§7Linhas: §e" + patchContent.size());
                        }
                        cancel();
                    }
                }
            }
        }.runTaskTimer(Lobby.getInstance(), 0L, 1L);
    }
}