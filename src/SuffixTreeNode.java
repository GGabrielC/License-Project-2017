import java.util.HashMap;
import java.util.HashSet;

public interface SuffixTreeNode {
	public int getStartIndex();
	public int getEndIndex();
	public int getLength();
	public void setStart(int index);
	
	public int getDepth();
	public void setParent(SuffixTreeNode parent);
	public SuffixTreeNode getParent();
	public SuffixTreeNode getPath(char character);
	public char charAt(int active_length) throws Exception;
	public SuffixTree.StringSource getIndexOwner();
	public String getPathString();
	public String getDepthString();
	/*
	public void mark();
	public boolean isMarked();
	public void unmark();
	*/
	public boolean isLeaf();
	public boolean isINode();
	
	public HashMap<Character, SuffixTreeNode> getChildren();
}
