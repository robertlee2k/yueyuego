package yueyueGo.fullModel;



import java.io.IOException;

import yueyueGo.ArffFormat;
import yueyueGo.UpdateHistoryArffFile;
import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.dataProcessor.InstanceHandler;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaInstances;
import yueyueGo.datasource.DataIOHandler;
import yueyueGo.utility.AppContext;

public class UpdateHistoryArffFullModel extends UpdateHistoryArffFile {
	
	public static void main(String[] args) {
		try {
			BackTestFullModel fullModelWorker=new BackTestFullModel();
			fullModelWorker.init();

			//刷新增量数据
			UpdateHistoryArffFullModel.callRefreshInstancesFullModel();
			
			//短线模型刷新最新月评估数据
			fullModelWorker.callRefreshFullModelUseLatestData();
			
			
			//短线模型生成初始全量数据
//			UpdateHistoryArffFullModel.createFullModelInstances();
			
			//短线模型合并新的属性
//			UpdateHistoryArffFullModel.callMergeExtDataForFullModel();			

		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	protected static void callRefreshInstancesFullModel() throws Exception {
		String startYearMonth="201609";
		String endYearMonth="201612";
		

		String originFilePrefix=AppContext.getC_ROOT_DIRECTORY()+ArffFormatFullModel.FULL_MODEL_ARFF_PREFIX;
		
		String newDataFileName=AppContext.getC_ROOT_DIRECTORY()+"sourceData\\group5\\onceyield_group5all_optional20160901_20161031.txt";		
		GeneralInstances newData = loadDataFromFullModelCSVFile(newDataFileName);
		//刷新的Arff文件
		refreshArffFile(startYearMonth,endYearMonth,originFilePrefix,newData);
		//为原始的历史文件Arff添加计算变量，并分拆。
		processHistoryFileFullModel();

		//以百分之一抽检率检查未被刷新数据（抽样部分）
		int lastYear=Integer.valueOf(startYearMonth.substring(0, 4))-1; 
		compareRefreshedInstancesForPeriod(String.valueOf(lastYear)+"11",String.valueOf(lastYear)+"12",originFilePrefix,100);
		//以五分之一抽检率抽样检测刷新过的数据
		compareRefreshedInstancesForPeriod(startYearMonth,endYearMonth,originFilePrefix,5);
	}
	
	protected static void callCreateFullModelInstances() throws Exception {

		String arffFileName=AppContext.getC_ROOT_DIRECTORY()+ArffFormatFullModel.FULL_MODEL_ARFF_PREFIX;
		GeneralInstances rawData = mergeSrcFullModelFiles();
		
		//处理所有的日期字段，并插入yearmonth
		processDateColumns(rawData);

		//处理各种nominal字段
		GeneralInstances fullData=DataIOHandler.getSuppier().loadDataFromFile(AppContext.getC_ROOT_DIRECTORY()+"fullModelFormat.arff");
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(rawData);
		instanceProcessor.calibrateAttributes(rawData, fullData);
		rawData=null; //试图释放内存
		
		//获取tradeDateIndex （从1开始）， 并按其排序
		int tradeDateIndex=BaseInstanceProcessor.findATTPosition(fullData, ArffFormatFullModel.TRADE_DATE);
		fullData.sort(tradeDateIndex-1);

		System.out.println("FULLMODEL...trans arff file sorted, start to save.... number of rows="+fullData.numInstances());
		DataIOHandler.getSaver().SaveDataIntoFile(fullData, arffFileName+".arff");
		System.out.println("FULLMODEL...trans arff file saved. ");
		
		//取出前半年的旧数据和当年的新数据作为验证的sample数据
		String splitSampleClause = "( ATT" + ArffFormatFullModel.YEAR_MONTH_INDEX + " >= 201506) and ( ATT" + ArffFormatFullModel.YEAR_MONTH_INDEX+ " <= 201612) ";
		GeneralInstances sampleData=instanceProcessor.getInstancesSubset(fullData, splitSampleClause);
		DataIOHandler.getSaver().SaveDataIntoFile(sampleData, arffFileName+"-sample.arff");
		System.out.println("FULLMODEL...sample arff file saved. ");
		sampleData=null;//试图释放内存
		
		//生成模型全套文件
		generateArffFileSetFullModel(arffFileName,fullData);
	}
	
	//这个函数是将原有的历史arff文件数据（比如说只有第一二三组）合并上新的数据列
	protected static void callMergeExtDataForFullModel() throws Exception{
		String file1=null;
		String file2=null;
		GeneralInstances extData=null;
		GeneralInstances extData2=null;
	
		file1=AppContext.getC_ROOT_DIRECTORY()+"\\sourceData\\自选股第五组增量\\onceyield_optional_hv_update_2005_2008.txt";
		file2=AppContext.getC_ROOT_DIRECTORY()+"\\sourceData\\自选股第五组增量\\onceyield_optional_hv_update_2009_2012.txt";
		extData = mergeExtDataFromTwoFiles(file1, file2,ArffFormatFullModel.FULL_MODEL_EXT_ARFF_FILE_FORMAT);
		System.out.println("NewGroup data 1 loaded. number="+extData.numInstances());
	
		String file3=AppContext.getC_ROOT_DIRECTORY()+"\\sourceData\\自选股第五组增量\\onceyield_optional_hv_update_2013_2015.txt";
		String file4=AppContext.getC_ROOT_DIRECTORY()+"\\sourceData\\自选股第五组增量\\onceyield_optional_hv_update_2016.txt";
		extData2 = mergeExtDataFromTwoFiles(file3, file4,ArffFormatFullModel.FULL_MODEL_EXT_ARFF_FILE_FORMAT);
		System.out.println("NewGroup data 2 loaded. number="+extData2.numInstances());
	
		extData=InstanceHandler.getHandler(extData).mergeTwoInstances(extData, extData2);
		System.out.println("NewGroup data merged and loaded. number="+extData.numInstances());
	
		//加载原始arff文件
		String originFileName=AppContext.getC_ROOT_DIRECTORY()+ArffFormatFullModel.FULL_MODEL_ARFF_PREFIX;
		GeneralInstances fullData = DataIOHandler.getSuppier().loadDataFromFile(originFileName+"-origin.arff");
	
	
		System.out.println("full trans data loaded. number="+fullData.numInstances());
	
		//将两边数据以ID排序
		fullData.sort(ArffFormat.ID_POSITION-1);
		extData.sort(ArffFormat.ID_POSITION-1);
		System.out.println("all data sorted by id");
	
	
		GeneralInstances result=mergeTransactionWithExtension(fullData,extData,ArffFormatFullModel.FULL_MODEL_EXT_ARFF_COLUMNS,ArffFormatFullModel.FULL_MODEL_EXT_ARFF_CRC);
		System.out.println("NewGroup data processed. number="+result.numInstances()+" columns="+result.numAttributes());
		extData=null;
		fullData=null;
	
		//返回结果之前需要按TradeDate重新排序
		int tradeDateIndex=BaseInstanceProcessor.findATTPosition(result, ArffFormat.TRADE_DATE);
		result.sort(tradeDateIndex-1);
	
		//保留原始的ext文件
		DataIOHandler.getSaver().SaveDataIntoFile(result, originFileName+".arff");
		System.out.println("history Data File saved: "+originFileName+".arff");
	
		//生成相应的一套Arff文件
		generateArffFileSetFullModel(originFileName,result);
	}

	private static GeneralInstances mergeSrcFullModelFiles() throws Exception,	IllegalStateException {
		String sourceFilePrefix=AppContext.getC_ROOT_DIRECTORY()+"sourceData\\自选股\\第四组自选股5天后卖出策略数据\\test_onceyield_group4allhis_optional";
		GeneralInstances fullData = loadDataFromFullModelCSVFile(sourceFilePrefix+"2005-2006.txt");
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(fullData);
		GeneralInstances addData = null;
		int startYear=2007;
		int endYear=2016;
		
		for (int i=startYear;i<=endYear;i++){
			addData = loadDataFromFullModelCSVFile(sourceFilePrefix+i+".txt");
			fullData=instanceProcessor.mergeTwoInstances(fullData, addData);
			System.out.println("FULLMODEL...merged "+i +" File,now row : "+ fullData.numInstances() + " column:"+ fullData.numAttributes());
		}
		return fullData;
	}
	
//	private static Instances mergeSrcFullModelFiles2016() throws Exception,	IllegalStateException {
//		String sourceFilePrefix=RuntimeParams.getC_ROOT_DIRECTORY()+"sourceData\\自选股\\第四组自选股5天后卖出策略数据\\test_onceyield_group4allhis_optional";
//		Instances fullData = FileUtilityFullModel.loadDataFromFullModelCSVFile(sourceFilePrefix+"2013.txt");
//		Instances addData = null;
//		int startYear=2014;
//		int endYear=2015;
//		for (int i=startYear;i<=endYear;i++){
//			addData = FileUtilityFullModel.loadDataFromFullModelCSVFile(sourceFilePrefix+i+".txt");
//			fullData=InstanceHandler.getHandler().mergeTwoInstances(fullData, addData);
//			System.out.println("FULLMODEL...merged "+i +" File,now row : "+ fullData.numInstances() + " column:"+ fullData.numAttributes());
//		}
//		return fullData;
//	}
	
	private static void generateArffFileSetFullModel(String originFileName,
			GeneralInstances fullSetData) throws Exception, IOException {

		// 存下用于计算收益率的数据
		GeneralInstances left=ArffFormatFullModel.getTransLeftPartForFullModel(fullSetData);
		DataIOHandler.getSaver().SaveDataIntoFile(left, originFileName+"-left.arff");
		System.out.println("FULLMODEL...history Data left File saved: "+originFileName+"-left.arff"  );
		left=null; //试图释放内存
		
		// 去除与训练无关的字段
		GeneralInstances result=ArffFormatFullModel.prepareTransDataForFullModel(fullSetData);
		
		//保存训练用的format，用于做日后的校验 
		GeneralInstances format=new WekaInstances(result,0);
		DataIOHandler.getSaver().SaveDataIntoFile(format, originFileName+"-format.arff");	
		//保存不含计算字段的格式
		DataIOHandler.getSaver().SaveDataIntoFile(result, originFileName+"-short.arff");
		
//		//添加计算字段
//		result=ArffFormat.addCalculateAttribute(result);
//		DataIOHandler.getSaver().SaveDataIntoFile(result, originFileName+"-new.arff");
		System.out.println("FULLMODEL...full Set Data File saved "  );

	}
	
	// 从增量的fullmodel交易CSV文件中加载数据
	private static GeneralInstances loadDataFromFullModelCSVFile(String fileName) throws Exception{ 
		return DataIOHandler.getSuppier().loadDataWithFormatFromCSVFile(fileName,ArffFormatFullModel.FULL_MODEL_DATA_FORMAT_NEW);
	}
	
	

	//这是处理历史全量数据，重新切割生成各种长、短以及格式文件的方法
	private static void processHistoryFileFullModel() throws Exception {
		System.out.println("loading history file into memory "  );
		String originFileName=AppContext.getC_ROOT_DIRECTORY()+ArffFormatFullModel.FULL_MODEL_ARFF_PREFIX;
		GeneralInstances fullSetData = DataIOHandler.getSuppier().loadDataFromFile(originFileName+".arff");
		System.out.println("finish  loading fullset File  row : "+ fullSetData.numInstances() + " column:"+ fullSetData.numAttributes());
		generateArffFileSetFullModel(originFileName, fullSetData);
	}
}
