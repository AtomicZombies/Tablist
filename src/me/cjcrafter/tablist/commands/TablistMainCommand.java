package me.cjcrafter.tablist.commands;

import me.deecaad.core.commands.MainCommand;

public class TablistMainCommand extends MainCommand {

    public TablistMainCommand() {
        super("tablist", "tablist.command");

        commands.register(new ReloadCommand());
    }
}
