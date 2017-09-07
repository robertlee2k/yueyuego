package yueyueGo.utility.modelEvaluation;

import java.io.Serializable;

import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.dataFormat.AvgLineDataFormat;
import yueyueGo.dataFormat.FullModelDataFormat;
import yueyueGo.dataFormat.MomentumDataFormat;
import yueyueGo.utility.ClassifyUtility;

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
	
	protected String[] m_policyGroup;


	public static final int PREVIOUS_MODELS_NUM=5; 	//暂时选取之前的6个文件（加上9个月评估数据，也就是最大倒推2年左右）


	public static final double REVERSED_TOP_AREA_RATIO=0.5; //缺省定义反向头部为50%


	public static final double TOP_AREA_RATIO=0.1; //缺省定义头部区域为10%

	public static double LIFT_UP_TARGET=1.8; //选择样本阀值时TP FP RATIO从何开始，这个是常量


	public EvaluationConfDefinition(String classifierName,String[] a_policyGroup,ArffFormat format) {
		m_policyGroup=a_policyGroup;
		// 缺省的配置
		if (format==null || format instanceof AvgLineDataFormat || format instanceof FullModelDataFormat){
			switch (classifierName) {
			case ClassifyUtility.BAGGING_M5P:
				//缩小选股比率
				SAMPLE_LOWER_LIMIT =new double[] { 0.012, 0.015, 0.02, 0.03, 0.03 }; // 各条均线选择样本的下限
				SAMPLE_UPPER_LIMIT =new double[] { 0.03, 0.04, 0.05, 0.06, 0.06 }; // 各条均线选择样本的上限
				break;
			case ClassifyUtility.ADABOOST:
				//缩小选股比率
				SAMPLE_LOWER_LIMIT =new double[] { 0.012, 0.015, 0.02, 0.03, 0.03 }; // 各条均线选择样本的下限
				SAMPLE_UPPER_LIMIT =new double[] { 0.03, 0.04, 0.05, 0.06, 0.06 }; // 各条均线选择样本的上限

//				SAMPLE_LOWER_LIMIT =new double[] { 0.02, 0.02, 0.02, 0.03, 0.03 }; // 各条均线选择样本的下限
//				SAMPLE_UPPER_LIMIT =new double[] { 0.06, 0.06, 0.06, 0.06, 0.06 }; // 各条均线选择样本的上限
				//			SAMPLE_UPPER_LIMIT =new double[] { 0.1, 0.1, 0.1, 0.1, 0.1 }; // 各条均线选择样本的上限
				break;
			case ClassifyUtility.RANDOM_FOREST:
				SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限
				SAMPLE_UPPER_LIMIT =new double[] { 0.06, 0.06, 0.07, 0.08, 0.09 }; // 各条均线选择样本的上限
				//			SAMPLE_UPPER_LIMIT =new double[] { 0.1, 0.1, 0.1, 0.1, 0.1 }; // 各条均线选择样本的上限
				break;
			case ClassifyUtility.BAGGING_LINEAR_REGRESSION:
				SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限
				SAMPLE_UPPER_LIMIT =new double[] { 0.2, 0.2, 0.2, 0.2, 0.2 }; // 各条均线选择样本的上限
				break;
			case ClassifyUtility.M5PAB:
				SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限
				SAMPLE_UPPER_LIMIT =new double[] { 0.2, 0.2, 0.2, 0.2, 0.2 }; // 各条均线选择样本的上限
				break;
			case ClassifyUtility.MLPAB:
				SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限
				SAMPLE_UPPER_LIMIT =new double[] { 0.2, 0.2, 0.2, 0.2, 0.2 }; // 各条均线选择样本的上限
				break;
			case ClassifyUtility.MYNN_MLP:
				SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限
				SAMPLE_UPPER_LIMIT =new double[] { 0.2, 0.2, 0.2, 0.2, 0.2 }; // 各条均线选择样本的上限
				break;
			case ClassifyUtility.MYNN_MLP_FULLMODEL:
				SAMPLE_LOWER_LIMIT =new double[] { 0.02}; // 各条均线选择样本的下限
				SAMPLE_UPPER_LIMIT =new double[] { 0.04}; // 各条均线选择样本的上限
				break;
			case ClassifyUtility.BAGGING_M5P_FULLMODEL:
				SAMPLE_LOWER_LIMIT = new double[] {0.02}; // 各条均线选择样本的下限 
				SAMPLE_UPPER_LIMIT = new double[]  {0.04};
				break;
			case ClassifyUtility.BAGGING_REGRESSION_FULLMODEL:
				SAMPLE_LOWER_LIMIT =new double[] { 0.03}; // 各条均线选择样本的下限
				SAMPLE_UPPER_LIMIT =new double[] { 0.05}; // 各条均线选择样本的上限
				break;
			case ClassifyUtility.ADABOOST_FULLMODEL:
				SAMPLE_LOWER_LIMIT =new double[] { 0.02}; // 各条均线选择样本的下限
				SAMPLE_UPPER_LIMIT =new double[] { 0.04}; // 各条均线选择样本的上限
				break;
			}
		} else if (format instanceof MomentumDataFormat){ //  动量策略
			switch (classifierName) {
			case ClassifyUtility.BAGGING_M5P:
				SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限
				SAMPLE_UPPER_LIMIT =new double[] { 0.06, 0.06, 0.07, 0.08, 0.09 }; // 各条均线选择样本的上限
				break;
			case ClassifyUtility.ADABOOST:
				SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限
				SAMPLE_UPPER_LIMIT =new double[] { 0.06, 0.06, 0.07, 0.08, 0.09 }; // 各条均线选择样本的上限
				break;
			}
		}
	}

	public EvaluationParams getEvaluationInstance(String policy){
		int pos=-1;
		for (int i=0;i<m_policyGroup.length;i++){
			if (m_policyGroup[i].equals(policy)){
				pos=i;
				break;
			}
		}
		if (pos==-1) {
			throw new RuntimeException("cannot find policy ["+policy+"]in m_policySubGroup");
		}
		return new EvaluationParams(SAMPLE_LOWER_LIMIT[pos],
				SAMPLE_UPPER_LIMIT[pos], LIFT_UP_TARGET);
	}


	/*
	 * 输出当前的评估阀值定义
	 */
	public String showEvaluationParameters(){
		StringBuffer result=new StringBuffer();
		result.append("SAMPLE_LOWER_LIMIT={");
		for (double d : SAMPLE_LOWER_LIMIT) {
			result.append(d);
			result.append(" / ");
		}
		result.append("}\n");

		result.append("SAMPLE_UPPER_LIMIT={");
		for (double d : SAMPLE_UPPER_LIMIT) {
			result.append(d);
			result.append(" / ");
		}
		result.append("}\n");
		result.append("LIFT_UP_TARGET="+LIFT_UP_TARGET);
		return result.toString();
	}

	/*
	 * 输出当前的合并阀值定义
	 */
//	public static String showMergeParameters(){
//		StringBuffer result=new StringBuffer();
//		result.append("WINRATE_FILTER_FOR_SHOUYILV={");
//		for (double d : WINRATE_FILTER_FOR_SHOUYILV) {
//			result.append(d);
//			result.append(",");
//		}
//		result.append("}\n");
//
//		result.append("SHOUYILV_FILTER_FOR_WINRATE={");
//		for (double d : SHOUYILV_FILTER_FOR_WINRATE) {
//			result.append(d);
//			result.append(",");
//		}
//		result.append("}");
//		return result.toString();

//	}
}
