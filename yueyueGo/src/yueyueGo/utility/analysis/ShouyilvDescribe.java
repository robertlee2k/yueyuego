package yueyueGo.utility.analysis;

import java.util.ArrayList;

import yueyueGo.utility.FormatUtility;

/*
 * Describe Shouyilv
 * "period","policy","category","count","shouyilv average","positive ratio"
 */
public class ShouyilvDescribe {

	public static final String ALL="ALL";
	public static final String HEADER="所属区间,所用模型,均线分组,总数,收益率平均值,正收益数,正收益率平均值,负收益数,负收益率平均值,正值率,";
	protected String period;
	protected String policy;
	protected String classifierName;
	protected int count;
	protected int positiveCount;
	protected double shouyilvAverage;
	protected double positiveShouyilvAverage;
	protected double negativeShouyilvAverage;
	
	
	public ShouyilvDescribe(String period, String classifierName,String policy,  int count,  double shouyilvAverage,int positiveCount,
			double positiveShouyilvAverage, double negativeShouyilvAverage) {
		this.period = period;
		this.policy = policy;
		this.classifierName= classifierName;
		this.count = count;
		this.positiveCount = positiveCount;
		this.shouyilvAverage = shouyilvAverage;
		this.positiveShouyilvAverage = positiveShouyilvAverage;
		this.negativeShouyilvAverage = negativeShouyilvAverage;
	}


	public String toString(){
		StringBuffer result=new StringBuffer();
		result.append(period);
		result.append(",");
		result.append(classifierName);
		result.append(",");
		result.append(policy);
		result.append(",");
		result.append(count);
		result.append(",");
		result.append(FormatUtility.formatPercent(shouyilvAverage,2,3));
		result.append(",");
		result.append(positiveCount);
		result.append(",");
		result.append(FormatUtility.formatPercent(positiveShouyilvAverage,2,3));
		result.append(",");
		result.append(count-positiveCount);
		result.append(",");
		result.append(FormatUtility.formatPercent(negativeShouyilvAverage,2,3));
		result.append(",");
		result.append(FormatUtility.formatPercent(this.getPositiveRatio(),2,2));
		result.append(",");
		return result.toString();
	}

	
	public String getPeriod() {
		return period;
	}


	public String getPolicy() {
		return policy;
	}


	public int getCount() {
		return count;
	}


	public int getPositiveCount() {
		return positiveCount;
	}


	public double getShouyilvAverage() {
		return shouyilvAverage;
	}


	public double getPositiveShouyilvAverage() {
		return positiveShouyilvAverage;
	}


	public double getNegativeShouyilvAverage() {
		return negativeShouyilvAverage;
	}


	public double getPositiveRatio() {
		double result;
		if (count==0){
			result=0;
		}else{
			result=(double)positiveCount/count;
		}
		return result;
	}


	/**
	 * @param shouyilvDescriptions
	 * @return
	 */
	public static String convertListToCSV(ArrayList<ShouyilvDescribe> shouyilvDescriptions) {
		StringBuffer outputCSV=new StringBuffer(HEADER+"\r\n");
		   for (ShouyilvDescribe shouyilvDescribe : shouyilvDescriptions) {
			   outputCSV.append(shouyilvDescribe.toString()+"\r\n");
		   }
		return outputCSV.toString();
	}
	
	/*
	 * 将同样大小的描述文件横向合并
	 */
	public static String mergeListsToCSV(ArrayList<ShouyilvDescribe>[] shouyilvDescriptionsArray) {
		int repeat=shouyilvDescriptionsArray.length;
		
		StringBuffer outputCSV=new StringBuffer();
		for (int i=0;i<repeat;i++){
			outputCSV.append(HEADER);
		}
		outputCSV.append("\r\n");

		for (int i=0;i<shouyilvDescriptionsArray[0].size();i++){
			for (int j=0;j<repeat;j++){
			  outputCSV.append(shouyilvDescriptionsArray[j].get(i).toString()+"\r\n");
			}
		}
		return outputCSV.toString();
	}
	

}
