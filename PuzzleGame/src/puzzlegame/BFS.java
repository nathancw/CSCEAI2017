package puzzlegame;
import javax.swing.JFrame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Timer;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Image;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.Graphics;
import java.io.File;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;
import java.util.TreeSet;
import java.awt.Color;
import java.awt.Frame;

class GameState
{
	GameState prev;
	byte[] state;
	boolean visited;
	
	public GameState(byte[] state2, GameState prev) {
		this.prev = prev;
		this.state = state2;
	}

	public void setVisited(boolean val){
		this.visited = val;
	}
	
	public boolean hasBeenVisited(){
		return visited;
	}
	
	public byte[] getState(){
		return state;
	}
	
	public void print(){
		for(int i = 0; i < 11; i++)
			System.out.print("(" + this.state[2 * i] + "," +
					this.state[2 * i + 1] + ") ");
				System.out.println();

	}
/*	GameState(GameState _prev)
	{
		prev = _prev;
		state = new byte[22];
	}
*/
}

class StateComparator implements Comparator<GameState>
{
	public int compare(GameState a, GameState b)
	{
		for(int i = 0; i < 22; i++)
		{
			if(a.state[i] < b.state[i])
				return -1;
			else if(a.state[i] > b.state[i])
				return 1;
		}
		return 0;
	}
}  


class Search implements MouseListener {
	BFS viz;
	Random rand;
	byte[] state;
	Graphics graphics;
	int size;
	boolean[][] board;
	Queue<GameState> queue;
	StateComparator comp;
	TreeSet<GameState> set;
	Stack solution;

	class Node {
		   char data;
		   public Node(char c) {
		      this.data=c;
		   }
		}
	
	Search(BFS v) throws IOException
	{
		viz = v;
		rand = new Random();
		state = new byte[22];
		size = 48;
		board = new boolean[10][10];
		queue = new LinkedList<GameState>();
		comp = new StateComparator();
		set = new TreeSet<GameState>(comp);
		solution = new Stack();
	}
	
	public void bfs(){
		
		// BFS uses Queue data structure
		//graphics = null;
		GameState root = new GameState(state, null);
		queue.add(root);
		root.setVisited(true);
		
		//set.add(root);
		
		//if(set.contains(root))
		//	System.out.println("THE SET CONTAINS THE ROOT");
		
		
		byte[] objective = state.clone();
		
		int solutionID = 0;
		byte x = -4;
		byte y = 2;
		
		
		objective[solutionID*2]=x;
		objective[solutionID*2+1]=y;
		GameState goal = null;
		
		while(!queue.isEmpty()) {
			GameState node = queue.remove();
			GameState child = null;
			
			if(node.getState()[solutionID*2]==objective[solutionID*2] && node.getState()[solutionID*2+1]==objective[solutionID*2+1]){
				System.out.println("found objective");
				goal = node;
				break;
			}
			else{
				
				//Populat the queue with each adjacent nodes. 
				for(int index = 0; index < 11; index++){
					
					moveLeft(index, node);
					moveRight(index, node);
					moveUp(index, node);
					moveDown(index,node);
				}
			}
		
		}  
		
		//Go backwards and print the trail
		GameState current = goal;
		//current.print();
		int moveCount = -1;
		while(current!=null){
			moveCount++;
			solution.add(current.getState());
			current.print();
			current = current.prev;
		}
		System.out.println("Move Count : " + moveCount);
		
	}
	
	public Stack getStack(){
		return solution;
	}
	
	public void mousePressed(MouseEvent e)
	{
	
		

	}

	public boolean moveDown(int id, GameState root) {
	
		byte[] state = root.getState().clone();
		state[2*id+1] = (byte) (state[2*id+1] + 1);
		GameState down = new GameState(state, root);
		
		if(!set.contains(down)){
			boolean validMove = drawShapes(id, state);
			
			if(validMove){
				queue.add(down); //add the game state
				set.add(down);
				return true;
			}
		}
		return false;
	}

	public boolean moveUp(int id, GameState root) {

		byte[] state = root.getState().clone();
		state[2*id+1] = (byte) (state[2*id+1] - 1);
		GameState up = new GameState(state, root);
		
		if(!set.contains(up)){
			boolean validMove = drawShapes(id, state);
				if(validMove){
						queue.add(up); //add the game state
						set.add(up);
						return true;
				}
		}
		return false;
	}

	public boolean moveRight(int id, GameState root) {
		
		byte[] state = root.getState().clone();
		state[2*id] = (byte) (state[2*id] + 1);
		GameState right = new GameState(state, root);
		
		if(!set.contains(right)){
			boolean validMove = drawShapes(id, state);
			if(validMove){
				queue.add(right); //add the game state
				set.add(right);
				return true;
			}
			
		}
		return false;

	}

	public boolean moveLeft(int id, GameState root) {
		
		byte[] state = root.getState().clone();
		state[2*id] = (byte) (state[2*id] - 1);
		GameState left = new GameState(state, root);
		
		if(!set.contains(left)){
			boolean validMove = drawShapes(id, state);
			if(validMove){
					queue.add(left); //add the game state
					set.add(left);
				}
				return true;
			}
		return false;
	
	}

	public boolean drawShapes(int id, byte[] state) {
	
		for(int x = 0; x < 10; x++)
			for(int c = 0; c < 10; c++)
				board[x][c] = false;
		
		for(int i = 0; i < 10; i++) { b(i, 0); b(i, 9); }
		for(int i = 1; i < 9; i++) { b(0, i); b(9, i); }
		b(1, 1); b(1, 2); b(2, 1);
		b(7, 1); b(8, 1); b(8, 2);
		b(1, 7); b(1, 8); b(2, 8);
		b(8, 7); b(7, 8); b(8, 8);
		b(3, 4); b(4, 4); b(4, 3);
		
		
		// Draw the pieces
		if(!shape(0, 1, 3, 2, 3, 1, 4, 2, 4, state))
				return false;
		else if(!shape(1, 1, 5, 1, 6, 2, 6, state))
			return false;
		else if(!shape(2, 2, 5, 3, 5, 3, 6, state))
				return false;
		else if(!shape(3, 3, 7, 3, 8, 4, 8, state))
				return false;
		else if(!shape(4, 4, 7, 5, 7, 5, 8, state))
				return false;
		else if(!shape(5, 6, 7, 7, 7, 6, 8, state))
				return false;
		else if(!shape(6, 5, 4, 5, 5, 5, 6, 4, 5, state))
				return false;
		else if(!shape(7, 6, 4, 6, 5, 6, 6, 7, 5, state))
				return false;
		else if(!shape(8, 8, 5, 8, 6, 7, 6,state)) 
				return false; 
		else if(!shape(9, 6, 2, 6, 3, 5, 3,state))
				return false; 
		else if(!shape(10, 5, 1, 6, 1, 5, 2, state)){
			return false;
		}
		else
			return true;
		
		
	}

	public void mouseReleased(MouseEvent e) {    }
	public void mouseEntered(MouseEvent e) {    }
	public void mouseExited(MouseEvent e) {    }
	public void mouseClicked(MouseEvent e) {    }

	//B checks valid states
	public boolean b(int x, int y)
	{
		//if(x < 10 && y < 10)
			if(board[x][y] == true){
			//	System.out.println("Found error at : " + x + "," + y);
				return false;
			}
		
		board[x][y] = true;
		//graphics.fillRect(size * x, size * y, size, size);
			return true;
	}

	// Draw a 3-block piece
	public boolean shape(int id,
		int x1, int y1, int x2, int y2, int x3, int y3, byte[] state)
	{
		//graphics.setColor(new Color(red, green, blue));
		boolean b1 = b(state[2 * id] + x1, state[2 * id + 1] + y1);
		boolean b2 = b(state[2 * id] + x2, state[2 * id + 1] + y2);
		boolean b3 = b(state[2 * id] + x3, state[2 * id + 1] + y3);
		//System.out.println("b1 :  " + b1 + " b2: " + b2 + " b3 : " + b3 + " total: " + (b1 && b2 && b3));
		return (b1 && b2 && b3);
	}

	// Draw a 4-block piece
	public boolean shape(int id,
		int x1, int y1, int x2, int y2,
		int x3, int y3, int x4, int y4, byte[] state)
	{
		boolean s1 = shape(id, x1, y1, x2, y2, x3, y3, state);
		boolean b1 = b(state[2 * id] + x4, state[2 * id + 1] + y4);
		
		return (s1 && b1);
	}
}

public class BFS extends JFrame
{
	Search search;
	public BFS() throws Exception
	{
		search = new Search(this);
		search.bfs();
		/*view.addMouseListener(view);
		this.setTitle("Puzzle");
		this.setSize(482, 505);
		this.getContentPane().add(view);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true); */
	}
	
	public Stack getStack(){
		return search.getStack();
	}
	public static void main(String[] args) throws Exception
	{
		new BFS();
	}
}