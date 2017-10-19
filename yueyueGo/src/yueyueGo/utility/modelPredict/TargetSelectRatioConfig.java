package yueyueGo.utility.modelPredict;

import java.io.Serializable;

import yueyueGo.AbstractModel;
import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.dataFormat.AvgLineDataFormat;
import yueyueGo.dataFormat.MomentumDataFormat;
import yueyueGo.utility.ClassifyUtility;



/**
 * @author robert
 * 设定各种均线状态下选股比率目标值
 */
public class TargetSelectRatioConfig implements Serializable{

	/**
	 * 
	 */

	private static final long serialVersionUID = 4464388266024527544L;
	

	protected double[] SAMPLE_LOWER_LIMIT; // 各条均线选择样本的下限
	protected double[] SAMPLE_UPPER_LIMIT; // 各条均线选择样本的上限  
	
	protected String[] m_policyGroup;



	public TargetSelectRatioConfig(AbstractModel clmodel,ArffFormat format) {
		String classifierName=clmodel.classifierName;
		m_policyGroup=clmodel.m_policySubGroup;

		if (format instanceof AvgLineDataFormat){		
			switch (classifierName) {
			case ClassifyUtility.BAGGING_M5P:
				//缩小选股比率
				SAMPLE_LOWER_LIMIT =new double[] { 0.0101, 0.0107, 0.0203};// 0.03, 0.03 }; // 各条均线选择样本的下限
				SAMPLE_UPPER_LIMIT =new double[] { 0.03, 0.03, 0.03};//, 0.06, 0.06 }; // 各条均线选择样本的上限
				break;
			case ClassifyUtility.ADABOOST:
				//缩小选股比率
				SAMPLE_LOWER_LIMIT =new double[] { 0.01};//, 0.015, 0.02, 0.03, 0.03 }; // 各条均线选择样本的下限
				SAMPLE_UPPER_LIMIT =new double[] { 0.03};//, 0.04, 0.05, 0.06, 0.06 }; // 各条均线选择样本的上限

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

			}
		}else if (format instanceof MomentumDataFormat){ //  动量策略
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
		}else { //format instanceof FullModelDataFormat
			//未定义的格式
//			break;
		}
	}

	public TargetSelectRatio getEvaluationInstance(String policy){
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
		return new TargetSelectRatio((SAMPLE_LOWER_LIMIT[pos]+SAMPLE_UPPER_LIMIT[pos])/2,SAMPLE_LOWER_LIMIT[pos],SAMPLE_UPPER_LIMIT[pos]);
	}


	/*
	 * 输出当前的目标选股比率定义
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
		
		return result.toString();
	}

}
