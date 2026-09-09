package com.minecraft.lobby.menu.patch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.minecraft.lobby.Lobby;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PatchLogStorage {

    private static PatchLogStorage instance;
    private List<PatchLog> patchLogs = new ArrayList<>();
    private final File storageFile;

    public PatchLogStorage() {
        this.storageFile = new File(Lobby.getInstance().getDataFolder(), "patchlogs.json");
        load();
    }

    public static PatchLogStorage getInstance() {
        if (instance == null) {
            instance = new PatchLogStorage();
        }
        return instance;
    }

    public void load() {
        try {
            if (storageFile.exists()) {
                Gson gson = new Gson();
                FileReader reader = new FileReader(storageFile);
                Type listType = new TypeToken<List<PatchLog>>() {}.getType();
                patchLogs = gson.fromJson(reader, listType);
                if (patchLogs == null) patchLogs = new ArrayList<>();
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            patchLogs = new ArrayList<>();
        }
        
        if (patchLogs.isEmpty()) {
            List<String> initialContent = java.util.Arrays.asList(
                "§6§lPATCH LOGS",
                "",
                "§e§l18/04/2026",
                "",
                "§e§lAdicionado:",
                "§7• Sistema de /amigos",
                "§7• Novos gadgets",
                "§7• Blocos coloridos",
                "§7• Novos títulos",
                "",
                "§e§lClan:",
                "§7• Novas cores de clantag",
                "",
                "§e§lNovos ranks:",
                "§7Férias, Beta, Start, Champion",
                "",
                "§e§lBedwars:",
                "§7• Top ranking de pontos",
                "§7• 4 cosméticos de Kill Finais",
                "",
                "§e§lCorrigido:",
                "§7• Diversos bugs e melhorias"
            );
            PatchLog initialLog = new PatchLog("18/04/2026", initialContent);
            patchLogs.add(initialLog);
            save();
        }
    }

    public void save() {
        try {
            Gson gson = new Gson();
            FileWriter writer = new FileWriter(storageFile);
            gson.toJson(patchLogs, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addPatchLog(PatchLog patchLog) {
        patchLogs.add(0, patchLog);
        save();
    }

    public void removePatchLog(UUID id) {
        patchLogs.removeIf(log -> log.getId().equals(id));
        save();
    }

    public PatchLog getPatchLog(UUID id) {
        return patchLogs.stream()
                .filter(log -> log.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Getter
    @Setter
    public static class PatchLog {
        private UUID id;
        private String date;
        private List<String> content;

        public PatchLog(String date, List<String> content) {
            this.id = UUID.randomUUID();
            this.date = date;
            this.content = content;
        }
    }
}