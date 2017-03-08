package puzzlegame.assignment5;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

class Node {
    List<Node> children = new ArrayList<Node>();
    Node parent = null;
	ChessState.ChessMove move = null;
    int val;
    boolean white;

    public Node(ChessState.ChessMove m) {
        this.move = m;
    }

    public Node(ChessState.ChessMove m, Node parent, int val, boolean p) {
        this.move = m;
        this.parent = parent;
        this.val = val;
        this.white = p;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setParent(Node parent) {
        parent.addChild(this);
        this.parent = parent;
    }

    public void addChild(ChessState.ChessMove m) {
        Node child = new Node(m);
        child.setParent(this);
        this.children.add(child);
    }

    public void addChild(Node child) {
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


/// Represents the state of a chess game
class ChessState {
	public static final int MAX_PIECE_MOVES = 27;
	public static final int None = 0;
	public static final int Pawn = 1;
	public static final int Rook = 2;
	public static final int Knight = 3;
	public static final int Bishop = 4;
	public static final int Queen = 5;
	public static final int King = 6;
	public static final int PieceMask = 7;
	public static final int WhiteMask = 8;
	public static final int AllMask = 15;

	int[] m_rows;

	ChessState() {
		m_rows = new int[8];
		resetBoard();
	}

	ChessState(ChessState that) {
		m_rows = new int[8];
		for(int i = 0; i < 8; i++)
			this.m_rows[i] = that.m_rows[i];
	}

	int getPiece(int col, int row) {
		return (m_rows[row] >> (4 * col)) & PieceMask;
	}

	boolean isWhite(int col, int row) {
		return (((m_rows[row] >> (4 * col)) & WhiteMask) > 0 ? true : false);
	}

	/// Sets the piece at location (col, row). If piece is None, then it doesn't
	/// matter what the value of white is.
	void setPiece(int col, int row, int piece, boolean white) {
		m_rows[row] &= (~(AllMask << (4 * col)));
		m_rows[row] |= ((piece | (white ? WhiteMask : 0)) << (4 * col));
	}

	/// Sets up the board for a new game
	void resetBoard() {
		setPiece(0, 0, Rook, true);
		setPiece(1, 0, Knight, true);
		setPiece(2, 0, Bishop, true);
		setPiece(3, 0, Queen, true);
		setPiece(4, 0, King, true);
		setPiece(5, 0, Bishop, true);
		setPiece(6, 0, Knight, true);
		setPiece(7, 0, Rook, true);
		for(int i = 0; i < 8; i++)
			setPiece(i, 1, Pawn, true);
		for(int j = 2; j < 6; j++) {
			for(int i = 0; i < 8; i++)
				setPiece(i, j, None, false);
		}
		for(int i = 0; i < 8; i++)
			setPiece(i, 6, Pawn, false);
		setPiece(0, 7, Rook, false);
		setPiece(1, 7, Knight, false);
		setPiece(2, 7, Bishop, false);
		setPiece(3, 7, Queen, false);
		setPiece(4, 7, King, false);
		setPiece(5, 7, Bishop, false);
		setPiece(6, 7, Knight, false);
		setPiece(7, 7, Rook, false);
	}

	/// Positive means white is favored. Negative means black is favored.
	int heuristic(Random rand)
	{
		int score = 0;
		boolean queen = false;
		for(int y = 0; y < 8; y++)
		{
			for(int x = 0; x < 8; x++)
			{
				int p = getPiece(x, y);
				int value;
				switch(p)
				{
					case None: value = 0; break;
					case Pawn: value = 10; break;
					case Rook: value = 63; break;
					case Knight: value = 31; break;
					case Bishop: value = 36; break;
					case Queen: value = 88; queen = true; break;
					case King: value = 500;  break;
					default: throw new RuntimeException("what?");
				}
			
				if(isWhite(x, y))
					score += value;
				else
					score -= value;
				
			}
		}
		return score + rand.nextInt(3) - 1;
	}

	/// Returns an iterator that iterates over all possible moves for the specified color
	ChessMoveIterator iterator(boolean white) {
		return new ChessMoveIterator(this, white);
	}

	/// Returns true iff the parameters represent a valid move
	boolean isValidMove(int xSrc, int ySrc, int xDest, int yDest) {
		ArrayList<Integer> possible_moves = moves(xSrc, ySrc);
		for(int i = 0; i < possible_moves.size(); i += 2) {
			if(possible_moves.get(i).intValue() == xDest && possible_moves.get(i + 1).intValue() == yDest)
				return true;
		}
		return false;
	}

	/// Print a representation of the board to the specified stream
	void printBoard(PrintStream stream)
	{
		stream.println("  A  B  C  D  E  F  G  H");
		stream.print(" +");
		for(int i = 0; i < 8; i++)
			stream.print("--+");
		stream.println();
		for(int j = 7; j >= 0; j--) {
			stream.print(Character.toString((char)(49 + j)));
			stream.print("|");
			for(int i = 0; i < 8; i++) {
				int p = getPiece(i, j);
				if(p != None) {
					if(isWhite(i, j))
						stream.print("w");
					else
						stream.print("b");
				}
				switch(p) {
					case None: stream.print("  "); break;
					case Pawn: stream.print("p"); break;
					case Rook: stream.print("r"); break;
					case Knight: stream.print("n"); break;
					case Bishop: stream.print("b"); break;
					case Queen: stream.print("q"); break;
					case King: stream.print("K"); break;
					default: stream.print("?"); break;
				}
				stream.print("|");
			}
			stream.print(Character.toString((char)(49 + j)));
			stream.print("\n +");
			for(int i = 0; i < 8; i++)
				stream.print("--+");
			stream.println();
		}
		stream.println("  A  B  C  D  E  F  G  H" + "\n");
	}

	/// Pass in the coordinates of a square with a piece on it
	/// and it will return the places that piece can move to.
	ArrayList<Integer> moves(int col, int row) {
		ArrayList<Integer> pOutMoves = new ArrayList<Integer>();
		int p = getPiece(col, row);
		boolean bWhite = isWhite(col, row);
		int nMoves = 0;
		int i, j;
		switch(p) {
			case Pawn:
				if(bWhite) {
					if(!checkPawnMove(pOutMoves, col, inc(row), false, bWhite) && row == 1)
						checkPawnMove(pOutMoves, col, inc(inc(row)), false, bWhite);
					checkPawnMove(pOutMoves, inc(col), inc(row), true, bWhite);
					checkPawnMove(pOutMoves, dec(col), inc(row), true, bWhite);
				}
				else {
					if(!checkPawnMove(pOutMoves, col, dec(row), false, bWhite) && row == 6)
						checkPawnMove(pOutMoves, col, dec(dec(row)), false, bWhite);
					checkPawnMove(pOutMoves, inc(col), dec(row), true, bWhite);
					checkPawnMove(pOutMoves, dec(col), dec(row), true, bWhite);
				}
				break;
			case Bishop:
				for(i = inc(col), j=inc(row); true; i = inc(i), j = inc(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				for(i = dec(col), j=inc(row); true; i = dec(i), j = inc(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				for(i = inc(col), j=dec(row); true; i = inc(i), j = dec(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				for(i = dec(col), j=dec(row); true; i = dec(i), j = dec(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				break;
			case Knight:
				checkMove(pOutMoves, inc(inc(col)), inc(row), bWhite);
				checkMove(pOutMoves, inc(col), inc(inc(row)), bWhite);
				checkMove(pOutMoves, dec(col), inc(inc(row)), bWhite);
				checkMove(pOutMoves, dec(dec(col)), inc(row), bWhite);
				checkMove(pOutMoves, dec(dec(col)), dec(row), bWhite);
				checkMove(pOutMoves, dec(col), dec(dec(row)), bWhite);
				checkMove(pOutMoves, inc(col), dec(dec(row)), bWhite);
				checkMove(pOutMoves, inc(inc(col)), dec(row), bWhite);
				break;
			case Rook:
				for(i = inc(col); true; i = inc(i))
					if(checkMove(pOutMoves, i, row, bWhite))
						break;
				for(i = dec(col); true; i = dec(i))
					if(checkMove(pOutMoves, i, row, bWhite))
						break;
				for(j = inc(row); true; j = inc(j))
					if(checkMove(pOutMoves, col, j, bWhite))
						break;
				for(j = dec(row); true; j = dec(j))
					if(checkMove(pOutMoves, col, j, bWhite))
						break;
				break;
			case Queen:
				for(i = inc(col); true; i = inc(i))
					if(checkMove(pOutMoves, i, row, bWhite))
						break;
				for(i = dec(col); true; i = dec(i))
					if(checkMove(pOutMoves, i, row, bWhite))
						break;
				for(j = inc(row); true; j = inc(j))
					if(checkMove(pOutMoves, col, j, bWhite))
						break;
				for(j = dec(row); true; j = dec(j))
					if(checkMove(pOutMoves, col, j, bWhite))
						break;
				for(i = inc(col), j=inc(row); true; i = inc(i), j = inc(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				for(i = dec(col), j=inc(row); true; i = dec(i), j = inc(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				for(i = inc(col), j=dec(row); true; i = inc(i), j = dec(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				for(i = dec(col), j=dec(row); true; i = dec(i), j = dec(j))
					if(checkMove(pOutMoves, i, j, bWhite))
						break;
				break;
			case King:
				checkMove(pOutMoves, inc(col), row, bWhite);
				checkMove(pOutMoves, inc(col), inc(row), bWhite);
				checkMove(pOutMoves, col, inc(row), bWhite);
				checkMove(pOutMoves, dec(col), inc(row), bWhite);
				checkMove(pOutMoves, dec(col), row, bWhite);
				checkMove(pOutMoves, dec(col), dec(row), bWhite);
				checkMove(pOutMoves, col, dec(row), bWhite);
				checkMove(pOutMoves, inc(col), dec(row), bWhite);
				break;
			default:
				break;
		}
		return pOutMoves;
	}

	/// Moves the piece from (xSrc, ySrc) to (xDest, yDest). If this move
	/// gets a pawn across the board, it becomes a queen. If this move
	/// takes a king, then it will remove all pieces of the same color as
	/// the king that was taken and return true to indicate that the move
	/// ended the game.
	boolean move(int xSrc, int ySrc, int xDest, int yDest) throws Exception {
		if(xSrc < 0 || xSrc >= 8 || ySrc < 0 || ySrc >= 8)
			throw new Exception("out of range");
		if(xDest < 0 || xDest >= 8 || yDest < 0 || yDest >= 8)
			throw new Exception("out of range");
		int target = getPiece(xDest, yDest);
		int p = getPiece(xSrc, ySrc);
		if(p == None)
			throw new Exception("There is no piece in the source location");
		if(target != None && isWhite(xSrc, ySrc) == isWhite(xDest, yDest))
			throw new Exception("It is illegal to take your own piece");
		if(p == Pawn && (yDest == 0 || yDest == 7))
			p = Queen; // a pawn that crosses the board becomes a queen
		boolean white = isWhite(xSrc, ySrc);
		setPiece(xDest, yDest, p, white);
		setPiece(xSrc, ySrc, None, true);
		if(target == King) {
			// If you take the opponent's king, remove all of the opponent's pieces. This
			// makes sure that look-ahead strategies don't try to look beyond the end of
			// the game (example: sacrifice a king for a king and some other piece.)
			int x, y;
			for(y = 0; y < 8; y++) {
				for(x = 0; x < 8; x++) {
					if(getPiece(x, y) != None) {
						if(isWhite(x, y) != white) {
							setPiece(x, y, None, true);
						}
					}
				}
			}
			return true;
		}
		return false;
	}

	static int inc(int pos) {
		if(pos < 0 || pos >= 7)
			return -1;
		return pos + 1;
	}

	static int dec(int pos) {
		if(pos < 1)
			return -1;
		return pos -1;
	}

	boolean checkMove(ArrayList<Integer> pOutMoves, int col, int row, boolean bWhite) {
		if(col < 0 || row < 0)
			return true;
		int p = getPiece(col, row);
		if(p > 0 && isWhite(col, row) == bWhite)
			return true;
		pOutMoves.add(col);
		pOutMoves.add(row);
		return (p > 0);
	}

	boolean checkPawnMove(ArrayList<Integer> pOutMoves, int col, int row, boolean bDiagonal, boolean bWhite) {
		if(col < 0 || row < 0)
			return true;
		int p = getPiece(col, row);
		if(bDiagonal) {
			if(p == None || isWhite(col, row) == bWhite)
				return true;
		}
		else {
			if(p > 0)
				return true;
		}
		pOutMoves.add(col);
		pOutMoves.add(row);
		return (p > 0);
	}

	/// Represents a possible  move
	static class ChessMove {
		int xSource;
		int ySource;
		int xDest;
		int yDest;
	}

	/// Iterates through all the possible moves for the specified color.
	static class ChessMoveIterator
	{
		int x, y;
		ArrayList<Integer> moves;
		ChessState state;
		boolean white;

		/// Constructs a move iterator
		ChessMoveIterator(ChessState curState, boolean whiteMoves) {
			x = -1;
			y = 0;
			moves = null;
			state = curState;
			white = whiteMoves;
			advance();
		}

		private void advance() {
			if(moves != null && moves.size() >= 2) {
				moves.remove(moves.size() - 1);
				moves.remove(moves.size() - 1);
			}
			while(y < 8 && (moves == null || moves.size() < 2)) {
				if(++x >= 8) {
					x = 0;
					y++;
				}
				if(y < 8) {
					if(state.getPiece(x, y) != ChessState.None && state.isWhite(x, y) == white)
						moves = state.moves(x, y);
					else
						moves = null;
				}
			}
		}

		/// Returns true iff there is another move to visit
		boolean hasNext() {
			return (moves != null && moves.size() >= 2);
		}

		/// Returns the next move
		ChessState.ChessMove next() {
			ChessState.ChessMove m = new ChessState.ChessMove();
			m.xSource = x;
			m.ySource = y;
			m.xDest = moves.get(moves.size() - 2);
			m.yDest = moves.get(moves.size() - 1);
			advance();
			return m;
		}
	}


	public static void main(String[] args) throws Exception {
		
		ChessState s = new ChessState();             // Make a new state
		s.resetBoard();                              // Initialize to starting setup

		int depth = 4;
		/*
		ChessState.ChessMove mFirst = new ChessState.ChessMove();
		mFirst.xSource = 6;
		mFirst.ySource = 1;
		mFirst.xDest = 6;
		mFirst.yDest = 3;
		
		//Fools mate
		s.move(mFirst.xSource, mFirst.ySource, mFirst.xDest, mFirst.yDest); //white
		Node root = new Node(mFirst);
		s.move(4, 6, 4, 5);//black
		
		//manually move the pawn up
		ChessState.ChessMove mSecond = new ChessState.ChessMove();
		mSecond.xSource = 5;
		mSecond.ySource = 1;
		mSecond.xDest = 5;
		mSecond.yDest = 2;
		//s.move(5, 1, 5, 2);//white
		//s.printBoard(System.out);
		s.move(mSecond.xSource, mSecond.ySource, mSecond.xDest, mSecond.yDest); //Move the board to the right state
		
	/*	
		s.move(3, 7, 7, 3); //Move queen to check mate - black
		s.printBoard(System.out);
		
		s.move(4, 1, 4, 2); //move random pawn - white
		s.printBoard(System.out);
		int h = s.heuristic(new Random()); //Find node value
		System.out.println(h);	
		
	//	s.move(7, 3, 4, 0); //Take king - black - game over
	//	h = s.heuristic(new Random()); //Find node value
	//	System.out.println(h);	
		*/

		Scanner reader = new Scanner(System.in); 
		Node root;
		s.printBoard(System.out);
	while(true){
			
		//Wait for player to make their next move by input....
		System.out.print("\nYour Move: " );
		String playerMove = "";
		playerMove = reader.nextLine();
		while(!validMove(playerMove)){
			System.out.println("Invalid input. Enter in  the form of: c2c3");
			System.out.print("Youre Move: ");
			playerMove = reader.nextLine();
		}
		System.out.println();
		ChessState.ChessMove mPlayer = getMove(playerMove);

		s.move(mPlayer.xSource, mPlayer.ySource, mPlayer.xDest, mPlayer.yDest); //white
		root = new Node(mPlayer);
		//////End player move
		
			////////////////////////
			//Calculate computer move and move the board to that new state
			computeTree(root,s,depth,false);
			
			int bestValue = miniMax(root,depth,-999,999,false); //Find the best move
			int childNum = 0;
			boolean found = false;
			for(int x = 0;  x < root.children.size(); x++){
				Node n = root.children.get(x);
				if(n.val == bestValue)
					found = true;
				
				if(found){
					//System.out.println("Found best child at: " + x + " val: " + n.val);
					childNum = x;
					x = root.children.size();
				}			
			}
			ChessState.ChessMove m1 = root.children.get(childNum).move;
			//System.out.println(" m1.xS "  + m1.xSource + " m1.yS " + m1.ySource + " m1.xDest: " + m1.xDest + " m1.yDest: " + m1.yDest);
			
			s.move(m1.xSource, m1.ySource, m1.xDest, m1.yDest);
			s.printBoard(System.out);
			
			////////////////////////////////////
			
		
		
		}
	}
	
	private static boolean validMove(String playerMove) {
		if(playerMove.length() < 4 || playerMove.length() > 5)
			return false;
		char arr[] = {'a','b','c','d','e','f','g','h'}; 
		char c1 = playerMove.charAt(0);
		char c2 = playerMove.charAt(1);
		char c3 = playerMove.charAt(2);
		char c4 = playerMove.charAt(3);
		boolean valid1 = false;
		boolean valid2 = false;
		for(int x = 0; x < 8; x++){
			if(c1 == arr[x])
				valid1 = true;
			 if(c3 == arr[x])
				valid2 = true;
		}
		
		if(!valid1)
			return false;
		if(!valid2)
			return false;
		//System.out.println("c2 - 49: " + (c2 - 49));
		if(c2 - 49 < -1 || c2 - 49 > 8)
			return false;
		
		
		if(c4 - 49 < -1 || c4 - 49 > 8)
			return false;
		
		return true;
	}

	static ChessState.ChessMove getMove(String s){
		char arr[] = {'a','b','c','d','e','f','g','h'}; 
		ChessState.ChessMove m = new ChessState.ChessMove();
		
		for(int x = 0; x < 8; x++){
			if(s.charAt(0) == arr[x])
				m.xSource = x;
			 if(s.charAt(2) == arr[x])
				m.xDest = x;
		}
		
		m.ySource = s.charAt(1) - 49;
		m.yDest = s.charAt(3) - 49;
		
		//System.out.println(m.xSource + " , " + m.ySource + "  " + m.xDest + "," + m.yDest);
		
		return m;
	}
	
	
	static void computeTree(Node root,ChessState s, int depth, boolean white) throws Exception{
		if(depth == 0)
			return;
		
		depth--;
		
		ChessMoveIterator it;  
		ChessState.ChessMove m;
		
		it = s.iterator(white); //Create new iterator
		if(it.hasNext())
			m = it.next(); //Find next move
		else
			return;
		
		while(it.hasNext()) {
			
			ChessState newState = new ChessState(s); //Make new state
			newState.move(m.xSource, m.ySource, m.xDest, m.yDest); //Move new state to the next move
			
			int h = newState.heuristic(new Random()); //Find node value
		    Node child = new Node(m,root,h,white); //Make new child with all the new values
		    root.addChild(child);
		    
			computeTree(child, newState,depth,!white);
		  
		    //System.out.println(h + " ");	
		
			//newState.printBoard(System.out);
			m = it.next();
		}
	
		
	}
	
	static int miniMax(Node n, int depth, int a, int b, boolean white){
		
		//System.out.println("Depth: " + depth + "Player: " + player + " Node.val: " + n.val + " Size : " + n.children.size());
		int size = n.children.size();
		int alpha = a;
		int beta = b;
		
		if(n.children.size() == 0)
			return n.val;
		int bestValue;
		
		if(white == true){ //maximizing player
			bestValue = -999;
			for(int x = 0; x  < size; x++){
			//	System.out.println("x : " + x);
				int value = miniMax(n.children.get(x),depth-1,alpha, beta,false);
				
				bestValue = Math.max(value, bestValue);
				alpha = Math.max(a,bestValue);
				
				if(b < alpha)
					break; //MAYBE ONLY RETURN?
				
				
			}
			n.val = bestValue;
			return bestValue;
		}
		else{
			bestValue = 999;
			for(int x = 0; x  < size; x++){
				int value = miniMax(n.children.get(x),depth-1,alpha, beta, true);
	
				bestValue = Math.min(value, bestValue);
				beta = Math.min(b,bestValue);
				
				if(beta < a)
					break;
				//insert alpha beta pruning
				
			}
			n.val = bestValue;
			return bestValue;
			
			
		}
		
	}
	
	
}




