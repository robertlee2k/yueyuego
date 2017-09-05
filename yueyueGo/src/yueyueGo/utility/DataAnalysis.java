package yueyueGo.utility;

import yueyueGo.BackTest;
import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.dataProcessor.InstanceHandler;
import yueyueGo.dataProcessor.WekaInstanceProcessor;
import yueyueGo.databeans.GeneralAttribute;
import yueyueGo.databeans.GeneralInstances;

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

	
	public static void analyzeMarket(String policyGroupName,String[] policyStrings,GeneralInstances fullData)throws Exception{
		int startYearMonth;
		int endYearMonth;
		int yearMonthPos=BaseInstanceProcessor.findATTPosition(fullData,ArffFormat.YEAR_MONTH);
		String attPos=WekaInstanceProcessor.WEKA_ATT_PREFIX + yearMonthPos;
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(fullData);
		
		for (int i=0;i<MARKET_DEFINITION.length;i++){
			startYearMonth=MARKET_DEFINITION[i].getStartYearMonth();
			endYearMonth=MARKET_DEFINITION[i].getEndYearMonth();
			System.out.println(".........now output    ...."+MARKET_DEFINITION[i].toString());
			String splitClause ="(" + attPos + " >= "+ startYearMonth + ") and (" + attPos + " <= " + endYearMonth + ") ";
			GeneralInstances marketData=instanceProcessor.getInstancesSubset(fullData, splitClause);
			analyzeDataDistribution(policyGroupName,policyStrings,marketData);
			System.out.println(".........end of output ...."+MARKET_DEFINITION[i].toString());
		}
	}
	
	/**
	 * @param data
	 */
	public static void analyzeDataDistribution(String policyGroupName,String[] policyStrings,GeneralInstances data) throws Exception {
		System.out.println("start of data distribution analysis.......");
		GeneralAttribute shouyilvAttribute=data.attribute(ArffFormat.SHOUYILV);
		System.out.println(" shouyilv average="+FormatUtility.formatPercent(data.meanOrMode(shouyilvAttribute),2,4)+" count="+data.numInstances());
		//		GeneralAttribute policyGroup=data.attribute(ArffFormat.)
		int shouyilvPos = BaseInstanceProcessor.findATTPosition(data,ArffFormat.SHOUYILV);
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(data);

		int policyPos = BaseInstanceProcessor.findATTPosition(data,policyGroupName);

		for (int j = BackTest.BEGIN_FROM_POLICY; j < policyStrings.length; j++) {
			String policy = policyStrings[j];		
			GeneralInstances policyData=instanceProcessor.getInstancesSubset(data, WekaInstanceProcessor.WEKA_ATT_PREFIX +policyPos+" is '"	+ policy + "'");
			System.out.println("\t shouyilv average for policy["+policy+"]=" +FormatUtility.formatPercent(policyData.meanOrMode(shouyilvAttribute),2,3)+" count="+policyData.numInstances());


			GeneralInstances partial=instanceProcessor.getInstancesSubset(policyData, WekaInstanceProcessor.WEKA_ATT_PREFIX +shouyilvPos+" > 0");
			int positiveCount=partial.numInstances();
			System.out.println("\t\t actual positive average="+FormatUtility.formatPercent(partial.meanOrMode(shouyilvAttribute),2,3)+" count="+positiveCount);

			partial=instanceProcessor.getInstancesSubset(policyData, WekaInstanceProcessor.WEKA_ATT_PREFIX +shouyilvPos+" <= 0");
			int negativeCount=partial.numInstances();
			System.out.println("\t\t actual negative average="+FormatUtility.formatPercent(partial.meanOrMode(shouyilvAttribute),2,3)+" count="+negativeCount);
			double percent=(double)positiveCount/(negativeCount+positiveCount);
			System.out.println("\t\t actual positive/total="+FormatUtility.formatPercent(percent,2,2));
		}
		System.out.println("...end of data distribution analysis");
	}
	
	
	
}
