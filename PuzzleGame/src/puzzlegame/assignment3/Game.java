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
		
		
		int numEvolutions = 0;
		int maxEvolutions = 700;
		
		
		while(numEvolutions < maxEvolutions){
		//Add mutation
		//int mutationCount = 0;
		int mutationRate = 400; //1/mutation rate to be mutated
		double mutationAverage = 1.8;
		for(int i = 0; i < 100; i++)
		{
			double[] chromosome = population.row(i);
			for(int j = 0; j < chromosome.length; j++)
				
				if(r.nextInt(mutationRate)==0){
					//Pick random chromosone
					int mut = r.nextInt(291);
					double gaus = r.nextGaussian();
					//System.out.println("Mutating chromosome[" + mut + "]: " + chromosome[mut] + " Mutation count : " + mutationCount);
					chromosome[mut]+= mutationAverage * gaus;
					//chromosome[mut]+=chromosome[mut]*mutationAverage;
					//System.out.println("New value chromosome[" + mut + "]: " + chromosome[mut]);
					//mutationCount++;
				}
		}
		//Done adding mutations
		
		//Natural Selection
		
		//Choose pair of chromosones
		int numTournaments = 10;
		int probToSurvive = 66;
		for(int x = 0; x < numTournaments; x++){
			int cNum1 = r.nextInt(100); //First chromosome num
			int cNum2 = r.nextInt(100); //Second chromosome num
			
			double [] chromoOne = population.row(cNum1);
			double [] chromoTwo = population.row(cNum2);
			
			//if(numEvolutions > maxEvolutions-20)
				//System.out.println("Chromo one: " + Arrays.toString(chromoOne));
			
			//If they aren't the same chromosome, continue to do battle! Also check if they aren't a dead chromo
			//I'm assuming the chances of 80 being zero are near to none
			if(cNum1 != cNum2 && chromoOne[80] != 0.0 && chromoTwo[80]!=0.0){
				
				//System.out.print("Battling " + cNum1 + " and " + cNum2 + " ");
				//IAgent red = new IAgent();
				
				int winner = 0;
				try {
					winner = Controller.doBattleNoGui(new NeuralAgent(chromoOne), new NeuralAgent(chromoTwo));
				} catch (Exception e) {
					e.printStackTrace();
				}

				if(winner == 1){
					//System.out.print("ChromoOne won.\n");
					int killLoser = r.nextInt(100);
					
					if(killLoser <= probToSurvive){ //67% to killLoser and live
						for(int i = 0; i < chromoTwo.length; i++)
							population.row(cNum2)[i] = 0; //Kill the chromoOne
						//System.out.println(" Killed  -- " + cNum2);
					}
					else{ //Oops he gets to be killed
						for(int i = 0; i < chromoOne.length; i++)
							population.row(cNum1)[i] = 0; //Kill the chromoOne
						//System.out.println(" Killed -- " + cNum1);
					}
				}
				else if(winner == -1){
					//System.out.print("ChromoTwo won.\n");
					
					int killLoser = r.nextInt(100);
					
					if(killLoser <=probToSurvive){ //67% to killLoser and live
						for(int i = 0; i < chromoOne.length; i++)
							population.row(cNum1)[i] = 0; //Kill the chromoOne
						//System.out.println(" Killed  -- " + cNum1);
					}
					else{ //Oops he gets to be killed
						for(int i = 0; i < chromoTwo.length; i++)
							population.row(cNum2)[i] = 0; //Kill the chromoOne
						//System.out.println(" Killed -- " + cNum2);
					}
					
				}
				else{
					//System.out.println("Nobody won?!");
				}
				
			}	
		}//End Natural Selection for loop
		
		//Replenish the population!
		
		int numCandidates = 5;
		double difference = 0.05;
		for(int i = 0; i < 100; i++){
			
			//If its a dead chromo, make a baby!!
			if(population.row(i)[0] == 0.0){
				int parent1 = r.nextInt(100); //Pick first parent
				
				while(parent1==i || population.row(parent1)[0] ==0.0) //Make sure its not the same as the dead child
					parent1 = r.nextInt(100);
				
				int candidates[] = new int[numCandidates];
				int parent2 = 0;
				for(int x = 0; x < numCandidates; x++){
					parent2 = r.nextInt(100); //Pick second parent
					
					while(parent2 == parent1 || parent2 == i || population.row(parent2)[0] ==0.0) //Make sure its not the same as the dead child or the first parent
						parent2 = r.nextInt(100);
					candidates[x] = parent2;
					//System.out.println("New candidate: " + Arrays.toString(population.row(parent2)));
				
				}
				
				//Find whos the most similiar
				double[] dad = population.row(parent1);
				//System.out.println("Dad: " + Arrays.toString(dad));
				int bestMom = 0;
				double parentDifference = 5000000; //We have hugely different parents
				double testDifference;
				for(int x = 0; x < numCandidates; x++){
					double[] testMom = population.row(candidates[x]);
					//System.out.println("Test mom: " + Arrays.toString(testMom));
					testDifference = 0;
					
					for(int c = 0; c < dad.length; c++){
						testDifference+= Math.pow((dad[c] - testMom[c]), 2);
					}
					if(testDifference < parentDifference && testMom[0]!=0.0){
						parentDifference = testDifference;
						bestMom = candidates[x];
						//System.out.println("Test diff: " + testDifference + " parentDiff: " + parentDifference + "bestMom: " + bestMom);
					}
						
				}//Done finding best parent
				
				//Lets mate!
				double[] mom = population.row(bestMom);
				//System.out.println("Mother: " + Arrays.toString(mom));
				for(int x = 0; x < dad.length; x++){
					int rand = r.nextInt(2);
					
					if(rand == 0){
						//if(dad[x] == 0.0)
						//	System.out.println("Dad has a zero?!?");
						//else
						population.row(i)[x] = dad[x];
						
					}
					else{
						//if(mom[x] == 0.0)
						//System.out.println("Dad has a zero?!?");
						//else
						population.row(i)[x] = mom[x];
					}
				}
				
				//System.out.println("Done making the child.");
				//System.out.println("Dad: " + Arrays.toString(dad));
				//System.out.println("Mother: " + Arrays.toString(mom));
				//System.out.println("Dad: " + parent1 + " Mom: " + parent2 + " New child " + i + " : " + Arrays.toString(population.row(i)));
			}
		}

		numEvolutions++;
		//System.out.println("Evolution number: " + numEvolutions);
	}//End of while



		// Return an arbitrary member from the population
		int num = r.nextInt(100);
		
		for(int x = 0; x < 100; x++){
			try {
				if(Controller.doBattleNoGui(new ReflexAgent(), new NeuralAgent(population.row(x))) == -1)
					return population.row(x);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("None won.");
		return population.row(num);
	}


	public static void main(String[] args) throws Exception
	{
		double[] w = evolveWeights();
		System.out.println(Arrays.toString(w) + "\n" + "\n");
		System.out.println(Controller.doBattleNoGui(new ReflexAgent(), new NeuralAgent(w))); //Looking for -1
		//Controller.doBattle(new ReflexAgent(), new NeuralAgent(w));
		
		//[1.4278062179093252, -0.8414658067512626, -3.430878069485397, -0.5530266700798886, -3.7339007174276717, -0.8271989009580576, -0.6865147401628494, -0.03869713902799515, -1.3806921654326125, 2.82242913120967, -0.9881675331449635, 2.885837730211544, -0.0058013211444239874, 5.078255543446284, 0.6267748150693737, 0.3285590569693366, 0.41050498765657784, -1.549613381323693, 0.8528682384816878, 1.5051028086458151, 1.3172907657501758, 3.6674043717425033, 1.054308872084251, -0.529803625255931, 1.664950175309028, 0.9658747920350554, -2.4344823539549334, -9.357918959529005E-4, 0.8590548427108929, 4.120906037644227, 3.1953840029126934, -0.3236926915185312, -0.049708527105420605, -0.016176689668226236, 0.021817964639907036, -1.1258030042411382, -0.8954386237409302, 0.5928425599268046, 2.3833773527779325, -0.5989372285582482, -0.004129206161506073, -4.22280302877334, 5.297672135645984, 4.017972623405293, -0.4014925640692525, 1.092304142746835, -2.1014456880214643, -4.125808036084513, -1.3910958126453326, 0.028562028878741905, -0.7648797695333228, 1.0715405393042952, -0.0429649162944304, -2.176979410916771, 0.05507403125424082, 2.0259868860649726, -0.2864410946819339, 0.029464799622009485, -0.403798268948381, -0.9286469835858872, 3.875835995216954, 0.007703429456867757, 1.1812341244413471, -1.6781289306429101, 0.40544420609098647, 0.011914996502699482, 0.021579065503858817, 0.0491275121392664, 0.5424253793028335, 1.9754773593778199, -0.02078035487788186, 0.015383935091437186, -1.4874290944857007, 1.3880191948104526, 1.007914221154071, -0.5201078416930786, 3.3765110039824204, 3.959113593419813, 4.056673290182691, -0.009469597020618042, -0.0933025624075741, -1.598519894622842, -1.1436546396602116, -0.006129120144730517, 5.592594219013105, -4.6849991047450485, -1.124178828890901, 2.2275096860375094, -1.6243912328608245, -3.9708823480901754, 3.2982308161336245, 1.5384100044229219, 0.016149544806168953, 5.041060176196333, 0.5247048918729021, 0.019421031039903305, -0.011254208650366193, -0.9835093475410646, -1.5317197265844957, 0.7231820684485295, -0.8145676059904946, 3.717669267156177, 1.6103793232106463, -1.779494608189427, -0.6725595721042474, 0.02147878414835494, -1.070809631361301, 1.4446747254561327, -0.019457866147553533, 2.9520334975666334, 2.024499075581172, 4.623912638110536, -1.0504686371604346, -4.935949983133007, -0.764917202541169, 0.07128896421003739, 0.4211672809920537, -2.3610975402664494, 0.025083410176054553, 0.003976425364538126, -1.7423205655134477, 1.2360226141222597, -1.962302423486924, -3.791955846773207, 2.618055038059088, -0.02342326736431585, 0.23804179443330864, -0.852531364957433, -0.532158255611194, 0.7338112737144074, -2.7568016933824624, 0.11302818942249883, -1.639792134742708, -0.22292573013783096, 3.406555614516079, 0.02639693309936592, 0.01407045216090671, 0.09423953747871322, 1.7741747695506496, 3.164962304805893, -0.28845922294544607, -1.6175010200088258, 1.7834869758205496, -0.00813436333100356, 0.029513035928942038, -4.717474335743921, -2.015642635344229, -1.1303957576011054, -0.0028959321275578917, 4.17797746491587, -0.013991496056339772, 1.3236739894447955, -2.1922142793992796, -0.7460959542608455, -0.10155408104496266, 0.8625560623167834, -2.316721317652577, -0.13993092809836205, -0.5085709506834823, -0.03242087818062531, -0.9138268523151487, -3.89869660176981, 3.7931705471324983, 0.7736022277058662, 0.045822559177341696, 1.153237736923841, -0.008634474022097069, -1.4372275698692605, 2.214895461804625, -1.5511884499540214, 0.0163081092147714, -0.3403174875022318, -0.018104340692746017, 0.045645555302397445, 0.3764180447861064, 1.0962117378834024, 1.5923730681601274, 3.1350556302921273, -1.6618135498067717, -3.609688679707067, -0.09851510791778564, -0.3260429425978005, 1.937210534588009, -3.402613721027617, 3.049417489110165, -2.302558246987306, -0.01606415400405845, 0.016630573285535653, 2.0275617069557716, -0.1807913665991987, -2.669002651733684, -1.5943492408889088, -2.67770818716589, 4.412455057447899, 2.1053550720458456, 1.8147547387464311, -3.855896728175755, -3.6722364635677067, -5.637348516455976, 0.8136094618750858, -0.03851645764863586, 2.0230061558308496, -0.8702016777480988, 1.868691488197543, -0.053796887700301006, -0.34745855696880223, -2.889428337117918, 0.017205831163966467, -5.1525890309288345, -0.13878439223017536, 2.0584945575667546, 1.48127430955818, 0.01121509363621666, 3.5873669309295932, 0.2431834988947903, -0.3132692394180031, 1.36879731291786, -2.0964148366865203, 2.45386013444738, 0.5523809653918539, 2.4449124533412188, 0.2519806346682446, 2.934664411525798, -1.1852024118998112, -4.48099658580024, -5.33616953889075, 0.758488600688501, 0.5175965834025389, -0.04848671135473764, -0.0726669380250318, -0.19871125730853967, 0.1377847849853322, -3.2004178273825032, -2.2102497753239585, -1.4135307645319994, 0.18020547640834006, -3.8482332453816643, 0.016170052250339816, -5.815331984171178, -0.3615651042554027, 2.1567636077013, 2.6039426640914036, 0.9959982656521289, 1.2165487980096932, -1.6688478983228394, -1.2352901038641082, 3.2143753302914173, -0.02525381902687531, 1.2829504796034583, 0.020882863882066827, 0.03126242395379702, 7.2025004525595495, -1.8531660452731549, 0.3123891975698835, 0.16169590637998957, 0.9293827795934003, 1.101791356818349, -2.657041572747084, 3.0766549926745093, 0.052816004239998084, 2.2535452559191618, 0.5117581127272232, 1.7825955522273098, -4.113031843050609, -0.3498172503931128, -1.4688725052381, 1.1544170955281734, 0.024996572018149633, 0.25895707143876723, -1.9461471562670027, 6.010018946578572, 0.014077317822364122, 4.907995564681843, 11.765932414279082, 0.47994659790790917, -0.04921634691221467, 0.028029631658977806, 0.004936227342838463, -0.461863575951299, 0.042035954906056196, -0.049688252529510035, 0.8284372033716509, 7.582748017159507E-4, -2.793977868818394, -0.6961662395060646, 0.657882445756073, -0.21293861999664077, 3.0854382705900787, -3.4600899283586206, -0.07231118201456643, -3.424624000289568]


	}

}
