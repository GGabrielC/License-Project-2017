
public class MyListNode {
	private Object value;
	private MyListNode next = null;
	
	public MyListNode(Object value){
		this.value = value;
	}
	public void setNext( MyListNode next ){
		this.next = next;
	}
	public Object getValue(){
		return value;
	}
	
}
