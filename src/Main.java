
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Random;

/**
 * Class Main implements Variable Elimination algorithm
 */
public class Main {
	
	public static void main(String[] args) {
		long currentTime, endTime;
		currentTime= System.currentTimeMillis();
		NetworkModel nm = new NetworkModel();
		String inputFile="";
		String evidFile="";
		try{
		inputFile = args[0];
		evidFile = args[1];
		}catch(Exception e){
			System.out.println("Format: VE_exe.jar inputFile evidFile");
			return;
		}
		
		// Read and model Bayesian/Markov network uai file and evidence file
		nm.read(inputFile, evidFile);	
		
		// Instantiate evidence variables
		nm.instantiate();	

		int maxClusterSize = 2;	
		ArrayList<Function> functionsList;
		Variable v;
		while(!nm.minOrder.isEmpty()) {
			
			// Extract variable which is next in the elimination order
			v = nm.minOrder.poll();
			functionsList = new ArrayList<Function>();
			for(Function f : nm.functionList) {
				if(f.variablesList.contains(v)) {
					functionsList.add(f);
				}
			}
			
			if(functionsList.size() > 0) {		
				Collections.sort(functionsList, new Comparator<Function>(){
					public int compare(Function f1, Function f2) {
						return(new Integer(f1.variablesList.size()).compareTo(new Integer(f2.variablesList.size())));
					}
				});
				
				// Multiply factors 
				Function f1  = functionsList.get(0);
				Function f2;
				Function newFactor = new Function(f1);
				nm.functionList.remove(f1);
				for(int i = 1; i < functionsList.size(); i++) {
					f2 = functionsList.get(i);
					newFactor = nm.product(newFactor, f2);
					nm.functionList.remove(f2);
				}
				
				// Sum-out the variable which is next in the elimination order 
				nm.sumout(newFactor, v);
				if(newFactor.variablesList.size() + 1 > maxClusterSize)
					maxClusterSize = newFactor.variablesList.size() + 1;
				
				// Update functions list with the newly created factor
				nm.functionList.add(newFactor);
			}
		}
		
		double probability = 0.0;
		for(Function f : nm.functionList) {
			probability += f.tableList.get(0).data;
		}
		
		Random rand = new Random();
	    int randomNum = rand.nextInt((9999 - 100) + 1) + 100;
		String outputFile = "output" + randomNum+".txt";
		endTime= System.currentTimeMillis();
		long executionTime = (endTime-currentTime)/1000;
		
		// Create output file
		nm.writeOutput(probability, maxClusterSize, outputFile, executionTime);		
	}
}