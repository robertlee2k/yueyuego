package yueyueGo.utility;

public class EvaluationParams {
	protected double eval_recent_portion;// 计算最近数据阀值从历史记录中选取多少比例的最近样本
	protected double lower_limit;// 各条均线选择样本的下限
	protected double upper_limit;// 各条均线选择样本的上限
	protected double tp_fp_ratio;//各条均线TP/FP选择阀值比例上限
	protected double tp_fp_bottom_line; //TP/FP的缺省下限
	protected double default_threshold; // 二分类器找不出threshold时缺省值。
	
	public EvaluationParams(double eval_recent_portion, double lower_limit,
			double upper_limit, double tp_fp_ratio, double tp_fp_bottom_line,
			double default_threshold) {
		super();
		this.eval_recent_portion = eval_recent_portion;
		this.lower_limit = lower_limit;
		this.upper_limit = upper_limit;
		this.tp_fp_ratio = tp_fp_ratio;
		this.tp_fp_bottom_line = tp_fp_bottom_line;
		this.default_threshold = default_threshold;
	}
	
	public double getEval_recent_portion() {
		return eval_recent_portion;
	}
	public void setEval_recent_portion(double eval_recent_portion) {
		this.eval_recent_portion = eval_recent_portion;
	}
	public double getLower_limit() {
		return lower_limit;
	}
	public void setLower_limit(double lower_limit) {
		this.lower_limit = lower_limit;
	}
	public double getUpper_limit() {
		return upper_limit;
	}
	public void setUpper_limit(double upper_limit) {
		this.upper_limit = upper_limit;
	}
	public double getTp_fp_ratio() {
		return tp_fp_ratio;
	}
	public void setTp_fp_ratio(double tp_fp_ratio) {
		this.tp_fp_ratio = tp_fp_ratio;
	}
	public double getTp_fp_bottom_line() {
		return tp_fp_bottom_line;
	}
	public void setTp_fp_bottom_line(double tp_fp_bottom_line) {
		this.tp_fp_bottom_line = tp_fp_bottom_line;
	}
	public double getDefault_threshold() {
		return default_threshold;
	}
	public void setDefault_threshold(double default_threshold) {
		this.default_threshold = default_threshold;
	}
		
}
