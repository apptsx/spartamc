package com.minecraft.core.bukkit.api.hologram.type.client;

import com.minecraft.core.bukkit.api.hologram.Hologram;
import com.minecraft.core.bukkit.api.hologram.row.HologramRow;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class HologramClient extends Hologram {

    private final Player receiver;

    public HologramClient(Player receiver, String tag, Location location) {
        super(tag, location);

        this.receiver = receiver;
    }

    public HologramClient(Player receiver, String tag, Location location, long expiresAt) {
        super(tag, location, expiresAt);

        this.receiver = receiver;
    }

    public synchronized void setTextRows(List<HologramRow> lines) {
        setText(lines.stream().map(HologramRow::getText).collect(Collectors.toList()));
    }

    public synchronized void setSpaciousText(List<String> lines) {
        // Always despawn old rows and create new ones to avoid accumulation
        getEntityIds().clear();
        
        // Despawn all existing rows
        getRows().forEach(row -> row.despawn(receiver));
        getRows().clear();
        
        Location loc = getLocation().clone();

        if (loc == null) return;

        int i = 0;
        for (String line : lines) {
            getRows().add(new HologramRow(this, loc, line));

            double offset = 0.25 + (i > 2 ? 0.05 : 0);

            loc = loc.subtract(0, offset, 0);
            i++;
        }

        getRows().forEach(row -> row.spawn(receiver));
    }

    @Override
    public synchronized void setText(List<String> lines) {
        getEntityIds().clear();

        if (getRows().isEmpty()) {
            Location loc = getLocation().clone();

            if (loc == null) return;

            for (String line : lines) {
                getRows().add(new HologramRow(this, loc, line));
                loc = loc.clone().subtract(0, 0.25, 0);
            }

            getRows().forEach(row -> row.spawn(receiver));

            return;
        }

        int diff = lines.size() - getRows().size();

        if (diff > 0) { // Adicionar novas linhas
            int index = 0;

            for (HologramRow row : getRows()) {
                row.setText(lines.get(index++));
            }

            updateRows();

            int lastIndex = getRows().size() - 1;
            HologramRow lastRow = getRows().get(lastIndex);
            Location loc = lastRow.getLocation().clone().subtract(0, 0.25, 0);

            while (diff > 0) {
                HologramRow row = new HologramRow(this, loc, lines.get(index++));

                getRows().add(row);
                row.spawn(receiver);

                loc = loc.clone().subtract(0, 0.25, 0);
                diff--;
            }
        } else { // Remove linhas e atualiza as restantes
            Iterator<HologramRow> rowIterator = getRows().iterator();

            for (int i = 0; rowIterator.hasNext(); i++) {
                HologramRow row = rowIterator.next();

                if (i < lines.size()) {
                    row.setText(lines.get(i));
                } else {
                    row.despawn(receiver);
                    rowIterator.remove();
                }
            }

            updateRows();
        }
    }

    @Override
    public void updateRows() {
        getRows().forEach(row -> {
            row.update(receiver);

            if (!isEntityId(row.getId()))
                getEntityIds().add(row.getId());
        });
    }
}
