package puzzlegame.assignment7;

import java.util.Random;


public class Main {
	static char[][] arr;

	public static void main(String[] args)
	{

		Random rand = new Random();
		Matrix arr = new Matrix(10,20);
		arr.addWall();
		//arr.print();
		int action;
		int count = 10000;
		int loopNumber = 0;
		
		System.out.println("Assignment 7 - Nathaniel Webb - Q Learning at " + count + " iterations.");
		while(loopNumber < count){
			int numberMoves = 0;
			while(!arr.hitGoal()){
				
				double e = 0.05;
				if(rand.nextDouble() < e)
				{
					// Explore (pick a random action)
					action = rand.nextInt(4);
				}
				else
				{
					// Exploit (pick the best action)
					action = 0;
					for(int candidate = 0; candidate < 4; candidate++){
				//	if(arr.checkAction(candidate) != arr.hitWall || arr.checkAction(action) != arr.hitWall)
						if(arr.checkQTable(candidate) > arr.checkQTable(action)){
							//System.out.println("Candiate: " + candidate + " val: " + arr.checkAction(candidate));
							action = candidate;
						}
						//System.out.println("Candiate: " + candidate + " val: " + arr.checkAction(candidate));
					}
				}
				String dir = "";
				switch(action){
				case 0: dir = "left"; break;
				case 1: dir = "up"; break;
				case 2: dir = "right"; break;
				case 3: dir = "down"; break;
				}
				//System.out.println("Best action: " + dir);
				// Do the action
				arr.do_action(action);
				numberMoves++;
				
				
				
			}
			//arr.printQTable();
			//System.out.println("Found goal. Moves: " + numberMoves + " Loop number: " + loopNumber);
			arr.reset();
			loopNumber++;
		}
		
		//arr.printQTable();
		arr.printWaveBoard();
	}
	
	
	
}

class Matrix{
	
	int cols;
	int rows;
	char[][] arr;
	double qTable[][];
	int currR;
	int currC;
	int startR;
	int startC;
	int endR;
	int endC;
	
	int normalMove = -1;
	int wrongMove = -1000;
	int hitWall = -400;
	int goal = 10;
	
	public Matrix(int row, int col){
		cols = col;
		rows = row;
		arr = new char[rows][cols];
		qTable = new double[rows][cols];
		
		for(int r = 0; r < rows; r++)
			for(int c = 0; c < cols; c++){
				arr[r][c] = '.';
				qTable[r][c] = 0.0;
			}
		
		startR = rows-1;
		startC = 0;
	//	qTable[startR][startC] = -1.0;
		endC = cols-1;
		endR = 0;
		
		currR = startR;
		currC = startC;
		
		arr[startR][startC] = 'S';
		arr[endR][endC] = 'G';
	}
	
	public void printQTable() {
		for(int r = 0; r < rows; r++){
			for(int c = 0; c < cols; c++){
				System.out.print(qTable[r][c] +", ");
			}
		System.out.println();
		}
		
	}

	public void transverseQTable(){
		reset();
		//print();
		while(!hitGoal()){
			int a = 0;
			for(int candidate = 0; candidate < 4; candidate++){
			//	System.out.println("Q val: " + checkQTable(candidate));
				
			
				if(checkQTable(candidate) > checkQTable(a)){
					a = candidate;
					
				}
			}
	
			
			if(a == 0){ //Left
				arr[currR][currC] = '<';
				currC = currC - 1;
			}
			else if(a == 1){ //Up
				arr[currR][currC] = '^';
				currR = currR-1;
			}
			else if(a == 2){ //Right
				arr[currR][currC] = '>';
				currC = currC + 1;
			}
			else if(a== 3){ //Down
				arr[currR][currC] = 'v';
				currR = currR + 1;
			}
			
			//System.out.println("CurrR: " + currR + "currC: " + currC);
		}
		print();
		
		
	}
	
	public void printWaveBoard() {
		//reset();
		for(int r = 0; r < rows; r++){
			for(int c = 0; c < cols; c++){
				
				if(arr[r][c]=='.'){
					//Compute best action
					int a = 0;
					for(int candidate = 0; candidate < 4; candidate++){
					//	System.out.println("Q val: " + checkQTable(candidate));
						if(checkQTableAnywhere(candidate,r,c) > checkQTableAnywhere(a,r,c)){
							a = candidate;
							
						}
					}
					
					
					if(currR == 0 && currC == cols - 1)
						System.out.println("Best action: " + a);
					
					
					if(a == 0){ //Left
						System.out.print("<");
					//	currC = currC - 1;
					}
					else if(a == 1){ //Up
						System.out.print("^");
					//	currR = currR-1;
					}
					else if(a == 2){ //Right
						System.out.print(">");
						//currC = currC + 1;
					}
					else if(a== 3){ //Down
						System.out.print("v");
						//currR = currR + 1;
					}
					
				}
				else
					System.out.print(arr[r][c]);
			}
			System.out.println();
		}
	}

	private double checkQTableAnywhere(int action, int r, int c) {
	
		if(action == 0){ //Move left
			if(c == 0)
				return 0.0;
			else
				return qTable[r][c-1];
			
		}
		else if(action == 1){ //Move up
			if(r == 0)
				return 0.0;
			else
				return qTable[r-1][c];
			
		}
		else if(action == 2){ //Move right
			if(c == cols - 1)
				return 0.0;
			else
				return qTable[r][c+1];
			
		}
		else if(action == 3){ //Move down
			if(r == rows-1)
				return 0.0;
			else
				return qTable[r+1][c];
		}
		return 0.0;
	}

	public boolean hitGoal() {
		
		return (currR == endR && currC == endC);
	}
	
	public void reset(){
		currR = startR;
		currC = startC;
	}

	public double checkQTable(int action){

		if(action == 0){ //Move left
			if(currC == 0)
				return 0.0;
			else
				return qTable[currR][currC-1];
			
		}
		else if(action == 1){ //Move up
			if(currR == 0)
				return 0.0;
			else
				return qTable[currR-1][currC];
			
		}
		else if(action == 2){ //Move right
			if(currC == cols - 1)
				return 0.0;
			else
				return qTable[currR][currC+1];
			
		}
		else if(action == 3){ //Move down
			if(currR == rows-1)
				return 0.0;
			else
				return qTable[currR+1][currC];
		}
		
		return 0.0;
		
		
		
	}
	
	public int checkAction(int action) {
		
		if(action == 0){ //Move left
			if(currC == 0)
				return wrongMove;
			else if(arr[currR][currC-1] == '#')
				return hitWall;
			else if(arr[currR][currC-1] == 'G')
				return goal;
			else
				return normalMove;
			
		}
		else if(action == 1){ //Move up
			if(currR == 0)
				return wrongMove;
			else if(arr[currR-1][currC] == '#')
				return hitWall;
			else if(arr[currR-1][currC] == 'G')
				return goal;
			else
				return normalMove;
			
		}
		else if(action == 2){ //Move right
			if(currC == cols-1)
				return wrongMove;
			else if(arr[currR][currC+1] == '#')
				return hitWall;
			else if(arr[currR][currC+1] == 'G')
				return goal;
			else
				return normalMove;
			
		}
		else if(action == 3){ //Move down
			
			if(currR == rows-1)
				return wrongMove;
			else if(arr[currR+1][currC] == '#')
				return hitWall;
			else if(arr[currR+1][currC] == 'G')
				return goal;
			else
				return normalMove;
		}
		
		return 0;
	}

	public void do_action(int action) {
		///0 = left,  1 = up, 2 = right, 3 = down

		//System.out.println("CheckngAction: " + action + " CurR: " + currR + " currC : " + currC + " val: " +  checkAction(action));
		if(checkAction(action) != wrongMove){
			int tempC = currC;
			int tempR = currR;
			double currVal = checkQTable(action);
			int reward = checkAction(action);
			//System.out.println("Doing action: " + action);
			if(action == 0){ //Left
				currC = currC - 1;
			}
			else if(action == 1){ //Up
				currR = currR-1;
			}
			else if(action == 2){ //Right
		
				currC = currC + 1;
			}
			else if(action == 3){ //Down
				currR = currR + 1;
			}
			//else do nothing?
			
			//Compute best action
			int a = 0;
			for(int candidate = 0; candidate < 4; candidate++){
				if(checkQTable(candidate) > checkQTable(a)){
					a = candidate;
				}
			}
			//System.out.println("(1-.1)*"+currVal+" + 0.1*("+checkAction(action)+") + 0.97 *"+checkQTable(a));
			qTable[tempR][tempC] = (1-.1)*currVal + 0.1* ((reward + 0.97 * checkQTable(a)) );
			//qTable[currR][currC] = currVal + 0.1* ((reward + 0.97 * checkQTable(a)) - currVal );
		//	System.out.println("Qtable["+currR+"]["+currC+"]:" + qTable[currR][currC]);
			//Q(i,a) = (1-0.1)*Q(i,a) + 0.1(r(i,a,j) + 0.97 * max Q(j,b))
		
		}
		
		
	}

	public void print() {
		for(int r = 0; r < rows; r++){
			for(int c = 0; c < cols; c++){
				System.out.print(arr[r][c]);
			}
			System.out.println();
		}
	}

	public void addWall(){
		for(int c = 0; c < cols; c++){
			for(int r = 0; r < rows; r++){
				if(c == 10){
					arr[r][c] = '#';
					//qTable[r][c] = (double)hitWall;
				}
				if(c==10 && ((r == 3) || (r==4)))
					arr[r][c] = '.';
			}
		}
	}
	
	
	
}

