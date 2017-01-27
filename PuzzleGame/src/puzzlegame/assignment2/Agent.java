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
	
	void drawPlan(Graphics g, Model m) {
		g.setColor(Color.red);
		g.drawLine((int)m.getX(), (int)m.getY(), (int)m.getX()+300, (int)m.getY()+300);
		g.fillOval((int)m.getX(), (int)m.getY(), (int)m.getX()+5, (int)m.getY()+5);
		Block current;
		while(!path.isEmpty()){
			current = (Block) path.pop();
			//current = (Block) path.pop(); 
			System.out.println("POPPING OFF");
			current.print();
			g.fillOval((int)current.x,(int)current.y,200,200);
			g.drawLine((int)m.getX(), (int)m.getY(), (int)(m.getX()+current.x), (int)(m.getY()+current.y));
		} 
		
		g.setColor(Color.red);
		
	}

	void update(Model m)
	{
		
		Controller c = m.getController();
	
		
		//System.out.println("UDPATING");
		while(true)
		{
			MouseEvent e = c.nextMouseEvent();
			
			if(e == null)
				break;
			
			Block startState = new Block(m.getX(),m.getY(),(float) 0.0,null);
			UFS ufs = new UFS();
			Block goal = new Block(m.getX()+300,m.getY()+300,(float) 0.0,null);
			path = ufs.uniform_cost_search(m, startState, goal);
			
			/*System.out.println("-----------------");
			Block current;
			while(!path.isEmpty()){
				current = (Block) path.pop();
				//current = (Block) path.pop(); 
				current.print();
				//m.setDestination(current.x, current.y);
			} */
			
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
			if(a.x != b.x || a.y != b.y)
				return -1;
		return 0;
	}
}  
class CostComparator implements Comparator<Block>
{
	public int compare(Block a, Block b)
	{
			if(a.cost < b.cost)
				return 1;
			else if(a.cost > b.cost)
					return -1;
		return 0;
	}
}  

class UFS {
	BlockComparator comp;
	CostComparator costComp;
	PriorityQueue frontier;
	HashSet<Block> beenThere;
	Stack path;
	
	  public Stack uniform_cost_search(Model m, Block startState, Block goal) {
		 
		comp = new BlockComparator();
		costComp = new CostComparator();
		frontier = new PriorityQueue(costComp);
		beenThere = new HashSet<Block>();
		path = new Stack<Block>();
	    
	    beenThere.add(startState);
	    frontier.add(startState);
	    while(frontier.size() > 0) {
	      Block s = (Block) frontier.remove(); // get lowest-cost state
	      //System.out.println("---------");
	      //s.print();
	      //System.out.println("---------");
	      if(s.x == goal.x || s.y == goal.y){
	    	  goal.parent = s;
	    	  break;
	      }
	      //
	      //MoveUpRight(m,s); //x+10, y-10;
	      MoveRight(m,s); //x+10
	      //MoveDownRight(m,s); //x+10, y+10
	      MoveDown(m,s); //y+10
	      //MoveDownLeft(m,s);//x-10, y+10
	      //MoveLeft(m,s); //x-10
	      //MoveUpLeft(m,s);//x-10, y-10
	      //MoveUp(m,s); //y-10
	     
	    } 
	    Block current = goal;
	    while(current!=null){
	    	//current.print();
	    	path.add(current);
	    	current = current.parent;
	    	
	    }
	    
	    /*current = (Block) path.pop();
	    while(!path.isEmpty()){
	    	current.print();
	    	current = (Block) path.pop();
	    } */
	    
	    return path;
	    //throw new RuntimeException("There is no path to the goal");
	  }

	private void MoveUp(Model m, Block root) {
		
		float x = (float) (root.x);
		float y = (float) (root.y-10); //MAY BE TOO SLOW
		
		float cost = m.getTravelSpeed(x,y) + root.cost; //Cost is speed associated with the terrain square AND distance you will travel at that speed
		
		Block child = new Block(x,y,cost,root);
		Block oldChild;
		
		
		if(!beenThere.contains(child)) {	//If its not in the set, add it to the set, dont care about cost
			frontier.add(child);
			beenThere.add(child);
		}
		else if(beenThere.contains(child)){ //If the new block is already in the set, then we need to check cost
			oldChild = findNode(child); //find the block with the same x,y
			if(cost < oldChild.cost) { //If the root cost + new cost is less than old cost, then update new cost and make 
		        oldChild.cost =  cost; //new parent
		        oldChild.parent = root;
		      }	
		}

		
	}

	private void MoveLeft(Model m, Block root) {
		
		float x = (float) (root.x-10);
		float y = (float) (root.y); //MAY BE TOO SLOW
		
		float cost = m.getTravelSpeed(x,y) + root.cost; //Cost is speed associated with the terrain square AND distance you will travel at that speed
		
		Block child = new Block(x,y,cost,root);
		Block oldChild;
		
		
		if(!beenThere.contains(child)) {	//If its not in the set, add it to the set, dont care about cost
			frontier.add(child);
			beenThere.add(child);
		}
		else if(beenThere.contains(child)){ //If the new block is already in the set, then we need to check cost
			oldChild = findNode(child); //find the block with the same x,y
			if(cost < oldChild.cost) { //If the root cost + new cost is less than old cost, then update new cost and make 
		        oldChild.cost =  cost; //new parent
		        oldChild.parent = root;
		      }	
		}
	
	}

	private void MoveDown(Model m, Block root) {
		//System.out.println("Moving down.");
		float x = (float) (root.x);
		float y = (float) (root.y+10); //MAY BE TOO SLOW
		
		float cost = m.getTravelSpeed(x,y) + root.cost; //Cost is speed associated with the terrain square AND distance you will travel at that speed
		
		Block child = new Block(x,y,cost,root);
		Block oldChild;
		
		
		if(!beenThere.contains(child)) {	//If its not in the set, add it to the set, dont care about cost
			frontier.add(child);
			beenThere.add(child);
		}
		else if(beenThere.contains(child)){ //If the new block is already in the set, then we need to check cost
			oldChild = findNode(child); //find the block with the same x,y
			if(cost < oldChild.cost) { //If the root cost + new cost is less than old cost, then update new cost and make 
		        oldChild.cost =  cost; //new parent
		        oldChild.parent = root;
		      }	
		}

	}

	private void MoveRight(Model m, Block root) {
		
		//System.out.println("Moving down.");
		float x = (float) (root.x+10);
		float y = (float) (root.y); //MAY BE TOO SLOW
		
		float cost = m.getTravelSpeed(x,y) + root.cost; //Cost is speed associated with the terrain square AND distance you will travel at that speed
		
		Block child = new Block(x,y,cost,root);
		Block oldChild;
		
		
		if(!beenThere.contains(child)) {	//If its not in the set, add it to the set, dont care about cost
			frontier.add(child);
			beenThere.add(child);
		}
		else if(beenThere.contains(child)){ //If the new block is already in the set, then we need to check cost
			oldChild = findNode(child); //find the block with the same x,y
			if(cost < oldChild.cost) { //If the root cost + new cost is less than old cost, then update new cost and make 
		        oldChild.cost =  cost; //new parent
		        oldChild.parent = root;
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