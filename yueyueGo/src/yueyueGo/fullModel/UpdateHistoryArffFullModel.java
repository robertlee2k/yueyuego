package yueyueGo.fullModel;



import java.io.IOException;

import yueyueGo.UpdateHistoryArffFile;
import yueyueGo.dataFormat.FullModelDataFormat;
import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.dataProcessor.InstanceHandler;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaInstances;
import yueyueGo.datasource.DataIOHandler;
import yueyueGo.utility.AppContext;

public class UpdateHistoryArffFullModel extends UpdateHistoryArffFile {
	protected static FullModelDataFormat ARFF_FORMAT_FULLMODEL=new FullModelDataFormat(); //当前所用数据文件格式
	
	public static void main(String[] args) {
		try {
			BackTestFullModel fullModelWorker=new BackTestFullModel();
			fullModelWorker.init();

			//短线模型生成初始全量数据
			UpdateHistoryArffFullModel.callCreateFullModelInstances();

//			//刷新增量数据
//			UpdateHistoryArffFullModel.callRefreshInstancesFullModel();
//			
//			//短线模型刷新最新月评估数据
//			fullModelWorker.callRefreshFullModelUseLatestData();
			
			//短线模型合并新的属性
//			UpdateHistoryArffFullModel.callMergeExtDataForFullModel();			

		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	protected static void callRefreshInstancesFullModel() throws Exception {
		String startYearMonth="201609";
		String endYearMonth="201612";
		

		String originFilePrefix=AppContext.getC_ROOT_DIRECTORY()+ARFF_FORMAT_FULLMODEL.m_arff_file_prefix;
		
		String newDataFileName=AppContext.getC_ROOT_DIRECTORY()+"sourceData\\group5\\onceyield_group5all_optional20160901_20161031.txt";		
		GeneralInstances newData = loadDataFromIncrementalCSVFile(newDataFileName);
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

		String arffFileName=AppContext.getC_ROOT_DIRECTORY()+ARFF_FORMAT_FULLMODEL.m_arff_file_prefix;
		GeneralInstances rawData = mergeSrcFullModelFiles();
		
		//处理所有的日期字段，并插入yearmonth
		processDateColumns(rawData);

		//处理各种nominal字段
		GeneralInstances fullData=DataIOHandler.getSuppier().loadDataFromFile(AppContext.getC_ROOT_DIRECTORY()+"fullModelFormat.arff");
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(rawData);
		instanceProcessor.calibrateAttributes(rawData, fullData);
		rawData=null; //试图释放内存
		
		//获取tradeDateIndex （从1开始）， 并按其排序
		int tradeDateIndex=BaseInstanceProcessor.findATTPosition(fullData, FullModelDataFormat.TRADE_DATE);
		fullData.sort(tradeDateIndex-1);

		System.out.println("FULLMODEL...trans arff file sorted, start to save.... number of rows="+fullData.numInstances());
		DataIOHandler.getSaver().SaveDataIntoFile(fullData, arffFileName+".arff");
		System.out.println("FULLMODEL...trans arff file saved. ");
		
		//取出前半年的旧数据和当年的新数据作为验证的sample数据
		String splitSampleClause = "( ATT" + FullModelDataFormat.YEAR_MONTH_INDEX + " >= 201606) and ( ATT" + FullModelDataFormat.YEAR_MONTH_INDEX+ " <= 201712) ";
		GeneralInstances sampleData=instanceProcessor.getInstancesSubset(fullData, splitSampleClause);
		DataIOHandler.getSaver().SaveDataIntoFile(sampleData, arffFileName+"-sample.arff");
		System.out.println("FULLMODEL...sample arff file saved. ");
		sampleData=null;//试图释放内存
		
		//生成模型全套文件
		generateArffFileSetFullModel(arffFileName,fullData);
	}
	


	private static GeneralInstances mergeSrcFullModelFiles() throws Exception,	IllegalStateException {
		String sourceFilePrefix=AppContext.getC_ROOT_DIRECTORY()+"sourceData\\group8\\v_onceyield_group8all_optional";
		GeneralInstances fullData = loadDataFromIncrementalCSVFile(sourceFilePrefix+"2005_2010.txt");

		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(fullData);
		GeneralInstances addData = null;

		addData = loadDataFromIncrementalCSVFile(sourceFilePrefix+"2011_2017.txt");
		fullData=instanceProcessor.mergeTwoInstances(fullData, addData);
		System.out.println("FULLMODEL...merged another File,now row : "+ fullData.numInstances() + " column:"+ fullData.numAttributes());

//		addData = loadDataFromIncrementalCSVFile(sourceFilePrefix+"2011_2013.txt");
//		fullData=instanceProcessor.mergeTwoInstances(fullData, addData);
//		System.out.println("FULLMODEL...merged another File,now row : "+ fullData.numInstances() + " column:"+ fullData.numAttributes());
//		
//		addData = loadDataFromIncrementalCSVFile(sourceFilePrefix+"2014_2016.txt");
//		fullData=instanceProcessor.mergeTwoInstances(fullData, addData);
//		System.out.println("FULLMODEL...merged another File,now row : "+ fullData.numInstances() + " column:"+ fullData.numAttributes());
		

//		int startYear=2014;
//		int endYear=2016;
//		
//		for (int i=startYear;i<=endYear;i++){
//			addData = loadDataFromFullModelCSVFile(sourceFilePrefix+i+".txt");
//			fullData=instanceProcessor.mergeTwoInstances(fullData, addData);
//			System.out.println("FULLMODEL...merged "+i +" File,now row : "+ fullData.numInstances() + " column:"+ fullData.numAttributes());
//		}
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
		GeneralInstances left=ARFF_FORMAT_FULLMODEL.getTransLeftPartFromAllTransaction(fullSetData);
		DataIOHandler.getSaver().SaveDataIntoFile(left, originFileName+"-left.arff");
		System.out.println("FULLMODEL...history Data left File saved: "+originFileName+"-left.arff"  );
		left=null; //试图释放内存
		
		// 去除与训练无关的字段
		GeneralInstances result=ARFF_FORMAT_FULLMODEL.getTransLeftPartFromAllTransaction(fullSetData);
		
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
	
	// 从增量的交易CSV文件中加载数据
	protected static GeneralInstances loadDataFromIncrementalCSVFile(String fileName) throws Exception{ 
		return DataIOHandler.getSuppier().loadDataWithFormatFromCSVFile(fileName,ARFF_FORMAT_FULLMODEL.m_arff_data_full);
	}

	//这是处理历史全量数据，重新切割生成各种长、短以及格式文件的方法
	private static void processHistoryFileFullModel() throws Exception {
		System.out.println("loading history file into memory "  );
		String originFileName=AppContext.getC_ROOT_DIRECTORY()+ARFF_FORMAT_FULLMODEL.m_arff_file_prefix;
		GeneralInstances fullSetData = DataIOHandler.getSuppier().loadDataFromFile(originFileName+".arff");
		System.out.println("finish  loading fullset File  row : "+ fullSetData.numInstances() + " column:"+ fullSetData.numAttributes());
		generateArffFileSetFullModel(originFileName, fullSetData);
	}
}
