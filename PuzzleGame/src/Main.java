import java.util.Comparator;
import java.util.TreeSet;

class GameState
{
	GameState prev;
	byte[] state;

	GameState(GameState _prev)
	{
		prev = _prev;
		state = new byte[22];
	}
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

class Main
{
	public static void main(String args[])
	{
		StateComparator comp = new StateComparator();
		TreeSet<GameState> set = new TreeSet<GameState>(comp);
		GameState a = new GameState(null);
		a.state[21] = 7;
		GameState b = new GameState(null);
		b.state[14] = 3;
		GameState c = new GameState(null);
		c.state[21] = 7;
		if(!set.contains(a))
			System.out.println("Passed 1");
		else
			System.out.println("oops 1");
		set.add(a);
		if(set.contains(a))
			System.out.println("Passed 2");
		else
			System.out.println("oops 2");
		if(!set.contains(b))
			System.out.println("Passed 3");
		else
			System.out.println("oops 3");
		if(set.contains(c))
			System.out.println("Passed 4");
		else
			System.out.println("oops 4");
	}
}
