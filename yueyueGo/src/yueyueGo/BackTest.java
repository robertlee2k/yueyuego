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

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import weka.core.Attribute;
import weka.core.Instances;
import yueyueGo.classifier.BaggingLinearRegression;
import yueyueGo.classifier.MyNNClassifier;
import yueyueGo.utility.AppContext;
import yueyueGo.utility.BlockedThreadPoolExecutor;
import yueyueGo.utility.ClassifySummaries;
import yueyueGo.utility.ClassifyUtility;
import yueyueGo.utility.FileUtility;
import yueyueGo.utility.FormatUtility;
import yueyueGo.utility.InstanceUtility;
import yueyueGo.utility.MergeClassifyResults;

public class BackTest {

	protected String C_ROOT_DIRECTORY =EnvConstants.AVG_LINE_ROOT_DIR;
	protected String BACKTEST_RESULT_DIR=null;
	protected int RUNNING_THREADS; //doOneModel时的并发，1表示仅有主线程单线运行。
	
	public static final String RESULT_EXTENSION = "-Test Result.csv";
	
	protected String STRAGEY_NAME; // 策略的名称，只是用于输出。
	
	private final static int BEGIN_FROM_POLICY=0; // 当回测需要跳过某些均线时，0表示不跳过
	
	protected String[] splitYear =null;

	protected  double[] shouyilv_thresholds; //对于胜率优先算法的收益率筛选阀值
	protected  double[] winrate_thresholds; //对于收益率优先算法的胜率筛选阀值


	//初始化环境参数，运行本类的方法必须先调用它
	public void init(){
		
		STRAGEY_NAME="均线策略";
		AppContext.clearContext();
		AppContext.createContext(this.C_ROOT_DIRECTORY);	
		BACKTEST_RESULT_DIR=AppContext.getBACKTEST_RESULT_DIR();
		

		RUNNING_THREADS=20;

		shouyilv_thresholds=new double[] {0.005,0.005,0.01,0.03,0.03};//{0.01,0.02,0.03,0.03,0.04};//{0,0,0,0,0};//{-100,-100,-100,-100,-100};
		winrate_thresholds=new double[] {0.45,0.45,0.45,0.35,0.25};//{0.5,0.5,0.5,0.5,0.5};//{0,0,0,0,0};//{0.3,0.3,0.3,0.3,0.3};
		
		splitYear=new String[] {
		//为年度模型使用
		  "2008","2009","2010","2011","2012","2013","2014","2015","2016",
		//为半年度模型使用		
		"200807","200907","201007","201107","201207","201307","201407","201507","201607"
		//为月度模型使用		
//		"200801","200802","200803","200804","200805","200806","200807","200808","200809","200810","200811","200812","200901","200902","200903","200904","200905","200906","200907","200908","200909","200910","200911","200912","201001","201002","201003","201004","201005","201006","201007","201008","201009","201010","201011","201012","201101","201102","201103","201104","201105","201106","201107","201108","201109","201110","201111","201112","201201","201202","201203","201204","201205","201206","201207","201208","201209","201210","201211","201212","201301","201302","201303","201304","201305","201306","201307","201308","201309","201310","201311","201312","201401","201402","201403","201404","201405","201406","201407","201408","201409","201410","201411","201412","201501","201502","201503","201504","201505","201506","201507","201508","201509","201510","201511","201512","201601","201602","201603", "201604","201605","201606","201607","201608"
		//生成预测使用模型		
//		"201607"	
		//生成预测所用的模型		
//		"201608"				

		};		
		
	}
	
	public static void main(String[] args) {
		try {
			BackTest worker=new BackTest();
			worker.init();

			//调用回测函数回测
			worker.callTestBack();
			
			//用最新的单次交易数据，更新原始的交易数据文件
//			UpdateHistoryArffFile.callRefreshInstances();

			//合并历史扩展数据
//			UpdateHistoryArffFile.callMergeExtData();
			
//			UpdateHistoryArffFile.createTransInstances();
			

			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

	/**
	 * @throws Exception
	 * @throws IOException
	 */
	protected void callTestBack() throws Exception, IOException {
		//按二分类器回测历史数据
		
		//神经网络
//		BaggingJ48 nModel=new BaggingJ48();
//		MLPABClassifier nModel = new MLPABClassifier();
		MyNNClassifier nModel=new MyNNClassifier(); 
//		AdaboostClassifier nModel=new AdaboostClassifier();


//		Instances nominalResult=testBackward(nModel);
		//不真正回测了，直接从以前的结果文件中加载
		Instances nominalResult=loadBackTestResultFromFile(nModel.getIdentifyName());

		//按连续分类器回测历史数据
//		BaggingM5P cModel=new BaggingM5P();
		BaggingLinearRegression cModel=new BaggingLinearRegression();

		Instances continuousResult=testBackward(cModel);
		//不真正回测了，直接从以前的结果文件中加载
//		Instances continuousResult=loadBackTestResultFromFile(cModel.getIdentifyName());
		
		//统一输出统计结果
		nModel.outputClassifySummary();
		cModel.outputClassifySummary();

		//输出用于计算收益率的CSV文件
		System.out.println("-----now output continuous predictions----------"+cModel.getIdentifyName());
		Instances m5pOutput=mergeResultWithData(continuousResult,nominalResult,ArffFormat.RESULT_PREDICTED_WIN_RATE,cModel.getModelArffFormat());
		saveSelectedFileForMarkets(m5pOutput,cModel.getIdentifyName());
		System.out.println("-----now output nominal predictions----------"+nModel.getIdentifyName());
		Instances mlpOutput=mergeResultWithData(nominalResult,continuousResult,ArffFormat.RESULT_PREDICTED_PROFIT,nModel.getModelArffFormat());
		saveSelectedFileForMarkets(mlpOutput,nModel.getIdentifyName());
		System.out.println("-----end of test backward------");
	}



	
	
	//用模型预测数据
	
	//历史回测
	protected  Instances testBackward(BaseClassifier clModel) throws Exception,
			IOException {
		Instances fullSetData = null;
		Instances result = null;
		
		
		//创建存储评估结果的数据容器
		ClassifySummaries modelSummaries=new ClassifySummaries(clModel.getIdentifyName(),false);
		clModel.setClassifySummaries(modelSummaries);

		System.out.println("test backward using classifier : "+clModel.getIdentifyName()+" @ model work path :"+clModel.WORK_PATH);
		
		
		 //创建一个可重用固定线程数的线程池
		ThreadPoolExecutor threadPool = null;
        Vector<Instances> threadResult=null;
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
			threadResult=new Vector<Instances>();
			System.out.println("####Thread Pool Created , size="+threadPoolSize);
		}else{
			System.out.println("####Thread Pool will not be used");
		}

		
		// 别把数据文件里的ID变成Nominal的，否则读出来的ID就变成相对偏移量了
		for (int i = 0; i < splitYear.length; i++) { 
			
			String splitMark = splitYear[i];
			System.out.println("****************************start ****************************   "+splitMark);

			//获取分割年的clause
			String[] splitYearClauses = splitYearClause(splitMark);
			String splitTrainYearClause=splitYearClauses[0];
			String splitEvalYearClause=splitYearClauses[1];
			String splitTestYearClause=splitYearClauses[2];

			String policy = null;

			for (int j = BEGIN_FROM_POLICY; j < clModel.m_policySubGroup.length; j++) {
				
				policy = clModel.m_policySubGroup[j];
				// 加载原始arff文件
				if (fullSetData == null) {
					fullSetData = getBacktestInstances(clModel,splitMark,policy);
				}

				// 准备输出数据格式
				if (result == null) {// initialize result instances
					result = prepareResultInstances(clModel, fullSetData);
				}
				int policyIndex=InstanceUtility.findATTPosition(fullSetData, ArffFormat.SELECTED_AVG_LINE);
				String splitTrainClause = getSplitClause(policyIndex,splitTrainYearClause,policy);
				String splitEvalClause =  getSplitClause(policyIndex,splitEvalYearClause,policy);;
				String splitTestClause =  getSplitClause(policyIndex,splitTestYearClause, policy);
				
				Instances trainingData = null;
				if (clModel.m_skipTrainInBacktest == false ){ //如果需要训练模型，则取训练数据 
					System.out.println("start to split training set from data: "+ splitTrainClause);
					trainingData=InstanceUtility.getInstancesSubset(fullSetData,splitTrainClause);
					int trainingDataSize=trainingData.numInstances();
					if (trainingDataSize>EnvConstants.TRAINING_DATA_LIMIT){
						trainingData=new Instances(trainingData,trainingDataSize-EnvConstants.TRAINING_DATA_LIMIT,EnvConstants.TRAINING_DATA_LIMIT);
					}
					//对于二分类器，这里要把输入的收益率转换为分类变量
					if (clModel instanceof NominalClassifier ){
						trainingData=((NominalClassifier)clModel).processDataForNominalClassifier(trainingData,false);
					}
					trainingData = InstanceUtility.removeAttribs(trainingData,  Integer.toString(ArffFormat.ID_POSITION)+","+ArffFormat.YEAR_MONTH_INDEX);

					System.out.println(" training data size , row : "
							+ trainingData.numInstances() + " column: "
							+ trainingData.numAttributes());					
				}

				Instances evaluationData = null;
				if (clModel.m_skipEvalInBacktest==false || clModel.m_skipTrainInBacktest == false  ){//如果需要评估模型，则取评估数据（训练时缺省要做一次评估）
					System.out.println("start to split evaluation set from  data: "+ splitEvalClause);
					evaluationData=InstanceUtility.getInstancesSubset(fullSetData,splitEvalClause);
					//对于二分类器，这里要把输入的收益率转换为分类变量
					if (clModel instanceof NominalClassifier ){
						evaluationData=((NominalClassifier)clModel).processDataForNominalClassifier(evaluationData,false);
					}
					evaluationData = InstanceUtility.removeAttribs(evaluationData,  Integer.toString(ArffFormat.ID_POSITION)+","+ArffFormat.YEAR_MONTH_INDEX);
					System.out.println(" evaluation data size , row : "
							+ evaluationData.numInstances() + " column: "
							+ evaluationData.numAttributes());
				}
	
				
				Instances testingData = null;				
				// prepare testing data
				System.out.println("start to split testing set: "+ splitTestClause);
				testingData = InstanceUtility
						.getInstancesSubset(fullSetData, splitTestClause);
				//处理testingData
				testingData = InstanceUtility.removeAttribs(testingData, ArffFormat.YEAR_MONTH_INDEX);

				//对于二分类器，这里要把输入的收益率转换为分类变量
				if (clModel instanceof NominalClassifier ){
					testingData=((NominalClassifier)clModel).processDataForNominalClassifier(testingData,true);
				}

				System.out.println(" testing raw data size , row : "
						+ testingData.numInstances() + " column: "
						+ testingData.numAttributes());
				
				//在不够强的机器上做模型训练时释放内存，改为每次从硬盘加载的方式
				if (clModel.m_skipTrainInBacktest == false){
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
					Instances resultClone=new Instances(result);
					threadResult.add(resultClone);
					//创建实现了Runnable接口对象
					ProcessFlowExecutor t = new ProcessFlowExecutor(clModelClone, resultClone,splitMark, policy,trainingData,evaluationData,testingData);
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
					ProcessFlowExecutor worker=new ProcessFlowExecutor(clModel, result,splitMark, policy,trainingData,evaluationData,testingData);
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
			for (Instances temp : threadResult) {
			  result=InstanceUtility.mergeTwoInstances(result, temp);
			}
			
			threadResult.removeAllElements(); //释放内存
		}
        
		FileUtility.write(BACKTEST_RESULT_DIR+clModel.getIdentifyName()+"-monthlySummary.csv",  modelSummaries.getEvaluationSummary(), "GBK");

		saveBacktestResultFile(result,clModel.getIdentifyName());

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
	protected Instances prepareResultInstances(BaseClassifier clModel,
			Instances fullSetData) throws Exception {
		Instances result;
		Instances header = new Instances(fullSetData, 0);
		// 去除不必要的字段，保留ID（第1），均线策略（第3）、bias5（第4）、收益率（最后一列）、增加预测值、是否被选择。
		int removeFromIndex=InstanceUtility.findATTPosition(fullSetData, ArffFormat.BIAS5)+1;
		result = InstanceUtility.removeAttribs(header, ArffFormat.YEAR_MONTH_INDEX + ","+removeFromIndex+"-"
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
	 * 可以考虑子类中覆盖
	 * @param clModel
	 * @return
	 * @throws Exception
	 */
	protected  Instances getBacktestInstances(BaseClassifier clModel,String splitMark,String policy)
			throws Exception {
		Instances fullSetData;
		// 根据模型来决定是否要使用有计算字段的ARFF
		String arffFile=null;
		if (clModel.m_noCaculationAttrib==true){
			arffFile=ArffFormat.SHORT_ARFF_FILE;
		}else{
			arffFile=ArffFormat.LONG_ARFF_FILE;
		}

		System.out.println("start to load File for fullset from File: "+ arffFile  );
		fullSetData = FileUtility.loadDataFromFile( C_ROOT_DIRECTORY+arffFile);
		System.out.println("finish loading fullset Data. row : "+ fullSetData.numInstances() + " column:"+ fullSetData.numAttributes());
		return fullSetData;
	}

	/**
	 * 	从全量数据中获取分割training和eval以及test的clause， test数据比较简单，就是当月的。train和eval的逻辑见下
	 * 这里build model的数据已变为当前周期前推一年的数据 如果是2010XX.mdl 则取2009年XX月之前的数据build，
	 * 剩下的一年数据（2009XX到2010XX，后者不包含）做评估用
	 * 如果是2010.mdl，则取2009年01月之前的数据build，2009当年数据做评估用
	 * @param splitMark
	 * @return
	 */
	protected final String[] splitYearClause(String a_yearSplit) {
		String lastYearSplit=ClassifyUtility.getLastYearSplit(a_yearSplit);
		String[] splitYearClauses=new String[3];
		String attPos = "ATT" + ArffFormat.YEAR_MONTH_INDEX;
		if (lastYearSplit.length() == 6) { // 按月分割时
			splitYearClauses[0] = "(" + attPos + " < "
					+ lastYearSplit + ") ";
			splitYearClauses[1] = "(" + attPos + " >= "
					+ lastYearSplit + ") and (" + attPos + " < "	+ a_yearSplit + ") ";
			splitYearClauses[2] = "(" + attPos + " = "
					+ a_yearSplit + ") ";
		} else if (lastYearSplit.length() == 4) {// 按年分割
			splitYearClauses[0] = "(" + attPos + " < "
					+ lastYearSplit + "01) ";
			splitYearClauses[1] = "(" + attPos + " >= "
					+ lastYearSplit + "01) and (" + attPos + " <= "
					+ lastYearSplit + "12) ";
			splitYearClauses[2] = "(" + attPos + " >= "
					+ a_yearSplit + "01) and (" + attPos + " <= "
					+ a_yearSplit + "12) ";
		}
		return splitYearClauses;
	}

	
	
	/**
	 * 根据policy拼出相应的分割表达式，可以在子类中被覆盖
	 * @param splitTrainYearClause
	 * @param policy
	 * @return
	 */
	protected String getSplitClause(int policyIndex,String splitYearClause,	String policy) {
		String splitClause;
		splitClause = splitYearClause + " and (ATT"+policyIndex+" is '"	+ policy + "')";
		return splitClause;
	}



	//合并格式，子类中可覆盖
	protected  Instances mergeResultWithData(Instances resultData,Instances referenceData,String dataToAdd,int format) throws Exception{
		//读取磁盘上预先保存的左侧数据
		Instances left=null;
		
//		if (format==ArffFormat.LEGACY_FORMAT){ //LEGACY 有少量模型尚使用原有格式作为结果对比
//			left=FileUtility.loadDataFromFile(C_ROOT_DIRECTORY+"AllTransaction20052016-left.arff");
//		}else if (format==ArffFormat.EXT_FORMAT){  
//			left=FileUtility.loadDataFromFile(C_ROOT_DIRECTORY+ArffFormat.TRANSACTION_ARFF_PREFIX+"-left.arff");
//		}

		left=FileUtility.loadDataFromFile(C_ROOT_DIRECTORY+ArffFormat.TRANSACTION_ARFF_PREFIX+"-left.arff");
		MergeClassifyResults merge=new MergeClassifyResults(shouyilv_thresholds, winrate_thresholds);
		Instances mergedResult =merge.mergeResults(resultData, referenceData,dataToAdd, left);

		//返回结果之前需要按TradeDate重新排序
		int tradeDateIndex=InstanceUtility.findATTPosition(mergedResult, ArffFormat.TRADE_DATE);
		mergedResult.sort(tradeDateIndex-1);
		
		// 给mergedResult瘦身。 2=yearmonth, 6=datadate,7=positive,8=bias
		String[] attributeToRemove=new String[]{ArffFormat.YEAR_MONTH,ArffFormat.DATA_DATE,ArffFormat.IS_POSITIVE,ArffFormat.BIAS5};
		mergedResult=InstanceUtility.removeAttribs(mergedResult, attributeToRemove);

		return mergedResult;
	}



	private void saveBacktestResultFile(Instances result,String classiferName) throws IOException{
		FileUtility.SaveDataIntoFile(result, BACKTEST_RESULT_DIR+"回测结果-"+ classiferName+".arff" );
	}
	protected Instances loadBackTestResultFromFile(String classiferName) throws Exception{
		Instances result=FileUtility.loadDataFromFile(BACKTEST_RESULT_DIR+"回测结果-"+ classiferName+".arff" );
		return result;
	}

	protected void saveSelectedFileForMarkets(Instances fullOutput,String classiferName) throws Exception{
		//输出全市场概况
		Attribute shouyilvAttribute=fullOutput.attribute(ArffFormat.SHOUYILV);
		System.out.println("number of records for full market="+fullOutput.numInstances());
		System.out.println("shouyilv average for full market="+FormatUtility.formatPercent(fullOutput.meanOrMode(shouyilvAttribute),2,2));
		
		//输出全市场选股结果
		int pos = InstanceUtility.findATTPosition(fullOutput,ArffFormat.RESULT_SELECTED);
		Instances fullMarketSelected=InstanceUtility.getInstancesSubset(fullOutput, InstanceUtility.WEKA_ATT_PREFIX +pos+" = 1");
		shouyilvAttribute=fullMarketSelected.attribute(ArffFormat.SHOUYILV);
		System.out.println("selected shouyilv average for full market ="+FormatUtility.formatPercent(fullMarketSelected.meanOrMode(shouyilvAttribute),2,2)+" count="+fullMarketSelected.numInstances());
		FileUtility.saveCSVFile(fullMarketSelected, BACKTEST_RESULT_DIR+"选股-"+ classiferName+"-full" + RESULT_EXTENSION );

//		//输出沪深300
//		Instances subsetMarketSelected=InstanceUtility.filterDataForIndex(fullMarketSelected,ArffFormat.IS_HS300);
//		shouyilvAttribute=subsetMarketSelected.attribute(ArffFormat.SHOUYILV);
//		System.out.println("selected shouyilv average for hs300 ="+FormatUtility.formatPercent(subsetMarketSelected.meanOrMode(shouyilvAttribute),2,2)+" count="+subsetMarketSelected.numInstances());
//		FileUtility.saveCSVFile(subsetMarketSelected, BACKTEST_RESULT_DIR+"选股-"+ classiferName+"-hs300" + RESULT_EXTENSION );
//		//输出中证300
//		subsetMarketSelected=InstanceUtility.filterDataForIndex(fullMarketSelected,ArffFormat.IS_ZZ500);
//		shouyilvAttribute=subsetMarketSelected.attribute(ArffFormat.SHOUYILV);
//		System.out.println("selected shouyilv average for zz500 ="+FormatUtility.formatPercent(subsetMarketSelected.meanOrMode(shouyilvAttribute),2,2)+" count="+subsetMarketSelected.numInstances());
//		FileUtility.saveCSVFile(subsetMarketSelected, BACKTEST_RESULT_DIR+"选股-"+ classiferName+"-zz500" + RESULT_EXTENSION );
	}



}