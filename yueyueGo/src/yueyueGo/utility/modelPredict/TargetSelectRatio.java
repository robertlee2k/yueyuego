package yueyueGo.utility.modelPredict;

/*
 * 选股比率的实例类
 */
public class TargetSelectRatio {
	protected double lowerLimit; // 各条均线选择样本的下限
	protected double upperLimit; // 各条均线选择样本的上限
	protected double targetRatio; //目标选股比率

	
	public TargetSelectRatio( double target_ratio,double lower_limit,
			double upper_limit) {
		this.targetRatio=target_ratio;
		this.lowerLimit = lower_limit;
		this.upperLimit = upper_limit;
	}
	
	public double getLowerLimit() {
		return lowerLimit;
	}

	public double getUpperLimit() {
		return upperLimit;
	}
	
	/*
	 * 因为程序内部一般是用百分位数来判断，所以选股率要转换为percentile
	 * 
	 */
	public double getTargetPercentile() {
		return (1-targetRatio)*100;
	}
}
