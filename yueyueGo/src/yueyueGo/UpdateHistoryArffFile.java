package yueyueGo;

import java.text.ParseException;
import java.util.ArrayList;

import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.dataFormat.AvgLineDataFormat;
import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.dataProcessor.InstanceHandler;
import yueyueGo.dataProcessor.WekaInstanceProcessor;
import yueyueGo.databeans.DataInstance;
import yueyueGo.databeans.GeneralAttribute;
import yueyueGo.databeans.GeneralInstance;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaAttribute;
import yueyueGo.databeans.WekaInstances;
import yueyueGo.datasource.DataIOHandler;
import yueyueGo.utility.AppContext;
import yueyueGo.utility.FormatUtility;

public class UpdateHistoryArffFile {


	public static void main(String[] args) {
		try {
			
			//当前所用数据文件格式定义
			ArffFormat currentArffFormat=
//					new MomentumDataFormat();
					new AvgLineDataFormat();  			

			AppContext.createContext(currentArffFormat.m_data_root_directory);	
			
			//重新创建ARFF文件
			callCreateTransInstances(currentArffFormat);
			
//			//用最新的单次交易数据，更新原始的交易数据文件
//			UpdateHistoryArffFile.callRefreshInstances(currentArffFormat);
//
//			//刷新最新月份的模型
//			BackTest worker=new BackTest();
//			worker.init();
//			worker.callRefreshModelUseLatestData();
			
			//校验数据文件
			WekaInstanceProcessor.analyzeDataAttributes(AppContext.getC_ROOT_DIRECTORY()+currentArffFormat.getFullArffFileName());
			convertDataForTensorFlow(currentArffFormat);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	protected static void callRefreshInstances(ArffFormat currentArffFormat) throws Exception {
		String startYearMonth="201601"; //刷新开始月份（包括该月份）
		String endYearMonth="201707";  //刷新结束月份（包括该月份数据）

		
		String newDataFileName=AppContext.getC_ROOT_DIRECTORY()+"sourceData\\group9\\onceyield_group9all20160101_20170630.txt";
		GeneralInstances newRawData = loadDataFromIncrementalCSVFile(newDataFileName,currentArffFormat);
		

		//刷新的Arff文件
		refreshArffFile(startYearMonth,endYearMonth,currentArffFormat,newRawData);

		//以十分之一抽检率抽样检测刷新过的数据
		compareRefreshedInstancesForPeriod(startYearMonth,endYearMonth,currentArffFormat,10);
	}



	protected static void callCreateTransInstances(ArffFormat currentArffFormat) throws Exception {
		//获取格式文件
		GeneralInstances fullFormat=loadFullArffFormat(currentArffFormat);
		
		String targetArffFilename=AppContext.getC_ROOT_DIRECTORY()+currentArffFormat.getFullArffFileName();
		
		System.out.println("Start to create arff. target file ="+targetArffFilename);
		//获取原始CSV文件并处理
		GeneralInstances rawData = mergeSrcTransFiles(currentArffFormat);
		GeneralInstances fullData = convertRawdataToFullsetArff(currentArffFormat,fullFormat, rawData);
		rawData=null; //试图释放内存
	
		//获取tradeDateIndex （从1开始）， 并按其排序
		int tradeDateIndex=BaseInstanceProcessor.findATTPosition(fullData, ArffFormat.TRADE_DATE);
		fullData.sort(tradeDateIndex-1);
	
		//保存Original 文件
		System.out.println("trans arff file sorted, start to save.... number of rows="+fullData.numInstances());
		DataIOHandler.getSaver().SaveDataIntoFile(fullData, targetArffFilename);
		System.out.println("trans arff file saved. ");
	
		//取出前半年的旧数据和当年的新数据作为验证的sample数据
		String splitSampleClause = "( ATT" + ArffFormat.YEAR_MONTH_INDEX + " >= 201606) and ( ATT" + ArffFormat.YEAR_MONTH_INDEX+ " <= 201712) ";
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(fullData);
		GeneralInstances sampleData=instanceProcessor.getInstancesSubset(fullData, splitSampleClause);
		DataIOHandler.getSaver().SaveDataIntoFile(sampleData, getSampleArffFileName(currentArffFormat));
		System.out.println("sample arff file saved. ");
		sampleData=null;//试图释放内存
	
		//根据原始ARFFF文件生成模型的全套文件（详见函数说明）
		generateArffFileSet(currentArffFormat,fullData,fullFormat);
	}

	public static void convertDataForTensorFlow(ArffFormat currentArffFormat) throws Exception{
		System.out.println("loading original history file into memory "  );
		GeneralInstances fullSetData = DataIOHandler.getSuppier().loadDataFromFile(AppContext.getC_ROOT_DIRECTORY()+currentArffFormat.getFullArffFileName());
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(fullSetData);
	
		//对原始旧数据进行处理
		System.out.println("finish  loading original File row : "+ fullSetData.numInstances() + " column:"+ fullSetData.numAttributes());
		//每5年数据分割一个文件
		int yearInterval=6;
		int startYear=2005;
		int endYear=2017;
		String attPos = WekaInstanceProcessor.WEKA_ATT_PREFIX + ArffFormat.YEAR_MONTH_INDEX;

		for (int i=startYear;i<endYear;i=i+yearInterval){
			int fromPeriod=i*100+01;
			int toPeriod=(i+yearInterval-1)*100+12;
			if (toPeriod>201709) toPeriod=201709;
			String splitClause="(" + attPos + " >= "+ fromPeriod + ") and (" + attPos + " <= "	+ toPeriod + ") ";
			System.out.println("start to split fulset for: "+ splitClause);
			GeneralInstances oneData=instanceProcessor.getInstancesSubset(fullSetData,splitClause);
			String fileName=AppContext.getC_ROOT_DIRECTORY()+"\\sourceData\\tensorFlowData("+fromPeriod+"-"+toPeriod+").csv";
			DataIOHandler.getSaver().saveCSVFile(oneData, fileName);
		}

	}
	
	/**
		 * 从原始CSV数据集中合并文件，返回原始的Arff
		 * @return
		 * @throws Exception
		 * @throws IllegalStateException
		 */
		private static GeneralInstances mergeSrcTransFiles(ArffFormat currentArffFormat) throws Exception{
			
	//		//动量
	//		String sourceFile=AppContext.getC_ROOT_DIRECTORY()+"sourceData\\group9\\onceyield_group9all_momentum2005_2017.txt";
	//		GeneralInstances fullData = loadDataFromIncrementalCSVFile(sourceFile);			
			
			//传统
			String sourceFilePrefix=AppContext.getC_ROOT_DIRECTORY()+"sourceData\\group10\\v_onceyield_group10all_";
			String fileSurfix=".txt"; 
//			String fileSurfix=".csv";
			GeneralInstances fullData = loadDataFromIncrementalCSVFile(sourceFilePrefix+"2005-2009"+fileSurfix,currentArffFormat);

			GeneralInstances addData = loadDataFromIncrementalCSVFile(sourceFilePrefix+"2010-2013"+fileSurfix,currentArffFormat);
			BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(fullData);
			fullData=instanceProcessor.mergeTwoInstances(fullData, addData);
			System.out.println("merged one File,now row : "+ fullData.numInstances() + " column:"+ fullData.numAttributes());
			addData = loadDataFromIncrementalCSVFile(sourceFilePrefix+"2014-2017"+fileSurfix,currentArffFormat);
			fullData=instanceProcessor.mergeTwoInstances(fullData, addData);
			System.out.println("merged one File,now row : "+ fullData.numInstances() + " column:"+ fullData.numAttributes());
	
	//		int startYear=2013;
	//		int endYear=2015;
	//		for (int i=startYear;i<=endYear;i=i+2){
	//			String fName=sourceFilePrefix+i+"_"+(i+1)+".txt";
	//			addData = loadDataFromIncrementalCSVFile(fName);
	//			fullData=instanceProcessor.mergeTwoInstances(fullData, addData);
	//			System.out.println("merged one File"+fName+" ,now row : "+ fullData.numInstances() + " column:"+ fullData.numAttributes());
	//		}
			return fullData;
		}

	// 从增量的交易CSV文件中加载数据
	protected static GeneralInstances loadDataFromIncrementalCSVFile(String fileName,ArffFormat currentArffFormat) throws Exception{ 
		return DataIOHandler.getSuppier().loadDataWithFormatFromCSVFile(fileName,currentArffFormat.m_arff_data_full);
	}
	
	//从文件中读取指定区间的数据，刷新原有数据，再用processHistoryData生成有计算字段之后的数据
	final protected static void refreshArffFile(String startYearMonth, String endYearMonth,ArffFormat currentArffFormat,GeneralInstances newRawData) throws Exception {
		System.out.println("loading original history file into memory "  );
		GeneralInstances originalData = DataIOHandler.getSuppier().loadDataFromFile(AppContext.getC_ROOT_DIRECTORY()+currentArffFormat.getFullArffFileName());
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(originalData);
	
	
		//对原始旧数据进行处理
		System.out.println("finish  loading original File row : "+ originalData.numInstances() + " column:"+ originalData.numAttributes());
		int originInstancesNum=originalData.numInstances();
		System.out.println ("refreshing period from: "+ startYearMonth+" to:" +endYearMonth+ "while fullsize ="+ originInstancesNum);
		//将原始文件里属于该时间段的数据保留下来供后期对比
		//TODO ATT should be processed
		String splitRemovedDataClause = "( ATT" + ArffFormat.YEAR_MONTH_INDEX + " >= " + startYearMonth+ ") and ( ATT" + ArffFormat.YEAR_MONTH_INDEX+ " <= " + endYearMonth + ") ";
		GeneralInstances removedData=instanceProcessor.getInstancesSubset(originalData, splitRemovedDataClause);
		int removedNumber=removedData.numInstances();
		System.out.println("number of rows removed = "+ removedNumber+" Now saving it to -removed.arff for future comparision");
		DataIOHandler.getSaver().SaveDataIntoFile(removedData,getRemovedArffFileName(currentArffFormat) );
		//释放内存
		removedData=null;
		//下面才是真正的对原始数据的删除处理
		//将原始文件里不属于该时间段的数据过滤出来（相当于把属于该段时间的原有数据删除）
		//TODO ATT should be processed
		String splitCurrentYearClause = "( ATT" + ArffFormat.YEAR_MONTH_INDEX + " < " + startYearMonth+ ") or ( ATT" + ArffFormat.YEAR_MONTH_INDEX+ " > " + endYearMonth + ") ";
		originalData=instanceProcessor.getInstancesSubset(originalData, splitCurrentYearClause);
		int filteredNumber=originalData.numInstances() ;
		if (removedNumber!=(originInstancesNum-filteredNumber)){
			System.err.println("删除的数据数量与过滤的数据数量不一致： 删除的="+removedNumber+ "过滤掉的="+ (originInstancesNum-filteredNumber));
		}
		System.out.println("number of rows filtered out = "+ (originInstancesNum-filteredNumber));
	
	
		//下面开始处理读入的新CSV数据
		processDateColumns(newRawData);
		//获取格式文件
		GeneralInstances fullFormat=loadFullArffFormat(currentArffFormat);
		GeneralInstances newArffData = convertRawdataToFullsetArff(currentArffFormat,fullFormat,newRawData);
		//试图释放内存
		newRawData=null;


		
		//比较要更新的数据与原有数据文件的格式，以及输出更新数量等信息。
		System.out.println("verifying new data format , you should read this .... "+ originalData.equalHeadersMsg(newArffData));
		int newDataNumber=newArffData.numInstances();
		System.out.println("number of new rows added or updated= "+ newDataNumber);
		if (newDataNumber==0){
			System.err.println("attention!!  No records have been retrieved from the new file. ");
		}
		if (newDataNumber < removedNumber){
			System.err.println("attention!!  new(refreshed) records="+newDataNumber +" are less than the removed records="+removedNumber+". normally this indicates something wrong with the new data");
		}
	
		//合并旧数据和新数据文件
		originalData=instanceProcessor.mergeTwoInstances(originalData, newArffData);
		System.out.println("number of refreshed dataset = "+originalData.numInstances());
	
	
		//保险起见把新数据按日期重新排序，虽然这样比较花时间，但可以确保日后处理时按tradeDate升序。
		int pos=BaseInstanceProcessor.findATTPosition(originalData, ArffFormat.TRADE_DATE);
		originalData.sort(pos);
		System.out.println("refreshed arff file sorted, start to save.... number of rows="+originalData.numInstances());
		DataIOHandler.getSaver().SaveDataIntoFile(originalData, currentArffFormat.getFullArffFileName());
		System.out.println("refreshed arff file saved. ");
	
		//根据原始ARFFF文件生成模型的全套文件（详见函数说明）
		generateArffFileSet(currentArffFormat,originalData,fullFormat);
	}

	/**
	 * @param fullFormat
	 * @param rawData
	 * @param instanceProcessor
	 * @return
	 * @throws ParseException
	 * @throws Exception
	 */
	private static GeneralInstances convertRawdataToFullsetArff(ArffFormat currentArffFormat,GeneralInstances fullFormat,GeneralInstances rawData) throws ParseException, Exception {

		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(rawData);
		//处理所有的日期字段，并插入yearmonth
		processDateColumns(rawData);
		//根据format 处理各种nominal字段，读入后转换为numeric
		GeneralInstances fullData=instanceProcessor.calibrateAttributes(rawData, fullFormat,currentArffFormat.convertNominalToNumeric);

		return fullData;
	}


	/**
	 * @param formatFileName
	 * @return
	 * @throws Exception
	 */
	private static GeneralInstances loadFullArffFormat(ArffFormat currentArffFormat) throws Exception {
		String formatFileName=AppContext.getC_ROOT_DIRECTORY()+currentArffFormat.getFullFormatFileName();
//				+"fullFormat-"+currentArffFormat.m_data_file_prefix+".arff";
		System.out.println("start to load format file="+formatFileName);
		GeneralInstances fullFormat=DataIOHandler.getSuppier().loadDataFromFile(formatFileName);

		return fullFormat;
	}

	/*
	 * 定义更新Arff文件时删除数据的文件名（这个未在ArffFormat中定义）
	 */
	protected static String getRemovedArffFileName(ArffFormat currentArffFormat){
		return AppContext.getC_ROOT_DIRECTORY()+currentArffFormat.m_data_file_prefix+"("+ArffFormat.CURRENT_FORMAT+")-removed.arff";
	}

	/*
	 * 定义Sample数据的的文件名（这个未在ArffFormat中定义）
	 */
	protected static String getSampleArffFileName(ArffFormat currentArffFormat){
		return AppContext.getC_ROOT_DIRECTORY()+currentArffFormat.m_data_file_prefix+"("+ArffFormat.CURRENT_FORMAT+")-sample.arff";
	}

	/**
	 * newData会在这里被改变（插入yearMonth字段）
	 * @param newData
	 * @throws ParseException
	 */
	final protected static void processDateColumns(GeneralInstances newData)
			throws ParseException {
		int yearMonthIndex=BaseInstanceProcessor.findATTPosition(newData, ArffFormat.ID); //在ID之后插入
		newData.insertAttributeAt(new WekaAttribute(ArffFormat.YEAR_MONTH), yearMonthIndex);
		//重新计算yearmonth
		GeneralAttribute tradeDateAtt=newData.attribute(ArffFormat.TRADE_DATE);
		GeneralAttribute mcDateAtt=newData.attribute(ArffFormat.SELL_DATE);
		GeneralAttribute dataDateAtt=newData.attribute(ArffFormat.DATA_DATE);
		GeneralAttribute yearMonthAtt=newData.attribute(ArffFormat.YEAR_MONTH);
		GeneralInstance curr;
		String tradeDate;
		double ym;
		for (int i=0;i<newData.numInstances();i++){
			curr=newData.instance(i);
			tradeDate=curr.stringValue(tradeDateAtt);
			//设置yearmonth
			ym=FormatUtility.parseYearMonth(tradeDate);
			curr.setValue(yearMonthAtt, ym);
			//修改日期格式
			curr.setValue(tradeDateAtt, FormatUtility.convertDate(tradeDate));
			curr.setValue(mcDateAtt, FormatUtility.convertDate(curr.stringValue(mcDateAtt)));
			curr.setValue(dataDateAtt, FormatUtility.convertDate(curr.stringValue(dataDateAtt)));
		}
	}


	/**
	 * 
	 * 根据原始ARFFF文件生成模型的全套文件，包括以下几类：
	 * 1. 用于预测数据转换和校验的格式文件
	 * 2. 回测建模的Arff文件
	 * 3. 用于计算收益率的Arff文件
	 * @param originFileName
	 * @param fullSetData
	 * @throws Exception
	 */
	private static void generateArffFileSet(ArffFormat arffFormat,
			GeneralInstances fullSetData,GeneralInstances fullFormat) throws Exception {
	
		
		//从原始格式数据中去除与训练无关的字段（因为要保留那些Nominal的属性为每日预测转换）
		GeneralInstances dailyFormat=arffFormat.prepareTransData(fullFormat);
		DataIOHandler.getSaver().SaveDataIntoFile(dailyFormat, AppContext.getC_ROOT_DIRECTORY()+arffFormat.getDailyFormatFileName());

		// 从原始格式中获取用于计算收益率的数据并保存
		GeneralInstances left=arffFormat.getTransLeftPartFromAllTransaction(fullSetData);
		String leftFileName=AppContext.getC_ROOT_DIRECTORY()+arffFormat.getLeftDataFileName();
		DataIOHandler.getSaver().SaveDataIntoFile(left, leftFileName);
		System.out.println("history Data left File saved: "+leftFileName );
		left=null; //试图释放内存
		
		//从转换后的数据中去除与训练无关的字段
		GeneralInstances result=arffFormat.prepareTransData(fullSetData);
		//保存训练用的文件
		DataIOHandler.getSaver().SaveDataIntoFile(result, AppContext.getC_ROOT_DIRECTORY()+arffFormat.getTrainingDataFileName());

//		//保存训练用的format，用于做日后的校验 
//		GeneralInstances format=new WekaInstances(result,0);
//		DataIOHandler.getSaver().SaveDataIntoFile(format, AppContext.getC_ROOT_DIRECTORY()+arffFormat.getTrainingFormatFileName());
	
		System.out.println("full Set Data File saved "  );
	
	}

	


	// replaced by compareRefreshedInstances which is more effecient 
	//此方法用于比较原始文件和refreshed文件之间的差异
	// 根据原始文件的格式ORIGINAL_TRANSACTION_ARFF_FORMAT
	//TRADE_DATE （2）,"code"（3） 和 SELECTED_MA（9） 是唯一性键值
	// checkSample 是指抽样率，如果是1表示每行都要比较，如果是100，则为每100类记录中抽取一条比较 
	final protected static void compareRefreshedInstancesForPeriod(String startYearMonth, String endYearMonth,ArffFormat currentArffFormat,int checkSample) throws Exception {
		
	
		String splitSampleClause = "( ATT" + ArffFormat.YEAR_MONTH_INDEX + " >= " + startYearMonth + ") and ( ATT" + ArffFormat.YEAR_MONTH_INDEX+ " <= "	+ endYearMonth + ") ";
		System.out.println("start to compare refreshed data, try to get removed sample data using clause: "+splitSampleClause);
		GeneralInstances removedOriginalData=DataIOHandler.getSuppier().loadDataFromFile(getRemovedArffFileName(currentArffFormat));
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(removedOriginalData);
		removedOriginalData=instanceProcessor.getInstancesSubset(removedOriginalData, splitSampleClause);
	
		int originDataSize=removedOriginalData.numInstances();
		System.out.println("loaded removed file into memory, number= "+originDataSize);
		GeneralInstances refreshedData=DataIOHandler.getSuppier().loadDataFromFile(currentArffFormat.getFullArffFileName());
		refreshedData=instanceProcessor.getInstancesSubset(refreshedData, splitSampleClause);
	
		int refreshedDataSize=refreshedData.numInstances();
		System.out.println("loaded refreshed file into memory, number= "+refreshedDataSize);
		System.out.println("compare originData and newData header,result is :"+removedOriginalData.equalHeadersMsg(refreshedData));
	
		//获取tradeDateIndex （从1开始）， 并按其排序
		int tradeDateIndex=BaseInstanceProcessor.findATTPosition(removedOriginalData, ArffFormat.TRADE_DATE);
		removedOriginalData.sort(tradeDateIndex-1);
	
		System.out.println("data sorted on tradeDate");
	
		int codeIndex=BaseInstanceProcessor.findATTPosition(removedOriginalData,ArffFormat.CODE);
		int maIndex=BaseInstanceProcessor.findATTPosition(removedOriginalData, currentArffFormat.m_policy_group); //对于短线策略，这里的值是-1
	
		GeneralInstances originDailyData=null;
		GeneralInstances refreshedDailyData=null;
		GeneralInstance originRow=null;
		GeneralInstance refreshedRow=null;
		String tradeDate=null;
		String code=null;
		int cursor=0;
		int rowCompare=0;
		int rowDiffer=0;
		int rowAdded=0;
		String lastDate=null;
		String lastCode=null;
	
		
		while (cursor<refreshedDataSize){
	
	
			//从刷新数据全集中取出某天某只股票的数据，然后进行比对
			tradeDate=refreshedData.instance(cursor).stringValue(tradeDateIndex-1);
			code=refreshedData.instance(cursor).stringValue(codeIndex-1);
			if (tradeDate.equals(lastDate) && code.equals(lastCode)){
				cursor+=checkSample;
				continue;
			}else{
				lastDate=tradeDate;
				lastCode=code;
			}
	
			//TODO ATT should be replaced
			originDailyData=instanceProcessor.getInstancesSubset(removedOriginalData, "(ATT"+tradeDateIndex +" is '"+ tradeDate+"') and (ATT"+codeIndex+" is '"+code+"')");
			refreshedDailyData=instanceProcessor.getInstancesSubset(refreshedData, "(ATT"+tradeDateIndex +" is '"+ tradeDate+"') and (ATT"+codeIndex+" is '"+code+"')");
	
			int refreshedDailyDataSize=refreshedDailyData.numInstances();
			int originDailyDataSize=originDailyData.numInstances();
			//对于均线策略，将同一天的数据按均线排序,对于短线策略，这里什么都不用做。
			if (maIndex>0){
				originDailyData.sort(maIndex-1);
				refreshedDailyData.sort(maIndex-1);
			}
			
			//如果新旧数据同时存在，记录条数应该一致 （也就是说新数据要么是完全新增的日期，要么需要整个替换之前的旧数据）
			if (refreshedDailyDataSize==originDailyDataSize || originDailyDataSize==0){
				//System.out.println("ready to compare data on date "+ tradeDate+" for code:"+code+" origin/refreshed data number= "+originDailyDataSize);
				//按天、股票代码、均线对比
				for(int i=0;i<refreshedDailyDataSize;i++){
					if (originDailyDataSize>0){ //新旧数据同时存在，比较新旧数据
						originRow=originDailyData.instance(i);
						refreshedRow=refreshedDailyData.instance(i);
						if (compareRefreshedRow(maIndex, originRow,refreshedRow, tradeDate, code)==false){
							rowDiffer++;
						}
						rowCompare++;
						if ((rowCompare % 1000)==0) {
							System.out.println("number of instances compared : "+rowCompare);
						}
					}else{ // 只有新数据，把新数据输出
						rowAdded++;
						if ((rowAdded % 1000)==0){
							System.out.println("number of new rows added="+ rowAdded+ "current is @"+tradeDate+"@"+code);
						}
					}
				}// end for i
				//进到下一个日期
				cursor+=refreshedDailyDataSize+checkSample-1;
			}else{
				System.out.println("daily data size in origin and refreshed data are not same on date "+tradeDate+" for code:"+code+" origin/refreshed data number= "+originDailyDataSize+" vs. "+refreshedDailyDataSize);
				
				for(int i=0;i<originDailyDataSize;i++){
					System.out.println("原数据第【"+(i+1)+"】行："+originDailyData.instance(i).toString());
				}
				for(int i=0;i<refreshedDailyDataSize;i++){
					System.out.println("新数据第【"+(i+1)+"】行："+refreshedDailyData.instance(i).toString());
				}
				rowDiffer+=originDailyDataSize-refreshedDailyDataSize;
				cursor+=checkSample;
			}			
		}// end while
		System.out.println("mission completed, rowSame="+(rowCompare-rowDiffer) +"row differ="+rowDiffer+"row Added="+rowAdded);
		System.out.println("number of original data="+originDataSize+ " vs. rowCompared"+(rowCompare));
		System.out.println("number of refreshed data="+refreshedDataSize+ " vs. rowCompared+rowAdded"+(rowCompare+rowAdded));
	}

	/**
	 * @param maIndex
	 * @param originRow
	 * @param refreshedRow
	 * @param tradeDate
	 * @param code
	 * @param rowDiffer
	 * @return
	 * @throws IllegalStateException
	 */
	final protected static boolean compareRefreshedRow(int maIndex, GeneralInstance originRow,
			GeneralInstance refreshedRow, String tradeDate, String code)
					throws IllegalStateException {
		boolean rowSame=true;
		double originMa;
		double refreshedMa;
		
		//对于均线策略，这里要比较MA，
		if(maIndex>0){
			originMa=originRow.value(maIndex-1);
			refreshedMa=refreshedRow.value(maIndex-1);
		}else{ //对于短线策略，这两个值是无意义的，设为0认为相等
			originMa=0;
			refreshedMa=originMa;
		}
		if (originMa!=refreshedMa){
			System.out.println("--------------------------probabily original data error---------------------------");
			System.out.println("daily origin and refreshed MA are not same on date "+tradeDate+" for code:"+code+" origin/refreshed MA= "+originMa+" vs. "+refreshedMa);
			System.out.println(originRow.toString());
			System.out.println(refreshedRow.toString());
			rowSame=false;
		}else{
			//从Bias5开始比较
			int startingPoint=BaseInstanceProcessor.findATTPosition(new WekaInstances(originRow.dataset()), ArffFormat.BIAS5);
			for (int n = startingPoint; n < originRow.numAttributes() ; n++) { //跳过左边的值 
	
				GeneralAttribute originAtt = originRow.attribute(n);
				GeneralAttribute refresedAtt=refreshedRow.attribute(n);
				if (originAtt.isNominal() || originAtt.isString()) {
					String originValue=originRow.stringValue(n);
					String refreshedValue=refreshedRow.stringValue(n);
					if (originValue.equals(refreshedValue)==false){
						String originAttName=originAtt.name();
						String refreshedAttName=refresedAtt.name();
						System.out.println("@"+tradeDate+"@"+code+" Attribute value is not the same. value= "+ originValue+" vs."+refreshedValue+" @ "+originAttName + " & "+ refreshedAttName);;
						rowSame=false;
					}
				} else if (originAtt.isNumeric()) {
					double originValue=originRow.value(n);
					double refreshedValue=refreshedRow.value(n);
					double difference=FormatUtility.compareDouble(originValue,refreshedValue);
	
					if ( difference!=0 ){
						String originAttName=originAtt.name();
						String refreshedAttName=refresedAtt.name();
						System.out.println("@"+tradeDate+"@"+code+" Attribute value is not the same. value= "+ originValue+" vs."+refreshedValue+" @ "+originAttName + " & "+ refreshedAttName+ " difference= "+difference);;
						rowSame=false;
	
					}
				} else {
					throw new IllegalStateException("Unhandled attribute type!");
				}
			}//end for n;
		}//end else
		return rowSame;
	}

	/**
	 * 合并两个Instances集（从txt文件读取） ，此处的合并是纵向合并，两个instances需要是同样格式的 
	 * @param firstFile
	 * @param secondFile
	 * @return
	 * @throws Exception
	 * @throws IllegalStateException
	 */
	@Deprecated
	final protected static GeneralInstances mergeExtDataFromTwoFiles(String firstFile,
			String secondFile,String[] verifyFormat) throws Exception, IllegalStateException {
		GeneralInstances extData=DataIOHandler.getSuppier().loadDataFromExtCSVFile(firstFile,verifyFormat);
		GeneralInstances extDataSecond=DataIOHandler.getSuppier().loadDataFromExtCSVFile(secondFile,verifyFormat);
	
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(extData);
		return instanceProcessor.mergeTwoInstances(extData, extDataSecond);
	}

	//数据必须是以ID排序的。
	@Deprecated
	final protected static GeneralInstances mergeTransactionWithExtension(GeneralInstances transData,GeneralInstances extData,String[] extDataFormat, String[] extArffCRC) throws Exception{
	
		//找出transData中的所有待校验字段
		GeneralAttribute[] attToCompare=new WekaAttribute[extArffCRC.length];
		for (int i=0;i<extArffCRC.length;i++){
			attToCompare[i]=transData.attribute(extArffCRC[i]);
		}
		GeneralInstances mergedResult=prepareMergedFormat(new WekaInstances(transData,0), new WekaInstances(extData,0),extArffCRC);
		System.out.println("merged output column number="+mergedResult.numAttributes());
	
		//开始准备合并
		if (transData.numInstances()!=extData.numInstances()){
			System.out.println("=============warning================= transData number ="+transData.numInstances() +" while extData="+extData.numInstances());
		}
	
		int leftProcessed=0;
		int rightProcessed=0;
		GeneralInstance leftCurr;
		GeneralInstance rightCurr;
		DataInstance newData;
	
		while (leftProcessed<transData.numInstances() && rightProcessed<extData.numInstances()){	
			leftCurr=transData.instance(leftProcessed);
			rightCurr=extData.instance(rightProcessed);
			double leftID = leftCurr.value(0);
			double rightID = rightCurr.value(0);
	
			if (leftID<rightID){ // 如果左边有未匹配的数据
				System.out.println("unmatched left====="+ leftCurr.toString());
				System.out.println("current right ====="+ rightCurr.toString());
				System.out.println("leftProcessed="+leftProcessed+" rightProcessed="+rightProcessed);
				leftProcessed++;
				continue;
			}else if (leftID>rightID){ // 如果右边有未匹配的数据
				System.out.println("unmatched right===="+ rightCurr.toString());	
				System.out.println("current left  ====="+ leftCurr.toString());
				System.out.println("leftProcessed="+leftProcessed+" rightProcessed="+rightProcessed);
				rightProcessed++;
				continue;
			}else if (leftID==rightID ){//找到相同ID的记录了
				//先对所有的冗余数据进行校验
				for (int j=0;j<extArffCRC.length;j++){
					GeneralAttribute att = attToCompare[j];
					if (att.isNominal() || att.isString()) {
						String leftLabel = leftCurr.stringValue(att);
						String rightLabel=rightCurr.stringValue(j);
						if (leftLabel.equals(rightLabel)){
							//do nothing
						}else{
							//可能是左边的旧数据为空
							if ("?".equals(leftLabel)){
								System.err.println("warning. leftLabel was null while right label is not null. at attribute:"+extArffCRC[j]+ " left= "+leftLabel+" while right= "+rightLabel +" @id="+leftID);
								System.err.println("current left  ====="+ leftCurr.toString());
								System.err.println("current right===="+ rightCurr.toString());
							}else{
								//可能是因为输入文件的date格式不一样，尝试转换一下
								try {
									rightLabel=FormatUtility.convertDate(rightLabel);			
								} catch (ParseException pe) {
									System.err.println("leftLabel doesn't equal to right label,tried to convert date but failed,seems it is not a date string. at attribute:"+extArffCRC[j]+ " left= "+leftLabel+" while right= "+rightLabel +" @id="+leftID);
									System.err.println("current left  ====="+ leftCurr.toString());
									System.err.println("current right===="+ rightCurr.toString());
									throw new Exception(pe);
								}
	
								if (leftLabel.equals(rightLabel)==false){
									System.out.println("current left====="+ leftCurr.toString());
									System.out.println("current right===="+ rightCurr.toString());		
									throw new Exception("data not equal! at attribute:"+extArffCRC[j]+ " left= "+leftLabel+" while right= "+rightLabel +" @id="+leftID);
								}
							}
						}
					} else if (att.isNumeric()) {
						double leftValue=leftCurr.value(att);
						double rightValue=rightCurr.value(j);
						if ( FormatUtility.compareDouble(leftValue,rightValue)==0 || Double.isNaN(leftValue) && Double.isNaN(rightValue)){
							// do nothing
						}else {
							System.out.println("current left====="+ leftCurr.toString());
							System.out.println("current right===="+ rightCurr.toString());						
							System.out.println("=========data not equal! at attribute:"+extArffCRC[j]+ " left= "+leftValue+" while right= "+rightValue);							
						}
					} else {
						throw new IllegalStateException("Unhandled attribute type!");
					}
	
				}//end for j
	
				newData=new DataInstance(mergedResult.numAttributes());
				newData.setDataset(mergedResult);
	
				//先拷贝transData中除了classvalue以外的数据
				int srcStartIndex=0;
				int srcEndIndex=leftCurr.numAttributes()-2;//注意这里的classvalue先不拷贝 
				int targetStartIndex=0;
				BaseInstanceProcessor.copyToNewInstance(leftCurr, newData, srcStartIndex, srcEndIndex,targetStartIndex);
	
				//再拷贝extData中除校验数据之外的数据				
				srcStartIndex=extArffCRC.length;
				srcEndIndex=extArffCRC.length+extDataFormat.length-1;
				targetStartIndex=leftCurr.numAttributes()-1; //接着拷贝
				BaseInstanceProcessor.copyToNewInstance(rightCurr, newData, srcStartIndex, srcEndIndex,targetStartIndex);
	
				//再设置classValue
				srcStartIndex=leftCurr.numAttributes()-1;
				srcEndIndex=leftCurr.numAttributes()-1;
				targetStartIndex=newData.numAttributes()-1;
				BaseInstanceProcessor.copyToNewInstance(leftCurr, newData, srcStartIndex, srcEndIndex,targetStartIndex);
	
				mergedResult.add(newData);
				leftProcessed++;
				rightProcessed++;
				if (leftProcessed % 100000 ==0){
					System.out.println("number of results processed. left="+ leftProcessed+ " right="+rightProcessed);
				}
	
			}// end if leftCurr
	
		}//end for
		if (rightProcessed!=extData.numInstances()){
			System.out.println("not all data in extData have been processed , processed= "+rightProcessed+" ,while total="+extData.numInstances());
		}else {
			System.out.println("number of data merged and processed: "+ rightProcessed+" origin data columns="+transData.numAttributes()+" new data columns="+mergedResult.numAttributes());
		}
	
	
		return mergedResult;
	}

	/**
	 * Merges two sets of Instances together（这里仅生成空格式不实际合并数据）. The resulting set will have all the
	 * attributes of the first set plus all the attributes of the second set. 
	 * 将第一个Instances的classvalue（最后一列）作为合并后的classvalue
	 * 
	 */
	@Deprecated
	private static GeneralInstances prepareMergedFormat(GeneralInstances transData, GeneralInstances extData,String[] extArffCRC) {
		// Create the vector of merged attributes
		ArrayList<GeneralAttribute> newAttributes = new ArrayList<GeneralAttribute>(transData.numAttributes() +
				extData.numAttributes()-extArffCRC.length);
		ArrayList<GeneralAttribute> transAtts = transData.getAttributeList();
		
		for (GeneralAttribute baseAttribute : transAtts) {
			newAttributes.add(baseAttribute.copy());// Need to copy because indices will change.	
		}	
		
		ArrayList<GeneralAttribute> extAtts = extData.getAttributeList();
		for (GeneralAttribute baseAttribute : extAtts) {
			//去掉冗余字段，将有效数据字段加入新数据集
			// Need to copy because indices will change.
			GeneralAttribute att=baseAttribute.copy();
			if (att.index()>= extArffCRC.length){
				newAttributes.add(att);
			}
		}
	
		//将第一个数据集里的Class属性作为新数据集的class属性
		GeneralAttribute classAttribute=transData.classAttribute().copy();// Need to copy because indices will change.
		newAttributes.add(classAttribute);
	
		// Create the set of mergedInstances
		GeneralInstances merged = new WekaInstances(transData.relationName(), newAttributes, 0);
		merged.setClassIndex(merged.numAttributes()-1);
	
		return merged;
	}


}
