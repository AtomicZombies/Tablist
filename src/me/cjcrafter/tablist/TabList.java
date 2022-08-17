package me.cjcrafter.tablist;

import joptsimple.internal.Strings;
import me.cjcrafter.tablist.commands.TablistMainCommand;
import me.deecaad.core.commands.MainCommand;
import me.deecaad.core.file.Configuration;
import me.deecaad.core.file.DuplicateKeyException;
import me.deecaad.core.file.LinkedConfig;
import me.deecaad.core.utils.Debugger;
import me.deecaad.core.utils.FileUtil;
import me.deecaad.core.utils.LogLevel;
import me.deecaad.core.utils.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

public class TabList extends JavaPlugin implements Listener {

    private static TabList instance;

    private Configuration configuration;
    private Debugger debug;
    private MainCommand command;
    private BukkitRunnable playerUpdater;

    private String header;
    private String footer;
    private List<GroupData> groups;

    @Override
    public void onEnable() {
        instance = this;

        debug = new Debugger(getLogger(), 2);
        command = new TablistMainCommand();
        if (!getDataFolder().exists() || getDataFolder().listFiles() == null || getDataFolder().listFiles().length == 0)
            FileUtil.copyResourcesTo(getClass(), getClassLoader(), "resources/TabList", getDataFolder());
        configuration = new LinkedConfig();
        try {
            configuration.add(getConfig());
        } catch (DuplicateKeyException e) {
            e.printStackTrace();
        }

        Method getCommandMap = ReflectionUtil.getMethod(ReflectionUtil.getCBClass("CraftServer"), "getCommandMap");
        SimpleCommandMap simpleCommandMap = (SimpleCommandMap) ReflectionUtil.invokeMethod(getCommandMap, Bukkit.getServer());
        simpleCommandMap.register("tablist", command);

        setupGroups();

        header = Strings.join(configuration.getList("Header"), "\n");
        footer = Strings.join(configuration.getList("Footer"), "\n");

        playerUpdater = new BukkitRunnable(){
            @Override
            public void run() {
                Collection<? extends Player> players = Bukkit.getOnlinePlayers();

                for (Player player : players) {
                    updatePlayer(player);
                }
            }
        };
        playerUpdater.runTaskTimer(this, 200, configuration.getInt("Interval", 600));

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    public void onReload() {

        setupGroups();

        header = Strings.join(configuration.getList("Header"), "\n");
        footer = Strings.join(configuration.getList("Footer"), "\n");

        playerUpdater = new BukkitRunnable(){
            @Override
            public void run() {
                Collection<? extends Player> players = Bukkit.getOnlinePlayers();

                for (Player player : players) {
                    updatePlayer(player);
                }
            }
        };
        playerUpdater.runTaskTimer(this, 200, configuration.getInt("Interval", 600));
    }

    public void registerListeners() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    public void setupGroups() {
        TreeSet<Integer> priorities = new TreeSet<>();

        configuration.forEach("Groups", (k, v) -> {

            String[] split = k.split("\\.");
            String last = split[split.length - 1];
            try {
                int i = Integer.parseInt(last);
                priorities.add(i);
                debug.debug("Adding group with priority " + i);

            } catch (NumberFormatException e) {
                if (!"Default".equals(last)) {
                    debug.error("Not a number: " + last);
                    debug.log(LogLevel.WARN, e);
                }
            }
        }, false);

        int digits = priorities.last() / 10 + 1;

        if (groups == null) {
            groups = new ArrayList<>();
        } else {
            groups.clear();
        }

        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        //sb.resetScores(StringUtils.color("&aKills:"));
        //Objective objective = sb.getObjective("showkills");
        //if (objective == null)
        //    objective = sb.registerNewObjective("showkills", "totalKillCount", "Kills");
//
        //// Redundancy
        //objective.setDisplayName("Kills");
        //objective.setDisplaySlot(DisplaySlot.BELOW_NAME);

        for (int priority : priorities) {

            String prefix = configuration.getString("Groups." + priority + ".Prefix");
            String suffix = configuration.getString("Groups." + priority + ".Suffix");
            String permission = configuration.getString("Groups." + priority + ".Permission");

            Permission perm = new Permission(permission, PermissionDefault.FALSE);

            int tempDigits = digits - (priority / 10 + 1);

            StringBuilder teamName = new StringBuilder();
            while (tempDigits-- > 0) {
                teamName.append(0);
            }
            teamName.append(priority);
            teamName.append("_TABLIST");

            String name = teamName.toString();
            debug.debug("Registering team: " + name);
            Team team = sb.getTeam(name);
            if (team == null) team = sb.registerNewTeam(name);

            GroupData data = new GroupData(prefix, suffix, perm, team);
            groups.add(data);
        }

        Team team = sb.getTeam("Default");
        if (team == null) team = sb.registerNewTeam("Default");

        String prefix = configuration.getString("Groups.Default.Prefix");
        String suffix = configuration.getString("Groups.Default.Suffix");
        GroupData defaultGroup = new GroupData(prefix, suffix, null, team);
        groups.add(defaultGroup);
    }

    public void updatePlayer(Player player) {
        player.setPlayerListHeaderFooter(header, footer);

        // Update health for objective
        player.setHealth(player.getHealth());

        for (GroupData group : groups) {
            Permission permission = group.getPermission();

            if (permission == null || player.hasPermission(permission)) {
                Team team = group.getTeam();
                if (!team.hasEntry(player.getName())) {
                    team.addEntry(player.getName());
                }

                player.setPlayerListName(group.getPrefix() + player.getName() + group.getSuffix());
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        updatePlayer(e.getPlayer());
    }

    public static TabList getInstance() {
        return instance;
    }
}