import net.minecraft.server.MinecraftServer;
import java.util.List;

public abstract class NonPlayerCharacter {	
	public static List players;
	
	private ep user; // ep = user object
	private gs handler; // gs = entity tracking handler
	
	public NonPlayerCharacter(String name, double x, double y, double z, float rotation, float pitch, int itemInHand) {
		if (players == null) getPlayerList();
	
		MinecraftServer s = etc.getServer().getMCServer();
		
		user = new ep(s, s.e, name, new jq(s.e));
		teleportTo(x,y,z,rotation,pitch);
		if (itemInHand > 0) {
			setItemInHand(itemInHand);
		}
	
		handler = new gs(user, /* tracking distance: */ 512, /* update pos every: */ 1 /* ticks */, true /* visibility? */);
	}
	
	public void delete() {
		for (Object player : players) {
			((ep)player).a.b(new df(handler.a.g));
		}
	}
	
	public void untrack(Player player) {
		if (handler.o.contains(player.getUser())) {
			handler.o.remove(player.getUser()); // o is the list of users the entity is sending position data to
		}	
	}
	
	public void broadcastPosition() {
		handler.b(players); 
	}
	
	public void broadcastMovement() {
		handler.a(players);
	}
		
	public void broadcastItemInHand() {
		for (Object player : players) {
			((ep)player).a.b(new gp(user.g, getItemInHand()));
		}
	}
	
	public String getName() {
		return user.ar;
	}
	
	public void setName(String name) {
		user.ar = name;
	}
	
	public double getX() {
		return user.m;
	}
	
	public void setX(double x) {
		user.m = x;
	}
	
	public double getY() {
		return user.n;
	}
	
	public void setY(double y) {
		user.n = y;
	}
	
	public double getZ() {
		return user.o;
	}
	
	public void setZ(double z) {
		user.o = z;
	}
	
	public float getRotation() {
		return user.v;
	}
	
	public void setRotation(float rot) {
		user.v = rot;
	}
	
	public float getPitch() {
		return user.w;
	}
	
	public void setPitch(float pitch) {
		user.w = pitch;
	}
	
	public int getItemInHand() {
		return user.ak.a[0].c;
	}
	
	public void setItemInHand(int type) {
		user.ak.a[0] = new hj(type);
	}
	
	public void teleportTo(double x, double y, double z, float rotation, float pitch) {
		user.b(x,y,z,rotation,pitch);
	}
	
	public static void getPlayerList() {
		players = etc.getServer().getMCServer().f.b; // f = gl.class
			// s.f.b from getPlayerList()
				// s = server
				// ft (f) = connection handler? (calls ea (user) constructor)
				// b is ArrayList of users
	}
}

