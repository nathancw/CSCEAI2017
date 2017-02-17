package puzzlegame.assignment4;

import java.util.List;
import java.util.ArrayList;

class Node<T> {
    private List<Node<T>> children = new ArrayList<Node<T>>();
    private Node<T> parent = null;
    private T data = null;

    public Node(T data) {
        this.data = data;
    }

    public Node(T data, Node<T> parent) {
        this.data = data;
        this.parent = parent;
    }

    public List<Node<T>> getChildren() {
        return children;
    }

    public void setParent(Node<T> parent) {
        parent.addChild(this);
        this.parent = parent;
    }

    public void addChild(T data) {
        Node<T> child = new Node<T>(data);
        child.setParent(this);
        this.children.add(child);
    }

    public void addChild(Node<T> child) {
        child.setParent(this);
        this.children.add(child);
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
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
	public TicTacToe(){
		char[] board = {'1','2','3','4','5','6','7','8','9'};
		root = new Node(board);
		calculateBoardStates(root);
		drawBoard(board);
	}
	
	public void drawBoard(char[] board){
		
		System.out.println(" " + board[0] + " | " + board[1] + " | " + board[2] + " ");
		System.out.println("---+---+---");
		System.out.println(" " + board[3] + " | " + board[4] + " | " + board[5] + " ");
		System.out.println("---+---+---");
		System.out.println(" " + board[6] + " | " + board[7] + " | " + board[8] + " ");
		
	}
	
	public void calculateBoardStates(Node root){
		
		
	}
	
	public static void main(String[] args){
		System.out.println("Playing tictactoe");
		TicTacToe t = new TicTacToe();
		
	}
}

