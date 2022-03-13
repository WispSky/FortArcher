package com.wispguardian.fortarcher;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	public static Main instance;
	public static final String PREFIX = ChatColor.GOLD+""+ChatColor.BOLD+"["+
			ChatColor.AQUA+""+ChatColor.BOLD+"FortArcher"+
			ChatColor.GOLD+""+ChatColor.BOLD+"] "+ChatColor.RESET+""+ChatColor.AQUA;
	
	@Override
    public void onEnable() {
		Main.instance = this;
		
		File f = new File(getDataFolder() + "/");
		if(!f.exists()) f.mkdir();
		f = new File(getDataFolder().getAbsolutePath() + "/bases");
		if(!f.exists()) f.mkdir();
		f = new File(getDataFolder().getAbsolutePath() + "/maps");
		if(!f.exists()) f.mkdir();
		
		getServer().getPluginManager().registerEvents(new EventListener(), this);
		
		// commands
		this.getCommand("fight").setExecutor(new FightCommand());
		this.getCommand("build").setExecutor(new BuildCommand());
		this.getCommand("map").setExecutor(new MapCommand());
    }
    
	
    @Override
    public void onDisable() {
    	
    }
    
    public static void sendMsg(Player player, String message) {
    	Main.sendMsg(player, message, ChatColor.AQUA);
    }
    public static void sendMsg(Player player, String message, ChatColor colour) {
    	player.sendMessage(Main.PREFIX + colour + message);
    }
    
}
