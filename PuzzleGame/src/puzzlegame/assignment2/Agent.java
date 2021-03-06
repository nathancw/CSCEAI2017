package puzzlegame.assignment2;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;
import javax.swing.SwingUtilities;
import java.util.PriorityQueue;
import java.util.Stack;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Color;

class Agent {
	Stack path = new Stack<Block>();
	int accum = 0;
	Block current;
	int destinations[][] = new int[80][80];
	float heuristic = 5;
	float lowest = 0;
	boolean heuristicFound = false;
	Block goal = new Block((float)100,(float)100,(float)0,null,(float)0.0);
	boolean searching = false;
	boolean aStar = false;
	
	void drawPlan(Graphics g, Model m) {
		g.setColor(Color.red);
		int i = m.getDestNum();
		State[] dest = m.getDestinations();
		ArrayList<State> visited = m.getVisited();
		
		for(int in = 0; in < visited.size(); in++){
			g.drawOval(visited.get(in).x,visited.get(in).y,9,9);
		}
			
		g.setColor(Color.black);
		for(int x = 0; x < i-1; x++){
			g.drawLine(dest[x].x,dest[x].y, dest[x+1].x,dest[x+1].y);
			
		}
		
		
	}

	void update(Model m)
	{
		Controller c = m.getController();	

		while(true)
		{
			MouseEvent e = c.nextMouseEvent();	
			
			if(searching && m.getX() == m.getDestinationX() && m.getY() == m.getDestinationY()){
				
					UFS ufs = new UFS(aStar,heuristic, lowest);
					Block startState = new Block(m.getX(),m.getY(),(float) 0.0,null,(float)0.0);
					path = ufs.uniform_cost_search(m, startState, goal);
					Block current = (Block) path.pop();
					current = (Block) path.pop(); //Pop twice to get second
					if(m.getX() == goal.x && m.getY() == goal.y)
						searching = false;
					
					m.setDestination((float)current.x, (float)current.y);
				
			}
			
			if(e !=null){
				if(SwingUtilities.isLeftMouseButton(e)){
					m.drawLine = true;
					aStar = false;
					m.emptyDestinations();
					m.visited.clear();
					int x = (e.getX()/10)*10;
					int y = (e.getY()/10)*10;
					goal = new Block(x,y,(float) 0.0,null,(float)0.0);
					searching = true;
				}
				else if(SwingUtilities.isRightMouseButton(e)){
					//System.out.println("Doing A* Search");
					m.drawLine = true;
					m.emptyDestinations();
					m.visited.clear();
					int x = (e.getX()/10)*10;
					int y = (e.getY()/10)*10;
					goal = new Block(x,y,(float) 0.0,null,(float)0.0);
					aStar = true;
					calculateLowest(m,x,y);
					searching = true;
				
				}
			}
			else
				break;
			
		}
	}

	private void calculateLowest(Model m, float xGoal,float yGoal) {
		
		float temp;
		
		if(!heuristicFound){
		for(int x = 0; x < 1200; x+=10)
			for(int y =0; y < 600; y+=10){
				temp = (m.getTravelSpeed(x,y));
				if(temp > lowest)
					lowest = temp;
			}
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
	  float heuristic;
	  
	  Block(float x, float y, float cost, Block par, float h) {
		  this.cost = cost;
		  this.parent = par;
		  this.x = x;
		  this.y = y;
		  this.heuristic = h;
	  }
	  
	  void print(){
		  System.out.println("(" + x + "," + y + ") cost : " + cost);
	  }
	}

class BlockComparator implements Comparator<Block>
{
	public int compare(Block a, Block b)
	{
		
		  Float x1 = a.x;
	        Float x2 = b.x;
	        int floatCompare1 = x1.compareTo(x2);

	        if (floatCompare1 != 0) {
	            return floatCompare1;
	        } else {
	            Float y1 = a.y;
	            Float y2 = b.y;
	            return y1.compareTo(y2);
	        }
	    }
		
		
	}
class CostComparator implements Comparator<Block>
{
	public int compare(Block a, Block b)
	{
			if((a.cost + a.heuristic) > (b.cost + b.heuristic))
				return 1;
			else if((a.cost + a.heuristic) < (b.cost + b.heuristic))
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
	boolean aStar;
	float heuristic;
	Block goal;
	float lowest;
	public UFS(boolean a, float h, float l){
		this.aStar = a;
		this.heuristic = h;
		this.lowest = l;
	}
	
	private float calculateHeur(float xCurr, float yCurr, float xGoal,float yGoal) {
		float pow1 = (float) Math.pow((xCurr - xGoal),2);
		float pow2 = (float) Math.pow((yCurr - yGoal),2);
		float total = (float) ((Math.sqrt(pow1 + pow2))/lowest);
		//System.out.println("Lowest: " + lowest +  "total: " + total);
		return total;
	}
	
	  public Stack<Block> uniform_cost_search(Model m, Block startState, Block goal) {
		boolean found = false;
		comp = new BlockComparator();
		costComp = new CostComparator();
		frontier = new PriorityQueue<Block>(costComp);
		beenThere = new TreeSet<Block>(comp);
		path = new Stack<Block>();
	    frontier.add(startState);
	    beenThere.add(startState);
	    this.goal = goal;
	    
	    while(frontier.size() > 0) {
	      Block s = (Block) frontier.remove(); // get lowest-cost state
	      
	      //s.print();
	      if(s.x == goal.x && s.y == goal.y){
	    	  goal.parent = s;
	    	  found = true;
	    	  break;
	      }
	      //
	      MoveState(m,s,10,-10); //x+10, y-10;
	      MoveState(m,s,10,0); //x+10
	      MoveState(m,s,10,10); //x+10, y+10
	      MoveState(m,s,0,10); //y+10
	      MoveState(m,s,-10,10);//x-10, y+10
	      MoveState(m,s,-10,0); //x-10
	      MoveState(m,s,-10,-10);//x-10, y-10
	      MoveState(m,s,0,-10); //y-10
	     
	    } 
	    
	    if(!found){
	    	System.out.println("Can't find route");
	    	return new Stack();
	    }
	    
	    m.visited.clear();
	    
	    for (Block e : frontier) {
	    	m.setVisited((int)e.x,(int)e.y);
	    }
	    
	    
	    Block current = goal;
	    while(current!=null){
	    	//System.out.println("visited : " + current.x + "," + current.y + " : " + m.visitedPoint((int)current.x,(int)current.y));
	    	if(m.drawLine)
	    		m.updateLine((int)current.x,(int)current.y);
	    	
	    	
	    	path.add(current);
	    	current = current.parent;
	    	
	    }
	    
	    m.drawLine =false;
	    return path;
	    //throw new RuntimeException("There is no path to the goal");
	  }

	public PriorityQueue<Block> getFrontier(){
		return frontier;
	}
	  
	private void MoveState(Model m, Block root, float xMove, float yMove) {
		float x = (float) (root.x+xMove);
		float y = (float) (root.y+yMove);
		
		if(x < 1199 && y < 599 && y > 0 && x > 0){
			
			float cost;
			if((Math.abs(xMove) + Math.abs(yMove))==20)
				cost = (float)(10/(m.getTravelSpeed(x,y))*Math.sqrt(2));//Cost is speed associated with the terrain square AND distance you will travel at that speed
			else
				cost =  (float)(10/(m.getTravelSpeed(x,y)));
			
			float heur = 0;
			if(aStar){
				heur = calculateHeur(x,y,goal.x,goal.y);
				cost = cost + root.cost;
			}
			else{
				cost = cost + root.cost;
			}
			
			Block child = new Block(x,y,cost,root, heur);
			Block oldChild;
		
			if(beenThere.contains(child)){ //If the new block is already in the set, then we need to check cost
				oldChild = beenThere.floor(child); //find the block with the same x,y
				if(cost < oldChild.cost) { //If the root cost + new cost is less than old cost, then update new cost and make 
			        oldChild.cost =  cost; //new parent
			        oldChild.parent = root;
			      }	
			}
			else {	//If its not in the set, add it to the set, dont care about cost
				frontier.add(child);
				beenThere.add(child);
			}	
		}
		
	}

}
		
