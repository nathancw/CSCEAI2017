package puzzlegame.assignment8;
import java.util.Random;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;


public class WebbNathaniel {
	
	static class ReflexAgent implements IAgent
	{
		int index; // a temporary value used to pass values around

		ReflexAgent() {
		}

		public void reset() {
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
				m.setDestination(i, m.getX(i) + dx * 10.0f, m.getY(i) + dy * 10.0f);
			}
		}

		void beDefender(Model m, int i) {
			// Find the opponent nearest to my flag
			nearestOpponent(m, Model.XFLAG, Model.YFLAG);
			if(index >= 0) {
				float enemyX = m.getXOpponent(index);
				float enemyY = m.getYOpponent(index);

				// Stay between the enemy and my flag
				m.setDestination(i, 0.5f * (Model.XFLAG + enemyX), 0.5f * (Model.YFLAG + enemyY));

				// Throw boms if the enemy gets close enough
				if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
					m.throwBomb(i, enemyX, enemyY);
			}
			else {
				// Guard the flag
				m.setDestination(i, Model.XFLAG + Model.MAX_THROW_RADIUS, Model.YFLAG);
			}

			// If I don't have enough energy to throw a bomb, rest
			if(m.getEnergySelf(i) < Model.BOMB_COST)
				m.setDestination(i, m.getX(i), m.getY(i));

			// Try not to die
			avoidBombs(m, i);
		}

		void beFlagAttacker(Model m, int i) {
			// Head for the opponent's flag
			m.setDestination(i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);

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
					m.setDestination(i, enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON));

					// Throw bombs
					if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
						m.throwBomb(i, enemyX, enemyY);
				}
				else {

					// If the opponent is close enough to shoot at me...
					if(sq_dist(enemyX, enemyY, myX, myY) <= (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS)) {
						m.setDestination(i, myX + 10.0f * (myX - enemyX), myY + 10.0f * (myY - enemyY)); // Flee
					}
					else {
						m.setDestination(i, myX, myY); // Rest
					}
				}
			}

			// Try not to die
			avoidBombs(m, i);
		}

		public void update(Model m) {
			beFlagAttacker(m, 0);
			beAggressor(m, 1);
			beDefender(m, 2);
		}
	}
	
	
	static class NeuralAgent implements IAgent
	{
		int index; // a temporary value used to pass values around
		NeuralNet nn;
		double[] in;

		NeuralAgent(double[] weights) {
			in = new double[20];
			nn = new NeuralNet();
			nn.layers.add(new LayerTanh(in.length, 8));
			nn.layers.add(new LayerTanh(8, 10));
			nn.layers.add(new LayerTanh(10, 3));
			setWeights(weights);
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
				m.setDestination(i, m.getX(i) + dx * 10.0f, m.getY(i) + dy * 10.0f);
			}
		}

		void beDefender(Model m, int i) {
			// Find the opponent nearest to my flag
			nearestOpponent(m, Model.XFLAG, Model.YFLAG);
			if(index >= 0) {
				float enemyX = m.getXOpponent(index);
				float enemyY = m.getYOpponent(index);

				// Stay between the enemy and my flag
				m.setDestination(i, 0.5f * (Model.XFLAG + enemyX), 0.5f * (Model.YFLAG + enemyY));

				// Throw boms if the enemy gets close enough
				if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
					m.throwBomb(i, enemyX, enemyY);
			}
			else {
				// Guard the flag
				m.setDestination(i, Model.XFLAG + Model.MAX_THROW_RADIUS, Model.YFLAG);
			}

			// If I don't have enough energy to throw a bomb, rest
			if(m.getEnergySelf(i) < Model.BOMB_COST)
				m.setDestination(i, m.getX(i), m.getY(i));

			// Try not to die
			avoidBombs(m, i);
		}

		void beFlagAttacker(Model m, int i) {
			// Head for the opponent's flag
			m.setDestination(i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);

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
					m.setDestination(i, enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON));

					// Throw bombs
					if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
						m.throwBomb(i, enemyX, enemyY);
				}
				else {

					// If the opponent is close enough to shoot at me...
					if(sq_dist(enemyX, enemyY, myX, myY) <= (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS)) {
						m.setDestination(i, myX + 10.0f * (myX - enemyX), myY + 10.0f * (myY - enemyY)); // Flee
					}
					else {
						m.setDestination(i, myX, myY); // Rest
					}
				}
			}

			// Try not to die
			avoidBombs(m, i);
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
			in[7] = nearestOpponent(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
			in[8] = nearestOpponent(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
			in[9] = nearestBombTarget(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
			in[10] = nearestBombTarget(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
			in[11] = nearestBombTarget(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
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
			for(int i = 0; i < 3; i++)
			{
				if(out[i] < -0.333)
					beDefender(m, i);
				else if(out[i] > 0.333)
					beAggressor(m, i);
				else
					beFlagAttacker(m, i);
			}
		}
	}
	
	static public class Vec
	{
		public static String toString(double[] vec) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			if(vec.length > 0) {
				sb.append(Double.toString(vec[0]));
				for(int i = 1; i < vec.length; i++) {
					sb.append(",");
					sb.append(Double.toString(vec[i]));
				}
			}
			sb.append("]");
			return sb.toString();
		}

		public static void setAll(double[] vec, double val) {
			for(int i = 0; i < vec.length; i++)
				vec[i] = val;
		}

		public static double squaredMagnitude(double[] vec) {
			double d = 0.0;
			for(int i = 0; i < vec.length; i++)
				d += vec[i] * vec[i];
			return d;
		}

		public static void normalize(double[] vec) {
			double mag = squaredMagnitude(vec);
			if(mag <= 0.0) {
				setAll(vec, 0.0);
				vec[0] = 1.0;
			} else {
				double s = 1.0 / Math.sqrt(mag);
				for(int i = 0; i < vec.length; i++)
					vec[i] *= s;
			}
		}

		public static void copy(double[] dest, double[] src) {
			if(dest.length != src.length)
				throw new IllegalArgumentException("mismatching sizes");
			for(int i = 0; i < src.length; i++) {
				dest[i] = src[i];
			}
		}

		public static double[] copy(double[] src) {
			double[] dest = new double[src.length];
			for(int i = 0; i < src.length; i++) {
				dest[i] = src[i];
			}
			return dest;
		}

		public static void add(double[] dest, double[] src) {
			if(dest.length != src.length)
				throw new IllegalArgumentException("mismatching sizes");
			for(int i = 0; i < dest.length; i++) {
				dest[i] += src[i];
			}
		}

		public static void scale(double[] dest, double scalar) {
			for(int i = 0; i < dest.length; i++) {
				dest[i] *= scalar;
			}
		}

		public static double dotProduct(double[] a, double[] b) {
			if(a.length != b.length)
				throw new IllegalArgumentException("mismatching sizes");
			double d = 0.0;
			for(int i = 0; i < a.length; i++)
				d += a[i] * b[i];
			return d;
		}

		public static double squaredDistance(double[] a, double[] b) {
			if(a.length != b.length)
				throw new IllegalArgumentException("mismatching sizes");
			double d = 0.0;
			for(int i = 0; i < a.length; i++) {
				double t = a[i] - b[i];
				d += t * t;
			}
			return d;
		}

		public static void clip(double[] vec, double min, double max) {
			if(max < min)
				throw new IllegalArgumentException("max must be >= min");
			for(int i = 0; i < vec.length; i++) {
				vec[i] = Math.max(min, Math.min(max, vec[i]));
			}
		}

		public static double[] concatenate(double[] a, double[] b) {
			double[] c = new double[a.length + b.length];
			for(int i = 0; i < a.length; i++)
				c[i] = a[i];
			for(int i = 0; i < b.length; i++)
				c[a.length + i] = b[i];
			return c;
		}

	}
	
	static public class Matrix
	{
		/// Used to represent elements in the matrix for which the value is not known.
		public static final double UNKNOWN_VALUE = -1e308; 

		// Data
		private ArrayList<double[]> m_data = new ArrayList<double[]>(); //matrix elements

		// Meta-data
		private String m_filename;                          // the name of the file
		private ArrayList<String> m_attr_name;                 // the name of each attribute (or column)
		private ArrayList<HashMap<String, Integer>> m_str_to_enum; // value to enumeration
		private ArrayList<HashMap<Integer, String>> m_enum_to_str; // enumeration to value

		/// Creates a 0x0 matrix. (Next, to give this matrix some dimensions, you should call:
		///    loadARFF
		///    setSize
		///    addColumn, or
		///    copyMetaData
		@SuppressWarnings("unchecked")
		public Matrix() 
		{
			this.m_filename    = "";
			this.m_attr_name   = new ArrayList<String>();
			this.m_str_to_enum = new ArrayList<HashMap<String, Integer>>();
			this.m_enum_to_str = new ArrayList<HashMap<Integer, String>>();
		}

		public Matrix(int rows, int cols)
		{
			this.m_filename    = "";
			this.m_attr_name   = new ArrayList<String>();
			this.m_str_to_enum = new ArrayList<HashMap<String, Integer>>();
			this.m_enum_to_str = new ArrayList<HashMap<Integer, String>>();
			setSize(rows, cols);
		}

		public Matrix(Matrix that)
		{
			setSize(that.rows(), that.cols());
			m_filename = that.m_filename;
			m_attr_name = new ArrayList<String>();
			m_str_to_enum = new ArrayList<HashMap<String, Integer>>();
			m_enum_to_str = new ArrayList<HashMap<Integer, String>>();
			copyBlock(0, 0, that, 0, 0, that.rows(), that.cols());
		}

		/// Loads the matrix from an ARFF file
		public void loadARFF(String filename)
		{
			HashMap<String, Integer> tempMap  = new HashMap<String, Integer>(); //temp map for int->string map (attrInts)
			HashMap<Integer, String> tempMapS = new HashMap<Integer, String>(); //temp map for string->int map (attrString)
			int attrCount = 0; // Count number of attributes
			int lineNum = 0; // Used for exception messages
			Scanner s = null;
			m_str_to_enum.clear();
			m_enum_to_str.clear();
			m_attr_name.clear();

			try
			{
				s = new Scanner(new File(filename));
				while (s.hasNextLine())
				{
					lineNum++;
					String line  = s.nextLine().trim();
					String upper = line.toUpperCase();

					if (upper.startsWith("@RELATION"))
						m_filename = line.split(" ")[1];
					else if (upper.startsWith("@ATTRIBUTE"))
					{
						String[] pieces = line.split("\\s+");
						m_attr_name.add(pieces[1]);
						
						tempMap.clear();
						tempMapS.clear();
						
						// If the attribute is nominal
						if (pieces[2].startsWith("{"))
						{
							// Splits this string based on curly brackets or commas
							String[] attributeNames = pieces[2].split("[{},]");
							int valCount = 0;
							
							for (String attribute : attributeNames)
							{
								if (!attribute.equals("")) // Ignore empty strings
								{
									tempMapS.put(valCount, attribute);
									tempMap.put(attribute, valCount++);
								}
							}
						}
						
						// The attribute is continuous if it wasn't picked up in the previous "if" statement
						
						m_str_to_enum.add(new HashMap<String, Integer>(tempMap));
						m_enum_to_str.add(new HashMap<Integer, String>(tempMapS));
						
						attrCount++;
					}
					else if (upper.startsWith("@DATA"))
					{
						m_data.clear();
						
						while (s.hasNextLine())
						{
							double[] temp = new double[attrCount];

							lineNum++;
							line  = s.nextLine().trim();
							
							if (line.startsWith("%") || line.isEmpty()) continue;
							String[] pieces = line.split(",");
							
							if (pieces.length < attrCount) throw new IllegalArgumentException("Expected more elements on line: " + lineNum + ".");
							
							for (int i = 0; i < attrCount; i++)
							{
								int vals   = valueCount(i);
								String val = pieces[i];
								
								// Unknown values are always set to UNKNOWN_VALUE
								if (val.equals("?"))
								{
									temp[i] = UNKNOWN_VALUE;
									continue;
								}
			
								// If the attribute is nominal
								if (vals > 0)
								{
									HashMap<String, Integer> enumMap = m_str_to_enum.get(i);
									if (!enumMap.containsKey(val))
										throw new IllegalArgumentException("Unrecognized enumeration value " + val + " on line: " + lineNum + ".");
										
									temp[i] = (double)enumMap.get(val);
								}
								else
									temp[i] = Double.parseDouble(val); // The attribute is continuous
							}
							
							m_data.add(temp);
						}
					}
				}
			}
			catch (FileNotFoundException e)
			{
				throw new IllegalArgumentException("Failed to open file: " + filename + ".");
			}
			finally
			{
				s.close();
			}
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			for(int j = 0; j < rows(); j++) {
				if(j > 0)
					sb.append("\n");
				sb.append(Vec.toString(row(j)));
			}
			return sb.toString();
		}

		/// Saves the matrix to an ARFF file
		public void saveARFF(String filename)
		{		
			PrintWriter os = null;
			
			try
			{
				os = new PrintWriter(filename);
				// Print the relation name, if one has been provided ('x' is default)
				os.print("@RELATION ");
				os.println(m_filename.isEmpty() ? "x" : m_filename);
				
				// Print each attribute in order
				for (int i = 0; i < m_attr_name.size(); i++)
				{
					os.print("@ATTRIBUTE ");
					
					String attributeName = m_attr_name.get(i);
					os.print(attributeName.isEmpty() ? "x" : attributeName);
					
					int vals = valueCount(i);
					
					if (vals == 0) os.println(" REAL");
					else
					{
						os.print(" {");
						for (int j = 0; j < vals; j++)
						{
							os.print(attrValue(i, j));
							if (j + 1 < vals) os.print(",");
						}
						os.println("}");
					}
				}
				
				// Print the data
				os.println("@DATA");
				for (int i = 0; i < rows(); i++)
				{
					double[] row = m_data.get(i);
					for (int j = 0; j < cols(); j++)
					{
						if (row[j] == UNKNOWN_VALUE)
							os.print("?");
						else
						{
							int vals = valueCount(j);
							if (vals == 0) os.print(row[j]);
							else
							{
								int val = (int)row[j];
								if (val >= vals) throw new IllegalArgumentException("Value out of range.");
								os.print(attrValue(j, val));
							}
						}
						
						if (j + 1 < cols())	os.print(",");
					}
					os.println();
				}
			}
			catch (FileNotFoundException e)
			{
				throw new IllegalArgumentException("Error creating file: " + filename + ".");
			}
			finally
			{
				os.close();
			}
		}

		/// Makes a rows-by-columns matrix of *ALL CONTINUOUS VALUES*.
		/// This method wipes out any data currently in the matrix. It also
		/// wipes out any meta-data.
		public void setSize(int rows, int cols)
		{
			m_data.clear();

			// Set the meta-data
			m_filename = "";
			m_attr_name.clear();
			m_str_to_enum.clear();
			m_enum_to_str.clear();

			// Make space for each of the columns, then each of the rows
			newColumns(cols);
			newRows(rows);
		}

		/// Clears this matrix and copies the meta-data from that matrix.
		/// In other words, it makes a zero-row matrix with the same number
		/// of columns as "that" matrix. You will need to call newRow or newRows
		/// to give the matrix some rows.
		@SuppressWarnings("unchecked")
		public void copyMetaData(Matrix that)
		{
			m_data.clear();
			m_attr_name = new ArrayList<String>(that.m_attr_name);
			
			// Make a deep copy of that.m_str_to_enum
			m_str_to_enum = new ArrayList<HashMap<String, Integer>>();
			for (HashMap<String, Integer> map : that.m_str_to_enum)
			{
				HashMap<String, Integer> temp = new HashMap<String, Integer>();
				for (Map.Entry<String, Integer> entry : map.entrySet())
					temp.put(entry.getKey(), entry.getValue());
				
				m_str_to_enum.add(temp);
			}
			
			// Make a deep copy of that.m_enum_to_string
			m_enum_to_str = new ArrayList<HashMap<Integer, String>>();
			for (HashMap<Integer, String> map : that.m_enum_to_str)
			{
				HashMap<Integer, String> temp = new HashMap<Integer, String>();
				for (Map.Entry<Integer, String> entry : map.entrySet())
					temp.put(entry.getKey(), entry.getValue());
				
				m_enum_to_str.add(temp);
			}
		}

		/// Adds a column to this matrix with the specified number of values. (Use 0 for
		/// a continuous attribute.) This method also sets the number of rows to 0, so
		/// you will need to call newRow or newRows when you are done adding columns.
		public void newColumn(int vals)
		{
			m_data.clear();
			String name = "col_" + cols();
			
			m_attr_name.add(name);
			
			HashMap<String, Integer> temp_str_to_enum = new HashMap<String, Integer>();
			HashMap<Integer, String> temp_enum_to_str = new HashMap<Integer, String>();
			
			for (int i = 0; i < vals; i++)
			{
				String sVal = "val_" + i;
				temp_str_to_enum.put(sVal, i);
				temp_enum_to_str.put(i, sVal);
			}
			
			m_str_to_enum.add(temp_str_to_enum);
			m_enum_to_str.add(temp_enum_to_str);
		}
		
		/// Adds a column to this matrix with 0 values (continuous data).
		public void newColumn()
		{
			this.newColumn(0);
		}
		
		/// Adds n columns to this matrix, each with 0 values (continuous data).
		public void newColumns(int n)
		{
			for (int i = 0; i < n; i++)
				newColumn();
		}
		
		/// Adds one new row to this matrix. Returns a reference to the new row.
		public double[] newRow()
		{
			int c = cols();
			if (c == 0)
				throw new IllegalArgumentException("You must add some columns before you add any rows.");
			double[] newRow = new double[c];
			m_data.add(newRow);
			return newRow;
		}
		
		/// Adds 'n' new rows to this matrix
		public void newRows(int n)
		{
			for (int i = 0; i < n; i++)
				newRow();
		}
		
		/// Returns the number of rows in the matrix
		public int rows() { return m_data.size(); }
		
		/// Returns the number of columns (or attributes) in the matrix
		public int cols() { return m_attr_name.size(); }
		
		/// Returns the name of the specified attribute
		public String attrName(int col) { return m_attr_name.get(col); }
		
		/// Returns the name of the specified value
		public String attrValue(int attr, int val)
		{		
			String value = m_enum_to_str.get(attr).get(val);
			if (value == null)
				throw new IllegalArgumentException("No name.");
			else return value;
		}
		
		/// Returns a reference to the specified row
		public double[] row(int index) { return m_data.get(index); }
		
		/// Swaps the positions of the two specified rows
		public void swapRows(int a, int b)
		{
			double[] temp = m_data.get(a);
			m_data.set(a, m_data.get(b));
			m_data.set(b, temp);
		}
		
		/// Returns the number of values associated with the specified attribute (or column)
		/// 0 = continuous, 2 = binary, 3 = trinary, etc.
		public int valueCount(int attr) { return m_enum_to_str.get(attr).size(); }
		
		/// Returns the mean of the elements in the specified column. (Elements with the value UNKNOWN_VALUE are ignored.)
		public double columnMean(int col)
		{
			double sum = 0.0;
			int count = 0;
			for (double[] list : m_data)
			{
				double val = list[col];
				if (val != UNKNOWN_VALUE)
				{
					sum += val;
					count++;
				}
			}
			
			return sum / count;
		}
		
		/// Returns the minimum element in the specified column. (Elements with the value UNKNOWN_VALUE are ignored.)
		public double columnMin(int col)
		{
			double min = Double.MAX_VALUE;
			for (double[] list : m_data)
			{
				double val = list[col];
				if (val != UNKNOWN_VALUE)
					min = Math.min(min, val);
			}
			
			return min;
		}

		/// Returns the maximum element in the specifed column. (Elements with the value UNKNOWN_VALUE are ignored.)
		public double columnMax(int col)
		{
			double max = -Double.MAX_VALUE;
			for (double[] list : m_data)
			{
				double val = list[col];
				if (val != UNKNOWN_VALUE)
					max = Math.max(max, val);
			}
			
			return max;
		}
		
		/// Returns the most common value in the specified column. (Elements with the value UNKNOWN_VALUE are ignored.)
		public double mostCommonValue(int col)
		{
			HashMap<Double, Integer> counts = new HashMap<Double, Integer>();
			for (double[] list : m_data)
			{
				double val = list[col];
				if (val != UNKNOWN_VALUE)
				{
					Integer result = counts.get(val);
					if (result == null) result = 0;
					
					counts.put(val, result + 1);
				}
			}
			
			int valueCount = 0;
			double value   = 0;
			for (Map.Entry<Double, Integer> entry : counts.entrySet())
			{
				if (entry.getValue() > valueCount)
				{
					value      = entry.getKey();
					valueCount = entry.getValue();
				}
			}
			
			return value;
		}

		/// Copies the specified rectangular portion of that matrix, and puts it in the specified location in this matrix.
		public void copyBlock(int destRow, int destCol, Matrix that, int rowBegin, int colBegin, int rowCount, int colCount)
		{
			if (destRow + rowCount > this.rows() || destCol + colCount > this.cols())
				throw new IllegalArgumentException("Out of range for destination matrix.");
			if (rowBegin + rowCount > that.rows() || colBegin + colCount > that.cols())
				throw new IllegalArgumentException("Out of range for source matrix.");

			// Copy the specified region of meta-data
			for (int i = 0; i < colCount; i++)
			{
				m_attr_name.set(destCol + i, that.m_attr_name.get(colBegin + i));
				m_str_to_enum.set(destCol + i, new HashMap<String, Integer>(that.m_str_to_enum.get(colBegin + i)));
				m_enum_to_str.set(destCol + i, new HashMap<Integer, String>(that.m_enum_to_str.get(colBegin + i)));
			}

			// Copy the specified region of data
			for (int i = 0; i < rowCount; i++)
			{
				double[] source = that.row(rowBegin + i);
				double[] dest = this.row(destRow + i);
				for(int j = 0; j < colCount; j++)
					dest[j] = source[colBegin + j];
			}
		}
		
		/// Sets every element in the matrix to the specified value.
		public void setAll(double val)
		{
			for (double[] list : m_data) {
				for(int i = 0; i < list.length; i++)
					list[i] = val;
			}
		}

		/// Sets this to the identity matrix.
		public void setToIdentity()
		{
			setAll(0.0);
			int m = Math.min(cols(), rows());
			for(int i = 0; i < m; i++)
				row(i)[i] = 1.0;
		}

		/// Throws an exception if that has a different number of columns than
		/// this, or if one of its columns has a different number of values.
		public void checkCompatibility(Matrix that)
		{
			int c = cols();
			if (that.cols() != c)
				throw new IllegalArgumentException("Matrices have different number of columns.");
			
			for (int i = 0; i < c; i++)
			{
				if (valueCount(i) != that.valueCount(i))
					throw new IllegalArgumentException("Column " + i + " has mis-matching number of values.");
			}
		}
	}
	
	static public class LayerTanh {
		public Matrix weights; // rows are inputs, cols are outputs
		public double[] bias;
		public double[] net;
		public double[] activation;
		public double[] error;


		LayerTanh(int inputs, int outputs) {
			weights = new Matrix();
			weights.setSize(inputs, outputs);
			bias = new double[outputs];
			net = new double[outputs];
			activation = new double[outputs];
			error = new double[outputs];
		}


		LayerTanh(LayerTanh that) {
			weights = new Matrix(that.weights);
			bias = Vec.copy(that.bias);
			net = Vec.copy(that.net);
			activation = Vec.copy(that.activation);
			error = Vec.copy(that.error);
		}


		void copy(LayerTanh src) {
			if(src.weights.rows() != weights.rows() || src.weights.cols() != weights.cols())
				throw new IllegalArgumentException("mismatching sizes");
			weights.setSize(src.weights.rows(), src.weights.cols());
			weights.copyBlock(0, 0, src.weights, 0, 0, src.weights.rows(), src.weights.cols());
			for(int i = 0; i < bias.length; i++) {
				bias[i] = src.bias[i];
			}
		}


		int inputCount() { return weights.rows(); }
		int outputCount() { return weights.cols(); }


		void initWeights(Random r) {
			double dev = Math.max(0.3, 1.0 / weights.rows());
			for(int i = 0; i < weights.rows(); i++) {
				double[] row = weights.row(i);
				for(int j = 0; j < weights.cols(); j++) {
					row[j] = dev * r.nextGaussian();
				}
			}
			for(int j = 0; j < weights.cols(); j++) {
				bias[j] = dev * r.nextGaussian();
			}
		}


		int countWeights() {
			return weights.rows() * weights.cols() + bias.length;
		}


		int setWeights(double[] w, int start) {
			int oldStart = start;
			for(int i = 0; i < bias.length; i++)
				bias[i] = w[start++];
			for(int i = 0; i < weights.rows(); i++)
			{
				double[] row = weights.row(i);
				for(int j = 0; j < weights.cols(); j++)
					row[j] = w[start++];
			}
			return start - oldStart;
		}


		void feedForward(double[] in) {
			if(in.length != weights.rows())
				throw new IllegalArgumentException("size mismatch. " + Integer.toString(in.length) + " != " + Integer.toString(weights.rows()));
			for(int i = 0; i < net.length; i++)
				net[i] = bias[i];
			for(int j = 0; j < weights.rows(); j++) {
				double v = in[j];
				double[] w = weights.row(j);
				for(int i = 0; i < weights.cols(); i++)
					net[i] += v * w[i];
			}
		}


		void feedForward2(double[] in1, double[] in2) {
			if(in1.length + in2.length != weights.rows())
				throw new IllegalArgumentException("size mismatch. " + Integer.toString(in1.length) + " + " + Integer.toString(in2.length) + " != " + Integer.toString(weights.rows()));
			for(int i = 0; i < net.length; i++)
				net[i] = bias[i];
			for(int j = 0; j < in1.length; j++) {
				double v = in1[j];
				double[] w = weights.row(j);
				for(int i = 0; i < weights.cols(); i++)
					net[i] += v * w[i];
			}
			for(int j = 0; j < in2.length; j++) {
				double v = in2[j];
				double[] w = weights.row(in1.length + j);
				for(int i = 0; i < weights.cols(); i++)
					net[i] += v * w[i];
			}
		}


		void activate() {
			for(int i = 0; i < net.length; i++) {
				activation[i] = Math.tanh(net[i]);
			}
		}


		void computeError(double[] target) {
			if(target.length != activation.length)
				throw new IllegalArgumentException("size mismatch. " + Integer.toString(target.length) + " != " + Integer.toString(activation.length));
			for(int i = 0; i < activation.length; i++) {
				if(target[i] < -1.0 || target[i] > 1.0)
					throw new IllegalArgumentException("target value out of range for the tanh activation function");
				error[i] = target[i] - activation[i];
			}
		}


		void deactivate() {
			for(int i = 0; i < error.length; i++) {
				error[i] *= (1.0 - activation[i] * activation[i]);
			}
		}


		void feedBack(double[] upstream) {
			if(upstream.length != weights.rows())
				throw new IllegalArgumentException("size mismatch");
			for(int j = 0; j < weights.rows(); j++) {
				double[] w = weights.row(j);
				double d = 0.0;
				for(int i = 0; i < weights.cols(); i++) {
					d += error[i] * w[i];
				}
				upstream[j] = d;
			}
		}


		void refineInputs(double[] inputs, double learningRate) {
			if(inputs.length != weights.rows())
				throw new IllegalArgumentException("size mismatch");
			for(int j = 0; j < weights.rows(); j++) {
				double[] w = weights.row(j);
				double d = 0.0;
				for(int i = 0; i < weights.cols(); i++) {
					d += error[i] * w[i];
				}
				inputs[j] += learningRate * d;
			}
		}


		void updateWeights(double[] in, double learningRate) {
			for(int i = 0; i < bias.length; i++) {
				bias[i] += learningRate * error[i];
			}
			for(int j = 0; j < weights.rows(); j++) {
				double[] w = weights.row(j);
				double x = learningRate * in[j];
				for(int i = 0; i < weights.cols(); i++) {
					w[i] += x * error[i];
				}
			}
		}

		// Applies both L2 and L1 regularization to the weights and bias values
		void regularizeWeights(double lambda) {
			for(int i = 0; i < weights.rows(); i++) {
				double[] row = weights.row(i);
				for(int j = 0; j < row.length; j++) {
					row[j] *= (1.0 - lambda);
					if(row[j] < 0.0)
						row[j] += lambda;
					else
						row[j] -= lambda;
				}
			}
			for(int j = 0; j < bias.length; j++) {
				bias[j] *= (1.0 - lambda);
				if(bias[j] < 0.0)
					bias[j] += lambda;
				else
					bias[j] -= lambda;
			}
		}
	}

	static public class NeuralNet {
		public ArrayList<LayerTanh> layers;
		/// General-purpose constructor. (Starts with no layers. You must add at least one.)
		NeuralNet() {
			layers = new ArrayList<LayerTanh>();
		}


		/// Copy constructor
		NeuralNet(NeuralNet that) {
			layers = new ArrayList<LayerTanh>();
			for(int i = 0; i < that.layers.size(); i++) {
				layers.add(new LayerTanh(that.layers.get(i)));
			}
		}


		/// Initializes the weights and biases with small random values
		void init(Random r) {
			for(int i = 0; i < layers.size(); i++) {
				layers.get(i).initWeights(r);
			}
		}


		/// Copies all the weights and biases from "that" into "this".
		/// (Assumes the corresponding topologies already match.)
		void copy(NeuralNet that) {
			if(layers.size() != that.layers.size())
				throw new IllegalArgumentException("Unexpected number of layers");
			for(int i = 0; i < layers.size(); i++) {
				layers.get(i).copy(that.layers.get(i));
			}
		}


		/// Feeds "in" into this neural network and propagates it forward to compute predicted outputs.
		double[] forwardProp(double[] in) {
			LayerTanh l = null;
			for(int i = 0; i < layers.size(); i++) {
				l = layers.get(i);
				l.feedForward(in);
				l.activate();
				in = l.activation;
			}
			return l.activation;
		}


		/// Feeds the concatenation of "in1" and "in2" into this neural network and propagates it forward to compute predicted outputs.
		double[] forwardProp2(double[] in1, double[] in2) {
			LayerTanh l = layers.get(0);
			l.feedForward2(in1, in2);
			l.activate();
			double[] in = l.activation;
			for(int i = 1; i < layers.size(); i++) {
				l = layers.get(i);
				l.feedForward(in);
				l.activate();
				in = l.activation;
			}
			return l.activation;
		}


		/// Backpropagates the error to the upstream layer.
		void backProp(double[] target) {
			int i = layers.size() - 1;
			LayerTanh l = layers.get(i);
			l.computeError(target);
			l.deactivate();
			for(i--; i >= 0; i--) {
				LayerTanh upstream = layers.get(i);
				l.feedBack(upstream.error);
				upstream.deactivate();
				l = upstream;
			}
		}


		/// Backpropagates the error from another neural network. (This is used when training autoencoders.)
		void backPropFromDecoder(NeuralNet decoder) {
			int i = layers.size() - 1;
			LayerTanh l = decoder.layers.get(0);
			LayerTanh upstream = layers.get(i);
			l.feedBack(upstream.error);
			l = upstream;
			//l.bendHinge(learningRate);
			l.deactivate();
			for(i--; i >= 0; i--) {
				upstream = layers.get(i);
				l.feedBack(upstream.error);
				//upstream.bendHinge(learningRate);
				upstream.deactivate();
				l = upstream;
			}
		}


		/// Updates the weights and biases
		void descendGradient(double[] in, double learningRate) {
			for(int i = 0; i < layers.size(); i++) {
				LayerTanh l = layers.get(i);
				l.updateWeights(in, learningRate);
				in = l.activation;
			}
		}


		/// Keeps the weights and biases from getting too big
		void regularize(double learningRate, double lambda) {
			double amount = learningRate * lambda;
			double smallerAmount = 0.1 * amount;
			for(int i = 0; i < layers.size(); i++) {
				LayerTanh lay = layers.get(i);
				//lay.straightenHinge(amount);
				lay.regularizeWeights(smallerAmount);
			}
		}


		/// Refines the weights and biases with on iteration of stochastic gradient descent.
		void trainIncremental(double[] in, double[] target, double learningRate) {
			forwardProp(in);
			backProp(target);
			//backPropAndBendHinge(target, learningRate);
			descendGradient(in, learningRate);
		}


		/// Refines "in" with one iteration of stochastic gradient descent.
		void refineInputs(double[] in, double[] target, double learningRate) {
			forwardProp(in);
			backProp(target);
			layers.get(0).refineInputs(in, learningRate);
		}


		static void testMath() {
			NeuralNet nn = new NeuralNet();
			LayerTanh l1 = new LayerTanh(2, 3);
			l1.weights.row(0)[0] = 0.1;
			l1.weights.row(0)[1] = 0.0;
			l1.weights.row(0)[2] = 0.1;
			l1.weights.row(1)[0] = 0.1;
			l1.weights.row(1)[1] = 0.0;
			l1.weights.row(1)[2] = -0.1;
			l1.bias[0] = 0.1;
			l1.bias[1] = 0.1;
			l1.bias[2] = 0.0;
			nn.layers.add(l1);

			LayerTanh l2 = new LayerTanh(3, 2);
			l2.weights.row(0)[0] = 0.1;
			l2.weights.row(0)[1] = 0.1;
			l2.weights.row(1)[0] = 0.1;
			l2.weights.row(1)[1] = 0.3;
			l2.weights.row(2)[0] = 0.1;
			l2.weights.row(2)[1] = -0.1;
			l2.bias[0] = 0.1;
			l2.bias[1] = -0.2;
			nn.layers.add(l2);

			System.out.println("l1 weights:" + l1.weights.toString());
			System.out.println("l1 bias:" + Vec.toString(l1.bias));
			System.out.println("l2 weights:" + l2.weights.toString());
			System.out.println("l2 bias:" + Vec.toString(l2.bias));

			System.out.println("----Forward prop");
			double in[] = new double[2];
			in[0] = 0.3;
			in[1] = -0.2;
			double[] out = nn.forwardProp(in);
			System.out.println("activation:" + Vec.toString(out));

			System.out.println("----Back prop");
			double targ[] = new double[2];
			targ[0] = 0.1;
			targ[1] = 0.0;
			nn.backProp(targ);
			System.out.println("error 2:" + Vec.toString(l2.error));
			System.out.println("error 1:" + Vec.toString(l1.error));
			
			nn.descendGradient(in, 0.1);
			System.out.println("----Descending gradient");
			System.out.println("l1 weights:" + l1.weights.toString());
			System.out.println("l1 bias:" + Vec.toString(l1.bias));
			System.out.println("l2 weights:" + l2.weights.toString());
			System.out.println("l2 bias:" + Vec.toString(l2.bias));

			if(Math.abs(l1.weights.row(0)[0] - 0.10039573704287) > 0.0000000001)
				throw new IllegalArgumentException("failed");
			if(Math.abs(l1.weights.row(0)[1] - 0.0013373814241446) > 0.0000000001)
				throw new IllegalArgumentException("failed");
			if(Math.abs(l1.bias[1] - 0.10445793808048) > 0.0000000001)
				throw new IllegalArgumentException("failed");
			System.out.println("passed");
		}

		public static void testVisual() throws Exception {
			// Make some data
			Random rand = new Random(1234);
			Matrix features = new Matrix();
			features.setSize(1000, 2);
			Matrix labels = new Matrix();
			labels.setSize(1000, 2);
			for(int i = 0; i < 1000; i++) {
				
				double x = rand.nextDouble() * 2 - 1;
				double y = rand.nextDouble() * 2 - 1;
				features.row(i)[0] = x;
				features.row(i)[1] = y;
				labels.row(i)[0] = (y < x * x ? 0.9 : 0.1);
				labels.row(i)[1] = (x < y * y ? 0.1 : 0.9);
			}

			// Train on it
			NeuralNet nn = new NeuralNet();
			nn.layers.add(new LayerTanh(2, 30));
			nn.layers.add(new LayerTanh(30, 2));
			nn.init(rand);
			int iters = 10000000;
			double learningRate = 0.01;
			double lambda = 0.0001;
			for(int i = 0; i < iters; i++) {
				int index = rand.nextInt(features.rows());
				nn.regularize(learningRate, lambda);
				nn.trainIncremental(features.row(index), labels.row(index), 0.01);
				if(i % 1000000 == 0)
					System.out.println(Double.toString(((double)i * 100)/ iters) + "%");
			}

			// Visualize it
			for(int i = 0; i < nn.layers.size(); i++) {
				System.out.print("Layer " + Integer.toString(i) + ": ");
//				for(int j = 0; j < nn.layers.get(i).hinge.length; j++)
//					System.out.print(Double.toString(nn.layers.get(i).hinge[j]) + ", ");
				System.out.println();
			}
			BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_ARGB);
			double[] in = new double[2];
			for(int y = 0; y < 100; y++) {
				for(int x = 0; x < 100; x++) {
					in[0] = ((double)x) / 100 * 2 - 1;
					in[1] = ((double)y) / 100 * 2 - 1;
					double[] out = nn.forwardProp(in);
					int g = Math.max(0, Math.min(255, (int)(out[0] * 256)));
					image.setRGB(x, y, new Color(g, g, g).getRGB());
					g = Math.max(0, Math.min(255, (int)(out[1] * 256)));
					image.setRGB(x, y + 100, new Color(g, g, g).getRGB());
				}
			}
			ImageIO.write(image, "png", new File("viz.png"));
		}
	}

	static class Game
	{

		static double[] evolveWeights()
		{
			// Create a random initial population
			Random r = new Random();
			Matrix population = new Matrix(100, 295);
			for(int i = 0; i < 100; i++)
			{
				double[] chromosome = population.row(i);
				for(int j = 0; j < 291; j++)
					chromosome[j] = 0.03 * r.nextGaussian();
				
				//add parameter mutation

				chromosome[291] = 200;
				chromosome[292] = 0.2;
				chromosome[293] = 5;
				chromosome[294] = 0;
				
				/*chromosome[291] = (double) r.nextInt(700); //mutation rate
				chromosome[292] = r.nextDouble() - 0.2; //mutation average
				chromosome[293] = (double) r.nextInt(10); //number of Tounraments
				chromosome[294] = (double) r.nextInt(40) + 60; //percentage to kill loser*/
				}


			// Evolve the population
			// todo: YOUR CODE WILL START HERE.
			//       Please write some code to evolve this population.
			//       (For tournament selection, you will need to call Controller.doBattleNoGui(agent1, agent2).)
			
			
			int numEvolutions = 0;
			int maxEvolutions = 700;
			
			
			while(numEvolutions < maxEvolutions){
			//Add mutation
			//int mutationCount = 0;
			int mutationRate; //1/mutation rate to be mutated
			double mutationAverage; 
			for(int i = 0; i < 100; i++)
			{
				double[] chromosome = population.row(i);
				mutationRate = (int) chromosome[291]; //Get rate
				mutationAverage = chromosome[292]; //Get average
				//System.out.println("rate: " + mutationRate + " avg: "+ mutationAverage);
				for(int j = 0; j < chromosome.length; j++)
					
					if(r.nextInt(mutationRate)==0){
						//Pick random chromosone value to mutate
						int mut = r.nextInt(291);
						double gaus = r.nextGaussian();
						chromosome[mut]+= mutationAverage * gaus;
					
					}
			}
			//Done adding mutations
			
			//Natural Selection
			
			//Choose pair of chromosones
			int r1 = r.nextInt(100); //Pick random tournaments
			double [] chromosome = population.row(r1);
			int numTournaments = (int) chromosome[293];
			int probToSurvive = (int) chromosome[294];
			
			for(int x = 0; x < numTournaments; x++){

				int cNum1 = r.nextInt(100); //First chromosome num
				int cNum2 = r.nextInt(100); //Second chromosome num
				double [] chromoTwo = population.row(cNum2);
				double [] chromoOne = population.row(cNum1);
				
				//If they aren't the same chromosome, continue to do battle! Also check if they aren't a dead chromo
				//I'm assuming the chances of 80 being zero are near to none
				if(cNum1 != cNum2 && chromoOne[0]!=0.0 && chromoTwo[0] !=0.0){
					
					int winner = 0;
					try {
						winner = Controller.doBattleNoGui(new NeuralAgent(getWeightsOnly(chromoOne)), new NeuralAgent(getWeightsOnly(chromoTwo)));
					} catch (Exception e) {
						e.printStackTrace();
					}

					if(winner == 1){
						for(int i = 0; i < chromoTwo.length; i++)
							population.row(cNum2)[i] = 0; //Kill the chromoOne

					}
					else if(winner == -1){
						for(int i = 0; i < chromoOne.length; i++)
							population.row(cNum1)[i] = 0; //Kill the chromoOne

					}
					
					
				}	
			}//End Natural Selection for loop
			
			//Replenish the population!
			
			int numCandidates = 5; //Not evolving this since it increases run time way way too much
			double difference = 0.05;
			for(int i = 0; i < 100; i++){
				
				//If its a dead chromo, make a baby!!
				if(population.row(i)[0] == 0.0){
					int parent1 = r.nextInt(100); //Pick first parent
					
					while(parent1==i || population.row(parent1)[0] ==0.0) //Make sure its not the same as the dead child
						parent1 = r.nextInt(100);
					
					int candidates[] = new int[numCandidates];
					int parent2 = 0;
					for(int x = 0; x < numCandidates; x++){
						parent2 = r.nextInt(100); //Pick second parent
						
						while(parent2 == parent1 || parent2 == i || population.row(parent2)[0] ==0.0) //Make sure its not the same as the dead child or the first parent
							parent2 = r.nextInt(100);
						candidates[x] = parent2;
					
					}
					
					//Find whos the most similiar
					double[] dad = population.row(parent1);
					int bestMom = 0;
					double parentDifference = 5000000; //We have hugely different parents
					double testDifference;
					for(int x = 0; x < numCandidates; x++){
						double[] testMom = population.row(candidates[x]);
						//System.out.println("Test mom: " + Arrays.toString(testMom));
						testDifference = 0;
						
						for(int c = 0; c < 291; c++){
							testDifference+= Math.pow((dad[c] - testMom[c]), 2);
						}
						if(testDifference < parentDifference && testMom[0]!=0.0){
							parentDifference = testDifference;
							bestMom = candidates[x];
							//System.out.println("Test diff: " + testDifference + " parentDiff: " + parentDifference + "bestMom: " + bestMom);
						}
							
					}//Done finding best parent
					
					//Lets mate!
					double[] mom = population.row(bestMom);
					for(int x = 0; x < dad.length; x++){
						int rand = r.nextInt(2);
						
						if(rand == 0){
							population.row(i)[x] = dad[x];
							
						}
						else{
							population.row(i)[x] = mom[x];
						}
					}

				}
			}

			numEvolutions++;
			System.out.println("Evolution number: " + numEvolutions);
		}//End of while



			// Return an arbitrary member from the population
			int num = r.nextInt(100);
			int count = 0;
			for(int x = 0; x < 100; x++){
				try {
					if(Controller.doBattleNoGui(new ReflexAgent(), new NeuralAgent(getWeightsOnly(population.row(x)))) == -1){
						count++;
						return population.row(x);
					
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} 
			System.out.println("won:" + count);
			return population.row(num);
		}

	public static double[] getWeightsOnly(double[] c){
		double[] newChromosome = new double[291];
		for(int x = 0; x < 291; x++)
			newChromosome[x] = c[x];
		
		return newChromosome;
		
	}
		
		public static void main(String[] args) throws Exception
		{
			double[] w = evolveWeights();
			
			//double[] w = {1.4278062179093252, -0.8414658067512626, -3.430878069485397, -0.5530266700798886, -3.7339007174276717, -0.8271989009580576, -0.6865147401628494, -0.03869713902799515, -1.3806921654326125, 2.82242913120967, -0.9881675331449635, 2.885837730211544, -0.0058013211444239874, 5.078255543446284, 0.6267748150693737, 0.3285590569693366, 0.41050498765657784, -1.549613381323693, 0.8528682384816878, 1.5051028086458151, 1.3172907657501758, 3.6674043717425033, 1.054308872084251, -0.529803625255931, 1.664950175309028, 0.9658747920350554, -2.4344823539549334, -9.357918959529005E-4, 0.8590548427108929, 4.120906037644227, 3.1953840029126934, -0.3236926915185312, -0.049708527105420605, -0.016176689668226236, 0.021817964639907036, -1.1258030042411382, -0.8954386237409302, 0.5928425599268046, 2.3833773527779325, -0.5989372285582482, -0.004129206161506073, -4.22280302877334, 5.297672135645984, 4.017972623405293, -0.4014925640692525, 1.092304142746835, -2.1014456880214643, -4.125808036084513, -1.3910958126453326, 0.028562028878741905, -0.7648797695333228, 1.0715405393042952, -0.0429649162944304, -2.176979410916771, 0.05507403125424082, 2.0259868860649726, -0.2864410946819339, 0.029464799622009485, -0.403798268948381, -0.9286469835858872, 3.875835995216954, 0.007703429456867757, 1.1812341244413471, -1.6781289306429101, 0.40544420609098647, 0.011914996502699482, 0.021579065503858817, 0.0491275121392664, 0.5424253793028335, 1.9754773593778199, -0.02078035487788186, 0.015383935091437186, -1.4874290944857007, 1.3880191948104526, 1.007914221154071, -0.5201078416930786, 3.3765110039824204, 3.959113593419813, 4.056673290182691, -0.009469597020618042, -0.0933025624075741, -1.598519894622842, -1.1436546396602116, -0.006129120144730517, 5.592594219013105, -4.6849991047450485, -1.124178828890901, 2.2275096860375094, -1.6243912328608245, -3.9708823480901754, 3.2982308161336245, 1.5384100044229219, 0.016149544806168953, 5.041060176196333, 0.5247048918729021, 0.019421031039903305, -0.011254208650366193, -0.9835093475410646, -1.5317197265844957, 0.7231820684485295, -0.8145676059904946, 3.717669267156177, 1.6103793232106463, -1.779494608189427, -0.6725595721042474, 0.02147878414835494, -1.070809631361301, 1.4446747254561327, -0.019457866147553533, 2.9520334975666334, 2.024499075581172, 4.623912638110536, -1.0504686371604346, -4.935949983133007, -0.764917202541169, 0.07128896421003739, 0.4211672809920537, -2.3610975402664494, 0.025083410176054553, 0.003976425364538126, -1.7423205655134477, 1.2360226141222597, -1.962302423486924, -3.791955846773207, 2.618055038059088, -0.02342326736431585, 0.23804179443330864, -0.852531364957433, -0.532158255611194, 0.7338112737144074, -2.7568016933824624, 0.11302818942249883, -1.639792134742708, -0.22292573013783096, 3.406555614516079, 0.02639693309936592, 0.01407045216090671, 0.09423953747871322, 1.7741747695506496, 3.164962304805893, -0.28845922294544607, -1.6175010200088258, 1.7834869758205496, -0.00813436333100356, 0.029513035928942038, -4.717474335743921, -2.015642635344229, -1.1303957576011054, -0.0028959321275578917, 4.17797746491587, -0.013991496056339772, 1.3236739894447955, -2.1922142793992796, -0.7460959542608455, -0.10155408104496266, 0.8625560623167834, -2.316721317652577, -0.13993092809836205, -0.5085709506834823, -0.03242087818062531, -0.9138268523151487, -3.89869660176981, 3.7931705471324983, 0.7736022277058662, 0.045822559177341696, 1.153237736923841, -0.008634474022097069, -1.4372275698692605, 2.214895461804625, -1.5511884499540214, 0.0163081092147714, -0.3403174875022318, -0.018104340692746017, 0.045645555302397445, 0.3764180447861064, 1.0962117378834024, 1.5923730681601274, 3.1350556302921273, -1.6618135498067717, -3.609688679707067, -0.09851510791778564, -0.3260429425978005, 1.937210534588009, -3.402613721027617, 3.049417489110165, -2.302558246987306, -0.01606415400405845, 0.016630573285535653, 2.0275617069557716, -0.1807913665991987, -2.669002651733684, -1.5943492408889088, -2.67770818716589, 4.412455057447899, 2.1053550720458456, 1.8147547387464311, -3.855896728175755, -3.6722364635677067, -5.637348516455976, 0.8136094618750858, -0.03851645764863586, 2.0230061558308496, -0.8702016777480988, 1.868691488197543, -0.053796887700301006, -0.34745855696880223, -2.889428337117918, 0.017205831163966467, -5.1525890309288345, -0.13878439223017536, 2.0584945575667546, 1.48127430955818, 0.01121509363621666, 3.5873669309295932, 0.2431834988947903, -0.3132692394180031, 1.36879731291786, -2.0964148366865203, 2.45386013444738, 0.5523809653918539, 2.4449124533412188, 0.2519806346682446, 2.934664411525798, -1.1852024118998112, -4.48099658580024, -5.33616953889075, 0.758488600688501, 0.5175965834025389, -0.04848671135473764, -0.0726669380250318, -0.19871125730853967, 0.1377847849853322, -3.2004178273825032, -2.2102497753239585, -1.4135307645319994, 0.18020547640834006, -3.8482332453816643, 0.016170052250339816, -5.815331984171178, -0.3615651042554027, 2.1567636077013, 2.6039426640914036, 0.9959982656521289, 1.2165487980096932, -1.6688478983228394, -1.2352901038641082, 3.2143753302914173, -0.02525381902687531, 1.2829504796034583, 0.020882863882066827, 0.03126242395379702, 7.2025004525595495, -1.8531660452731549, 0.3123891975698835, 0.16169590637998957, 0.9293827795934003, 1.101791356818349, -2.657041572747084, 3.0766549926745093, 0.052816004239998084, 2.2535452559191618, 0.5117581127272232, 1.7825955522273098, -4.113031843050609, -0.3498172503931128, -1.4688725052381, 1.1544170955281734, 0.024996572018149633, 0.25895707143876723, -1.9461471562670027, 6.010018946578572, 0.014077317822364122, 4.907995564681843, 11.765932414279082, 0.47994659790790917, -0.04921634691221467, 0.028029631658977806, 0.004936227342838463, -0.461863575951299, 0.042035954906056196, -0.049688252529510035, 0.8284372033716509, 7.582748017159507E-4, -2.793977868818394, -0.6961662395060646, 0.657882445756073, -0.21293861999664077, 3.0854382705900787, -3.4600899283586206, -0.07231118201456643, -3.424624000289568};
			//		System.out.println(Arrays.toString(w) + "\n" + "\n");
			//System.out.println(Controller.doBattleNoGui(new SittingDuck(), new NeuralAgent(getWeightsOnly(w)))); //Looking for -1
			//Controller.doBattle(new SittingDuck(), new NeuralAgent(w));
			
			//[1.4278062179093252, -0.8414658067512626, -3.430878069485397, -0.5530266700798886, -3.7339007174276717, -0.8271989009580576, -0.6865147401628494, -0.03869713902799515, -1.3806921654326125, 2.82242913120967, -0.9881675331449635, 2.885837730211544, -0.0058013211444239874, 5.078255543446284, 0.6267748150693737, 0.3285590569693366, 0.41050498765657784, -1.549613381323693, 0.8528682384816878, 1.5051028086458151, 1.3172907657501758, 3.6674043717425033, 1.054308872084251, -0.529803625255931, 1.664950175309028, 0.9658747920350554, -2.4344823539549334, -9.357918959529005E-4, 0.8590548427108929, 4.120906037644227, 3.1953840029126934, -0.3236926915185312, -0.049708527105420605, -0.016176689668226236, 0.021817964639907036, -1.1258030042411382, -0.8954386237409302, 0.5928425599268046, 2.3833773527779325, -0.5989372285582482, -0.004129206161506073, -4.22280302877334, 5.297672135645984, 4.017972623405293, -0.4014925640692525, 1.092304142746835, -2.1014456880214643, -4.125808036084513, -1.3910958126453326, 0.028562028878741905, -0.7648797695333228, 1.0715405393042952, -0.0429649162944304, -2.176979410916771, 0.05507403125424082, 2.0259868860649726, -0.2864410946819339, 0.029464799622009485, -0.403798268948381, -0.9286469835858872, 3.875835995216954, 0.007703429456867757, 1.1812341244413471, -1.6781289306429101, 0.40544420609098647, 0.011914996502699482, 0.021579065503858817, 0.0491275121392664, 0.5424253793028335, 1.9754773593778199, -0.02078035487788186, 0.015383935091437186, -1.4874290944857007, 1.3880191948104526, 1.007914221154071, -0.5201078416930786, 3.3765110039824204, 3.959113593419813, 4.056673290182691, -0.009469597020618042, -0.0933025624075741, -1.598519894622842, -1.1436546396602116, -0.006129120144730517, 5.592594219013105, -4.6849991047450485, -1.124178828890901, 2.2275096860375094, -1.6243912328608245, -3.9708823480901754, 3.2982308161336245, 1.5384100044229219, 0.016149544806168953, 5.041060176196333, 0.5247048918729021, 0.019421031039903305, -0.011254208650366193, -0.9835093475410646, -1.5317197265844957, 0.7231820684485295, -0.8145676059904946, 3.717669267156177, 1.6103793232106463, -1.779494608189427, -0.6725595721042474, 0.02147878414835494, -1.070809631361301, 1.4446747254561327, -0.019457866147553533, 2.9520334975666334, 2.024499075581172, 4.623912638110536, -1.0504686371604346, -4.935949983133007, -0.764917202541169, 0.07128896421003739, 0.4211672809920537, -2.3610975402664494, 0.025083410176054553, 0.003976425364538126, -1.7423205655134477, 1.2360226141222597, -1.962302423486924, -3.791955846773207, 2.618055038059088, -0.02342326736431585, 0.23804179443330864, -0.852531364957433, -0.532158255611194, 0.7338112737144074, -2.7568016933824624, 0.11302818942249883, -1.639792134742708, -0.22292573013783096, 3.406555614516079, 0.02639693309936592, 0.01407045216090671, 0.09423953747871322, 1.7741747695506496, 3.164962304805893, -0.28845922294544607, -1.6175010200088258, 1.7834869758205496, -0.00813436333100356, 0.029513035928942038, -4.717474335743921, -2.015642635344229, -1.1303957576011054, -0.0028959321275578917, 4.17797746491587, -0.013991496056339772, 1.3236739894447955, -2.1922142793992796, -0.7460959542608455, -0.10155408104496266, 0.8625560623167834, -2.316721317652577, -0.13993092809836205, -0.5085709506834823, -0.03242087818062531, -0.9138268523151487, -3.89869660176981, 3.7931705471324983, 0.7736022277058662, 0.045822559177341696, 1.153237736923841, -0.008634474022097069, -1.4372275698692605, 2.214895461804625, -1.5511884499540214, 0.0163081092147714, -0.3403174875022318, -0.018104340692746017, 0.045645555302397445, 0.3764180447861064, 1.0962117378834024, 1.5923730681601274, 3.1350556302921273, -1.6618135498067717, -3.609688679707067, -0.09851510791778564, -0.3260429425978005, 1.937210534588009, -3.402613721027617, 3.049417489110165, -2.302558246987306, -0.01606415400405845, 0.016630573285535653, 2.0275617069557716, -0.1807913665991987, -2.669002651733684, -1.5943492408889088, -2.67770818716589, 4.412455057447899, 2.1053550720458456, 1.8147547387464311, -3.855896728175755, -3.6722364635677067, -5.637348516455976, 0.8136094618750858, -0.03851645764863586, 2.0230061558308496, -0.8702016777480988, 1.868691488197543, -0.053796887700301006, -0.34745855696880223, -2.889428337117918, 0.017205831163966467, -5.1525890309288345, -0.13878439223017536, 2.0584945575667546, 1.48127430955818, 0.01121509363621666, 3.5873669309295932, 0.2431834988947903, -0.3132692394180031, 1.36879731291786, -2.0964148366865203, 2.45386013444738, 0.5523809653918539, 2.4449124533412188, 0.2519806346682446, 2.934664411525798, -1.1852024118998112, -4.48099658580024, -5.33616953889075, 0.758488600688501, 0.5175965834025389, -0.04848671135473764, -0.0726669380250318, -0.19871125730853967, 0.1377847849853322, -3.2004178273825032, -2.2102497753239585, -1.4135307645319994, 0.18020547640834006, -3.8482332453816643, 0.016170052250339816, -5.815331984171178, -0.3615651042554027, 2.1567636077013, 2.6039426640914036, 0.9959982656521289, 1.2165487980096932, -1.6688478983228394, -1.2352901038641082, 3.2143753302914173, -0.02525381902687531, 1.2829504796034583, 0.020882863882066827, 0.03126242395379702, 7.2025004525595495, -1.8531660452731549, 0.3123891975698835, 0.16169590637998957, 0.9293827795934003, 1.101791356818349, -2.657041572747084, 3.0766549926745093, 0.052816004239998084, 2.2535452559191618, 0.5117581127272232, 1.7825955522273098, -4.113031843050609, -0.3498172503931128, -1.4688725052381, 1.1544170955281734, 0.024996572018149633, 0.25895707143876723, -1.9461471562670027, 6.010018946578572, 0.014077317822364122, 4.907995564681843, 11.765932414279082, 0.47994659790790917, -0.04921634691221467, 0.028029631658977806, 0.004936227342838463, -0.461863575951299, 0.042035954906056196, -0.049688252529510035, 0.8284372033716509, 7.582748017159507E-4, -2.793977868818394, -0.6961662395060646, 0.657882445756073, -0.21293861999664077, 3.0854382705900787, -3.4600899283586206, -0.07231118201456643, -3.424624000289568]
			//[-0.04202596109304965, -1.19274463259643, -0.8201516905362104, 2.0925533821287563, 1.8866004394655507, -0.3694257972568298, 0.15213484344281208, 1.1937982202438422, 0.022851234224101746, 0.870893635450261, 0.33641360340607707, 1.2899167272021996, 0.596466056745895, 1.385276765808045, 0.4897397172739073, 1.4814854219579505, 0.30612303635559646, 0.8272949507757844, 1.6554977382075666, 0.17036265053191202, -0.7933144174752881, -1.5502865889137434, 0.7741752662422746, -1.135859905077241, -0.008630748308777922, 1.3711690419307099, -0.010275812558147714, -0.647789417466728, -0.8394520570715265, 0.02559400088172995, 0.21024801720041666, -0.02729888265961223, -1.4098153996999943, -0.39027438370961587, -0.8153993800519114, 0.8882692583729558, 0.08775098296175288, -0.1595300479454702, 0.026254954889622862, -0.1889735565080679, -1.5010127387445058, 0.2515950055896642, 0.030552374337986576, 1.068584026845925, 0.5971865308830273, 0.0017069257691517078, -0.7977528254571721, -0.0034317153798423816, -0.48422334897329067, -0.6741011006685692, -0.28565327570223853, 0.42515131820132007, 0.4897759443148575, 0.057126847677891, -1.024609548751705, -0.0305676883234153, -0.012052663899796373, -0.352406956241039, 0.024674378611621358, -0.007753584423737005, 1.6803076733009519, -0.023250561539306065, 1.0870183106784876, -4.521279038549622, 1.9565870422370393, 0.011150560982637121, -0.1439794747501965, -0.15157565822210634, -0.6225559294273161, -2.6642391845339444, -0.9468016344457489, 0.5941483085509636, 0.5711703690676627, 0.10379925124604919, -0.0043135070066996975, 0.18396120307749741, -0.326810201829818, -1.4783045092510638, 0.4097368605394314, 0.05556624264490168, -0.3809494416761281, 1.9110265329566791, 1.0174685873947618, -0.4556200218680041, 0.5697266523503666, 1.1345185940452533, 0.6148365081641883, 0.8673769321389073, -0.7960891902552296, 0.3047492201670916, 0.03224634855905304, 0.08479020360598195, -0.01403618869097337, 2.391556115578495, 0.00207548540179155, 1.292064935990143, -0.5854991169678215, 0.016538148466026904, -1.161842126459817, -0.4996701176276368, -0.497261644758318, 0.5933273097148465, -0.24076528315515583, 1.0903842016883323, -0.01545626470898227, -0.007659204006921296, 0.40749920239169396, 0.7858128960501314, 0.03049605765123128, -0.1568853896823931, -0.04103916553160806, -0.6486122507075572, -0.033102699095138875, -0.14913152284913267, -0.053683834029854265, 1.5264026369986585, -0.5679596389015433, -0.0021064784414278536, -0.3845742171411388, -0.13133708625802817, -0.3536184978357301, 0.7466658754472897, -0.0012936954344933693, -1.317894927330312, 0.012814691305882928, 0.04059738653243608, 0.02794822263860571, -0.8020379973391674, 1.6242580135309133, -0.012584517648648059, -0.03297079255203283, 0.10876294273392201, -0.017449154483678397, 0.7969164741989385, -0.21226752503340468, -0.7390012387787176, 1.0298822940919299, 0.42955020465678395, -0.6660958752143495, 0.05073784899810985, 0.05810965662121376, 0.03695264255232812, -0.005725596299092663, 0.9760292477102983, -1.4546275993341526, -0.28723817012617614, 0.29444022501701017, 1.2702810785745795, 0.042864089122547834, 0.7598109196435316, 0.015257291195112162, -0.15136355371733518, -0.23019813783895754, -0.8935054279558976, 0.788521238062045, 0.2296042840543437, 0.6607941915529538, -0.2095877538414216, -0.7832968401304874, -0.011729714632165888, 0.01253726764837414, -1.814919983193163, -0.06385543597595555, -1.1862580349264527, 1.3148739303291193, -1.4585324672363311, 0.24880080220518241, -0.39262213106121174, -0.47322241536134135, 0.03299724834064486, 0.04213375784469808, 0.7067443495254221, -1.682580247259741, 0.0031953854596109346, 0.39805101111012203, 0.30513507554899655, 1.1279401428893474, -0.7721975812723363, -0.7913548443856214, -0.347086104225803, 0.4919858392877484, 0.9734966171119936, -1.5787335412384556, -0.40053210612896145, 0.7724154226597199, 0.09307637404705893, -1.4698975947087587, 2.1741723867811054, 1.236368221338807, -0.019118338704325274, -0.01473669948054731, -1.2477533562391, -0.5809125690778556, -0.6114515879745547, -0.7271941646008955, -0.047952451787726946, -0.22920824513050905, 1.1534182992230375, -0.00775621835808042, 1.3313434248973444, -0.883863863328346, 0.2999002744858053, -0.21813003538210377, -0.7331912954946018, 0.05573082720714251, -0.897483544893537, -0.37667939368671954, -0.03696570824482757, -0.21334111631438846, -0.32872665814860413, -0.5057492961467962, 3.8997503606966903, 0.17457937186155353, 0.7525341741745251, -0.007263922026518036, -0.06927177036225414, -0.018488058496954868, 1.100255140356369, 0.6977439648224287, -0.3393655609068078, -1.3225171117110164, -0.7938062898867284, 0.641860675639315, 1.2578400324784789, -0.2647781992165795, -0.0012854953102882762, -0.7557484024754053, -0.6415107858279814, -3.2879240571554615, 0.6634971995288793, -0.012224587632246186, 0.3793830681080449, -0.7378104009555263, -1.9284270207012177, 0.3670118097225893, 0.11079173636731937, 0.02875946741325313, -1.4842163024559993, -0.3887185592261491, -0.9839331062559209, -1.1222919962640796, 0.03665529119083636, 0.30727012894561595, -1.1591794825652013, -1.1478077227275694, 0.006958755370254929, -0.21790308622240379, 0.4964601239547349, -1.166735333031679, -0.8698378431576556, -2.845904131626832, -1.0278537146385274, 0.06598299317756896, 0.681994610647244, -0.3122574537639122, 1.2223366075011493, -0.3556515602540341, -0.15793841752725743, -1.5876760624867734, -0.7733167162446448, 4.1522662783376925, -0.02527083676765564, -0.0440620026246377, 0.9299628592658309, 0.02661343942020661, 0.1917522873756768, 0.8010697651355743, -0.4776744065195511, 0.2833773599866234, -0.6574646871543612, -0.04523021384506874, 0.9475845892444101, -2.267993590758275, -1.0672899310986828, 0.3815793032646689, -0.23010267287871983, -0.335109605947336, 0.020672178255933565, -1.7413650301575494, 0.06317539371749843, -0.05385887512384716, 0.9257359921135485, 0.6803717824186543, 0.26066687471934546, 0.36588058466388884, -1.016507647210125, -1.467333272092833, -0.013836952036398332, 0.5691096752893035, 0.9288640324288269, 0.5850175915148472]

			
		}

	}

}
