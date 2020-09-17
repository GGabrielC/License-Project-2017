import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import javafx.application.Application;
import javafx.stage.Stage;
//pdftotext -enc UTF-8 t.pdf t.txt

public class Test {
	String path_conference = "C:\\Users\\Gabriel\\workspace neon\\Suffix2\\src\\text-conference.txt";
	String path_journal = "C:\\Users\\Gabriel\\workspace neon\\Suffix2\\src\\text-journal.txt";
	
	public Test( int start_length, int max_length, int times_for_each_length, int length_growth, int chars_used, boolean brute_force_test ) throws Exception{
		find_problem( start_length, max_length, times_for_each_length, length_growth, chars_used, brute_force_test );
	}//function
	public Test( boolean brute_force_test ) throws Exception{
		SuffixTree suf_tree = new SuffixTree();
		suf_tree.addByFilePath(path_conference);
		suf_tree.addByFilePath(path_journal);

		valid_tree( suf_tree, brute_force_test );
	}//function
	public Test( String[] strs ){
		SuffixTree tree = new SuffixTree();
		for( String s: strs)
			tree.addByString(s);
	}
	
	private static boolean valid_tree(SuffixTree tree, boolean brute_force ) throws Exception{
		boolean is_valid = true;
		/* testing number of leafs */
		if( tree.total_leafs != tree.total_source_length ){
			is_valid = false;
			System.out.println( "Tree doesn't have all the leafs "+tree.total_leafs+"/"+tree.total_source_length );
		}//if
		// */
		
		/* testing suffixes */
		if( brute_force == true ){
			for( SuffixTree.StringSource ss : tree.str_sources ){
				//for(int i=0; i<ss.getLength(); i++){
					//System.out.println(" Checking suffix "+i);
				for(int i=ss.getLength()-1; i>=0; i--){
					if( i%1000 == 0 ) System.out.println(" Checking suffix "+i);
					int last_index = ss.getLength()-1;
					String suffix = ss.getSubstring(i, last_index );
					if ( !tree.hasSuffix( suffix ) ){
						is_valid = false;
						System.out.println( ss.name+" doesn't have suffix of label "+i );
						break;
					}//if
				}//for
			}//for
		}//if
		// */
		
		/* testing */
		
		// */
		//System.out.println( "test done !" );
		return is_valid;
	}//for

	private static void find_problem( int start_length, int max_length, int times_for_each_length, int length_growth, int chars_used, boolean brute_force_test) throws Exception{
		for( int length = start_length; length <= max_length; length+=1){
			System.out.println("length="+length);
			for( int times = 0; times < times_for_each_length; times += 1  ){
				//if( times %10000 == 0) System.out.println("num_generates="+num_generates);
				String t1 = new String( random_text( length, chars_used ) );
				//String t2 = new String( random_text( length ) );
				
				SuffixTree tree = new SuffixTree();
				tree.addByString( new String(t1) );
				//tree.addByString( new String(t2) );
				
				if( !valid_tree(tree, brute_force_test) )
					//System.out.println( "\""+t1+"-"+t2+"\"" );
					System.out.println( "\""+t1+"\"" );
			}//for
		}//for
		System.out.println( "test done !" );
	}
	
	static Random r = new Random();
	public static char[] random_text( int length, int chars_used ){
		char[] charcodes = new char[length];
		for(int i=0; i<length; i++){
			charcodes[i] = (char)( 65+r.nextInt( chars_used ) );
		}
		return charcodes;
	}//function
	
	public static String pretty_json( String str, int indent_spaces ){
		String new_str = "";
		int level = 0;
		for(int i=0; i<str.length(); i++){
			if( str.charAt(i) == '{' || str.charAt(i) == '['){
				new_str += "\n";
				for( int j=0; j<level*indent_spaces; j++){
					new_str += " "; 
				}
				new_str += str.charAt(i);
				level ++;
				new_str += "\n";
				for( int j=0; j<level*indent_spaces; j++){
					new_str += " "; 
				}
			}else if( str.charAt(i) == '}' || str.charAt(i) == ']' ){
				level --;
				new_str += "\n";
				for( int j=0; j<level*indent_spaces; j++){
					new_str += " "; 
				}
				new_str += str.charAt(i);
			}else if( str.charAt(i) == ',' ){
				new_str += str.charAt(i)+"\n";
				for( int j=0; j<level*indent_spaces; j++){
					new_str += " "; 
				}
			}else if( str.charAt(i) == ':' ){
				new_str += str.charAt(i)+" ";
			}else if( str.charAt(i) != ' ' ){
				new_str += str.charAt(i);
			} 
		}
		return new_str;
	}//function
}//class

//"EEEB-EEEB"
//"CEEEC-BEEEC"
//"DDDAD-BDDDA"
//"BAAAC-BAAAC"
//"AAADC-BAAAD"
//"AAACAAAC"
//"HEHHAHHA"
//"AAGAAAAA"
//"CCCFCCC"
//"BHAAFFFDAHBBFCAFFFFBHAFFFCFFD"
//"AABAAABAAAADIDCDBAAA"
//"BAAABAAAABAAA"
//"AAABAAAA"
//"ABAACADBAA"
//"DACAAABDAC"
//"BAAACAAAAABAAAC"
//"AAABAAAABDBAAAABBA"
//"BABAAAABAAAAABAAAA"

/*
String s1 = "abaaba";
String s2 = "abcabxabcd";
String s3 = "mississi";
String s4 = "abc";
String s5 = "abcc";
String s6 = "abaa";
String s7 = "abbabb";
String s8 = "aax";
String[] some_strings = new String[]{ s1,s2,s3,s4,s5,s6,s7,s8 };
*/