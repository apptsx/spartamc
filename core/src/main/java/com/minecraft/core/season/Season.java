package com.minecraft.core.season;

import com.minecraft.core.Core;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class Season {

    private final int id;
    private final String name;

    private final LocalDateTime startTime, endTime;

    private final Map<String, Object> properties;

    private boolean finished;

    public Season(int id, String name, LocalDateTime endTime) {
        this.id = id;
        this.name = name;

        this.startTime = LocalDateTime.now();
        this.endTime = endTime;

        this.properties = new HashMap<>();
    }

    protected void save(String... fields) {
        for (String field : fields) {
            Core.getSeasonData().update(this, field);
        }
    }

    /* Boolean */
    public boolean isOver() {
        return endTime.isBefore(LocalDateTime.now());
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
        save("finished");
    }

    /* Properties */
    public Object getProperty(String field) {
        return properties.get(field.toLowerCase());
    }

    public void setProperty(String field, Object value) {
        properties.put(field.toLowerCase(), value);
        save("properties");
    }

    public void removeProperty(String field) {
        properties.remove(field.toLowerCase());
        save("properties");
    }
}
