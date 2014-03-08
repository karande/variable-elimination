# Variable Elimination

## Probabilistic Graphical Model Exact Inferencing
Variable Elimination is an exact inference algorithm for Bayesian networks and Markov random fields.

## Introduction
Implement Variable Elimination. 
Read Bayesian and Markov networks and evidence file specified using .uai format (http://graphmod.ics.uci.edu/uai08/FileFormat).
Variable elimination algorithm has three steps:

1. Instantiate Evidence (Reduce the CPTs or factors)

2. Order the variables

3. Eliminate variables one by one along the order. To eliminate a variable, we
compute a product of all functions that mention the variable and sum-out
the variable from the result. Then we replace all functions that mention
the variable with this new function


## How to run

1) Navigate into the VariableElimination folder

2) Launch command prmpt

3) Run VE_exe.exe on command promt using following command

	$ java -jar VE_exe.jar <InputFileName> <EvidenceFileName>

## Sample Input and Output

	$ java -jar VE_exe.jar BN_4.uai BN_4.uai.evid
	
---------------------------RESULT------------------------------

Maximum width: 19

Log Base e probability of evidence = -3.991165429944898

Log Base 10 probability of evidence = -1.7330288449608764

Probability of evidence = 0.01847816655753799

Total execution time = 17 seconds!

Output file: output1611.txt written successfully!

--------------------------------------------------------------

NOTE: 
1) OutputXXXX.txt file is created with the above output in the same folder

2) BN_6.uai and BN_8.uai produces Out Of Memory Error: Java heap space 
because min order generated produces higher treewidth.


