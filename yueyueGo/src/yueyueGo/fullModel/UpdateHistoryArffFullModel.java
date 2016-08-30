package yueyueGo.fullModel;



import java.io.IOException;

import weka.core.Instances;
import yueyueGo.ArffFormat;
import yueyueGo.FileUtility;
import yueyueGo.InstanceUtility;
import yueyueGo.UpdateHistoryArffFile;

public class UpdateHistoryArffFullModel extends UpdateHistoryArffFile {
	
	protected static void createFullModelInstances() throws Exception {

		String arffFileName=ProcessDataFullModel.C_ROOT_DIRECTORY+ArffFormatFullModel.TRANSACTION_ARFF_PREFIX;
		Instances rawData = mergeSrcFullModelFiles();
		
		//处理所有的日期字段，并插入yearmonth
		processDateColumns(rawData);

		//处理各种nominal字段
		Instances fullData=FileUtility.loadDataFromFile(ProcessDataFullModel.C_ROOT_DIRECTORY+"fullModelFormat.arff");
		System.out.println("!!!!!verifying input data format , you should read this .... "+ fullData.equalHeadersMsg(rawData));
		InstanceUtility.calibrateAttributes(rawData, fullData);
		rawData=null; //试图释放内存
		
		//获取tradeDateIndex （从1开始）， 并按其排序
		int tradeDateIndex=InstanceUtility.findATTPosition(fullData, ArffFormatFullModel.TRADE_DATE);
		fullData.sort(tradeDateIndex-1);

		System.out.println("FULLMODEL...trans arff file sorted, start to save.... number of rows="+fullData.numInstances());
		FileUtility.SaveDataIntoFile(fullData, arffFileName+".arff");
		System.out.println("FULLMODEL...trans arff file saved. ");
		
		//取出前半年的旧数据和当年的新数据作为验证的sample数据
		String splitSampleClause = "( ATT" + ArffFormatFullModel.YEAR_MONTH_INDEX + " >= 201506) and ( ATT" + ArffFormatFullModel.YEAR_MONTH_INDEX+ " <= 201612) ";
		Instances sampleData=InstanceUtility.getInstancesSubset(fullData, splitSampleClause);
		FileUtility.SaveDataIntoFile(sampleData, arffFileName+"-sample.arff");
		System.out.println("FULLMODEL...sample arff file saved. ");
		sampleData=null;//试图释放内存
		
		//生成模型全套文件
		generateArffFileSetFullModel(arffFileName,fullData);
	}
	
	private static Instances mergeSrcFullModelFiles() throws Exception,	IllegalStateException {
		String sourceFilePrefix=ProcessDataFullModel.C_ROOT_DIRECTORY+"sourceData\\自选股\\第四组自选股5天后卖出策略数据\\test_onceyield_group4allhis_optional";
		Instances fullData = FileUtilityFullModel.loadDataFromFullModelCSVFile(sourceFilePrefix+"2005-2006.txt");
		Instances addData = null;
		int startYear=2007;
		int endYear=2016;
		for (int i=startYear;i<=endYear;i++){
			addData = FileUtilityFullModel.loadDataFromFullModelCSVFile(sourceFilePrefix+i+".txt");
			fullData=InstanceUtility.mergeTwoInstances(fullData, addData);
			System.out.println("FULLMODEL...merged one File,now row : "+ fullData.numInstances() + " column:"+ fullData.numAttributes());
		}
		return fullData;
	}
	
	private static void generateArffFileSetFullModel(String originFileName,
			Instances fullSetData) throws Exception, IOException {

		// 存下用于计算收益率的数据
		Instances left=ArffFormatFullModel.getTransLeftPartForFullModel(fullSetData);
		FileUtility.SaveDataIntoFile(left, originFileName+"-left.arff");
		System.out.println("FULLMODEL...history Data left File saved: "+originFileName+"-left.arff"  );
		left=null; //试图释放内存
		
		// 去除与训练无关的字段
		Instances result=ArffFormatFullModel.prepareTransData(fullSetData);
		
		//保存训练用的format，用于做日后的校验 
		Instances format=new Instances(result,0);
		FileUtility.SaveDataIntoFile(format, originFileName+"-format.arff");	
		//保存不含计算字段的格式
		FileUtility.SaveDataIntoFile(result, originFileName+"-short.arff");
		
		//添加计算字段
		result=ArffFormat.addCalculateAttribute(result);
		FileUtility.SaveDataIntoFile(result, originFileName+"-new.arff");
		System.out.println("FULLMODEL...full Set Data File saved "  );

	}
}
