package yueyueGo.fullModel;

import java.io.IOException;

import yueyueGo.ArffFormat;
import yueyueGo.BackTest;
import yueyueGo.BaseClassifier;
import yueyueGo.EnvConstants;
import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.dataProcessor.InstanceHandler;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.datasource.DataIOHandler;
import yueyueGo.fullModel.classifier.AdaboostFullModel;
import yueyueGo.fullModel.classifier.BaggingM5PFullModel;
import yueyueGo.fullModel.classifier.BaggingRegressionFullModel;
import yueyueGo.fullModel.classifier.MyNNFullModel;
import yueyueGo.utility.AppContext;
import yueyueGo.utility.MergeClassifyResults;

public class BackTestFullModel extends BackTest {
	private boolean applyToMaModelInTestBack=false; //default is false
	
	//覆盖父类
	public void init() {
		STRAGEY_NAME="短线策略";
		C_ROOT_DIRECTORY = EnvConstants.FULL_MODEL_ROOT_DIR;
		AppContext.clearContext();
		AppContext.createContext(C_ROOT_DIRECTORY);	
		BACKTEST_RESULT_DIR=AppContext.getBACKTEST_RESULT_DIR();
		
		RUNNING_THREADS=10;
		
		shouyilv_thresholds=new double[] {0.02};
		winrate_thresholds=new double[] {0.7};
		
		splitYear=new String[] {
				//为年度模型使用
//				"2016","201607"
//  			  "2008","2009","2010","2011","2012","2013","2014","2015","2016",
				//为半年度模型使用		
//				"200807","200907","201007","201107","201207","201307","201407","201507","201607"
				//为月度模型使用		
				"200801","200802","200803","200804","200805","200806","200807","200808","200809","200810","200811","200812","200901","200902","200903","200904","200905","200906","200907","200908","200909","200910","200911","200912","201001","201002","201003","201004","201005","201006","201007","201008","201009","201010","201011","201012","201101","201102","201103","201104","201105","201106","201107","201108","201109","201110","201111","201112","201201","201202","201203","201204","201205","201206","201207","201208","201209","201210","201211","201212","201301","201302","201303","201304","201305","201306","201307","201308","201309","201310","201311","201312","201401","201402","201403","201404","201405","201406","201407","201408","201409","201410","201411","201412","201501","201502","201503","201504","201505","201506","201507","201508","201509","201510","201511","201512","201601","201602","201603", "201604","201605","201606","201607","201608","201609"
				//生成预测所使用半年度模型		
//				"201607"	
		};
	}


	public static void main(String[] args) {
		try {
			BackTestFullModel fullModelWorker=new BackTestFullModel();
			fullModelWorker.init();
			
			//短线模型的历史回测
			fullModelWorker.callFullModelTestBack();
			
			//短线模型刷新最新月评估数据
//			fullModelWorker.callRefreshFullModelUseLatestData();
			
			//短线模型生成初始全量数据
//			UpdateHistoryArffFullModel.createFullModelInstances();
			
			//刷新增量数据
//			UpdateHistoryArffFullModel.callRefreshInstancesFullModel();
			
			//短线模型合并新的属性
//			UpdateHistoryArffFullModel.callMergeExtDataForFullModel();			

		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据最新这个月的增量数据刷新模型
	 * @throws Exception
	 */
	protected void callRefreshFullModelUseLatestData() throws Exception{
		BaseClassifier model=null;
		splitYear=new String[] {"201609"};
		RUNNING_THREADS=5;
		
		//逐次刷新数据
		model=new AdaboostFullModel();
		model.m_skipTrainInBacktest=true;
		model.m_skipEvalInBacktest=false;
		testBackward(model);
		
		model=new BaggingM5PFullModel();
		model.m_skipTrainInBacktest=true;
		model.m_skipEvalInBacktest=false;
		testBackward(model);
		
		model=new MyNNFullModel();
		model.m_skipTrainInBacktest=true;
		model.m_skipEvalInBacktest=false;
		testBackward(model);

		model=new BaggingRegressionFullModel();
		model.m_skipTrainInBacktest=true;
		model.m_skipEvalInBacktest=false;
		testBackward(model);
	}
	/**
	 * @throws Exception
	 * @throws IOException
	 */
	protected void callFullModelTestBack() throws Exception, IOException {
		//按二分类器回测历史数据
//		BaggingJ48FullModel nModel=new BaggingJ48FullModel();
		AdaboostFullModel nModel=new AdaboostFullModel();
//		MyNNFullModel nModel=new MyNNFullModel();
		if (applyToMaModelInTestBack==true){//用fullModel模型来测试均线模型时不用重新build和评估
			nModel.m_skipTrainInBacktest=true;
			nModel.m_skipEvalInBacktest=true;
		}	
		
		GeneralInstances nominalResult=testBackward(nModel);
		//不真正回测了，直接从以前的结果文件中加载
//		BaseInstances nominalResult=loadBackTestResultFromFile(nModel.getIdentifyName());

		//按连续分类器回测历史数据
		BaggingM5PFullModel cModel=new BaggingM5PFullModel();
//		BaggingRegressionFullModel cModel=new BaggingRegressionFullModel();
		if (applyToMaModelInTestBack==true){//用fullModel模型来测试均线模型时不用重新build和评估
			cModel.m_skipTrainInBacktest=true;
			cModel.m_skipEvalInBacktest=true;
		}

//		GeneralInstances continuousResult=testBackward(cModel);
		//不真正回测了，直接从以前的结果文件中加载
		GeneralInstances continuousResult=loadBackTestResultFromFile(cModel.getIdentifyName());
		
		//统一输出统计结果
		nModel.outputClassifySummary();
		cModel.outputClassifySummary();

		//输出用于计算收益率的CSV文件
		System.out.println("-----now output continuous predictions----------"+cModel.getIdentifyName());
		GeneralInstances m5pOutput=mergeResultWithData(continuousResult,nominalResult,ArffFormat.RESULT_PREDICTED_WIN_RATE,cModel.getModelArffFormat());
		saveSelectedFileForMarkets(m5pOutput,cModel.getIdentifyName());
		System.out.println("-----now output nominal predictions----------"+nModel.getIdentifyName());
		GeneralInstances mlpOutput=mergeResultWithData(nominalResult,continuousResult,ArffFormat.RESULT_PREDICTED_PROFIT,nModel.getModelArffFormat());
		saveSelectedFileForMarkets(mlpOutput,nModel.getIdentifyName());
		System.out.println("-----end of test backward------");
	}

	/**
	 * 隐藏父类的函数。
	 * @param splitTrainYearClause
	 * @param policy
	 * @return
	 */
	protected String getSplitClause(int policyIndex,String splitYearClause,	String policy) {
		return splitYearClause;
	}
	
	

	/**
	 * 子类覆盖
	 * @param clModel
	 * @return
	 * @throws Exception
	 */
	@Override
	protected GeneralInstances getBacktestInstances(BaseClassifier clModel,String splitMark,String policy)
			throws Exception {

		String arffFullFileName = null;
		if (applyToMaModelInTestBack==true){//用fullModel模型来测试均线模型时加载均线模型的arff
			arffFullFileName=getMaArffFileName(clModel);
		}else{
			arffFullFileName=getFullModelArffFileName(clModel);
		}
		GeneralInstances fullSetData;
		System.out.println("start to load File for fullset from File: "+ arffFullFileName  );
		fullSetData = DataIOHandler.getSuppier().loadDataFromFile( arffFullFileName);
		if (applyToMaModelInTestBack==true){//用fullModel模型来测试均线模型时加载均线模型的arff
			int pos = BaseInstanceProcessor.findATTPosition(fullSetData,ArffFormat.SELECTED_AVG_LINE);
			fullSetData = InstanceHandler.getHandler(fullSetData).removeAttribs(fullSetData,""+pos );
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


	
	protected GeneralInstances mergeResultWithData(GeneralInstances resultData,GeneralInstances referenceData,String dataToAdd,int format) throws Exception{
		GeneralInstances left=null;		
		//读取磁盘上预先保存的左侧数据
		if (applyToMaModelInTestBack==true){
			left=DataIOHandler.getSuppier().loadDataFromFile(EnvConstants.AVG_LINE_ROOT_DIR+ArffFormat.TRANSACTION_ARFF_PREFIX+"-left.arff");
		}else{
			left=DataIOHandler.getSuppier().loadDataFromFile(C_ROOT_DIRECTORY+ArffFormatFullModel.FULL_MODEL_ARFF_PREFIX+"-left.arff");
		}
		
		MergeClassifyResults merge=new MergeClassifyResults(shouyilv_thresholds, winrate_thresholds);
		GeneralInstances mergedResult = merge.mergeResults(resultData, referenceData,dataToAdd, left);
		
		//返回结果之前需要按TradeDate重新排序
		int tradeDateIndex=BaseInstanceProcessor.findATTPosition(mergedResult, ArffFormat.TRADE_DATE);
		mergedResult.sort(tradeDateIndex-1);
		// 给mergedResult瘦身。 2=yearmonth, 6=datadate,7=positive,8=bias
		String[] attributeToRemove=new String[]{ArffFormat.YEAR_MONTH,ArffFormat.DATA_DATE,ArffFormat.IS_POSITIVE,ArffFormat.BIAS5};
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(mergedResult);
		mergedResult=instanceProcessor.removeAttribs(mergedResult, attributeToRemove);
		
		if (applyToMaModelInTestBack==false){
			//插入一列“均线策略”为计算程序使用
			mergedResult=instanceProcessor.AddAttributeWithValue(mergedResult, ArffFormat.SELECTED_AVG_LINE,"numeric","0");
		}
		return mergedResult;

		

	}

}
