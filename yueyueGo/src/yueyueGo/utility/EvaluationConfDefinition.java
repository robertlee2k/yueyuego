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
	protected double[] LIFT_UP_TARGET; //TP/FP的目标提升率
	
	public EvaluationConfDefinition(String classifierName) {
		if (classifierName.equals(ClassifyUtility.BAGGING_M5P)){
			SAMPLE_LOWER_LIMIT = new double[]{ 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限 
			SAMPLE_UPPER_LIMIT = new double[]  { 0.06, 0.07, 0.1, 0.11, 0.12 };
			LIFT_UP_TARGET = new double[] { 1.8, 1.7, 1.5, 1.2, 1}; //选择样本阀值时TP FP RATIO到了何种值就可以提前停止了。
		} else if (classifierName.equals(ClassifyUtility.M5PAB)) {
			SAMPLE_LOWER_LIMIT = new double[]{ 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限 
			SAMPLE_UPPER_LIMIT = new double[]  { 0.06, 0.07, 0.1, 0.11, 0.12 };
			LIFT_UP_TARGET = new double[] { 1.8, 1.7, 1.5, 1.2, 1}; //选择样本阀值时TP FP RATIO到了何种值就可以提前停止了。
		}else if (classifierName.equals(ClassifyUtility.MLPAB)) {
			SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.04, 0.05, 0.06, 0.07 }; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.06, 0.07, 0.1, 0.11, 0.12 }; // 各条均线选择样本的上限
			LIFT_UP_TARGET=new double[] { 1.8, 1.7, 1.3, 1.1, 0.9};//选择样本阀值时TP FP RATIO从何开始
		} else if(classifierName.equals(ClassifyUtility.ADABOOST)){
			SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.2, 0.2, 0.2, 0.2, 0.2 }; // 各条均线选择样本的上限
			LIFT_UP_TARGET=new double[] { 1.8, 1.8, 1.8, 1.8, 1.8};//选择样本阀值时TP FP RATIO从何开始, 这个没必要做成数组了
		}else if(classifierName.equals(ClassifyUtility.MYNN_MLP)){
			SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.2, 0.2, 0.2, 0.2, 0.2 }; // 各条均线选择样本的上限
			LIFT_UP_TARGET=new double[] { 1.8, 1.8, 1.8, 1.8, 1.8};//选择样本阀值时TP FP RATIO从何开始, 这个没必要做成数组了
		}else if (classifierName.equals(ClassifyUtility.MYNN_MLP_FULLMODEL)){
			SAMPLE_LOWER_LIMIT =new double[] { 0.02}; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.04}; // 各条均线选择样本的上限
			LIFT_UP_TARGET=new double[] { 1.8};//选择样本阀值时TP FP RATIO从何开始
		}else if(classifierName.equals(ClassifyUtility.BAGGING_M5P_FULLMODEL)) {
			SAMPLE_LOWER_LIMIT = new double[] {0.02}; // 各条均线选择样本的下限 
			SAMPLE_UPPER_LIMIT = new double[]  {0.04};
			LIFT_UP_TARGET = new double[] { 1.8}; //选择样本阀值时TP FP RATIO到了何种值就可以提前停止了。
		}else if (classifierName.equals(ClassifyUtility.BAGGING_J48_FULLMODEL)){
			SAMPLE_LOWER_LIMIT =new double[] { 0.03}; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.05}; // 各条均线选择样本的上限
			LIFT_UP_TARGET=new double[] { 1.8};//选择样本阀值时TP FP RATIO从何开始
//			TP_FP_BOTTOM_LINE=new double[] {0.8}; //TP/FP的下限
//			DEFAULT_THRESHOLD=new double[] {0.6}; // 找不出threshold时缺省值。
		}else if (classifierName.equals(ClassifyUtility.ADABOOST_FULLMODEL)){
			SAMPLE_LOWER_LIMIT =new double[] { 0.02}; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.04}; // 各条均线选择样本的上限
			LIFT_UP_TARGET=new double[] { 1.8};//选择样本阀值时TP FP RATIO从何开始
//			TP_FP_BOTTOM_LINE=new double[] {0.9}; //TP/FP的下限
//			DEFAULT_THRESHOLD=new double[] {0.6}; // 找不出threshold时缺省值。
		}
	}

	public EvaluationParams getEvaluationInstance(int policyIndex){
//		double defaultThreshold;
//		if (DEFAULT_THRESHOLD==null){ //对于连续分类器，没有这个参数的设定
//			defaultThreshold=Double.NaN;
//		}else{
//			defaultThreshold=DEFAULT_THRESHOLD[policyIndex];
//		}
		return new EvaluationParams(SAMPLE_LOWER_LIMIT[policyIndex],
				SAMPLE_UPPER_LIMIT[policyIndex], LIFT_UP_TARGET[policyIndex]);
	}
	


}
