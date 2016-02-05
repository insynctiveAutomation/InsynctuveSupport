package insynctive.support.utils;

public enum Notify { 
	NOTIFY(true), DONT_NOTIFY(false);
	
	public boolean value;
	
	private Notify(boolean value){
		this.value = value;
	} 
}