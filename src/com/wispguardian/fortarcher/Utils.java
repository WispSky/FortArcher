package com.wispguardian.fortarcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;

public class Utils {

	public static Material[] breakable = {
			Material.WHITE_WOOL, Material.BLACK_WOOL, Material.BLUE_WOOL, Material.BROWN_WOOL,
			Material.CYAN_WOOL, Material.GRAY_WOOL, Material.GREEN_WOOL, Material.LIGHT_BLUE_WOOL,
			Material.LIGHT_GRAY_WOOL, Material.LIME_WOOL, Material.MAGENTA_WOOL, Material.ORANGE_WOOL,
			Material.PINK_WOOL, Material.PURPLE_WOOL, Material.RED_WOOL, Material.YELLOW_WOOL,
	};
	
	public static ItemStack[] loadout = {
			new ItemStack(Material.IRON_SWORD), new ItemStack(Material.BOW),
			new ItemStack(Material.IRON_AXE),
			new ItemStack(Material.ARROW, 128)
	};
	
	public static WorldEditPlugin WE() {
    	Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
    	if(p instanceof WorldEditPlugin) return (WorldEditPlugin)p;
    	return null;
    }
    
    public static Clipboard getClipboard(String filePath) {
    	File file = new File(filePath);
		return getClipboard(file);
    }
    public static Clipboard getClipboard(File file) {
    	Clipboard clipboard = null;
		ClipboardFormat format = ClipboardFormats.findByFile(file);
		try(ClipboardReader reader = format.getReader(new FileInputStream(file))) {
		    clipboard = reader.read();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return clipboard;
    }
    
    public static void pasteClipboard(Clipboard clipboard, Location location) {
    	try(EditSession editSession = WorldEdit.getInstance().newEditSession(new BukkitWorld(location.getWorld()))) {
		    Operation operation = new ClipboardHolder(clipboard)
		            .createPaste(editSession)
		            .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
		            .build();
		    Operations.complete(operation);
		} catch (WorldEditException e) {
			e.printStackTrace();
		}
    }
    public static void pasteClipboard(ClipboardHolder clipboard, Location location) {
    	try(EditSession editSession = WorldEdit.getInstance().newEditSession(new BukkitWorld(location.getWorld()))) {
		    Operation operation = clipboard
		            .createPaste(editSession)
		            .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
		            .build();
		    Operations.complete(operation);
		} catch (WorldEditException e) {
			e.printStackTrace();
		}
    }
    
    public static Clipboard getBase(Player player) {
    	String path = Main.instance.getDataFolder().getAbsolutePath()+
    			"/bases/base_"+player.getUniqueId();
    	File f = new File(path);
    	if(f.exists()) return getClipboard(f);
    	else {
    		path = WE().getDataFolder().getAbsolutePath()+"/schematics/base.schem";
    		f = new File(path);
    		return getClipboard(f);
    	}
    }
    
//    public static BlockVector3[] findBedrock(Clipboard clipboard) {
////    	BlockVector3 dims = clipboard.getDimensions();
//    	ArrayList<BlockVector3> locs = new ArrayList<BlockVector3>();
//    	BlockVector3 min = clipboard.getMinimumPoint();
//    	BlockVector3 max = clipboard.getMaximumPoint();
//    	for(int y = min.getBlockY(); y < max.getBlockY(); y++) {
//    		for(int z = min.getBlockZ(); z < max.getBlockZ(); z++) {
//    			for(int x = min.getBlockX(); x < max.getBlockX(); x++) {
//    				BlockVector3 v = BlockVector3.at(x, y, z);
//    				BlockType b = clipboard.getFullBlock(v).getBlockType();
//    				Material m = BukkitAdapter.adapt(b);
//    				if(m == Material.BEDROCK) {
//    					locs.add(v);
//    					if(locs.size() == 4) {
//    						BlockVector3[] loc = new BlockVector3[4];
//    						return locs.toArray(loc);
//    					}
//    				}
//    			}
//    		}
//    	}
//    	return null;
//    }
	
}
