package yueyueGo.utility.analysis;

import java.util.ArrayList;

import yueyueGo.BackTest;
import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.dataProcessor.InstanceHandler;
import yueyueGo.dataProcessor.WekaInstanceProcessor;
import yueyueGo.databeans.GeneralAttribute;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.utility.FormatUtility;

/*
 * 分析历史各阶段的收益率数据分布
 * 
 大牛市 200601-200711
 大熊市  200712-200810
小牛市  200811-200907
慢熊市   200908-201306
慢反弹  201307-201409
大牛市  201410-201505
大熊市   201506-201508
小反弹  201509-201512
小熊市    201601-201609
慢反弹  201610-201707
 * 
 */
public class DataAnalysis {

	public static final MarketDefinition[] MARKET_DEFINITION={
			new MarketDefinition("大牛市" , 200601, 200711),
			new MarketDefinition("大熊市" , 200712, 200810),
			new MarketDefinition("小牛市" , 200811, 200907),
			new MarketDefinition("慢熊市" , 200908, 201306),
			new MarketDefinition("慢反弹" , 201307, 201409),
			new MarketDefinition("大牛市" , 201410, 201505),
			new MarketDefinition("大熊市" , 201506, 201508),
			new MarketDefinition("小反弹" , 201509, 201512),
			new MarketDefinition("小熊市" , 201601, 201609),
			new MarketDefinition("慢反弹" , 201610, 201707),
	};


	
	public static String analyzeMarket(String policyGroupName,String[] policyStrings,GeneralInstances fullData)throws Exception{
		int startYearMonth;
		int endYearMonth;
		int yearMonthPos=BaseInstanceProcessor.findATTPosition(fullData,ArffFormat.YEAR_MONTH);
		String attPos=WekaInstanceProcessor.WEKA_ATT_PREFIX + yearMonthPos;
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(fullData);
			
		StringBuffer outputCSV=new StringBuffer("所属区间,均线分组,总数,收益率平均值,正收益数,正收益率平均值,负收益数,负收益率平均值,正值率\r\n");
		
		for (int i=0;i<MARKET_DEFINITION.length;i++){
			startYearMonth=MARKET_DEFINITION[i].getStartYearMonth();
			endYearMonth=MARKET_DEFINITION[i].getEndYearMonth();
			
			System.out.println(".........now output    ...."+MARKET_DEFINITION[i].toString());
			String timeRange=MARKET_DEFINITION[i].toString()+"["+startYearMonth+"-"+endYearMonth+"]";
			String splitClause ="(" + attPos + " >= "+ startYearMonth + ") and (" + attPos + " <= " + endYearMonth + ") ";
			GeneralInstances marketData=instanceProcessor.getInstancesSubset(fullData, splitClause);
			ArrayList<ShouyilvDescribe> shouyilvDescriptions=analyzeDataDistribution(policyGroupName,policyStrings,timeRange,marketData);
			for (ShouyilvDescribe shouyilvDescribe : shouyilvDescriptions) {
				outputCSV.append(shouyilvDescribe.toString()+"\r\n");
			}
			System.out.println(".........end of output ...."+MARKET_DEFINITION[i].toString());
		}
		return outputCSV.toString();
		
	}
	
	/**
	 * @param data
	 */
	public static ArrayList<ShouyilvDescribe> analyzeDataDistribution(String policyGroupName,String[] policyStrings,String timeRange,GeneralInstances data) throws Exception {
		ArrayList<ShouyilvDescribe> shouyilvDesc=new ArrayList<ShouyilvDescribe>();
		System.out.println("start of data distribution analysis.......");
		GeneralAttribute shouyilvAttribute=data.attribute(ArffFormat.SHOUYILV);
		int shouyilvPos = BaseInstanceProcessor.findATTPosition(data,ArffFormat.SHOUYILV);
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(data);
		int policyPos = BaseInstanceProcessor.findATTPosition(data,policyGroupName);

		int count;
		double shouyilvAverage;
		
		ShouyilvDescribe oneDescription;
		shouyilvAverage=data.meanOrMode(shouyilvAttribute);
		count=data.numInstances();
		oneDescription=new ShouyilvDescribe(timeRange, ShouyilvDescribe.ALL, count,shouyilvAverage,0,0,0);
		shouyilvDesc.add(oneDescription);
		System.out.println(" shouyilv average="+FormatUtility.formatPercent(shouyilvAverage,2,4)+" count="+count);

		for (int j = BackTest.BEGIN_FROM_POLICY; j < policyStrings.length; j++) {
	
			int positiveCount;
			double positiveShouyilvAverage;
			double negativeShouyilvAverage;


			String policy = policyStrings[j];
			GeneralInstances policyData=instanceProcessor.getInstancesSubset(data, WekaInstanceProcessor.WEKA_ATT_PREFIX +policyPos+" is '"	+ policy + "'");
			
			shouyilvAverage=policyData.meanOrMode(shouyilvAttribute);
			count=policyData.numInstances();
			System.out.println("\t shouyilv average for policy["+policy+"]=" +FormatUtility.formatPercent(shouyilvAverage,2,3)+" count="+count);
			
			GeneralInstances partial=instanceProcessor.getInstancesSubset(policyData, WekaInstanceProcessor.WEKA_ATT_PREFIX +shouyilvPos+" > 0");
			positiveCount=partial.numInstances();
			positiveShouyilvAverage=partial.meanOrMode(shouyilvAttribute);
			System.out.println("\t\t actual positive average="+FormatUtility.formatPercent(positiveShouyilvAverage,2,3)+" count="+positiveCount);
			
			partial=instanceProcessor.getInstancesSubset(policyData, WekaInstanceProcessor.WEKA_ATT_PREFIX +shouyilvPos+" <= 0");
			negativeShouyilvAverage=partial.meanOrMode(shouyilvAttribute);
			System.out.println("\t\t actual negative average="+FormatUtility.formatPercent(negativeShouyilvAverage,2,3)+" count="+(count-positiveCount));
			
			oneDescription=new ShouyilvDescribe(timeRange,policy, count, shouyilvAverage,positiveCount,positiveShouyilvAverage, negativeShouyilvAverage);
			shouyilvDesc.add(oneDescription);
			double percent=oneDescription.getPositiveRatio();
			System.out.println("\t\t actual positive/total="+FormatUtility.formatPercent(percent,2,2));
		}
		System.out.println("...end of data distribution analysis");
		return shouyilvDesc;
	}
	
	

}
