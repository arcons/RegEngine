package com.github.izbay.siegeengine;

//import net.minecraft.server.v1_7_R1.EntityMinecartAbstract;
//import net.minecraft.server.v1_7_R1.NBTTagCompound;

import com.github.izbay.regengine.RegEnginePlugin;

import net.minecraft.server.v1_7_R1.EntityMinecartAbstract;
import net.minecraft.server.v1_7_R1.NBTTagCompound;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftMinecart;
//import org.bukkit.World;
//import org.bukkit.craftbukkit.v1_7_R1.entity.CraftMinecart;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
//import org.bukkit.event.block.BlockPhysicsEvent;
//import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

public class PlayerListener implements Listener{
	SiegeEnginePlugin plugin = (SiegeEnginePlugin)Bukkit.getServer().getPluginManager().getPlugin("SiegeEngine");
	RegEnginePlugin reg = (RegEnginePlugin)Bukkit.getServer().getPluginManager().getPlugin("RegEngine");
	
	@EventHandler
    private void spawnRam(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if(e.getAction()==Action.RIGHT_CLICK_BLOCK &&
				e.getClickedBlock().getType() == Material.ANVIL &&
				e.getClickedBlock().getLocation().add(0,-1,0).getBlock().getType() == Material.CAULDRON &&
				p.getItemInHand().getType() == Material.FLINT_AND_STEEL){
			
			p.getWorld().playSound(p.getLocation(), Sound.IRONGOLEM_DEATH, 1, 1);
			e.getClickedBlock().setType(Material.AIR);
			e.getClickedBlock().getLocation().add(0,-1,0).getBlock().setType(Material.AIR);
			Location target = e.getClickedBlock().getLocation().add(0, 0, 0);
			
			// Ensure that all carts are on the same yaw coord system.
			target.setYaw(0);
			Minecart minecart = (Minecart) p.getWorld().spawnEntity(target, EntityType.MINECART);
			setData(minecart, Material.ANVIL, 5);
		}
    }
	
	@EventHandler
	private void cancelRide(PlayerInteractEntityEvent e){
		//TODO: differentiate minecarts and rams.
		if(e.getRightClicked().getType() == EntityType.MINECART){
			System.out.println(e.getRightClicked().getLocation().getYaw());
			e.setCancelled(true);
		}
		//TODO: Move the climb slope code to here. Take it out of collision for controllability.
	}
	
	@EventHandler
	private void ramCollision(VehicleBlockCollisionEvent e){
		if(e.getVehicle().getType() == EntityType.MINECART){
			Location v = e.getVehicle().getLocation();
			Location upLoc = e.getBlock().getLocation().add(0,1,0);
			if(v.getBlock().getType().isSolid()){
				e.getVehicle().teleport(v.add(0,1,0));
			}
			if(e.getBlock().getLocation().getBlock().getType().isSolid()){
				if(!upLoc.getBlock().getType().isSolid()){
					reg.alter(v, Material.RAILS);
					reg.alter(upLoc, Material.RAILS);
				} else {
					Double yaw = toRadians(v.getYaw());
					int sin = (int) Math.round(Math.sin(yaw));
					int cos = (int) Math.round(Math.cos(yaw));
					
					//Generate a small cone. We'll worry about multipliers and non-right angles later.
					Vector[] vec = {
							new Vector(0,0,0),
							// Up Down
							new Vector(0,1,0), new Vector(0,-1,0),
							// Left Right
							new Vector(0,0,-sin), new Vector(-cos,0,0),
							new Vector(cos,0,0), new Vector(0,0,sin),
							// Forward
							new Vector(sin,0,0), new Vector(sin*2,0,0),
							new Vector(0,0,cos), new Vector(0,0,cos*2)};
					
					for(int i=0; i<vec.length; i++){
						Location test = new Location(upLoc.getWorld(), upLoc.getX(), upLoc.getY(), upLoc.getZ());
						test = test.add(vec[i]);
						reg.alter(test, Material.AIR);
					}
				}
			}
		}
	}
	
	private double toRadians(double yaw){
		return (270-yaw) * Math.PI / 180;
	}
	
	//TODO: Detect downhill slope and set tracks.
	@EventHandler
	private void ramDownhill(VehicleMoveEvent e){
		/** This doesn't work.
		if(e.getVehicle().getType() == EntityType.MINECART){
			if(e.getFrom().add(0,-1,0).getBlock().getType().isSolid() &&
					!e.getTo().add(0,-1,0).getBlock().getType().isSolid() &&
					e.getTo().add(0,-2,0).getBlock().getType().isSolid()){
				reg.alter(e.getFrom(), Material.RAILS);
				reg.alter(e.getTo().add(0,-1,0), Material.RAILS);
			}
		}
		*/
	}

	//TODO: Degenericize this. It's 'borrowed' from DiagonalBlocks, and much of it is unneeded.
	@SuppressWarnings("deprecation")
	private void setData(Minecart minecart, Material block, int data)
	  {
	    
		if (!block.isBlock()) {
	      throw new IllegalArgumentException("The material of the minecart must be a block!");
	    }
	    EntityMinecartAbstract rawMinecart = getRaw(minecart);
	    NBTTagCompound minecartTag = getCompound(rawMinecart);
	    minecartTag.setByte("CustomDisplayTile", (byte)1);
	    minecartTag.setInt("DisplayTile", block.getId());
	    minecartTag.setInt("DisplayOffset", data);
	    
	    setCompound(rawMinecart, minecartTag);
	  }
	
	  private static EntityMinecartAbstract getRaw(Minecart target)
	  {
	    return ((CraftMinecart)target).getHandle();
	  }
	  private static NBTTagCompound getCompound(EntityMinecartAbstract target)
	  {
	    NBTTagCompound tag = new NBTTagCompound();
	    target.c(tag);
	    return tag;
	  }
	  
	private static void setCompound(EntityMinecartAbstract target, NBTTagCompound compound)
	  {
	    target.f(compound);
	  }
}
