
public class MyList {
	private MyListNode first=null;
	private MyListNode last=null;
	
	public MyList(){}
	public void add(MyList list){
		if( list == null )
			return;
		if( this.first == null )
			this.first = list.first;
		else
			this.last.setNext( list.getFirst() );
	}
	
	public MyListNode getFirst(){
		return first;
	}
}
