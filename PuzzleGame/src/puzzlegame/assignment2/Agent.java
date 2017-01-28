package puzzlegame.assignment2;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Color;

class Agent {
	Stack path = new Stack<Block>();
	int accum = 0;
	Block current;
	
	void drawPlan(Graphics g, Model m) {
		g.setColor(Color.red);
		g.fillOval(500, 500, 10, 10);	
	}

	void update(Model m)
	{
		Controller c = m.getController();
		accum++;
		if(accum == 5){
		
			if(!path.isEmpty()){
				current = (Block) path.pop();
				System.out.println("Setting dest");
				current.print();
				System.out.println("Setting dest to " + current.x + " ," + current.y);
				m.setDestination(current.x, current.y);
			}
			accum = 0;
		}
		
		//System.out.println("UDPATING" + accum);
		while(true)
		{
			MouseEvent e = c.nextMouseEvent();
			
			if(e == null)
				break;
			
			Block startState = new Block(m.getX(),m.getY(),(float) 0.0,null);
			UFS ufs = new UFS();
			Block goal = new Block(m.getX()+450,m.getY()-80,(float) 0.0,null);
			path = ufs.uniform_cost_search(m, startState, goal);
			
		}
	}

	public static void main(String[] args) throws Exception
	{
		Controller.playGame();
	}
}

class Block {
	  public float cost;
	  Block parent;
	  float x;
	  float y;
	  
	  Block(float x, float y, float cost, Block par) {
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
		if(a.x > b.x)
				return -1;
		else if(a.y > b.y)
			return -1;
		else if(a.y < b.y) 
				return 1;
		else if(a.x < b.x)
			return 1;
	
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
	PriorityQueue<Block> frontier;
	TreeSet<Block> beenThere;
	Stack<Block> path;
	
	  public Stack<Block> uniform_cost_search(Model m, Block startState, Block goal) {
		boolean found = false;
		comp = new BlockComparator();
		costComp = new CostComparator();
		frontier = new PriorityQueue<Block>(costComp);
		beenThere = new TreeSet<Block>(comp);
		path = new Stack<Block>();
	    frontier.add(startState);
	    beenThere.add(startState);
	    
	    while(frontier.size() > 0) {
	      Block s = (Block) frontier.remove(); // get lowest-cost state
	      //s.print();
	      if(s.x == goal.x && s.y == goal.y){
	    	  System.out.println("Found dest");
	    	  goal.parent = s;
	    	  found = true;
	    	  break;
	      }
	      //
	      //MoveUpRight(m,s); //x+10, y-10;
	      MoveRight(m,s); //x+10
	      //MoveDownRight(m,s); //x+10, y+10
	      MoveDown(m,s); //y+10
	      //MoveDownLeft(m,s);//x-10, y+10
	      MoveLeft(m,s); //x-10
	      //MoveUpLeft(m,s);//x-10, y-10
	      MoveUp(m,s); //y-10
	     
	    } 
	    
	    if(!found){
	    	System.out.println("Can't find route");
	    	return new Stack();
	    }
	    
	    Block current = goal;
	    while(current!=null){
	    	//current.print();
	    	path.add(current);
	    	current = current.parent;
	    	
	    }
	    
	    return path;
	    //throw new RuntimeException("There is no path to the goal");
	  }

	private void MoveUp(Model m, Block root) {
		
		float x = (float) (root.x);
		float y = (float) (root.y-10); //MAY BE TOO SLOW
		
		if(y>0){ //if we aren't off top of the map
			
			float cost = (1/(m.getTravelSpeed(x,y)*10)) + root.cost; //Cost is speed associated with the terrain square AND distance you will travel at that speed
			
			Block child = new Block(x,y,cost,root);
			Block oldChild;
		
			if(beenThere.contains(child)){ //If the new block is already in the set, then we need to check cost
				oldChild = beenThere.floor(child); //find the block with the same x,y
				//System.out.println("Down. Found : " + x + "," + y + " already in beenThere. oldChildCost: " + oldChild.x + "," + oldChild.y + " newChild: " + child.x + "," + child.y);
				if(cost < oldChild.cost) { //If the root cost + new cost is less than old cost, then update new cost and make 
			        oldChild.cost =  cost; //new parent
			        oldChild.parent = root;
			      }	
			}
			else {	//If its not in the set, add it to the set, dont care about cost
				//System.out.println("Down. Adding: x : " + child.x + " y : " + child.y );
				frontier.add(child);
				beenThere.add(child);
			}
	
		}
		
		
	}

	private void MoveLeft(Model m, Block root) {
		
		float x = (float) (root.x-10);
		float y = (float) (root.y); //MAY BE TOO SLOW
		
		if(x>0){ //if we aren't too far to the left
			
			float cost = (1/(m.getTravelSpeed(x,y)*10)) + root.cost; //Cost is speed associated with the terrain square AND distance you will travel at that speed
			
			Block child = new Block(x,y,cost,root);
			Block oldChild;
		
			if(beenThere.contains(child)){ //If the new block is already in the set, then we need to check cost
				oldChild = beenThere.floor(child); //find the block with the same x,y
				//System.out.println("Down. Found : " + x + "," + y + " already in beenThere. oldChildCost: " + oldChild.x + "," + oldChild.y + " newChild: " + child.x + "," + child.y);
				if(cost < oldChild.cost) { //If the root cost + new cost is less than old cost, then update new cost and make 
			        oldChild.cost =  cost; //new parent
			        oldChild.parent = root;
			      }	
			}
			else {	//If its not in the set, add it to the set, dont care about cost
				//System.out.println("Down. Adding: x : " + child.x + " y : " + child.y );
				frontier.add(child);
				beenThere.add(child);
			}
		}
	
	}

	private void MoveDown(Model m, Block root) {
		//System.out.println("Moving down.");
		float x = (float) (root.x);
		float y = (float) (root.y+10); //MAY BE TOO SLOW
		
		if(y<600){
			float cost = (1/(m.getTravelSpeed(x,y)*10)) + root.cost; //Cost is speed associated with the terrain square AND distance you will travel at that speed
			
			Block child = new Block(x,y,cost,root);
			Block oldChild;
		
			if(beenThere.contains(child)){ //If the new block is already in the set, then we need to check cost
				oldChild = beenThere.floor(child); //find the block with the same x,y
				//System.out.println("Down. Found : " + x + "," + y + " already in beenThere. oldChildCost: " + oldChild.x + "," + oldChild.y + " newChild: " + child.x + "," + child.y);
				if(cost < oldChild.cost) { //If the root cost + new cost is less than old cost, then update new cost and make 
			        oldChild.cost =  cost; //new parent
			        oldChild.parent = root;
			      }	
			}
			else {	//If its not in the set, add it to the set, dont care about cost
				//System.out.println("Down. Adding: x : " + child.x + " y : " + child.y );
				frontier.add(child);
				beenThere.add(child);
			}
		}

	}

	private void MoveRight(Model m, Block root) {
		
		//System.out.println("Moving down.");
		float x = (float) (root.x+10);
		float y = (float) (root.y); //MAY BE TOO SLOW
		
		if(x < 1100){
			float cost = (1/(m.getTravelSpeed(x,y)*10)) + root.cost; //Cost is speed associated with the terrain square AND distance you will travel at that speed
			
			Block child = new Block(x,y,cost,root);
			Block oldChild;
			
			//System.out.println("beenthere contains child? :" + beenThere.contains(child) + " x : " + child.x + " y : " + child.y );
			
			if(beenThere.contains(child)){ //If the new block is already in the set, then we need to check cost
				oldChild = beenThere.floor(child); //find the block with the same x,y
				//System.out.println("Right. Found : " + x + "," + y + " already in beenThere. oldChildCost: " + oldChild.x + "," + oldChild.y + " newChild: " + child.x + "," + child.y);
				if(cost < oldChild.cost) { //If the root cost + new cost is less than old cost, then update new cost and make 
			        oldChild.cost =  cost; //new parent
			        oldChild.parent = root;
			      }	
			}
			else {	//If its not in the set, add it to the set, dont care about cost
				//System.out.println("Right. Adding: x : " + child.x + " y : " + child.y );
				frontier.add(child);
				beenThere.add(child);
			}
		}
      }//End moveRight
		
	 public Block findNode(Block block) {  
	        Iterator<Block> iterator = beenThere.iterator();
	        while(iterator.hasNext()) {
	            Block node = iterator.next();
	            if(node.x == block.x && node.y == block.y)             
	                return node;
	        }

	        return null;                
	    }
	
	}