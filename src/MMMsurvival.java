// Decompiled by DJ v3.11.11.95 Copyright 2009 Atanas Neshkov  Date: 11/11/2010 5:14:57 PM
// Home Page: http://members.fortunecity.com/neshkov/dj.html  http://www.neshkov.com/dj.html - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   MMMsurvival.java

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import javax.xml.bind.*;
import org.apache.commons.net.ftp.FTPClient;
@SuppressWarnings("unused")

public class MMMsurvival extends Plugin
{
	
    private MMMsurvivalListener listener 						= new MMMsurvivalListener();;
    private Random randomGenerator 								= new Random();
    
    private List<Warp> warps 									= new ArrayList<Warp>();
    private HashMap<Player,PlayerStats> stats 					= new HashMap<Player,PlayerStats>();
    private List<Player> playerlist 							= new ArrayList<Player>();
    private HashMap<String,HashSet<PlayerBlock>> playerBlocks 	= new HashMap<String,HashSet<PlayerBlock>>();
    private ArrayList<Integer> transparentBlocks 				= new ArrayList<Integer>();
    
    private ArrayList<Objective> objectives 					= new ArrayList<Objective>();
    private ArrayList<Reputation> reputations 					= new ArrayList<Reputation>();
    
    private HashMap<Player,ThreadPlayer> playerThreads 			= new HashMap<Player,ThreadPlayer>();
    private HashMap<String,ThreadFishing> fishingthreads 		= new HashMap<String,ThreadFishing>();
    
    private HashMap<Integer,String> items						= new HashMap<Integer,String>();
    private HashMap<Integer,String> mobs 						= new HashMap<Integer,String>();
    
    private ArrayList<NPC> npcs									= new ArrayList<NPC>();
    private HashSet<House> houses 								= new HashSet<House>();
    
    private Location house = new Location();
    private Location tmpTo = new Location();
    
    Server server = etc.getServer();
    String Server = "\247c[Server]\247f ";
    
    int 	statStarvation;
    double 	meleeRange;
    int 	warpPrize;
    int 	sethomePrize;
    int		breathCapacity;
    boolean test = false;
	
    NPC testnpc;
    ThreadNPCPath moveth;
    ThreadBuildHouse buildhouse;
    
    public class MMMsurvivalListener extends PluginListener
    {

        public void onLogin(Player player)
        {
            playerlist = server.getPlayerList();
            loadPlayerstats(player);
            playerThreads.put(player, new ThreadPlayer(player));
            String Onlineplayers = "";
            
            for (int i = 0; i < playerlist.size(); i++)
            {
            	if(player(i) != player) {Onlineplayers += getCName(player(i));}
            }
            
            if (Onlineplayers != "") {player.sendMessage("Currently online: " + Onlineplayers);}
            server.setTimer("Spawnprotection-"+player.getName(), 42);
        }

        public void onDisconnect(Player player)
        {
            playerlist = server.getPlayerList();
            savePlayerstats(player);
        }

        public void onArmSwing(Player player)
        {
            switch(player.getItemInHand())
            {
            
            //Eating Stuff
            case 319: 
                playerConsumeInHand(player);
                stats(player).eat(statStarvation / 5, statStarvation);
                break;
            case 39:
                playerConsumeInHand(player);
                stats(player).eat(statStarvation / 25, statStarvation);
                break;
            case 40:
                playerConsumeInHand(player);
                stats(player).eat(statStarvation / 25, statStarvation);
                break;
            case 260: 
                playerConsumeInHand(player);
                stats(player).eat(statStarvation / 2, statStarvation);
                break;
            case 297: 
                playerConsumeInHand(player);
                stats(player).eat(statStarvation / 5, statStarvation);
                break;
            case 320: 
                playerConsumeInHand(player);
                stats(player).eat(statStarvation / 4, statStarvation);
                break;
            case 344: 
                playerConsumeInHand(player);
                stats(player).eat(statStarvation / 4, statStarvation);
                break;

            //GetBlockData
            case 41:
                blox = new HitBlox(player, 300, 0.3);
                Block block = blox.getTargetBlock();
                if(block != null)
                {
                	player.sendMessage(""+block.getX()+" "+block.getY()+" "+block.getZ());
                }
            	break;
                
            //Check Durability
            case 69:
                blox = new HitBlox(player, 300, 0.3);
                block = blox.getTargetBlock();
                if(block != null)
                {
                    PlayerBlock p = getPlayerblock(block);
                    if(p != null)
                    {
                        if (p.breakable)
                        {
                            player.sendMessage("Durability: " + p.durability);
                        }
                        else
                        {
                            player.sendMessage("Durability: Unbreakable");
                        }
                    }
                }
                break;

            case 326: 
                if(stats(player).onfire)
                {
                    stats(player).onfire = false;
                    player.sendMessage("\2477You empty the waterbucket over your head. Good thinking!");
                    playerConsumeInHand(player);
                }
                break;

            case 346: 
                blox = new HitBlox(player, 3, 0.2);
                if(blox.getTargetBlock() != null)
                {
                    int cur = etc.getServer().getBlockAt(blox.getCurBlock().getX(), blox.getCurBlock().getY(), blox.getCurBlock().getZ()).getType();
                    if(cur == 9)
                    {
                    	//????????
                        if(!fishingthreads.containsKey(player.getName()))
                        {
                        	fishingthreads.put(player.getName(), new ThreadFishing(player));
                        }else if(!fishingthreads.get(player.getName()).abort)
	                    {
                        	fishingthreads.get(player.getName()).abort = true;
	                    }
	                    else if (fishingthreads.get(player.getName()).catchy) 
	                    {
	                        ((ThreadFishing)fishingthreads.get(player.getName())).catchy = true;
	                    }
                    }
                }
                break;
            
            case 337:
        		blox = new HitBlox(player, 300, 0.3);
                block = blox.getTargetBlock();
                player.sendMessage(""+block.getData());
            	break;
                
            case 339: 
        		blox = new HitBlox(player, 300, 0.3);
                block = blox.getTargetBlock();
        	 	Block tblock = server.getBlockAt(block.getX(), block.getY(), block.getZ());
            	if (tblock.getType() == 64)
            	{
            		
            		for (String str:getAdjacentDoorBlocks(tblock.getX(),tblock.getY(),tblock.getZ()))
            		{
            			String splitar[] = str.split(":");
            			int x = Integer.valueOf(splitar[0]);
            			int y = Integer.valueOf(splitar[1]);
            			int z = Integer.valueOf(splitar[2]);
            			
            			tblock = server.getBlockAt(x, y, z);
            		}
            	}
            	break;
            	
            case 340: 
                Location playerLoc = new Location();
                Location tempLoc = player.getLocation();
                blox = new HitBlox(player, 300, 0.3);
                if(blox.getTargetBlock() != null)
                {
                    for(int i = 0; i < 100; i++)
                    {
                        int cur = etc.getServer().getBlockAt(blox.getCurBlock().getX(), blox.getCurBlock().getY() + i, blox.getCurBlock().getZ()).getType();
                        int above = etc.getServer().getBlockAt(blox.getCurBlock().getX(), blox.getCurBlock().getY() + i + 1, blox.getCurBlock().getZ()).getType();
                        if(cur == 0 && above == 0)
                        {
                            playerLoc.x = (double)blox.getCurBlock().getX() + 0.5;
                            playerLoc.y = blox.getCurBlock().getY() + i;
                            playerLoc.z = (double)blox.getCurBlock().getZ() + 0.5;
                            playerLoc.rotX = tempLoc.rotX;
                            playerLoc.rotY = tempLoc.rotY;
                            player.teleportTo(playerLoc);
                            i = 100;
                        }
                    }

                }
                break;

            case 280: 
            	//Steal
                Player WatchTarget = (Player)getTargetInFront(player, 2, 1, 0);
                if(WatchTarget != null)
                {
                    playerConsumeInHand(player);
                    Player WatchBack = (Player)getTargetInFront(WatchTarget, 2, 3, 0);
                    if (WatchBack == null)
                    { 
                    	player.sendMessage("YOU GOT RICH!");
                    }else if(WatchBack.getName() == player.getName()){
                        player.sendMessage("Oh god he spotted you...");
                    }
                }
                break;

            case 267: 
            case 268: 
            case 272: 
            case 276: 
            case 283: 
            	//PVP
                for (int i = 0; i < playerlist.size(); i++)
                {
                    if ( player == player(i) || !stats(player).pvp || !stats(player(i)).pvp || !isNear(player.getLocation(), player(i).getLocation(), meleeRange) ) {continue;}
                    playerDamage(player(i), getWeaponPVPDamage(player.getItemInHand()));
                    break;
                }

            //MonsterBattle
            default:
                Object target = getTargetInFront(player, 2, 1, 0);
                if ( target != null && target.getClass().getName().equals("Mob"))
                {
	                Mob mob = (Mob)target;
	                int mobhealth = mob.getHealth();
	                
	                if(mobhealth > 0)
	                {
	                	stats(player).hostilemobs.add(String.valueOf(mob.getId()));
	                	mobhealth -= getWeaponDamage(player.getItemInHand());
	                	mob.setHealth(mobhealth);
	                }
	                
	                if(mobhealth <= 0)
	                {
	                    int last = 0;
	                    if (stats(player).monsterkills.containsKey(mob.getName()))
	                    {
	                        last = stats(player).monsterkills.get(mob.getName());
	                    }
	                    stats(player).monsterkills.put(mob.getName(), last + 1);
	                    stats(player).hostilemobs.remove(mob.getName());
	                }
	                
                }
                
                if ( target != null && target.getClass().getName().equals("NPC"))
                {
                	NPC npc = (NPC)target;
                	npc.delete();
                	npc.broadcastMovement();
                	npc.broadcastPosition();
                }
                
                break;
            }
        }

        public boolean onChat(Player player, String message)
        {
        	for (Player p:playerlist)
        	{
        		if (isNear(p.getLocation(), player.getLocation(), 30)) {p.sendMessage("<"+player.getName()+"> "+message);}
        	}
            return true;
        }

        public void onPlayerMove(Player player, Location from, Location to)
        {
            int bt 	= server.getBlockAt( LtB(player.getX()), (int)Math.round(player.getY()), LtB(player.getZ())).getType();
            int dbt = server.getBlockAt( LtB(player.getX()), (int)Math.round(player.getY()) - 1, LtB(player.getZ())).getType();
            
            //Unable to swim
            int tmptype = server.getBlockAt(LtB(to.x), LtB(to.y), LtB(to.z)).getType();
            if (tmptype == 9 && !server.isTimerExpired("Spawnprotection-" + player.getName())) {stats(player).underwater = true;}else{stats(player).underwater = false; stats(player).breath = 0;}
            
            if (server.isTimerExpired("Spawnprotection-" + player.getName()))
            {
                if (tmptype == 9 && server.getHighestBlockY(LtB(to.x), LtB(to.z)) - 1 == LtB(to.y))
                {
                    int x = LtB(to.x);
                    int z = LtB(to.z);
                    int tx = x;
                    int tz = z;
                    int ty = 0;
                    
                    for (int i = 0; i < 100; i++)
                    {
                        tx = x + i;
                        tz = z;
                        ty = server.getHighestBlockY(tx, tz) - 1;
                        int tmp = server.getBlockAt(tx, ty, tz).getType();
                        if(tmp != 9 && tmp != 8 && tmp != 0) {break;}
                        
                        tx = x - i;
                        tz = z;
                        ty = server.getHighestBlockY(tx, tz) - 1;
                        tmp = server.getBlockAt(tx, ty, tz).getType();
                        if(tmp != 9 && tmp != 8 && tmp != 0) {break;}
                        
                        tx = x;
                        tz = z - i;
                        ty = server.getHighestBlockY(tx, tz) - 1;
                        tmp = server.getBlockAt(tx, ty, tz).getType();
                        if(tmp != 9 && tmp != 8 && tmp != 0) {break;}
                        
                        tx = x;
                        tz = z + i;
                        ty = server.getHighestBlockY(tx, tz) - 1;
                        tmp = server.getBlockAt(tx, ty, tz).getType();
                        if(tmp != 9 && tmp != 8 && tmp != 0) {break;}
                    }

                    Location tmp = to;
                    tmp.x = BtL(tx);
                    tmp.z = BtL(tz);
                    tmp.y = ty + 1;
                    stats(player).fallingDamage = 0;
                    player.teleportTo(tmp);
                }
            }
            
            //Falling Damage
            if (stats(player).reputation < 5 && !server.isTimerExpired("Spawnprotection-" + player.getName()))
            {
                if(dbt == 0 && to.y < from.y)
                {
                    stats(player).fallingDamage += from.y - to.y;
                }
                else
                {
                	player.sendMessage(""+stats(player).fallingDamage);
                    if(stats(player).fallingDamage - 3 > 0 && dbt != 8 && dbt != 9)
                    {
                        playerDamage(player, (int)Math.round(stats(player).fallingDamage) - 3);
                    }
                    stats(player).fallingDamage = 0;
                }
            }
            
            //s0001
            String id = "s0001";
            if( !(Boolean)stats(player).objectivesStatus.get(id) && (bt == 8 || bt == 9) )
            {
                player.sendMessage("Ohhhh... so fresh");
                stats(player).objectivesStatus.put(id, true);
                playerCompleteObjective(player, getObjectiveFromID(id));
            }
            
            //s0002
            id = "s0002";
            if( !(Boolean)stats(player).objectivesStatus.get(id) && (bt == 10 || bt == 11) )
            {
                stats(player).objectivesStatus.put(id, true);
                playerCompleteObjective(player, getObjectiveFromID(id));
            }
            
            //s010X
            for (int i = 1; i <= 6; i++)
            {
                id = ("s010"+i);
                Objective obj = getObjectiveFromID(id);
                if (!stats(player).objectives.contains(obj.id))
                {
                    int distance = (Integer)stats(player).objectivesStatus.get(id) + 1;
                    stats(player).objectivesStatus.put(id, distance);
                    
                    if(distance >= obj.amount)
                    {                    	
                    	playerCompleteObjective(player, getObjectiveFromID(id));
                    }
                }
            }

            //On Fire!
            if(!stats(player).onfire)
            {
                if(bt == 10 || bt == 11)
                {
                    server.setTimer("OnFire-"+player.getName(), 280);
                    stats(player).onfire = true;
                    player.sendMessage("\247c!!!!\2474you are LITERARY on FIRE\247c!!!!");
                }
            } 
            else
            {
	            if(bt == 8 || bt == 9)
	            {
	                stats(player).onfire = false;
	                player.sendMessage("Ahhh... sweet sweet water...");
	            }
            }
        }

        public boolean onCommand(Player player, String split[]) 
        {
        	
        	//Prints stats
        	if(split[0].equalsIgnoreCase("/stats"))
        	{
        		player.sendMessage("\2476Your stats:");
        		player.sendMessage((new StringBuilder("\247eXP: \247f")).append(stats(player).XP).toString());
        		player.sendMessage((new StringBuilder("\247eHealth: \247f")).append(stats(player).health).toString());
        		player.sendMessage((new StringBuilder("\247eAppetite: \247f")).append(stats(player).getAppetiteStatus(statStarvation)).toString());
        		player.sendMessage((new StringBuilder("\247eReputation: \247f")).append(stats(player).reputationName).toString());
        		player.sendMessage((new StringBuilder("\247eKills: \247f")).append(stats(player).monsterkills.toString()).toString());
        		player.sendMessage((new StringBuilder("\247eDeployed: \247f")).append(stats(player).deployedblocks.toString()).toString());
        		return true;
        	}
        	
        	//GetHouse repots
        	if(split[0].equalsIgnoreCase("/houses"))
        	{
        		for(House house:houses)
        		{
        		  player.sendMessage(house.owner+" "+house.blockCount);
        		}
        		return true;
        	}
        	
        	if(split[0].equalsIgnoreCase("/get"))
        	{
        		int i = 0;
	        	for(House house:houses)
	        	{
	        		if (split.length > 1)
	        		{
		        		 if (i == Integer.valueOf(split[1])) {house.getReport(player);}
		        		 i++;
	        		}else{
	        			player.sendMessage(""+house.hashCode());
	        		}
	        	}
        		return true;
        	}
        	
        	//Shows Professions
        	if(split[0].equalsIgnoreCase("/profs"))
        	{
        		player.sendMessage("\2476Your professions:");
        		player.sendMessage((new StringBuilder("\247eFishing: \247f")).append(stats(player).profFishing).toString());
        		return true;
        	}
        	
        	//Teleports home
        	if(split[0].equalsIgnoreCase("/home"))
        	{
        		if(!stats(player).cdhome)
        		{
        			stats(player).cdhome = true;
        			server.setTimer((new StringBuilder("HomeCooldown-")).append(player.getName()).toString(), 24000);
        			server.setTimer((new StringBuilder("Spawnprotection-")).append(player.getName()).toString(), 28);
        			return false;
        		}
        		else
        		{
        			player.sendMessage("\247cCooldown is still running...");
        			return true;
        		}
        	}
        	
        	//Sets home
        	if(split[0].equalsIgnoreCase("/sethome"))
        	{
        		if(playerRequire(player, 331, sethomePrize))
        		{
        			playerConsume(player, 331, sethomePrize);
        			return false;
        		}
        		else
        		{
        			player.sendMessage("\247cYou don't have enough redstone!");
        			return true;
        		}
        	}
        	
        	//Set Time
        	if(split[0].equalsIgnoreCase("/time"))
        	{
        		if(!player.isAdmin())
        		{
        			player.sendMessage("\247cYou don't have permission to do that!");
        			return true;
        		}
        		else
        		{
        			return false;
        		}
        	}
        	
        	//Unlock all Professions and Prestiges
        	if(split[0].equalsIgnoreCase("/unlock"))
        	{
        		if(split.length > 1)
        		{
        			stats(player).prohibitedblocks.remove(Integer.valueOf(split[1]));
        			player.sendMessage((new StringBuilder("You unlocked: ")).append((String)items.get(Integer.valueOf(split[1]))).toString());
        			return true;
        		} else
        		{
        			stats(player).prohibitedblocks.clear();
        			stats(player).reputation = 10;
        			player.sendMessage("You are free from any restrictions! & You gained all Reputation Ranks!");
        			return true;
        		}
        	}
        	
        	//Set PVP
        	if(split[0].equalsIgnoreCase("/pvp"))
        	{
        		if(split.length > 1)
        		{
        			if(split[1].equalsIgnoreCase("on"))
        			{
        				stats(player).pvp = true;
        				player.sendMessage("You\247a ACTIVATED \247fPVP Combat!");
        				return true;
        			}
        			if(split[1].equalsIgnoreCase("off"))
        			{
        				stats(player).pvp = false;
        				player.sendMessage("You\2474 DEACTIVATED \247fPVP Combat...");
        				return true;
        			}
        		} else
        		{
        			if(stats(player).pvp)
        				player.sendMessage("You can receive and deal damage to other players! ");
        			else
        				player.sendMessage("You are safe from PVP harm!");
        			return true;
        		}
        	}
        	
        	//Reset all Stats
        	if(split[0].equalsIgnoreCase("/rs"))
        	{
        		player.sendMessage("\2474Resetting Stats...");
        		stats.put(player, new PlayerStats(player));
        		savePlayerstats(player);
        		return true;
        	}
        	
        	//New Wave
        	if(split[0].equalsIgnoreCase("/west"))
        	{
        		new ThreadWave(player);
        		return true;
        	}
        	
        	if(split[0].equalsIgnoreCase("/yy"))
        	{
        		/*player.sendMessage(""+player.getY());
        		server.setBlockAt(20, LtB(player.getX()), (int)player.getY(), LtB(player.getZ()));*/
        		
                double rot_x = (player.getRotation() + 90) % 360;

                int Ox = (int) Math.round(1 * Math.cos(Math.toRadians(rot_x)));
                int Oz = (int) Math.round(1 * Math.sin(Math.toRadians(rot_x)));
                
                player.sendMessage(""+Ox+""+Oz);
        		return true;
        	}
        	
        	if(split[0].equalsIgnoreCase("/ww"))
        	{
        		if (moveth != null) {moveth.running = false; testnpc.delete(); testnpc.untrack(player); freeze(500);}
        		testnpc = new NPC("",playerCenter(player.getX()),player.getY(),playerCenter(player.getZ()),(float)player.getRotation(),(float)player.getPitch(),1);
        		//moveth = new ThreadNPCMovement(testnpc);
        		return true;
        	}
        	
        	if(split[0].equalsIgnoreCase("/q1"))
        	{
        		if (moveth != null) {moveth.running = false; testnpc.delete(); testnpc.untrack(player); freeze(500);}
        		testnpc = new NPC("",playerCenter(player.getX()),player.getY(),playerCenter(player.getZ()),(float)player.getRotation(),(float)player.getPitch(),1);
        		moveth = new ThreadNPCPath(testnpc,tmpTo,player);
        		return true;
        	}
        	
        	if(split[0].equalsIgnoreCase("/q2"))
        	{
        		tmpTo = new Location(LtB(player.getX()),(int)player.getY(),LtB(player.getZ()));
        		return true;
        	}
        	
        	if(split[0].equalsIgnoreCase("/work"))
        	{

        		
        		for (int i=-100;i<100;i++)
        		{
        			for (int j=-100;j<100;j++)
        			{
        				for (int l=-20;l<20;l++)
        				{
        					if (server.getBlockAt(LtB(player.getX())+i, LtB(player.getY())+l, LtB(player.getZ())+j).getType() == 17)
        					{
        						player.sendMessage("FountTree");
        		        		testnpc = new NPC("",LtB(player.getX())+i+1, LtB(player.getY())+l, LtB(player.getZ())+j+1,(float)player.getRotation(),(float)player.getPitch(),1);
        		        		testnpc.broadcastMovement();
        		        		testnpc.broadcastPosition();
        					}
        				}
        			}
        		}
        		
        		return true;
        	}
        	
        	if(split[0].equalsIgnoreCase("/qq"))
        	{
        		String str = "";
        		for (int type : transparentBlocks)
        		{
        			str += items.get(type) + " ";
        		}
        		player.sendMessage(str);
        	
        		int x = LtB(player.getX());
        		int z = LtB(player.getZ());
        		
        		player.sendMessage(""+server.getHighestBlockY(x, z)+" "+getHighestBlockY(x, z));
        		
        		player.sendMessage(""+((player.getRotation()+90)%360));
        		return true;
        	}
        	
        	//Build House
        	if(split[0].equalsIgnoreCase("/aaa"))
        	{

        	}
        	
        	//LeakCheck
        	if(split[0].equalsIgnoreCase("/ho"))
        	{
        		house = player.getLocation();
        		new ThreadBuildHouse(player, LtB(house.x), LtB(house.y), LtB(house.z));
        		return true;
        	}
        	
        	//Get Current Pos
        	if(split[0].equalsIgnoreCase("/pos"))
        	{
        		player.sendMessage((new StringBuilder()).append(LtB(player.getX())).append(" ").append(LtB(player.getY())).append(" ").append(LtB(player.getZ())).toString());
        		return true;
        	}
        	
        	//Redistribute Blocks
        	if(split[0].equalsIgnoreCase("/sblock"))
        	{
        		boolean sameblock = true;
        		int x = 0;
        		int y = 0;
        		int z = 0;
        		int spawntype = 2;
        		for(int j = 1; j < 200; j++)
        		{
        			int type = 0;
        			for(int blockover = 20; type != spawntype || !transparentBlocks.contains(Integer.valueOf(blockover)); blockover = server.getBlockAt(x, y + 1, z).getType())
        			{
        				x = LtB(player.getX());
        				y = LtB(player.getY()) - 1;
        				z = LtB(player.getZ());
        				x += randSign(rand(140));
        				y += randSign(rand(40));
        				z += randSign(rand(140));
        				type = server.getBlockAt(x, y, z).getType();
        				if(!sameblock && !transparentBlocks.contains(Integer.valueOf(type)))
        					type = spawntype;
        			}
        			
        			server.setBlockAt(spawntype, x, y + 1, z);
        		}
        		
        		return true;
        	}
        	
        	
        	if(split[0].equalsIgnoreCase("/test"))
        	{
        		String location = "MMMsurvival\\Models\\spongeguy.txt";
        		if((new File(location)).exists())
        			try
        		{
        				Scanner scanner = new Scanner(new File(location));
        				ArrayList<String> model = new ArrayList<String>();
        				while (scanner.hasNextLine()) {model.add(scanner.nextLine());}
        				
        				scanner.close();
        				if(model.size() > 0)
        				{
        					int x = LtB(player.getX());
        					int y = LtB(player.getY()) + model.size() + 1;
        					int z = LtB(player.getZ());
        					int way = rand(2);
        					int sign = (int)Math.pow(-1, rand(2) + 1);
        					//player.sendMessage((new StringBuilder()).append(way).append(sign).toString());
        					for (String Twline:model)
        					{
        						for(int i = 0; i < Twline.length(); i++)
        							if(Twline.charAt(i) != ' ' && Twline.charAt(i) != '_')
        								if(way == 1)
        									server.setBlockAt(19, x + i * sign, y, z);
        								else
        									server.setBlockAt(19, x, y, z + i * sign);
        						
        						y--;
        					}
        				}
        		}
        		catch(Exception e)
        		{
        			System.out.println((new StringBuilder("Exception while reading ")).append(location).toString());
        		}
        		return true;
        	}
        	
        	if(split[0].equalsIgnoreCase("/kill"))
        	{
        		playerKill(player);
        		return true;
        	}
        	
        	if(split[0].equalsIgnoreCase("/god"))
        	{
        		stats(player).god = !stats(player).god;
        		player.sendMessage((new StringBuilder("God mode: ")).append(stats(player).god).toString());
        		return true;
        	}
        	
        	if(split[0].equalsIgnoreCase("/del"))
        	{
        		HashSet<PlayerBlock> blocks = playerBlocks.get(player.getName());
        		Iterator<PlayerBlock> it = blocks.iterator();
        		ArrayList<PlayerBlock> removes = new ArrayList<PlayerBlock>();
        		PlayerBlock p;
        		for(; it.hasNext(); removes.add(p))
        		{
        			p = it.next();
        			server.setBlockAt(0, p.x, p.y, p.z);
        			try
        			{
        				//if(p.type != 0)
        					//player.giveItem(p.type, 1);
        			}
        			catch(Exception exception) { }
        		}
        		
        		for(int i = 0; i < removes.size(); i++)
        			blocks.remove(removes.get(i));
        		
        		return true;
        	}
        	
        	if(split[0].equalsIgnoreCase("/warp"))
        		if(isNearBlock(player.getLocation(), 84, 4))
        		{
        			if(split.length == 1)
        			{
        				String stringy = "\2479Warps: \247f ";
        				for(int i = 0; i < warps.size(); i++)
        					stringy = (new StringBuilder(String.valueOf(stringy))).append(((Warp)warps.get(i)).Name).append(" ").toString();
        				
        				player.sendMessage(stringy);
        				return true;
        			}
        			for(int i = 0; i < warps.size(); i++)
        				if(split[1].equalsIgnoreCase(((Warp)warps.get(i)).Name))
        					if(playerRequire(player, 331, warpPrize))
        					{
        						playerConsume(player, 331, warpPrize);
        						player.teleportTo(((Warp)warps.get(i)).Location);
        						return true;
        					} else
        					{
        						player.sendMessage("\247cYou don't have enough redstone!");
        						return true;
        					}
        			
        			player.sendMessage("\247cThere is no such warpname!");
        			return true;
        		} else
        		{
        			player.sendMessage("\247cYou need to stand on a teleporter!");
        			return true;
        		}
        	
        	if(split[0].equalsIgnoreCase("/setwarp"))
        		if(isNearBlock(player.getLocation(), 84, 4))
        		{
        			if(split.length < 2)
        			{
        				player.sendMessage("\247cCorrect usage is: /setwarp [warpname]");
        				return true;
        			}
        			for(int i = 0; i < warps.size(); i++)
        				if(split[1].equalsIgnoreCase(((Warp)warps.get(i)).Name))
        				{
        					player.sendMessage("\2474Already exists!");
        					return true;
        				}
        			
        			Warp warp = new Warp();
        			warp.Name = split[1];
        			warp.Location = player.getLocation();
        			if(split.length == 3)
        				warp.Group = split[2];
        			else
        				warp.Group = "";
        			warps.add(warp);
        			UpdateWarps();
        			saveWarps();
        			player.sendMessage((new StringBuilder("\2472")).append(warp.Name).append("\247a").append(" succesfully set!").toString());
        			return true;
        		} else
        		{
        			player.sendMessage("\247cYou can only set Warps on Teleporter!");
        			return true;
        		}
        	
        	if(split[0].equalsIgnoreCase("/removewarp"))
        	{
        		if(!player.isAdmin())
        		{
        			player.sendMessage("\247cYou don't have permission to do that!");
        			return true;
        		}
        		if(isNearBlock(player.getLocation(), 84, 4))
        		{
        			if(split.length < 2)
        			{
        				player.sendMessage("\247cCorrect usage is: /removewarp [warpname]");
        				return true;
        			}
        			for(int i = 0; i < warps.size(); i++)
        				if(split[1].equalsIgnoreCase(((Warp)warps.get(i)).Name))
        				{
        					warps.remove(warps.get(i));
        					UpdateWarps();
        					saveWarps();
        					player.sendMessage((new StringBuilder("\2472")).append(((Warp)warps.get(i)).Name).append("\247a").append(" succesfully deleted!").toString());
        					return true;
        				}
        			
        			player.sendMessage("\247cWarp not found...");
        			return true;
        		} else
        		{
        			player.sendMessage("\247cYou can only set Warps on Teleporter!");
        			return true;
        		}
        	}
        	
        	if(split[0].equalsIgnoreCase("/eat"))
        	{
        		if(!player.isAdmin())
        		{
        			player.sendMessage("\247cYou don't have permission to do that!");
        			return true;
        		}
        		if(split.length > 1)
        		{
        			stats(player).appetite = Integer.valueOf(split[1]).intValue();
        			player.sendMessage((new StringBuilder("Setting appetite to ")).append(split[1]).toString());
        		} else
        		{
        			stats(player).appetite = 0;
        			player.sendMessage("You have eaten!");
        		}
        		return true;
        	}
        	
        	if(split[0].equalsIgnoreCase("/heal"))
        		if(!player.isAdmin())
        		{
        			player.sendMessage("\247cYou don't have permission to do that!");
        			return true;
        		} else
        		{
        			stats(player).heal(100);
        			return true;
        		}
        	
        	if(split[0].equalsIgnoreCase("/threads"))
        	{
        		String stringy = "Playerthreads: ";
        		for(int i = 0; i < playerThreads.size(); i++)
        			stringy = (new StringBuilder(String.valueOf(stringy))).append(getCName(((ThreadPlayer)playerThreads.get(player(i))).player)).append("  ").toString();
        		
        		player.sendMessage(stringy);
        		return true;
        	} else
        	{
        		return false;
        	}
        }
        
        public boolean onBlockCreate(Player player, Block blockPlaced, Block blockClicked, int itemInHand)
        {
        	if (blockPlaced.getType() == 58)
        		return true;
        	
        	//Unlock&&Lock Doors
        	if (blockClicked.getType() == 64)
        	{
        		if (server.isTimerExpired("LockUnlockDoor-"+player.getName()))
        		{
        			House tmphouse = isPartOfAHouse(blockClicked,false);
        			if (tmphouse != null)
        			{
        				tmphouse.openDoors();
        				houses.remove(tmphouse);
        				player.sendMessage("Unlocking...");
        			}else{
                		int x = blockClicked.getX();
                		int y = blockClicked.getY();
                		int z = blockClicked.getZ();
                		
                		buildhouse = new ThreadBuildHouse(player,x,y,z);
        			}
        			server.setTimer("LockUnlockDoor-"+player.getName(),0);
        		}else{
        			server.setTimer("LockUnlockDoor-"+player.getName(),5);
        		}
        		
        	}
        	
        	if (!(isPartOfAHouse(blockClicked,true) == null))
        		return true;
        	
            if(stats(player).prohibitedblocks.contains(Integer.valueOf(blockPlaced.getType())))
                return true;
            
            Block underBlock = server.getBlockAt(blockPlaced.getX(), blockPlaced.getY() - 1, blockPlaced.getZ());
            if(underBlock.getType() == 0 && stats(player).reputation < 2 && blockPlaced.getType() != -1)
            {
                player.sendMessage("You don't have enough reputation!");
                return true;
            }
            addPlayerblock(player, blockPlaced);
            int last = 0;
            if(stats(player).deployedblocks.containsKey(Integer.valueOf(blockPlaced.getType())))
                last = ((Integer)stats(player).deployedblocks.get(Integer.valueOf(blockPlaced.getType()))).intValue();
            stats(player).deployedblocks.put(Integer.valueOf(blockPlaced.getType()), Integer.valueOf(last + 1));
            return false;
        }

        public boolean onBlockDestroy(Player player, Block block)
        {        	
        	if (block.getType() == 54 && block.getStatus()==0) {
        		Chest cblock = (Chest)server.getComplexBlock(block.getX(), block.getY(), block.getZ());
        		String str = "I:";

        		for (hj item:cblock.getArray())
        		{
        			if (item != null){
        				str += " [Type:"+item.c+" Count:"+item.a+"] ";
        			}
        		}
        		player.sendMessage(str);
        	}
        	
        	if (!(isPartOfAHouse(block,false) == null))
        		return true;
        	
            if(block.getStatus() == 1 && stats(player).prohibitedblocks.contains(Integer.valueOf(block.getType())))
                return true;
            
            PlayerBlock playerblock = getPlayerblock(block);
            if(block.getStatus() == 3 && playerblock != null)
            {
                (playerBlocks.get(playerblock.owner)).remove(playerblock);
                int last = 0;
                if(stats(player).deployedblocks.containsKey(Integer.valueOf(block.getType())))
                    last = ((Integer)stats(player).deployedblocks.get(Integer.valueOf(block.getType()))).intValue();
                stats(player).deployedblocks.put(Integer.valueOf(block.getType()), Integer.valueOf(last - 1));
            }
            int druber = server.getBlockAt(block.getX(), block.getY() + 1, block.getZ()).getType();
            if(block.getStatus() == 3 && (druber == 8 || druber == 9))
            {
                player.sendMessage((new StringBuilder()).append(druber).toString());
                server.setBlockAt(0, block.getX(), block.getY() + 1, block.getZ());
            }

            return false;
        }

        Item cur;
        HitBlox blox;
        final MMMsurvival this$0;

        public MMMsurvivalListener()
        {
            this$0 = MMMsurvival.this;
            //super();
        }
    }

    public class ThreadBuildHouse extends Thread
    {
    	//Thread Stuff
        boolean running = false;
        Player player;
        
        House house;
        
        //Global Stuff
        ArrayList<HashSet<String>> rooms = new ArrayList<HashSet<String>>();
        HashSet<String> doors = new HashSet<String>();
        ArrayList<String> entries = new ArrayList<String>();

        //Queue/Stack Stuff
        Stack<String> doorStack = new Stack<String>();
        
        public ThreadBuildHouse(Player player, int x, int y, int z)
        {
        	this.player = player;
        	house = new House(player.getName());
        	
        	//Begin
        	doorStack.push(XYZ(x,y,z));
            
            running = true;
            this.start();
        }
        
        public void run()
        {
        	//Do for every added Doorblock
        	while (!doorStack.empty())
        	{
        		String door = doorStack.pop(); 
        		
        		String split[] = door.split(":");
        		int x = Integer.valueOf(split[0]);
        		int y = Integer.valueOf(split[1]);
        		int z = Integer.valueOf(split[2]);
            	
            	//Try to fixate the whole door, not only one block
            	for (String Tdoor:getAdjacentDoorBlocks(x,y,z)) {doors.add(Tdoor);}
            	
            	//Get the direction of the door and only process these rooms
            	int tx = 0, ty = 0, tz = 0, tt = 0;
            	if (transparentBlocks.contains(server.getBlockAt(x, y, z+1).getType()) && transparentBlocks.contains(server.getBlockAt(x, y, z-1).getType()))
            	{
            		tx = x; ty = y; tz = z+1; tt = server.getBlockAt(tx,ty,tz).getType();
            		if (!roomProcessed(tx,ty,tz,tt)) {makeRoom(door,tx,ty,tz);}
            		
            		tx = x; ty = y; tz = z-1; tt = server.getBlockAt(tx,ty,tz).getType();
            		if (!roomProcessed(tx,ty,tz,tt)) {makeRoom(door,tx,ty,tz);}
            	}
            	else if (transparentBlocks.contains(server.getBlockAt(x+1, y, z).getType()) && transparentBlocks.contains(server.getBlockAt(x-1, y, z).getType()))
            	{
            		tx = x+1; ty = y; tz = z; tt = server.getBlockAt(tx,ty,tz).getType();
            		if (!roomProcessed(tx,ty,tz,tt)) {makeRoom(door,tx,ty,tz);}
            		
            		tx = x-1; ty = y; tz = z; tt = server.getBlockAt(tx,ty,tz).getType();
            		if (!roomProcessed(tx,ty,tz,tt)) {makeRoom(door,tx,ty,tz);}
            	}
        	}
        	        	
        	if (house.blockCount > 1)
        	{
        		player.sendMessage("Locking...");
        		
        		house.setDoors(doors);
        		house.setEntries(entries);
        		
        		if (!house.isSomebodyInside())
        		{
        			//Close Doors
        			house.closeDoors();
        			
	        		//Test a Random Block against other houses
	        		int size = house.rooms.get(0).blocks.size();
	        		int item = new Random().nextInt(size); // In real life, the Random object should be rather more shared than this
	        		int i = 0;
	        		
	        		String str = "";
	        		for(String obj : house.rooms.get(0).blocks)
	        		{
	        		    if (i == item)
	        		        str = obj;
	        		    i = i + 1;
	        		}
	        		
	        		if (str!="")
	        		{
		        		String tbsplit[] = str.split(":");
		        		int x = Integer.valueOf(tbsplit[0]);
		        		int y = Integer.valueOf(tbsplit[1]);
		        		int z = Integer.valueOf(tbsplit[2]);
		        		int t = Integer.valueOf(tbsplit[3]);
		        		
		        		for (House ehouse:houses)
		        		{
		        			if (ehouse.isPartOf(x, y, z, t))
		        			{
			        			houses.remove(ehouse);
		        			}
		        		}
		        		houses.add(house);
		        		house.getReport(player);
	        		}
        		}else{
        			player.sendMessage("Get out of there!");
        		}
        	}else{
        		player.sendMessage("No room");
        	}
        }
        
        public void makeRoom(String bdoor, int bx, int by, int bz)
        {        	
    		String bsplit[] = bdoor.split(":");
    		int dx = Integer.valueOf(bsplit[0]);
    		int dy = Integer.valueOf(bsplit[1]);
    		int dz = Integer.valueOf(bsplit[2]);
        	
        	boolean leaked = false;
        	
        	//Temp
        	String tmpEntry = "";
        	ArrayList<String> tmpDoors = new ArrayList<String>();
        	Stack<String> blockStack = new Stack<String>();
        	
        	//Globals
        	int blockCount = 0;
        	HashSet<String> blocks = new HashSet<String>();
        	HashMap<Integer,Integer> itemList = new HashMap<Integer,Integer>();
        	HashMap<Integer,Integer> blockList = new HashMap<Integer,Integer>();
        	
        	blockStack.push(XYZ(bx,by,bz));
        	
        	//Trying to fixate the whole door not only one doorblock
        	for (String Tdoor:getAdjacentDoorBlocks(dx,dy,dz))
        	{
        		blocks.add(Tdoor+":64");
        		String split[] = Tdoor.split(":");
        	}
        	
        	Step:
        	while (blockStack.size() > 0 && !leaked)
        	{
        		//Get Block-Coordinates
        		String block = blockStack.pop();
        		
        		String split[] = block.split(":");
        		int x = Integer.valueOf(split[0]);
        		int y = Integer.valueOf(split[1]);
        		int z = Integer.valueOf(split[2]);
        		int t = server.getBlockAt(x, y, z).getType();
        		
        		//Quit if block was already processed
        		if (blocks.contains(XYZT(x,y,z,t))) {continue Step;}
                blocks.add(XYZT(x,y,z,t));
                
                //If the block under current block is the highest, it has to be leaked, break the while
                if (server.getBlockAt(x, y-1, z).getY() == getHighestBlockY(x, z))
                {
                	leaked = true;
                	break Step;
                }
                
                //Get if that Block is solid
                if (!transparentBlocks.contains(t))
                {
                	//Add to blockList
                	int blockListCount = 0;
                	if (blockList.containsKey(t))
                	{
                		blockListCount = blockList.get(t);
                	}
                	blockListCount++;
                	blockList.put(t, blockListCount);
                	
                	//If its a door, add it in a temporary collection
                	if (t == 64 && server.getBlockAt(x, y-1, z).getType() != 64 && server.getBlockAt(x, y-1, z).getType() != 0)
                	{
                		if (server.getBlockAt(x, y+1, z).getType() == 64)
                		{
                			if (!doors.contains(XYZ(x,y,z)))
                			{
                				tmpDoors.add(XYZ(x,y,z));
                			}
                		}
                	}
                	
                	//If its a chest, add its contents to items
                	if (t == 54)
                	{
                		Chest chest = (Chest)server.getComplexBlock(x,y,z);
                		for (hj item:chest.getArray())
                		{
                			if (item != null){
                            	int itemListCount = item.a;
                            	if (itemList.containsKey(item.c))
                            	{
                            		itemListCount += itemList.get(item.c);
                            	}
                            	itemList.put(item.c, itemListCount);
                			}
                		}
                	}
                	
                	continue Step;
                }
                
                //Add all blocks around that block if its a transparent one
                blockStack.push( XYZ((x+1),y,z) );
                blockStack.push( XYZ((x-1),y,z) );
                blockStack.push( XYZ(x,y,(z+1)) );
                blockStack.push( XYZ(x,y,(z-1)) );
                blockStack.push( XYZ(x,(y+1),z) );
                blockStack.push( XYZ(x,(y-1),z) );
                blockCount++;
                
        	}
        	
        	//If there is no leak
        	if (!leaked)
	        {
	        	//ping( "Room size: "+blockCount);
	        	rooms.add(blocks);
	        	house.addRoom( new Room(blockCount,blocks,blockList,itemList) );
	        		
	        	//Add the temporary doors finally to the doorqueue
	        	for (String tmpDoor:tmpDoors)
	        	{
	        		doorStack.push(tmpDoor);
	        	}
        	}else{
        		entries.add(tmpEntry);
        		//ping("Leaked!");
        	}
        	
        }

        public boolean roomProcessed(int x, int y, int z, int t)
        {
        	for (HashSet<String> room : rooms) {if (room.contains(XYZT(x,y,z,t))) {return true;}}
        	return false;
        }
        
        public void ping(String txt)
        {
            server.getPlayer("Mendel").sendMessage(txt);
        }
    }

    public class ThreadNPCMovement extends Thread
    {
        NPC npc;
        boolean running = false;
        float lastrot;
        
        public ThreadNPCMovement(NPC npc)
        {
        	this.npc = npc;
        	lastrot = npc.getRotation();
        	
            running = true;
            this.start();
            npc.broadcastPosition();
        }
        
        public void run()
        {
        	double way = 1;
        	while (running)
        	{
                double rot_x = (npc.getRotation()+90)%360;

                int Ox = (int) Math.round(1 * Math.cos(Math.toRadians(rot_x)));
                int Oz = (int) Math.round(1 * Math.sin(Math.toRadians(rot_x)));
                
                double Wx = way*Ox;
                double Wz = way*Oz;
                
                int x = LtB(npc.getX())+Ox;
                int z = LtB(npc.getZ())+Oz;
                	
                double y = npc.getY();

                /*if(etc.getServer().getBlockAt(x,y+1,z).getType() == 0 && etc.getServer().getBlockAt(x,y,z).getType() != 0)
                {
                	move(Wx,1,Wz);
                }else if(etc.getServer().getBlockAt(x,y-1,z).getType() == 0){
                	move(Wx,-1,Wz);
                }else if(etc.getServer().getBlockAt(x,y,z).getType() == 0 && etc.getServer().getBlockAt(x,y+1,z).getType() == 0){
                	move(Wx,0,Wz);
                }else{
                	npc.setRotation((float) (npc.getRotation()+randSign(90)));
                }*/
                
                if (!transparentBlocks.contains(etc.getServer().getBlockAt(x, (int)y, z).getType()))
                {
                	boolean done = false;
                	int mx=0,my=0,mz=0,rata=0;
                	
                	//
                	
                	if (!done) {
                	mx = LtB(npc.getX())+1; my = (int)y; mz = LtB(npc.getZ()); rata = 270;
                	if ((rata != ((lastrot)+180)%360) && transparentBlocks.contains(etc.getServer().getBlockAt(mx,my,mz).getType()))
                	{npc.setRotation(rata); done=true;} }
                	
                	if (!done) {
                	mx = LtB(npc.getX())-1; my = (int)y; mz = LtB(npc.getZ()); rata = 90;
                	if ((rata != ((lastrot)+180)%360) && transparentBlocks.contains(etc.getServer().getBlockAt(mx,my,mz).getType()))
                	{npc.setRotation(rata); done=true;} }
                	
                	if (!done) {
                	mx = LtB(npc.getX()); my = (int)y; mz = LtB(npc.getZ()+1); rata = 360;
                	if ((rata != ((lastrot)+180)%360) && transparentBlocks.contains(etc.getServer().getBlockAt(mx,my,mz).getType()))
                	{npc.setRotation(rata); done=true;} }
                	
                	if (!done) {
                	mx = LtB(npc.getX()); my = (int)y; mz = LtB(npc.getZ()-1); rata = 180;
                	if ((rata != ((lastrot)+180)%360) && transparentBlocks.contains(etc.getServer().getBlockAt(mx,my,mz).getType()))
                	{npc.setRotation(rata); done=true;} }
                	
                	
                	npc.broadcastPosition();
                	npc.broadcastMovement();
                	//server.getPlayer("Mendel").sendMessage(""+x+" "+y+" "+z);
                	//server.getPlayer("Mendel").sendMessage("Move");
                }else{
                	move(Wx,0,Wz);
                	//server.getPlayer("Mendel").sendMessage("Rotate");
                }
                freeze(100);
        	}
        }
        
        public void move(double x, double y, double z)
        {
        	//double off = 0.5;
        	//x += off; z += off;
        	npc.teleportTo(npc.getX()+x, npc.getY()+y, npc.getZ()+z, npc.getRotation(), npc.getPitch());
        	npc.broadcastPosition();
        	npc.broadcastMovement();
        	
        	lastrot = npc.getRotation();
        	//server.getPlayer("Mendel").sendMessage(""+BtL(x)+y+BtL(z));
        }
    }
    
    public class ThreadNPCPath extends Thread
    {
    	boolean running = false;
    	boolean goal = false;
    	
    	Player player;
        NPC npc;
        Location to;
        Stack<String> walked = new Stack<String>();
        
        public ThreadNPCPath(NPC npc, Location to, Player player)
        {
        	this.npc = npc;
        	this.to = to;
        	this.player = player;
        	
            running = true;
            this.start();
        }
        
        public void run()
        {
        	step(LtB(npc.getX()),(int)npc.getY(),LtB(npc.getX()));
        	player.sendMessage("Tracking completed");
        	for (String str:walked)
        	{
        		String split[] = str.split(":");
        		move(Integer.valueOf(split[0]),Integer.valueOf(split[1]),Integer.valueOf(split[2]));
        	}
        }
        
        public void step(int x,int y,int z)
        {
        	if (walked.contains(x+":"+y+":"+z)) {return;}
        	walked.add(x+":"+y+":"+z);
        	
        	if (server.getBlockAt(x, y, z).getType()!=0) {return;}
        	if (x==to.x && z==to.z) {goal = true;}
        	
        	Block tblock = new Block(20,x,y,z);
        	addPlayerblock(player,tblock);
        	server.setBlock(tblock);
        	
        	if (!goal) {step(x+1,y,z);}
        	if (!goal) {step(x-1,y,z);}
        	if (!goal) {step(x,y,z+1);}
        	if (!goal) {step(x,y,z-1);}
        }
        
        public void move(double x, double y, double z)
        {
        	npc.teleportTo(x, npc.getY(), z, npc.getRotation(), npc.getPitch());
        	npc.broadcastPosition();
        	npc.broadcastMovement();
        	freeze(1000);
        }
    }
    
    public class ThreadFishing extends Thread
    {

        public void run()
        {
            player.sendMessage(" ");
            stats(player).profFishingFished++;
            if(stats(player).profFishingFished >= 1 + 2 * stats(player).profFishing)
            {
                stats(player).profFishingFished = 0;
                stats(player).profFishing++;
                player.sendMessage((new StringBuilder("\2476[PROFESSION] \247f")).append(stats(player).profFishing - 1).append(" -> ").append(stats(player).profFishing).toString());
                player.sendMessage(" ");
            }
            for(int w = (10 + rand(40)) * 100; w >= 0 && !abort; w -= 100)
                freeze(100);

            if(!abort)
            {
                catchy = false;
                abort = true;
                player.sendMessage("\2477*Something moves*");
                for(int w = 800; w >= 0 && !catchy; w -= 100)
                    freeze(100);

                if(catchy)
                {
                    int fishthrow = stats(player).profFishing + rand(100);
                    if(fishthrow >= 90)
                    {
                        String stringy = "\247aI found something!\247f It's a";
                        if(rand(101) == 100)
                        {
                            player.giveItem(260, 1);
                            stringy = (new StringBuilder(String.valueOf(stringy))).append("\247bn Apple! OMGOMGOMGOMGOMG!!!!111").toString();
                        } else
                        {
                            player.giveItem(39, 1);
                            stringy = (new StringBuilder(String.valueOf(stringy))).append("\247f Mushroom!").toString();
                        }
                        player.sendMessage(stringy);
                    } else
                    {
                        ArrayList<String> msgs = new ArrayList<String>();
                        msgs.add("These goddamn fish!!!!");
                        msgs.add("Ahhh... just my imagination...");
                        msgs.add("I lost track...");
                        msgs.add("Strange...");
                        msgs.add("NOW COME ON!");
                        msgs.add("It was right there...");
                        msgs.add("Let's start over...");
                        msgs.add("Was that a... MUSHROOM?");
                        msgs.add("Doo-Bee-Doo-Bee-Doo!");
                        msgs.add("What are YOU looking at?");
                        msgs.add("Are there ANY fish in this place?");
                        msgs.add("Nearly had it!");
                        player.sendMessage((String)msgs.get(rand(msgs.size())));
                    }
                } else
                {
                    player.sendMessage("\2477*It got away*");
                }
            } else
            {
                player.sendMessage("\2477*You missed*");
            }
            fishingthreads.remove(player.getName());
        }

        Player player;
        boolean catchy;
        boolean abort;
        final MMMsurvival this$0;

        public ThreadFishing(Player player)
        {
            this$0 = MMMsurvival.this;
            //super();
            this.player = player;
            start();
            abort = false;
        }
    }

    public class ThreadPlayer extends Thread
    {

        Player player;
        boolean running;
        long time;
        long survivedtime = 0;
        long survived;
        
        public ThreadPlayer(Player player)
        {
            running = true;
            time = 0;
            this.player = player;
            start();
        }
    	
        public void run()
        {
            while(running) 
            {
                freeze(100);
                time += 100L;
                if(server.getPlayer(player.getName()) == null)
                    running = false;
                if(elapsed(0x1d4c0L))
                    savePlayerstats(player);
                
                //server.setBlockAt(20, LtB(player.getX()), (int)player.getY(), LtB(player.getZ()));

                //Check
                if (buildhouse != null && buildhouse.isAlive())
                {
                	player.sendMessage("Not null");
                }
                
                if(elapsed(5000L))
                {
                    for(Iterator<Objective> iterator = objectives.iterator(); iterator.hasNext();)
                    {
                        Objective o = (Objective)iterator.next();
                        if(!stats(player).objectives.contains(o.id))
                        {
                            if(o.category.equals("gather") && o.check(getItemAmount(player, o.type)))
                                playerCompleteObjective(player, o);
                            if(o.category.equals("kill"))
                            {
                                int amount = 0;
                                String mob = parseMobNames(o.type);
                                if(stats(player).monsterkills.containsKey(mob))
                                    amount = ((Integer)stats(player).monsterkills.get(mob)).intValue();
                                if(o.check(amount))
                                    playerCompleteObjective(player, o);
                            }
                            if(o.category.equals("deploy") && o.check(((Integer)stats(player).deployedblocks.get(Integer.valueOf(o.type))).intValue()))
                                playerCompleteObjective(player, o);
                        }
                    }

                }
                
                //Always Day
                if (elapsed(10000))
                {
                	server.setTime(2000);
                }
                
                //Nights Survived Increase
                if (elapsed(1000))
                {
                	long servertime = (server.getTime()%24000);
                	if (servertime > 13000 && servertime < 24000)
                	{
                		if (survivedtime > 0) { survived += (servertime-survivedtime);}
                		survivedtime = servertime;
                	}else{
                		if (survivedtime > 0)
                		{ 
                			if (survived >= 9500)
	                		{
	                			player.sendMessage("You survived a night!");
	                			stats(player).survived++;
	                			savePlayerstats(player);
	                		}
                		}
                		
                		survivedtime = 0;
                		survived = 0;
                	}
                }
                
                //Drowning
                if (elapsed(1000))
                {
                    if (stats(player).reputation < 4 && stats(player).underwater)
                    {
                    	stats(player).breath++;
                    	
                    	int interval = (breathCapacity+1-stats(player).breath);
                    	if (interval <= 0)
                    	{
                    		playerDamage(player,4);
                    	}
                    	else
                    	{
		                    String str = Colors.Blue+"Air:"+Colors.Gray+" ";
		                    for (int i=0; i < interval; i++)
		                    {
		                    	int r = rand(3);
		                    	if (r == 1) {str += "o";}
		                    	if (r == 2) {str += "O";}
		                    	if (r == 0) {str += "0";}
		                    }
		                    player.sendMessage(str);
                    	}
                    }
                }
                
                //Mob-Attack
                try
                {
                    List<Mob> servermobs = Collections.synchronizedList(server.getMobList());
                    double durRange = 20D;
                    for(int i = 0; i < servermobs.size(); i++)
                    {
                        Mob mob = (Mob)servermobs.get(i);
                        if(Math.abs(mob.getX() - player.getX()) <= meleeRange && Math.abs(mob.getY() - player.getY()) <= meleeRange && Math.abs(mob.getZ() - player.getZ()) <= meleeRange)
                        {
                            Location loc = new Location();
                            loc.x = ((Mob)servermobs.get(i)).getX();
                            loc.y = ((Mob)servermobs.get(i)).getY();
                            loc.z = ((Mob)servermobs.get(i)).getZ();
                            if(hasSight(loc, player.getLocation()))
                            {
                                    if(((Mob)servermobs.get(i)).getName() == "Zombie" && elapsed(500L))
                                        playerDamage(player, 1);
                                    else if(((Mob)servermobs.get(i)).getName() == "Skeleton" && elapsed(1000L))
                                        playerDamage(player, 3);
                                    else if(((Mob)servermobs.get(i)).getName() == "Spider" && elapsed(2000L))
                                        playerDamage(player, 7);
                                    else if(((Mob)servermobs.get(i)).getName() == "Creeper" && elapsed(200L))
                                        playerDamage(player, 2);
                                    else if(stats(player).hostilemobs.contains(String.valueOf(mob.getId())))
                                    	if(elapsed(2000L))
                                            playerDamage(player, 1);
                                    
                                    continue;
                            }
                        }
                        if(elapsed(1000) && mob.getHealth() > 10 && Math.abs(mob.getX() - player.getX()) <= durRange && Math.abs(mob.getY() - player.getY()) <= durRange && Math.abs(mob.getZ() - player.getZ()) <= durRange)
                        {
                            List<Block> mobBlocks = getNearBlocks(new Location(mob.getX(), mob.getY(), mob.getZ()), 0, 1, player.getName());
                            if(mobBlocks != null && mobBlocks.size() > 0)
                            {
                                Block block = (Block)mobBlocks.get(rand(mobBlocks.size()));
                                PlayerBlock p = getPlayerblock(block);
                                if(p.breakable)
                                {
                                    p.durability--;
                                    if(p.durability <= 0)
                                    {
                                        server.setBlockAt(0, block.getX(), block.getY(), block.getZ());
                                        (playerBlocks.get(player.getName())).remove(p);
                                    }
                                }
                            }
                        }
                    }

                }
                catch(Exception e)
                {
                    player.sendMessage((new StringBuilder("Concurrent! (")).append(e.getMessage()).append(")").toString());
                }
                if(elapsed(0x1d4c0L))
                    stats(player).heal(2);
                if(stats(player).cdhome && !server.isTimerExpired((new StringBuilder("HomeCooldown-")).append(player.getName()).toString()))
                {
                    player.sendMessage("\247bHome recharged!");
                    stats(player).cdhome = false;
                }
                if(stats(player).onfire)
                {
                    if(!server.isTimerExpired((new StringBuilder("OnFire-")).append(player.getName()).toString()))
                    {
                        stats(player).onfire = false;
                        player.sendMessage("\2477You eventually extinguish the  flames");
                    }
                    if(elapsed(1000L))
                        playerDamage(player, 1);
                }
                if(elapsed(1000L))
                {
                    stats(player).appetite++;
                    int appetite = stats(player).appetite;
                    if(appetite >= statStarvation - 900 && appetite < statStarvation && appetite % 60 == 0)
                        player.sendMessage(stats(player).getAppetiteStatus(statStarvation));
                    else
                    if(appetite >= statStarvation / 2 + statStarvation / 4 && appetite % 120 == 0)
                        player.sendMessage(stats(player).getAppetiteStatus(statStarvation));
                    else
                    if(appetite >= statStarvation / 2 && appetite % 600 == 0)
                        player.sendMessage(stats(player).getAppetiteStatus(statStarvation));
                    else
                    if(appetite == statStarvation / 5)
                        player.sendMessage(stats(player).getAppetiteStatus(statStarvation));
                    if(stats(player).appetite >= statStarvation)
                        playerKill(player);
                }
            }
            playerThreads.remove(this);
        }

        public boolean elapsed(long t)
        {
            return time % t == 0L;
        }
    }

    public class ThreadWave extends Thread
    {

        public void run()
        {
            int minrange = 3;
            int maxrange = 12;
            int lx = LtB(player.getX());
            int lz = LtB(player.getZ());
            ArrayList<Mob> mobs = new ArrayList<Mob>();
            for(int i = 0; i < 40; i++)
            {
                int tx = lx + randSign(minrange + rand(maxrange - minrange));
                int tz = lz + randSign(minrange + rand(maxrange - minrange));
                int ty = server.getHighestBlockY(tx, tz);
                mobs.add(new Mob("Zombie", new Location(tx, ty, tz)));
                ((Mob)mobs.get(i)).spawn();
            }

        }

        Player player;
        boolean running;
        final MMMsurvival this$0;

        public ThreadWave(Player player)
        {
            this$0 = MMMsurvival.this;
            //super();
            running = true;
            this.player = player;
            start();
        }
    }


    public MMMsurvival()
    {
        statStarvation = 5500;
        meleeRange = 3;
        warpPrize = 13;
        sethomePrize = 45;
        breathCapacity = 4;
    }

    public void initialize()
    {
        etc.getLoader().addListener(PluginLoader.Hook.ARM_SWING, listener, this, PluginListener.Priority.CRITICAL);
        etc.getLoader().addListener(PluginLoader.Hook.COMMAND, listener, this, PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(PluginLoader.Hook.CHAT, listener, this, PluginListener.Priority.MEDIUM);
        etc.getLoader().addListener(PluginLoader.Hook.LOGIN, listener, this, PluginListener.Priority.LOW);
        etc.getLoader().addListener(PluginLoader.Hook.DISCONNECT, listener, this, PluginListener.Priority.LOW);
        etc.getLoader().addListener(PluginLoader.Hook.BLOCK_CREATED, listener, this, PluginListener.Priority.LOW);
        etc.getLoader().addListener(PluginLoader.Hook.BLOCK_DESTROYED, listener, this, PluginListener.Priority.CRITICAL);
        etc.getLoader().addListener(PluginLoader.Hook.PLAYER_MOVE, listener, this, PluginListener.Priority.MEDIUM);
    }

    public void enable()
    {
        echo("Enabling...");
        playerlist = server.getPlayerList();
        for(int i = 0; i < playerlist.size(); i++)
        {
            loadPlayerstats(player(i));
            playerThreads.put(player(i), new ThreadPlayer(player(i)));
        }

        warps = new ArrayList<Warp>();
        String location = "MMMsurvival\\warps.txt";
        if((new File(location)).exists())
            try
            {
                Scanner scanner;
                for(scanner = new Scanner(new File(location)); scanner.hasNextLine();)
                {
                    String line = scanner.nextLine();
                    if(!line.startsWith("#") && !line.equals(""))
                    {
                        String split[] = line.split(":");
                        if(split.length >= 4)
                        {
                            Location loc = new Location();
                            loc.x = Double.parseDouble(split[1]);
                            loc.y = Double.parseDouble(split[2]);
                            loc.z = Double.parseDouble(split[3]);
                            if(split.length == 6)
                            {
                                loc.rotX = Float.parseFloat(split[4]);
                                loc.rotY = Float.parseFloat(split[5]);
                            }
                            Warp warp = new Warp();
                            warp.Name = split[0];
                            warp.Location = loc;
                            warps.add(warp);
                        }
                    }
                }

                scanner.close();
            }
            catch(Exception e)
            {
                System.out.println((new StringBuilder("Exception while reading ")).append(location).toString());
            }
            
        UpdateWarps();
        
        location = "MMMsurvival\\items.txt";
        if((new File(location)).exists())
            try
            {
                Scanner scanner;
                for(scanner = new Scanner(new File(location)); scanner.hasNextLine();)
                {
                    String line = scanner.nextLine();
                    if(!line.startsWith("#") && !line.equals(""))
                    {
                        String split[] = line.split(":");
                        if(split.length >= 2)
                        {
                            if(split.length == 2)
                                items.put(Integer.valueOf(split[1]), split[0]);
                            if(split.length == 3)
                                items.put(Integer.valueOf(split[1]), split[0]);
                            if(split.length == 4)
                            {
                                items.put(Integer.valueOf(split[1]), split[0]);
                                String transparent = String.valueOf(split[3]);
                                if(transparent.length() >= 1 && Integer.valueOf(split[3]).intValue() == 1)
                                    transparentBlocks.add(Integer.valueOf(split[1]));
                            }
                        }
                    }
                }

                scanner.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getLocalizedMessage()+"NOGGER");
            }
            
        location = "MMMsurvival\\mobs.txt";
        if((new File(location)).exists())
            try
            {
                Scanner scanner;
                for(scanner = new Scanner(new File(location)); scanner.hasNextLine();)
                {
                    String line = scanner.nextLine();
                    if(!line.startsWith("#") && !line.equals(""))
                    {
                        String split[] = line.split(":");
                        if(split.length >= 2)
                            mobs.put(Integer.valueOf(split[1]), split[0]);
                    }
                }

                scanner.close();
            }
            catch(Exception e)
            {
                System.out.println((new StringBuilder("Exception while reading ")).append(location).toString());
            }
            
        location = "MMMsurvival\\objectives.txt";
        if((new File(location)).exists())
            try
            {
                Scanner scanner;
                for(scanner = new Scanner(new File(location)); scanner.hasNextLine();)
                {
                    String line = scanner.nextLine();
                    if(!line.startsWith("#") && !line.equals(""))
                    {
                        String split[] = line.split(":");
                        if(split.length >= 6)
                            objectives.add(new Objective(split[0], split[1], Integer.valueOf(split[2]).intValue(), Integer.valueOf(split[3]).intValue(), Integer.valueOf(split[4]).intValue(), split[5]));
                    }
                }

                scanner.close();
            }
            catch(Exception e)
            {
                System.out.println((new StringBuilder("Exception while reading ")).append(location).toString());
            }
            
        location = "MMMsurvival\\reputations.txt";
        if((new File(location)).exists())
            try
            {
                Scanner scanner;
                for(scanner = new Scanner(new File(location)); scanner.hasNextLine();)
                {
                    String line = scanner.nextLine();
                    if(!line.startsWith("#") && !line.equals(""))
                    {
                        String split[] = line.split(":");
                        if(split.length >= 3)
                            reputations.add(new Reputation(Integer.valueOf(split[0]).intValue(), split[2], Long.valueOf(split[1]).longValue()));
                    }
                }

                scanner.close();
            }
            catch(Exception e)
            {
                System.out.println((new StringBuilder("Exception while reading ")).append(e.getMessage()).toString());
            }
            
        loadPlayerblocks();
        loadHouses();
        /*uploadFile("/var/www/html/minecraft/armory", "MMMsurvival\\items.txt", "items.txt");
        uploadFile("/var/www/html/minecraft/armory", "MMMsurvival\\objectives.txt", "objectives.txt");
        uploadFile("/var/www/html/minecraft/armory", "MMMsurvival\\mobs.txt", "mobs.txt");*/
    }

    public void disable()
    {
    	if (moveth != null)
    	{
	    	testnpc.delete();
	    	testnpc.untrack(server.getPlayer("Mendel"));
	    	moveth.running = false;
    	}
    	
        echo("Disabling...");
        playerlist = server.getPlayerList();
        for(int i = 0; i < playerlist.size(); i++)
        {
            savePlayerstats(player(i));
            ((ThreadPlayer)playerThreads.get(player(i))).running = false;
        }

        savePlayerblocks();
        saveWarps();
        saveHouses();
    }

    public PlayerBlock getPlayerblock(Block block)
    {
        Set<String> player = playerBlocks.keySet();
        for (Iterator<String> it = player.iterator(); it.hasNext();)
        {
            String curPlayer = (String)it.next();
            HashSet<PlayerBlock> blocks = playerBlocks.get(curPlayer);
            for (Iterator<PlayerBlock> pit = blocks.iterator(); pit.hasNext();)
            {
                PlayerBlock curBlock = (PlayerBlock)pit.next();
                if(curBlock.x == block.getX() && curBlock.y == block.getY() && curBlock.z == block.getZ())
                    return curBlock;
            }

        }

        return null;
    }

    public void Knockback(Player player)
    {
        int x = (int)Math.round(player.getX() + randDouble(1.0D) + 0.5D);
        int y = (int)Math.round(player.getY());
        int z = (int)Math.round(player.getZ() + randDouble(1.0D) + 0.5D);
        if(server.getBlockAt(x, y, z).getType() == 0)
        {
            player.setX(x);
            player.setZ(z);
        }
    }

    public PlayerStats stats(Player player)
    {
        return (PlayerStats)stats.get(player);
    }

    public void playerDropInventory(Player player)
    {
        for(int i = 0; i < 36; i++)
        {
            Item cur = player.getInventory().getItemFromSlot(i);
            if(cur != null)
            {
                player.getInventory().removeItem(cur);
                player.giveItemDrop(cur.getItemId(), cur.getAmount());
            }
        }

        player.getInventory().updateInventory();
        for(int i = 0; i < 4; i++)
        {
            Item cur = player.getCraftingTable().getItemFromSlot(i);
            if(cur != null)
            {
                player.getCraftingTable().removeItem(cur);
                player.giveItemDrop(cur.getItemId(), cur.getAmount());
            }
        }

        player.getCraftingTable().updateInventory();
        for(int i = 0; i < 4; i++)
        {
            Item cur = player.getEquipment().getItemFromSlot(i);
            if(cur != null)
            {
                player.getEquipment().removeItem(cur);
                player.giveItemDrop(cur.getItemId(), cur.getAmount());
            }
        }

        player.getEquipment().updateInventory();
    }

    public boolean isNear(Location l1, Location l2, double i)
    {
        double x1 = l1.x;
        double y1 = l1.y;
        double z1 = l1.z;
        double x2 = l2.x;
        double y2 = l2.y;
        double z2 = l2.z;
        return Math.abs(x1 - x2) <= i && Math.abs(y1 - y2) <= i && Math.abs(z1 - z2) <= i;
    }

    public boolean isInRange(Player player, Location loc, int range)
    {
        return false;
    }

    public boolean isNearBlock(Location tloc, int type, int radi)
    {
        Location loc = new Location(tloc.x, tloc.y, tloc.z);
        loc.x -= radi;
        loc.y -= radi;
        loc.z -= radi;
        int x = 0;
        int y = 0;
        int z = 0;
        int Tradi = radi * 2 + 1;
        for(x = 0; x < Tradi; x++)
        {
            if(server.getBlockAt(LtB(loc.x) + x, LtB(loc.y), LtB(loc.z)).getType() == type)
                return true;
            for(y = 0; y < Tradi; y++)
            {
                if(server.getBlockAt(LtB(loc.x) + x, LtB(loc.y) + y, LtB(loc.z)).getType() == type)
                    return true;
                for(z = 0; z < Tradi; z++)
                    if(server.getBlockAt(LtB(loc.x) + x, LtB(loc.y) + y, LtB(loc.z) + z).getType() == type)
                        return true;

            }

        }

        return false;
    }

    public List<Block> getNearBlocks(Location tloc, int type, int radi)
    {
        return getNearBlocks(tloc, type, radi, null);
    }

    public List<Block> getNearBlocks(Location tloc, int type, int radi, String player)
    {
        List<Block> blocks = new ArrayList<Block>();
        Block block = null;
        Location loc = new Location(tloc.x, tloc.y, tloc.z);
        loc.x -= radi;
        loc.y -= radi;
        loc.z -= radi;
        int x = 0;
        int y = 0;
        int z = 0;
        int Tradi = radi * 2 + 1;
        for(x = 0; x < Tradi; x++)
        {
            block = server.getBlockAt(LtB(loc.x) + x, LtB(loc.y), LtB(loc.z));
            if(type != 0 && block.getType() == type && !blocks.contains(block))
                blocks.add(block);
            if(type == 0 && block.getType() != type && !blocks.contains(block) && isPlayerOwning(block, player))
                blocks.add(block);
            for(y = 0; y < Tradi; y++)
            {
                block = server.getBlockAt(LtB(loc.x) + x, LtB(loc.y) + y, LtB(loc.z));
                if(type != 0 && block.getType() == type && !blocks.contains(block))
                    blocks.add(block);
                if(type == 0 && block.getType() != type && !blocks.contains(block) && isPlayerOwning(block, player))
                    blocks.add(block);
                for(z = 0; z < Tradi; z++)
                {
                    block = server.getBlockAt(LtB(loc.x) + x, LtB(loc.y) + y, LtB(loc.z) + z);
                    if(type != 0 && block.getType() == type && !blocks.contains(block))
                        blocks.add(block);
                    if(type == 0 && block.getType() != type && !blocks.contains(block) && isPlayerOwning(block, player))
                        blocks.add(block);
                }

            }

        }

        return blocks;
    }

    public boolean isPlayerOwning(Block block, String player)
    {
        HashSet<PlayerBlock> blocks = playerBlocks.get(player);
        if(blocks != null)
        {
            for(Iterator<PlayerBlock> pit = blocks.iterator(); pit.hasNext();)
            {
                PlayerBlock curBlock = (PlayerBlock)pit.next();
                if(curBlock.x == block.getX() && curBlock.y == block.getY() && curBlock.z == block.getZ())
                    return true;
            }

        }
        return false;
    }

    public int LtB(double d)
    {
        return (int)Math.round(d - 0.5D);
    }

    public double BtL(int i)
    {
        return (double)i + 0.5;
    }

    public boolean hasSight(Location ent1, Location ent2)
    {
        int x1 = LtB(ent1.x);
        int y1 = LtB(ent1.y);
        int z1 = LtB(ent1.z);
        int x2 = LtB(ent2.x);
        int y2 = LtB(ent2.y);
        for(int z2 = LtB(ent2.z); x1 != x2 || y1 != y2 || z1 != z2; z1 = approach(z1, z2))
        {
            int abc = server.getBlockAt(x1, y1, z1).getType();
            if(!transparentBlocks.contains(Integer.valueOf(abc)))
                return false;
            x1 = approach(x1, x2);
            y1 = approach(y1, y2);
        }

        return true;
    }

    public int approach(int i1, int i2)
    {
        if(i1 != i2)
        {
            if(i1 - i2 < 0)
                return i1 + 1;
            else
                return i1 - 1;
        } else
        {
            return i1;
        }
    }

    public Player player(int i)
    {
        return (Player)playerlist.get(i);
    }

    public void playerDamage(Player player, int amount)
    {
        if(!stats(player).god)
        {
            int oldhealth = stats(player).health;
            stats(player).health -= amount;
            player.sendMessage((new StringBuilder("Health: ")).append(oldhealth).append(" -> ").append(stats(player).health).toString());
            if(stats(player).health <= 0)
                playerKill(player);
        }
    }

    public void playerKill(Player player)
    {
        player.sendMessage("You died!");
        playerDropInventory(player);
        stats(player).revive();
        playerRandomSpawn(player);
        Warp home = new Warp();
        home.Location = player.getLocation();
        home.Group = "";
        home.Name = player.getName();
        etc.getInstance().changeHome(home);
    }

    public void playerRandomSpawn(Player player)
    {
        Location loc = player.getLocation();
        loc.y = 100D;
        loc.x = randSign(500 + rand(1000));
        loc.z = randSign(500 + rand(1000));
        server.setTimer((new StringBuilder("Spawnprotection-")).append(player.getName()).toString(), 60);
        player.teleportTo(loc);
    }

    public String getCName(Player player)
    {
        return (new StringBuilder(String.valueOf(player.getColor()))).append(player.getName()).append("\247f").append(" ").toString();
    }

    public void playerConsumeInHand(Player player)
    {
        Item item = player.getInventory().getItemFromId(player.getItemInHand());
        item.setAmount(item.getAmount() - 1);
        player.getInventory().addItem(item);
        player.getInventory().updateInventory();
    }

    public boolean playerRequire(Player player, int item, int amount)
    {
        return getItemAmount(player, item) >= amount;
    }

    public int getItemAmount(Player player, int item)
    {
        int amount = 0;
        for(int i = 0; i < 40; i++)
        {
            Item cur = player.getInventory().getItemFromSlot(i);
            if(cur != null && cur.getItemId() == item)
                amount += cur.getAmount();
        }

        return amount;
    }

    public String parseNames(String str)
    {
        String split[] = str.split("%");
        if(split != null && split.length > 1)
            return (new StringBuilder(String.valueOf(split[0]))).append((String)items.get(Integer.valueOf(split[1]))).append(split[2]).toString();
        split = str.split("#");
        if(split != null && split.length > 1)
            return (new StringBuilder(String.valueOf(split[0]))).append((String)mobs.get(Integer.valueOf(split[1]))).append(split[2]).toString();
        else
            return str;
    }

    public String parseMobNames(int type)
    {
        return (String)mobs.get(Integer.valueOf(type));
    }

    public void playerConsume(Player player, int item, int amount)
    {
        for(int i = 0; i < amount; i++)
        {
            Item cur = player.getInventory().getItemFromId(item);
            cur.setAmount(cur.getAmount() - 1);
            player.getInventory().addItem(cur);
            player.getInventory().updateInventory();
        }

    }

    public void loadPlayerstats(Player player)
    {
        try
        {
            String xml = "";
            try
            {
                BufferedReader in = new BufferedReader(new FileReader((new StringBuilder("MMMsurvival\\Saves\\")).append(player.getName()).append(".xml").toString()));
                String str;
                while((str = in.readLine()) != null) 
                    xml = (new StringBuilder(String.valueOf(xml))).append(str).toString();
                in.close();
            }
            catch(IOException ioexception) { }
            Unmarshaller unmarshaller = JAXBContext.newInstance(PlayerStats.class).createUnmarshaller();
            PlayerStats obj = (PlayerStats)unmarshaller.unmarshal(new StringReader(xml));
            obj.player = player;
            
            stats.put(player, obj);
        }
        catch(Exception e)
        {
            player.sendMessage((new StringBuilder(String.valueOf(Server))).append("Welcome new Player!").toString());
            stats.put(player, new PlayerStats(player));
            playerRandomSpawn(player);
        }
    }

    public void savePlayerstats(Player player)
    {
        try
        {
            PlayerStats playerstats = stats(player);
            JAXBContext context = JAXBContext.newInstance(PlayerStats.class);
            Marshaller m = context.createMarshaller();
            
            m.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
            FileOutputStream fs = new FileOutputStream((new StringBuilder("MMMsurvival\\Saves\\")).append(player.getName()).append(".xml").toString());
            m.marshal(playerstats, fs);
            uploadFile("/var/www/html/minecraft/armory/player", (new StringBuilder("MMMsurvival\\Saves\\")).append(player.getName()).append(".xml").toString(), (new StringBuilder(String.valueOf(player.getName()))).append(".xml").toString());
        }
        catch(Exception e)
        {
            System.out.println((new StringBuilder("Error saving : ")).append(e.getMessage()).toString());
        }
    }

    public void loadHouses()
    {
    	File player = new File("MMMsurvival\\Houses\\");
    	if (player.exists()){
	    	File[] directories = player.listFiles();
	    	for (int jndex = 0; jndex < directories.length; jndex++)
	    	{
	    		
	        File house = new File(directories[jndex].toString());
	        File[] files = house.listFiles();
	    	for (int index = 0; index < files.length; index++)
	    	{
	    		
	        try
	        {
	            String xml = "";
	            try
	            {
	                BufferedReader in = new BufferedReader(new FileReader(files[index].toString()));
	                String str;
	                while((str = in.readLine()) != null) 
	                    xml = (new StringBuilder(String.valueOf(xml))).append(str).toString();
	                in.close();
	            }
	            catch(IOException ioexception) { }
	            
	            Unmarshaller unmarshaller = JAXBContext.newInstance(House.class).createUnmarshaller();
	            
				House obj = (House)unmarshaller.unmarshal(new StringReader(xml));
	            houses.add(obj);
	        }
	        catch(Exception e)
	        {
	        	this.houses = new HashSet<House>();
	        	System.out.println("New house!");
	        }
	    	}
	    	}
    	}
    }
    
    public void loadPlayerblocks()
    {
        try
        {
            FileInputStream fis = new FileInputStream("MMMsurvival\\playerblocks.dat");
            ObjectInputStream o = new ObjectInputStream(fis);

			@SuppressWarnings("unchecked")
			HashMap<String, HashSet<PlayerBlock>> load = (HashMap<String, HashSet<PlayerBlock>>) o.readObject();
            playerBlocks = load;
            o.close();
        }
        catch(Exception e)
        {
            System.out.println();
            playerBlocks = new HashMap<String, HashSet<PlayerBlock>>();
        }
    }

    public void savePlayerblocks()
    {
        try
        {
            FileOutputStream f_out = new FileOutputStream("MMMsurvival\\playerblocks.dat");
            ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
            obj_out.writeObject(playerBlocks);
        }
        catch(Exception e)
        {
            System.out.println((new StringBuilder("ERROAR")).append(e.getMessage()).toString());
        }
    }

    public void saveHouses()
    {
    	//Delete house folder
//    	String directory = ("MMMsurvival\\Houses");
//    	File dir = new File(directory);
//    	if (deleteDir(dir)){log("Deleting houses...");}else{log("Error while deleting houses!");}
//    	
//    	dir.mkdir();
    	
    	//Save houses with its hashname in playerfolder
    	for (House house:houses)
    	{
    		
    	String name = (house.owner+"\\"+String.valueOf(house.hashCode()));
    	String directory = ("MMMsurvival\\Houses\\"+house.owner);
    	File dir = new File(directory);
    	if (!dir.exists())
    	{
    		dir.mkdir();
    	}

        try
        {
            JAXBContext context = JAXBContext.newInstance(House.class);
            Marshaller m = context.createMarshaller();
            
            m.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
            FileOutputStream fs = new FileOutputStream("MMMsurvival\\Houses\\"+name+".xml");
            m.marshal(house, fs);
            //uploadFile("/var/www/html/minecraft/armory/player", (new StringBuilder("MMMsurvival\\Saves\\")).append(player.getName()).append(".xml").toString(), (new StringBuilder(String.valueOf(player.getName()))).append(".xml").toString());
        }
        catch(Exception e)
        {
            System.out.println((new StringBuilder("Error saving : ")).append(e.getMessage()).toString());
        }
        
    	}
        /*try
        {
            JAXBContext context = JAXBContext.newInstance(HashSet.class);
            Marshaller m = context.createMarshaller();
            
            m.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
            FileOutputStream fs = new FileOutputStream("MMMsurvival\\houses.xml");
            m.marshal(houses, fs);
        }
        catch(Exception e)
        {
            System.out.println((new StringBuilder("Error saving : ")).append(e.getMessage()).toString());
        }*/
    }
    
    public void log(String string)
    {
		System.out.println(string);
	}

	public void UpdateWarps()
    {
        for(int i = 0; i < warps.size(); i++)
        {
            for(int j = 0; j < playerlist.size(); j++)
            {
                int t = 35;
                if(Math.abs(((Warp)warps.get(i)).Location.x - player(j).getX()) <= (double)t && Math.abs(((Warp)warps.get(i)).Location.y - player(j).getY()) <= (double)t && Math.abs(((Warp)warps.get(i)).Location.z - player(j).getZ()) <= (double)t && !isNearBlock(((Warp)warps.get(i)).Location, 84, 4))
                {
                    server.useConsoleCommand((new StringBuilder("say ")).append(((Warp)warps.get(i)).Location.x).append(" ").append(((Warp)warps.get(i)).Location.y).append(" ").append(((Warp)warps.get(i)).Location.z).toString());
                    warps.remove(i);
                }
            }

        }

    }

    public void saveWarps()
    {
        StringBuilder builder = null;
        Warp warp = null;
        try
        {
            PrintWriter out = new PrintWriter(new FileWriter("warps.txt"));
            for(int i = 0; i < warps.size(); i++)
            {
                warp = (Warp)warps.get(i);
                builder = new StringBuilder();
                builder.append(warp.Name);
                builder.append(":");
                builder.append(warp.Location.x);
                builder.append(":");
                builder.append(warp.Location.y);
                builder.append(":");
                builder.append(warp.Location.z);
                builder.append(":");
                builder.append(warp.Location.rotX);
                builder.append(":");
                builder.append(warp.Location.rotY);
                out.println(builder.toString());
            }

            out.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }

    public int getWeaponPVPDamage(int w)
    {
    	if(w == 268)
        	return 10;
       	if(w == 272)
       		return 20;
       	if(w == 267)
       		return 30;
       	if(w == 283)
       		return 60;
       	if(w == 276)
       		return 40;

        return 1;
    }
    
    public int getWeaponDamage(int w)
    {
    	if(w == 268)
    		return 2;
        if(w == 272)
        	return 2;
        if(w == 267)
        	return 3;
        if(w == 283)
            return 4;
        if(w == 276)
        	return 3;
        
        return 1;
    }

    public int rand(int i)
    {
        return randomGenerator.nextInt(i);
    }

    public double randDouble(double d)
    {
        int i = (int)d * 1000;
        i = randomGenerator.nextInt(i);
        if(randomGenerator.nextBoolean())
            i *= -1;
        return (double)i / 1000D;
    }

    public int randSign(int i)
    {
        if(randomGenerator.nextBoolean())
            return i * -1;
        else
            return i;
    }

    public void echo(String str)
    {
        System.out.println((new StringBuilder("[MMMsurvival] ")).append(str).toString());
    }

    public void freeze(int i)
    {
        try
        {
            Thread.sleep(i);
        }
        catch(InterruptedException interruptedexception) { }
    }

    public void playerCompleteObjective(Player player, Objective o)
    {
        player.sendMessage((new StringBuilder("You completed ")).append(o.description).toString());
        stats(player).objectives.add(o.id);
        playerRewardXP(player, o.reward);
        savePlayerstats(player);
    }

    public void playerRewardXP(Player player, int reward)
    {
        stats(player).XP += reward;
        for(Iterator<Reputation> iterator = reputations.iterator(); iterator.hasNext();)
        {
            Reputation rep = (Reputation)iterator.next();
            if(rep.id > stats(player).reputation && stats(player).XP >= rep.xprequirement)
            {
                player.sendMessage((new StringBuilder("You reached level: ")).append(rep.id).toString());
                stats(player).reputation = rep.id;
                stats(player).reputationName = rep.description;
            }
        }

    }

    public void addPlayerblock(Player player, Block block)
    {
        if(playerBlocks.get(player.getName()) == null)
            playerBlocks.put(player.getName(), new HashSet<PlayerBlock>());
        int dur = 0;
        switch(block.getType())
        {
        case 1: // '\001'
            dur = 15;
            break;

        case 2: // '\002'
            dur = 5;
            break;

        case 3: // '\003'
            dur = 5;
            break;

        case 4: // '\004'
            dur = 15;
            break;

        case 5: // '\005'
            dur = 10;
            break;

        case 12: // '\f'
            dur = 8;
            break;

        case 13: // '\r'
            dur = 20;
            break;

        case 17: // '\021'
            dur = 10;
            break;

        case 45: // '-'
            dur = 50;
            break;
        }
        ((HashSet<PlayerBlock>)playerBlocks.get(player.getName())).add(new PlayerBlock(player, block, dur));
    }

    public void uploadFile(String path, String file, String filename)
    {
        /*FTPClient client;
        FileInputStream fis;
        client = new FTPClient();
        fis = null;
        try
        {
            client.connect("msplhs23.bon.at");
            client.login("b939117093@clock-work.at", "DmJB9aK+");
            fis = new FileInputStream(file);
            client.changeWorkingDirectory(path);
            client.storeFile(filename, fis);
            client.logout();
            client.disconnect();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }*/
    }

    public Objective getObjectiveFromID(String string)
    {
        for(Iterator<Objective> iterator = objectives.iterator(); iterator.hasNext();)
        {
            Objective o = (Objective)iterator.next();
            if(o.id.equals(string))
                return o;
        }

        return null;
    }

    public Object getTargetInFront(Player subject, int range, double splash, int mode)
    {
        List<Mob> servermobs = Collections.synchronizedList(server.getMobList());
        if(subject == null)
            return null;
        
        Location loc = subject.getLocation();
        double rot_x = (loc.rotX + 90F) % 360F;
        double rot_y = loc.rotY * -1F;
        double y_offset = 1.0D * Math.sin(Math.toRadians(rot_y));
        double x_offset = 1.0D * Math.cos(Math.toRadians(rot_x));
        double z_offset = 1.0D * Math.sin(Math.toRadians(rot_x));
        for(int i = range; i > 0; i--)
        {
            double target_x = (double)i * x_offset + loc.x;
            double target_y = (double)i * y_offset + loc.y + 1.0D;
            double target_z = (double)i * z_offset + loc.z;
            if(mode != 1)
            {
                for(Iterator<Player> iterator = playerlist.iterator(); iterator.hasNext();)
                {
                    Player player = (Player)iterator.next();
                    if(player != subject && isBetweenDouble(player.getX(), target_x, splash) && isBetweenDouble(player.getY(), target_y, 3D) && isBetweenDouble(player.getZ(), target_z, splash) && hasSight(loc, player.getLocation()))
                        return player;
                }

            }
            if(mode != 2)
            {
                for(Iterator<Mob> iterator1 = servermobs.iterator(); iterator1.hasNext();)
                {
                    Mob mob = (Mob)iterator1.next();
                    if(isBetweenDouble(mob.getX(), target_x, splash) && isBetweenDouble(mob.getY(), target_y, 4D) && isBetweenDouble(mob.getZ(), target_z, splash) && hasSight(loc, new Location(mob.getX(), mob.getY(), mob.getZ())))
                        return mob;
                }

            }

            for (NPC npc : npcs)
            {
            	if(isBetweenDouble(npc.getX(), target_x, splash) && isBetweenDouble(npc.getY(), target_y, 4D) && isBetweenDouble(npc.getZ(), target_z, splash) && hasSight(loc, new Location(npc.getX(), npc.getY(), npc.getZ())))
                    return npc;
            }
        }

        return null;
    }

    public boolean isBetweenDouble(double x1, double x2, double range)
    {
        return x2 >= x1 - range && x2 <= x1 + range;
    }

    public void buildHouse(Player player, int tx, int ty, int tz)
    {
        int x = tx;
        int z = tz;
        int blocktype = 1;
        int blockwalltype = 5;
        int rows = 10;
        int columns = 12;
        int height = 4;
        x -= columns / 2;
        z -= rows / 2;
        for(int i = 1; i < rows * columns + 1; i++)
        {
            int lx = x + i % rows;
            int lz = z;
            int ly = server.getHighestBlockY(lx, lz) - 1;
            Block block = null;
            if(i % rows != 0 && i % rows != columns - 1 && i > rows && i < rows * columns - rows)
            {
                int type = rand(3);
                if(type == 1)
                {
                    for(int j = 0; j <= height; j++)
                    {
                        block = new Block(blocktype, lx, ly + j, lz);
                        server.setBlock(block);
                        addPlayerblock(player, block);
                    }

                } else
                {
                    block = new Block(blocktype, lx, ly + height, lz);
                    if(rand(2) == 1)
                    {
                        block.setType(50);
                        block.setY(ly + (height - 1));
                        server.setBlock(block);
                        addPlayerblock(player, block);
                    }
                    block = new Block(blocktype, lx, ly + height, lz);
                    server.setBlock(block);
                    addPlayerblock(player, block);
                }
            } else
            {
                for(int j = 0; j <= height; j++)
                {
                    block = new Block(blockwalltype, lx, ly + j, lz);
                    server.setBlock(block);
                    addPlayerblock(player, block);
                }

            }
            if(i % rows == 0)
                z++;
        }

    }

    public void Error(Exception e)
    {
    	System.out.println("====================");
    	System.out.println("      Exception     ");
    	System.out.println(e.getMessage());
    	System.out.println(e.getStackTrace());
    	System.out.println(e.getCause());
    	System.out.println("====================");
	}
    
    public int getHighestBlockY(int x, int z)
    {
		for (int i = 128; i > 0; i--)
		{
			if (!transparentBlocks.contains(server.getBlockAt(x, i, z).getType()))
			{
    			return i+1;
			}
		}
		return 0;
    }

    public int getNextFloorBlock(int x, int y, int z)
    {
		for (int i = y; i > 0; i--)
		{
			if (!transparentBlocks.contains(server.getBlockAt(x, i, z).getType()))
			{
    			return i+1;
			}
		}
		return 0;
    }
    
	public double mathFixed(double x, double d)
	{
		int da = (int)(x/d);
		return (da*d);
	}

	public double playerCenter(double x)
	{
		return BtL(LtB(x));
	}

	public double mathCut(double d, int p)
	{
		int a = (int) (d*p);
		return ( (double)a/p );
	}

	public ArrayList<String> getAdjacentDoorBlocks(int x, int y, int z)
	{
		ArrayList<String> doors = new ArrayList<String>();
		y--; x--; z--;
		
		for (int fx=0;fx<=2;fx++)
		{
			for (int fz=0;fz<=2;fz++)
			{
				for (int fy=0;fy<=2;fy++)
				{
					if (server.getBlockAt((x+fx), (y+fy), (z+fz)).getType() == 64) {doors.add((x+fx)+":"+(y+fy)+":"+(z+fz));}
				}
			}
		}
		
		return doors;
	}

	public House isPartOfAHouse(Block block, boolean ignoreType)
	{
		int x = block.getX();
		int y = block.getY();
		int z = block.getZ();
		int t = block.getType();
		
		for (House house:houses)
		{
			if (ignoreType)
			{
				if (house.isPartOf(x, y, z)) {return house;}
			}else{
				if (house.isPartOf(x, y, z, t)) {return house;}
			}
		}
		return null;
	}

	public String XYZ(int x,int y,int z)
	{
		return (x+":"+y+":"+z);
	}

	public String XYZT(int x,int y,int z,int t)
	{
		return (x+":"+y+":"+z+":"+t);
	}

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    } 
	
}
