import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class Function models all functions defined over a set of variables and
 *  consists of operations over these functions
 */
public class Function {	
	ArrayList<Variable> variablesList;
	ArrayList<Variable> evidanceList;
	ArrayList<LogValue> tableList;
	
	// Default Constructor
	public Function() {
		this.variablesList = new ArrayList<Variable>();
		this.tableList = new ArrayList<LogValue>();
		this.evidanceList = new ArrayList<Variable>();
	}
	
	// Copy Constructor 
	public Function(Function f) {
		this.variablesList = f.variablesList;
		this.tableList = f.tableList;
		this.evidanceList = f.evidanceList;
	}
	
	/**
	 * This method is used to add new variable into the function
	 * @param k is new variable to be added to function
	 */
	public void addVariable(Variable k) {
		if(this.variablesList.contains(k)== false) {
			ArrayList<LogValue> table = new ArrayList<LogValue>();
			this.variablesList.add(k);
			int tableSize = this.tableList.size();
			if(tableSize == 0)
				tableSize = 1;
			for(int i = 0; i <  k.scope * tableSize; i++)
				table.add(new LogValue(1d, false));
			this.tableList = table;
		}
	}
	
	/**
	 * This method provides index/address given a setting of function variables
	 * @param variableSettings is mapping of variable to its setting
	 */
	public int getAddressFromSettings(HashMap<Integer, Integer> variableSettings) {
		int address = 0;
		int p = this.tableList.size();
		int blockLength;
		for(Variable var : this.variablesList) {
			blockLength = p / var.scope;
			address = address + variableSettings.get(var.id) * blockLength;
			p = blockLength;
		}
		return address;
	}
	
	/**
	 * This method provides variables settings in a function for a given address
	 * @param address is the index at which settings is to be computed
	 */
	public HashMap<Integer, Integer> getSettingsFromAddress(int address) {
		HashMap<Integer, Integer> variableSettings = new HashMap<Integer, Integer>();
		int p = this.tableList.size();
		int remainder, value, blockLength;
		for(Variable var : this.variablesList) {
			blockLength = p / var.scope;
			value = address / blockLength;
			variableSettings.put(var.id, value);
			remainder = address % blockLength;
			address = remainder;
			p = blockLength;
		}
		return variableSettings;
	}		

}
