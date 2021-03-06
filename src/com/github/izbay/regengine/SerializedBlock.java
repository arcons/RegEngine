package com.github.izbay.regengine;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.util.Vector;

import com.github.izbay.util.Util;

public class SerializedBlock implements Comparable<SerializedBlock> {
		
	private final int x, y, z;
	@SuppressWarnings("unused")
	private final long date;
	@SuppressWarnings("unused")
	private final String type, world;
	private final byte data;
	private final String[] signtext;
	private final SerializedInventory inventory;

	@SuppressWarnings("deprecation")
	public SerializedBlock(Block block){
		// Unfortunately the nested ternary is required to make the constructor the first instruction of the overloaded constructor.
		this(block.getWorld().getTime(), block.getWorld().getName(), block.getType().name(), block.getData(), (block.getType() == Material.SIGN)? ((Sign)block).getLines():null, (block.getState() instanceof Chest)?((Chest)block.getState()).getBlockInventory():(block.getState() instanceof InventoryHolder)?((InventoryHolder)block.getState()).getInventory():null, block.getX(), block.getY(), block.getZ());
	}
	
	public SerializedBlock(long date, String world, String type, byte data, String[] signtext, Inventory inventory, int x, int y, int z) {
		this.date = date;
		this.world = world;
		this.type = type;
		this.data = data;
		this.signtext = signtext;
		this.inventory = new SerializedInventory(inventory);
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@SuppressWarnings("deprecation")
	public void place(Location l) {
		l.getBlock().setType(Material.getMaterial(type));
	    l.getBlock().setData(data);
	    if(inventory.getInventory() != null){
	    	BlockState state = l.getBlock().getState();
	    	if(state instanceof Chest){
	    		((Chest) state).getBlockInventory().setContents(inventory.getInventory());
	    	} else if(state instanceof InventoryHolder){
	    		((InventoryHolder) state).getInventory().setContents(inventory.getInventory());
	    	} else if(state instanceof Sign){
	    		int i = 0;
	    		for (String str: signtext){
	    			((Sign) state).setLine(i++, str);
	    		}
	    	}
	    }
		
	}

	@Override
	public int compareTo(SerializedBlock other) {
		return Util.compare(new Vector(x, y, z), new Vector(other.getX(), other.getY(), other.getZ()));
	}

	private int getX() {
		return x;
	}
	
	private int getY() {
		return y;
	}
	
	private int getZ() {
		return z;
	}
}