package yueyueGo.utility.modelPredict;

import java.io.Serializable;

/*
 * 用来存储预测时的中间状态（当前累计选股率）的持久化类
 */
public class PredictStatus implements Serializable {

	/**
	 * 这个在每日预测时是需要持久化的
	 */
	private static final long serialVersionUID = -3987707659874509564L;

	private String yearSplit; //年月分类
	private String policy;  //策略分类
	private int cummulativePredicted=0; //累计预测总数
	private int cummulativeSelected=0; //累计选股数

	
	public PredictStatus(String yearSplit, String policy) {
		this.yearSplit = yearSplit;
		this.policy = policy;
	}

	/*
	 * 反序列化后的校验
	 */
	public boolean verifyStatusData(String a_yearSplit, String a_policy) {
		 if (a_yearSplit.equals(yearSplit) && a_policy.equals(policy)){
			 return true;
		 }else {
			 return false;
		 }
	}

	public int getCummulativePredicted() {
		return cummulativePredicted;
	}


	public void addCummulativePredicted(int currentPredicted) {
		this.cummulativePredicted += currentPredicted;
	}


	public int getCummulativeSelected() {
		return cummulativeSelected;
	}


	public void addCummulativeSelected(int currentSelected) {
		this.cummulativeSelected += currentSelected;
	}
	
	//累计的选股比率
	public double getCummulativeSelectRatio(){
		double ratio=0.0;
		if (cummulativePredicted>0){
			ratio=((double)cummulativeSelected)/cummulativePredicted;
		}
		return ratio;
		
	}
	
}
