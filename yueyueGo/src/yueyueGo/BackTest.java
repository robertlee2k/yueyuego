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
 * 感谢悦悦的冠名支持:-)
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
import yueyueGo.datasource.DataIOHandler;
import yueyueGo.datasource.GeneralDataSaver;
import yueyueGo.utility.AppContext;
import yueyueGo.utility.BlockedThreadPoolExecutor;
import yueyueGo.utility.ClassifySummaries;
import yueyueGo.utility.FileUtility;
import yueyueGo.utility.FormatUtility;
import yueyueGo.utility.ParrallelizedRunning;
import yueyueGo.utility.YearMonthProcessor;
import yueyueGo.utility.analysis.DataAnalysis;
import yueyueGo.utility.analysis.ShouyilvDescriptiveList;
import yueyueGo.utility.modelEvaluation.EvaluationStore;
import yueyueGo.utility.modelEvaluation.ModelStore;
import yueyueGo.utility.modelEvaluation.ThresholdData;
import yueyueGo.utility.modelPredict.ResultsHolder;
import yueyueGo.utility.modelPredict.TargetSelectRatioConfig;

public class BackTest {
	public static final String RESULT_EXTENSION = "-Test Result.csv";
	public final static int BEGIN_FROM_POLICY = 0; // 当回测需要跳过某些均线时，0表示不跳过

	protected boolean m_concurrent = true; // 并发控制，缺省为并发
	protected String m_backtest_result_dir = null;
	protected String m_currentPolicy; // 策略的名称，只是用于输出。
	protected ArffFormat m_currentArffFormat; // 当前所用数据文件格式

	protected String m_startYearMonth = "200801"; //回测的起始月，包含这个月的数据（设置时不一定需要从1月份开始的）
	protected String m_endYearMonth = "201712"; // 回测的结尾月(该月数据为文件中最新数据）

	protected String[] m_handSetSplitYear = new String[] {};

	// 初始化环境参数，运行本类的方法必须先调用它
	public void init() {
		m_currentPolicy =
				// "动量策略";
				"均线策略";
		m_currentArffFormat =
				// new MomentumDataFormat();
				new AvgLineDataFormat();
		AppContext.clearContext();
		AppContext.createContext(m_currentArffFormat.m_data_root_directory);
		m_backtest_result_dir = AppContext.getBACKTEST_RESULT_DIR();

	}

	public static void main(String[] args) {
		try {

			BackTest worker = new BackTest();
			worker.init();


			// 调用回测函数回测
//			worker.callRebuildModels();
			worker.callReEvaluateModels();
			worker.callTestBack();
//			worker.callRefreshModelUseLatestData();
//			worker.callDataAnlysis();

//			worker.callRenameModels();
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public void callDataAnlysis() throws Exception {
		AbstractModel cModel = new BaggingM5P();
		GeneralInstances fulldata = getBacktestInstances(cModel);
		ShouyilvDescriptiveList shouyilvDescriptions = DataAnalysis.analyzeByMarketTrend("原始数据", m_startYearMonth,
				m_endYearMonth, m_currentArffFormat.m_policy_group, cModel.m_policySubGroup, fulldata);

		String outputCSV = shouyilvDescriptions.convertVerticallyToCSV();
		FileUtility.write(m_backtest_result_dir + "marketAnalysis-Summary.csv", outputCSV, "GBK");

	}

	/**
	 * 根据最新月的增量数据刷新模型
	 * 
	 * @throws Exception
	 */
	protected void callRefreshModelUseLatestData() throws Exception {
		AbstractModel model = null;

		m_startYearMonth = "201701";
		m_endYearMonth = "201709";
		// m_handSetSplitYear=new String[] {"201701"};

		// 逐次构建新的模型
		model = AdaboostClassifier.initModel(m_currentArffFormat, AbstractModel.FOR_BUILD_MODEL);
		testBackward(model);

		model = BaggingM5P.initModel(m_currentArffFormat, AbstractModel.FOR_BUILD_MODEL);
		testBackward(model);

		// 重新评估模型 （因为模型的评估数据受影响比构建数据受影响多，所以这里前推一年
		m_startYearMonth = "201601";
		// m_handSetSplitYear=
		// new String[] {"201602","201603","201604","201605","201606","201607",
		// "201608","201609","201610","201611","201612",
		// "201701","201702","201703","201704","201705"};

		// 逐次构建新的模型
		model = AdaboostClassifier.initModel(m_currentArffFormat, AbstractModel.FOR_EVALUATE_MODEL);
		testBackward(model);

		model = BaggingM5P.initModel(m_currentArffFormat, AbstractModel.FOR_EVALUATE_MODEL);
		testBackward(model);

	}

	/**
	 * 全部重建模型模型
	 * 
	 * @throws Exception
	 */
	protected void callRebuildModels() throws Exception {

		// m_handSetSplitYear=
		// new String[] {
		// 为年度模型使用
		// "2008","2009","2010","2011","2012","2013","2014","2015","2016","2017",
		// 为半年度模型使用
		// "200807","200907","201007","201107","201207","201307","201407","201507","201607",
		// 为季度模型使用
		// "200804","200904","201004","201104","201204","201304","201404","201504","201604","200810","200910","201010","201110","201210","201310","201410","201510","201610",
		// 为月度模型使用
		// "200801","200802","200803","200804","200805","200806","200807","200808","200809","200810","200811","200812","200901","200902","200903","200904","200905","200906","200907","200908","200909","200910","200911","200912","201001","201002","201003","201004","201005","201006","201007","201008","201009","201010","201011","201012","201101","201102","201103","201104","201105","201106","201107","201108","201109","201110","201111","201112","201201","201202","201203","201204","201205","201206","201207","201208","201209","201210","201211","201212","201301","201302","201303","201304","201305","201306","201307","201308","201309","201310","201311","201312","201401","201402","201403","201404","201405","201406","201407","201408","201409","201410","201411","201412","201501","201502","201503","201504","201505","201506","201507","201508","201509","201510","201511","201512","201601","201602","201603",
		// "201604","201605","201606","201607","201608","201609","201610","201611"
		// };

		// 按连续分类器回测历史数据
		BaggingM5P cModel = BaggingM5P.initModel(m_currentArffFormat, AbstractModel.FOR_BUILD_MODEL);
		//用6个月的评估区段（不用9个月，免得少生成一个模型）
		cModel.m_evalDataSplitMode=EvaluationStore.USE_HALF_YEAR_DATA_FOR_EVAL;

		//根据modelStore中的定义数组，构建使用不同年份数据的模型
		for (int dataYear : ModelStore.DATA_YEARS_TO_COMPARE) {
			cModel.setUseNYearForTraining(dataYear);	
			testBackward(cModel);
		}
		
		// 不真正回测了，直接从以前的结果文件中加载
		// GeneralInstances
		// continuousResult=loadBackTestResultFromFile(cModel.getIdentifyName());

		// 按二分类器回测历史数据
		AdaboostClassifier nModel = AdaboostClassifier.initModel(m_currentArffFormat, AbstractModel.FOR_BUILD_MODEL);
		//用6个月的评估区段（不用9个月，免得少生成一个模型）
		nModel.m_evalDataSplitMode=EvaluationStore.USE_HALF_YEAR_DATA_FOR_EVAL;

		//根据modelStore中的定义数组，构建使用不同年份数据的模型
		for (int dataYear : ModelStore.DATA_YEARS_TO_COMPARE) {
			nModel.setUseNYearForTraining(dataYear);	
			testBackward(nModel);
		}
		
		// 不真正回测了，直接从以前的结果文件中加载
		// GeneralInstances
		// nominalResult=loadBackTestResultFromFile(nModel.getIdentifyName());

		// 统一输出统计结果
		nModel.outputClassifySummary();
		cModel.outputClassifySummary();

		System.out.println("-----end of rebuild models------");
	}

	/**
	 * 重新评估全部模型
	 * 
	 * @throws Exception
	 */
	protected void callReEvaluateModels() throws Exception {

		// 按二分类器回测历史数据
		AdaboostClassifier nModel = AdaboostClassifier.initModel(m_currentArffFormat, AbstractModel.FOR_EVALUATE_MODEL);
		ResultsHolder nominalResult = testBackward(nModel);
		// 不真正回测了，直接从以前的结果文件中加载
		// GeneralInstances
		// nominalResult=loadBackTestResultFromFile(nModel.getIdentifyName());

		// 按连续分类器回测历史数据
		BaggingM5P cModel = BaggingM5P.initModel(m_currentArffFormat, AbstractModel.FOR_EVALUATE_MODEL);
		ResultsHolder continuousResult = testBackward(cModel);
		// 不真正回测了，直接从以前的结果文件中加载
		// GeneralInstances
		// continuousResult=loadBackTestResultFromFile(cModel.getIdentifyName());

		saveResultsAndStatistics(nModel, nominalResult, cModel, continuousResult);

		System.out.println("-----end of reevaluation models------");
	}

	protected void callTestBack() throws Exception {
		// 按二分类器回测历史数据
		AdaboostClassifier nModel = AdaboostClassifier.initModel(m_currentArffFormat, AbstractModel.FOR_BACKTEST_MODEL);
		ResultsHolder nominalResult = testBackward(nModel);
		// 不真正回测了，直接从以前的结果文件中加载
		// GeneralInstances
		// nominalResult=loadBackTestResultFromFile(nModel.getIdentifyName());

		// 按连续分类器回测历史数据
		BaggingM5P cModel = BaggingM5P.initModel(m_currentArffFormat, AbstractModel.FOR_BACKTEST_MODEL);
		ResultsHolder continuousResult = testBackward(cModel);
		// 不真正回测了，直接从以前的结果文件中加载
		// GeneralInstances
		// continuousResult=loadBackTestResultFromFile(cModel.getIdentifyName());

		saveResultsAndStatistics(nModel, nominalResult, cModel, continuousResult);

		System.out.println("-----end of test backward------");

	}

	// 历史回测
	protected ResultsHolder testBackward(AbstractModel clModel) throws Exception {

		// 根据分类器和数据类别确定回测模型的工作目录
		String modelFilePath = prepareModelWorkPath(clModel);
		String modelPrefix = m_currentArffFormat.getModelDataPrefix();

		GeneralInstances fullSetData = null;
		ResultsHolder selectedResult = null;

		// 获取选股比例配置的实例
		TargetSelectRatioConfig selectRatioConfig=new TargetSelectRatioConfig(clModel,m_currentArffFormat);
		clModel.setSelectRatioConfig(selectRatioConfig);
		
		// 创建存储评估结果的数据容器
		ClassifySummaries modelSummaries = new ClassifySummaries(
				clModel.getIdentifyName() + " format=" + clModel.modelArffFormat, false);

		clModel.setClassifySummaries(modelSummaries);

		System.out.println("test backward using classifier : " + clModel.getIdentifyName() + " @ model work path:"
				+ modelFilePath + modelPrefix);

		// 创建一个可重用固定线程数的线程池
		ThreadPoolExecutor threadPool = null;
		Vector<ResultsHolder> threadResult = null;
		int threadPoolSize = 0;

		// 如果采用并发模式，则按下面的逻辑创建线程池
		if (m_concurrent == true) {
			threadPoolSize = estimateConcurrentThreads(clModel);
		}

		if (threadPoolSize > 1) {
			threadPool = BlockedThreadPoolExecutor.newFixedThreadPool(threadPoolSize);
			threadResult = new Vector<ResultsHolder>();
			System.out.println("####Thread Pool Created , size=" + threadPoolSize);
		} else {
			System.out.println("####Thread Pool will not be used");
		}

		String[] splitYear = generateSplitYearForModel(clModel, m_startYearMonth, m_endYearMonth);

		// 别把数据文件里的ID变成Nominal的，否则读出来的ID就变成相对偏移量了
		for (int i = 0; i < splitYear.length; i++) {

			String splitMark = splitYear[i];
			System.out.println("****************************start ****************************   " + splitMark);

			// 获取分割年的clause
			GeneralDataTag[] splitYearTags = YearMonthProcessor.getDataSplitTags(clModel, splitMark);
			String splitTrainYearClause = splitYearTags[0].getSplitClause();
			String splitEvalYearClause = splitYearTags[1].getSplitClause();
			String splitTestYearClause = splitYearTags[2].getSplitClause();

			String policy = null;

			for (int j = BEGIN_FROM_POLICY; j < clModel.m_policySubGroup.length; j++) {

				policy = clModel.m_policySubGroup[j];
				
				
				// 加载原始arff文件
				if (fullSetData == null) {
					fullSetData = getBacktestInstances(clModel);
				}
				// 准备输出数据格式
				if (selectedResult == null) {// initialize result instances
					selectedResult = new ResultsHolder(clModel,fullSetData, m_currentArffFormat);
				}
				
				BaseInstanceProcessor instanceProcessor = InstanceHandler.getHandler(fullSetData);

				int policyIndex = BaseInstanceProcessor.findATTPosition(fullSetData,
						m_currentArffFormat.m_policy_group);
				String splitTrainClause = appendSplitClause(splitTrainYearClause, policyIndex, policy);
				String splitEvalClause = appendSplitClause(splitEvalYearClause, policyIndex, policy);
				;
				String splitTestClause = appendSplitClause(splitTestYearClause, policyIndex, policy);

				GeneralInstances trainingData = null;

				if (clModel.is_skipTrainInBacktest() == false) { // 如果需要训练模型，则取训练数据
					System.out.println("start to split training set from data: " + splitTrainClause);
					trainingData = instanceProcessor.getInstancesSubset(fullSetData, splitTrainClause);
					int trainingDataSize = trainingData.numInstances();
					if (trainingDataSize > EnvConstants.TRAINING_DATA_LIMIT) {
						trainingData = new DataInstances(trainingData,
								trainingDataSize - EnvConstants.TRAINING_DATA_LIMIT, EnvConstants.TRAINING_DATA_LIMIT);
					}
					// 对于二分类器，这里要把输入的收益率转换为分类变量
					if (clModel instanceof NominalModel) {
						trainingData = ((NominalModel) clModel).processDataForNominalClassifier(trainingData);
					}
					trainingData = instanceProcessor.removeAttribs(trainingData,
							Integer.toString(ArffFormat.ID_POSITION) + "-" + ArffFormat.YEAR_MONTH_INDEX);

					System.out.println(" training data size , row : " + trainingData.numInstances() + " column: "
							+ trainingData.numAttributes());
				}

				GeneralInstances evaluationData = null;
				if (clModel.is_skipEvalInBacktest() == false || clModel.is_skipTrainInBacktest() == false) {// 如果需要评估模型，则取评估数据（训练后有个confusion matrix也需要评估数据）
					System.out.println("start to split evaluation set from  data: " + splitEvalClause);
					evaluationData = instanceProcessor.getInstancesSubset(fullSetData, splitEvalClause);
					// 对于二分类器，这里要把输入的收益率转换为分类变量
					if (clModel instanceof NominalModel) {
						evaluationData = ((NominalModel) clModel).processDataForNominalClassifier(evaluationData);
					}
					evaluationData = instanceProcessor.removeAttribs(evaluationData,
							Integer.toString(ArffFormat.ID_POSITION) + "-" + ArffFormat.YEAR_MONTH_INDEX);
					System.out.println(" evaluation data size , row : " + evaluationData.numInstances() + " column: "
							+ evaluationData.numAttributes());
				}

				GeneralInstances testingData = null;
				if (clModel.is_skipPredictInBacktest() == false) {// 如果需要预测，则取评估数据
					// prepare testing data
					System.out.println("start to split testing set: " + splitTestClause);
					testingData = instanceProcessor.getInstancesSubset(fullSetData, splitTestClause);
					// 处理testingData：注意，预测数据的处理方式不大一样，需要保留ID和tradeDate，由预测方法内部处理

					System.out.println(" testing raw data size , row : " + testingData.numInstances() + " column: "
							+ testingData.numAttributes());
				}
				
	
				// // 在不够强的机器上做模型训练时释放内存，改为每次从硬盘加载的方式
				// if (clModel.is_skipTrainInBacktest() == false) {
				// fullSetData = null; // 释放内存
				// }

				if (threadPool != null) { // 需要多线程并发
					// 多线程的时候clone一个clModel执行任务，当前的Model继续走下去。
					ClassifySummaries commonSummaries = clModel.getClassifySummaries();
					clModel.setClassifySummaries(null); // 不要clone
														// classifySummaries，这个需要各线程同用一个对象
					AbstractModel clModelClone = AbstractModel.makeCopy(clModel);// 利用序列化方法完整深度复制
					clModel.setClassifySummaries(commonSummaries);
					clModelClone.setClassifySummaries(commonSummaries);

					// 多线程的时候clone一个空result执行分配给线程。
					ResultsHolder resultClone = ResultsHolder.makeCopy(selectedResult);
					threadResult.add(resultClone);
					// 创建实现了Runnable接口对象
					ProcessFlowExecutor t = new ProcessFlowExecutor(clModelClone, resultClone, splitMark, policy,
							trainingData, evaluationData, testingData, splitYearTags, modelFilePath, modelPrefix);
					// 将线程放入池中进行执行
					threadPool.submit(t);

					// 如果线程池已满，等待一下
					int waitCount = 0;
					do {
						// 阻塞等待，直到有空余线程
						// ，虽然getActiveCount只是给大概的值，但因为只有主进程分发任务，这还是可以信赖的。
						Thread.sleep(2000);
						waitCount++;
						if (waitCount % 30 == 0) {
							System.out.println("waited for idle thread for seconds: " + waitCount * 2);
						}
					} while (threadPool.getActiveCount() == threadPool.getMaximumPoolSize());
				} else {
					// 不需要多线程并发的时候，还是按传统方式处理
					ProcessFlowExecutor worker = new ProcessFlowExecutor(clModel, selectedResult, splitMark, policy,
							trainingData, evaluationData, testingData, splitYearTags, modelFilePath, modelPrefix);
					worker.doPredictProcess();
					System.out.println("accumulated predicted rows: " + selectedResult.getResultInstances().numInstances());
				}

			} // end for (int j = BEGIN_FROM_POLICY

			System.out.println("********************complete **************************** " + splitMark);
			System.out.println(" ");
		} // end for (int i

		if (threadPool != null) { // 需要多线程并发
			// 全部线程已放入线程池，关闭线程池的入口。
			threadPool.shutdown();

			// 等待所有线程运行完毕
			try {
				boolean loop = true;
				do { // 等待所有任务完成
					loop = !threadPool.awaitTermination(2, TimeUnit.SECONDS); // 阻塞，直到线程池里所有任务结束
				} while (loop);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 将所有线程的result合并
			for (ResultsHolder threadResultsHolder : threadResult) {
				selectedResult.addResults(threadResultsHolder);
			}

			threadResult.removeAllElements(); // 释放内存
		}

		// 保存评估结果至文件
		saveBacktestResultFile(selectedResult.getResultInstances(), clModel.getIdentifyName());

		FileUtility.write(
				m_backtest_result_dir + m_currentArffFormat.getModelDataPrefix() + "-" + clModel.getIdentifyName()
						+ "-monthlyAUCs.csv",
				modelSummaries.getEvaluationHeader() + modelSummaries.getEvaluationSummary(), "GBK");

		FileUtility.write(
				m_backtest_result_dir + m_currentArffFormat.getModelDataPrefix() + "-" + clModel.getIdentifyName()
						+ "-dailySummary.csv",
				modelSummaries.getDailyHeader() + modelSummaries.getDailySummary(), "GBK");

		System.out.println(clModel.getIdentifyName() + " test result file saved.");
		return selectedResult;
	}

	/**
	 * 可以考虑子类中覆盖
	 * 
	 * @param clModel
	 * @return
	 * @throws Exception
	 */
	protected GeneralInstances getBacktestInstances(AbstractModel clModel) throws Exception {
		GeneralInstances fullSetData;

		String traingArffFileName = AppContext.getC_ROOT_DIRECTORY() + m_currentArffFormat.getTrainingDataFileName();
		System.out.println("start to load File for fullset from File: " + traingArffFileName);
		fullSetData = DataIOHandler.getSuppier().loadDataFromFile(traingArffFileName);
		System.out.println("finish loading fullset Data. row : " + fullSetData.numInstances() + " column:"
				+ fullSetData.numAttributes());

		return fullSetData;
	}

	/**
	 * 根据policy拼出相应的分割表达式，可以在子类中被覆盖
	 * 
	 * @param splitTrainYearClause
	 * @param policy
	 * @return
	 */
	public static String appendSplitClause(String originClause, int policyIndex, String policy) {
		String splitPolicy;

		// 如果没有splitPolicy则不需要对policy的部分处理
		if ("".equals(policy) || policy == null) {
			splitPolicy = "";
		} else { // 如果需要添加splitPolicy
			if ("".equals(originClause)) { // 如果传入条件为空，就无须添加"and"
				splitPolicy = "";
			} else { // 否则添加Add
				splitPolicy = " and";
			}
			// 将用"-"分割的policy拆分
			String[] policyArray = policy.split("-");
			if (policyArray.length == 1) { // 没有需要拆分的policy组合，传入参数就是单一的policy
				splitPolicy += " (" + WekaInstanceProcessor.WEKA_ATT_PREFIX + policyIndex + " = " + policy + ")";
			} else { // 有要拆分的
				splitPolicy += " (";
				for (int i = 0; i < policyArray.length - 1; i++) {
					splitPolicy += " (" + WekaInstanceProcessor.WEKA_ATT_PREFIX + policyIndex + " = " + policyArray[i]
							+ ") or ";
				}
				splitPolicy += " (" + WekaInstanceProcessor.WEKA_ATT_PREFIX + policyIndex + " = "
						+ policyArray[policyArray.length - 1] + ")";
				splitPolicy += " )";
			}
			// " is '" + policy + "')";
		}
		String splitClause = originClause + splitPolicy;
		return splitClause;
	}

	// 合并格式，子类中可覆盖
	protected GeneralInstances mergeResultWithData(ResultsHolder resultData, ResultsHolder referenceData, int format)
			throws Exception {
		// 读取磁盘上预先保存的左侧数据
		GeneralInstances left = null;

		// if (format==ArffFormat.LEGACY_FORMAT){ //LEGACY 有少量模型尚使用原有格式作为结果对比
		// left=FileUtility.loadDataFromFile(C_ROOT_DIRECTORY+"AllTransaction20052016-left.arff");
		// }else if (format==ArffFormat.EXT_FORMAT){
		// left=FileUtility.loadDataFromFile(C_ROOT_DIRECTORY+ArffFormat.TRANSACTION_ARFF_PREFIX+"-left.arff");
		// }

		left = DataIOHandler.getSuppier()
				.loadDataFromFile(AppContext.getC_ROOT_DIRECTORY() + m_currentArffFormat.getLeftDataFileName());

		GeneralInstances mergedResult = resultData.mergeResults(referenceData, left);

		// 返回结果之前需要按TradeDate重新排序
		int tradeDateIndex = BaseInstanceProcessor.findATTPosition(mergedResult, ArffFormat.TRADE_DATE);
		mergedResult.sort(tradeDateIndex - 1);

		// 给mergedResult瘦身。 2=yearmonth, 6=datadate,7=positive,8=bias
		// (尝试把ArffFormat.DATA_DATE,ArffFormat.YEAR_MONTH,保留）
		String[] attributeToRemove = new String[] { ArffFormat.IS_POSITIVE, ArffFormat.BIAS5 };
		BaseInstanceProcessor instanceProcessor = InstanceHandler.getHandler(mergedResult);
		mergedResult = instanceProcessor.removeAttribs(mergedResult, attributeToRemove);

		// 将策略分组改名为外部收益率计算程序所需要的名字
		if (AvgLineDataFormat.SELECTED_AVGLINE.equals(m_currentArffFormat.m_policy_group) == false) {
			mergedResult = instanceProcessor.renameAttribute(mergedResult, m_currentArffFormat.m_policy_group,
					AvgLineDataFormat.SELECTED_AVGLINE);
		}
		return mergedResult;
	}

	private void saveBacktestResultFile(GeneralInstances result, String classiferName) throws IOException {
		DataIOHandler.getSaver().SaveDataIntoFile(result, m_backtest_result_dir + "回测结果-"
				+ m_currentArffFormat.getModelDataPrefix() + "-" + classiferName + ".arff");
	}

	protected GeneralInstances loadBackTestResultFromFile(String classiferName) throws Exception {
		GeneralInstances result = DataIOHandler.getSuppier().loadDataFromFile(m_backtest_result_dir + "回测结果-"
				+ m_currentArffFormat.getModelDataPrefix() + "-" + classiferName + ".arff");
		return result;
	}

	protected void saveSelectedFileForMarkets(GeneralInstances selected, String classiferName) throws Exception {
		GeneralDataSaver dataSaver = DataIOHandler.getSaver();
		dataSaver.saveCSVFile(selected, m_backtest_result_dir + "选股-" + m_currentArffFormat.getModelDataPrefix() + "-"
				+ classiferName + RESULT_EXTENSION);

	}

	// protected void testForModelStore(){
	// AdaboostClassifier nModel=new AdaboostClassifier();
	// boolean[] mode=new boolean[] {true,false};
	// int[] evalDataSplitMode=new int[]{0,6,9,12};
	// int[] modelFileShareMode=new int[]{1,3,6,12};
	//
	//
	// for (int m : modelFileShareMode) {
	// for (int j : evalDataSplitMode) {
	// for (boolean k : mode) {
	// nModel.m_skipTrainInBacktest=k;
	// nModel.m_evalDataSplitMode=j;
	// nModel.m_modelFileShareMode=m;
	// System.out.println("=====================================================================================");
	// System.out.println("参数设置：跳过构建="+k+"/评估数据模式="+j+"/模型共享模式="+m);
	// String[] splitYear
	// =generateSplitYearForModel(nModel,m_startYear,m_endYearMonth);
	// for (int i = 0; i < splitYear.length; i++) {
	// String splitMark = splitYear[i];
	// System.out.println("=====");
	// getSplitYearTags(nModel,splitMark);
	// }
	// System.out.println("=====================================================================================");
	// }
	// }
	//
	// }
	// }

	/**
	 * @param nModel
	 * @param nominalResult
	 * @param cModel
	 * @param continuousResult
	 * @throws Exception
	 */
	protected void saveResultsAndStatistics(NominalModel nModel, ResultsHolder nominalResult, ContinousModel cModel,
			ResultsHolder continuousResult) throws Exception {

		String[] targetPolicies = { "5", "10", "20", "30", "60" }; // 缺省输出各种均线分布
		// targetPolicies=cModel.m_policySubGroup;//按模型输出分布

		// 输出原始数据的收益率分析统计结果 （分别为按牛熊市场和按月）
		ShouyilvDescriptiveList[] marketTrendAnalysis = new ShouyilvDescriptiveList[3];
		ShouyilvDescriptiveList[] monthlyShouyilvAnalysis = new ShouyilvDescriptiveList[3];

		marketTrendAnalysis[0] = DataAnalysis.analyzeByMarketTrend(ShouyilvDescriptiveList.ORIGINAL_DATA,
				m_startYearMonth, m_endYearMonth, m_currentArffFormat.m_policy_group, targetPolicies,
				continuousResult.getResultInstances());
		monthlyShouyilvAnalysis[0] = DataAnalysis.analyzeByMonth(ShouyilvDescriptiveList.ORIGINAL_DATA, m_startYearMonth,
				m_endYearMonth, m_currentArffFormat.m_policy_group, targetPolicies,
				continuousResult.getResultInstances());

		// 输出连续分类器的收益率分析统计结果
		saveStatisiticsForOneModel(cModel, continuousResult, nModel, nominalResult, targetPolicies, marketTrendAnalysis,
				monthlyShouyilvAnalysis);

		// targetPolicies=nModel.m_policySubGroup;//按模型输出分布
		// //再输出一遍原始数据的原因是，连续分类器和二分类器的policy分组可能不一样，统计口径不一样
		// shouyilvDescriptionsArray[0]=DataAnalysis.analyzeMarket("原始数据",m_startYear+"01",m_endYearMonth,m_currentArffFormat.m_policy_group,targetPolicies,nominalResult);

		// 输出二分类器的收益率分析统计结果
		saveStatisiticsForOneModel(nModel, nominalResult, cModel, continuousResult, targetPolicies, marketTrendAnalysis,
				monthlyShouyilvAnalysis);

	}

	/**
	 * 输出单模型的选股分析统计
	 * 
	 * @param secondaryModel
	 * @param secondaryResult
	 * @param mainModel
	 * @param mainResult
	 * @param targetPolicies
	 * @param marketTrendAnalysis
	 * @throws Exception
	 * @throws IOException
	 */
	private void saveStatisiticsForOneModel(AbstractModel mainModel, ResultsHolder mainResult,
			AbstractModel secondaryModel, ResultsHolder secondaryResult, String[] targetPolicies,
			ShouyilvDescriptiveList[] marketTrendAnalysis, ShouyilvDescriptiveList[] monthlyShouyilvAnalysis)
			throws Exception, IOException {
		// 输出主分类器结果和参数
		String cModelSummary = mainModel.outputClassifySummary();

		// 输出主分类器的收益率分析统计结果
		System.out.println("\r\n-----now output main model predictions----------" + mainModel.getIdentifyName());
		GeneralInstances selectedInstances = ResultsHolder.returnSelectedInstances(mainResult.getResultInstances());
		marketTrendAnalysis[1] = DataAnalysis.analyzeByMarketTrend("单模型选股:" + mainModel.getIdentifyName(),
				m_startYearMonth , m_endYearMonth, m_currentArffFormat.m_policy_group, targetPolicies,
				selectedInstances);
		monthlyShouyilvAnalysis[1] = DataAnalysis.analyzeByMonth("单模型选股:" + mainModel.getIdentifyName(), m_startYearMonth,
				m_endYearMonth, m_currentArffFormat.m_policy_group, targetPolicies, selectedInstances);

		// 用另一个模型合并选股
		GeneralInstances classifyResult = mergeResultWithData(mainResult, secondaryResult,
				mainModel.getModelArffFormat());
		selectedInstances = ResultsHolder.returnSelectedInstances(classifyResult);
		System.out.println("--filtered by secondary classifier:( " + secondaryModel.getIdentifyName() + ")");
		marketTrendAnalysis[2] = DataAnalysis.analyzeByMarketTrend("双模型选股:" + mainModel.getIdentifyName(),
				m_startYearMonth, m_endYearMonth, m_currentArffFormat.m_policy_group, targetPolicies,
				selectedInstances);
		monthlyShouyilvAnalysis[2] = DataAnalysis.analyzeByMonth("双模型选股:" + mainModel.getIdentifyName(), m_startYearMonth,
				m_endYearMonth, m_currentArffFormat.m_policy_group, targetPolicies, selectedInstances);

		// 输出上述收益率分析的csv文件(按市场和按月）
		String output = ShouyilvDescriptiveList.mergeHorizontallyToCSV(marketTrendAnalysis[0], marketTrendAnalysis[1],
				marketTrendAnalysis[2]) + "\r\n\r\n\r\n" + cModelSummary;
		FileUtility.write(m_backtest_result_dir + "市场牛熊区间分析-" + mainModel.getIdentifyName()
				+ FormatUtility.getDateStringFor(0) + ".csv", output, "GBK");
		// 按月分析
		String monthlyOutput = ShouyilvDescriptiveList.mergeHorizontallyToCSV(monthlyShouyilvAnalysis[0],
				monthlyShouyilvAnalysis[1], monthlyShouyilvAnalysis[2]) + "\r\n\r\n\r\n" + cModelSummary;
		FileUtility.write(m_backtest_result_dir + "按月分析-" + mainModel.getIdentifyName()
				+ FormatUtility.getDateStringFor(0) + ".csv", monthlyOutput, "GBK");

		// 保存连续分类器已选股结果的CSV文件
		this.saveSelectedFileForMarkets(selectedInstances, mainModel.getIdentifyName());
	}

	protected String[] generateSplitYearForModel(AbstractModel clModel, String startYearMonth, String endYearMonth) throws Exception {

		String[] result = null;
		int interval=0;
		if (clModel.is_skipTrainInBacktest() == false) { // 需要构建模型
			interval=clModel.m_modelFileShareMode;
			
//			switch (clModel.m_modelFileShareMode) {
//			case ModelStore.MONTHLY_MODEL: // 这个是全量模型
//				result = YearMonthProcessor.manipulateYearMonth(startYearMonth, endYearMonth, 1);
//				break;
//			case ModelStore.YEAR_SHARED_MODEL: // 生成年度模型
//				result = YearMonthProcessor.manipulateYearMonth(startYearMonth, endYearMonth, 12);
//				break;
//			case ModelStore.QUARTER_SHARED_MODEL: // 生成季度模型
//				result = YearMonthProcessor.manipulateYearMonth(startYearMonth, endYearMonth, 3);
//				break;
//			case ModelStore.HALF_YEAR_SHARED_MODEL: // 生成半年度模型
//				result = YearMonthProcessor.manipulateYearMonth(startYearMonth, endYearMonth, 6);
//				break;
//			}
		} else {// 不需要构建模型，则按月生成所有的数据即可
			interval=1;
		}
		
		result = YearMonthProcessor.manipulateYearMonth(startYearMonth, endYearMonth, interval);
		// 调用手工覆盖的函数接口
		String[] needOverride = overrideSplitYear();
		if (needOverride.length > 0) {
			result = needOverride;
		}

		YearMonthProcessor.printYearMonthArray(interval, result);

		return result;
	}

	/*
	 * 设置回测时模型和评估文件的存储位置
	 */
	protected String prepareModelWorkPath(AbstractModel clModel) {
		String workPath = null;
		if (clModel instanceof ContinousModel) {
			workPath = AppContext.getCONTINOUS_CLASSIFIER_DIR() + clModel.getIdentifyName() + "\\";
		} else if (clModel instanceof NominalModel) {
			workPath = AppContext.getNOMINAL_CLASSIFIER_DIR() + clModel.getIdentifyName() + "\\";
		}
		// 根据不同的原始数据（策略）设置不同的模型工作目录
		workPath += m_currentArffFormat.getModelSubDirecotry() + "\\";
		FileUtility.mkdirIfNotExist(workPath);

		return workPath;
	}

	/**
	 * 如果需要手动设置 splitYear的内容，则在此方法中设置
	 */
	protected String[] overrideSplitYear() {
		return m_handSetSplitYear;

	}

	protected HashMap<String, String> findModelFiles(AbstractModel clModel, String a_targetYearSplit) throws Exception {
		// 根据分类器和数据类别确定回测模型的工作目录
		String modelFilePath = prepareModelWorkPath(clModel);
		String modelPrefix = m_currentArffFormat.getModelDataPrefix();
		EvaluationStore evaluationStore = null;

		HashMap<String, String> fileMap = new HashMap<String, String>();
		String policySplit = null;
		for (int j = BEGIN_FROM_POLICY; j < clModel.m_policySubGroup.length; j++) {
			policySplit = clModel.m_policySubGroup[j];

			// 查找最新的EvaluationStore
			evaluationStore = clModel.locateEvalutationStore(a_targetYearSplit, policySplit, modelFilePath,
					modelPrefix);
			// 获取评估数据
			ThresholdData thresholdData = evaluationStore.loadDataFromFile();

			// 从评估结果中找到评估文件。
			String evalFullName = evaluationStore.getEvalFileName();

			// TODO 未来要对这个加校验
			// evaluationStore.m_modelFileShareMode!=Threshold.m_modelFileShareMode

			// 从评估结果中找到正向模型文件。
			String modelFullname = thresholdData.getModelFileName() + ModelStore.MODEL_FILE_EXTENSION;
			// 从评估结果中找到反向模型文件。
			String reversedFullname = thresholdData.getReversedModelFileName() + ModelStore.MODEL_FILE_EXTENSION;

			// 通过hashmap消除重复的文件名
			fileMap.put(evalFullName + ModelStore.TXT_EXTENSION, modelFilePath);
			fileMap.put(evalFullName, modelFilePath);
			fileMap.put(modelFullname, modelFilePath);
			fileMap.put(reversedFullname, modelFilePath);
		}
		return fileMap;
	}

	/**
	 * 根据传入的目标年份找出该模型最近的数据年份
	 * @param targetYearSplit
	 * @param clModel
	 * @return
	 */
	public static String findNearestModelYearSplit(String targetYearSplit, AbstractModel clModel) {
		int modelFileShareMode=clModel.m_modelFileShareMode;
		int evalDataSplitMode=clModel.m_evalDataSplitMode;

		//根据modelDataSplitMode推算出评估数据的起始区间 （目前主要有三种： 最近6个月、9个月、12个月）
		String evalYearSplit=YearMonthProcessor.caculateEvalYearSplit(targetYearSplit,evalDataSplitMode);

		//历史数据切掉评估数据后再去getModelYearSplit看选多少作为模型构建数据（因为不是每月都有模型,要根据modelEvalFileShareMode来定）
		String modelYearSplit=YearMonthProcessor.caculateModelYearSplit(evalYearSplit,modelFileShareMode);
		//		modelYearSplit = legacyModelName(modelYearSplit);
		return modelYearSplit;
	}

	/*
	 * 计算可以并发线程的数量
	 */
	public static int estimateConcurrentThreads(AbstractModel classifier) {
		int threadNum = 1;
		// 先按一般的模型计算线程数
		if (classifier.is_skipTrainInBacktest() == false) { // 模型需要训练，这个所需内存比较大
			threadNum = 8;
		} else if (classifier.is_skipEvalInBacktest() == false) { // 模型需要评估，这个需要内存中等
			threadNum = EnvConstants.CPU_CORE_NUMBER + EnvConstants.CPU_CORE_NUMBER / 2;
		} else { // 只要单纯回测，这个内存无须太多，可以全开线程
			threadNum = EnvConstants.CPU_CORE_NUMBER * 2;
		}

		// 对于模型自身内部就有多线程的分类器，将外部线程进行系数转换
		if (classifier instanceof ParrallelizedRunning) {// 模型内部有
			threadNum = ((ParrallelizedRunning) classifier).recommendRunningThreads(threadNum);
		}

		return threadNum;
	}
	
	
	/*
	 * 以下为临时工具类
	 */
	protected void callRenameModels() throws Exception {

		AdaboostClassifier nModel = AdaboostClassifier.initModel(m_currentArffFormat, AbstractModel.FOR_BACKTEST_MODEL);
		renameModels(nModel);

		BaggingM5P cModel = BaggingM5P.initModel(m_currentArffFormat, AbstractModel.FOR_BACKTEST_MODEL);
		renameModels(cModel);
		System.out.println("-----end of rename process------");

	}
	
	protected void renameModels(AbstractModel clModel) throws Exception {
		String[] splitYear = generateSplitYearForModel(clModel, m_startYearMonth, m_endYearMonth);
		// 根据分类器和数据类别确定回测模型的工作目录
		String modelFilePath = prepareModelWorkPath(clModel);
		String modelPrefix = m_currentArffFormat.getModelDataPrefix();

		// 别把数据文件里的ID变成Nominal的，否则读出来的ID就变成相对偏移量了
		for (int i = 0; i < splitYear.length; i++) {
			String yearSplit = splitYear[i];

			String policySplit = null;

			//初始化回测创建模型时使用的modelStore对象（按yearSplit和policysplit分割处理）
			String classifierName=clModel.classifierName;
			String modelYearSplit = BackTest.findNearestModelYearSplit(yearSplit, clModel);
			for (int j = BEGIN_FROM_POLICY; j < clModel.m_policySubGroup.length; j++) {
				policySplit = clModel.m_policySubGroup[j];	

				ModelStore modelStore=new ModelStore(modelYearSplit, policySplit, modelFilePath, modelPrefix, classifierName,clModel.m_useRecentNYearForTraining) ;
				String newName=modelStore.getWorkFilePath()+modelStore.getModelFileName();
				String legacyName=modelStore.getWorkFilePath()+modelStore.getLegacyModelFileName();

				FileUtility.renameFile(newName+ModelStore.MODEL_FILE_EXTENSION, legacyName+ModelStore.MODEL_FILE_EXTENSION);  
				FileUtility.renameFile(newName+ModelStore.TXT_EXTENSION, legacyName+ModelStore.TXT_EXTENSION);
			}
		}
		
	}

}