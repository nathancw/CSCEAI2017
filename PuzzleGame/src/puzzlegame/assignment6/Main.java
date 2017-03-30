package puzzlegame.assignment6;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

class Main
{
	static void test(SupervisedLearner learner, String challenge)
	{
		// Load the training data
		String fn = "data/" + challenge;
		Matrix trainFeatures = new Matrix();
		trainFeatures.loadARFF(fn + "_train_feat.arff");
		Matrix trainLabels = new Matrix();
		trainLabels.loadARFF(fn + "_train_lab.arff");

		// Train the model
		learner.train(trainFeatures, trainLabels);

		// Load the test data
		Matrix testFeatures = new Matrix();
		testFeatures.loadARFF(fn + "_test_feat.arff");
		Matrix testLabels = new Matrix();
		testLabels.loadARFF(fn + "_test_lab.arff");

		// Measure and report accuracy
		int misclassifications = learner.countMisclassifications(testFeatures, testLabels);
		System.out.println("Misclassifications by " + learner.name() + " at " + challenge + " = " + Integer.toString(misclassifications) + "/" + Integer.toString(testFeatures.rows()));
	}

	public static void testLearner(SupervisedLearner learner)
	{
		test(learner, "hep");
		test(learner, "vow");
		test(learner, "soy");
	}

	public static void main(String[] args)
	{
		Random rand = new Random();
		testLearner(new BaselineLearner());
		testLearner(new DecisionTree(rand));
		testLearner(new RandomForest(5, rand));
	}
}

abstract class Node
{
	abstract boolean isLeaf();
}

class InteriorNode extends Node
{
	int attribute; // which attribute to divide on
	double pivot; // which value to divide on
	Node a;
	Node b;

	boolean isLeaf() { return false; }
}

class LeafNode extends Node
{
	double[] label;

	boolean isLeaf() { return true; }
}

class DecisionTree extends SupervisedLearner
{
	Node root;
	
	Random rand;
	
	public DecisionTree(Random r){
		this.rand = r;
	}
	
	@Override
	String name() {
		
		return "DecisionTree";
	}
	
	Node buildTree(Matrix features, Matrix labels){
		
		//If we have no more rows left
		if(features.rows() <= 1 ){
			LeafNode leaf = new LeafNode();
			leaf.label = computeLabel(labels);
			//System.out.println(" Leaf Label:" + Arrays.toString(leaf.label));
			return leaf;
			
		}
		
		
		Matrix af = new Matrix();
		Matrix bf = new Matrix();

		Matrix al = new Matrix();
		Matrix bl = new Matrix();
		//System.out.println("\nRow size: " + features.rows()  + " features cols: " + features.cols());
		
		InteriorNode n = new InteriorNode();
		int splitCol = 0;
		double splitVal = 0.0;
		int splitA = 0;
		int splitB = 0;
		boolean splitAgain = true;
		
		while(splitAgain){
			
			splitAgain = false;
			splitA = 0;
			splitB = 0;
			splitCol = rand.nextInt(features.cols());
			int randRow = rand.nextInt(features.rows());
			splitVal = features.row(randRow)[splitCol]; //splits on that column for that random row
		
			af.copyMetaData(features);
			bf.copyMetaData(features);
			
			al.copyMetaData(labels);
			bl.copyMetaData(labels);
			//System.out.println("SplitCol: " + splitCol + " Splittng on : " + splitVal);
			boolean categorical = false;
			if(features.valueCount(splitCol) == 0)
				categorical = true;
		
			for(int i = 0; i < features.rows(); i++){
			
				
				//System.out.println("val: " + features.valueCount((int)features.row(i)[splitCol]));
				//NOTE THIS SPLITTING IS ONLY FOR CONTINUOUS DATA. We need to change to categorical data.
				//System.out.println("Features.row(" + i + ")[" + splitCol + "]: " + features.row(i)[splitCol] + " < " + splitVal);
				
				if(categorical){
					if(features.row(i)[splitCol] == splitVal){ //if the current feature row is less than the split value, we need to split it
						Vec.copy(af.newRow(), features.row(i));
						Vec.copy(al.newRow(), labels.row(i));
						splitA++;
					}
					else
					{
						Vec.copy(bf.newRow(), features.row(i));
						Vec.copy(bl.newRow(), labels.row(i));
						splitB++;
					}
				}
				else{
					if(features.row(i)[splitCol] < splitVal){ //if the current feature row is less than the split value, we need to split it
						Vec.copy(af.newRow(), features.row(i));
						Vec.copy(al.newRow(), labels.row(i));
						splitA++;
					}
					else
					{
						Vec.copy(bf.newRow(), features.row(i));
						Vec.copy(bl.newRow(), labels.row(i));
						splitB++;
					}
				}
		
			}
			
			if(splitA == 0 && splitB > ((splitA+1)*2))
				splitAgain = true;
			
			if(splitB == 0 && splitA > ((splitB+1)*2))
				splitAgain = true;
			
			if(splitA * 2 < splitB)
				splitAgain = true;
			
			if(splitB * 2 < splitA)
				splitAgain = true;
			
		} //End while
		
		//System.out.println("SplitA: " + splitA + " splitB: " + splitB);
		
		//System.out.println("n.attribute: " + splitCol + " n.pivot: " + splitVal);
		//Store the values in a new node
		n.attribute = splitCol;
	    n.pivot = splitVal;
	    n.a = buildTree(af,al);
	    n.b = buildTree(bf,bl);
		
		return n;
	}
	
	double[] computeLabel(Matrix labels){
		
		double[] mode = new double[labels.cols()];
		for(int i = 0; i < labels.cols(); i++)
		{
			if(labels.valueCount(i) == 0)
				mode[i] = labels.columnMean(i);
			else
				mode[i] = labels.mostCommonValue(i);
		}
		return mode;
	}
	
	
	@Override
	void train(Matrix features, Matrix labels) {
		
		root = buildTree(features, labels);
	}

	@Override
	void predict(double[] in, double[] out) {
		
	//	System.out.println("Predicting. In: " + Arrays.toString(in) + " out: " + Arrays.toString(out));
		//InteriorNode n = (InteriorNode) root;
		Node n = root;
		while (!n.isLeaf()) {
			if (in[((InteriorNode) n).attribute] < ((InteriorNode) n).pivot)
			n = ((InteriorNode) n).a;
		else
			n = ((InteriorNode) n).b; 
		}
		LeafNode leafNode = (LeafNode) n;
		Vec.copy(out,leafNode.label);  
	}

} //End decisionTree

class RandomForest extends SupervisedLearner
{
	int n;
	DecisionTree tree[];
	
	RandomForest(int n, Random rand){
		this.n = n;
		tree = new DecisionTree[n];
		
		for(int x = 0; x < n; x++)
			tree[x] = new DecisionTree(rand);
		
	}
	
	@Override
	String name() {
		return "Random Forest";
	}

	@Override
	void train(Matrix features, Matrix labels) {
		//Create and train n decision trees
		
		for(int x = 0; x < n; x++){
			tree[x].train(features, labels);
		}
		
	}

	@Override
	void predict(double[] in, double[] out) {
		
		ArrayList<double[]> labels = new ArrayList<double[]>();
		//We predict one value
		//double[] temp = new double[1];
		
		for(int x = 0; x < n; x++){
			tree[x].predict(in,out);
			labels.add(out);
		}
		
		for(int x = 0; x < labels.size(); x++){
			//System.out.println("Predicted label for tree["+x+"]: " + Arrays.toString(labels.get(x)));
		}
		double[] num = computeBestLabel(labels);
		
		Vec.copy(out,num);
		
	}

	private double[] computeBestLabel(ArrayList<double[]> labels) {
		
		int count = 1, tempCount;
		double[] popular = labels.get(0);
		double[] temp;
		  for (int i = 0; i < labels.size(); i++)
		  {
		    temp = labels.get(i);
		    tempCount = 0;
		    for (int j = 1; j < labels.size(); j++)
		    {
		      if (temp == labels.get(j))
		        tempCount++;
		    }
		    if (tempCount > count)
		    {
		      popular = temp;
		      count = tempCount;
		    }
		  }
		  
		//  System.out.println("Most popular: " + Arrays.toString(popular));
		  return popular;
		
	}
	
	
}
