package yueyueGo.utility;

import java.io.Serializable;

/**
 * @author robert
 *  用于评估模型的各种参数
 */
public class EvaluationConfDefinition implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4464388266024527544L;
	
	protected double[] SAMPLE_LOWER_LIMIT; // 各条均线选择样本的下限
	protected double[] SAMPLE_UPPER_LIMIT; // 各条均线选择样本的上限
	public static double LIFT_UP_TARGET=1.8; //选择样本阀值时TP FP RATIO从何开始，这个是常量
	
	public EvaluationConfDefinition(String classifierName) {
		if (classifierName.equals(ClassifyUtility.BAGGING_M5P)){
			SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.03, 0.03, 0.03, 0.03 };//{ 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.1, 0.1, 0.1, 0.1, 0.1 }; // 各条均线选择样本的上限
		}else if (classifierName.equals(ClassifyUtility.BAGGING_LINEAR_REGRESSION)){
			SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.2, 0.2, 0.2, 0.2, 0.2 }; // 各条均线选择样本的上限
		}else if (classifierName.equals(ClassifyUtility.M5PAB)) {
			SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.2, 0.2, 0.2, 0.2, 0.2 }; // 各条均线选择样本的上限
		}else if (classifierName.equals(ClassifyUtility.MLPAB)) {
			SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.2, 0.2, 0.2, 0.2, 0.2 }; // 各条均线选择样本的上限
		} else if(classifierName.equals(ClassifyUtility.ADABOOST)){
			SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.1, 0.1, 0.1, 0.1, 0.1 }; // 各条均线选择样本的上限
		}else if(classifierName.equals(ClassifyUtility.MYNN_MLP)){
			SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.2, 0.2, 0.2, 0.2, 0.2 }; // 各条均线选择样本的上限
		}else if (classifierName.equals(ClassifyUtility.MYNN_MLP_FULLMODEL)){
			SAMPLE_LOWER_LIMIT =new double[] { 0.02}; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.04}; // 各条均线选择样本的上限
		}else if(classifierName.equals(ClassifyUtility.BAGGING_M5P_FULLMODEL)) {
			SAMPLE_LOWER_LIMIT = new double[] {0.02}; // 各条均线选择样本的下限 
			SAMPLE_UPPER_LIMIT = new double[]  {0.04};
		}else if (classifierName.equals(ClassifyUtility.BAGGING_REGRESSION_FULLMODEL)){
			SAMPLE_LOWER_LIMIT =new double[] { 0.03}; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.05}; // 各条均线选择样本的上限
		}else if (classifierName.equals(ClassifyUtility.ADABOOST_FULLMODEL)){
			SAMPLE_LOWER_LIMIT =new double[] { 0.02}; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.04}; // 各条均线选择样本的上限
		}
	}

	public EvaluationParams getEvaluationInstance(int policyIndex){
		return new EvaluationParams(SAMPLE_LOWER_LIMIT[policyIndex],
				SAMPLE_UPPER_LIMIT[policyIndex], LIFT_UP_TARGET);
	}
	


}
