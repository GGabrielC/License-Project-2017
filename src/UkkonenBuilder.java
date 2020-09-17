import java.util.HashMap;
import java.util.HashSet;


public class UkkonenBuilder implements SuffixTreeBuilder {
	final SuffixTree.StringSource string_source;
	final int string_source_length;
	int  current_phase;
	int  current_extension;
	char current_phase_char;
	
	final SuffixTree tree;
	final InternalNode root;
	final ActivePoint active_point; 
	
	final Leaf[] leafs;
	final End end = new End(-1);
	
	int num_of_inodes = 0;
	
	public UkkonenBuilder( SuffixTree.StringSource string_source , SuffixTreeNode root, SuffixTree tree ){
		this.tree = tree;
		this.string_source = string_source;
		this.string_source_length = string_source.getLength();
		this.leafs = new Leaf[ this.string_source_length ];
		this.root = (root == null) ? new InternalNode() : (InternalNode) root;
		this.tree.setRoot(this.root);
		this.active_point = new ActivePoint( this.root );
	}

	@SuppressWarnings("finally")
	public HashMap<Info, Object> execute() {
		HashMap<Info, Object> return_info = new HashMap<>();
		try{
			for( current_phase=current_extension=0; current_phase < string_source_length; current_phase++ ){
				tree.notify_char_read( current_phase_char = this.string_source.charAt(current_phase) );
				end.increment();
				InternalNode prev_link = null;
				while( current_extension <= current_phase && active_point.stepForward() == false ){
					InternalNode link = active_point.addExtension();
					if( prev_link != null ){			if( prev_link.depth != link.depth +1 ) System.out.println("HOPA link depth");
						prev_link.setSuffixLink( link );
					}//if
					prev_link = link;
					active_point.jumpToLink();
				}//while
			}//for
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if( active_point.active_length > 0 )
				System.out.println("HOPA length=" + active_point.active_length);
			if( leafs[leafs.length-1] == null )
				System.out.println( "HOPA only "+current_extension+"/"+leafs.length+" leafs" );
			return_info.put(Info.root, root);
			return_info.put(Info.leafs, leafs);
			return_info.put(Info.num_of_leafs,current_extension);
			return_info.put(Info.num_of_inodes,num_of_inodes);
			return return_info;
		}//finally
	}//function
	
	public char getCurrentExtensionChar(){
		return string_source.charAt( current_extension );
	}//function
	
	public class ActivePoint{
		SuffixTreeNode active_path = null;
		InternalNode   active_node;
		int active_length = 0;
		
		public ActivePoint( InternalNode node ){
			this.active_node = node;
		}//function
		public void reset( InternalNode active_n ){
			active_node = active_n;
			active_length = current_phase - current_extension - active_node.depth;
			if( active_length > 0 ){
				char c = string_source.charAt( current_extension + active_node.depth );
				active_path = active_node.getPath( c );
				climb();
			}else{
				active_length = 0;
				active_path = null;
			}//else
		}//function
		
		public boolean isAt( InternalNode node ){
			return active_node == node;
		}//function
		public boolean isAtPathEnd(){
			return active_path.getLength() == active_length;
		}//function
		
		public char getEdgeChar(){ 
			return string_source.charAt( current_extension + active_node.getDepth() ); 
		}//function
		
		private void jumpToLink() throws Exception{
			active_node = active_node.getSuffixLink();
			reset( active_node );
		}//function
		
		private InternalNode addExtension() throws Exception {
			if( active_length == 0){
				return addLeaf( active_node );
			}else if( this.isAtPathEnd() ){
				return addLeaf( (InternalNode) active_path );
			}else{
				return insertLeaf();
			}//else
		}//function
		
		private InternalNode addLeaf( InternalNode inode ) throws Exception{
			inode.getChildren().put( current_phase_char, new Leaf(inode));
			return inode;
		}//function
		
		private InternalNode insertLeaf() throws Exception{
			int break_index = active_path.getStartIndex() + active_length;
			InternalNode new_internal_node = new InternalNode( active_path.getStartIndex(), break_index-1, active_node );
			new_internal_node.setIndexOwner( active_path.getIndexOwner() );
			new_internal_node.getChildren().put( current_phase_char, new Leaf( new_internal_node ) );
			new_internal_node.getChildren().put( active_path.getIndexOwner().charAt(break_index), active_path); 
			active_node.getChildren().remove( active_path.charAt(0) );
			active_path.setParent( new_internal_node );
			active_path.setStart(break_index);
			active_node.getChildren().put( new_internal_node.charAt(0), new_internal_node ); 
			active_path = new_internal_node;
			return new_internal_node;
		}//function
		
		private void climb(){
			if( active_path == null )
				return;
			while( active_length > active_path.getLength() ){
				active_length -= active_path.getLength();
				active_node = (InternalNode) active_path;
				active_path = active_node.getPath( getEdgeChar() );
			}//while
		}//function
		
		public boolean stepForward() throws Exception{
			if( active_length == 0 ){
				active_path = active_node.getPath( current_phase_char );
				if( active_path == null ){
					return false;
				}else{
					active_length = 1;
					return true;
				}//else
			}else if( this.isAtPathEnd() ){      if(active_path.isLeaf()) System.out.println("HOPA e leaf");
				if( active_path.getPath( current_phase_char ) != null ){
					active_length = 1;
					active_node = (InternalNode) active_path;
					active_path = active_path.getPath( getEdgeChar() );
					return true;
				}else{
					return false;
				}//else
			}else{
				char stepped_char = active_path.charAt( active_length );
				if( current_phase_char == stepped_char ){
					active_length++;
					return true;
				}else{
					return false;
				}//else
			}//else
		}//function
	}//class
	
	public abstract class Node implements SuffixTreeNode{
		protected SuffixTree.StringSource index_owner = null;
		protected int start_index = -1;
		protected InternalNode parent;
		
		public char charAt( int index ) throws Exception{
			if( index < 0 || index >= this.getLength() ){
				System.out.println( "HOPA Index out of bounds for node");
				throw new Exception("Index out of bounds for node");
			}
			return index_owner.charAt( this.start_index+index );
		}
		public int getDepth(){
			if( this.isLeaf() ){
				return this.parent.depth + this.getLength();
			}else{
				return ((InternalNode) this).depth;
			}
		}
		public void setParent(SuffixTreeNode parent){
			this.parent = (InternalNode) parent;
		}
		public SuffixTreeNode getParent(){
			return this.parent;
		}
		/*
		public void mark(){
			this.is_marked = true;
		}
		public boolean isMarked(){
			return this.is_marked;
		}
		public void unmark(){
			this.is_marked = false;
		}
		*/
	}//class
	
	public class Leaf extends Node{
		final int label;
		final End last_index;
		
		public Leaf( InternalNode parent ){
			this.label = current_extension;
			this.start_index = current_phase;
			this.last_index = end;
			this.parent = parent;
			this.index_owner = string_source;
			leafs[ current_extension++ ] = this;
		}
		
		public SuffixTree.StringSource getIndexOwner(){
			return this.index_owner;
		}
		public int getStartIndex(){
			return this.start_index;
		}
		public int getEndIndex(){
			return this.last_index.getIndex();
		}
		public int getLabel(){
			return this.label;
		}
		public void setStart(int index){
			this.start_index = index;
		}
		public int getLength(){
			return this.last_index.getIndex() - this.start_index + 1;
		}
		public SuffixTreeNode getPath(char character){
			return null;
		}
		public HashMap<Character, SuffixTreeNode> getChildren() {
			return null;
		}
		public String toString(){
			return "{ "
				 + "Type: Leaf"
				 + ", Label: "  + label
				 + ", Depth: "	+ getDepth()
				 //+ ", Parent: " + parent.name
				 + ", Owner: "  + index_owner
				 + ", Path: \"" + getPathString()+"\""
				 + ", Interval: "+ start_index +" ; "+ last_index +""
				 + " }";
		}
		@Override
		public boolean isLeaf() {
			return true;
		}
		@Override
		public boolean isINode() {
			return false;
		}

		@Override
		public String getPathString() {
			return index_owner.getSubstring( start_index, last_index.getIndex());
		}
		@Override
		public String getDepthString() {
			String s = "";
			SuffixTreeNode n = this;
			while( n != root ){
				s = n.getPathString() +s;
			}//while
			return s;
		}
	}//class
	
	public class InternalNode extends Node{
		final HashMap<Character, SuffixTreeNode> children = new HashMap<>();
	    InternalNode suffix_link = null;
		int last_index = -2;
		//String name = "ROOT";
		int depth = 0;
		int is_suffix_link_times = 0;
		
		public Leaf any_next_leaf = null;
		public int mark_num_next_leafs = 0;
		public boolean good_mark = false;
		public boolean visited = false;
		public boolean visited_via_link = false;
		// public boolean has_insufficient_chiald = false;
		// public boolean is_suffix_and_prefix = false;
		
		private InternalNode(){ //for root
			setSuffixLink(this);
			this.parent = this;
			num_of_inodes++;
		}
		
		public InternalNode( int start_index, int last_index, SuffixTreeNode parent ){
			this.start_index = start_index;
			this.last_index  = last_index;
			setSuffixLink(root);
			this.parent 	 = (InternalNode) parent;
			this.depth 		 = this.parent.depth + this.getLength();
			this.index_owner = string_source;
			//this.name = ""+(char)('A'+(num_of_inodes -1))+"-"+this.index_owner.id;
			num_of_inodes++;
		}
		public HashSet<Character> getCharKeys(){
			return new HashSet<Character>( children.keySet() );
		}
		public SuffixTreeNode getChiald( Character c ){
			return children.get( c );
		}
		private void addChiald( char key, Leaf leaf ){
			//children.put( key, leaf);
		}
		private void addChiald( char key, InternalNode leaf ){
			//children.put( key, leaf);
		}
		private void removeChiald(){
			// TODO
		}
		public void setIndexOwner( SuffixTree.StringSource string_source){
			this.index_owner = string_source;
		}
		public SuffixTree.StringSource getIndexOwner(){
			return this.index_owner;
		}
		public HashMap<Character, SuffixTreeNode> getChildren() {
			return this.children;
		}
		/*public void addLeaf(){ BAD IDEEA coz of new Leaf and this
			children.put(current_char, new Leaf( this ) );
		}*/
		public int getStartIndex(){
			return this.start_index;
		}
		public int getEndIndex(){
			return this.last_index;
		}
		public InternalNode getSuffixLink(){
			return this.suffix_link;
		}
		public SuffixTreeNode getPath(char character){
			return this.children.get(character);
		}
		public boolean isSuffixLink(){
			return is_suffix_link_times > 0;
		}
		public int isSuffixLinkTimes(){
			return is_suffix_link_times;
		}
		public void setStart(int index){
			this.start_index = index;
			if( start_index > last_index ){
				System.out.println("HOPA error start_index > last_index");
				throw new Error("error start_index > last_index");
			}
		}
		public void setEnd(int index){
			this.last_index = index;
		}
		public void setSuffixLink( InternalNode node ){
			if( this.suffix_link != null ){
				this.suffix_link.is_suffix_link_times--;
			}//if
			node.is_suffix_link_times++;
			this.suffix_link = node;
		}
		public int getLength(){
			return this.last_index - this.start_index + 1;
		}
		public String toString(){
			return "{ "
				 + "Type: INode"
			     //+ ", Name: "	+ name
			     //+ ", Parent: " + parent.name
			     //+ ", Link: "	+ this.suffix_link.name
			     + ", Depth: " 	+ depth
			     + ", Owner: "  + index_owner
				 + ((this==root) ? "":", Path: \"" + getPathString()+"\"")	
				 + ", Interval: "+ start_index +" ; "+ last_index +""
				 + ", Children: " + children
				 + " }";
		}
		@Override
		public boolean isLeaf() {
			return false;
		}
		@Override
		public boolean isINode() {
			return true;
		}

		@Override
		public String getPathString() {
			return index_owner.getSubstring( start_index, last_index );
		}

		@Override
		public String getDepthString() {
			String s = "";
			SuffixTreeNode n = this;
			while( n != root ){
				s = n.getPathString() +s;
			}//while
			return s;
		}
	}//class
	
	public class End {
		private int index;
		public End(int index){
			this.index = index;
		}
		public void increment(){
			this.index++;
		}
		public int getIndex(){
			return index;
		}
		@Override
		public String toString() {
			return ""+index;
		}
	}//class
}//class