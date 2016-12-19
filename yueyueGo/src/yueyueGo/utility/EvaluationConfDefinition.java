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
	//对于收益率优先算法的胜率筛选阀值 
	//尝试过下面的数据全部调整至0.5，只对2009年的收益率有影响（下降20%左右），因此恢复原状
	public final static double[] WINRATE_FILTER_FOR_SHOUYILV=new double[] {0.45,0.45,0.45,0.35,0.3};//{0.4,0.4,0.4,0.35,0.35};//{0.5,0.5,0.5,0.5,0.5};//{0,0,0,0,0};//{0.3,0.3,0.3,0.3,0.3};
	//对于胜率优先算法的收益率筛选阀值
	//因为Adaboost不是很有效，这个阀值不太管用
	public final static double[] SHOUYILV_FILTER_FOR_WINRATE=new double[] {0.005,0.005,0.01,0.03,0.03};//{0.005,0.01,0.03,0.05,0.08};//{0.01,0.02,0.03,0.03,0.04};//{0.005,0.005,0.01,0.03,0.03};////{0,0,0,0,0};//{-100,-100,-100,-100,-100}; 
	
	
	protected double[] SAMPLE_LOWER_LIMIT; // 各条均线选择样本的下限
	protected double[] SAMPLE_UPPER_LIMIT; // 各条均线选择样本的上限  

	public static double LIFT_UP_TARGET=1.8; //选择样本阀值时TP FP RATIO从何开始，这个是常量
	
	public EvaluationConfDefinition(String classifierName) {
		if (classifierName.equals(ClassifyUtility.BAGGING_M5P)){
			//M5P的上限选0.1比0.05收益率高（每年都稍高），下限选到0.02后比较差，恢复0.03
			SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.1, 0.1, 0.1, 0.1, 0.1 }; // 各条均线选择样本的上限
		} else if(classifierName.equals(ClassifyUtility.ADABOOST)){
			SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限
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
