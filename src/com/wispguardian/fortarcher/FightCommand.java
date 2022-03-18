package com.wispguardian.fortarcher;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class FightCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if(sender instanceof Player) {
			Player player = (Player)sender;
			if(args.length == 0) {
				Main.sendMsg(player,
						"You must type a player's name to send a challenge!", ChatColor.RED);
			}else {
				String oppName = args[0];
				Player opp = Bukkit.getPlayer(oppName);
				if(opp != null) {
//					if(opp == player) Main.sendMsg(player,
//							"You can't challenge yourself!", ChatColor.RED);
//					else {
						// successful challenge
						Challenge c = Challenge.getChallenge(player, opp);
						if(c == null) {
							int maxPoints = 5;
							if(args.length > 1) {
								try {
									maxPoints = Integer.parseInt(args[1]);
								}catch(Exception e) {
									maxPoints = 5;
								}
							}
							sendChallenge(player, opp, maxPoints);
						}else if(!c.isAccepted() && player == c.getOpp()) c.accept();
						else if(c.isAccepted()) Main.sendMsg(player, "This match has already begun.", ChatColor.RED);
						else if(player == c.getHost()) Main.sendMsg(player, "You already sent a challenge!", ChatColor.RED);
//					}
				}else if(args[0].equals("-1")) {
					Challenge.clearAll();
				}else Main.sendMsg(player, "Player not found", ChatColor.RED);
			}
		}

		return true;
	}

	private void sendChallenge(Player player, Player opp, int maxPoints) {
		Main.sendMsg(player, "You sent a challenge to "+opp.getDisplayName());

		TextComponent challengeMsg = new TextComponent(
				Main.PREFIX+"You have been challenged by "+
						player.getDisplayName());
		challengeMsg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
				new Text(ChatColor.AQUA+""+ChatColor.BOLD+"Click to accept!")));
		challengeMsg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
				"/fight "+player.getName()));

		new Challenge(player, opp, maxPoints);

		opp.spigot().sendMessage(challengeMsg);
	}

}
