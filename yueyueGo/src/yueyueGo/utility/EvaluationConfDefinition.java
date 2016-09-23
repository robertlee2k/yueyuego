package yueyueGo.utility;

/**
 * @author robert
 *  用于评估模型的各种参数
 */
public class EvaluationConfDefinition {

	protected double EVAL_RECENT_PORTION;// 计算最近数据阀值从历史记录中选取多少比例的最近样本
	protected double[] SAMPLE_LOWER_LIMIT; // 各条均线选择样本的下限
	protected double[] SAMPLE_UPPER_LIMIT; // 各条均线选择样本的上限
	protected double[] TP_FP_RATIO_LIMIT; //各条均线TP/FP选择阀值比例上限
	protected double[] TP_FP_BOTTOM_LINE; //TP/FP的缺省下限
	protected double[] DEFAULT_THRESHOLD; // 二分类器找不出threshold时缺省值。
	
	public EvaluationParams getEvaluationInstance(int policy){
		return new EvaluationParams(EVAL_RECENT_PORTION, SAMPLE_LOWER_LIMIT[policy],
				SAMPLE_UPPER_LIMIT[policy], TP_FP_RATIO_LIMIT[policy], TP_FP_BOTTOM_LINE[policy],
				DEFAULT_THRESHOLD[policy]);
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
