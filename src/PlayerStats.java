// Decompiled by DJ v3.11.11.95 Copyright 2009 Atanas Neshkov  Date: 11/11/2010 5:40:25 PM
// Home Page: http://members.fortunecity.com/neshkov/dj.html  http://www.neshkov.com/dj.html - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   Playerstats.java
import java.io.File;
import java.util.*;
import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="Playerstats")
@XmlRootElement(name="Playerstats")
public class PlayerStats
{

    transient Player player;
    transient Block lastblock;
    
    int health;
    int appetite;
    long XP;
    long survived;
    int reputation;
    String reputationName;
    double fallingDamage;
    int profFishing;
    int profFishingFished;
    
    int breath;
    boolean underwater;
    
    HashMap<String,Object> objectivesStatus = new HashMap<String,Object>();
    HashMap<String,Integer> monsterkills = new HashMap<String,Integer>();
    HashMap<Integer,Integer> deployedblocks = new HashMap<Integer,Integer>();
    ArrayList<Integer> prohibitedblocks = new ArrayList<Integer>();
    ArrayList<String> objectives = new ArrayList<String>();
    TreeSet<String> hostilemobs = new TreeSet<String>();
    
    boolean pvp = false;
    boolean onfire;
    boolean cdhome;
    boolean god = false;
    
	
    public PlayerStats() {}
    public PlayerStats(Player player)
    {
        this.player = player;
        reputation = 0;
        XP = 0;
        survived = 0;
        
        profFishing = 0;
        profFishingFished = 0;
        loadProhibitedList();
        loadSpecialObjectives();
        reset();
    }

    public void loadSpecialObjectives()
    {
        String location = "MMMsurvival\\objectives.txt";
        if((new File(location)).exists())
        {
            try
            {
                Scanner scanner;
                for(scanner = new Scanner(new File(location)); scanner.hasNextLine();)
                {
                    String line = scanner.nextLine();
                    if(!line.startsWith("#") && !line.equals(""))
                    {
                        String split[] = line.split(":");
                        if(split.length >= 6 && split[0].startsWith("s"))
                        {
                            if(split[1].equals("boolean")) 	{ objectivesStatus.put(split[0], new Boolean(false)); }
                            if(split[1].equals("int")) 		{ objectivesStatus.put(split[0], new Integer(0)); }
                        }
                    }
                }

                scanner.close();
            }
            catch(Exception e)
            {
                System.out.println((new StringBuilder("Exception while reading ")).append(location).toString());
            }
        }
    }

    public void unlockRandomBlueprint()
    {
        for(int cur = 0; !prohibitedblocks.contains(Integer.valueOf(cur)););
    }

    public void loadProhibitedList()
    {
        String location = "MMMsurvival\\items.txt";
        if((new File(location)).exists())
        {
            try
            {
                Scanner scanner;
                for (scanner = new Scanner(new File(location)); scanner.hasNextLine();)
                {
                    String line = scanner.nextLine();
                    if (!line.startsWith("#") && !line.equals(""))
                    {
                        String split[] = line.split(":");
                        if (split.length >= 3 && !split[1].isEmpty() && !split[2].isEmpty() && Integer.valueOf(split[2]) == 1)
                        {
                            prohibitedblocks.add(Integer.valueOf(split[1]));
                            if (Integer.valueOf(split[1]) == 3) {player.sendMessage("SOMETHINGWENTWRONG");}
                        }
                    }
                }

                scanner.close();
            }
            catch(Exception e)
            {
                Error(e);
            }
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
    
	public void reset()
    {
    	underwater = false;
    	
        appetite = 0;
        health = 20;
        onfire = false;
        cdhome = false;
        survived = 0L;
        fallingDamage = 0;
        hostilemobs = new TreeSet<String>();
        player.giveItem(50, 4);
    }

    public String getAppetiteStatus(int limit)
    {
        if(appetite >= limit - 900)
            return "\2474You're dying of hunger... eat something fast!";
        if(appetite >= limit / 2 + limit / 4)
            return "\2477You're feeling \247cextremely\2477 hungry.";
        if(appetite >= limit / 2)
            return "\2477You're feeling \247ehungry\2477.";
        if(appetite >= limit / 5)
            return "\2477You're feeling \247bslightly\2477 hungry.";
        else
            return "\2477You're feeling stuffed.";
    }

    public void setPVP(boolean v)
    {
        pvp = v;
    }

    public void heal(int i)
    {
        if(health < 20)
        {
            health += i;
            if(health > 20)
                health = 20;
            player.sendMessage((new StringBuilder("You regenerate \2476")).append(String.valueOf(i)).append("\247f").append(" Health!").toString());
        }
    }

    public void revive()
    {
        reset();
    }

    public void eat(int amount, int statStarvation)
    {
        String tempOld = getAppetiteStatus(statStarvation);
        appetite -= amount;
        if(appetite < 0)
            appetite = 0;
        player.sendMessage("\2477*NomNomNomNomNom*");
        String tempNew = getAppetiteStatus(statStarvation);
        if(tempNew != tempOld)
            player.sendMessage(tempNew);
    }
    
}
