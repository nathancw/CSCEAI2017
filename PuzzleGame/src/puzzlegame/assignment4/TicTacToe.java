package puzzlegame.assignment4;

public class TicTacToe {

	
	public static void main(String[] args){
		System.out.println("Playing tictactoe");
		
		char[] board = {'1','2','3','4','5','6','7','8','9'};
		
		drawBoard(board);
		/*System.out.println(" 1 | 2 | 3 ");
		System.out.println("---+---+---");
		System.out.println(" 4 | 5 | 6 ");
		System.out.println("---+---+---");
		System.out.println(" 7 | 8 | 9 ");
		*/
		
	}
	
	
	public static void drawBoard(char[] board){
		
		System.out.println(" " + board[0] + " | " + board[1] + " | " + board[2] + " ");
		System.out.println("---+---+---");
		System.out.println(" " + board[3] + " | " + board[4] + " | " + board[5] + " ");
		System.out.println("---+---+---");
		System.out.println(" " + board[6] + " | " + board[7] + " | " + board[8] + " ");
		
	}
}
