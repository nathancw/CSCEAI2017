package puzzlegame.assignment4;

import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

class Node {
    List<Node> children = new ArrayList<Node>();
    Node parent = null;
    char[] data = null;
    int val;
    char player;

    public Node(char[] data) {
        this.data = data;
    }

    public Node(char[] data, Node parent, int val, char p) {
        this.data = data;
        this.parent = parent;
        this.val = val;
        this.player = p;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setParent(Node parent) {
        parent.addChild(this);
        this.parent = parent;
    }

    public void addChild(char[] data) {
        Node child = new Node(data);
        child.setParent(this);
        this.children.add(child);
    }

    public void addChild(Node child) {
     //   child.setParent(this);
        this.children.add(child);
    }

 

    public boolean isRoot() {
        return (this.parent == null);
    }

    public boolean isLeaf() {
        if(this.children.size() == 0) 
            return true;
        else 
            return false;
    }

    public void removeParent() {
        this.parent = null;
    }
}



public class TicTacToe {
	Node root;
	Queue queue;
	int depth;
	char[] board = {'1','2','3','4','5','6','7','8','9'};
	public TicTacToe(){
		
		depth = 0;
		root = new Node(board,null,20,'O');
		queue = new LinkedList<Node>();
		queue.add(root);
		calculateBoardStates(root);
		//drawBoard(board);
		
		playTicTacToe();
		
		
	
	}
	
	public void playTicTacToe(){
		
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		Node curr = root;
		int d = depth;
		while(true){
			drawBoard(board);
			System.out.print("Your Move:" );
			int n = reader.nextInt(); //X move
			board[n-1] = 'X';
			
			/*check winners or losers*/
			if(gameCompleted(board)){
				drawBoard(board);
				System.out.println("Draw match.");
				break;
			}
			else if(playerWon(board,'X')){
				drawBoard(board);
				System.out.println("You won! Wait... how?");
				break;
			}
			
			
			//Move the root node.
			int size = curr.children.size();
			for(int x = 0 ; x < size; x++){
				char [] tempBoard = curr.children.get(x).data; //get temp board
				//System.out.println("-----");
				//drawBoard(tempBoard);
				//System.out.println("-----");
				boolean found = true;
				for(int i = 0; i < 9; i++)
					if(tempBoard[i] != board[i])
						found = false;
				
				if(found){
					//System.out.println("found child at: " + x);
					curr = curr.children.get(x);
					x = 9;
					//drawBoard(tempBoard);
				}	
			}
			//Done moving root node	
			//Move depth down since we played
			d = d - 1;
			
	
			
			//IF game is not over and nobody won then
			//Find computer move.
			miniMax(curr,d,'X');
		
			//Move to O move node with greatest value
			size = curr.children.size();
			int max = -9999;
			int maxIndex = 0;
			for(int x = 0 ; x < size; x++){
				if(curr.children.get(x).val > max){
					maxIndex = x;	
					max = curr.children.get(x).val;
					//System.out.println("Max idnex " + maxIndex + "max : " + max);
				}
			}
			
			//We done, print the new board
			curr = curr.children.get(maxIndex);
			board = curr.data;
			
			/*Check winners*/
			
			if(playerWon(board,'O')){
				drawBoard(board);
				System.out.println("You lost, A.I prevails once more.");
				
				break;
			}
			
		}
		
		
		
	}
	
	public void drawBoard(char[] board){
		
		System.out.println("\n " + board[0] + " | " + board[1] + " | " + board[2] + " ");
		System.out.println("---+---+---");
		System.out.println(" " + board[3] + " | " + board[4] + " | " + board[5] + " ");
		System.out.println("---+---+---");
		System.out.println(" " + board[6] + " | " + board[7] + " | " + board[8] + " " + "\n");
		
	}
	
	public void calculateBoardStates(Node root){
		char player = 'X';
		while(!queue.isEmpty()){
			Node current = (Node) queue.remove();
			if(current.player =='X')
				player = 'O';
			else
				player = 'X';
			
			addState(1,player,current);
			addState(2,player, current);
			addState(3,player, current);
			addState(4,player, current);
			addState(5,player, current);
			addState(6,player, current);
			addState(7,player, current);
			addState(8,player, current);
			addState(9,player, current);
		}
		
		//for(int x = 0; x < 9; x++){
		Node curr = root;
		while(curr.children.size()!=0){	
			//drawBoard(curr.children.get(0).data);
			curr = curr.children.get(0);
			depth++;
			//System.out.println("val:  " + curr.val + "\n");
		}
		
	}
	
	public void addState(int x, char player, Node root){
		
		char[] boardCopy = root.data.clone();
		char c = root.data[x-1];
		int val = 20;
		//System.out.println("Adding: " + x  + " player: " + player);
	
		if(root.val == 20)
			if(c != 'X' && c!='O'){
					boardCopy[x-1] = player;
					
					
					if(playerWon(boardCopy,'X'))
						val = -1;
					else if(playerWon(boardCopy,'O'))
						val = 1;
					else if(gameCompleted(boardCopy))
							val = 0;
					
					Node node = new Node(boardCopy, root,  val, player);
					
					//drawBoard(boardCopy);
					queue.add(node);
					root.addChild(node);
				}
	}
	
	boolean playerWon(char [] board, char player){
			
		//check row
		for(int x = 0 ; x < 3; x++){
			if(board[x*2+x] == player && board[x*2+1+x] == player && board[x*2+2+x] == player)
				return true;
		}
		
		//check column
		for(int x = 0 ; x < 3; x++){
			if(board[x] == player && board[x+3] == player && board[x+6] == player)
				return true;
		}
		
		if(board[2] == player && board[4] == player && board[6] == player)
			return true;
		if(board[0] == player && board[4] == player && board[8] == player)
			return true;
		
		
		
		return false;
	}
	

	public boolean gameCompleted(char[] board){
		
		for(int x = 0; x < 9; x++)
			if(board[x] != 'X' && board[x] !='O')
				return false;
		
		return true;
	}
	
	public static void main(String[] args){
		System.out.println("Playing tictactoe");
		TicTacToe t = new TicTacToe();
		
	}
	
	int miniMax(Node n, int depth, char player){
		
		//System.out.println("Depth: " + depth + "Player: " + player + " Node.val: " + n.val + " Size : " + n.children.size());
		int size = n.children.size();
		
		if(n.children.size() == 0)
			return n.val;
		int bestValue;
		
		if(player == 'X'){ //maximizing player
			bestValue = -999;
			for(int x = 0; x  < size; x++){
			//	System.out.println("x : " + x);
				int value = miniMax(n.children.get(x),depth-1,'O');
				
				if(value > bestValue)
					bestValue = value;
				else
					bestValue = bestValue;
				
				
			}
			n.val = bestValue;
			return bestValue;
		}
		else{
			bestValue = 999;
			for(int x = 0; x  < size; x++){
				int value = miniMax(n.children.get(x),depth-1,'X');
	
				if(value <	 bestValue)
					bestValue = value;
				else
					bestValue = bestValue;
				
			}
			n.val = bestValue;
			return bestValue;
			
			
		}
		
	}
	
}

