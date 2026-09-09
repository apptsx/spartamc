package com.minecraft.lobby.architect.list.duels;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.item.Item;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.manager.list.TagManager;
import com.minecraft.core.bukkit.menu.server.arcade.mode.duels.DuelsMenu;
import com.minecraft.core.member.list.duels.DuelMember;
import com.minecraft.core.server.type.ServerType;
import com.minecraft.core.util.Util;
import com.minecraft.core.util.list.DateUtil;
import com.minecraft.lobby.Lobby;
import com.minecraft.lobby.architect.Architect;
import com.minecraft.lobby.user.User;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public class DuelsArchitect extends Architect {

    public DuelsArchitect() {
        super(ServerType.HUB_DUELS);
    }

    @Override
    public void load() {
        super.load();

        Lobby.getInstance().getLogger().info("Iniciando sistema de colecionáveis...");
        Core.getCollectibleController().handle(Lobby.getInstance(), "com.minecraft.core.api.collectible");
        Lobby.getInstance().getLogger().info("Sistema de colecionáveis inicializado com sucesso!");
    }

    @Override
    public void join(Player player) {
        UUID id = player.getUniqueId();

        DuelMember member = Core.getDuelsData().of(id, true);

        if (member == null)
            member = Core.getDuelsData().save(new DuelMember(id, player.getName()));

        Core.getMemberController().save(member);
        Core.getDuelsData().cancelExpiration(id);

        User user = (User) User.of(player.getUniqueId());
        super.join(player);
    }

    @Override
    public void quit(Player player) {
        super.quit(player);

        Core.getDuelsData().startExpiration(player.getUniqueId());
    }

    @Override
    public void handleSidebar(User user) {
        DuelMember member = user.getMember(DuelMember.class);

        if (member == null) {
            Core.getLogger().warning("[DuelsArchitect] DuelMember é null para " + user.getAccount().getNickname());
            return;
        }

        Sidebar sidebar = user.getSidebar();

        if (sidebar == null) {
            Core.getLogger().warning("[DuelsArchitect] Sidebar é null para " + user.getAccount().getNickname());
            return;
        }

        sidebar.clear();
        sidebar.setTitle("§b§lPRACTICE");

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
    public void handleHotbar(Player player) {
        super.handleHotbar(player);

        PlayerInventory inv = player.getInventory();

        inv.setItem(2, Item.of(Material.EMERALD, "§aMenu do Practice")
                .interact(event -> new DuelsMenu(event.getPlayer()).handle()));
    }

    @Override
    public void handleEntities() {
        List<Location> locations = getLocationsByKey("npc_simulator", "npc_soup", "npc_sumo", "npc_gladiator", "npc_uhc",
                "npc_fireball", "npc_boxing", "npc_chamber", "npc_thebridge", "npc_nodebuff", "npc_skywars", "npc_stats");

        Core.getLogger().info("[DuelsArchitect] Encontradas " + locations.size() + " locations. Esperadas: 12");

        if (locations.size() < 12) {
            Core.getLogger().warning("[DuelsArchitect] Aviso: Apenas " + locations.size() + " locations encontradas. Alguns NPCs não serão spawnados.");
        }

        if (locations.size() > 0) {
            handleArcadeNpc(ArcadeCategory.DUELS_SIMULATOR, locations.get(0),
                "ewogICJ0aW1lc3RhbXAiIDogMTY4NzEzMTcwODY1OCwKICAicHJvZmlsZUlkIiA6ICI2NDU4Mjc0MjEyNDg0MDY0YTRkMDBlNDdjZWM4ZjcyZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaDNtMXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzY1N2VmNmU4NjNhZmViYjVjZmM5YmQ0YTFlYWMzZDdmMmVlNmVlZjM1YzE4ZWQyZjQ3MmMxMDE3Y2YwOGNkZCIKICAgIH0KICB9Cn0=",
                "RKh+hREnrO/wW1erBWI/+Ws9+u/3g6dP2iWLxewkFQ7vPchVlYe6XA+NZpORu0eVCVF/2tN9fNQnWWKqO746gyWXKe67LXtbZZGE7JOObIZWr3toTeQxrI87SmPU32jsgSEEIfeMhmaFxzaeSUdJMPcdhsRo9sf4C4ZqGBGVaEaHUwPAyxTi1j3ix6EBQRORjQv1fhHECSjRXr/d3Go5bUWbgw0Kr6+XNJrTsTNnHPPglAamH2OQCfcr2CUKa4dxVBUwXdntDu6ef84fU1RWed7ASSUi99EBMgPmkxhEw4IcI7+mRlFXtk3iivclU2Cz/NRWkZg29ucsx1EqAEuVCGPC+E1bst2Y1H6uwZw5/8CIqwKfqdkV7k0w9mTPj7rMJxksmH3MZyxqWxFnrlO5OA/D9i5H6bEryUxpeG2pygLI/uzUJAfZnOBi8lMi7p+OUKyySA8OwBlHDcWruzXzC3KwswlZiT4jnrZllfCQydzM1si5CI5n4DOHltlMzt90tzn0C6cKR24FhiWhBNDoVxs1CSjiiSL0eUQPhaDPndDA8ww0zi/MG4HOjhXq0/QKs9F7GE+HW3Woj1PgR1JZCIqlnG6T9bw313pVyvcOz4hlSpL+HKDAji8WbMP1qSDtXFfwfbK1f5osDNJpIAU3GscQzW+yWtuI4OOdpXhzOjc=");
        }

        if (locations.size() > 1) {
            handleArcadeNpc(ArcadeCategory.DUELS_SOUP, locations.get(1),
                "eyJ0aW1lc3RhbXAiOjE1ODY2MDkyMDU4MjcsInByb2ZpbGVJZCI6ImIwZDRiMjhiYzFkNzQ4ODlhZjBlODY2MWNlZTk2YWFiIiwicHJvZmlsZU5hbWUiOiJNaW5lU2tpbl9vcmciLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdlMDZjZmMzZDQzZGIxMmRhMmRiYjYwMjkzZjA0ODIxNDk0YjAwMjdiMjc3MTU0NzNmMWMxNGRiZjUzMGU4YTkifX19",
                "QcwpXqAjlurqUHnm+QKs2aGmW/Fb7gcSQFCA8KrtENUp4E5ftBhDhHaAZAxIMb53KElRc/4kSpL2C8FB139EOOnv1kLIOumz/HDFYfThZp4KxzU03ffAaZ58c4q4ja1T4ackVHqAurWvneGjgGpkpy1QDG+rCwD28g1vloppjd1OYpgIQ7Er6NO3psDuQRrWR4Ywdi1ZqC1ipQbvQgK/R8GWpX7yBmDbrPfh3PKgyPvT/aQBh1klmGcfPb6GZCFpInYZJSvW0izFop+mlpBIXnUs6K7M8mzuQRlcC8FQrgrXrSJW4co9anHpT5xBoC3NbWv2O0iV1qmVQrFHVbzOJvEZTYT1lc3G1TnXl63cLUvebgKfWMlRphQe9efqvKRHVnz+xYorINOefs7cVVao7ysn/6fy7mtKOIgi6eoOUPX7A7lUg5w3hMGggww3qNQSDn3HE9klF967pAovuSvAc8IU/xEEsbvB9KgB19QAPRLdzWXPiC1oQlpNzymlJGhLANjT3tyKGa1Y14kmuZza3d4v07ZMX7gj33jHbdLUhspCng4e5E1aK0SLIXAyh38KwkIUsowljh9vzkgJ8OU85q4OuMYBBh2pegnXoD9qRn3DcihCqvfEawsJ8WGh3fhDkrhe2RyLJZLOAcOAPOjgF1QMlv/J/RKjdi4DQP+gRs8=");
        }

        if (locations.size() > 2) {
            handleArcadeNpc(ArcadeCategory.DUELS_SUMO, locations.get(2),
                "ewogICJ0aW1lc3RhbXAiIDogMTYwMDI5MDQ4NTkzOSwKICAicHJvZmlsZUlkIiA6ICIwNjEzY2I1Y2QxYjg0M2JjYjI4OTk1NWU4N2QzMGEyYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJicmVhZGxvYWZzcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iN2U3YTQ2NGQzOGQyNGJjYWY5MDMwM2RhZDdkOGQ5YTQ1NTJlN2E4ZDJhOWQyOGU0M2U0Y2I4NGZiOWQ1M2MxIgogICAgfQogIH0KfQ==",
                "cBQP562nqVttcD97H1sFI081pP6vKlU0w8cKh2g8+1JAaqOU62F5dJMGx49qMvWPx5o8+EplCQWboR+J6Xe6rwFLakb1o1DHaW7N6vMDssr0e9bIHI8LXhsHG07GdiksxcGCpwFKpezZYsMYyGLoGuspGRfehOrGgThGhHrwJlZdLl/o0gqTqUXrEvq+Ys3bycutf1qQFnKox8urGX2MipAPA68G8Nwy6RAAq4lEp6vcyTbN06kbvdowqFv1FLxjcXKuCA3DWmFn6Ypcd7ea0h4oVPxuZlVg5hxE0rzv1DjJ9M+ZRBJtT7nTiJ1FhDXxf1vsFM5YL66wrQg9UOBfObvG4tTcvMeJ8gSzOWdFxZ+RUmGqJQuTDsIxzXsNGbV9BlRR1LCM5LaG9WP15SGAOi7nDCImhIlj4CLw45cyfmYd93hTq2P1FffpApBylyr9mJF5kMWtTNr5Bb5ad8bNtHGwtiLyzdGnS26FkclWijCGG25TlsAdCj+cSDwj8jJz/qeHq5CFpJYobrob9FaXmdBf2r5kxJ3Q0dmH2VU2B9aIytiYBG5eAw2SyokqVP0KMDWyNi6lmeEOrtKjC7PbFEjRdTNFEiy5FTbcUMtkbODApY4jQeCCdWsrKyX2opfYpRTfCJ7/T5D/pvX3eJZcu4nF9nQZcXNr49CHSA6VkBs=");
        }

        if (locations.size() > 3) {
            handleArcadeNpc(ArcadeCategory.DUELS_GLADIATOR, locations.get(3),
                "ewogICJ0aW1lc3RhbXAiIDogMTYyMTUyNjk2NTc0MCwKICAicHJvZmlsZUlkIiA6ICI5MThhMDI5NTU5ZGQ0Y2U2YjE2ZjdhNWQ1M2VmYjQxMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJCZWV2ZWxvcGVyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzQ1NTI2YTc0NDJmN2EzZWRlYzYzZjg3MGFkNDUxY2I1OTQwNWNkZTYzZWYzM2NlOWI1YjZkYWJmNDlkMmMzNDYiCiAgICB9CiAgfQp9",
                "Jkj+oR6pUO4Tr3vTJQC+cuG1kjUWPY53XpI6/eCihRRAikVZObpSG9EWmbnxDzrkqb+/ShIu65mprIF+QB/AahotB4lPwR3j8WPxjRJtnkDPo1jYwwaM43jSrf9vIS7u2hXXSQbwOKa5/8yVz6AK+SnEwZ+C357WC9pxS7ozWz7T0DRhjKPlTAirx/+6gyl69XTQS6ljtvTEAl0DrODeEzFl02yrsxMEuTvA897TeYTdnvlMUzdk4Wg3cY6ZC4HSr60KcWGOwwfflBSM1ij0660wYtlKyrDJC72j7ZMl9RNkEJHmQLLA6PEsxOAw/qBfSZPPRCkOQ/cavC5OzwFQmj6hJSvXqEq0MXdxKVC6AIssBx2aa5b3iKKj10NU1YiPQkewguj1elXPJS1Zbe7UCoEPXtWkm1/c1aWGTpstEGkZlJktp7RsL1HutKV8NoHOTNGpKQp3o72SvG7r0bRUmTI0puyiIDrpnymjbn8TowknJybi+NCRLFp5Mb0CzU3ksK2p1hmzCy3l2XDRQGgOBgU2pdt4wSxefNpOmI+QywaO3GoV3q13t/qtG7+/o0bpA/wrJj9Bs8rgUT2qHYwvW0wcyo+2qGFfDed4Qp3VWsXAidvIsvQ0rp/YyXBCL0QUfxykEvXXpC9YrzZ4UzEAGcmkuSgUBZvagxUcGddcghk=");
        }

        if (locations.size() > 4) {
            handleArcadeNpc(ArcadeCategory.DUELS_UHC, locations.get(4),
                "ewogICJ0aW1lc3RhbXAiIDogMTYxNjk0NjkxMDkyMSwKICAicHJvZmlsZUlkIiA6ICIyNzc1MmQ2ZTUyYmM0MzVjYmNhOWQ5NzY1MjQ2YWNhNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJkZW1pbWVkIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzU2NmE4NzQ2MDE3M2FkZjA2N2NiMzU2YWUyMDBkMDMwNTA0Mzc4YzU1MmUzNDI4YjQ3NzRjNGMxMWE1OTljMjQiCiAgICB9CiAgfQp9",
                "L+ohbl85Yw0QGCSeAntiV+1G6Uh+fYP6/Uxa1n9HI/dG1sIuJUto1iQNxXDuxn3maclmXBHH26Ebp1jaKj5JVXiPrTN1qfsFZcePiUCY0PaaEbi2/FRZ710grMusSg3VP1aeKIXTSlPJrxMejx60MfzfSfbTfDaz441yMmWqKFrLqs0MQbhAgg3iK268dLioLq1dAMVNTzGbYph1wrrXLxN179vEARMtXh3ExCUGB8OOoGbIH9paDPhAiKLcJU/Nos/l/SfnimpspS3LGb/lYyvfLzVMZG7isnD8+U/tjC9DEwTdGOruA4+1Fg4kPGu3TzRb/sv5xw1xDBTBBym4BnfWx31PgGO6e+UbcR6+AsgZ9viceb9WErazd1ed/vQZCABZCdymLAR19cqVsBKMwh0ycZ2c8OGFjuQGENJkukthsvlMnFWMCXaHg0fbXYrQqIAIHm5Na4H0GeJT/kNF2ay3TKVG9QLgzCDBzt1guBTImDUYtYUrpNCZzSwV73NXZLV5HhpY42Qi/dos1oAktyRa06s7uTMQDfYZe/gVGB1EdCUwA4v0RsLf/Plq24RHUYB7CFd1aJ59C8SftjKXIKqQLj2KTcbHzni7YvjYA0RlLYAZfRT6gDZBYk3Er21r/d8TLzZ3OFjEe+j35VCbQPv0tLT8nIGUtR2pyMQzqfA=");
        }

        if (locations.size() > 5) {
            handleArcadeNpc(ArcadeCategory.DUELS_FIREBALL, locations.get(5),
                "ewogICJ0aW1lc3RhbXAiIDogMTcyMjA3NjQ2NjEzMCwKICAicHJvZmlsZUlkIiA6ICIzMzE5Y2ZkMTdiMDU0MTEzOTdiYTI4ZjlhMmJjMTg1NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJQYW5zaG9GZWlrIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2JlYWFkOTAyM2E2ZjU0MmVhMjA5YjA2MDk4ZGQ3OTBhMDZmY2YxOTdkN2QyYTVlMTc1NmRkNDFhMmU4MmQ5NzMiCiAgICB9CiAgfQp9",
                "Qtw837D7klEiHw1rxNor+G+C1UDKCFcwn1naFrCoQ9E4xNzc2q1QKKdlvOOtur6T0BOP2cV4dxGF3hXk2KAQb3of4vYvcD3EVhDWFT3FnDYzNq5yl0c9IfwHnh1fB4XuGIJyBDCqYWcxQX51Ng5CKhJfaTz2G7UaJp+ZIZfCSJ3jY9CeSvtkayWZoeHRA6TeQ9dz2gBlGEn+Ku68Z8cAZR1tUZ+X4fAsVyUTRn5Zd1Hqi/GLXYsNHBAUDZv2xcmgrO0D3h18J7eA1zRuXHYyIQ3OSg6AhVBvHBvGBbcFk242ecoOOsL8xhEMwwmvvK/ehm0EltJDg8SwBlb5NIgpATfr9rUXVGRT8BstNzCt2H2F/URMR6LrbaY1sHhCQFYtKROSSB41l5rm3Nsq7iLMf1Z8SFbg3rxd04/9jEBcNQ3zcyD4r0VCEsXcHnrNGASc411nR3BSgm9JWoLaNr8oh7XukatlMTkB+E3EGfEw57P6uXcZWIgPzEx407WS0R2HTSvbnL+j9XfsObq10+UzELW9i1dPP4A8R2PkgJXhPCmMgs69iT/UiVFPOlt0WwZrCq6bDi9JKkz+tFHu8gDoG4verXuKzvi0xhx6Z49vL4wKz+k0do8vOA/Th6znDnAQBgqYDGK7Ki0d99AtFz6eNn1abX2HIorPBwDFJwh6fyk=");
        }

        if (locations.size() > 6) {
            handleArcadeNpc(ArcadeCategory.DUELS_BOXING, locations.get(6),
                "ewogICJ0aW1lc3RhbXAiIDogMTY4MzkwMjMzODc5MywKICAicHJvZmlsZUlkIiA6ICJkMTJiOTk3ZWI2YTQ0ODQ5ODJmNDE1ZTI1NzFlNmY4NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJUd2lybGJlbGwiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjAzMjQ2YzYyNDYwYTc0NWE4ZDdiZmY0ZGE1YjZhMjIwZDFhZTU4MWRmZWU1N2QxYTVhZDM1Y2E5OWVlNDRjOCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                "a4SgcTSmm2ejuf3/qGAbss0zse97cf549LZwvHpRwJ0WebdqUcbxytTsoaZEeIvglGnrxuDxCv4twp2gSQrsYKZWkIM5wN6KYARF9k7c+sMUdkns4MyHCSqYjRH7POKUs4Nl578n8fVQzo1L+Kl0KLyBBuSItuqLf7eiElyyyZDfbOd6bsCkvfPpMlAWsi2E/YqDrUwGNPscRAhtSI27WSiI/gJYGMOcNBC7dZWgKaa8jFZ9KhLOrOzNf6iDtMWmzJs/H5Lry8/ucy0jJ7ZNidHlbyoh/D9rRMTD5r2k6/K1jJfTaTzBI29Nx9LPwmdptiEDQMvClIdX/i3URTOsd7vluVFndIxb2D0zwaK6mPZMbLgfWWjDOHThXpIJk2Xw9Dme8SoO2kju9KN72EoQHD41IEyseOIAQT3KVqpL0bGAxFI4AJbR7QbbfPF7d2FfB5PoFNNwvA6FKtyJdg4G0/pGGrYer7/wqw5wLUroM14SUKhVWigrin3mqZjp01iomL37ywnabshb/8J/+Td/edkeUX3njUheIDyym1ucHFXpI7tYRCubiwq9o0Xaz4rp8gMqij/LOgbjFGwIu5Nq3vnTNrHgZX0qhHAPJXDB6Fl4VX5ijme85hb52fDl3DXpLayFc0GCzkmDPnanw9pFxuZ6ex7YfqbK/AEHsMwvDDc=");
        }

        if (locations.size() > 7) {
            handleArcadeNpc(ArcadeCategory.DUELS_CHAMBER, locations.get(7),
                "ewogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJpZCIgOiAiM2MwYTI3ZjI3YWE4NDFmODk5YmU3NjBjMjhkYTkzMWMiLAogICAgICAidHlwZSIgOiAiU0tJTiIsCiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWIxOTQwMGJiODk2MjkwNjRmYzQ1NzM2MWE2MGZjZDA4NzY1NDdhMTBlMDRmYjJkMjQ5Y2ZhYjU2N2NkNmFkNCIsCiAgICAgICJwcm9maWxlSWQiIDogImQ2MGYzNDczNmExMjQ3YTI5YjgyY2M3MTViMDA0OGRiIiwKICAgICAgInRleHR1cmVJZCIgOiAiNWIxOTQwMGJiODk2MjkwNjRmYzQ1NzM2MWE2MGZjZDA4NzY1NDdhMTBlMDRmYjJkMjQ5Y2ZhYjU2N2NkNmFkNCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfSwKICAic2tpbiIgOiB7CiAgICAiaWQiIDogIjNjMGEyN2YyN2FhODQxZjg5OWJlNzYwYzI4ZGE5MzFjIiwKICAgICJ0eXBlIiA6ICJTS0lOIiwKICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWIxOTQwMGJiODk2MjkwNjRmYzQ1NzM2MWE2MGZjZDA4NzY1NDdhMTBlMDRmYjJkMjQ5Y2ZhYjU2N2NkNmFkNCIsCiAgICAicHJvZmlsZUlkIiA6ICJkNjBmMzQ3MzZhMTI0N2EyOWI4MmNjNzE1YjAwNDhkYiIsCiAgICAidGV4dHVyZUlkIiA6ICI1YjE5NDAwYmI4OTYyOTA2NGZjNDU3MzYxYTYwZmNkMDg3NjU0N2ExMGUwNGZiMmQyNDljZmFiNTY3Y2Q2YWQ0IiwKICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgIH0KICB9LAogICJjYXBlIiA6IG51bGwKfQ==",
                "bdvDyFs8RuVw4g2dwNg4v+f0rO1FATAkfyUYO5e8GVH+kI7A6ianzFk7HmP83T5qG2RvFKIwSpy7QRUnYuBh3ZVhdfMIDYAYgmG3OE0UijILFKaXjCXoHLJ8dx0V8FXpHYfX7gWoFqraKrhXbFoVi43e0R5NwQc3YFzT6HdS4XqkxWYsvWi6R4SrY5dwfDw+HRaVmuwnavwZXSRAC5Xh2Siz3NwEja0pc7wt0eoH05P7zXySYkrCPqO1A7wAXJw8YYxuFZiBir10fFvsZIx57txyu9QQ8ZWWrFVf37PyVNz3BQ0SBAjjeJ+tqWuSTgaq7RTFKvIWIfhqYc5iihEDEMOrclU+GEUjsi2inYi3gSC7x2ww115cOpXfjVvx416UsdNTsXD6AvynY0D6ABWp+hZnWjK3G6f7pSl7x6WRZGO5HbOCTUjstfI95o+/n7E8cXstCtVRtyr9ssLOWvAqeFmfKpfKxKFfILARqVkk0Fcm3FEvaY67CU5pNNpkMHjxGS6yGY/6rd3QH2UQseJflBp3Qw/AkF/aGy7K6MCuWtkaYDjvSvuAZ4VWJ+kpRRBUSQZUyJ39MVPfid7+5Ek9Td6MX7Y1JhrV0xsQ1OfeaZRBMi2JM5kaNR86rv8Jyg+9l4D4x5c6uSPSh7PkbaDNPurOzwooWcoeV3KHeNm0SMU=");
        }

        if (locations.size() > 8) {
            handleArcadeNpc(ArcadeCategory.DUELS_THE_BRIDGE, locations.get(8),
                "ewogICJ0aW1lc3RhbXAiIDogMTYyOTc0MTc3ODEzMSwKICAicHJvZmlsZUlkIiA6ICJjNjc3MGJjZWMzZjE0ODA3ODc4MTU0NWRhMGFmMDI1NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDVUNGTDE2IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzEzZDlmNzQ0MDBmYWM3ZGNlZmJkN2UzZmIwOGJiYmEwYTAyOWFjNTI5MWQ2ZGRiZjJmZGM2NmM5Zjc3M2QwOTMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                "bM2F1UUTW2ID6a0VZr2hFC28RZr+cM02JbpIX1wWP2sNP5G/1asOVS/D5RGcRFF2JPgQHPWlkMsh2C26U8UGoQUHoq3qWku6eO4S+ZJeiUMfnHPvxzcdXXEt5NaPexMURghmgrJlw7DRlXgUxUGXQ8G4j8LOkHHD5L6f998DOibUbZacyHklJkC8hA/OizdRsQ+aR+g7RDH8pSQx+mYO+T4OUBHSBOEVSKT9zozEGglTNUCJVtR01/sIYm0d5gQIj+yu/wBhsTVQ1pUSkV22DEhGLHPQJOO5+c0xU1gkhBst3k3dZhWqNAaSUQYVFQhTbZ0I59qs7fQbUNuUP05rlLPKYhLj7oewSOgt7XsdATwdrMP3ZPF/ldJmbCXKGYEY/kmZhV2jTm6bnALN6QbG92vEK2pdvvi9J0Q/1kuIRJuqzmS8LOhTDAK+eVv81MTC8VVZ33uWqaz8Y8PbBFZjxN4/oGEtZcA/XB+Qg1TCO5p74rs/dkx5PACe3qEgyPRM0N/BH6BtbFhiAd0WF5005X9J8TrT9WU2WVX65oF3eAB15t78+VgeX5o1THu+QkbaHZFKL4oQbK7h+KoaLrxSFk4Zy7QhOy5joQG6tttrETrXcwDh4+ZfPS/+Etd89CrygS9eUHhUbzcabbkXbq8jgOb0X7dAu01dkynfnOO6rJg=");
        }

        if (locations.size() > 9) {
            handleArcadeNpc(ArcadeCategory.DUELS_NO_DEBUFF, locations.get(9),
                "ewogICJ0aW1lc3RhbXAiIDogMTYwMTYwMDAxMzU4MiwKICAicHJvZmlsZUlkIiA6ICIyM2YxYTU5ZjQ2OWI0M2RkYmRiNTM3YmZlYzEwNDcxZiIsCiAgInByb2ZpbGVOYW1lIiA6ICIyODA3IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzcxZTcxYWJhOWFlZmE3N2QyOTg2ZDBiZjU2NTQ1NWFiZjY0ZjRkMDZkZTcxMDY3NDg0Mzc3N2VlNDM0Y2M1NDAiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                "ZM9DujqnM7jEukH8WxCfrHVV8GjGf+02T3edDQg47bqMbAz8hxntcOvPZcdk2Ciy9wopgnRDqtyKAeuAKq4eNcPv8eu1qeEwPE3x+AMuJ6xMij5SKAWaSrgPlgjF9K3quAhtI3mtjW4qwl1uI1deP76HNKuA2JTR+Tj8dctZ/w0okvcM4c2/ABBsGoNheM1bn5KSn+A8+adHBV0NfJGea4u9xurc99IVbjQ41GoiPqCldkE8z+PtZG1KUZo7MwFg3FbWg2+9oD4v7+8w3pjf2ewfcC7Mar+l5w64cGmMnXtmlnqUWiG2/iN2MroDGjSUctHKNX5F0s2u6LSRYO5YKm3VjPYpH7wk6Wguer2SIrJ8Ltz/ZFMZ1DBPDTyv1pTQBEPQOdskll7NBlMLKJvFSkJNPacNvmTAT2FPQ6N+vEcchVkMueAYP7yHL81X6L+JbVB4x8yTghs4RznCsMBKqcCDSvfcdAdDsODwtn1JIgtMzeR9XJZpFNZxqDA0rLWX/APJ1hqHXadLk9RnTBqeKhTpol/4WMJoQdqNY9Q/jiMt1sCMW0ybiwmof8Iyt2UX8IFWIh8iLrpy3BqbkKnjJUo6wtpbVUYnzwnsdM7c7FxnuknKkR8UP6dRu8rxx5awcmvAXXRD6vRcSF1+H2Obwkwnoo4dYEEh5DmnLDcG+sg=");
        }

        if (locations.size() > 11) {
            handleStatsNpc(locations.get(11));
        }
    }

    @Override
    public void chat(Player player, String message) {
        User user = (User) User.of(player.getUniqueId());

        Account account = user.getAccount();

        DuelMember member = user.getMember(DuelMember.class);

        if (member == null) {
            player.sendMessage("§cNão foi possível recolher os seus dados. Contate um administrador!");
            return;
        }

        Core.getAccountController().list().forEach(target -> {
            StringBuilder builder = new StringBuilder();
            if (account.getMedal() != null && !account.getMedal().equals(com.minecraft.core.account.context.objects.medal.Medal.NONE)) {
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