package yueyueGo.fullModel;

import java.io.IOException;
import java.util.HashMap;

import weka.core.Instances;
import yueyueGo.ArffFormat;
import yueyueGo.BaseClassifier;
import yueyueGo.EnvConstants;
import yueyueGo.NominalClassifier;
import yueyueGo.ProcessData;
import yueyueGo.fullModel.classifier.BaggingJ48FullModel;
import yueyueGo.fullModel.classifier.BaggingM5PFullModel;
import yueyueGo.utility.ClassifySummaries;
import yueyueGo.utility.FileUtility;
import yueyueGo.utility.FormatUtility;
import yueyueGo.utility.InstanceUtility;
import yueyueGo.utility.RuntimeParams;

public class ProcessDataFullModel extends ProcessData {
	private boolean applyToMaModel=true; //default is false
	
	//覆盖父类
	public void init() {
		STRAGEY_NAME="短线策略";
		C_ROOT_DIRECTORY = EnvConstants.FULL_MODEL_ROOT_DIR;
		RuntimeParams.createInstance(C_ROOT_DIRECTORY);	
		BACKTEST_RESULT_DIR=RuntimeParams.getBACKTEST_RESULT_DIR();
		PREDICT_WORK_DIR=RuntimeParams.getPREDICT_WORK_DIR();	
		
		RUNNING_THREADS=6;
		
		SHOUYILV_THREDHOLD=new double[] {0.05};
		WINRATE_THREDHOLD=new double[] {0.6};
		
		splitYear=new String[] {
//			"201606", "2008","2009","2010","2011","2012","2013","2014","2015","2016"
			"200801","200802","200803","200804","200805","200806","200807","200808","200809","200810","200811","200812","200901","200902","200903","200904","200905","200906","200907","200908","200909","200910","200911","200912","201001","201002","201003","201004","201005","201006","201007","201008","201009","201010","201011","201012","201101","201102","201103","201104","201105","201106","201107","201108","201109","201110","201111","201112","201201","201202","201203","201204","201205","201206","201207","201208","201209","201210","201211","201212","201301","201302","201303","201304","201305","201306","201307","201308","201309","201310","201311","201312","201401","201402","201403","201404","201405","201406","201407","201408","201409","201410","201411","201412","201501","201502","201503","201504","201505","201506","201507","201508","201509","201510","201511","201512","201601","201602","201603", "201604","201605","201606","201607"
//			"201606","201607"
		};
	}


	public static void main(String[] args) {
		try {
			ProcessDataFullModel fullModelWorker=new ProcessDataFullModel();
			fullModelWorker.init();
			
			
//			UpdateHistoryArffFullModel.createFullModelInstances();
			
			//短线模型的历史回测
			fullModelWorker.callFullModelTestBack();
			
			//短线模型的每日预测
//			fullModelWorker.callFullModelPredict();

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
//		AdaboostFullModel nModel=new AdaboostFullModel();
		
		if (applyToMaModel==true){//用fullModel模型来测试均线模型时不用重新build和评估
			nModel.m_skipTrainInBacktest=true;
			nModel.m_skipEvalInBacktest=true;
		}	
		ClassifySummaries nModelSummaries=new ClassifySummaries(nModel.getIdentifyName());
		nModel.setClassifySummaries(nModelSummaries);
		
//		Instances nominalResult=testBackward(nModel);
		//不真正回测了，直接从以前的结果文件中加载
		Instances nominalResult=loadBackTestResultFromFile(nModel.getIdentifyName());

		//按连续分类器回测历史数据
		BaggingM5PFullModel cModel=new BaggingM5PFullModel();
		if (applyToMaModel==true){//用fullModel模型来测试均线模型时不用重新build和评估
			cModel.m_skipTrainInBacktest=true;
			cModel.m_skipEvalInBacktest=true;
		}
		ClassifySummaries cModelSummaries=new ClassifySummaries(cModel.getIdentifyName());
		cModel.setClassifySummaries(cModelSummaries);
//		Instances continuousResult=testBackward(cModel);
		//不真正回测了，直接从以前的结果文件中加载
		Instances continuousResult=loadBackTestResultFromFile(cModel.getIdentifyName());
		
		//统一输出统计结果
		nModelSummaries.outputClassifySummary();
		cModelSummaries.outputClassifySummary();

		//输出用于计算收益率的CSV文件
		System.out.println("-----now output continuous predictions----------"+cModel.getIdentifyName());
		Instances m5pOutput=mergeResultWithData(continuousResult,nominalResult,ArffFormat.RESULT_PREDICTED_WIN_RATE,cModel.getModelArffFormat());
		saveSelectedFileForMarkets(m5pOutput,cModel.getIdentifyName());
		System.out.println("-----now output nominal predictions----------"+nModel.getIdentifyName());
		Instances mlpOutput=mergeResultWithData(nominalResult,continuousResult,ArffFormat.RESULT_PREDICTED_PROFIT,nModel.getModelArffFormat());
		saveSelectedFileForMarkets(mlpOutput,nModel.getIdentifyName());
		System.out.println("-----end of test backward------");
	}

	@Override
	protected void definePredictModels(){
		PREDICT_MODELS=new HashMap<String, String>();
		String EVAL="-EVAL";
		String classifierName;
		
		//M5P当前使用的预测模型
		classifierName=new BaggingM5PFullModel().classifierName;
		PREDICT_MODELS.put(classifierName, "\\extData2005-2016-BaggingM5PABFullModel-201606 MA ");
		PREDICT_MODELS.put(classifierName+EVAL, "\\extData2005-2016-BaggingM5PABFullModel-201607 MA ");
		
		//MLP当前使用的预测模型
		classifierName=new BaggingJ48FullModel().classifierName;
		PREDICT_MODELS.put(classifierName, "\\extData2005-2016-BaggingJ48ABFullModel-201606 MA ");
		PREDICT_MODELS.put(classifierName+EVAL, "\\extData2005-2016-BaggingJ48ABFullModel-201607 MA ");
	}
	
	@Override
	protected Instances getDailyPredictDataFormat(int formatType)
			throws Exception {
		String formatFile=null;
		switch (formatType) {
		case ArffFormatFullModel.FULLMODEL_FORMAT:
			formatFile=ArffFormatFullModel.FULL_MODEL_ARFF_PREFIX+"-format.arff";
			break;
		default:
			throw new Exception("invalid arffFormat type");
		}

		Instances outputData=FileUtility.loadDataFromFile(C_ROOT_DIRECTORY+formatFile);
		return outputData;
	}
	
	
	/**
	 * @throws Exception
	 */
	protected void callFullModelPredict() throws Exception {
		definePredictModels();
		
		//BaggingM5P
		BaggingM5PFullModel cBagModel=new BaggingM5PFullModel();
		Instances cBagInstances=predictFullModelWithDB(cBagModel,PREDICT_WORK_DIR);		
		
		//BaggingJ48
		BaggingJ48FullModel nBagModel=new BaggingJ48FullModel();
		Instances nBagInstances=predictFullModelWithDB(nBagModel,PREDICT_WORK_DIR);		
		
		//合并baggingJ48和baggingM5P
		System.out.println("-----now output combined predictions----------"+cBagModel.getIdentifyName());

		Instances left=InstanceUtility.keepAttributes(cBagInstances, ArffFormat.DAILY_PREDICT_RESULT_LEFT) ; //为了使用下面的合并文件方法造出一个LEFT来
		Instances mergedOutput=mergeResults(cBagInstances,nBagInstances,ArffFormat.RESULT_PREDICTED_WIN_RATE,left);
		FileUtility.saveCSVFile(mergedOutput, PREDICT_WORK_DIR + "FullModel Selected Result-"+cBagModel.getIdentifyName()+"-"+FormatUtility.getDateStringFor(1)+".csv");
		
	}	
	
	
	//直接访问数据库预测每天的自选股数据，不单独保存每个模型的选股
	protected Instances predictFullModelWithDB(BaseClassifier clModel, String pathName) throws Exception {
		System.out.println("predict using classifier : "+clModel.getIdentifyName()+" @ prediction work path :"+PREDICT_WORK_DIR);
		System.out.println("-----------------------------");
		Instances fullData = DBAccessFullModel.LoadFullModelDataFromDB();//"2016-08-26");
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
	@Override
	protected Instances getBacktestInstances(BaseClassifier clModel,String splitMark,String policy)
			throws Exception {

		String arffFullFileName = null;
		if (applyToMaModel==true){//用fullModel模型来测试均线模型时加载均线模型的arff
			arffFullFileName=getMaArffFileName(clModel);
		}else{
			arffFullFileName=getFullModelArffFileName(clModel);
		}
		Instances fullSetData;
		System.out.println("start to load File for fullset from File: "+ arffFullFileName  );
		fullSetData = FileUtility.loadDataFromFile( arffFullFileName);
		if (applyToMaModel==true){//用fullModel模型来测试均线模型时加载均线模型的arff
			int pos = InstanceUtility.findATTPosition(fullSetData,ArffFormat.SELECTED_AVG_LINE);
			fullSetData = InstanceUtility.removeAttribs(fullSetData,""+pos );
		}
		System.out.println("finish loading fullset Data. row : "+fullSetData.numInstances() + " column:"+ fullSetData.numAttributes());
		return fullSetData;
	}


	/**
	 * 这是加载fullModel的arff文件，用于训练模型或为Fullmodel做评估
	 * @param clModel
	 * @return
	 */
	private String getFullModelArffFileName(BaseClassifier clModel) {
		// 根据模型来决定是否要使用有计算字段的ARFF
		String arffFile=null;
		if (clModel.m_noCaculationAttrib==true){
			arffFile=ArffFormatFullModel.FULL_MODEL_SHORT_ARFF_FILE;
		}else{
			arffFile=ArffFormatFullModel.FULL_MODEL_LONG_ARFF_FILE;
		}
		String arffFullFileName=C_ROOT_DIRECTORY+arffFile;
		//		int year=Integer.parseInt(splitMark);
//		//根据年份查找相应的目录
//		if (year<=2009){
//			arffFile="2009\\"+arffFile;
//		}
		return arffFullFileName;
	}
	
	/**
	 * 这是加载原始的arff文件，仅用于回测，不用于训练。
	 * @param clModel
	 * @return
	 */
	private String getMaArffFileName(BaseClassifier clModel) {
		// 根据模型来决定是否要使用有计算字段的ARFF
		String arffFile=null;
		if (clModel.m_noCaculationAttrib==true){
			arffFile=ArffFormat.SHORT_ARFF_FILE;
		}else{
			arffFile=ArffFormat.LONG_ARFF_FILE;
		}
		String arffFullFileName=EnvConstants.AVG_LINE_ROOT_DIR+arffFile;
		return arffFullFileName;
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
		Instances left=null;		
		//读取磁盘上预先保存的左侧数据
		if (applyToMaModel==true){
			left=FileUtility.loadDataFromFile(EnvConstants.AVG_LINE_ROOT_DIR+ArffFormat.TRANSACTION_ARFF_PREFIX+"-left.arff");
		}else{
			left=FileUtility.loadDataFromFile(C_ROOT_DIRECTORY+ArffFormatFullModel.FULL_MODEL_ARFF_PREFIX+"-left.arff");
		}
		Instances mergedResult = mergeResults(resultData, referenceData,dataToAdd, left);
		//返回结果之前需要按TradeDate重新排序
		int tradeDateIndex=InstanceUtility.findATTPosition(mergedResult, ArffFormat.TRADE_DATE);
		mergedResult.sort(tradeDateIndex-1);
		// 给mergedResult瘦身。 2=yearmonth, 6=datadate,7=positive,8=bias
		String[] attributeToRemove=new String[]{ArffFormat.YEAR_MONTH,ArffFormat.DATA_DATE,ArffFormat.IS_POSITIVE,ArffFormat.BIAS5};
		mergedResult=InstanceUtility.removeAttribs(mergedResult, attributeToRemove);
		
		if (applyToMaModel==false){
			//插入一列“均线策略”为计算程序使用
			mergedResult=InstanceUtility.AddAttributeWithValue(mergedResult, ArffFormat.SELECTED_AVG_LINE,"numeric","0");
		}
		return mergedResult;

		

	}

}
