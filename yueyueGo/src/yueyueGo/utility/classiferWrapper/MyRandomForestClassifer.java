package yueyueGo.utility.classiferWrapper;

import weka.classifiers.trees.RandomForest;

/*
 * 为了能设置单一分类树的Minimum number of instances for leaf. 改写原有RandomForest 
 */
@Deprecated
public class MyRandomForestClassifer extends RandomForest {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7177987329864919124L;
	/** Minimum number of instances for leaf. */
	protected double m_MinNum = 100.0;

//	@Override
//	public void buildClassifier(Instances data) throws Exception {
//
//		// can classifier handle the data?
//		getCapabilities().testWithFail(data);
//
//		// remove instances with missing class
//		data = new Instances(data);
//		data.deleteWithMissingClass();
//
//		m_bagger = new Bagging();
//
//		// RandomTree implements WeightedInstancesHandler, so we can
//		// represent copies using weights to achieve speed-up.
//		m_bagger.setRepresentCopiesUsingWeights(true);
//
//		RandomTree rTree = new RandomTree();
//
//		// set up the random tree options
//		m_KValue = m_numFeatures;
//		if (m_KValue < 1) {
//			m_KValue = (int) Utils.log2(data.numAttributes() - 1) + 1;
//		}
//		rTree.setKValue(m_KValue);
//		// rTree.setMaxDepth(getMaxDepth()); 不设这个参数
//		rTree.setMinNum(getMinNum()); //改为设这个参数
//		
//		rTree.setDoNotCheckCapabilities(true);
//		rTree.setBreakTiesRandomly(getBreakTiesRandomly());
//
//		// set up the bagger and build the forest
//		m_bagger.setClassifier(rTree);
//		m_bagger.setSeed(m_randomSeed);
//		m_bagger.setNumIterations(m_numTrees);
//		m_bagger.setCalcOutOfBag(!getDontCalculateOutOfBagError());
//		m_bagger.setNumExecutionSlots(m_numExecutionSlots);
//		m_bagger.buildClassifier(data);
//	}

	/**
	 * Get the value of MinNum.
	 * 
	 * @return Value of MinNum.
	 */
	public double getMinNum() {

		return m_MinNum;
	}

	/**
	 * Set the value of MinNum.
	 * 
	 * @param newMinNum
	 *            Value to assign to MinNum.
	 */
	public void setMinNum(double newMinNum) {

		m_MinNum = newMinNum;
	}
}
