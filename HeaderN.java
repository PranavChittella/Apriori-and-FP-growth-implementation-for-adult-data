import java.util.Comparator;

public class HeaderN {
	public String itemID;
	public int supportCount;
	public FPTree nodeLink;

	public HeaderN(String itemID, int supportCount){
		this.itemID = itemID;
		this.supportCount = supportCount;
	}

	@Override
	public String toString() {
		return "HeaderN [itemID=" + itemID + ", supportCount=" + supportCount + "]";
	}
}
