import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
 
public final class GUI extends Application {
    SuffixTree suffix_tree = null;
    final ObservableList<SuffixTree.SubstringX>   data_lv_substrings = FXCollections.observableArrayList();
    final ObservableList<SuffixTree.StringSource> data_lv_sources    = FXCollections.observableArrayList();
    HashMap< SuffixTree.StringSource, LinkedList<Integer> > info_selected_substring = null;
    boolean auto_sort;
    
    //pdftotext -enc utf-8
    public static void main(String[] args) throws Exception {
    	 Application.launch(args);
    	
    	// Testings
    	// new Test( new String[]{"abaaba","abcabxabcd"} );
    	// new Test( new String[]{"HAHACHAC"} );
    	// new Test( false );
    	
    	// new Test( 1000, Integer.MAX_VALUE, 1, 2, false );
    	/* * /
    	int start_length = 8;
    	int max_length = 22;
    	int times_for_each_length = 111111;
    	int length_growth = 1;
    	int chars_used = 5;
    	boolean brute_force_test = false;
    	new Test( start_length, max_length, times_for_each_length, length_growth, chars_used, brute_force_test );
    	// */
    }//main
    
    final FileChooser fileChooser  		= new FileChooser();
    final Button 	bt_add_files		= new Button("Add files");
    final Button 	bt_empty_tree		= new Button("Empty tree");
    final Button 	bt_compute			= new Button("Compute");
    final Button    bt_sort_substrings 	= new Button("sort");
    final CheckBox  cb_auto_sort		= new CheckBox("Sort substrings after compute");
    final TextField tf_min_len_sub		= new TextField("50");
    final Label 	label_tf    		= new Label("Min. common substring length:");
   
    ListView<SuffixTree.SubstringX>   lv_substrings = new ListView<>( data_lv_substrings );
    ListView<SuffixTree.StringSource> lv_sources    = new ListView<>( data_lv_sources );
    final Label 	label_lv_substrings     = new Label("Common substrings:");
    final Label 	label_lv_sources        = new Label("Selected substring occurs in following texts:");
    
    final TextArea	ta_indexes	    	  	= new TextArea();
    final Label		label_ta_indexes    	= new Label("Substring occurences in selected text:");
    final TextArea 	ta_miscellaneous     	= new TextArea();
	final Label 	label_ta_miscellaneous  = new Label("Miscellaneous info:");
	final TextArea	ta_substring      		= new TextArea();
    final Label 	label_ta_substring      = new Label("Selected substring:");
    
    @Override
    public void start(final Stage stage) {
        stage.setTitle("Common Substring Finder");
        
        bt_compute.setPrefWidth(200);
        bt_compute.setOnAction( new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent e) {
                	trigger_bt_compute();
        }});
        
        bt_empty_tree.setPrefWidth(200);
        bt_empty_tree.setOnAction( new EventHandler<ActionEvent>() { 
                @Override
                public void handle(final ActionEvent e) {
                	trigger_bt_empty_tree();
        }});
        
        bt_add_files.setPrefWidth(200);
        bt_add_files.setOnAction( new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent e) {
                	trigger_bt_add_files( fileChooser.showOpenMultipleDialog(stage) );
        }});
        
        bt_sort_substrings.setOnAction( new EventHandler<ActionEvent>() { 
            @Override
            public void handle(final ActionEvent e) {
            	trigger_bt_sort_substrings();
        }});
        cb_auto_sort.setOnAction( new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
            	//cb_auto_sort.isSelected();
        }});
        
        lv_substrings.setPrefSize(400, 135);
        lv_substrings.setItems( data_lv_substrings );
        lv_substrings.getSelectionModel().selectedItemProperty().addListener(
            (ObservableValue<? extends SuffixTree.SubstringX> ovstr, SuffixTree.SubstringX old_substring, SuffixTree.SubstringX new_substring) -> {
            	trigger_substring_select( new_substring );
		});
        
        lv_sources.setPrefSize(260, 200);
        lv_sources.setItems( data_lv_sources );
    	lv_sources.getSelectionModel().selectedItemProperty().addListener(
            (ObservableValue<? extends SuffixTree.StringSource> ovss, SuffixTree.StringSource old_source, SuffixTree.StringSource new_source) -> {
        		trigger_source_select( new_source );
		});
        
        ta_indexes.setEditable(false);
        ta_indexes.setWrapText(true);
        ta_indexes.setPrefSize(230, 200);
        
        ta_miscellaneous.setEditable(false);
        ta_miscellaneous.setPrefSize(230, 200);
        
        ta_substring.setEditable(false);
        ta_substring.setWrapText(true);// setWrappingWidth
        ta_substring.setPrefSize(700, 200);
        
        cb_auto_sort.setSelected( true );
        
        GridPane grid_substring = new GridPane();
        grid_substring.setVgap(4);
        grid_substring.setHgap(10);
        grid_substring.setPadding(new Insets(5, 5, 5, 5));
        grid_substring.add( label_ta_substring, 0, 0);
        grid_substring.add( ta_substring, 0, 1);
        
        final GridPane grid_lv = new GridPane();
        grid_lv.setVgap(4);
        grid_lv.setHgap(10);
        grid_lv.setPadding(new Insets(5, 5, 5, 5));
        grid_lv.add( label_lv_substrings, 0, 0 );
        grid_lv.add( lv_substrings, 0, 1 );
        
        final GridPane grid_control = new GridPane();
        grid_control.setVgap(4);
        grid_control.setHgap(10);
        grid_control.setPadding(new Insets(5, 5, 5, 5));
        grid_control.add( label_tf, 0, 0);
        grid_control.add( tf_min_len_sub, 0, 1);
        grid_control.add( bt_add_files, 0, 2);
        grid_control.add( bt_empty_tree, 0, 3);
        grid_control.add( bt_compute, 0, 4);
        grid_control.add( cb_auto_sort, 0, 5);
      
        GridPane grid_top = new GridPane();
        grid_top.setVgap(4);
        grid_top.setHgap(10);
        grid_top.setPadding(new Insets(5, 5, 5, 5));
        grid_top.add( grid_control , 0, 0);
        grid_top.add( grid_lv, 1, 0);
        grid_top.add( bt_sort_substrings, 2, 0);
        
        GridPane some_info = new GridPane();
        some_info.setVgap(4);
        some_info.setHgap(10);
        some_info.setPadding(new Insets(5, 5, 5, 5));
        some_info.add( label_ta_miscellaneous, 0, 0);
        some_info.add( ta_miscellaneous, 0, 1);
        some_info.add( label_lv_sources, 1, 0);
        some_info.add( lv_sources, 1, 1);
        some_info.add( label_ta_indexes, 2, 0);
        some_info.add( ta_indexes, 2, 1);
        
        final GridPane grid_root = new GridPane();
        grid_root.setVgap(4);
        grid_root.setHgap(10);
        grid_root.setPadding(new Insets(5, 5, 5, 5));
        grid_root.add( grid_top , 0, 0);
        grid_root.add( some_info , 0, 1);
        grid_root.add( grid_substring , 0, 2);
        
        final Pane rootGroup = new VBox(12);
        rootGroup.getChildren().addAll( grid_root );
        rootGroup.setPadding(new Insets(12, 12, 12, 12));
        
        stage.setScene(new Scene(rootGroup));
        stage.show();
    }//function
    
    public void trigger_substring_select( SuffixTree.SubstringX substring ){
    	if( substring != null ){
	    	ta_indexes.clear();
	    	data_lv_sources.clear();
	        ta_substring.setText( substring.getActualSubstring() );
	        
	        info_selected_substring = substring.getInfo();
	        Set<SuffixTree.StringSource> common_sources = info_selected_substring.keySet();
	    	data_lv_sources.addAll( common_sources );
    	}else{
    		data_lv_sources.clear();
        	ta_indexes.clear();
        	ta_substring.clear();
    	}
    	
    	if( data_lv_sources.size() > 0 )
    		label_lv_sources.setText("Selected substring occurs in following texts ("+data_lv_sources.size()+"):");
    	else label_lv_sources.setText("Selected substring occurs in following texts:");
    	label_ta_indexes.setText("Substring occurences in selected text:");
    }//function
    
    public void trigger_source_select(SuffixTree.StringSource source){
    	if( source != null ){
            ta_indexes.setText( info_selected_substring.get(source).toString() );
    		label_ta_indexes.setText("Substring occurences ("+info_selected_substring.get(source).size()+") in selected text:");
    	}else{
    		ta_indexes.clear();
    		label_ta_indexes.setText("Substring occurences in selected text:");
    	}
    }//function
    
    public void trigger_bt_compute(){
    	if( suffix_tree == null || suffix_tree.source_count < 2 )
    		return;
    	trigger_source_select(null);
    	trigger_substring_select(null);
    	data_lv_substrings.clear();
    	
    	LinkedList<SuffixTree.SubstringX> substrings;
    	try{
	    	String 	text = tf_min_len_sub.getText();
	    	Integer min_length = Integer.parseInt( text );
	    	substrings = suffix_tree.findCommonSubstrings( min_length );
	    	label_lv_substrings.setText("Common substrings ("+substrings.size()+"):");
	        data_lv_substrings.addAll( substrings );
	        if( cb_auto_sort.isSelected() )
	        	trigger_bt_sort_substrings();
    	}catch(Exception e){
    		label_lv_substrings.setText("Common substrings:");
    	}
    }//function
    
    public void trigger_bt_empty_tree(){
    	if( suffix_tree == null )
    		return;
    	
        suffix_tree = new SuffixTree();
        ta_miscellaneous.clear();
        data_lv_sources.clear();
    	ta_indexes.clear();
    	ta_substring.clear();
        if( data_lv_substrings != null ){
        	data_lv_substrings.clear();
        }//if
        trigger_source_select(null);
        trigger_substring_select(null);
        label_lv_substrings.setText("Common substrings");
    }//function
    
    public void trigger_bt_add_files( List<File> files ){
    	trigger_source_select(null);
    	trigger_substring_select(null);
    	data_lv_substrings.clear();
    	label_lv_substrings.setText("Common substrings");
        if ( files != null && files.size()>0 ) {
            for (File file : files) {
            	appendFileToTree(file);
            }//for
            ta_miscellaneous.setText( Test.pretty_json(suffix_tree.toString(),3).trim() );
        }//if
    }//function
    
    public void trigger_bt_sort_substrings(){
        data_lv_substrings.sort(new Comparator<SuffixTree.SubstringX>() {
            @Override
            public int compare(SuffixTree.SubstringX s1, SuffixTree.SubstringX s2) {
                if( s1.getLength() == s2.getLength() )
               	 return s2.getTotalOccurences() - s1.getTotalOccurences();
                else return s2.getLength() - s1.getLength();
            }//function
        });
    }//function
    
    private void appendFileToTree( File file ){
    	if( suffix_tree == null )
    		suffix_tree = new SuffixTree();
    	if (file != null) {
            try {
				suffix_tree.addByFile(file);
			} catch (IOException e1) {
				e1.printStackTrace();
			}finally{
			}//finally
        }//if
    }//function
}//class