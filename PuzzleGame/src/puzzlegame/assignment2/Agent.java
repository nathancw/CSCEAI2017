package puzzlegame.assignment2;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
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
			UFS ufs = new UFS();
			ufs.uniform_cost_search(m, startState, goal);
		
			//m.setDestination(e.getX(), e.getY());
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
	  
	  void print(){
		  System.out.println("(" + x + "," + y + ") cost : " + cost);
	  }
	}

class BlockComparator implements Comparator<Block>
{
	public int compare(Block a, Block b)
	{
			if(a.x != b.x || a.y != b.y)
				return -1;
		return 0;
	}
}  
class CostComparator implements Comparator<Block>
{
	public int compare(Block a, Block b)
	{
			if(a.cost > b.cost)
				return 1;
			else if(a.cost < b.cost)
					return -1;
		return 0;
	}
}  

class UFS {
	BlockComparator comp;
	CostComparator costComp;
	PriorityQueue frontier;
	TreeSet<Block> beenThere;
	Stack path;
	
	  public Stack uniform_cost_search(Model m, Block startState, Block goal) {
		 
		comp = new BlockComparator();
		costComp = new CostComparator();
		frontier = new PriorityQueue(costComp);
		beenThere = new TreeSet<Block>(comp);
		path = new Stack<Block>();
	    //Block startState = new Block(Model.getX(),Model.getY(),0.0,null);
	    //startState.cost = 0.0;
	    //startState.parent = null;
	    
	    beenThere.add(startState);
	    frontier.add(startState);
	    System.out.println("Starting UFS");
	    while(frontier.size() > 0) {
	      Block s = (Block) frontier.remove(); // get lowest-cost state
	      
	      if(s.x == goal.x || s.y == goal.y){
	    	  goal.parent = s;
	    	  break;
	      }
	      //
	      //MoveUpRight(); //x+10, y-10;
	      MoveRight(m,s); //x+10
	      //MoveDownRight(); //x+10, y+10
	      //MoveDown(); //y+10
	     // MoveDownLeft();//x-10, y+10
	      //MoveLeft(); //x-10
	     // MoveUpLeft();//x-10, y-10
	      //MoveUp(); //y-10
	     
	    } 
	    Block current = goal;
	    while(current!=null){
	    	path.add(current.parent);
	    	current = current.parent;	
	    }
	    
	    current = (Block) path.pop();
	    while(!path.isEmpty()){
	    	current.print();
	    }
	    
	    return path;
	    //throw new RuntimeException("There is no path to the goal");
	  }

	private void MoveRight(Model m, Block root) {
		
		System.out.println("Moving right.");
		float x = (float) (root.x + 10.0);
		float y = (float) (root.y); //MAY BE TOO SLOW
		
		float cost = m.getTravelSpeed(x,y) * 10; //Cost is speed associated with the terrain square AND distance you will travel at that speed
		
		Block right = new Block(x,y,cost,root);
        Block oldChild;
        
        if(beenThere.contains(right)) {	//If right is in the set
         /* oldchild = beenThere.find(right);
          if(root.cost + cost < oldchild.cost) {
            oldchild.cost = root.cost + cost;
            oldchild.parent = root;
          }*/
        }
        else {
        	System.out.println("right isn't contained in the set");
          right.cost = root.cost + cost;
          right.parent = root;
          frontier.add(right);
          beenThere.add(right);
        }
      }
		
	}