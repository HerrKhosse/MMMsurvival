public class JobProduction extends Thread implements HouseJob
{
	//Requirements
	String name;
	
	//Produce
	int type;
	int amount;
	
	House house;
	boolean running;
	
	public JobProduction(House house)
	{
		this.house = house;
		running = true;
        this.start();
	}
	
    public void run()
    {
    	while (running)
    	{
    		freeze(1000);
    		etc.getServer().getPlayer("HerrKhosse").sendMessage("Badam");
    	}
    		
    }
    
    public String toString() {return "JobProduction-"+name;}
    
    public void quit()
    {
    	running = false;
    }
    
	public String getProduct()
	{
		return "Type:"+type+" Amount:"+amount;
	}
	
    public void freeze(int i)
    {
        try
        {
            Thread.sleep(i);
        }
        catch(InterruptedException interruptedexception) { }
    }
}
