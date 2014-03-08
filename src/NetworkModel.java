import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 * Class NetworkModel provides data model for input network and also
 * implements functions for file handling, input processing and minimum
 * order computations.
 */
public class NetworkModel {
	String networkType; // Bayesian or Markov
	int countVariables; // Total number of variables in the network
	ArrayList<Function> functionList; // Functions defined over network
	ArrayList<Variable> variablesList; // All variables in the network at any given time
	PriorityQueue<Variable> minOrder; // Order of variables for variable elimination

	public NetworkModel() {
		this.variablesList = new ArrayList<Variable>();
		this.minOrder = new PriorityQueue<Variable>(100,
				new Comparator<Variable>() {
					public int compare(Variable var1, Variable var2) {
						return (new Integer(var1.neighboursList.size())
								.compareTo(new Integer(var2.neighboursList.size())));
					}
				});
		this.functionList = new ArrayList<Function>();
	}

	/**
	 * This method parses input file and initializes data model for network
	 * @param networkFile is the name of network file
	 * @param evidenceFile is the name of evidence file
	 */
	public void read(String networkFile, String evidenceFile) {

		try {
			Scanner fs = new Scanner(new File(networkFile));
			this.networkType = fs.next();
			this.countVariables = fs.nextInt();

			// Initialize variable list
			for (int i = 0; i < this.countVariables; i++) {
				this.variablesList.add(new Variable(fs.nextInt(), i));
			}

			// Initialize factor list
			int numFactors = fs.nextInt();
			for (int i = 0; i < numFactors; i++) {
				this.functionList.add(new Function());
			}

			// Store variables in factors
			int numVarsForFactor = 0;
			for (int i = 0; i < this.functionList.size(); i++) {
				numVarsForFactor = fs.nextInt();
				for (int j = 0; j < numVarsForFactor; j++) {
					this.functionList.get(i).variablesList
							.add(this.variablesList.get(fs.nextInt()));
				}
			}

			// Store table in factors
			int tableSize = 0;
			double value;
			LogValue log;
			for (int i = 0; i < this.functionList.size(); i++) {
				tableSize = fs.nextInt();
				for (int j = 0; j < tableSize; j++) {
					value = fs.nextDouble();
					if (value == 0)
						log = new LogValue(value, true);
					else
						log = new LogValue(Math.log(value), false);
					this.functionList.get(i).tableList.add(log);
				}
			}

			// Add neighbors 
			addNeighbours();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// Read evidence file
		try {
			Scanner fs = new Scanner(new File(evidenceFile));
			int countEvidences = fs.nextInt();
			Variable var;
			for (int i = 0; i < countEvidences; i++) {
				var = this.variablesList.get(fs.nextInt());
				var.isInstantiated = true;
				var.value = fs.nextInt();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This function writes output into output file
	 * @param probablity is probability distribution given evidence
	 * @param maxClusterSize is the maximum cluster size
	 * @param fileName is the name of output file
	 * @param executionTime is total time taken by variable elimination algorithm
	 */
	public void writeOutput(double probability,int maxClusterSize, String fileName, long executionTime) {
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(
					fileName));
			System.out.println("---------------------------RESULT------------------------------");
			System.out.println("Maximum width: " + maxClusterSize);
			System.out.println("Log Base e probability of evidence = " + probability);
			System.out.println("Log Base 10 probability of evidence = " + probability/2.303);
			System.out.println("Probability of evidence = " + Math.exp(probability));			
			System.out.println("Total execution time = "+ executionTime+" seconds!");
			
			
			output.write("---------------------------RESULT------------------------------");
			output.write("\nMaximum width: " + maxClusterSize);
			output.write("\nLog Base e probability of evidence = " + probability);
			output.write("\nLog Base 10 probability of evidence = " + probability/2.303);
			output.write("\nProbability of evidence = " + Math.exp(probability));
			output.write("\nTotal execution time = "+ executionTime+" seconds!");
			output.write("\n--------------------------------------------------------------");
		
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Output file: " + fileName+" written successfully!");	
		System.out.println("--------------------------------------------------------------");
	}
	
	/**
	 * This function modifies neighbor list of Functions by adding 
	 * variables based on variable list
	 */
	private void addNeighbours() {
		int size;
		Variable current, runner;
		for (Function f : this.functionList) {
			size = f.variablesList.size();
			for (int i = 0; i < size - 1; i++) {
				current = f.variablesList.get(i);
				for (int j = i + 1; j < size; j++) {
					runner = f.variablesList.get(j);
					if (!current.neighboursList.contains(runner))
						current.neighboursList.add(runner);
					if (!runner.neighboursList.contains(current))
						runner.neighboursList.add(current);
				}
			}
		}
		for (Variable v : this.variablesList) {
			if (v.isInstantiated == false) {
				minOrder.add(v);
			}
		}
	}

	/**
	 * This function adds fill edges to into a variable  
	 * @param v is the variable for which edges are to be added
	 */
	private void addFillEdges(Variable v) {
		int size = v.neighboursList.size();
		Variable current, runner;
		for (int i = 0; i < size - 1; i++) {
			current = v.neighboursList.get(i);
			minOrder.remove(current);
			for (int j = i + 1; j < size; j++) {
				runner = v.neighboursList.get(j);
				if (!current.neighboursList.contains(runner)
						&& !runner.neighboursList.contains(current)) {
					current.neighboursList.add(runner);
					runner.neighboursList.add(current);
				}
			}
			minOrder.add(current);
		}
	}
	
	/**
	 * This function instantiates all evidence variables
	 */
	public void instantiate() {
		ProcessTableThread[] myThreads = new ProcessTableThread[1000];
		
		// Find variables in a function to be instantiated
		for (Variable v : this.variablesList) {
			if (v.isInstantiated) {
				for (Function f : this.functionList) {
					if (f.variablesList.contains(v)) {
						f.evidanceList.add(v);
					}
				}
			}
		}

		int i = 0;
		for (Function f : this.functionList) {

			if (f.evidanceList.size() > 0) {
				myThreads[i++] = new ProcessTableThread(f, f.evidanceList);

			}
		}
		try {
			for (int j = 0; j < i; j++)
				myThreads[j].join();
		} catch (InterruptedException e) {
			System.out.println("Thread interrupted.");
		}
	}

	/**
	 * This function multiplies two functions
	 * @param f1 is first function
	 * @param f2 is second function
	 */
	public Function product(Function f1, Function f2) {
		Function newFactor = new Function();
		
		// Get the variable for newFactor
		for (Variable v2 : f2.variablesList) {
			for (Variable v1 : f1.variablesList) {
				newFactor.addVariable(v1);
			}
			if (!newFactor.variablesList.contains(v2))
				newFactor.addVariable(v2);
		}

		Collections.sort(newFactor.variablesList, new Comparator<Variable>() {
			public int compare(Variable v1, Variable v2) {
				return (new Integer(v1.id).compareTo(new Integer(v2.id)));
			}
		});

		HashMap<Integer, Integer> values = new HashMap<Integer, Integer>();
		LogValue value1, value2, product;
		for (int i = 0; i < newFactor.tableList.size(); i++) {
			values = newFactor.getSettingsFromAddress(i);
			value1 = f1.tableList.get(f1.getAddressFromSettings(values));
			value2 = f2.tableList.get(f2.getAddressFromSettings(values));
			if (value1.isZero || value2.isZero) {
				product = new LogValue(0.0, true);
			} else
				product = new LogValue(value1.data + value2.data, false);
			newFactor.tableList.set(i, product);
		}
		return newFactor;
	}

	/**
	 * This function sums out a variable from function 
	 * @param f is function from which variable is to be removed
	 * @param vout is variable which is to be removed
	 */
	public void sumout(Function f, Variable vout) {
		int size = f.tableList.size();
		int blockSize = size;
		for (Variable v : f.variablesList) {
			blockSize = blockSize / v.scope;
			if (vout == v)
				break;
		}

		int newSize = size / vout.scope;

		ArrayList<LogValue> table = new ArrayList<LogValue>(newSize);
		LogValue sum, value;
		for (int i = 0; i < size; i++) {
			sum = new LogValue(0.0, true);
			if (i - blockSize >= 0) {
				if ((i - blockSize) % (blockSize * vout.scope) == 0) {
					i += (vout.scope - 1) * blockSize;
				}
				if (i >= size)
					break;
			}
			for (int j = 0; j < vout.scope; j++) {
				value = f.tableList.get(i + j * blockSize);
				if (sum.isZero && !value.isZero)
					sum = value;
				else if (!sum.isZero && !value.isZero)
					if (sum.data > value.data)
						sum.data = sum.data
								+ Math.log(1 + Math.exp(value.data - sum.data));
					else
						sum.data = value.data
								+ Math.log(1 + Math.exp(sum.data - value.data));
			}
			table.add(sum);
		}
		f.variablesList.remove(vout);

		// Add fill edge
		addFillEdges(vout);

		// Remove edges from this variable
		for (Variable v : vout.neighboursList) {
			v.neighboursList.remove(vout);
		}

		f.tableList = table;
	}
	
}
