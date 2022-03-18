package com.wispguardian.fortarcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;

public class BuildCommand implements CommandExecutor {

	//	private ArrayList<Player> players = new ArrayList<Player>();
	private HashMap<Player, BlockVector3[]> players = new HashMap<Player, BlockVector3[]>();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if(sender instanceof Player) {
			Player player = (Player)sender;
			toggleBuild(player);
		}

		return true;
	}

	private void toggleBuild(Player player) {
		if(players.containsKey(player)) {
			// disable/finish
			saveBase(player);
			players.remove(player);
			player.sendMessage(Main.PREFIX + "BASE SAVED");
		}else {
			// enable/build
			player.setGameMode(GameMode.CREATIVE);
			if(!playerHasBase(player)) pasteDefaultBase(player);
			else pastePlayerBase(player);
			player.sendMessage(Main.PREFIX + "BUILD MODE ACTIVATED");
		}
	}

	private static WorldEditPlugin WE() {
		return Utils.WE();
	}

	private boolean playerHasBase(Player player) {
		String baseFile = "/bases/base_"+player.getUniqueId();
		File file = new File(Main.instance.getDataFolder().getAbsolutePath() + baseFile);
		return file.exists();
	}

	@SuppressWarnings("deprecation")
	private void saveBase(Player player) {
		String baseFile = "/bases/base_"+player.getUniqueId();
		File file = new File(Main.instance.getDataFolder().getAbsolutePath() + baseFile);
		BlockVector3[] blocks = players.get(player);
		BlockVector3 pos1 = blocks[0];
		BlockVector3 pos2 = blocks[1];
		// copy region
		CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(player.getWorld()), pos1, pos2);
		BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
		BlockVector3 origin = clipboard.getOrigin();
		clipboard.setOrigin(BlockVector3.at(origin.getBlockX()+region.getWidth()/2, origin.getBlockY()+1, origin.getBlockZ()+region.getLength()/2));

		try(EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(region.getWorld(), -1)) {
			ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
			forwardExtentCopy.setCopyingEntities(true);
			Operations.complete(forwardExtentCopy);
		}catch(WorldEditException e) {
			e.printStackTrace();
		}
		// save to file
		try(ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
			writer.write(clipboard);
		}catch(FileNotFoundException e) {
			e.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
		}
		// set air
		try(EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(region.getWorld(), -1)) {
			editSession.setBlocks(region, BukkitAdapter.adapt(Material.AIR.createBlockData()));
		} catch (MaxChangedBlocksException e) {
			e.printStackTrace();
		}
	}

	private void pasteDefaultBase(Player player) {
		Clipboard clipboard = Utils.getClipboard(WE().getDataFolder().getAbsolutePath() + "/schematics/base.schem");

		Location loc = player.getLocation();
		loc = new Location(player.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		Utils.pasteClipboard(clipboard, loc);

		// get positions of region to save when done
		BlockVector3 origin = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		BlockVector3 dim = clipboard.getDimensions();
		BlockVector3 pos1 = origin.subtract(dim.getBlockX()/2, 1, dim.getBlockZ()/2);
		BlockVector3 pos2 = origin.add(dim.getBlockX()/2, dim.getBlockY()-1, dim.getBlockZ()/2);
		BlockVector3[] blocks = {pos1, pos2};
		players.put(player, blocks);
	}

	private void pastePlayerBase(Player player) {
		String baseFile = "/bases/base_"+player.getUniqueId();
		Clipboard clipboard = Utils.getClipboard(Main.instance.getDataFolder().getAbsolutePath()+baseFile);

		Location loc = player.getLocation();
		loc = new Location(player.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		Utils.pasteClipboard(clipboard, loc);

		// get positions of region to save when done
		BlockVector3 origin = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		BlockVector3 dim = clipboard.getDimensions();
		BlockVector3 pos1 = origin.subtract(dim.getBlockX()/2, 1, dim.getBlockZ()/2);
		BlockVector3 pos2 = origin.add(dim.getBlockX()/2, dim.getBlockY()-1, dim.getBlockZ()/2);
		BlockVector3[] blocks = {pos1, pos2};
		players.put(player, blocks);
	}

}
