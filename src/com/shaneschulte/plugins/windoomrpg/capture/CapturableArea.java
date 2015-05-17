/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shaneschulte.plugins.windoomrpg.capture;

import com.shaneschulte.plugins.windoomrpg.RPGperms;
import com.shaneschulte.plugins.windoomrpg.WindoomRPG;
import static com.shaneschulte.plugins.windoomrpg.WindoomRPG.getWorldGuard;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.ArrayList;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Hikeru
 */
public class CapturableArea implements Listener {

    private static final int UNDERATTACK = -1, NEUTRAL = 0, UNDERCONTROL = 1;
    protected int currentMode = NEUTRAL;
    protected int health = 300; //ender dragon health on spigot servers

    private final JavaPlugin plugin;
    WindoomRPG wplugin = null;

    protected String type = "CapturableArea";
    protected ArrayList<Player> playersInArea = new ArrayList<Player>();
    protected ArrayList<Player> playersInCaptureArea = new ArrayList<Player>();

    protected String name = "Undiscovered Fortress", tag = "&7Undiscovered Fortress", id = "Unknown";
    protected Location capPoint = null;
    protected BlockVector q1, q2;
    protected int capRadius = 8, capTimeInSeconds = 20;
    protected int c = 8, e = 20;
    protected Clan clanInControl = null;

    public CapturableArea(JavaPlugin plugin) {
        this.plugin = plugin;
        this.wplugin = (WindoomRPG) Bukkit.getServer().getPluginManager().getPlugin("WindoomRPG");

        //this.runTaskTimer(plugin, 20, 20);
    }

    public ArrayList<Player> getPlayersInArea() {
        //reset list
        playersInArea = new ArrayList<>();
        //ProtectedRegion region = WindoomRPG.getWorldGuard().getRegionManager(capPoint.getWorld()).getRegion(type + "_" + id);
        String hi = type + "_" + id;

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (isWithinRegion(p, hi)) {
                playersInArea.add(p);
            }
        }
        return playersInArea;

    }

    public void update() {
        //runs every 2 seconds
    }

    public ArrayList<Player> getPlayersInCaptureRadius() {
        //reset list
        playersInCaptureArea = new ArrayList<Player>();
        //ProtectedRegion region = WindoomRPG.getWorldGuard().getRegionManager(capPoint.getWorld()).getRegion(type + "_" + id);
        String hi = type + "_" + id;

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (capPoint.distance(p.getLocation()) <= getCapRadius()) {
                playersInCaptureArea.add(p);
            }
        }
        return playersInCaptureArea;

    }

    public int getCurrentMode() {
        return currentMode;
    }

    public String getCurrentModeTitle() {
        if (currentMode == UNDERATTACK) {
            return "Under Attack!";
        }
        if (currentMode == UNDERCONTROL) {
            return "Claimed";
        } else {
            return "Neutral";
        }
    }

    public void startAttack(Clan clan) {
        onAttack(clan);
        this.currentMode = UNDERATTACK;
    }

    public void forceClaim(Clan clan) {
        onClaim(clan);
        this.currentMode = UNDERCONTROL;
    }

    public void forceNeutral() {
        onNeutral();
        this.currentMode = NEUTRAL;
    }

    /*public void changeMode(int newMode) {
     if (newMode == UNDERCONTROL) onClaim();
     if (newMode == UNDERATTACK) onAttack();
     else onNeutral();
     }*/
    public void onClaim(Clan clan) {
        if (this.getClanInControl() != null) {
            Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', this.getClanInControl().getTagLabel() + "&b"
                    + getTag() + " &7has been captured by &e" + clan.getTagLabel() + "&7!"));
        } else {
            Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&b" + getTag()
                    + " &7has been captured by &e" + clan.getTagLabel() + "&7!"));
        }
    }

    public void onAttack(Clan clan) {
        if (this.getClanInControl() != null) {
            Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', this.getClanInControl().getTagLabel() + "&b"
                    + getTag() + " &7is under attack from &e" + clan.getTagLabel() + "&7!"));
        } else {
            Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&b " + getTag()
                    + " &7is under attack from &e" + clan.getTagLabel() + "&7!"));
        }
    }

    public void onNeutral() {
        Bukkit.getServer().broadcastMessage(/*RPGperms.RECIEVE_CAPTURE_MESSAGES.getPermission(),*/ChatColor.translateAlternateColorCodes('&', getTag()
                        + "&7 is now &6neutral&7."));
    }

    public String getTag() {
        return tag;
    }

    public String getId() {
        return id;
    }

    public void setTag(String tag) {
        this.tag = tag;
        wplugin.getFortressConfig().getConfig().set(type + "." + id + ".tag", tag);

    }

    public Clan getClanInControl() {
        return clanInControl;
    }

    public String getName() {
        return name;
    }

    public Location getCapPoint() {
        return capPoint;
    }

    public int getCapRadius() {
        return capRadius;
    }

    public int getCapTimeInSeconds() {
        return capTimeInSeconds;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setName(String name) {
        this.name = name;
        wplugin.getFortressConfig().getConfig().set(type + "." + id + ".name", name);

    }

    public void setCapPoint(Location capPoint) {
        this.capPoint = capPoint;

        if (capPoint.getWorld() == null) {
            wplugin.getFortressConfig().getConfig().set(type + "." + id + ".capPoint.world", "world");
        } else {
            wplugin.getFortressConfig().getConfig().set(type + "." + id + ".capPoint.world", capPoint.getWorld().getName());
        }
        wplugin.getFortressConfig().getConfig().set(type + "." + id + ".capPoint.x", capPoint.getBlockX());
        wplugin.getFortressConfig().getConfig().set(type + "." + id + ".capPoint.y", capPoint.getBlockY());
        wplugin.getFortressConfig().getConfig().set(type + "." + id + ".capPoint.z", capPoint.getBlockZ());
    }

    public void setCapRadius(int capRadius) {
        this.capRadius = capRadius;
        wplugin.getFortressConfig().getConfig().set(type + "." + id + ".capRadius", capRadius);
    }

    public void setCapTimeInSeconds(int capTimeInSeconds) {
        this.capTimeInSeconds = capTimeInSeconds;
        wplugin.getFortressConfig().getConfig().set(type + "." + id + ".capTimeInSeconds", capTimeInSeconds);

    }

    public void setClanInControl(Clan clanInControl) {
        forceClaim(clanInControl);
        this.clanInControl = clanInControl;
        if (clanInControl != null) {
            wplugin.getFortressConfig().getConfig().set(type + "." + id + ".clan", clanInControl.getTag());
        }

        if (getClanInControl() != null && WindoomRPG.getWorldGuard().getRegionManager(capPoint.getWorld()).getRegion("fortress" + "_" + getId()) != null) {
            ProtectedRegion region = WindoomRPG.getWorldGuard().getRegionManager(capPoint.getWorld()).getRegion("fortress" + "_" + getId());
            region.getOwners().removeAll();
            for (ClanPlayer p : wplugin.getClanManager().getAllClanPlayers()) {
                //owners.addPlayer(p.toPlayer());
                region.getOwners().addPlayer(p.getName());
            }
        }
    }

    public BlockVector getQ1() {
        return q1;
    }

    public void setQ1(BlockVector q1) {
        this.q1 = q1;
        if (q1 != null) {
            wplugin.getFortressConfig().getConfig().set(type + "." + id + ".q1.x", q1.getBlockX());
            wplugin.getFortressConfig().getConfig().set(type + "." + id + ".q1.y", q1.getBlockY());
            wplugin.getFortressConfig().getConfig().set(type + "." + id + ".q1.z", q1.getBlockZ());
        }
    }

    public BlockVector getQ2() {
        return q2;
    }

    public void setQ2(BlockVector q2) {
        this.q2 = q2;
        if (q2 != null) {
            wplugin.getFortressConfig().getConfig().set(type + "." + id + ".q2.x", q2.getBlockX());
            wplugin.getFortressConfig().getConfig().set(type + "." + id + ".q2.y", q2.getBlockY());
            wplugin.getFortressConfig().getConfig().set(type + "." + id + ".q2.z", q2.getBlockZ());
        }
    }

    public boolean isWithinRegion(Player player, String region) {
        return isWithinRegion(player.getLocation(), region);
    }

    public boolean isWithinRegion(Block block, String region) {
        return isWithinRegion(block.getLocation(), region);
    }

    public boolean isWithinRegion(Location loc, String region) {
        WorldGuardPlugin guard = getWorldGuard();
        Vector v = BukkitUtil.toVector(loc.toVector());
        RegionManager manager = guard.getRegionManager(loc.getWorld());
        ApplicableRegionSet set = manager.getApplicableRegions(v);
        for (ProtectedRegion each : set) {
            if (each.getId().equalsIgnoreCase(region)) {
                return true;
            }
        }
        return false;
    }

    /* */
}