// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)
package puzzlegame.assignment3;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Stack;
import java.util.TreeSet;


class NeuralAgent implements IAgent
{
	int index; // a temporary value used to pass values around
	NeuralNet nn;
	double[] in;
	boolean heuristicFound = false;
	UCS ucs;
	float lowestVal = -100;
	int accum;

	NeuralAgent(double[] weights) {
		in = new double[20];
		nn = new NeuralNet();
		nn.layers.add(new LayerTanh(in.length, 8));
		nn.layers.add(new LayerTanh(8, 10));
		nn.layers.add(new LayerTanh(10, 3));
		setWeights(weights);
		ucs = new UCS(true);
		accum = 0;
	}

	public void reset() {
	}

	/// Returns the number of weights necessary to fully-parameterize this agent
	int countWeights() {
		int n = 0;
		for(int i = 0; i < nn.layers.size(); i++)
			n += nn.layers.get(i).countWeights();
		return n;
	}


	/// Sets the parameters of this agent with the specified weights
	void setWeights(double[] weights) {
		if(weights.length != countWeights())
			throw new IllegalArgumentException("Wrong number of weights. Got " + Integer.toString(weights.length) + ", expected " + Integer.toString(countWeights()));
		int start = 0;
		for(int i = 0; i < nn.layers.size(); i++)
			start += nn.layers.get(i).setWeights(weights, start);
	}


	public static float sq_dist(float x1, float y1, float x2, float y2) {
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
	}

	float nearestBombTarget(Model m, float x, float y) {
		index = -1;
		float dd = Float.MAX_VALUE;
		for(int i = 0; i < m.getBombCount(); i++) {
			float d = sq_dist(x, y, m.getBombTargetX(i), m.getBombTargetY(i));
			if(d < dd) {
				dd = d;
				index = i;
			}
		}
		return dd;
	}

	float nearestOpponent(Model m, float x, float y) {
		index = -1;
		float dd = Float.MAX_VALUE;
		for(int i = 0; i < m.getSpriteCountOpponent(); i++) {
			if(m.getEnergyOpponent(i) < 0)
				continue; // don't care about dead opponents
			float d = sq_dist(x, y, m.getXOpponent(i), m.getYOpponent(i));
			if(d < dd) {
				dd = d;
				index = i;
			}
		}
		return dd;
	}

	void avoidBombs(Model m, int i) {
		if(nearestBombTarget(m, m.getX(i), m.getY(i)) <= 2.0f * Model.BLAST_RADIUS * Model.BLAST_RADIUS) {
			float dx = m.getX(i) - m.getBombTargetX(index);
			float dy = m.getY(i) - m.getBombTargetY(index);
			if(dx == 0 && dy == 0)
				dx = 1.0f;
			//float newX = (m.getX(i) + dx * 10.0f);
			//float newY = (m.getY(i) + dy * 10.0f);
			/*
			if(newX < 0)
				newX = newX * - 1;
			if(newY < 0)
				newY = newY * -1;
			*/
			float newX = Model.XFLAG;
			float newY = Model.YFLAG;
			
			
			//if(m.getX(i) < 200)
			//	newX = (m.getX(i) + dx * 10.0f);
			//if((m.getEnergyOpponent(0) < 0 || m.getEnergyOpponent(1) < 0 || m.getEnergyOpponent(2) < 0)){
			//	newX = Model.XFLAG_OPPONENT;
			//	newY = Model.YFLAG_OPPONENT;
			//}
			//float newX = Model.XFLAG;
			//float newY = Model.YFLAG;
			//if(m.getX(i) > 300)
			findBestDestination(m,i,newX,newY);
			//else
			//	m.setDestination(i, m.getX(i) + dx * 10.0f, m.getY(i) + dy * 10.0f);
		}
	}

	void beDefender(Model m, int i) {
		
		float myX = m.getX(i);
		float myY = m.getY(i);
		// Find the opponent nearest to my flag
		nearestOpponent(m, Model.XFLAG, Model.YFLAG);
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);
			float dx = myX - enemyX;
			float dy = myY - enemyY;
			float t = 1.0f / Math.max(Model.EPSILON, (float)Math.sqrt(dx * dx + dy * dy));
			dx *= t;
			dy *= t;
			
			// Stay between the enemy and my flag
			findBestDestination(m,i,0.6f * (Model.XFLAG + enemyX), 0.5f * (Model.YFLAG + enemyY));
			//m.setDestination(i, 0.5f * (Model.XFLAG + enemyX), 0.5f * (Model.YFLAG + enemyY));

			// Throw bombs
			if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS){
				m.throwBomb(i, enemyX, enemyY);
			}
			else if (Math.sqrt(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)))  < Model.MAX_THROW_RADIUS + (Model.BLAST_RADIUS * 0.25 ) ) {
				float factor = (float) (Model.MAX_THROW_RADIUS / Math.sqrt(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i))) );
				float throwX = dx * factor + enemyX;
				float throwY = dy * factor + enemyY;
				//System.out.println("---------------------------------------------------");
				m.throwBomb(i,throwX,throwY);
			}
		}
		//else {
			// Guard the flag
		//	findBestDestination(m,i,Model.XFLAG + Model.MAX_THROW_RADIUS, Model.YFLAG);
		//	m.setDestination(i, Model.XFLAG + Model.MAX_THROW_RADIUS, Model.YFLAG);
		//}

		// Try not to die
		avoidBombs(m, i);
	}

	void beFlagAttacker(Model m, int i) {
		// Head for the opponent's flag
		findBestDestination(m,i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS, Model.YFLAG_OPPONENT);
		// Shoot at the flag if I can hit it
		if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
			m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
		}

		// Try not to die
		avoidBombs(m, i);
	}

	void beAggressor(Model m, int i) {
	
		float myX = m.getX(i);
		float myY = m.getY(i);
		
		shootFlag(m,i);
		// Find the opponent nearest to me
		nearestOpponent(m, myX, myY);
		
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);

			if(m.getEnergySelf(i) >= m.getEnergyOpponent(index)) {

				// Get close enough to throw a bomb at the enemy
				float dx = myX - enemyX;
				float dy = myY - enemyY;
				float t = 1.0f / Math.max(Model.EPSILON, (float)Math.sqrt(dx * dx + dy * dy));
				dx *= t;
				dy *= t;
				findBestDestination(m,i, enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON));
				//m.setDestination(i, enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON));

				// Throw bombs
				if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS){
					m.throwBomb(i, enemyX, enemyY);
				}
				else if (Math.sqrt(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)))  < Model.MAX_THROW_RADIUS + (Model.BLAST_RADIUS * 0.25 ) ) {
					float factor = (float) (Model.MAX_THROW_RADIUS / Math.sqrt(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i))) );
					float throwX = dx * factor + enemyX;
					float throwY = dy * factor + enemyY;
					//System.out.println("---------------------------------------------------");
					m.throwBomb(i,throwX,throwY);
				}
			}
			
		}

		
		// Try not to die
		avoidBombs(m, i);
	}
	
	
	
	void shootFlag(Model m, int i){
	//	findBestDestination(m,i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS, Model.YFLAG_OPPONENT);
		
		// Shoot at the flag if I can hit it
		if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
			m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
		}
	}
	
	private void findBestDestination(Model m, int i, float f, float g) {
	
		if(lowestVal == -100)
			lowestVal = calculateLowest(m);
		
		int destX = (int) (Math.ceil(f) / 10) * 10;
		int destY = (int) (Math.ceil(g) / 10) * 10;
		
		int sX = (int) ((m.getX(i)) / 10) * 10;
		int sY = (int) ((m.getY(i)) / 10) * 10;
		
	//	System.out.println(i + " BeFlagAttacker. Sx: " + sX + " Sy: " + sY);
		Block next = ucs.uniform_cost_search(m, new Block(sX,sY,(float) 0.0,null,(float)0.0), 
				new Block(destX,destY,(float) 0.0,null,(float)0.0), lowestVal);
		
		//System.out.println("Flag: " + flagX + "," + flagY);
		//System.out.println("Next: " + next.x + ", " + next.y);
		m.setDestination(i, next.x, next.y);
		
		
	}

	public void update(Model m) {
	
		// Compute some features
		in[0] = m.getX(0) / 600.0 - 0.5;
		in[1] = m.getY(0) / 600.0 - 0.5;
		in[2] = m.getX(1) / 600.0 - 0.5;
		in[3] = m.getY(1) / 600.0 - 0.5;
		in[4] = m.getX(2) / 600.0 - 0.5;
		in[5] = m.getY(2) / 600.0 - 0.5;
		in[6] = nearestOpponent(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
		in[7] = nearestOpponent(m, m.getX(1), m.getY(1)) / 600.0 - 0.5;
		in[8] = nearestOpponent(m, m.getX(2), m.getY(2)) / 600.0 - 0.5;
		in[9] = nearestBombTarget(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
		in[10] = nearestBombTarget(m, m.getX(1), m.getY(1)) / 600.0 - 0.5;
		in[11] = nearestBombTarget(m, m.getX(2), m.getY(2)) / 600.0 - 0.5;
		in[12] = m.getEnergySelf(0);
		in[13] = m.getEnergySelf(1);
		in[14] = m.getEnergySelf(2);
		in[15] = m.getEnergyOpponent(0);
		in[16] = m.getEnergyOpponent(1);
		in[17] = m.getEnergyOpponent(2);
		in[18] = m.getFlagEnergySelf();
		in[19] = m.getFlagEnergyOpponent();

		// Determine what each agent should do
		double[] out = nn.forwardProp(in);

		// Do it
		if(accum>150){
			for(int i = 0; i < 3; i++)
			{
			
				if(out[i] < -0.333)
					beDefender(m, i);
				else if(out[i] > 0.333)
					beAggressor(m, i);
				else
					beAggressor(m, i);
			}
		}
		accum++;
	
	}
	
	float calculateLowest(Model m) {
		
		float temp;
		float lowestVal = 0;
		if(!heuristicFound){
		for(int x = 0; x < 1200; x+=10)
			for(int y =0; y < 600; y+=10){
				temp = (m.getTravelSpeed(x,y));
				if(temp > lowestVal)
					lowestVal = temp;
			}
		}
		return lowestVal;
	}
}




class Block {
	  public float cost;
	  Block parent;
	  float x;
	  float y;
	  float heuristic = 0;
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


class UCS {
	BlockComparator comp;
	CostComparator costComp;
	PriorityQueue<Block> frontier;
	TreeSet<Block> beenThere;
	Stack<Block> path;
	boolean aStar;
	Block goal;
	float lowest;
	
	public UCS(boolean a){
		this.aStar = a;
	}
	
	private float calculateHeur(float xCurr, float yCurr, float xGoal,float yGoal) {
		float pow1 = (float) Math.pow((xCurr - xGoal),2);
		float pow2 = (float) Math.pow((yCurr - yGoal),2);
		float total = (float) ((Math.sqrt(pow1 + pow2))/lowest);
		//System.out.println("Lowest: " + lowest +  "total: " + total);
		return total;
	}
	
	  public Block uniform_cost_search(Model m, Block startState, Block goal, float lowest) {
		this.lowest = lowest;
		
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
	    	System.out.println("Start: " + startState.x + "," + startState.y + " goal: " + goal.x + "," + goal.y);
	    	//return new Stack();
	    }
	    
	    
	    Block current = goal;
	    while(current!=null){ 	
	    	path.add(current);
	    	current = current.parent;
	    	
	    }
	    
	    //Pop one before
	   if(!(path.size() > 2))
	    return startState;
	   else
		   path.pop();

	    return path.pop();
	    //throw new RuntimeException("There is no path to the goal");
	  }

	public PriorityQueue<Block> getFrontier(){
		return frontier;
	}
	  
	private void MoveState(Model m, Block root, float xMove, float yMove) {
		float x = (float) (root.x+xMove);
		float y = (float) (root.y+yMove);
	//	System.out.println("Checking: " + x + " ," + y);
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
