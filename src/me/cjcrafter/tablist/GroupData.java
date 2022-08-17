package me.cjcrafter.tablist;

import org.bukkit.permissions.Permission;
import org.bukkit.scoreboard.Team;

public class GroupData {

    private String prefix;
    private String suffix;
    private Permission permission;
    private Team team;

    public GroupData(String prefix, String suffix, Permission permission, Team team) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.permission = permission;
        this.team = team;

        team.setPrefix(prefix);
        team.setSuffix(suffix);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
