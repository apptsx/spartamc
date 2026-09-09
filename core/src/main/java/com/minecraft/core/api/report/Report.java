package com.minecraft.core.api.report;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.route.RouteContext;
import com.minecraft.core.api.report.context.ReportContext;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class Report {

    private final int id;
    private final UUID target;

    private final Set<ReportContext> contexts = new HashSet<>();

    private final long createdAt = System.currentTimeMillis(), expiresAt;

    public Report(UUID target, long expiresAt) {
        this.id = Core.getReportData().list().size() + 1;

        this.target = target;
        this.expiresAt = expiresAt;

        Core.getReportData().save(this);
    }

    protected void update() {
        Core.getReportData().update(this);
    }

    public void delete() {
        Core.getReportData().delete(this);
    }

    public String getTargetName() {
        Account account = Core.getAccountController().of(target);

        return account != null ? account.getNickname() : "...";
    }

    /* Route */
    public RouteContext getRoute() {
        Account account = Core.getAccountData().of(target);

        return account != null ? account.getRoute() : RouteContext.builder().build();
    }

    /* Context System */
    public ReportContext getContext(UUID sender) {
        return contexts.stream()
                .filter(context -> context.isValid() && context.getSender().equals(sender))
                .findFirst()
                .orElse(null);
    }

    public boolean hasContext(UUID sender) {
        return getContext(sender) != null;
    }

    public void addContext(ReportContext context) {
        contexts.add(context);
        update();
    }

    public void removeContext(ReportContext context) {
        contexts.remove(context);
        update();
    }

    /* Methods to Check */
    public boolean hasExpired() {
        return expiresAt < System.currentTimeMillis();
    }
}
