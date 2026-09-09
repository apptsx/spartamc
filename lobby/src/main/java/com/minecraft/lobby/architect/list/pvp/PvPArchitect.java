package com.minecraft.lobby.architect.list.pvp;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.member.list.pvp.PvPMember;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.DateUtil;
import com.minecraft.lobby.architect.Architect;
import com.minecraft.lobby.user.User;
import com.minecraft.core.account.context.objects.medal.Medal;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;

public class PvPArchitect extends Architect {

    public PvPArchitect() {
        super(ServerType.HUB_PVP);
    }

    @Override
    public void join(Player player) {
        UUID id = player.getUniqueId();

        PvPMember member = Core.getPvpData().of(id, true);

        if (member == null)
            member = Core.getPvpData().save(new PvPMember(id, player.getName()));

        Core.getMemberController().save(member);
        Core.getPvpData().cancelExpiration(id);

        User user = (User) User.of(player.getUniqueId());
        super.join(player);
        
        // Atualizar sidebar após salvar o membro para garantir que os dados estejam disponíveis
        if (user != null) {
            handleSidebar(user);
        }
    }

    @Override
    public void quit(Player player) {
        super.quit(player);

        Core.getPvpData().startExpiration(player.getUniqueId());
    }

    @Override
    public void handleSidebar(User user) {
        PvPMember member = user.getMember(PvPMember.class);

        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle("§b§lPVP");

        sidebar.addRow("date", "§7" + DateUtil.getCurrentDate() + " §8" + getId());
        sidebar.blankRow();

        sidebar.addRow("text_one", "§7Bem vindo(a)!");
        sidebar.addRow("text_two", "§7Selecione um modo!");
        sidebar.blankRow();
        sidebar.addRow("lobby", "Sala: §7#" + Core.getServerId());
        sidebar.addRow("players", "Players: §a" + Util.formatNumber(Core.getServerData().getOnlinePlayers()));

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
        TagManager.updateTag(user.getAccount());
    }

    @Override
    public void handleEntities() {
        super.handleEntities();
        
        Core.getLogger().info("[PvPArchitect] Carregando NPCs no mundo '" + getWorld().getName() + "' com " + getLocations().size() + " locations configuradas.");
        Location statsLoc = getLocation("npc_stats");
        Location arenaLoc = getLocation("npc_arena");
        Location fpsLoc = getLocation("npc_fps");
        Location lavaLoc = getLocation("npc_lava");
        
        Core.getLogger().info("[PvPArchitect] Locations encontradas: stats=" + (statsLoc != null) +
                ", arena=" + (arenaLoc != null) + ", fps=" + (fpsLoc != null) + 
                ", lava=" + (lavaLoc != null));

        if (statsLoc != null) {
            handleStatsNpc(statsLoc);
        }

        if (arenaLoc != null) {
            handleMode(ArcadeCategory.PVP_ARENA, arenaLoc,
                    "ewogICJ0aW1lc3RhbXAiIDogMTY4NzEzMTcwODY1OCwKICAicHJvZmlsZUlkIiA6ICI2NDU4Mjc0MjEyNDg0MDY0YTRkMDBlNDdjZWM4ZjcyZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaDNtMXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzY1N2VmNmU4NjNhZmViYjVjZmM5YmQ0YTFlYWMzZDdmMmVlNmVlZjM1YzE4ZWQyZjQ3MmMxMDE3Y2YwOGNkZCIKICAgIH0KICB9Cn0=",
                    "RKh+hREnrO/wW1erBWI/+Ws9+u/3g6dP2iWLxewkFQ7vPchVlYe6XA+NZpORu0eVCVF/2tN9fNQnWWKqO746gyWXKe67LXtbZZGE7JOObIZWr3toTeQxrI87SmPU32jsgSEEIfeMhmaFxzaeSUdJMPcdhsRo9sf4C4ZqGBGVaEaHUwPAyxTi1j3ix6EBQRORjQv1fhHECSjRXr/d3Go5bUWbgw0Kr6+XNJrTsTNnHPPglAamH2OQCfcr2CUKa4dxVBUwXdntDu6ef84fU1RWed7ASSUi99EBMgPmkxhEw4IcI7+mRlFXtk3iivclU2Cz/NRWkZg29ucsx1EqAEuVCGPC+E1bst2Y1H6uwZw5/8CIqwKfqdkV7k0w9mTPj7rMJxksmH3MZyxqWxFnrlO5OA/D9i5H6bEryUxpeG2pygLI/uzUJAfZnOBi8lMi7p+OUKyySA8OwBlHDcWruzXzC3KwswlZiT4jnrZllfCQydzM1si5CI5n4DOHltlMzt90tzn0C6cKR24FhiWhBNDoVxs1CSjiiSL0eUQPhaDPndDA8ww0zi/MG4HOjhXq0/QKs9F7GE+HW3Woj1PgR1JZCIqlnG6T9bw313pVyvcOz4hlSpL+HKDAji8WbMP1qSDtXFfwfbK1f5osDNJpIAU3GscQzW+yWtuI4OOdpXhzOjc=");
        }

        if (fpsLoc != null) {
            handleMode(ArcadeCategory.PVP_FPS, fpsLoc,
                    "ewogICJ0aW1lc3RhbXAiIDogMTcyOTA0MTc4MDI3MiwKICAicHJvZmlsZUlkIiA6ICIxNTUyNmU1OGZhOWE0NjBmODhhNmZhNjk1M2RlNjgzNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJQaWVkcml0YTE3IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzEyYmI5MDI5ZWE2ZGVhYjdkYjMwMTc1NzA5MDRjNTk0OTUwZjVlZDZkOGIxOGY5Mjk2NmZlYzgwYjhmMTFjN2MiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                    "HsLFmnRHr+fxHPO5siAMwL6wLF5S8rVDqk5/9sGaej92be0dUTgMUIxbCRhQBT1//4nKSiolc7sap7rlPPEeOiHgU8vOvnk1PeZ5N3d6zPPcKhiwHIIUsvQ67v3uQ/Do5C86JI7HpaIX3BXImk5ng0grnjR3gr9m6C88CRaePa8TL46pUoh+6EhFnzO4LyCrOFpKBFsx+NqueITfN1YtmB7JIit7LIVnOG3FVDbkv0f1i0OmHRWcBV9qlJuW3eJgFr8JQKnWHPL94te3IXizgVf84lzdPl4RaMayu0sy+RwL9Y/xZ2cCH8UoRYtvab8bKhFdFxb1q8ottpHUb12ZSf0roDmjMn3kJVkAN4LrwwWmNNI+weYEa7Bo8wSB7lKSYc088hDgkdib+7zbC79Z5qSJUmJ8s6cyrvnA15eqv7O8vVAeq8RWR1B9+/CWQFTO0ITlmZJCxxjpr9B3d2oGB/edJBxDSEbQzX1u1/UuI7jUSUBiVT10vZNQhE9+SHtT9KU8P9JPQ28c4mRm9JrzbXhBk2k6Fy5RyjV7VFw2Bn6J6dZNLFKc0iCZbC1muJENGEgmDzuF1ufV3jzdMjHpoOoAUDroubCS6wAXfvJ7DzepeaXD/jO+3RZqcmnjTr9MycIzrS6Puy85eU750crH3SowjWwSBbtZvksU5xRVrMw=");
        }

        if (lavaLoc != null) {
            handleMode(ArcadeCategory.PVP_LAVA, lavaLoc,
                    "ewogICJ0aW1lc3RhbXAiIDogMTYxNzEzODU4NTcyNSwKICAicHJvZmlsZUlkIiA6ICI0NWYzNjA5OGY0YmU0NmFiODkwMDc0ZmFkZjMxMDE3MyIsCiAgInByb2ZpbGVOYW1lIiA6ICJEcXJpdWgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGFlMjZlZjhmODFiMWMxOWMzYzM5ODc5NzliZDE5NzJlN2E1NjE0MWFlZDhiZmE2MTZjYTM0ZjExM2Y4ZDUwMCIKICAgIH0KICB9Cn0=",
                    "PjJDMtDRHlsXjYHiCq6veCUMgB/2Q9/bzt+8RqKubwxRF4mJwhvceufbu8HTp49No91fcqEN0apiMdC4KcXLUE8Wpv837Re60L0kT3TyDSqMEhuKh0VmoT/or4gOXSVMryfLl1qXLb+Fztzh46Z0tRTsrPDwU3ZRrtwo7VCFUGLR+h4VLAhSn5J7Zc02yPclxAm2ItIzaBPuo0jgY48qb+is8gEerLEScR5O7ONSMlR/ANKS+I2WuuWO5QvF670cBA8WHu1v2Pe90KCgkvzGBzFbNlOUJ1xlULO10zkCzmAizvlSvMDDs1hAe6d2VnsNnNTox6WoGf5K7Tm9hbMcwi8PYXkDSZ1LuDzCh7kUmzLq+1olVPOI2g3SONv5qqZa79oqYbgsj3I7ngSeUhMmeywsVvRYATxVoIdDYIoWyBna91SONkcROuzGjUzVr256FgPSgH0zJwaK8R6QtadlGQ7TCVCug8A9xD74tOwLoPuxbcjOcqjawZyWG7UMnz+KAIqAcMLNW/XgkyIPP0cI1ySYvBHl0N89vqNBptmy9UJ9mPBbazi94jVRND8+i0yO5cbYDeJ5TYgqttQOhER32I45NXK17MTETrMomuEyYEtjg5GTa8hroAmfPHs+c4Pxwh98mhp8AoI1cjTe+m3gVkz4Qxi53DKGltR7JoW9aQg=");
        }
    }

    public void handleMode(ArcadeCategory arcade, Location location, String value, String signature) {
        handleArcadeNpc(arcade, location, (player, action) -> {
            Account account = Core.getAccountController().of(player.getUniqueId());

            if (account != null) {
                account.redirect(ArcadeRouteContext.builder()
                        .arcade(arcade)
                        .build());
            }

        }, value, signature);
    }

    @Override
    public void chat(Player player, String message) {
        User user = (User) User.of(player.getUniqueId());

        Account account = user.getAccount();

        PvPMember member = user.getMember(PvPMember.class);

        if (member == null) {
            player.sendMessage("§cNão foi possível recolher os seus dados. Contate um administrador!");
            return;
        }

        player.getWorld().getPlayers().forEach(worldPlayer -> {
            Account target = Core.getAccountController().of(worldPlayer.getUniqueId());
            if (target == null) return;
            
            StringBuilder builder = new StringBuilder();

            if (account.getMedal() != null && !account.getMedal().equals(Medal.NONE)) {
                builder.append(account.getMedal().getColoredSymbol()).append(" ");
            }
            
            builder.append(account.getTag().getByPrefix(target.getTagPrefix()))
                   .append(account.getNickname())
                   .append(": ")
                   .append(account.isAllowColoredChat() ? "§f" + Util.color(message) : "§7" + message);

            target.send(TextComponent.fromLegacyText(builder.toString()));
        });
    }

    @Override
    public Vector getSlimeJump(Vector direction) {
        return direction.multiply(0.8).setY(0.4);
    }
}
