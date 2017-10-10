package yueyueGo.utility.modelPredict;

import java.io.Serializable;

import yueyueGo.utility.FormatUtility;

/*
 * 用来存储预测时的中间状态（当前累计选股率）的持久化类
 */
public class PredictStatus implements Serializable {

	/**
	 * 这个在每日预测时是需要持久化的
	 */
	private static final long serialVersionUID = -3987707659874509564L;
	
	public static final String FILE_PREFIX="predictorStatus";

	private String modelID; //模型的ID
	private String yearSplit; //年月分类
	private String policy;  //策略分类
	private String startDate; //以下的累计数值的起始日 （这个是在每日预测中使用）
	private int cummulativePredicted=0; //累计预测总数
	private int cummulativeSelected=0; //累计选股数

	
	public PredictStatus(String modelID,String yearSplit, String policy) {
		this.modelID=modelID;
		this.yearSplit = yearSplit;
		this.policy = policy;
		this.startDate=FormatUtility.getDateStringFor(0);
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
		double ratio=Double.NaN;
		if (cummulativePredicted>0){
			ratio=((double)cummulativeSelected)/cummulativePredicted;
		}
		return ratio;
		
	}
	
	/*
	 * 输出可读的txt文件
	 */
	public String toTXTString(){
		StringBuffer result=new StringBuffer();
		result.append("startDate="+startDate);
		result.append("\r\n");
		result.append("模型ID="+modelID);
		result.append("\r\n");
		result.append("yearmonth="+yearSplit);
		result.append("\r\n");
		result.append("policy="+policy);
		result.append("\r\n");
		result.append("本月累计预测总数="+cummulativePredicted);
		result.append("\r\n");
		result.append("本月累计选股数"+cummulativeSelected);
		result.append("\r\n");
		result.append("generateDate="+FormatUtility.getDateStringFor(0));
		result.append("\r\n");
		return result.toString();
	}



	/*
	 * 反序列化后的校验
	 */
	public boolean verifyStatusData(String a_modelID, String a_policy) {
		 if ( a_policy.equals(policy) && a_modelID.equals(modelID)){
			 return true;
		 }else {
			 return false;
		 }
	}	
}
