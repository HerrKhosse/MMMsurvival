import java.io.Serializable;

/*import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Playerblock")
@XmlRootElement(name="Playerblock")*/
public class PlayerBlock implements Serializable
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7340609289012211338L;
	int x;
	int y;
	int z;
	int type;
	String owner;
	
	int durability;
	boolean breakable;
	boolean protect;
	
	public PlayerBlock (Player player, Block block, int dur)
	{
		this.owner = player.getName();
		this.x = 		block.getX();
		this.y = 		block.getY();
		this.z = 		block.getZ();
		this.type = 	block.getType();
		
		if (dur == 0) {breakable = false;}else{breakable = true; this.durability = dur;}
		this.protect = 	false;
	}
	
}
