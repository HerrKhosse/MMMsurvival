import java.util.HashMap;
import java.util.HashSet;
import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="Room")
@XmlRootElement(name="Room")
public class Room
{
	int blockCount;
	
	@XmlList
	HashSet<String> blocks = new HashSet<String>();
	HashMap<Integer,Integer> itemList = new HashMap<Integer,Integer>();
	HashMap<Integer,Integer> blockList = new HashMap<Integer,Integer>();
	
	
	public Room() {}
    public Room(int blockCount,HashSet<String> blocks,HashMap<Integer,Integer> blockList,HashMap<Integer,Integer> itemList)
    {
    	this.blockCount = blockCount;
    	
    	this.blocks = blocks;
    	this.itemList = itemList;
    	this.blockList = blockList;
    }
    
	public int hashCode()
	{
		return blocks.hashCode();
	}
	
}
