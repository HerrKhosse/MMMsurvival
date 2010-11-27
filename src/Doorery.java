public class Doorery extends JobProduction
{
	
	public Doorery(House house)
	{
		super(house);
		name = "Doorery";
		
		type = 324; //Wooddoor
		amount = 1;
		
		etc.getServer().getPlayer("HerrKhosse").sendMessage("C"+house.blockCount);
	}
	
	static public boolean fits(House house)
	{
		if (house.getBlockTypeAmount(3) >= 3){return true;}
		return false;
	}
	
	public void getReport(Player player)
	{
		
		player.sendMessage("This is a doorery you fool!");
	}
	
}
