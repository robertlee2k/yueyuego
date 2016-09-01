package yueyueGo.fullModel;

import java.io.IOException;

import weka.core.Instances;
import yueyueGo.ArffFormat;
import yueyueGo.BaseClassifier;
import yueyueGo.EnvConstants;
import yueyueGo.FileUtility;
import yueyueGo.FormatUtility;
import yueyueGo.InstanceUtility;
import yueyueGo.NominalClassifier;
import yueyueGo.ProcessData;
import yueyueGo.RuntimeParams;
import yueyueGo.classifier.AdaboostClassifier;
import yueyueGo.classifier.BaggingM5P;
import yueyueGo.fullModel.classifier.BaggingJ48FullModel;
import yueyueGo.fullModel.classifier.BaggingM5PFullModel;

public class ProcessDataFullModel extends ProcessData {
	
	//覆盖父类
	public void init() {
		C_ROOT_DIRECTORY = EnvConstants.FULL_MODEL_ROOT_DIR;
		RuntimeParams.createInstance(C_ROOT_DIRECTORY);	
		BACKTEST_RESULT_DIR=RuntimeParams.getBACKTEST_RESULT_DIR();
		PREDICT_WORK_DIR=RuntimeParams.getPREDICT_WORK_DIR();	
		
		STRAGEY_NAME="短线策略";
		splitYear=new String[] {
				  "2008","2009","2010","2011","2012","2013","2014","2015","2016"
//				"200801","200802","200803","200804","200805","200806","200807","200808","200809","200810","200811","200812","200901","200902","200903","200904","200905","200906","200907","200908","200909","200910","200911","200912","201001","201002","201003","201004","201005","201006","201007","201008","201009","201010","201011","201012","201101","201102","201103","201104","201105","201106","201107","201108","201109","201110","201111","201112","201201","201202","201203","201204","201205","201206","201207","201208","201209","201210","201211","201212","201301","201302","201303","201304","201305","201306","201307","201308","201309","201310","201311","201312","201401","201402","201403","201404","201405","201406","201407","201408","201409","201410","201411","201412","201501","201502","201503","201504","201505","201506","201507","201508","201509","201510","201511","201512","201601","201602","201603", "201604","201605","201606","201607"
//				"2009"
		};
	}


	public static void main(String[] args) {
		try {
			ProcessDataFullModel fullModelWorker=new ProcessDataFullModel();
			fullModelWorker.init();
			
			
//			UpdateHistoryArffFullModel.createFullModelInstances();
			fullModelWorker.callFullModelTestBack();
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @throws Exception
	 * @throws IOException
	 */
	protected void callFullModelTestBack() throws Exception, IOException {
		//按二分类器回测历史数据
		BaggingJ48FullModel nModel=new BaggingJ48FullModel();
		Instances nominalResult=testBackward(nModel);
		//不真正回测了，直接从以前的结果文件中加载
//		Instances nominalResult=loadBackTestResultFromFile(nModel.getIdentifyName());

		//按连续分类器回测历史数据
		BaggingM5PFullModel cModel=new BaggingM5PFullModel();
		Instances continuousResult=testBackward(cModel);
		//不真正回测了，直接从以前的结果文件中加载
//		Instances continuousResult=loadBackTestResultFromFile(cModel.getIdentifyName());
		
		
		//统一输出统计结果
		nModel.outputClassifySummary();
		cModel.outputClassifySummary();

		//输出用于计算收益率的CSV文件
		System.out.println("-----now output continuous predictions----------"+cModel.getIdentifyName());
		Instances m5pOutput=mergeResultWithData(continuousResult,nominalResult,ArffFormat.RESULT_PREDICTED_WIN_RATE,cModel.arff_format);
		saveSelectedFileForMarkets(m5pOutput,cModel.getIdentifyName());
		System.out.println("-----now output nominal predictions----------"+nModel.getIdentifyName());
		Instances mlpOutput=mergeResultWithData(nominalResult,continuousResult,ArffFormat.RESULT_PREDICTED_PROFIT,nModel.arff_format);
		saveSelectedFileForMarkets(mlpOutput,nModel.getIdentifyName());
		System.out.println("-----end of test backward------");
	}
	
	/**
	 * @throws Exception
	 */
	protected void callFullModelPredict() throws Exception {

		//BaggingM5P
		BaggingM5P cBagModel=new BaggingM5P();
		Instances baggingInstances=predictFullModelWithDB(cBagModel,PREDICT_WORK_DIR);		
		
		//Adaboost
		AdaboostClassifier adaModel=new AdaboostClassifier();
		Instances adaboostInstances=predictFullModelWithDB(adaModel,PREDICT_WORK_DIR);		
		
		//合并adaboost和bagging
		System.out.println("-----now output combined predictions----------"+adaModel.getIdentifyName());
		Instances left=InstanceUtility.removeAttribs(adaboostInstances, "5,6"); //只是为了使用下面的方法
		Instances mergedOutput=mergeResults(adaboostInstances,baggingInstances,ArffFormat.RESULT_PREDICTED_PROFIT,left);
		FileUtility.saveCSVFile(mergedOutput, PREDICT_WORK_DIR + "FullModel Selected Result-"+adaModel.getIdentifyName()+"-"+FormatUtility.getDateStringFor(1)+".csv");
		
	}	
	
	
	//直接访问数据库预测每天的自选股数据，不单独保存每个模型的选股
	protected Instances predictFullModelWithDB(BaseClassifier clModel, String pathName) throws Exception {
		System.out.println("predict using classifier : "+clModel.getIdentifyName()+" @ prediction work path :"+PREDICT_WORK_DIR);
		System.out.println("-----------------------------");
		Instances fullData = DBAccessFullModel.LoadFullModelDataFromDB(FormatUtility.getDateStringFor(0));
		Instances result=predict(clModel, pathName, fullData);
		return result;
	}	
	
	
	/**
	 * 隐藏父类的函数。
	 * @param splitTrainYearClause
	 * @param policy
	 * @return
	 */
	protected String getSplitClause(String splitYearClause,	String policy) {
		return splitYearClause;
	}
	
	/**
	 * 子类覆盖
	 * @param clModel
	 * @param fullSetData
	 * @return
	 * @throws Exception
	 */
	protected Instances prepareResultInstances(BaseClassifier clModel,
			Instances fullSetData) throws Exception {
		Instances result;
		Instances header = new Instances(fullSetData, 0);
		// 去除不必要的字段，保留ID（第1），bias5（第3）、收益率（最后一列）、增加预测值、是否被选择。
		result = InstanceUtility.removeAttribs(header, ArffFormat.YEAR_MONTH_INDEX + ",4-"
				+ (header.numAttributes() - 1));
		if (clModel instanceof NominalClassifier ){
			result = InstanceUtility.AddAttribute(result, ArffFormat.RESULT_PREDICTED_WIN_RATE,
					result.numAttributes());
		}else{
			result = InstanceUtility.AddAttribute(result, ArffFormat.RESULT_PREDICTED_PROFIT,
					result.numAttributes());
		}
		result = InstanceUtility.AddAttribute(result, ArffFormat.RESULT_SELECTED,
				result.numAttributes());
		return result;
	}

	/**
	 * 子类覆盖
	 * @param clModel
	 * @return
	 * @throws Exception
	 */
	protected Instances getBacktestInstances(BaseClassifier clModel,String splitMark,String policy)
			throws Exception {
		Instances fullSetData;
		// 根据模型来决定是否要使用有计算字段的ARFF
		String arffFile=null;
		if (clModel.m_noCaculationAttrib==true){
			arffFile=ArffFormatFullModel.FULL_MODEL_SHORT_ARFF_FILE;
		}else{
			arffFile=ArffFormatFullModel.FULL_MODEL_LONG_ARFF_FILE;
		}
//		int year=Integer.parseInt(splitMark);
//		//根据年份查找相应的目录
//		if (year<=2009){
//			arffFile="2009\\"+arffFile;
//		}

		System.out.println("start to load File for fullset from File: "+C_ROOT_DIRECTORY+ arffFile  );
		fullSetData = FileUtility.loadDataFromFile( C_ROOT_DIRECTORY+arffFile);
		
		System.out.println("finish loading fullset Data. row : "+fullSetData.numInstances() + " column:"+ fullSetData.numAttributes());
		return fullSetData;
	}

	/**
	 * 	获取分割年的clause，该方法提供子类覆盖的灵活性
	 * @param splitMark
	 * @return
	 */
	protected String[] splitYearClause(String splitMark) {
		String[] splitYearClauses=new String[2];
		String attribuateYear = "ATT" + ArffFormat.YEAR_MONTH_INDEX;
		if (splitMark.length() == 6) { // 按月分割时
			splitYearClauses[0] = "(" + attribuateYear + " < "
					+ splitMark + ") ";
			splitYearClauses[1] = "(" + attribuateYear + " = "
					+ splitMark + ") ";
		} else if (splitMark.length() == 4) {// 按年分割
			splitYearClauses[0] = "(" + attribuateYear + " < "
					+ splitMark + "01) ";
			splitYearClauses[1] = "(" + attribuateYear + " >= "
					+ splitMark + "01) and (" + attribuateYear + " <= "
					+ splitMark + "12) ";
		}
		return splitYearClauses;
	}
	
	protected Instances mergeResultWithData(Instances resultData,Instances referenceData,String dataToAdd,int format) throws Exception{
		//读取磁盘上预先保存的左侧数据
		Instances left=FileUtility.loadDataFromFile(C_ROOT_DIRECTORY+ArffFormatFullModel.FULL_MODEL_ARFF_PREFIX+"-left.arff");
		

		Instances mergedResult = mergeResults(resultData, referenceData,dataToAdd, left);
		
		//返回结果之前需要按TradeDate重新排序
		int tradeDateIndex=InstanceUtility.findATTPosition(mergedResult, ArffFormat.TRADE_DATE);
		mergedResult.sort(tradeDateIndex-1);
		
		//TODO 给mergedResult瘦身。
		mergedResult=InstanceUtility.removeAttribs(mergedResult, "2,6,7,8");

		return mergedResult;
	}

}
