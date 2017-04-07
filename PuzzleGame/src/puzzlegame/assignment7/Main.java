package puzzlegame.assignment7;

import java.util.Random;


public class Main {
	
	public static void main(String[] args)
	{
		System.out.println("Assignment 7 Running.");
		Random rand = new Random();
		/* 
		// Pick an action
		Let i be the current state;
		if(rand.nextDouble() < 0.05)
		{
			// Explore (pick a random action)
			action = rand.nextInt(4);
		}
		else
		{
			// Exploit (pick the best action)
			action = 0;
			for(int candidate = 0; candidate < 4; candidate++)
				if(Q(i, candidate) > Q(i, action))
					action = candidate;
		}

		// Do the action
		do_action(action);
		Let j be the new state

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
