package puzzlegame.assignment7;

import java.util.Random;


public class Main {
	static char[][] arr;
	static int rows = 10;
	static int cols = 10;
	
	public static void main(String[] args)
	{
		System.out.println("Assignment 7 Running.");
		Random rand = new Random();
		Matrix arr = new Matrix(10,10);
		arr.addWall();
		arr.print();
		int action;
		int count = 100;
		int loopNumber = 0;
		
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
						//if(Q(i, candidate) > Q(i, action))
						
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
			//	System.out.println("Best action: " + dir);
				// Do the action
				arr.do_action(action);
				numberMoves++;
				
				
			}
			System.out.println("Found goal. Moves: " + numberMoves + " Loop number: " + loopNumber);
			arr.reset();
			loopNumber++;
		}
		
	
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
	
	int normalMove = 1;
	int wrongMove = -1;
	int hitWall = -1;
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
		
		endC = cols-1;
		endR = 0;
		
		currR = startR;
		currC = startC;
		
		arr[startR][startC] = 'S';
		arr[endR][endC] = 'E';
	}
	
	public void printWaveBoard() {

		for(int r = 0; r < rows; r++){
			for(int c = 0; c < cols; c++){
				
				if(arr[r][c]=='.'){
					//Compute best action
					int a = 0;
					for(int candidate = 0; candidate < 4; candidate++){
						if(checkQTable(candidate) > checkQTable(a)){
							a = candidate;
						}
					}
					
					if(a == 0)
						System.out.print("<");
					else if(a == 1)
						System.out.print("^");
					else if(a==2)
						System.out.print(">");
					else if(a==3)
						System.out.print("v");
					
				}
				else
					System.out.print(arr[r][c]);
			}
			System.out.println();
		}
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
				return (double)wrongMove;
			else if(arr[currR][currC-1] == '#')
				return (double)hitWall;
			else if(arr[currR][currC-1] == 'E')
				return (double)goal;
			else
				return qTable[currR][currC-1];
			
		}
		else if(action == 1){ //Move up
			if(currR == 0)
				return (double)wrongMove;
			else if(arr[currR-1][currC] == '#')
				return (double)hitWall;
			else if(arr[currR-1][currC] == 'E')
				return (double)goal;
			else
				return qTable[currR-1][currC];
			
		}
		else if(action == 2){ //Move right
			if(currC == cols-1)
				return (double)wrongMove;
			else if(arr[currR][currC+1] == '#')
				return (double)hitWall;
			else if(arr[currR][currC+1] == 'E')
				return (double)goal;
			else
				return qTable[currR][currC+1];
			
		}
		else if(action == 3){ //Move down
			
			if(currR == rows-1)
				return (double)wrongMove;
			else if(arr[currR+1][currC] == '#')
				return (double)hitWall;
			else if(arr[currR+1][currC] == 'E')
				return (double)goal;
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
			else if(arr[currR][currC-1] == 'E')
				return goal;
			else
				return normalMove;
			
		}
		else if(action == 1){ //Move up
			if(currR == 0)
				return wrongMove;
			else if(arr[currR-1][currC] == '#')
				return hitWall;
			else if(arr[currR-1][currC] == 'E')
				return goal;
			else
				return normalMove;
			
		}
		else if(action == 2){ //Move right
			if(currC == cols-1)
				return wrongMove;
			else if(arr[currR][currC+1] == '#')
				return hitWall;
			else if(arr[currR][currC+1] == 'E')
				return goal;
			else
				return normalMove;
			
		}
		else if(action == 3){ //Move down
			
			if(currR == rows-1)
				return wrongMove;
			else if(arr[currR+1][currC] == '#')
				return hitWall;
			else if(arr[currR+1][currC] == 'E')
				return goal;
			else
				return normalMove;
		}
		
		return 0;
	}

	public void do_action(int action) {
		///0 = left,  1 = up, 2 = right, 3 = down
		double currVal = qTable[currR][currC];
		if(checkAction(action) != wrongMove && checkAction(action) != hitWall){
			
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
		}
		//else do nothing?
		
		//Compute best action
		int a = 0;
		for(int candidate = 0; candidate < 4; candidate++){
			if(checkQTable(candidate) > checkQTable(a)){
				a = candidate;
			}
		}
		
		qTable[currR][currC] = (1-.1)*currVal + 0.1* (checkAction(action) + 0.97 * a );
		//System.out.println("Qtable["+currR+"]["+currC+"]:" + qTable[currR][currC]);
		//Q(i,a) = (1-0.1)*Q(i,a) + 0.1(r(i,a,j) + 0.97 * max Q(j,b))
		
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
				if(c == 5){
					arr[r][c] = '#';
					qTable[r][c] = (double)hitWall;
				}
				if(c==5 && ((r == 3) || (r==4)))
					arr[r][c] = '.';
			}
		}
	}
	
	
	
}

