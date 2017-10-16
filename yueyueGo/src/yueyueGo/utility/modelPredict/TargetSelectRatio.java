package yueyueGo.utility.modelPredict;

/*
 * 选股比率的实例类
 */
public class TargetSelectRatio {
	protected double lower_limit;// 各条均线选择样本的下限
	protected double upper_limit;// 各条均线选择样本的上限

	
	public TargetSelectRatio( double lower_limit,
			double upper_limit) {
		this.lower_limit = lower_limit;
		this.upper_limit = upper_limit;
	}
	
	public double getLower_limit() {
		return lower_limit;
	}

	public double getUpper_limit() {
		return upper_limit;
	}
		
}
