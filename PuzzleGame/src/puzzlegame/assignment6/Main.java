package puzzlegame.assignment6;

import java.util.Arrays;
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
		//test(learner, "soy");
	}

	public static void main(String[] args)
	{
		//testLearner(new BaselineLearner());
		testLearner(new DecisionTree());
		//testLearner(new RandomForest(50));
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
	Random rand = new Random();
	
	@Override
	String name() {
		
		return "DecisionTree";
	}
	
	Node buildTree(Matrix features, Matrix labels){
		
		//If we have no more rows left
		if(features.rows() <= 1 || features.cols() <= 1){
			LeafNode leaf = new LeafNode();
			leaf.label = computeLabel(labels);
			return leaf;
			
		}
		
	
		//System.out.println("\nRow size: " + features.rows()  + " features cols: " + features.cols());
		
		InteriorNode n = new InteriorNode();
		
		int splitCol = rand.nextInt(features.cols());
		int randRow = rand.nextInt(features.rows());
		double splitVal = features.row(randRow)[splitCol]; //splits on that column for that random row
		
		//System.out.println("SplitCol: " + splitCol + " Splittng on : " + splitVal);
		
		Matrix af = new Matrix();
		af.copyMetaData(features);
		Matrix bf = new Matrix();
		bf.copyMetaData(features);
		
		Matrix al = new Matrix();
		al.copyMetaData(labels);
		Matrix bl = new Matrix();
		bl.copyMetaData(labels);
		
		for(int i = 0; i < features.rows(); i++){
			
			System.out.println("val: " + features.valueCount((int)features.row(i)[splitCol]));
			//NOTE THIS SPLITTING IS ONLY FOR CONTINUOUS DATA. We need to change to categorical data.
			//System.out.println("Features.row(" + i + ")[" + splitCol + "]: " + features.row(i)[splitCol] + " < " + splitVal);
			if(features.row(i)[splitCol] < splitVal){ //if the current feature row is less than the split value, we need to split it
				Vec.copy(af.newRow(), features.row(i));
				Vec.copy(al.newRow(), labels.row(i));
			}
			else
			{
				Vec.copy(bf.newRow(), features.row(i));
				Vec.copy(bl.newRow(), labels.row(i));
			}
	
		}
		
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
		InteriorNode n = (InteriorNode) root;
		
		while(!n.isLeaf()){
						
				if(in[n.attribute] < n.pivot){
					//Check if its leaf node before we move...
					if(n.a.isLeaf()){
						LeafNode leaf = (LeafNode) n.a;
						Vec.copy(out,leaf.label);
						break;
					}
					else
						n = (InteriorNode) n.a;
				}
				else{
					
					if(n.b.isLeaf()){
						LeafNode leaf = (LeafNode) n.b;
						Vec.copy(out,leaf.label);
						break;
					}
					else
						n = (InteriorNode) n.b;
				}
				//System.out.println("Parent: " + n.attribute);
				//n = (InteriorNode) n.b;
				//System.out.println(" a: " + n.attribute);
				
		}
		

		
		//System.out.println("Found leaf.");
		
	//	Vec.copy(out, mode);
		
	}

}

class RandomForest extends SupervisedLearner
{
	int n;
	DecisionTree tree[];
	RandomForest(int n){
		this.n = n;
	}
	
	@Override
	String name() {
		return "Random Forest";
	}

	@Override
	void train(Matrix features, Matrix labels) {
		//Create and train n decision trees
		tree = new DecisionTree[n];
		
		for(int x = 0; x < n; x++){
			tree[x].train(features, labels);
		}
		
	}

	@Override
	void predict(double[] in, double[] out) {
		//HOW DO I PREDICT? THIS GETS CALLED FROM learner.countmissclassifications(...) like a hundred times.
		
	}
	
	
	
	
}
