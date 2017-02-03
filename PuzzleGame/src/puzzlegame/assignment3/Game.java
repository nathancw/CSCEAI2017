package puzzlegame.assignment3;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

class Game
{

	static double[] evolveWeights()
	{
		// Create a random initial population
		Random r = new Random();
		Matrix population = new Matrix(100, 291);
		for(int i = 0; i < 100; i++)
		{
			double[] chromosome = population.row(i);
			for(int j = 0; j < chromosome.length; j++)
				chromosome[j] = 0.03 * r.nextGaussian();
		}


		// Evolve the population
		// todo: YOUR CODE WILL START HERE.
		//       Please write some code to evolve this population.
		//       (For tournament selection, you will need to call Controller.doBattleNoGui(agent1, agent2).)
		
		//Add mutation
		int mutationCount = 0;
		int mutationRate = 500; //1/mutation rate to be mutated
		for(int i = 0; i < 100; i++)
		{
			double[] chromosome = population.row(i);
			for(int j = 0; j < chromosome.length; j++)
				
				if(r.nextInt(mutationRate)==0){
					//Pick random chromosone
					int mut = r.nextInt(291);
					double gaus = r.nextGaussian();
					System.out.println("Mutating chromosome[" + mut + "]: " + chromosome[mut] + " Mutation count : " + mutationCount);
					chromosome[mut]+= 0.05 * gaus;
					System.out.println("New value chromosome[" + mut + "]: " + chromosome[mut]);
					mutationCount++;
				}
		}
		//Done adding mutations
		
		//Natural Selection
		
		//Choose pair of chromosones
		int numTournaments = 5;
		int probToSurvive = 66;
		for(int x = 0; x < numTournaments; x++){
			int cNum1 = r.nextInt(100); //First chromosome num
			int cNum2 = r.nextInt(100); //Second chromosome num
			
			double [] chromoOne = population.row(cNum1);
			double [] chromoTwo = population.row(cNum2);
			
			//If they aren't the same chromosome, continue to do battle! Also check if they aren't a dead chromo
			//I'm assuming the chances of 80 being zero are near to none
			if(cNum1 != cNum2 && (chromoOne[80] != 0 || chromoTwo[80]!=0)){
				
				System.out.println("Battling " + cNum1 + " and " + cNum2);
				//IAgent red = new IAgent();
				
				int winner = 0;
				try {
					winner = Controller.doBattleNoGui(new NeuralAgent(chromoOne), new NeuralAgent(chromoTwo));
				} catch (Exception e) {
					e.printStackTrace();
				}

				if(winner == 1){
					System.out.print("ChromoOne won.");
					int killLoser = r.nextInt(100);
					
					if(killLoser <= probToSurvive){ //67% to killLoser and live
						for(int i = 0; i < chromoTwo.length; i++)
							population.row(cNum2)[i] = 0; //Kill the chromoOne
						System.out.println(" Killed chromoTwo -- " + cNum2);
					}
					else{ //Oops he gets to be killed
						for(int i = 0; i < chromoOne.length; i++)
							population.row(cNum1)[i] = 0; //Kill the chromoOne
						System.out.println(" Killed chromoOne -- " + cNum1);
					}
				}
				else if(winner == -1){
					System.out.print("ChromoTwo won.");
					
					int killLoser = r.nextInt(100);
					
					if(killLoser <=probToSurvive){ //67% to killLoser and live
						for(int i = 0; i < chromoOne.length; i++)
							population.row(cNum1)[i] = 0; //Kill the chromoOne
						System.out.println(" Killed chromoOne -- " + cNum1);
					}
					else{ //Oops he gets to be killed
						for(int i = 0; i < chromoTwo.length; i++)
							population.row(cNum2)[i] = 0; //Kill the chromoOne
						System.out.println(" Killed chromoTwo -- " + cNum2);
					}
					
				}
				else{
					System.out.println("Nobody won?!");
				}
				
			}	
		}//End Natural Selection for loop
		
		//Replenish the population!
		
		int numCandidates = 5;
		double difference = 0.05;
		for(int i = 0; i < 100; i++){
			
			//If its a dead chromo, make a baby!!
			if(population.row(i)[0] == 0){
				int parent1 = r.nextInt(100); //Pick first parent
				
				while(parent1==i) //Make sure its not the same as the dead child
					parent1 = r.nextInt(100);
				
				int candidates[] = new int[numCandidates];
				int parent2 = 0;
				for(int x = 0; x < numCandidates; x++){
					parent2 = r.nextInt(100); //Pick second parent
					
					while(parent2 == parent1 || parent2 == i) //Make sure its not the same as the dead child or the first parent
						parent2 = r.nextInt(100);
					candidates[x] = parent2;
				
				}
				
				//Find whos the most similiar
				double[] dad = population.row(parent1);
				int bestMom = 0;
				int parentDifference = 5000000; //We have hugely different parents
				for(int x = 0; x < numCandidates; x++){
					double[] testMom = population.row(candidates[x]);
					int testDifference = 0;
					
					for(int c = 0; c < dad.length; c++){
						testDifference+= Math.pow((dad[c] - testMom[c]), 2);
						if(testDifference < parentDifference){
							parentDifference = testDifference;
							bestMom = x;
						}
					}
						
				}//Done finding best parent
				
				//Lets mate!
				double[] mom = population.row(bestMom);
				
				for(int x = 0; x < dad.length; x++){
					int rand = r.nextInt(2);
					
					if(rand == 0)
						population.row(i)[x] = dad[x];
					else
						population.row(i)[x] = mom[x];
				}
				
				System.out.println("Done making the child.");
				System.out.println("Dad: " + Arrays.toString(dad));
				System.out.println("Mother: " + Arrays.toString(mom));
				System.out.println("New child: " + Arrays.toString(population.row(i)));
			}
		}




		// Return an arbitrary member from the population
		return population.row(0);
	}


	public static void main(String[] args) throws Exception
	{
		double[] w = evolveWeights();
		//Controller.doBattle(new ReflexAgent(), new NeuralAgent(w));
	}

}
