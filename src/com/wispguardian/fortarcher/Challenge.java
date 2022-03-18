package com.wispguardian.fortarcher;

import java.util.ArrayList;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;

import net.md_5.bungee.api.ChatColor;

public class Challenge {

	private static ArrayList<Challenge> challenges = new ArrayList<Challenge>();

	private Player[] players = new Player[2];
	private boolean accepted;
	private int[] score = new int[2];
	private int maxPoints = 5;

	private Location[] flags = new Location[2];
	private Location[] bases = new Location[2];
	private Clipboard mapClip;
	private Map map;

	private ArrayList<Block> blocks = new ArrayList<Block>();

	public Challenge(Player host, Player opp, int maxPoints) {
		players[0] = host;
		players[1] = opp;
		this.maxPoints = maxPoints;
		Challenge.challenges.add(this);
	}

	public void closeChallenge() {
		for(Player p : players) {
			p.setGameMode(GameMode.SPECTATOR);
		}
		Challenge.challenges.remove(this);
	}

	public void addPoint(Player player) {
		if(players[0] == player) score[0]++;
		else score[1]++;
		if(score[0] == maxPoints || score[1] == maxPoints) {
			announceWin(player);
			closeChallenge();
		}else {
			announceScore(player);
			resetMap();
		}
	}
	private void announceScore(Player scorer) {
		String aqua = ChatColor.AQUA + "" + ChatColor.BOLD;
		String red = ChatColor.RED + "" + ChatColor.BOLD;
		players[0].sendTitle(aqua+score[0]+":"+red+score[1],
				((scorer==players[0])?aqua:red)+scorer.getName()+" SCORED",
				20, 20*4, 20);
		players[1].sendTitle(aqua+score[1]+":"+red+score[0],
				((scorer==players[1])?aqua:red)+scorer.getName()+" SCORED",
				20, 20*4, 20);
	}
	private void announceWin(Player winner) {
		String aqua = ChatColor.AQUA + "" + ChatColor.BOLD;
		String red = ChatColor.RED + "" + ChatColor.BOLD;
		players[0].sendTitle(aqua+score[0]+":"+red+score[1],
				((winner==players[0])?aqua:red)+winner.getName()+" WINS!",
				20, 20*4, 20);
		players[1].sendTitle(aqua+score[1]+":"+red+score[0],
				((winner==players[1])?aqua:red)+winner.getName()+" WINS!",
				20, 20*4, 20);
	}

	private void equipPlayers() {
		for(Player p : players) {
			PlayerInventory i = p.getInventory();
			i.clear();

			int randWool = (int)(Math.random()*Utils.breakable.length);
			ItemStack wool = new ItemStack(Utils.breakable[randWool], 64);

			// add hotbar items
			i.addItem(new ItemStack(Material.IRON_SWORD),
					new ItemStack(Material.BOW),
					new ItemStack(Material.IRON_AXE),
					wool,
					new ItemStack(Material.ARROW, 128)
					);
			// set offhand (shield)
			i.setItemInOffHand(new ItemStack(Material.SHIELD));
			// set armour
			ItemStack[] armour = {new ItemStack(Material.IRON_BOOTS),
					new ItemStack(Material.IRON_LEGGINGS),
					new ItemStack(Material.IRON_CHESTPLATE),
					new ItemStack(Material.IRON_HELMET)};
			i.setArmorContents(armour);
		}
	}

	public void resetMap() {
		World world = players[0].getWorld();
		clearBlocks();
		map.getFlag1(world).getBlock().setType(Material.GOLD_BLOCK);
		map.getFlag2(world).getBlock().setType(Material.GOLD_BLOCK);
		players[0].teleport(bases[0]);
		players[1].teleport(bases[1]);
		equipPlayers();
	}
	private void clearBlocks() {
		blocks.forEach((b) -> {
			b.setType(Material.AIR);
		});
	}

	public void accept() {
		accepted = true;
		//		Challenge.challenges.remove(this);

		String mapName = "islands";
		mapClip = Utils.getClipboard(Utils.WE().getDataFolder().getAbsolutePath() + "/schematics/"+mapName+".schem");
		Utils.pasteClipboard(mapClip, players[0].getWorld().getSpawnLocation()); // paste map
		map = Map.loadFromFile(mapName);

		flags[0] = map.getFlag1(players[0].getWorld());
		flags[0].getBlock().setType(Material.GOLD_BLOCK);
		flags[1] = map.getFlag2(players[0].getWorld());
		flags[1].getBlock().setType(Material.GOLD_BLOCK);

		// paste first base
		Location base1Loc = map.getBase1(players[0].getWorld());
		Clipboard base1 = Utils.getBase(players[0]);
		Utils.pasteClipboard(base1, base1Loc);

		// paste second base
		Location base2Loc = map.getBase2(players[0].getWorld());
		Clipboard base2 = Utils.getBase(players[1]);
		ClipboardHolder holder = new ClipboardHolder(base2);
		holder.setTransform(new AffineTransform().rotateY(180));
		Utils.pasteClipboard(holder, base2Loc);

		// teleport players
		bases[0] = base1Loc;
		bases[1] = base2Loc;
		players[0].teleport(base1Loc);
		players[1].teleport(base2Loc);
		players[0].setGameMode(GameMode.SURVIVAL);
		players[1].setGameMode(GameMode.SURVIVAL);
		equipPlayers();
	}

	public void addBlock(Block block) {
		blocks.add(block);
	}

	// getters/setters
	public boolean isAccepted() {
		return accepted;
	}

	public Player getHost() {
		return players[0];
	}

	public Player getOpp() {
		return players[1];
	}

	public Location[] getFlags() {
		return flags;
	}

	public Location[] getBases() {
		return bases;
	}

	// static methods
	public static Challenge getChallenge(Player sender, Player target) {
		for(Challenge c : challenges) {
			if(c.getHost() == target && c.getOpp() == sender) {
				return c;
			}
		}
		return null;
	}
	public static Challenge getChallenge(Player player) {
		for(Challenge c : challenges) {
			if(c.getHost() == player || c.getOpp() == player) {
				return c;
			}
		}
		return null;
	}

	public static boolean inGame(Player player) {
		for(Challenge c : challenges) {
			if(c.getHost() == player || c.getOpp() == player) {
				return true;
			}
		}
		return false;
	}

	public static void clearAll() {
		Challenge.challenges.clear();
	}

}
