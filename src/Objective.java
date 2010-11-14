public class Objective
{

			String 	id;
	
			String	category;
			int 	type;
	    	int 	amount; 
	    	int		reward;
	    	
	    	String description;
	    	
	    	public Objective() {}
	    	public Objective(String id, String category, int key, int value, int reward, String description)
	    	{
	    		this.id				= id;
	    		this.category 		= category;
	    		this.type			= key;
	    		this.amount			= value;
	    		this.reward			= reward;
	    		this.description 	= description;
	    	}
	    	
	    	public void load(Objective obj)
	    	{
	    		this.category 		= obj.category;
	    		this.type			= obj.type;
	    		this.amount			= obj.amount;
	    		this.reward			= obj.reward;
	    		this.description 	= obj.description;
	    	}
	    	
	    	public boolean check(int playeramount)
	    	{
	    		if (playeramount >= amount)
	    		{
	    			return true;
	    		}
	    		
	    		return false;
	    	}
	    	
	    	public String toString()
	    	{
	    		//gather
	    		//kill
	    		//deploy
	    		String prefix = "";
	    		if (category.equals("kill")) {prefix = "#";}else if(category.equals("gather")) {prefix = "%";}
	    		return category + " " + amount + " " + prefix + type + prefix + " ";
	    	}
	    	
	}