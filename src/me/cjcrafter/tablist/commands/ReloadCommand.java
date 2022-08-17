package me.cjcrafter.tablist.commands;

import me.cjcrafter.tablist.TabList;
import me.deecaad.core.commands.CommandPermission;
import me.deecaad.core.commands.SubCommand;
import me.deecaad.core.utils.StringUtil;
import org.bukkit.command.CommandSender;

@CommandPermission(permission = "tablist.command.reload")
public class ReloadCommand extends SubCommand {

    public ReloadCommand() {
        super("tablist", "reload", "Reloads the plugin");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        TabList.getInstance().onReload();
        sender.sendMessage(StringUtil.color("&aReloaded plugin"));
    }
}
