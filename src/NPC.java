public class NPC extends NonPlayerCharacter
{
	private ep user; // ep = user object
	private gs handler; // gs = entity tracking handler
	
	public NPC(String name, double x, double y, double z, float rotation,float pitch, int itemInHand)
	{
		super(name, x, y, z, rotation, pitch, itemInHand);
	}	
	
	//v = rotation
	//m = X
	//n = Y
	//o = Z
	
	public void moveForward()
	{
        double rot_x = (user.v + 90) % 360;

        double x = 1 * Math.cos(Math.toRadians(rot_x));
        double z = 1 * Math.sin(Math.toRadians(rot_x));
        
	}
	
	public void move(float rot, double distance)
	{
        double rot_x = (rot + 90) % 360;
        
        double x = 1 * Math.cos(Math.toRadians(rot_x));
        double z = 1 * Math.sin(Math.toRadians(rot_x));
	}
}

