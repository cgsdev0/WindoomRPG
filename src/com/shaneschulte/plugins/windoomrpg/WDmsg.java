/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shaneschulte.plugins.windoomrpg;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Hikeru
 */
public class WDmsg {

	private WDmsg() { }
        
        //message colors
	public final static ChatColor info = ChatColor.GRAY, bad = ChatColor.RED, nice = ChatColor.AQUA;
        public final static String prefix = ChatColor.translateAlternateColorCodes('&', "&e<&6wRPG&e> " + info);
	
	private static WDmsg instance = new WDmsg();
	
	public static String getPrefix() {
		return prefix;
	}
	
	public static void info(CommandSender s, String msg) {
		msgColor(s, info, msg);
	}
	
	public static void bad(CommandSender s, String msg) {
		msgColor(s, bad, msg);
	}
	
	public static void nice(CommandSender s, String msg) {
		msgColor(s, nice, msg);
	}
	
	
	public static void message(CommandSender s, String msg) {
		s.sendMessage(ChatColor.GOLD + ChatColor.translateAlternateColorCodes('&', msg));
	}
	
	
	private static void msgColor(CommandSender s, ChatColor color, String msg) {
		s.sendMessage(prefix + color + ChatColor.translateAlternateColorCodes('&', msg));
	}
	
}
