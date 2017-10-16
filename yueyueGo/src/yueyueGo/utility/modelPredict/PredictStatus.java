package yueyueGo.utility.modelPredict;

import java.io.Serializable;
import java.util.Date;

import weka.core.SerializedObject;
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
	private String startDate; //以下的累计数值的起始日 （这个是作为参考值记录）
	private Date tradeDate; //最终更新数据的日期 （这个是在每日预测中使用）
	private int cummulativePredicted=0; //累计预测总数
	private int cummulativeSelected=0; //累计选股数
	
	private int nonZeroPredictedDays=0;//累计有选股机会时的预测天数
	private double averageSelectedRatio=0;//当前累计平均比率

	
	public PredictStatus(String modelID,String yearSplit, String policy) {
		this.modelID=modelID;
		this.yearSplit = yearSplit;
		this.policy = policy;
		this.startDate=FormatUtility.getDateStringFor(0);
	}

	public int getCummulativePredicted() {
		return cummulativePredicted;
	}


	/*
	 * 当期预测结束后，更新统计数据
	 */
	public void updateAfterCurrentPeriod(int currentPredicted,int currentSelected){

		if (currentPredicted>0){ //当期进行过预测时更新，否则（机会数为0时）保持原值
			double ratioInCurrentPeriod=((double)currentSelected)/currentPredicted;
			this.averageSelectedRatio=(ratioInCurrentPeriod+averageSelectedRatio*nonZeroPredictedDays)/(nonZeroPredictedDays+1);
			this.cummulativePredicted += currentPredicted;
			this.cummulativeSelected += currentSelected;				
			this.nonZeroPredictedDays++;				
		}

	}
	


	public int getCummulativeSelected() {
		return cummulativeSelected;
	}


	
	
	//累计的选股比率
//	public double getCummulativeSelectRatio(){
//		
//		return ratio;
//		
//	}
	
	public int getNonZeroPredictedDays() {
		return nonZeroPredictedDays;
	}

	public double getAverageSelectedRatio() {
		return averageSelectedRatio;
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
		result.append("tradeDate="+tradeDate);
		result.append("\r\n");
		return result.toString();
	}



	public Date getTradeDate() {
		return tradeDate;
	}

	public void setTradeDate(Date tradeDate) {
		this.tradeDate = tradeDate;
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
	
	/**
	 * Creates a deep copy of the given status using serialization.
	 *
	 * @return a deep copy of the status
	 * @exception Exception if an error occurs
	 */
	public static PredictStatus makeCopy(PredictStatus status) throws Exception {

		return (PredictStatus) new SerializedObject(status).getObject();
	}
}
