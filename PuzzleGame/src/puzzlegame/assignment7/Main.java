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
		 
		// Pick an action
		//Let i be the current state;
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
			for(int candidate = 0; candidate < 4; candidate++)
				//if(Q(i, candidate) > Q(i, action))
				if(arr.checkAction(candidate) > arr.checkAction(action))
					action = candidate;
		}

		// Do the action
		arr.do_action(action);
		/*Let j be the new state

		// Learn from that experience
		Apply the equation below to update the Q-table.
		Where a = action.
		Q(i,a) refers to the Q-table entry for doing action "a" in state "i".
		Q(j,b) refers to the Q-table entry for doing action "b" in state "j".
		Use 0.1 for αk. (Don't get "αk" mixed up with "a".)
		use 0.97 for γ (gamma).
		A(j) is the set of four possible actions, {<,>,^,v}.
		r(i,a,j) is the reward you obtained when you landed in state j.
		
		Equation:
		Q(i,a) = (1-0.1)*Q(i,a) + 0.1(r(i,a,j) + 0.97 * max Q(j,b))

		// Reset
		If j is the goal state, teleport to the start state.
		*/
	
	}
	
	
	
}

class Matrix{
	
	int cols;
	int rows;
	char[][] arr;
	int currR;
	int currC;
	int startR;
	int startC;
	int endR;
	int endC;
	
	public Matrix(int row, int col){
		cols = col;
		rows = row;
		arr = new char[rows][cols];
		
		for(int r = 0; r < rows; r++)
			for(int c = 0; c < cols; c++)
				arr[r][c] = '.';
		
		startR = rows-1;
		startC = 0;
		
		endC = cols-1;
		endR = 0;
		
		arr[startR][startC] = 'S';
		arr[endR][endC] = 'E';
	}
	
	public int checkAction(int candidate) {

		return 0;
	}

	public void do_action(int action) {
		
		
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
				if(c == 5)
					arr[r][c] = '#';
				if(c==5 && ((r == 3) || (r==4)))
					arr[r][c] = '.';
			}
		}
	}
	
	
	
}

