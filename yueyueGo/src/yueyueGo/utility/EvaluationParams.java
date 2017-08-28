package yueyueGo.utility;

public class EvaluationParams {
	protected double lower_limit;// 各条均线选择样本的下限
	protected double upper_limit;// 各条均线选择样本的上限
	protected double lift_up_target;//TP/FP的目标提升率
	
	public EvaluationParams( double lower_limit,
			double upper_limit, double lift_up_target) {
		this.lower_limit = lower_limit;
		this.upper_limit = upper_limit;
		this.lift_up_target = lift_up_target;
	}
	
	public double getLower_limit() {
		return lower_limit;
	}

	public double getUpper_limit() {
		return upper_limit;
	}
	public double getLift_up_target() {
		return lift_up_target;
	}
		
}
