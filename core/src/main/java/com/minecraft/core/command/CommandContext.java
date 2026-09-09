package com.minecraft.core.command;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public abstract class CommandContext {

    private final CommandSender sender;

    private final String[] args;
    private final String label;

    public CommandContext(CommandSender sender, String[] args, String label, int subCommands) {
        int index = args.length - subCommands;

        String[] text = new String[index];

        System.arraycopy(args, subCommands, text, 0, index);

        StringBuilder sb = new StringBuilder();

        sb.append(label);

        for (int i = 0; i < subCommands; i++)
            sb.append(".").append(args[i]);

        this.sender = sender;

        this.args = text;
        this.label = sb.toString();
    }

    public abstract List<String> getServerPlayerList();

    public Account getAccount() {
        return getAccount(sender.getId());
    }

    public Account getAccount(UUID id) {
        return Core.getAccountData().of(id);
    }

    public Account getAccount(String name) {
        return Core.getAccountData().of(name);
    }

    public String getMessage(int start, String... args) {
        StringBuilder sb = new StringBuilder();

        for (int i = start; i < args.length; i++)
            sb.append(args[i]).append(" ");

        return sb.toString().trim();
    }

    public String getCommandLabel() {
        return label.replace(".", " ");
    }
}
