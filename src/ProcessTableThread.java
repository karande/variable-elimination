import java.util.ArrayList;

/**
 * Class ProcessTableThread provide parallel solution for instantiating
 * multiple variables from different factors/functions simultaneously
 */
public class ProcessTableThread extends Thread {

	Function f; // Function from which variable are to instantiated
	ArrayList<Variable> var; // List of instance variables

	ProcessTableThread(Function f, ArrayList<Variable> var) {
		super();
		this.f = f;
		this.var = var;
		start(); // Start the thread
	}

	/**
	 * Starting method for thread which removes unrelated entries from 
	 * function table during instantiation
	 */
	public void run() {
		ArrayList<Integer> product = new ArrayList<Integer>();
		ArrayList<Integer> index = new ArrayList<Integer>();
		int tableSize;
		try {
			for (int i = 0; i < var.size(); i++)
				index.add(f.variablesList.indexOf(var.get(i)));

			for (int i = 0; i < var.size(); i++) {
				int p = 1;
				for (int j = f.variablesList.size() - 1; j >= 0; j--) {
					if (j < index.get(i))
						break;
					p *= f.variablesList.get(j).scope;
				}
				product.add(p);
			}
			
			tableSize = f.tableList.size();
			for (int j = tableSize - 1; j >= 0; j--) {
				boolean flag = true;
				for (int k = 0; k < var.size(); k++) {
					flag = flag
							&& ((j % product.get(k))
									/ ((product.get(k) / var.get(k).scope)) == var
									.get(k).value);
				}
				// Update function after removing unrelated entries
				if (flag == false)
					f.tableList.remove(j);
			}

			for (Variable v : var) {
				if (f.evidanceList.contains(v)) {
					f.variablesList.remove(v);
				}
			}

		} catch (Exception e) {
			System.out.println("Child interrupted.");
		} 
	}
}
