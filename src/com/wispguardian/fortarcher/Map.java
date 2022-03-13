package com.wispguardian.fortarcher;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Map implements Serializable {
	private static final long serialVersionUID = 2255278366627016585L;
	
	private transient Location base1, base2, flag1, flag2;
	private transient Player player;
	
	private int[][] locations = new int[4][3];
	private String name;
	
	public Map(Player player, String name) {
		this.player = player;
		this.name = name;
	}
	
	private void updateArray() {
		locations[0][0] = base1.getBlockX();
		locations[0][1] = base1.getBlockY();
		locations[0][2] = base1.getBlockZ();
		locations[1][0] = base2.getBlockX();
		locations[1][1] = base2.getBlockY();
		locations[1][2] = base2.getBlockZ();
		locations[2][0] = flag1.getBlockX();
		locations[2][1] = flag1.getBlockY();
		locations[2][2] = flag1.getBlockZ();
		locations[3][0] = flag2.getBlockX();
		locations[3][1] = flag2.getBlockY();
		locations[3][2] = flag2.getBlockZ();
	}
	
	public void saveToFile() {
		String f = Main.instance.getDataFolder().getAbsolutePath()+"/maps/"+name;
		try {
			FileOutputStream fout = new FileOutputStream(f);
			try (ObjectOutputStream oos = new ObjectOutputStream(fout)) {
				oos.writeObject(this);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Map loadFromFile(String filename) {
		String f = Main.instance.getDataFolder().getAbsolutePath()+"/maps/"+filename;
		Map m = null;
		ObjectInputStream objectIn = null;
		try {
			FileInputStream fileIn = new FileInputStream(f);
			objectIn = new ObjectInputStream(fileIn);
			m = (Map)objectIn.readObject();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}finally {
			try {
				objectIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return m;
	}
	
	public boolean setNext(Location location) {
		boolean done = false;
		if(base1 == null) {
			base1 = location;
			Main.sendMsg(player, "Left click flag #1 (bedrock block)");
		}else if(flag1 == null) {
			flag1 = location;
			Main.sendMsg(player, "Left click base origin #2 (bedrock block)");
		}else if(base2 == null) {
			base2 = location;
			Main.sendMsg(player, "Left click flag #2 (bedrock block)");
		}else if(flag2 == null) {
			flag2 = location;
			updateArray();
			done = true;
		}
		return done;
	}
	
	// getters/setters
	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public int[][] getLocations() {
		return locations;
	}
	
	public Location getBase1(World world) {
		return new Location(world, locations[0][0], locations[0][1], locations[0][2]);
	}
	
	public Location getBase2(World world) {
		return new Location(world, locations[1][0], locations[1][1], locations[1][2]);
	}
	
	public Location getFlag1(World world) {
		return new Location(world, locations[2][0], locations[2][1], locations[2][2]);
	}
	
	public Location getFlag2(World world) {
		return new Location(world, locations[3][0], locations[3][1], locations[3][2]);
	}
	
}
