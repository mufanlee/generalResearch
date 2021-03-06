package commands.model2.gm;

import generativemodel.RVariable;
import generativemodel.RVariableValue;

public class CommandLenValue extends RVariableValue{

	int l;
	
	public CommandLenValue(RVariable owner){
		this.setOwner(owner);
	}
	
	public CommandLenValue(RVariable owner, int l){
		this.setOwner(owner);
		this.l = l;
	}
	
	@Override
	public boolean valueEquals(RVariableValue other) {
		if(this == other){
			return true;
		}
		
		if(!(other instanceof CommandLenValue)){
			return false;
		}
		
		CommandLenValue that = (CommandLenValue)other;
		
		return this.l == that.l;
	}

	@Override
	public String stringRep() {
		return String.valueOf(l);
	}
	
	
	
}
