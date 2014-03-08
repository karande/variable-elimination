import java.util.ArrayList;

/**
 * Class Variable models different properties of a network variable
 */
public class Variable {
	int scope;	// domain size of variable
	int id; // unique identifier
	int value;	// value assigned for variable
	boolean isInstantiated;	// is evidence or not
	ArrayList<Variable> neighboursList;	// list of neighbors
	
	/**
	 * Copy Constructor 
	 * @param scope is the domain size of that variable
	 * @param id is the id value of the variable
	 */
	public Variable(int scope, int id) {
		this.scope = scope;
		this.id = id;
		this.isInstantiated = false;
		this.neighboursList = new ArrayList<Variable>();
	}
	
	public boolean equals(Variable k) {
		return this.id == k.id;
	}
}
