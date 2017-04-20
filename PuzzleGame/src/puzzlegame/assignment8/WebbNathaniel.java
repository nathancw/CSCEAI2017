package puzzlegame.assignment8;
import java.util.Random;
import java.util.Scanner;
import java.util.ArrayList;
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

	
	
	
	
}
