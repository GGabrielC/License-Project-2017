import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;




public class SuffixTree {
	SuffixTreeNode root;
	public final LinkedList< StringSource > str_sources = new LinkedList<>();
	int source_count = 0;
	final HashMap< StringSource, UkkonenBuilder.Leaf[] > leafs_by_source = new HashMap <>();
	int total_leafs  = 0;
	int total_inodes = 0;
	int total_source_length = 0;

	public final HashMap< Character, StringSource > source_by_unique_char = new HashMap<>();
	boolean[] char_code_used = new boolean[65536];
	int potential_unique_charcode = 65535;
	
	public SuffixTree() {}
	public SuffixTree( String[] strings ) {
		for( String str : strings ){
			addByString( str ); 
		}//for
	}//function
	
	public void setRoot( SuffixTreeNode n ){
		this.root = n;
	}//function
	
	public boolean hasSuffix( String suffix ) throws Exception{
		ActivePointSF ap = new ActivePointSF();
		
		for( int i=0; i<suffix.length(); i++){
			if( ap.step_forward( suffix.charAt(i) ) == false ){
				return false;
			}//if
		}//for
			
		return true;
	}//function

	public void notify_char_read( char char_read ){
		int charcode_read = (int) char_read;
		
		StringSource source = source_by_unique_char.get( char_read );
		if( source != null ){
			source.setUniqueLastChar();
		}//for
		
		char_code_used[ charcode_read ] = true;
	}//function
	
	public char getNextUniqueChar(){
		while( char_code_used[ potential_unique_charcode ] ){
			potential_unique_charcode--;
		}//while
		return (char) potential_unique_charcode;
	}//function
	
	public void buildTree( StringSource new_source ){
		HashMap<Info,Object> info = new UkkonenBuilder( new_source, this.root, this ).execute();
		update( info, new_source );
	}//function
	
	private void update( HashMap<Info,Object> info, StringSource new_source){
		this.root = (SuffixTreeNode) info.get(Info.root);
		this.str_sources.add( new_source );
		this.leafs_by_source.put( new_source, ( UkkonenBuilder.Leaf[] ) info.get(Info.leafs) );		
		this.total_leafs  += (int) info.get(Info.num_of_leafs);
		this.total_inodes += (int) info.get(Info.num_of_inodes);
		this.total_source_length += new_source.getLength();
	}//function
	
	private int generateStringSourceId(){
		return this.source_count;
	}
	
	public SuffixTree addByStrings( String[] strs ) throws IOException{
		for(int i=0; i<strs.length; i++)
			addByString( strs[i] );
		return this;
	}//function
	public SuffixTree addByString( String str ){
		StrContainer str_container = new StrContainer( str );
		buildTree( str_container );
		return this;
	}//function
	
	public SuffixTree addByFilePaths( String[] paths ) throws IOException{
		for(int i=0; i<paths.length; i++)
			addByFilePath( paths[i] );
		return this;
	}//function
	public SuffixTree addByFilePath( String path ) throws IOException{
		File file = new File( path );
		addByFile( file );
		return this;
	}//function
	
	public SuffixTree addByFiles( Iterable<File> files ) throws IOException{
		for( File file: files )
			addByFile( file );
		return this;
	}//function
	public SuffixTree addByFile( File file ) throws IOException{
		try {
	    	String text = new String( Files.readAllBytes( file.toPath()), StandardCharsets.UTF_8);
			buildTree( new StrContainer( text, file.getPath() ) );
	    }catch (IOException e) {
            e.printStackTrace();
        }finally{
        }//finally
		return this;
	}//function
		
	/* */	
	public LinkedList<SubstringX> findCommonSubstrings(int min_length){
		LinkedList<UkkonenBuilder.InternalNode> potential_nodes = new LinkedList<>();
		LinkedList<SubstringX> substrings_x = new LinkedList<>();
		fcs( root, min_length, potential_nodes );
		for( UkkonenBuilder.InternalNode node: potential_nodes ){
			UkkonenBuilder.InternalNode n = node;
			UkkonenBuilder.InternalNode prefix_node = n;
			UkkonenBuilder.InternalNode last_sufix_prefix_node = n;
			int prefix_active_length = n.getLength();
			while( !n.visited ){
				if( --prefix_active_length == 0 ){
					prefix_node = (UkkonenBuilder.InternalNode) prefix_node.getParent();
					prefix_active_length = prefix_node.getLength();
				}//if
				UkkonenBuilder.InternalNode link_node = n.getSuffixLink();
				
				if( link_node == prefix_node ){
					if( link_node.mark_num_next_leafs - last_sufix_prefix_node.mark_num_next_leafs > n.mark_num_next_leafs ){
						link_node.good_mark = true;
						last_sufix_prefix_node = link_node;// maybe outside this if ? doesn't matter
					}//if
				}else if( link_node.mark_num_next_leafs > n.mark_num_next_leafs ){
					
					link_node.good_mark = true;
				}//if
				n.visited = true;
				n = n.getSuffixLink();
				n.visited_via_link=true;
			}//while
		}//for
		for( UkkonenBuilder.InternalNode node: potential_nodes ){
			if( ( node.good_mark || !node.visited_via_link) ) { // && node.has_insufficient_chiald ){
					substrings_x.add( new SubstringX(node) );
			}//if
		}//for
		return substrings_x;
	}//function
	
	public HashMap<Info,Object> fcs(SuffixTreeNode node, int min_length, LinkedList<UkkonenBuilder.InternalNode> potential_nodes){
		HashMap<Info,Object> node_info = new HashMap<>();
		node_info.put(Info.sufficient_sources, false);
		
		if( node.isLeaf() ){
			UkkonenBuilder.Leaf leaf = (UkkonenBuilder.Leaf) node;
			node_info.put(Info.num_leafs, 1);
			node_info.put(Info.source, leaf.index_owner);
			node_info.put(Info.any_next_leaf, leaf);
		}else{
			UkkonenBuilder.InternalNode inode = (UkkonenBuilder.InternalNode) node;
			inode.mark_num_next_leafs = 0;
			inode.good_mark = false;
			inode.visited = false;
			inode.visited_via_link = false;
			inode.any_next_leaf = null;
			//inode.has_insufficient_chiald = false;
			
			for(SuffixTreeNode chiald: inode.getChildren().values() ){
				HashMap<Info,Object> chiald_info  = fcs(chiald, min_length, potential_nodes);
				inode.mark_num_next_leafs += (int)chiald_info.get(Info.num_leafs);
				
				//if( !inode.has_insufficient_chiald )
					//inode.has_insufficient_chiald = !(boolean)chiald_info.get(Info.sufficient_sources);
				
				if( inode.any_next_leaf == null ){
					inode.any_next_leaf = (UkkonenBuilder.Leaf) chiald_info.get(Info.any_next_leaf);
					node_info.put(Info.any_next_leaf, inode.any_next_leaf );
				}
					
				if( !(boolean)node_info.get(Info.sufficient_sources) )
					node_info.put(Info.sufficient_sources, (boolean)chiald_info.get(Info.sufficient_sources ) );
				
				if( node_info.get(Info.source) == null )
					node_info.put(Info.source, (StringSource)chiald_info.get(Info.source ));
				
				if( node_info.get(Info.source) != chiald_info.get(Info.source) )
					node_info.put(Info.sufficient_sources, true);
			}//for
			
			if( inode.getDepth() >= min_length && (boolean)node_info.get(Info.sufficient_sources) ){
				potential_nodes.add(inode);
			}
			node_info.put(Info.num_leafs, inode.mark_num_next_leafs);
		}//else
		return node_info;
	}//function
	
	public ArrayList<Integer> getSubstringIndexes( String str ) throws Exception{
		ArrayList<Integer> indexes = new ArrayList<>();
		SuffixTreeNode node = this.root;
		int i, i_str=0;
		for( 	node = this.root.getPath(str.charAt(0)) 
				;; node=node.getPath(str.charAt(i_str)) ){
			if( node==null )
				break;
			for( i=0; i<node.getLength() && i_str<str.length() ; i++, i_str++ ){
				if( str.charAt(i_str) != node.charAt(i) )
					break;
			}//for
			if( i==node.getLength() && i_str<str.length() )
				continue;			
			else break;
		}//for
		if( i_str >= str.length() ){
			for( UkkonenBuilder.Leaf n: getAllNextLeafs( node ) ){
				indexes.add( n.getLabel() );
			}//for
		}//if
		return indexes;
	}//function
	
	private LinkedList<UkkonenBuilder.Leaf> getAllNextLeafs( SuffixTreeNode node ){
		LinkedList<UkkonenBuilder.Leaf> next_leafs = new LinkedList<UkkonenBuilder.Leaf>();
		if( node.isLeaf() ){
			next_leafs.add( (UkkonenBuilder.Leaf) node );
		}else{
			HashMap<Character, SuffixTreeNode> children = node.getChildren();
			for(SuffixTreeNode chiald : children.values() ){
				if( chiald.isLeaf() ){
					next_leafs.add( (UkkonenBuilder.Leaf) chiald );
				}else{
					next_leafs.addAll( getAllNextLeafs(chiald) );
				}//else
			}//for
		}//else
		return next_leafs;
	}//function
	private UkkonenBuilder.Leaf getAnyNextLeaf( SuffixTreeNode node ){
		if( node.isLeaf() )
			return (UkkonenBuilder.Leaf) node;
		HashMap<Character, SuffixTreeNode> children = node.getChildren();
		for(SuffixTreeNode chiald : children.values() ){
			if( chiald.isLeaf() ){
				return (UkkonenBuilder.Leaf) chiald ;
			}else{
				return getAnyNextLeaf( chiald );
			}//else
		}//for
		System.out.println("UPS ST-123");
		return null;
	}//function
	
	public String toString() {
		return //Test.pretty_json( 
			"{ "
		 	 + "Sources: "  	  + this.str_sources
		 	 + ", Total-length: " + (this.total_source_length-2)
		 	 + ", Leafs: "  	  + this.total_leafs
		 	 + ", Inodes: "   	  + this.total_inodes
		 	 + ", Nodes: "  	  + (this.total_inodes + this.total_leafs)
			 //+ ", Root: "         + this.root.toString()
			 + " }"
			 ;//,4);
	}//function
	
	//######## Internal Classes ##################
	public abstract class StringSource {
		protected final String name;
		protected final int id;
		protected char unique_last_character;
		
		public int getUniqueLastCharCode(){
			return (int) this.unique_last_character;
		}//function
		
		public StringSource(){
			this.id = generateStringSourceId(); source_count++;
			this.name = "T"+id;
			setUniqueLastChar();
		}//function
		
		public void setUniqueLastChar(){
			this.unique_last_character = getNextUniqueChar();
		}
		public StringSource( String name ){
			this.id = generateStringSourceId(); source_count++;
			this.name = name;
			setUniqueLastChar();
		}//function
		
		public abstract char charAt( int index );
		public abstract int getLength();
		public abstract String getSubstring(int start_index, int last_index);
		public int getId(){
			return this.id;
		}//function
		
		public String toString(){
			return name +"-" +id +": " +(getLength()-1);
		}//function
	}//class
	
	public class SubstringX{
		final UkkonenBuilder.InternalNode inode;
		
		public SubstringX( UkkonenBuilder.InternalNode inode ){
			this.inode = inode;
		}//function
		@Override
		public String toString() {
			return "{ "
					+ "length: " + inode.getDepth()
				    + ", total occurences: "+ inode.mark_num_next_leafs
				    +" }";
		}//function
		public int getTotalOccurences(){
			return inode.mark_num_next_leafs;
		}//function
		
		public String getActualSubstring() {
			// UkkonenBuilder.Leaf any_leaf = getAnyNextLeaf(this.inode);
			UkkonenBuilder.Leaf any_leaf = this.inode.any_next_leaf;
			StringSource index_owner = any_leaf.getIndexOwner();
			int start_index = any_leaf.getLabel();
			int last_index  = start_index+this.inode.getDepth()-1;
			return index_owner.getSubstring( start_index, last_index );
		}//function
		public int getLength() {
			return this.inode.getDepth();
		}//function
		public HashMap< StringSource, LinkedList<Integer> > getInfo(){
			HashMap< StringSource, LinkedList<Integer> > indexes = new HashMap<>();
			for( UkkonenBuilder.Leaf leaf : getAllNextLeafs(inode) ){
				StringSource ss = leaf.getIndexOwner();
				LinkedList<Integer> list = indexes.get( ss );
				if( list != null ){
					list.add( leaf.getLabel() );
				}else{
					list = new LinkedList<Integer>();
					list.add( leaf.label );
					indexes.put( ss, list );
				}//else
			}//for
			return indexes;
		}//function
	}//class
	
	public class StrContainer extends StringSource{
		private final String str;
		
		public StrContainer(String str) {
			super();
			this.str  = str;
		}
		public StrContainer(String str, String name) {
			super( name );
			this.str  = str;
		}//function
		
		public char charAt( int index ){
			if( index == str.length() )
				return unique_last_character;
			return str.charAt( index );
		}
		public int getLength(){
			return str.length()+1;
		}//function
		
		public String getSubstring(int start_index, int last_index) {
			if( start_index == str.length()  && last_index == str.length() )
				return ""+unique_last_character;
			if( last_index == str.length() )
				return str.substring(start_index, last_index)+unique_last_character;
			return str.substring(start_index, last_index+1);
		}//function
	}//class
	
	public class CharContainer extends StringSource{
		private final char[] chars;
		public CharContainer(char[] chars) {
			super();
			this.chars  = chars;
		}//function
		public CharContainer(char[] chars, String name) {
			super( name );
			this.chars  = chars;
		}//function
		
		public char charAt( int index ){
			if( index == chars.length )
				return unique_last_character;
			return chars[index];
		}//function
		
		public int getLength(){
			return chars.length+1;
		}//function

		public String getSubstring(int start_index, int last_index) {
			String str = "";
			if( start_index == chars.length  && last_index == chars.length )
				return ""+unique_last_character;
			if( last_index == chars.length ){
				for(int i=start_index; i<last_index; i++)
					str += chars[i];
				return str+unique_last_character;
			}
			for(int i=start_index; i<=last_index; i++)
				str += chars[i];
			return str;
		}//function
	}//class
	
	public class ActivePointSF{
		SuffixTreeNode active_node = root;
		SuffixTreeNode active_path = null;
		int active_edge = -1;
		int active_length=0;
		
		public boolean step_forward( char next_char ) throws Exception{
			if( active_length == 0 ){
				active_path = active_node.getPath( next_char );
				if( active_path == null ){
					return false;
				}else{
					active_edge = active_path.getStartIndex();
					active_length = 1;
					return true;
				}//else
			}else if( active_length == active_path.getLength() ){
				if( active_path.getPath( next_char ) != null ){
					active_node = active_path;
					active_path = active_path.getPath( next_char );
					active_edge = active_path.getStartIndex();
					active_length = 1;
					return true;
				}else{
					return false;
				}//else
			}else{
				char stepped_char = active_path.charAt( active_length );
				if( next_char == stepped_char ){
					active_length++;
					return true;
				}else{
					return false;
				}//else
			}//else
		}//function
	}//class

}//class


