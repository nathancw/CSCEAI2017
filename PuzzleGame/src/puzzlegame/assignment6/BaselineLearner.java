package puzzlegame.assignment6;
class BaselineLearner extends SupervisedLearner
{
	double[] mode;

	String name()
	{
		return "Baseline";
	}

	void train(Matrix features, Matrix labels)
	{
		mode = new double[labels.cols()];
		for(int i = 0; i < labels.cols(); i++)
		{
			if(labels.valueCount(i) == 0)
				mode[i] = labels.columnMean(i);
			else
				mode[i] = labels.mostCommonValue(i);
		}
	}

	void predict(double[] in, double[] out)
	{
		Vec.copy(out, mode);
	}
}
