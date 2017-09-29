package yueyueGo.utility.analysis;

import yueyueGo.utility.FormatUtility;

/*
 * Describe Shouyilv
 * "period","policy","category","count","shouyilv average","positive ratio"
 */
public class ShouyilvDescriptive {

	public static final String ALL="ALL";
	protected String period;
	protected String policy;
	protected int count;
	protected int positiveCount;
	protected double shouyilvAverage;
	protected double positiveShouyilvAverage;
	protected double negativeShouyilvAverage;
	
	
	public ShouyilvDescriptive(String period, String policy,  int count,  double shouyilvAverage,int positiveCount,
			double positiveShouyilvAverage, double negativeShouyilvAverage) {
		this.period = period;
		this.policy = policy;
		this.count = count;
		this.positiveCount = positiveCount;
		this.shouyilvAverage = shouyilvAverage;
		this.positiveShouyilvAverage = positiveShouyilvAverage;
		this.negativeShouyilvAverage = negativeShouyilvAverage;
	}


	public String toCSVString(){
		StringBuffer result=new StringBuffer();
		result.append(period);
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
		return result.toString();
	}
	
	public String toDescriptions(){
		StringBuffer result=new StringBuffer();
		String tab;
		//根据policy不同决定是否要加tab缩进
		if (ALL.equals(this.policy)){
			tab="";
		}else{
			tab="\t";
		}
		result.append(tab+" shouyilv average for policy["+policy+"]=" +FormatUtility.formatPercent(shouyilvAverage,2,3)+" count="+count);
		result.append("\r\n");
		result.append(tab+"\t actual positive average="+FormatUtility.formatPercent(positiveShouyilvAverage,2,3)+" count="+positiveCount);
		result.append("\r\n");
		result.append(tab+"\t actual negative average="+FormatUtility.formatPercent(negativeShouyilvAverage,2,3)+" count="+(count-positiveCount));
		result.append("\r\n");
		result.append(tab+"\t actual positive ratio="+FormatUtility.formatPercent(this.getPositiveRatio(),2,2));
		result.append("\r\n");
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
	

}
