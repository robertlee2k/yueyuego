/**********************************************
//                   _ooOoo_
//                  o8888888o
//                  88" . "88
//                  (| -_- |)
//                  O\  =  /O
//               ____/`---'\____
//             .'  \\|     |//  `.
//            /  \\|||  :  |||//  \
//           /  _||||| -:- |||||-  \
//           |   | \\\  -  /// |   |
//           | \_|  ''\---/''  |   |
//           \  .-\__  `-`  ___/-. /
//         ___`. .'  /--.--\  `. . __
//      ."" '<  `.___\_<|>_/___.'  >'"".
//     | | :  `- \`.;`\ _ /`;.`/ - ` : | |
//     \  \ `-.   \_ __\ /__ _/   .-` /  /
//======`-.____`-.___\_____/___.-`____.-'======
//                   `=---='
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//             菩萨保佑       平安运转
//             纵有 BUG   也不亏钱
************************************************/
package yueyueGo;

/*
 * 谢谢悦悦的冠名支持
 */
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import yueyueGo.classifier.AdaboostClassifier;
import yueyueGo.classifier.BaggingM5P;
import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.dataFormat.AvgLineDataFormat;
import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.dataProcessor.InstanceHandler;
import yueyueGo.dataProcessor.WekaInstanceProcessor;
import yueyueGo.databeans.DataInstances;
import yueyueGo.databeans.GeneralDataTag;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaDataTag;
import yueyueGo.datasource.DataIOHandler;
import yueyueGo.datasource.GeneralDataSaver;
import yueyueGo.utility.AppContext;
import yueyueGo.utility.BlockedThreadPoolExecutor;
import yueyueGo.utility.ClassiferInitFactory;
import yueyueGo.utility.ClassifySummaries;
import yueyueGo.utility.FileUtility;
import yueyueGo.utility.MergeClassifyResults;
import yueyueGo.utility.ThresholdData;
import yueyueGo.utility.analysis.DataAnalysis;
import yueyueGo.utility.analysis.ShouyilvDescribe;

public class BackTest {
	protected String C_ROOT_DIRECTORY =EnvConstants.AVG_LINE_ROOT_DIR;
	protected int RUNNING_THREADS; //并发控制，1表示仅有主线程单线运行。
//	protected double[] shouyilv_thresholds=null; //对于胜率优先算法的收益率筛选阀值
//	protected double[] winrate_thresholds=null; //对于收益率优先算法的胜率筛选阀值
	
	protected String BACKTEST_RESULT_DIR=null;	
	public static final String RESULT_EXTENSION = "-Test Result.csv";
	protected String STRAGEY_NAME; // 策略的名称，只是用于输出。
	protected ArffFormat ARFF_FORMAT; //当前所用数据文件格式 
	public final static int BEGIN_FROM_POLICY=0; // 当回测需要跳过某些均线时，0表示不跳过
	protected String m_startYear= "2008";
	protected String m_endYearMonth="201708"; //结尾月一般是当前月，这个月是没有数据的，最新数据是上月的
	
	protected String[] m_handSetSplitYear=new String[] {};

	
	//初始化环境参数，运行本类的方法必须先调用它
	public void init(){
		RUNNING_THREADS=10;
		STRAGEY_NAME=
//				"动量策略";
				"均线策略";
		ARFF_FORMAT=
//				new MomentumDataFormat(); 
				new AvgLineDataFormat();
		AppContext.clearContext();
		AppContext.createContext(this.C_ROOT_DIRECTORY);	
		BACKTEST_RESULT_DIR=AppContext.getBACKTEST_RESULT_DIR();
//		shouyilv_thresholds=EvaluationConfDefinition.SHOUYILV_FILTER_FOR_WINRATE; //对于胜率优先算法的收益率筛选阀值
//		winrate_thresholds=EvaluationConfDefinition.WINRATE_FILTER_FOR_SHOUYILV; //对于收益率优先算法的胜率筛选阀值
		
	}

	
	public static void main(String[] args) {
		try {
			
			BackTest worker=new BackTest();
			worker.init();

			//调用回测函数回测
//			worker.callRebuildModels();
//			worker.callReEvaluateModels();
//			worker.callTestBack();
//			worker.callRefreshModelUseLatestData();
			
			worker.callDataAnlysis();
//			worker.testForModelStore();
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	public void callDataAnlysis() throws Exception{
	   BaseClassifier  cModel=new BaggingM5P();
	   GeneralInstances fulldata=getBacktestInstances(cModel);
	   String result=DataAnalysis.analyzeMarket(m_startYear+"01",m_endYearMonth,ARFF_FORMAT.m_policy_group,cModel.m_policySubGroup,fulldata,ShouyilvDescribe.ALL);
	   FileUtility.write(BACKTEST_RESULT_DIR+"marketAnalysis-Summary.csv", result, "GBK");
	   
	}
	
	/**
	 * 根据最新月的增量数据刷新模型
	 * @throws Exception
	 */
	protected void callRefreshModelUseLatestData() throws Exception{
		BaseClassifier model=null;
		
		m_startYear= "2017";
		m_endYearMonth="201708"; //结尾月一般是当前月，这个月是没有数据的，最新数据是上月的
//		m_handSetSplitYear=new String[] {"201701"};
		RUNNING_THREADS=5;
		
		//逐次构建新的模型
		model=new AdaboostClassifier();
		model.initModelPurpose(BaseClassifier.FOR_BUILD_MODEL);
		
		testBackward(model);
		
		model=new BaggingM5P();
		model.initModelPurpose(BaseClassifier.FOR_BUILD_MODEL);
		testBackward(model);

		//重新评估模型 （因为模型的评估数据受影响比构建数据受影响多，所以这里前推一年
		m_startYear= "2016";
//		m_handSetSplitYear=
//				new String[] {"201602","201603","201604","201605","201606","201607",
//						"201608","201609","201610","201611","201612",
//						"201701","201702","201703","201704","201705"};
		RUNNING_THREADS=15;
		
		//逐次构建新的模型
		model=new AdaboostClassifier();
		model.initModelPurpose(BaseClassifier.FOR_EVALUATE_MODEL);
		testBackward(model);
		
		model=new BaggingM5P();
		model.initModelPurpose(BaseClassifier.FOR_EVALUATE_MODEL);
		testBackward(model);
		
	}
	
	/**
	 * 全部重建模型模型
	 * @throws Exception
	 */	
	protected void callRebuildModels() throws Exception {
		RUNNING_THREADS=5;
		
//		m_handSetSplitYear=
//		new String[] {
		//为年度模型使用
//		  "2008","2009","2010","2011","2012","2013","2014","2015","2016","2017",
		//为半年度模型使用		
//		"200807","200907","201007","201107","201207","201307","201407","201507","201607",
		//为季度模型使用		
//		"200804","200904","201004","201104","201204","201304","201404","201504","201604","200810","200910","201010","201110","201210","201310","201410","201510","201610",
		//为月度模型使用		
//		"200801","200802","200803","200804","200805","200806","200807","200808","200809","200810","200811","200812","200901","200902","200903","200904","200905","200906","200907","200908","200909","200910","200911","200912","201001","201002","201003","201004","201005","201006","201007","201008","201009","201010","201011","201012","201101","201102","201103","201104","201105","201106","201107","201108","201109","201110","201111","201112","201201","201202","201203","201204","201205","201206","201207","201208","201209","201210","201211","201212","201301","201302","201303","201304","201305","201306","201307","201308","201309","201310","201311","201312","201401","201402","201403","201404","201405","201406","201407","201408","201409","201410","201411","201412","201501","201502","201503","201504","201505","201506","201507","201508","201509","201510","201511","201512","201601","201602","201603", "201604","201605","201606","201607","201608","201609","201610","201611"
//		};
		
		//按连续分类器回测历史数据
		BaggingM5P cModel=ClassiferInitFactory.initBaggingM5P(ARFF_FORMAT, BaseClassifier.FOR_BUILD_MODEL);
		testBackward(cModel);
		//不真正回测了，直接从以前的结果文件中加载
//		GeneralInstances continuousResult=loadBackTestResultFromFile(cModel.getIdentifyName());
		
		//按二分类器回测历史数据
		AdaboostClassifier nModel=ClassiferInitFactory.initAdaboost(ARFF_FORMAT, BaseClassifier.FOR_BUILD_MODEL);
		testBackward(nModel);
		//不真正回测了，直接从以前的结果文件中加载
//		GeneralInstances nominalResult=loadBackTestResultFromFile(nModel.getIdentifyName());
		

		//统一输出统计结果
		nModel.outputClassifySummary();
		cModel.outputClassifySummary();

	}	
	/**
	 * 重新评估全部模型
	 * @throws Exception
	 */	
	protected void callReEvaluateModels() throws Exception {
		RUNNING_THREADS=25;
		//按二分类器回测历史数据
		AdaboostClassifier nModel=ClassiferInitFactory.initAdaboost(ARFF_FORMAT, BaseClassifier.FOR_EVALUATE_MODEL);
		GeneralInstances nominalResult=testBackward(nModel);
		//不真正回测了，直接从以前的结果文件中加载
//		GeneralInstances nominalResult=loadBackTestResultFromFile(nModel.getIdentifyName());
		
		//按连续分类器回测历史数据
		BaggingM5P cModel=ClassiferInitFactory.initBaggingM5P(ARFF_FORMAT, BaseClassifier.FOR_EVALUATE_MODEL);
		GeneralInstances continuousResult=testBackward(cModel);
		//不真正回测了，直接从以前的结果文件中加载
//		GeneralInstances continuousResult=loadBackTestResultFromFile(cModel.getIdentifyName());
				


		outputStatistics(nModel, nominalResult, cModel, continuousResult);
	}


	protected void callTestBack() throws Exception {
		RUNNING_THREADS=25;
		//按连续分类器回测历史数据
		BaggingM5P cModel=ClassiferInitFactory.initBaggingM5P(ARFF_FORMAT, BaseClassifier.FOR_BACKTEST_MODEL);
//		GeneralInstances continuousResult=testBackward(cModel);
		//不真正回测了，直接从以前的结果文件中加载
		GeneralInstances continuousResult=loadBackTestResultFromFile(cModel.getIdentifyName());
		
		//按二分类器回测历史数据
		AdaboostClassifier nModel=ClassiferInitFactory.initAdaboost(ARFF_FORMAT, BaseClassifier.FOR_BACKTEST_MODEL);
//		GeneralInstances nominalResult=testBackward(nModel);
		//不真正回测了，直接从以前的结果文件中加载
		GeneralInstances nominalResult=loadBackTestResultFromFile(nModel.getIdentifyName());
		
		outputStatistics(nModel, nominalResult, cModel, continuousResult);
		
//		//统一输出统计结果
//		nModel.outputClassifySummary();
//		cModel.outputClassifySummary();
//		
//
//
//		//输出用于计算收益率的CSV文件
//		System.out.println("-----now output continuous predictions----------"+cModel.getIdentifyName() + " (filtered by nominal: "+nModel.getIdentifyName()+")");
//		GeneralInstances m5pOutput=mergeResultWithData(continuousResult,nominalResult,ArffFormat.RESULT_PREDICTED_WIN_RATE,cModel.getModelArffFormat());
//		saveSelectedFileForMarkets(m5pOutput,cModel.getIdentifyName());
//
//
//		System.out.println("-----now output nominal predictions----------"+nModel.getIdentifyName()+" (filtered by continuous: "+cModel.getIdentifyName()+")");
//		GeneralInstances mlpOutput=mergeResultWithData(nominalResult,continuousResult,ArffFormat.RESULT_PREDICTED_PROFIT,nModel.getModelArffFormat());
//		saveSelectedFileForMarkets(mlpOutput,nModel.getIdentifyName());
//		System.out.println("-----end of test backward------");
	}


	//历史回测
	protected  GeneralInstances testBackward(BaseClassifier clModel) throws Exception{
		
		//根据分类器和数据类别确定回测模型的工作目录
		String modelFilePath=prepareModelWorkPath(clModel);
		String modelPrefix=ARFF_FORMAT.m_arff_file_prefix+"("+ArffFormat.CURRENT_FORMAT+")"; //"extData2005-2016";
		
		GeneralInstances fullSetData = null;
		GeneralInstances result = null;
		
		
		//创建存储评估结果的数据容器
		ClassifySummaries modelSummaries=new ClassifySummaries(clModel.getIdentifyName()+" format="+clModel.modelArffFormat,false);

		clModel.setClassifySummaries(modelSummaries);

		System.out.println("test backward using classifier : "+clModel.getIdentifyName()+" @ model work path:"+modelFilePath+modelPrefix);
		
		
		 //创建一个可重用固定线程数的线程池
		ThreadPoolExecutor threadPool = null;
        Vector<GeneralInstances> threadResult=null;
        int threadPoolSize=0;
        
		// 按下面的逻辑创建线程池
		if ( RUNNING_THREADS>1 ){ 
			if (clModel instanceof ParrallelizedRunning){//模型内部有多线程，将外部线程进行系数转换
				threadPoolSize=((ParrallelizedRunning) clModel).recommendRunningThreads(RUNNING_THREADS);
			}else { //算法本身没有多线程，按原计划进行并发
				threadPoolSize=RUNNING_THREADS;
			}
        }
		if (threadPoolSize>1){
			threadPool=BlockedThreadPoolExecutor.newFixedThreadPool(threadPoolSize);
			threadResult=new Vector<GeneralInstances>();
			System.out.println("####Thread Pool Created , size="+threadPoolSize);
		}else{
			System.out.println("####Thread Pool will not be used");
		}
		
		
		String[] splitYear =generateSplitYearForModel(clModel,m_startYear,m_endYearMonth);
		
		// 别把数据文件里的ID变成Nominal的，否则读出来的ID就变成相对偏移量了
		for (int i = 0; i < splitYear.length; i++) { 
			
			String splitMark = splitYear[i];
			System.out.println("****************************start ****************************   "+splitMark);

			//获取分割年的clause
			GeneralDataTag[] splitYearTags = getSplitYearTags(clModel,splitMark);
			String splitTrainYearClause=splitYearTags[0].getSplitClause();
			String splitEvalYearClause=splitYearTags[1].getSplitClause();
			String splitTestYearClause=splitYearTags[2].getSplitClause();

			String policy = null;

			for (int j = BEGIN_FROM_POLICY; j < clModel.m_policySubGroup.length; j++) {
				
				policy = clModel.m_policySubGroup[j];
				// 加载原始arff文件
				if (fullSetData == null) {
					fullSetData = getBacktestInstances(clModel);
				}
				BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(fullSetData);
				// 准备输出数据格式
				if (result == null) {// initialize result instances
					result = prepareResultInstances(clModel, fullSetData);
				}
				int policyIndex=BaseInstanceProcessor.findATTPosition(fullSetData, ARFF_FORMAT.m_policy_group);
				String splitTrainClause = getSplitClause(policyIndex,splitTrainYearClause,policy);
				String splitEvalClause =  getSplitClause(policyIndex,splitEvalYearClause,policy);;
				String splitTestClause =  getSplitClause(policyIndex,splitTestYearClause, policy);
				
				GeneralInstances trainingData = null;
				
				if (clModel.is_skipTrainInBacktest() == false ){ //如果需要训练模型，则取训练数据 
					System.out.println("start to split training set from data: "+ splitTrainClause);
					trainingData=instanceProcessor.getInstancesSubset(fullSetData,splitTrainClause);
					int trainingDataSize=trainingData.numInstances();
					if (trainingDataSize>EnvConstants.TRAINING_DATA_LIMIT){
						trainingData=new DataInstances(trainingData,trainingDataSize-EnvConstants.TRAINING_DATA_LIMIT,EnvConstants.TRAINING_DATA_LIMIT);
					}
					//对于二分类器，这里要把输入的收益率转换为分类变量
					if (clModel instanceof NominalClassifier ){
						trainingData=((NominalClassifier)clModel).processDataForNominalClassifier(trainingData,false);
					}
					trainingData = instanceProcessor.removeAttribs(trainingData,  Integer.toString(ArffFormat.ID_POSITION)+","+ArffFormat.YEAR_MONTH_INDEX);

					System.out.println(" training data size , row : "
							+ trainingData.numInstances() + " column: "
							+ trainingData.numAttributes());					
				}

				GeneralInstances evaluationData = null;
				if (clModel.is_skipEvalInBacktest()==false || clModel.is_skipTrainInBacktest() == false  ){//如果需要评估模型，则取评估数据（训练时缺省要做一次评估）
					System.out.println("start to split evaluation set from  data: "+ splitEvalClause);
					evaluationData=instanceProcessor.getInstancesSubset(fullSetData,splitEvalClause);
					//对于二分类器，这里要把输入的收益率转换为分类变量
					if (clModel instanceof NominalClassifier ){
						evaluationData=((NominalClassifier)clModel).processDataForNominalClassifier(evaluationData,false);
					}
					evaluationData = instanceProcessor.removeAttribs(evaluationData,  Integer.toString(ArffFormat.ID_POSITION)+","+ArffFormat.YEAR_MONTH_INDEX);
					System.out.println(" evaluation data size , row : "
							+ evaluationData.numInstances() + " column: "
							+ evaluationData.numAttributes());
				}
	
				
				GeneralInstances testingData = null;				
				// prepare testing data
				System.out.println("start to split testing set: "+ splitTestClause);
				testingData = instanceProcessor.getInstancesSubset(fullSetData, splitTestClause);
				//处理testingData
				testingData = instanceProcessor.removeAttribs(testingData, ArffFormat.YEAR_MONTH_INDEX);

				//对于二分类器，这里要把输入的收益率转换为分类变量
				if (clModel instanceof NominalClassifier ){
					testingData=((NominalClassifier)clModel).processDataForNominalClassifier(testingData,true);
				}

				System.out.println(" testing raw data size , row : "
						+ testingData.numInstances() + " column: "
						+ testingData.numAttributes());
				
				//在不够强的机器上做模型训练时释放内存，改为每次从硬盘加载的方式
				if (clModel.is_skipTrainInBacktest() == false){
					fullSetData=null; //释放内存
				}				

	
				if (threadPool!=null){ //需要多线程并发
					//多线程的时候clone一个clModel执行任务，当前的Model继续走下去。
					ClassifySummaries commonSummaries=clModel.getClassifySummaries();
					clModel.setClassifySummaries(null); //不要clone classifySummaries，这个需要各线程同用一个对象
					BaseClassifier clModelClone=BaseClassifier.makeCopy(clModel);//利用序列化方法完整深度复制
					clModel.setClassifySummaries(commonSummaries);
					clModelClone.setClassifySummaries(commonSummaries);

					//多线程的时候clone一个空result执行分配给线程。
					DataInstances resultClone=new DataInstances(result);
					threadResult.add(resultClone);
					//创建实现了Runnable接口对象
					ProcessFlowExecutor t = new ProcessFlowExecutor(clModelClone, resultClone,splitMark, policy,trainingData,evaluationData,testingData,splitYearTags,modelFilePath,modelPrefix);
					//将线程放入池中进行执行
					threadPool.submit(t);

					//如果线程池已满，等待一下
					int waitCount=0;
					do {    
						//阻塞等待，直到有空余线程  ，虽然getActiveCount只是给大概的值，但因为只有主进程分发任务，这还是可以信赖的。
						Thread.sleep(2000);
						waitCount++;
						if (waitCount%30==0){
							System.out.println("waited for idle thread for seconds: "+ waitCount*2);
						}
					} while(threadPool.getActiveCount()==threadPool.getMaximumPoolSize());  
				}else{
					//不需要多线程并发的时候，还是按传统方式处理
					ProcessFlowExecutor worker=new ProcessFlowExecutor(clModel, result,splitMark, policy,trainingData,evaluationData,testingData,splitYearTags,modelFilePath,modelPrefix);
					worker.doPredictProcess();
					System.out.println("accumulated predicted rows: "+ result.numInstances());
				}

			} //end for (int j = BEGIN_FROM_POLICY

			System.out.println("********************complete **************************** " + splitMark);
			System.out.println(" ");
		}//end for (int i 

		if (threadPool!=null){ //需要多线程并发
			//全部线程已放入线程池，关闭线程池的入口。
			threadPool.shutdown();		

			//等待所有线程运行完毕
			try {  
				boolean loop = true;  
				do {    //等待所有任务完成  
					loop = !threadPool.awaitTermination(2, TimeUnit.SECONDS);  //阻塞，直到线程池里所有任务结束
				} while(loop);  
			} catch (InterruptedException e) {  
				e.printStackTrace();  
			}  
			//将所有线程的result合并
			BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(result);
			for (GeneralInstances temp : threadResult) {
			  result=instanceProcessor.mergeTwoInstances(result, temp);
			}
			
			threadResult.removeAllElements(); //释放内存
		}
        
		//保存评估结果至文件
		saveBacktestResultFile(result,clModel.getIdentifyName());
		
		FileUtility.write(BACKTEST_RESULT_DIR+ARFF_FORMAT.m_arff_file_prefix+"-"+clModel.getIdentifyName()+"-Summary.csv", modelSummaries.getEvaluationHeader()+modelSummaries.getEvaluationSummary(), "GBK");
		
		System.out.println(clModel.getIdentifyName()+" test result file saved.");
		return result;
	}
	

	/**
	 * 可以考虑子类中覆盖
	 * @param clModel
	 * @param fullSetData
	 * @return
	 * @throws Exception
	 */
	protected GeneralInstances prepareResultInstances(BaseClassifier clModel,
			GeneralInstances fullSetData) throws Exception {
		GeneralInstances result;
		DataInstances header = new DataInstances(fullSetData, 0);
		// 去除不必要的字段，保留ID（第1），YEARMONTH（第二）均线策略（第3）、bias5（第4）、收益率（最后一列）、增加预测值、是否被选择。
		int removeFromIndex=BaseInstanceProcessor.findATTPosition(fullSetData, ArffFormat.BIAS5)+1;
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(header);
//		ArffFormat.YEAR_MONTH_INDEX + ","+
		result = instanceProcessor.removeAttribs(header, removeFromIndex+"-"
				+ (header.numAttributes() - 1));
		if (clModel instanceof NominalClassifier ){
			result = instanceProcessor.AddAttribute(result, ArffFormat.RESULT_PREDICTED_WIN_RATE,
					result.numAttributes());
		}else{
			result = instanceProcessor.AddAttribute(result, ArffFormat.RESULT_PREDICTED_PROFIT,
					result.numAttributes());
		}
		result = instanceProcessor.AddAttribute(result, ArffFormat.RESULT_SELECTED,
				result.numAttributes());
		return result;
		
	}

	/**
	 * 可以考虑子类中覆盖
	 * @param clModel
	 * @return
	 * @throws Exception
	 */
	protected  GeneralInstances getBacktestInstances(BaseClassifier clModel)
			throws Exception {
		GeneralInstances fullSetData;
		// 根据模型来决定是否要使用有计算字段的ARFF
//		String arffFile=null;
//		if (clModel.m_noCaculationAttrib==true){
//			arffFile=ARFF_FORMAT.m_arff_ext;
//		}else{
////			arffFile=ArffFormat.LONG_ARFF_FILE;
//			throw new RuntimeException("we don't support Calculation fields any more");
//		}

		System.out.println("start to load File for fullset from File: "+ ARFF_FORMAT.m_arff_ext  );
		fullSetData = DataIOHandler.getSuppier().loadDataFromFile( C_ROOT_DIRECTORY+ARFF_FORMAT.m_arff_ext);
		System.out.println("finish loading fullset Data. row : "+ fullSetData.numInstances() + " column:"+ fullSetData.numAttributes());
		
//		//决定是否删除申万行业数据
//		if (clModel.m_removeSWData==true){
//			fullSetData=ArffFormat.removeSWData(fullSetData);
//			System.out.println("removed SW Data based on model definition. now column="+ fullSetData.numAttributes());
//		}
		return fullSetData;
	}

	/**
	 * 	从全量数据中获取分割training和eval以及test的clause， test数据比较简单，就是当月的。
	 * train和eval的逻辑由ModelStore定义:
	 */
	protected final GeneralDataTag[] getSplitYearTags(BaseClassifier clModel,String targetYearSplit) {
		String evalYearSplit=EvaluationStore.caculateEvalYearSplit(targetYearSplit, clModel.m_evalDataSplitMode);
		String modelYearSplit=ModelStore.caculateModelYearSplit(evalYearSplit, clModel.m_modelFileShareMode);

		//TODO 缺省用5年的训练数据
		String modelDataStartYearSplit=ModelStore.modelDataStartYearSplit(modelYearSplit, 5);
		System.out.println("模型构建数据 from "+modelDataStartYearSplit+" to "+modelYearSplit+"（评估数据切分日期="+evalYearSplit+" 模型共享模式="+clModel.m_modelFileShareMode+"）");
		GeneralDataTag[] dataTags=new WekaDataTag[3];
		
		dataTags[0]=new WekaDataTag(GeneralDataTag.TRAINING_DATA,modelDataStartYearSplit,modelYearSplit);
		dataTags[1]=new WekaDataTag(GeneralDataTag.EVALUATION_DATA,evalYearSplit,targetYearSplit);
		dataTags[2]=new WekaDataTag(GeneralDataTag.TESTING_DATA,targetYearSplit,targetYearSplit);
		
//		System.out.println("训练样本分割："+splitYearClauses[0]);
//		System.out.println("评估样本分割："+splitYearClauses[1]);
//		System.out.println("预测样本分割："+splitYearClauses[2]);
		return dataTags;
	}

	
	
	/**
	 * 根据policy拼出相应的分割表达式，可以在子类中被覆盖
	 * @param splitTrainYearClause
	 * @param policy
	 * @return
	 */
	protected String getSplitClause(int policyIndex,String splitYearClause,	String policy) {
		String splitClause;
		String splitPolicy;
		
		//如果没有splitPolicy则不需要对policy的部分处理
		if ("".equals(policy) || policy==null){
			splitPolicy="";
		}else{
			splitPolicy=" and (ATT"+policyIndex+" is '"	+ policy + "')";
		}
		splitClause = splitYearClause + splitPolicy ;
		return splitClause;
	}



	//合并格式，子类中可覆盖
	protected  GeneralInstances mergeResultWithData(GeneralInstances resultData,GeneralInstances referenceData,String dataToAdd,int format) throws Exception{
		//读取磁盘上预先保存的左侧数据
		GeneralInstances left=null;
		
//		if (format==ArffFormat.LEGACY_FORMAT){ //LEGACY 有少量模型尚使用原有格式作为结果对比
//			left=FileUtility.loadDataFromFile(C_ROOT_DIRECTORY+"AllTransaction20052016-left.arff");
//		}else if (format==ArffFormat.EXT_FORMAT){  
//			left=FileUtility.loadDataFromFile(C_ROOT_DIRECTORY+ArffFormat.TRANSACTION_ARFF_PREFIX+"-left.arff");
//		}

		left=DataIOHandler.getSuppier().loadDataFromFile(C_ROOT_DIRECTORY+ARFF_FORMAT.m_arff_file_prefix+"-left.arff");
		MergeClassifyResults merge=new MergeClassifyResults(ARFF_FORMAT.m_policy_group);
		GeneralInstances mergedResult =merge.mergeResults(resultData, referenceData,dataToAdd, left);

		//返回结果之前需要按TradeDate重新排序
		int tradeDateIndex=BaseInstanceProcessor.findATTPosition(mergedResult, ArffFormat.TRADE_DATE);
		mergedResult.sort(tradeDateIndex-1);
		
		// 给mergedResult瘦身。 2=yearmonth, 6=datadate,7=positive,8=bias   (尝试把ArffFormat.DATA_DATE,ArffFormat.YEAR_MONTH,保留）
		String[] attributeToRemove=new String[]{ArffFormat.IS_POSITIVE,ArffFormat.BIAS5};
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(mergedResult);
		mergedResult=instanceProcessor.removeAttribs(mergedResult, attributeToRemove);


		// 将策略分组改名为外部收益率计算程序所需要的名字
		if (AvgLineDataFormat.SELECTED_AVGLINE.equals(ARFF_FORMAT.m_policy_group)==false){
			mergedResult=instanceProcessor.renameAttribute(mergedResult, ARFF_FORMAT.m_policy_group, AvgLineDataFormat.SELECTED_AVGLINE);
		}
		return mergedResult;
	}



	private void saveBacktestResultFile(GeneralInstances result,String classiferName) throws IOException{
		DataIOHandler.getSaver().SaveDataIntoFile(result, BACKTEST_RESULT_DIR+"回测结果-"+ARFF_FORMAT.m_arff_file_prefix+"-"+ classiferName+".arff" );
	}
	protected GeneralInstances loadBackTestResultFromFile(String classiferName) throws Exception{
		GeneralInstances result=DataIOHandler.getSuppier().loadDataFromFile(BACKTEST_RESULT_DIR+"回测结果-"+ARFF_FORMAT.m_arff_file_prefix+"-"+ classiferName+".arff" );
		return result;
	}

	protected void saveSelectedFileForMarkets(GeneralInstances selected,String classiferName) throws Exception{
		GeneralDataSaver dataSaver=DataIOHandler.getSaver();		
		dataSaver.saveCSVFile(selected, BACKTEST_RESULT_DIR+"选股-"+ ARFF_FORMAT.m_arff_file_prefix+"-"+classiferName+ RESULT_EXTENSION );
		
	}


	/**
	 * @param model
	 * @param fullOutput
	 * @return
	 * @throws Exception
	 */
	private GeneralInstances returnSelectedInstances(GeneralInstances fullOutput)
			throws Exception {
		//返回选股结果		
		int pos = BaseInstanceProcessor.findATTPosition(fullOutput,ArffFormat.RESULT_SELECTED);
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(fullOutput);
		GeneralInstances fullMarketSelected=instanceProcessor.getInstancesSubset(fullOutput, WekaInstanceProcessor.WEKA_ATT_PREFIX +pos+" = "+BaseClassifier.VALUE_SELECTED);
		return fullMarketSelected;
	}


	


//	protected void testForModelStore(){
//		AdaboostClassifier nModel=new AdaboostClassifier();
//		boolean[] mode=new boolean[] {true,false};
//		int[] evalDataSplitMode=new int[]{0,6,9,12};
//		int[] modelFileShareMode=new int[]{1,3,6,12};
//	
//	
//		for (int m : modelFileShareMode) {
//			for (int j : evalDataSplitMode) {
//				for (boolean k : mode) {
//					nModel.m_skipTrainInBacktest=k;
//					nModel.m_evalDataSplitMode=j;
//					nModel.m_modelFileShareMode=m;
//					System.out.println("=====================================================================================");
//					System.out.println("参数设置：跳过构建="+k+"/评估数据模式="+j+"/模型共享模式="+m);
//					String[] splitYear =generateSplitYearForModel(nModel,m_startYear,m_endYearMonth);
//					for (int i = 0; i < splitYear.length; i++) { 
//						String splitMark = splitYear[i];
//						System.out.println("=====");
//						getSplitYearTags(nModel,splitMark);
//					}
//					System.out.println("=====================================================================================");
//				}
//			}
//			
//		}
//	}


	/**
	 * @param nModel
	 * @param nominalResult
	 * @param cModel
	 * @param continuousResult
	 * @throws Exception
	 */
	protected void outputStatistics(NominalClassifier nModel, GeneralInstances nominalResult, ContinousClassifier cModel,
			GeneralInstances continuousResult) throws Exception {
		String timeRange=m_startYear+"-"+m_endYearMonth;
		//统一输出统计结果
		nModel.outputClassifySummary();
		cModel.outputClassifySummary();

		System.out.println(" now output the full distribution of results:");
		DataAnalysis.analyzeDataDistribution(ARFF_FORMAT.m_policy_group,cModel.m_policySubGroup,timeRange,continuousResult,cModel.classifierName);
	
		//输出用于计算收益率的CSV文件
		System.out.println("-----now output continuous predictions----------"+cModel.getIdentifyName() + " (filtered by nominal: "+nModel.getIdentifyName()+")");
		System.out.println(" now output the uncombined results");
		GeneralInstances selectedInstances=returnSelectedInstances(continuousResult);
		DataAnalysis.analyzeMarket(m_startYear+"01",m_endYearMonth,ARFF_FORMAT.m_policy_group,cModel.m_policySubGroup,selectedInstances,cModel.classifierName);
		System.out.println(" now output the combined results");
		GeneralInstances m5pOutput=mergeResultWithData(continuousResult,nominalResult,ArffFormat.RESULT_PREDICTED_WIN_RATE,cModel.getModelArffFormat());
		selectedInstances=returnSelectedInstances(m5pOutput);
		DataAnalysis.analyzeMarket(m_startYear+"01",m_endYearMonth,ARFF_FORMAT.m_policy_group,cModel.m_policySubGroup,selectedInstances,cModel.classifierName);
		this.saveSelectedFileForMarkets(selectedInstances, cModel.getIdentifyName());
		
	
		System.out.println("-----now output nominal predictions----------"+nModel.getIdentifyName()+" (filtered by continuous: "+cModel.getIdentifyName()+")");
		System.out.println(" now output the uncombined results");
		selectedInstances=returnSelectedInstances(nominalResult);
		DataAnalysis.analyzeDataDistribution(ARFF_FORMAT.m_policy_group,nModel.m_policySubGroup,timeRange,selectedInstances,nModel.classifierName);
		System.out.println(" now output the combined results");
		GeneralInstances mlpOutput=mergeResultWithData(nominalResult,continuousResult,ArffFormat.RESULT_PREDICTED_PROFIT,nModel.getModelArffFormat());
		selectedInstances=returnSelectedInstances(mlpOutput);
		DataAnalysis.analyzeDataDistribution(ARFF_FORMAT.m_policy_group,nModel.m_policySubGroup,timeRange,selectedInstances,nModel.classifierName);
		this.saveSelectedFileForMarkets(selectedInstances, nModel.getIdentifyName());
		System.out.println("-----end of test backward------");
	}


	protected String[] generateSplitYearForModel(BaseClassifier clModel,String startYear,String endYearMonth){
	
		String[] result=null;
		if(clModel.is_skipTrainInBacktest()==false){ //需要构建模型
			switch (clModel.m_modelFileShareMode){
			case ModelStore.MONTHLY_MODEL: //这个是全量模型
				result=manipulateYearMonth(startYear,endYearMonth,1);
				break;
			case ModelStore.YEAR_SHARED_MODEL:	 //生成年度模型 
				result=manipulateYearMonth(startYear,endYearMonth,12);
				break;
			case ModelStore.QUARTER_SHARED_MODEL: //生成季度模型
				result=manipulateYearMonth(startYear,endYearMonth,3);
				break;
			case ModelStore.HALF_YEAR_SHARED_MODEL:	//生成半年度模型
				result=manipulateYearMonth(startYear,endYearMonth,6);
				break;
			}
		}else{//不需要构建模型，则按月生成所有的数据即可
			result=manipulateYearMonth(startYear,endYearMonth,1);
		}
		//调用手工覆盖的函数接口
		String[] needOverride=overrideSplitYear();
		if(needOverride.length>0){
			result=needOverride;
		}
		
		System.out.println(" splitYear size="+result.length);
		for (String string : result) {
			System.out.print(string+",");
		}
		System.out.println("");
		return result;
	}


	/*
	 * 根据给定的起始年和终止月份自动生成回测的年月阶段
	 */
	private String[] manipulateYearMonth(String a_startYear,String endYearMonth, int interval){
		int startYear=Integer.parseInt(a_startYear);	
		String[] result=null;
		String currentYearMonth=endYearMonth;//FormatUtility.getCurrentYearMonth();
		int currentYear=Integer.parseInt(currentYearMonth.substring(0,4)); 
		int currentMonth=Integer.parseInt(currentYearMonth.substring(4,6));
	
		int size=0;
		int pos=0;
		//计算有多少个周期，当前月是没有数据的，最新数据是上月的currentMonth-1,然后再去减1的原因是超过了interval才会周期进位加一（比如201703除以interval 3 为1，但不进位）
		size=(currentYear-startYear)*(12/interval)+(currentMonth-1-1)/interval+1; 
		result=new String[size];
		pos=0;
		for (int year=startYear;year<=currentYear;year++){
			for (int month=1;month<=12;month+=interval){
				if (year==currentYear && month>currentMonth-1){ //当前年的当前月之后是没有数据的
					break;
				}
				result[pos]=""+(year*100+month);
				pos++;
			}
		}
		return result;
	}


	 /*
	  * 设置回测时模型和评估文件的存储位置
	  */
	protected String prepareModelWorkPath(BaseClassifier clModel){
		String workPath=null;
		if (clModel instanceof ContinousClassifier){
			workPath=AppContext.getCONTINOUS_CLASSIFIER_DIR()+clModel.getIdentifyName()+"\\";
		}else if (clModel instanceof NominalClassifier){
			workPath=AppContext.getNOMINAL_CLASSIFIER_DIR()+clModel.getIdentifyName()+"\\";
		}
		//根据不同的原始数据（策略）设置不同的模型工作目录
		workPath+=ARFF_FORMAT.m_arff_file_prefix+"\\";
		FileUtility.mkdirIfNotExist(workPath);

		return workPath;
	}


	/**
	 * 如果需要手动设置 splitYear的内容，则在此方法中设置
	 */
	protected String[] overrideSplitYear() {
		return m_handSetSplitYear;
		
	}

	
	protected HashMap<String,String> findModelFiles(BaseClassifier clModel,String a_targetYearSplit) throws Exception{
		//根据分类器和数据类别确定回测模型的工作目录
		String modelFilePath=prepareModelWorkPath(clModel);
		String modelPrefix=ARFF_FORMAT.m_arff_file_prefix+"("+ArffFormat.CURRENT_FORMAT+")"; 
		EvaluationStore evaluationStore=null;
		
		HashMap<String,String> fileMap=new HashMap<String,String>();
		String policySplit = null;
		for (int j = BEGIN_FROM_POLICY; j < clModel.m_policySubGroup.length; j++) {
			policySplit = clModel.m_policySubGroup[j];
			
			//查找最新的EvaluationStore
			evaluationStore=clModel.locateEvalutationStore(a_targetYearSplit,policySplit,modelFilePath,modelPrefix);
			//获取评估数据
			ThresholdData thresholdData=evaluationStore.loadDataFromFile();

			//从评估结果中找到评估文件。
			String evalFullName=evaluationStore.getEvalFileName();
			
			//TODO 未来要对这个加校验
//			evaluationStore.m_modelFileShareMode!=Threshold.m_modelFileShareMode
					
					
			//从评估结果中找到正向模型文件。		
			String modelFullname=thresholdData.getModelFileName()+ModelStore.MODEL_FILE_EXTENSION;
			//从评估结果中找到反向模型文件。
			String reversedFullname=thresholdData.getReversedModelFileName()+ModelStore.MODEL_FILE_EXTENSION;
			
			//通过hashmap消除重复的文件名
			fileMap.put(evalFullName+ModelStore.TXT_EXTENSION, modelFilePath);
			fileMap.put(evalFullName, modelFilePath);
			fileMap.put(modelFullname, modelFilePath);
			fileMap.put(reversedFullname, modelFilePath);
		}
		return fileMap;
	}


}