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


	
	/*
	 * fromYearMonth 待统计数据的起始区间
	 * toYearMonth 待统计数据的结束区间
	 * 针对这个区间的数据，分别统计各种市场情况下的收益率分布
	 */
	public static ArrayList<ShouyilvDescribe> analyzeMarket(String fromYearMonth,String toYearMonth,String policyGroupName,String[] policyStrings,GeneralInstances fullData,String ClassiferName)throws Exception{
		int earliest=Integer.valueOf(fromYearMonth).intValue();
		int latest=Integer.valueOf(toYearMonth).intValue();
		int marketStartMonth;
		int marketEndMonth;
		int yearMonthPos=BaseInstanceProcessor.findATTPosition(fullData,ArffFormat.YEAR_MONTH);
		String attPos=WekaInstanceProcessor.WEKA_ATT_PREFIX + yearMonthPos;
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(fullData);
		ArrayList<ShouyilvDescribe> shouyilvDescriptions=new ArrayList<ShouyilvDescribe>();
			

		for (int i=0;i<MARKET_DEFINITION.length;i++){

			marketStartMonth=MARKET_DEFINITION[i].getStartYearMonth();
			marketEndMonth=MARKET_DEFINITION[i].getEndYearMonth();
			if (marketEndMonth>earliest){ //如果结束区间比最早开始日都早，就不需要统计该区间段了
				if (marketStartMonth<earliest) 
					marketStartMonth=earliest;
				if (marketEndMonth>latest)
					marketEndMonth=latest;

				System.out.println(".........now output    ...."+MARKET_DEFINITION[i].toString());
				String timeRange=MARKET_DEFINITION[i].toString()+"["+marketStartMonth+"-"+marketEndMonth+"]";
				String splitClause ="(" + attPos + " >= "+ marketStartMonth + ") and (" + attPos + " <= " + marketEndMonth + ") ";
				GeneralInstances marketData=instanceProcessor.getInstancesSubset(fullData, splitClause);
				ArrayList<ShouyilvDescribe> descriptions=analyzeDataDistribution(policyGroupName,policyStrings,timeRange,marketData,ClassiferName);
				shouyilvDescriptions.addAll(descriptions);
				System.out.println(".........end of output ...."+MARKET_DEFINITION[i].toString());

			}
		}
		return shouyilvDescriptions;
		
	}
	
	/**
	 * @param data
	 */
	public static ArrayList<ShouyilvDescribe> analyzeDataDistribution(String policyGroupName,String[] policyStrings,String timeRange,GeneralInstances data,String ClassiferName) throws Exception {
		ArrayList<ShouyilvDescribe> shouyilvDesc=new ArrayList<ShouyilvDescribe>();
		System.out.println("start of data distribution analysis.......");
		GeneralAttribute shouyilvAttribute=data.attribute(ArffFormat.SHOUYILV);
		int shouyilvPos = BaseInstanceProcessor.findATTPosition(data,ArffFormat.SHOUYILV);
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(data);
		int policyPos = BaseInstanceProcessor.findATTPosition(data,policyGroupName);
		
		ShouyilvDescribe oneDescription;
		
		oneDescription=describeShouyilv(timeRange, shouyilvAttribute,shouyilvPos,instanceProcessor,ShouyilvDescribe.ALL,data,ClassiferName);
		shouyilvDesc.add(oneDescription);

		for (int j = BackTest.BEGIN_FROM_POLICY; j < policyStrings.length; j++) {
			String policy = policyStrings[j];
			GeneralInstances policyData=instanceProcessor.getInstancesSubset(data, WekaInstanceProcessor.WEKA_ATT_PREFIX +policyPos+" is '"	+ policy + "'");
			
			oneDescription = describeShouyilv(timeRange, shouyilvAttribute, shouyilvPos, instanceProcessor, policy,
					policyData,ClassiferName);

			shouyilvDesc.add(oneDescription);
		}
		System.out.println("...end of data distribution analysis");
		return shouyilvDesc;
	}

	/**
	 * @param timeRange
	 * @param shouyilvAttribute
	 * @param shouyilvPos
	 * @param instanceProcessor
	 * @param policy
	 * @param data
	 * @return
	 * @throws Exception
	 */
	private static ShouyilvDescribe describeShouyilv(String timeRange, GeneralAttribute shouyilvAttribute,
			int shouyilvPos, BaseInstanceProcessor instanceProcessor, String policy, GeneralInstances data,String ClassiferName)
			throws Exception {
		ShouyilvDescribe oneDescription;
		int count;
		double shouyilvAverage;
		int positiveCount;
		double positiveShouyilvAverage;
		double negativeShouyilvAverage;
		shouyilvAverage=data.meanOrMode(shouyilvAttribute);
		count=data.numInstances();
		System.out.println("\t shouyilv average for policy["+policy+"]=" +FormatUtility.formatPercent(shouyilvAverage,2,3)+" count="+count);
		
		GeneralInstances partial=instanceProcessor.getInstancesSubset(data, WekaInstanceProcessor.WEKA_ATT_PREFIX +shouyilvPos+" > 0");
		positiveCount=partial.numInstances();
		positiveShouyilvAverage=partial.meanOrMode(shouyilvAttribute);
		System.out.println("\t\t actual positive average="+FormatUtility.formatPercent(positiveShouyilvAverage,2,3)+" count="+positiveCount);
		
		partial=instanceProcessor.getInstancesSubset(data, WekaInstanceProcessor.WEKA_ATT_PREFIX +shouyilvPos+" <= 0");
		negativeShouyilvAverage=partial.meanOrMode(shouyilvAttribute);
		System.out.println("\t\t actual negative average="+FormatUtility.formatPercent(negativeShouyilvAverage,2,3)+" count="+(count-positiveCount));
		
		oneDescription=new ShouyilvDescribe(timeRange,ClassiferName,policy, count, shouyilvAverage,positiveCount,positiveShouyilvAverage, negativeShouyilvAverage);			
		double percent=oneDescription.getPositiveRatio();
		System.out.println("\t\t actual positive/total="+FormatUtility.formatPercent(percent,2,2));
		return oneDescription;
	}
	
	

}
