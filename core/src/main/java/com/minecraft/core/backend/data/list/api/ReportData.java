package com.minecraft.core.backend.data.list.api;

import com.minecraft.core.api.report.Report;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class ReportData {

    private final RedisDatabase redis;

    private final String REPORT_KEY = "report:";

    public void save(Report report) {
        redis.save(REPORT_KEY + report.getTarget(), report);
    }

    public void delete(Report report) {
        redis.delete(REPORT_KEY + report.getTarget());
    }

    public Report of(UUID target) {
        Report report = redis.load(REPORT_KEY + target, Report.class);

        if (report == null) {
            report = new Report(target, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15));

            save(report);
        }

        return report;
    }

    public void update(Report report) {
        redis.update(REPORT_KEY + report.getTarget(), report);
    }

    public List<Report> list() {
        return redis.loadAll(REPORT_KEY, Report.class);
    }
}
