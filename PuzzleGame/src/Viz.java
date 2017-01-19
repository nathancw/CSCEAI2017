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
import java.util.Random;
import java.awt.Color;

class View extends JPanel implements MouseListener {
	Viz viz;
	Random rand;
	byte[] state;
	Graphics graphics;
	int size;
	boolean[][] board;

	View(Viz v) throws IOException
	{
		viz = v;
		rand = new Random();
		state = new byte[22];
		size = 48;
		board = new boolean[10][10];
	}

	public void mousePressed(MouseEvent e)
	{
		//state[rand.nextInt(22)] += (rand.nextInt(2) == 0 ? -1 : 1);
		int id = 10;
		boolean validMove;
		
		//Looping through all the shapes and seeing if it can move in that direction
		for(int index = 0; index < 11; index++){
			
			validMove = moveLeft(index);
			if(validMove)
				viz.repaint(); 
			
			validMove = moveRight(index);
			if(validMove)
				viz.repaint();
			
			validMove = moveUp(index);
			if(validMove)
				viz.repaint(); 
			
			validMove = moveDown(index);
			if(validMove)
				viz.repaint();
		}
		
		
	}

	public boolean moveDown(int id) {
	
		state[2*id+1] = (byte) (state[2*id+1] + 1);
		boolean validMove = drawShapes(id, state);
		
		if(validMove)
			return true;
		else{
			state[2*id+1] = (byte) (state[2*id+1] - 1);
			return false;
		}
	}

	public boolean moveUp(int id) {
		state[2*id+1] = (byte) (state[2*id+1] - 1);
		boolean validMove = drawShapes(id, state);
		
		if(validMove)
			return true;
		else{
			state[2*id+1] = (byte) (state[2*id+1] + 1);
			return false;
		}
	}

	public boolean moveRight(int id) {
		
		state[2*id] = (byte) (state[2*id] + 1);
		boolean validMove = drawShapes(id, state);
		
		if(validMove)
			return true;
		else{
			state[2*id] = (byte) (state[2*id] - 1);
			return false;
		}

	}

	public boolean moveLeft(int id) {
		
		state[2*id] = (byte) (state[2*id] - 1);
		boolean validMove = drawShapes(id, state);
		
		if(validMove)
			return true;
		else{
			state[2*id] = (byte) (state[2*id] + 1);
			return false;
		}
		
		/*validMove = drawShapes(id, state);
		System.out.println("Valid move: " + validMove + " i: " + index);
		if(validMove){
			
			for(int i = 0; i < 11; i++)
			System.out.print("(" + state[2 * i] + "," +
					state[2 * i + 1] + ") ");
				System.out.println();

				viz.repaint();
		}
		else{
			state[2*index] = (byte) (state[2*index] + 1);
		} */
		
	}

	public boolean drawShapes(int id, byte[] state2) {
	
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
		if(!shape(0, 255, 0, 0, 1, 3, 2, 3, 1, 4, 2, 4))
				return false;
		else if(!shape(1, 0, 255, 0, 1, 5, 1, 6, 2, 6))
			return false;
		else if(!shape(2, 128, 128, 255, 2, 5, 3, 5, 3, 6))
				return false;
		else if(!shape(3, 255, 128, 128, 3, 7, 3, 8, 4, 8))
				return false;
		else if(!shape(4, 255, 255, 128, 4, 7, 5, 7, 5, 8))
				return false;
		else if(!shape(5, 128, 128, 0, 6, 7, 7, 7, 6, 8))
				return false;
		else if(!shape(6, 0, 128, 128, 5, 4, 5, 5, 5, 6, 4, 5))
				return false;
		else if(!shape(7, 0, 128, 0, 6, 4, 6, 5, 6, 6, 7, 5))
				return false;
		else if(!shape(8, 0, 255, 255, 8, 5, 8, 6, 7, 6)) 
				return false; 
		else if(!shape(9, 0, 0, 255, 6, 2, 6, 3, 5, 3))
				return false; 
		else if(!shape(10, 255, 128, 0, 5, 1, 6, 1, 5, 2)){
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
		graphics.fillRect(size * x, size * y, size, size);
			return true;
	}

	
	/*public void b(int x, int y)
	{
		
		graphics.fillRect(size * x, size * y, size, size);
	} */

	// Draw a 3-block piece
	public boolean shape(int id, int red, int green, int blue,
		int x1, int y1, int x2, int y2, int x3, int y3)
	{
		graphics.setColor(new Color(red, green, blue));
		boolean b1 = b(state[2 * id] + x1, state[2 * id + 1] + y1);
		boolean b2 = b(state[2 * id] + x2, state[2 * id + 1] + y2);
		boolean b3 = b(state[2 * id] + x3, state[2 * id + 1] + y3);
		//System.out.println("b1 :  " + b1 + " b2: " + b2 + " b3 : " + b3 + " total: " + (b1 && b2 && b3));
		return (b1 && b2 && b3);
	}

	// Draw a 4-block piece
	public boolean shape(int id, int red, int green, int blue,
		int x1, int y1, int x2, int y2,
		int x3, int y3, int x4, int y4)
	{
		boolean s1 = shape(id, red, green, blue, x1, y1, x2, y2, x3, y3);
		boolean b1 = b(state[2 * id] + x4, state[2 * id + 1] + y4);
		
		return (s1 && b1);
	}

	public void paintComponent(Graphics g)
	{
		for(int x = 0; x < 10; x++)
			for(int c = 0; c < 10; c++)
				board[x][c] = false;
	
		
		// Draw the black squares
		graphics = g;
		g.setColor(new Color(0, 0, 0));
		for(int i = 0; i < 10; i++) { b(i, 0); b(i, 9); }
		for(int i = 1; i < 9; i++) { b(0, i); b(9, i); }
		b(1, 1); b(1, 2); b(2, 1);
		b(7, 1); b(8, 1); b(8, 2);
		b(1, 7); b(1, 8); b(2, 8);
		b(8, 7); b(7, 8); b(8, 8);
		b(3, 4); b(4, 4); b(4, 3);

		// Draw the pieces
		shape(0, 255, 0, 0, 1, 3, 2, 3, 1, 4, 2, 4);
		shape(1, 0, 255, 0, 1, 5, 1, 6, 2, 6);
		shape(2, 128, 128, 255, 2, 5, 3, 5, 3, 6);
		shape(3, 255, 128, 128, 3, 7, 3, 8, 4, 8);
		shape(4, 255, 255, 128, 4, 7, 5, 7, 5, 8);
		shape(5, 128, 128, 0, 6, 7, 7, 7, 6, 8);
		shape(6, 0, 128, 128, 5, 4, 5, 5, 5, 6, 4, 5);
		shape(7, 0, 128, 0, 6, 4, 6, 5, 6, 6, 7, 5);
		shape(8, 0, 255, 255, 8, 5, 8, 6, 7, 6); 
		shape(9, 0, 0, 255, 6, 2, 6, 3, 5, 3); 
		shape(10, 255, 128, 0, 5, 1, 6, 1, 5, 2);
	}
}

public class Viz extends JFrame
{
	public Viz() throws Exception
	{
		View view = new View(this);
		view.addMouseListener(view);
		this.setTitle("Puzzle");
		this.setSize(482, 505);
		this.getContentPane().add(view);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	public static void main(String[] args) throws Exception
	{
		new Viz();
	}
}