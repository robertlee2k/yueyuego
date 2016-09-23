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
	
	protected double EVAL_RECENT_PORTION;// 计算最近数据阀值从历史记录中选取多少比例的最近样本
	protected double[] SAMPLE_LOWER_LIMIT; // 各条均线选择样本的下限
	protected double[] SAMPLE_UPPER_LIMIT; // 各条均线选择样本的上限
	protected double[] TP_FP_RATIO_LIMIT; //各条均线TP/FP选择阀值比例上限
	protected double[] TP_FP_BOTTOM_LINE; //TP/FP的缺省下限
	protected double[] DEFAULT_THRESHOLD; // 二分类器找不出threshold时缺省值。
	
	public EvaluationConfDefinition(String classifierName) {
		if (classifierName.equals(ClassifyUtility.BAGGING_M5P)){
			EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
			SAMPLE_LOWER_LIMIT = new double[]{ 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限 
			SAMPLE_UPPER_LIMIT = new double[]  { 0.06, 0.07, 0.1, 0.11, 0.12 };
			TP_FP_RATIO_LIMIT = new double[] { 1.8, 1.7, 1.5, 1.2, 1}; //选择样本阀值时TP FP RATIO到了何种值就可以提前停止了。
			TP_FP_BOTTOM_LINE= new double[] { 0.9,0.8,0.7,0.6,0.5}; //TP/FP的下限
		}else if(classifierName.equals(ClassifyUtility.ADABOOST)){
			EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
			SAMPLE_LOWER_LIMIT =new double[] { 0.04, 0.05, 0.06, 0.07, 0.08 }; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.07, 0.08, 0.11, 0.12, 0.13 }; // 各条均线选择样本的上限
			TP_FP_RATIO_LIMIT=new double[] { 1.8, 1.7, 1.3, 1.1, 0.9};//选择样本阀值时TP FP RATIO从何开始
			TP_FP_BOTTOM_LINE=new double[] {0.8,0.7,0.6,0.5,0.4}; //TP/FP的下限
			DEFAULT_THRESHOLD=new double[] {0.8,0.75,0.7,0.6,0.5}; // 找不出threshold时缺省值。
		}else if(classifierName.equals(ClassifyUtility.MYNN_MLP)){
			EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
			SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.04, 0.05, 0.06, 0.07 }; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.06, 0.07, 0.1, 0.11, 0.12 }; // 各条均线选择样本的上限
			TP_FP_RATIO_LIMIT=new double[] { 1.8, 1.7, 1.3, 1.1, 0.9};//选择样本阀值时TP FP RATIO从何开始
			TP_FP_BOTTOM_LINE=new double[] {0.6,0.6,0.6,0.6,0.6}; //TP/FP的下限
			DEFAULT_THRESHOLD=new double[] {0.5,0.5,0.5,0.5,0.5}; // 找不出threshold时缺省值。
		}else if (classifierName.equals(ClassifyUtility.MYNN_MLP_FULLMODEL)){
			EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
			SAMPLE_LOWER_LIMIT =new double[] { 0.02}; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.04}; // 各条均线选择样本的上限
			TP_FP_RATIO_LIMIT=new double[] { 1.8};//选择样本阀值时TP FP RATIO从何开始
			TP_FP_BOTTOM_LINE=new double[] {0.9}; //TP/FP的下限
			DEFAULT_THRESHOLD=new double[] {0.6}; // 找不出threshold时缺省值。
		}else if(classifierName.equals(ClassifyUtility.BAGGING_M5P_FULLMODEL)) {
			EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
			SAMPLE_LOWER_LIMIT = new double[] {0.02}; // 各条均线选择样本的下限 
			SAMPLE_UPPER_LIMIT = new double[]  {0.04};
			TP_FP_RATIO_LIMIT = new double[] { 1.8}; //选择样本阀值时TP FP RATIO到了何种值就可以提前停止了。
			TP_FP_BOTTOM_LINE=new double[] {0.9}; //TP/FP的下限
		}
	}

	public EvaluationParams getEvaluationInstance(int policyIndex){
		return new EvaluationParams(EVAL_RECENT_PORTION, SAMPLE_LOWER_LIMIT[policyIndex],
				SAMPLE_UPPER_LIMIT[policyIndex], TP_FP_RATIO_LIMIT[policyIndex], TP_FP_BOTTOM_LINE[policyIndex],
				DEFAULT_THRESHOLD[policyIndex]);
	}
	
	public double getEVAL_RECENT_PORTION() {
		return EVAL_RECENT_PORTION;
	}
	public void setEVAL_RECENT_PORTION(double eVAL_RECENT_PORTION) {
		EVAL_RECENT_PORTION = eVAL_RECENT_PORTION;
	}
	public double[] getSAMPLE_LOWER_LIMIT() {
		return SAMPLE_LOWER_LIMIT;
	}
	public void setSAMPLE_LOWER_LIMIT(double[] sAMPLE_LOWER_LIMIT) {
		SAMPLE_LOWER_LIMIT = sAMPLE_LOWER_LIMIT;
	}
	public double[] getSAMPLE_UPPER_LIMIT() {
		return SAMPLE_UPPER_LIMIT;
	}
	public void setSAMPLE_UPPER_LIMIT(double[] sAMPLE_UPPER_LIMIT) {
		SAMPLE_UPPER_LIMIT = sAMPLE_UPPER_LIMIT;
	}
	public double[] getTP_FP_RATIO_LIMIT() {
		return TP_FP_RATIO_LIMIT;
	}
	public void setTP_FP_RATIO_LIMIT(double[] tP_FP_RATIO_LIMIT) {
		TP_FP_RATIO_LIMIT = tP_FP_RATIO_LIMIT;
	}
	public double[] getTP_FP_BOTTOM_LINE() {
		return TP_FP_BOTTOM_LINE;
	}
	public void setTP_FP_BOTTOM_LINE(double[] tP_FP_BOTTOM_LINE) {
		TP_FP_BOTTOM_LINE = tP_FP_BOTTOM_LINE;
	}
	public double[] getDEFAULT_THRESHOLD() {
		return DEFAULT_THRESHOLD;
	}
	public void setDEFAULT_THRESHOLD(double[] dEFAULT_THRESHOLD) {
		DEFAULT_THRESHOLD = dEFAULT_THRESHOLD;
	}


}
