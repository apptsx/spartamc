package com.minecraft.lobby.architect.list.hungergames;

import com.minecraft.core.Core;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.core.bukkit.api.npc.type.server.NpcServer;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.member.list.hungergames.HungerGamesMember;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import com.minecraft.lobby.architect.Architect;
import com.minecraft.lobby.architect.list.hungergames.leaderboard.HGKillsLeaderboard;
import com.minecraft.lobby.architect.list.hungergames.leaderboard.HGWinsLeaderboard;
import com.minecraft.lobby.architect.list.hungergames.leaderboard.HGWinstreakLeaderboard;
import com.minecraft.lobby.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.Arrays;

public class HungerGamesArchitect extends Architect {

    public HungerGamesArchitect() {
        super(ServerType.HUB_HUNGERGAMES);
    }

    @Override
    public void join(Player player) {
        HungerGamesMember member = Core.getHungerGamesData().of(player.getUniqueId(), true);

        if (member == null)
            member = Core.getHungerGamesData().save(new HungerGamesMember(player.getUniqueId(), player.getName()));

        Core.getMemberController().save(member);
        Core.getHungerGamesData().cancelExpiration(player.getUniqueId());

        User user = (User) User.of(player.getUniqueId());
        super.join(player);

        Core.getPlatform().runSync(() -> handleLeaderboards(user), 20L);
    }

    @Override
    public void quit(Player player) {
        super.quit(player);

        Core.getHungerGamesData().startExpiration(player.getUniqueId());
    }

    @Override
    public void handleSidebar(User user) {
        HungerGamesMember member = user.getMember(HungerGamesMember.class);

        Sidebar sidebar = user.getSidebar();

        sidebar.clear();
        sidebar.setTitle("§6§lHUNGER GAMES");
        sidebar.blankRow();

        sidebar.addRow("solo", "§eSolo:");
        sidebar.addRow("solo_wins", " Vitórias: §a" + Util.formatNumber(member.getStats(ArcadeCategory.HUNGERGAMES).getWins()));
        sidebar.addRow("solo_kills", " Kills: §a" + Util.formatNumber(member.getStats(ArcadeCategory.HUNGERGAMES).getKills()));
        sidebar.addRow("solo_games", " Jogos: §a" + Util.formatNumber(member.getStats(ArcadeCategory.HUNGERGAMES).getMatches()));

        sidebar.blankRow();
        sidebar.addRow("players", "Players: §b" + Util.formatNumber(Core.getServerData().getOnlinePlayers()));

        sidebar.blankRow();
        sidebar.addWebsiteRow();

        sidebar.display();
    }

    @Override
    public void handleHotbar(Player player) {
        super.handleHotbar(player);

        PlayerInventory inv = player.getInventory();
    }

    @Override
    public void handleEntities() {
        super.handleEntities();

        Location npcHGMIX = getLocation("npc_hgmix");
        Location npcSCRIM = getLocation("npc_scrim");
        Location npcEVENT = getLocation("npc_event");

        if (npcHGMIX != null) {
            handleServerNpc(com.minecraft.core.server.type.ServerType.HGMIX, npcHGMIX,
                    "ewogICJ0aW1lc3RhbXAiIDogMTYzNjE5NjA3ODk1NiwKICAicHJvZmlsZUlkIiA6ICI2NDU4Mjc0MjEyNDg0MDY0YTRkMDBlNDdjZWM4ZjcyZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaDNtMXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzJmMjNiMGQ1NjkyNWEwYTI5YmU0ZDUzMDExODAzODFjMGIxODdmZDRmNTJkOTAyOTA4ODY1MDQxNDdmY2VmZCIKICAgIH0KICB9Cn0=",
                    "Q+M5kRV6Vsl0tgFl08yeulUhJdVN1j8NpaH88w7sQ3qShBvgzwixn0HZN6+Uh9fTLXkmypE8LLXmXQDioXtDBlFfDlIxo1VjfszLsDAP6l2UHrCA67Qeg0N0zVn2NlqapoTIKuL4loa/VnY1BStTIdoKZBLpKMYwY0XBlFwnGIjGVlyLAGNINfrUpH53gf0ugBZi4MtQJzxQkGqQuTOzt30mPWMgR5lhqLj5J5emgiXXFxZQOXOXpkC2S3Q9zk9uPKM31+ekMnvNILlreEA1hV5rU1jlnT3ujVT+5EZqXjmd32QBrWNgm2i7MHc0P5Rd30urH0na7hoB8LzfrbNXj7rHnmxTROC6Ktpnz6S08RE9Z4RvdqA2Z4mlxFvT5pirXCWjEAY0goHXR2HBewTsep6WJNNNyCERH46cNOfQ/oGrFuYujZoyEGr71YacNcbOE84QEz2aIe+a1b937+JHg0Opd65ef/cVLEidgC4bmYyqUk673vEf6Xf6z59WwmFWMgpJedUx6JaWmdCtXKkCT/mxleMlJ72OoZ30xF5Avq0WfWBlaYW3ZNsDugHX5i+JzuNfh0VAyF4ReQDsWMeT3pTeaoZB46eVwDC7REeRkeNH00R4Xr81dda6LD9YTFrUlDHts3tGXPFWWZflEBlSmqcCvXfgBT0vme31DJeYld0=");
        }

        if (npcSCRIM != null) {
            handleServerNpc(com.minecraft.core.server.type.ServerType.CHAMPION, npcSCRIM,
                    "ewogICJ0aW1lc3RhbXAiIDogMTU4ODIzOTc2NTQ3MiwKICAicHJvZmlsZUlkIiA6ICI1MDAwZGJkNmVlMjY0ZDY4ODViM2M2YWFjOGEzNmE1YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJWaWN0aW1zX0ZyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFmMjFkMzllZTZiMzI4ODZhZDU4ZDU3MGU1MjdhNDAzMzYwZTI2OGE0NzVkN2E4NzE2MjdiNDVhZDE5ZmQ2YTYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                    "x/pV98Kid4/nt4VxwVvsqwSOesyeUAgRCyX8D34m/k6ANAriwtzkxc+i/RV/NC7IuMnJPyHD35NOLtYOh0Z+cRazN7qmQMuz/J/b/cwhiq93LcnHPwLCAg1rJJmS0NlRJzFZArip0XBozYEvQWnPKw+0VC5DAF79cD0maOvLTC165uT0UA88rizSasITVT3xHjZ7njVrYllj/x4M5hhfLMS4e/MSKgU+uceixnv3nQD9iCnNfVZS8921/zOi7HG65nm9BlbDTPYElclwD/PDXTD7Eylja6pHJkMrvSdCttXP6ejWbGmDE075Svsjw4xRejywfkptblJbh12ZavbuID1az653ExalNREB2YBzXm1VmUTkB2c30q+oG1vtw2wYoIRJwwOGBk5UEyjSGfdYsjfKemJm9sWIw6OGltFR6h4jjxqaHBi44Nzxn6SSRKrD3KO5H2MDQlcwP+s0n0FB0x2zL8pIoOdwQXTuwn+PntsFccL1pOIc5lyIQaZFZ+HRqANwRqlk8bTAKpyB0PiJyTvg5A6yCjM9AZbQxBskpfGOUCx6wijoQKtZ8AG3PGsYVwnREOKXpifda97BvyedB57mJHGpcWpS8T7BRWhnyPI0aGXCxY6T856njU+AKl1TfbB+IMHCn/gbDPp/Y9qqr/GSK4iosWHXNUo8s5aY5EA=");
        }

        if (npcEVENT != null) {
            handleServerNpc(com.minecraft.core.server.type.ServerType.EVENT, npcEVENT,
                    "ewogICJ0aW1lc3RhbXAiIDogMTYyNjc0NDc0NDA4OCwKICAicHJvZmlsZUlkIiA6ICI5MzI0N2IzMzllMTQ0MDBkYjk5Y2ViM2Y0NzA4ZTBhNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJBemFyb3dfIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2UxMDk2MTc4ZDlkY2JiNDZkOGUyYTJiNWMzMzUyYTEzYjY5NTE3Y2ZiM2ViZmFhODBlOTM0YjJmMmI2MDc0MTkiCiAgICB9CiAgfQp9",
                    "k7Gt5/W/tiu7C8gcfK6AE0CA3BYVMUAP7Hs268YjIm8r6smuT5/W2Hlu7aoi/nfcUcS5BhYBzOdqkmwOsYod56RkhgiKhZaBJ5KykHZI2peY4yz7vX/Ck8FyDwhblZ5bUg7MtTP7Cp2tgp2i2BG2QMtWc6wKkP59ynXd9OFc4RwldOagLzEqWQFPe/0jJ0+RICr/DGDK0mW16N6uB6+rCxwD2mi95/xpt2unZtjsrgFoc24tfR32IFI/XpthGM6kQXRqJC4QoLR/Vqk9DCRsySxgwKyRKe8rMXuQFmMH5anaHHPh365jhUs9t/GrZIc/z9ABRO490wpd6eqgEvGEOdDxYe+3FQO3vLqMjMlXR/msJXZMlhzETVr+Kei1ZoE6/RTOOaCjRUSYnm5P1YaLOmmSf16+e746JGUtnTuUDQRH9bOrC6JAvJjz32vglEP+JweYE1QrUA1joxYlm6bV2e8OHIYsySlT0uI0LEx25yB0CtJqlKPwgmdG2rOW/gtSEg3LrNubHwi4zyAEJ65Qbpcog6e+2riaJXjFdQh9XsEu70esq42+7TQ2UDFp4Xy9CACRCfAKM8fk/HUuM4JY6vyf3Ce3eI/MeNcEMbObtXGoXD/7CfwNFHUNJkwfzYcjENDXB7eYujFqY0coAgA6Zym3anxD2nCLHm4y48dDm5A=");
        }
    }

    protected void handleLeaderboards(User user) {
        Player player = user.getAccount().player();
        if (player == null || !player.isOnline()) {
            return;
        }

        org.bukkit.Location winsLoc = getLocation("leaderboard_wins");
        if (winsLoc != null) {
            HologramClient existing = BukkitCore.getManager().getHologram().getClient(player, "leaderboard_wins");
            if (existing != null) {
                BukkitCore.getManager().getHologram().removeClient(existing);
            }
            new HGWinsLeaderboard(player, winsLoc.add(0, 3, 0));
        }

        org.bukkit.Location killsLoc = getLocation("leaderboard_kills");
        if (killsLoc != null) {
            HologramClient existing = BukkitCore.getManager().getHologram().getClient(player, "leaderboard_kills");
            if (existing != null) {
                BukkitCore.getManager().getHologram().removeClient(existing);
            }
            new HGKillsLeaderboard(player, killsLoc.add(0, 3, 0));
        }

        org.bukkit.Location winstreakLoc = getLocation("leaderboard_winstreak");
        if (winstreakLoc != null) {
            HologramClient existing = BukkitCore.getManager().getHologram().getClient(player, "leaderboard_winstreak");
            if (existing != null) {
                BukkitCore.getManager().getHologram().removeClient(existing);
            }
            new HGWinstreakLeaderboard(player, winstreakLoc.add(0, 3, 0));
        }
    }

    @Override
    public void handleArcadeNpc(ArcadeCategory arcade, Location location, String value, String signature) {
        NpcServer npc = BukkitCore.getManager().getNpc().spawnServer(location, value, signature);

        npc.setContact(true);

        npc.display();

        HologramServer hologram = BukkitCore.getManager().getHologram().spawnServer(arcade.getId(), location);

        hologram.setText(Arrays.asList(
                "§6§l" + arcade.getName().toUpperCase(),
                "§e" + Util.formatNumber(arcade.getPlayingNow()) + " jogando."
        ));
    }

    @Override
    public Vector getSlimeJump(Vector direction) {
        return direction.multiply(1.8).setY(0.5);
    }
}
