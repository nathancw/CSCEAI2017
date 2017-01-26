package puzzlegame.assignment2;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Color;

class Agent {

	void drawPlan(Graphics g, Model m) {
		g.setColor(Color.red);
		g.drawLine((int)m.getX(), (int)m.getY(), (int)m.getDestinationX(), (int)m.getDestinationY());
	}

	void update(Model m)
	{
		Controller c = m.getController();
		while(true)
		{
			MouseEvent e = c.nextMouseEvent();
			if(e == null)
				break;
			Block startState = new Block(m.getX(),m.getY(),0.0,null);
			Block goal = new Block(e.getX(),e.getY(),0.0,null);
			m.setDestination(e.getX(), e.getY());
		}
	}

	public static void main(String[] args) throws Exception
	{
		Controller.playGame();
	}
}

class Block {
	  public double cost;
	  Block parent;
	  float x;
	  float y;
	  
	  Block(float x, float y, double cost, Block par) {
		  this.cost = cost;
		  this.parent = par;
		  this.x = x;
		  this.y = y;
	  }
	}

class UFS {
		
	  public Block uniform_cost_search(Block startState, Block goal) {
		  
	    PriorityQueue frontier = new PriorityQueue();
	    Set beenthere = new TreeSet();
	    
	    //Block startState = new Block(Model.getX(),Model.getY(),0.0,null);
	    //startState.cost = 0.0;
	    //startState.parent = null;
	    
	    beenthere.add(startState);
	    frontier.add(startState);
	    
	    while(frontier.size() > 0) {
	      Block s = (Block) frontier.remove(); // get lowest-cost state
	      
	      if(s.x == goal.x && s.y == goal.y)
	        return s;
	      
	     /* for(int i = 0; i < )
	      
	      
	      
	      for each action, a {
	        child = transition(s, a); // compute the next state
	        acost = action_cost(s, a); // compute the cost of the action
	        if(child is in beenthere) {
	          oldchild = beenthere.find(child)
	          if(s.cost + acost < oldchild.cost) {
	            oldchild.cost = s.cost + acost;
	            oldchild.parent = s;
	          }
	        }
	        else {
	          child.cost = s.cost + acost;
	          child.parent = s;
	          frontier.add(child);
	          beenthere.add(child);
	        }
	      }*/
	    } 
	    throw new RuntimeException("There is no path to the goal");
	  }
	}