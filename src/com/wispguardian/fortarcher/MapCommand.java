package com.wispguardian.fortarcher;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class MapCommand implements CommandExecutor, Listener {

	private Map map;

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if(sender instanceof Player) {
			Player p = (Player)sender;
			if(map != null) {
				Main.sendMsg(p, "A map is currently being set-up.", ChatColor.RED);
				return true;
			}
			if(args.length == 0) {
				Main.sendMsg(p, "You must provide a name for the map.", ChatColor.RED);
				return true;
			}
			String name = args[0];
			map = new Map(p, name);
			listenOn();
			Main.sendMsg(p, "Left click base origin #1 (bedrock block)");
		}

		return true;
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		if(event.getPlayer() != map.getPlayer()) return;
		event.setCancelled(true);

		boolean done = map.setNext(event.getBlock().getLocation());
		if(done) {
			map.saveToFile();
			Main.sendMsg(map.getPlayer(), "Map coords has been saved");
			map = null;
			listenOff();
		}
	}

	private void listenOn() {
		Main.instance.getServer().getPluginManager().registerEvents(this, Main.instance);
	}

	private void listenOff() {
		HandlerList.unregisterAll(this);
	}

}
