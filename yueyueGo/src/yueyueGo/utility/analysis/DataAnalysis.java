package yueyueGo.utility.analysis;

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
			new MarketDefinition("大牛市" , 200601, 200710),
			new MarketDefinition("大熊市" , 200711, 200810),
			new MarketDefinition("反弹市" , 200811, 200907),
			new MarketDefinition("震荡市" , 200908, 201103),
			new MarketDefinition("慢熊市" , 201104, 201211),
			new MarketDefinition("结构市" , 201212, 201406),
			new MarketDefinition("大牛市" , 201407, 201505),
			new MarketDefinition("大熊市" , 201506, 201508),
			new MarketDefinition("小反弹" , 201509, 201512),
			new MarketDefinition("熔断后" , 201601, 201610),
			new MarketDefinition("平衡市" , 201610, 201707),
 * 
 */
public class DataAnalysis {

	public static final MarketDefinition ALL_HISTORY=new MarketDefinition("全时段" , 200001, 209901);
	
	public static final MarketDefinition[] MARKET_DEFINITION={
			new MarketDefinition("大牛市" , 200601, 200710),
			new MarketDefinition("大熊市" , 200711, 200810),
			new MarketDefinition("反弹市" , 200811, 200907),
			new MarketDefinition("震荡市" , 200908, 201103),
			new MarketDefinition("慢熊市" , 201104, 201211),
			new MarketDefinition("结构市" , 201212, 201406),
			new MarketDefinition("大牛市" , 201407, 201505),
			new MarketDefinition("大熊市" , 201506, 201508),
			new MarketDefinition("小反弹" , 201509, 201512),
			new MarketDefinition("熔断后" , 201601, 201610),
			new MarketDefinition("平衡市" , 201610, 201707),
	};


	
	/*
	 * fromYearMonth 待统计数据的起始区间
	 * toYearMonth 待统计数据的结束区间
	 * 针对这个区间的数据，分别统计各种市场情况下的收益率分布
	 */
	public static ShouyilvDescriptiveList analyzeMarket(String identify,String fromYearMonth,String toYearMonth,String policyGroupName,String[] policyStrings,GeneralInstances fullData)throws Exception{
		int earliest=Integer.valueOf(fromYearMonth).intValue();
		int latest=Integer.valueOf(toYearMonth).intValue();
		int marketStartMonth;
		int marketEndMonth;

		System.out.println(" now output the data distribution of results:"+identify);
		//先分析全时间段的并输出打印，下面的明细就不打印了。
		ShouyilvDescriptiveList descriptions=analyzeDataDistribution(policyGroupName,policyStrings,ALL_HISTORY.getExplain(),fullData);
		System.out.println(descriptions.toDescriptionList());
		
		ShouyilvDescriptiveList	shouyilvDescriptions=new ShouyilvDescriptiveList(identify);
		shouyilvDescriptions.mergeDescriptionList(descriptions);	
		int yearMonthPos=BaseInstanceProcessor.findATTPosition(fullData,ArffFormat.YEAR_MONTH);
		String attPos=WekaInstanceProcessor.WEKA_ATT_PREFIX + yearMonthPos;
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(fullData);
		//分段分析并保存
		for (int i=0;i<MARKET_DEFINITION.length;i++){
			marketStartMonth=MARKET_DEFINITION[i].getStartYearMonth();
			marketEndMonth=MARKET_DEFINITION[i].getEndYearMonth();
			if (marketEndMonth>earliest){ //如果结束区间比最早开始日都早，就不需要统计该区间段了
				if (marketStartMonth<earliest) 
					marketStartMonth=earliest;
				if (marketEndMonth>latest)
					marketEndMonth=latest;
				String timeRange=MARKET_DEFINITION[i].getExplain()+"["+marketStartMonth+"-"+marketEndMonth+"]";
				String splitClause ="(" + attPos + " >= "+ marketStartMonth + ") and (" + attPos + " <= " + marketEndMonth + ") ";
				GeneralInstances marketData=instanceProcessor.getInstancesSubset(fullData, splitClause);
				descriptions=analyzeDataDistribution(policyGroupName,policyStrings,timeRange,marketData);
				shouyilvDescriptions.mergeDescriptionList(descriptions);
			}
		}
		return shouyilvDescriptions;
		
	}
	
	/**
	 * @param data
	 */
	public static ShouyilvDescriptiveList analyzeDataDistribution(String policyGroupName,String[] policyStrings,String timeRange,GeneralInstances data) throws Exception {
		ShouyilvDescriptiveList shouyilvDesc=new ShouyilvDescriptiveList("temp");
//		System.out.println("start of data distribution analysis.......");
		GeneralAttribute shouyilvAttribute=data.attribute(ArffFormat.SHOUYILV);
		int shouyilvPos = BaseInstanceProcessor.findATTPosition(data,ArffFormat.SHOUYILV);
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(data);
		int policyPos = BaseInstanceProcessor.findATTPosition(data,policyGroupName);
		
		ShouyilvDescriptive oneDescription;
		

		//先分析整体的
		oneDescription=analyzeShouyilv(timeRange, shouyilvAttribute,shouyilvPos,instanceProcessor,ShouyilvDescriptive.ALL,data);
		shouyilvDesc.addDescription(oneDescription);

		for (int j = BackTest.BEGIN_FROM_POLICY; j < policyStrings.length; j++) {
			String policy = policyStrings[j];
			GeneralInstances policyData=null;
			if ("".equals(policy)){
				policyData=data;
			}else{
				policyData=instanceProcessor.getInstancesSubset(data, WekaInstanceProcessor.WEKA_ATT_PREFIX +policyPos+" = "+ policy );
				//" is '"	+ policy + "'");
				
			}
			oneDescription = analyzeShouyilv(timeRange, shouyilvAttribute, shouyilvPos, instanceProcessor, policy,
					policyData);

			shouyilvDesc.addDescription(oneDescription);
		}
//		System.out.println("...end of data distribution analysis");
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
	private static ShouyilvDescriptive analyzeShouyilv(String timeRange, GeneralAttribute shouyilvAttribute,
			int shouyilvPos, BaseInstanceProcessor instanceProcessor, String policy, GeneralInstances data)
			throws Exception {
		ShouyilvDescriptive oneDescription;
		int count;
		double shouyilvAverage;
		int positiveCount;
		double positiveShouyilvAverage;
		double negativeShouyilvAverage;
		shouyilvAverage=data.meanOrMode(shouyilvAttribute);
		count=data.numInstances();
		GeneralInstances partial=instanceProcessor.getInstancesSubset(data, WekaInstanceProcessor.WEKA_ATT_PREFIX +shouyilvPos+" > 0");
		positiveCount=partial.numInstances();
		positiveShouyilvAverage=partial.meanOrMode(shouyilvAttribute);
		
		partial=instanceProcessor.getInstancesSubset(data, WekaInstanceProcessor.WEKA_ATT_PREFIX +shouyilvPos+" <= 0");
		negativeShouyilvAverage=partial.meanOrMode(shouyilvAttribute);
		
		oneDescription=new ShouyilvDescriptive(timeRange,policy, count, shouyilvAverage,positiveCount,positiveShouyilvAverage, negativeShouyilvAverage);			

		
		return oneDescription;
		

	}
	
	

}
