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

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import yueyueGo.classifier.AdaboostClassifier;
import yueyueGo.classifier.BaggingM5P;
import yueyueGo.classifier.M5PABClassifier;
import yueyueGo.classifier.M5PClassifier;
import yueyueGo.classifier.MLPABClassifier;
import yueyueGo.classifier.MLPClassifier;

public class ProcessData {

	
	public static final String C_ROOT_DIRECTORY = "C:\\trend\\";//C:\\Users\\robert\\Desktop\\提升均线策略\\";
	public static final String NOMINAL_CLASSIFIER_DIR = C_ROOT_DIRECTORY+"models\\01-二分类器\\";
	public static final String CONTINOUS_CLASSIFIER_DIR = C_ROOT_DIRECTORY+"models\\02-连续分类器\\";
	public static final String BACKTEST_RESULT_DIR=C_ROOT_DIRECTORY+"testResult\\";
	public static final String PREDICT_WORK_DIR=C_ROOT_DIRECTORY+"03-预测模型\\";
	public static final String RESULT_EXTENSION = "-Test Result.csv";
	
	public static String M5P_PREDICT_MODEL="\\extData2005-2016-m5p-201607 MA ";//交易分析2005-2016 by month-new-m5p-201605 MA ";
	public static String M5P_EVAL_MODEL="\\extData2005-2016-m5p-201607 MA ";//交易分析2005-2016 by month-new-m5p-201605 MA ";

	public static final String MLP_PREDICT_MODEL= "\\extData2005-2016 month-new-mlp-2016 MA ";
	public static final String MLP_EVAL_MODEL= "\\extData2005-2016 month-new-mlp-201606 MA ";
	
	//经过主成分分析后的数据
	public static final String M5PAB_PREDICT_MODEL="\\extData2005-2016-m5pAB-201607 MA ";
	public static final String M5PAB_EVAL_MODEL="\\extData2005-2016-m5pAB-201607 MA "; 
	public static final String MLPAB_PREDICT_MODEL="\\extData2005-2016-mlpAB-2016 MA ";
	public static final String MLPAB_EVAL_MODEL="\\extData2005-2016-mlpAB-2016 MA "; 
	public static final String BAGGING_PREDICT_MODEL="\\extData2005-2016-baggingM5P-201606 MA ";
	public static final String BAGGING_EVAL_MODEL="\\extData2005-2016-baggingM5P-201607 MA ";
	
	public static final String[] splitYear ={
	  "2008","2009","2010","2011","2012","2013","2014","2015","2016"
//	"200801","200802","200803","200804","200805","200806","200807","200808","200809","200810","200811","200812","200901","200902","200903","200904","200905","200906","200907","200908","200909","200910","200911","200912","201001","201002","201003","201004","201005","201006","201007","201008","201009","201010","201011","201012","201101","201102","201103","201104","201105","201106","201107","201108","201109","201110","201111","201112","201201","201202","201203","201204","201205","201206","201207","201208","201209","201210","201211","201212","201301","201302","201303","201304","201305","201306","201307","201308","201309","201310","201311","201312","201401","201402","201403","201404","201405","201406","201407","201408","201409","201410","201411","201412","201501","201502","201503","201504","201505","201506","201507","201508","201509","201510","201511","201512","201601","201602","201603", "201604","201605","201606","201607"
		
	};

	public static void main(String[] args) {
		try {
			//用模型预测每日增量数据
//			callDailyPredict();

			//调用回测函数回测
			callTestBack();
			
			//用最新的单次交易数据，更新原始的交易数据文件
//			UpdateHistoryArffFile.callRefreshInstances();

			//为原始的历史文件Arff添加计算变量，并分拆，因为其数据量太大，所以提前处理，不必每次分割消耗内存
//			UpdateHistoryArffFile.processHistoryFile();
			
			//合并历史扩展数据
//			UpdateHistoryArffFile.mergeExtData();
			
//			UpdateHistoryArffFile.createTransInstances();
			
			
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}



	/**
	 * @throws Exception
	 */
	protected static void callDailyPredict() throws Exception {
		//用二分类模型预测每日增量数据
//		MLPClassifier nModel=new MLPClassifier();
		
		//用旧连续模型预测每日增量数据
		M5P_PREDICT_MODEL="\\交易分析2005-2016 by month-new-m5p-201605 MA ";
		M5P_EVAL_MODEL="\\交易分析2005-2016 by month-new-m5p-201605 MA ";
		M5PClassifier cModel=new M5PClassifier();
		cModel.arff_format=ArffFormat.LEGACY_FORMAT; 
		predictWithDB(cModel,PREDICT_WORK_DIR);
		
		//MLP主成分分析预测
		MLPABClassifier nABModel=new MLPABClassifier();
		predictWithDB(nABModel,PREDICT_WORK_DIR);
		
		//M5P主成分分析预测
		M5PABClassifier cABModel=new M5PABClassifier();
		predictWithDB(cABModel,PREDICT_WORK_DIR);		
		
		//BaggingM5P
		BaggingM5P cBagModel=new BaggingM5P();
		predictWithDB(cBagModel,PREDICT_WORK_DIR);		

//			//使用文件预测
//			String dataFileName=("t_stock_avgline_increment_zuixin_v"+FormatUtility.getDateStringFor(-1)).trim();
//			//用二分类模型预测每日增量数据
//			MLPClassifier nModel=new MLPClassifier();
//			predictWithFile(nModel,PREDICT_WORK_DIR,dataFileName);
//			//用连续模型预测每日增量数据
//			M5PClassifier cModel=new M5PClassifier();
//			predictWithFile(cModel,PREDICT_WORK_DIR,dataFileName);
	}



	/**
	 * @throws Exception
	 * @throws IOException
	 */
	protected static void callTestBack() throws Exception, IOException {
		//按二分类器回测历史数据
		//	投票感知器
//		VotedPerceptionClassifier nModel = new VotedPerceptionClassifier();
//		Instances nominalResult=testBackward(nModel);

		//REP树（C45树的变种，规则过于简单）
//		REPTreeClassifier nModel = new REPTreeClassifier();
//		Instances nominalResult=testBackward(nModel);

		//神经网络
//		MLPClassifier nModel = new MLPClassifier();
//		MLPABClassifier nModel = new MLPABClassifier();
//		RandomForestClassifier nModel=new RandomForestClassifier ();
		AdaboostClassifier nModel=new AdaboostClassifier();
		Instances nominalResult=testBackward(nModel);
		//不真正回测了，直接从以前的结果文件中加载
//		Instances nominalResult=loadBackTestResultFromFile(nModel.classifierName);

		//按连续分类器回测历史数据
//		M5PClassifier cModel=new M5PClassifier();
//		M5PABClassifier cModel=new M5PABClassifier();
		BaggingM5P cModel=new BaggingM5P();
//		Instances continuousResult=testBackward(cModel);
		//不真正回测了，直接从以前的结果文件中加载
		Instances continuousResult=loadBackTestResultFromFile(cModel.classifierName);
		
		//统一输出统计结果
		nModel.outputClassifySummary();
		cModel.outputClassifySummary();

		//输出用于计算收益率的CSV文件
		Instances m5pOutput=mergeResultWithData(continuousResult,nominalResult,ArffFormat.RESULT_PREDICTED_WIN_RATE,cModel.arff_format);
		saveSelectedFileForMarkets(m5pOutput,cModel.classifierName);
		Instances mlpOutput=mergeResultWithData(nominalResult,continuousResult,ArffFormat.RESULT_PREDICTED_PROFIT,nModel.arff_format);
		saveSelectedFileForMarkets(mlpOutput,nModel.classifierName);
	}



	//使用文件预测每天的增量数据
	protected static void predictWithFile(BaseClassifier clModel, String pathName,
			String dataFileName) throws Exception {
		System.out.println("-----------------------------");
		Instances fullData = FileUtility.loadDailyNewDataFromCSVFile(pathName + dataFileName
				+ ".txt");
	
		predict(clModel, pathName, fullData);

		System.out.println("file saved and mission completed.");
	}
	
	//直接访问数据库预测每天的增量数据
	protected static void predictWithDB(BaseClassifier clModel, String pathName) throws Exception {
		System.out.println("-----------------------------");
		Instances fullData = DBAccess.LoadDataFromDB(clModel.arff_format);

		
		predict(clModel, pathName, fullData);
		System.out.println("Database updated and mission completed.");
	}
	
	//用模型预测数据
	private static Instances predict(BaseClassifier clModel, String pathName, Instances inData) throws Exception {
		Instances newData = null;
		Instances result = null;

		Instances fullData=calibrateAttributesForDailyData(pathName, inData,clModel.arff_format);
	
		//如果模型需要计算字段，则把计算字段加上
		if (clModel.m_noCaculationAttrib==false){
			fullData=ArffFormat.addCalculateAttribute(fullData);		
		}
		
//		FileUtility.SaveDataIntoFile(fullData, pathName+FormatUtility.getDateStringFor(1)+"dailyInput-new116.arff");
		
//		//获得”均线策略"的位置属性
		int maIndex=InstanceUtility.findATTPosition(fullData,ArffFormat.SELECTED_AVG_LINE);

		if (clModel instanceof NominalClassifier ){
			fullData=((NominalClassifier)clModel).processDataForNominalClassifier(fullData,false);
		}

		
		for (int j = 0; j < clModel.m_policySubGroup.length; j++) {

			System.out.println("start to load data for " + ArffFormat.SELECTED_AVG_LINE+"  : "	+ clModel.m_policySubGroup[j]);
			String expression=null;
			expression=InstanceUtility.WEKA_ATT_PREFIX+ maIndex+" is '"+ clModel.m_policySubGroup[j] + "'";
			
			newData = InstanceUtility.getInstancesSubset(fullData, expression);

			
			String modelFileName;
			String evalFileName;
			if (clModel instanceof MLPClassifier ){

				modelFileName = pathName+"\\"+clModel.classifierName+ MLP_PREDICT_MODEL
						+ clModel.m_policySubGroup[j]	;				
				evalFileName = pathName+"\\"+clModel.classifierName+MLP_EVAL_MODEL
						 + clModel.m_policySubGroup[j]+BaseClassifier.THRESHOLD_EXTENSION	;				
			}else if (clModel instanceof M5PClassifier ){
				modelFileName = pathName+"\\"+clModel.classifierName+M5P_PREDICT_MODEL
						+  clModel.m_policySubGroup[j]	;
				evalFileName = pathName+"\\"+clModel.classifierName+M5P_EVAL_MODEL
						 + clModel.m_policySubGroup[j]+BaseClassifier.THRESHOLD_EXTENSION	;				
			}else if (clModel instanceof M5PABClassifier ){
				modelFileName = pathName+"\\"+clModel.classifierName+M5PAB_PREDICT_MODEL
						+  clModel.m_policySubGroup[j]	;
				evalFileName = pathName+"\\"+clModel.classifierName+M5PAB_EVAL_MODEL
						 + clModel.m_policySubGroup[j]+BaseClassifier.THRESHOLD_EXTENSION	;				
			}else if (clModel instanceof MLPABClassifier ){
				modelFileName = pathName+"\\"+clModel.classifierName+MLPAB_PREDICT_MODEL
						+  clModel.m_policySubGroup[j]	;
				evalFileName = pathName+"\\"+clModel.classifierName+MLPAB_EVAL_MODEL
						 + clModel.m_policySubGroup[j]+BaseClassifier.THRESHOLD_EXTENSION	;				
			}else if (clModel instanceof BaggingM5P ){
				modelFileName = pathName+"\\"+clModel.classifierName+BAGGING_PREDICT_MODEL
						+  clModel.m_policySubGroup[j]	;
				evalFileName = pathName+"\\"+clModel.classifierName+BAGGING_EVAL_MODEL
						 + clModel.m_policySubGroup[j]+BaseClassifier.THRESHOLD_EXTENSION	;				
			}else {
				throw new Exception("undefined predict model");
			}

			clModel.setModelFileName(modelFileName);
			clModel.setEvaluationFilename(evalFileName);
	
			System.out.println(" new data size , row : "+ newData.numInstances() + " column: "	+ newData.numAttributes());
			if (result == null) {// initialize result instances
				// remove unnecessary data,leave 均线策略 & shouyilv alone
				Instances header = new Instances(newData, 0);
				result = InstanceUtility.removeAttribs(header,
						"4-" + String.valueOf(header.numAttributes() - 1)); 
				if (clModel instanceof NominalClassifier ){
					result = InstanceUtility.AddAttribute(result, ArffFormat.RESULT_PREDICTED_WIN_RATE,
							result.numAttributes());
				}else{
					result = InstanceUtility.AddAttribute(result, ArffFormat.RESULT_PREDICTED_PROFIT,
						result.numAttributes());
				}
				result = InstanceUtility.AddAttribute(result, ArffFormat.RESULT_SELECTED,
						result.numAttributes());

			}
 
			clModel.predictData(newData, result);
			System.out.println("accumulated predicted rows: "+ result.numInstances());
			System.out.println("complete for 均线策略: " + clModel.m_policySubGroup[j]);
		}
		if (result.numInstances()!=inData.numInstances()) {
			throw new Exception("not all data have been processed!!!!! incoming Data number = " +inData.numInstances() + " while predicted number is "+result.numInstances());
		}
		
		result.renameAttribute(1, ArffFormat.SELECTED_AVG_LINE); //输出文件的“均线策略”名字不一样
		FileUtility.saveCSVFile(result, pathName + clModel.classifierName+"Selected Result"+FormatUtility.getDateStringFor(1)+".csv");		
		clModel.outputClassifySummary();
		return result;
	}





	//历史回测
	protected static Instances testBackward(BaseClassifier clModel) throws Exception,
			IOException {
		Instances fullSetData = null;
		Instances result = null;
		StringBuffer evalResultSummary=new StringBuffer();
		evalResultSummary.append("时间段,均线策略,整体正收益股数,整体股数,整体TPR,所选正收益股数,所选总股数,所选股TPR,提升率,所选股平均收益率,整体平均收益率,收益率差,是否改善,阀值下限,阀值上限\r\n");
		System.out.println("test backward using classifier : "+clModel.classifierName);
		// 别把数据文件里的ID变成Nominal的，否则读出来的ID就变成相对偏移量了
		for (int i = 0; i < splitYear.length; i++) { // if i starts from 1, the
														// first one is to use
														// as validateData
			
			String splitMark = splitYear[i];
			System.out.println("****************************start ****************************   "+splitMark);

			String splitTrainYearClause = "";
			String splitTestYearClause = "";

			String attribuateYear = "ATT" + ArffFormat.YEAR_MONTH_INDEX;
			if (splitMark.length() == 6) { // 按月分割时
				splitTrainYearClause = "(" + attribuateYear + " < "
						+ splitYear[i] + ") ";
				splitTestYearClause = "(" + attribuateYear + " = "
						+ splitYear[i] + ") ";
			} else if (splitMark.length() == 4) {// 按年分割
				splitTrainYearClause = "(" + attribuateYear + " < "
						+ splitYear[i] + "01) ";
				splitTestYearClause = "(" + attribuateYear + " >= "
						+ splitYear[i] + "01) and (" + attribuateYear + " <= "
						+ splitYear[i] + "12) ";
			}

			String policy = null;
			double lower_limit = 0;
			double upper_limit = 0;
			double tp_fp_ratio=0;
			String splitTrainClause = "";
			String splitTestClause = "";
			Instances trainingData = null;
			Instances testingRawData = null;

			for (int j = 0; j < clModel.m_policySubGroup.length; j++) {
				// 加载原始arff文件
				if (fullSetData == null) {

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
				}

				// 准备输出数据格式
				if (result == null) {// initialize result instances
					Instances header = new Instances(fullSetData, 0);
					// 去除不必要的字段，保留ID（第1），均线策略（第3）、bias5（第4）、收益率（最后一列）、增加预测值、是否被选择。
					result = InstanceUtility.removeAttribs(header, ArffFormat.YEAR_MONTH_INDEX + ",5-"
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

				}

				policy = clModel.m_policySubGroup[j];
				lower_limit = clModel.SAMPLE_LOWER_LIMIT[j];
				upper_limit = clModel.SAMPLE_UPPER_LIMIT[j];
				tp_fp_ratio= clModel.TP_FP_RATIO_LIMIT[j];
				splitTrainClause = splitTrainYearClause + " and (ATT3 is '"	+ policy + "')";
				splitTestClause = splitTestYearClause + " and (ATT3 is '"+ policy + "')";
				if (clModel.m_skipTrainInBacktest == false || clModel.m_skipEvalInBacktest==false ) { //如果不需要培训和评估，则无需训练样本
					System.out.println("start to split training set");
					trainingData = InstanceUtility.getInstancesSubset(fullSetData,
							splitTrainClause);
					trainingData = InstanceUtility.removeAttribs(trainingData,  Integer.toString(ArffFormat.ID_POSITION)+","+ArffFormat.YEAR_MONTH_INDEX);

					//对于二分类器，这里要把输入的收益率转换为分类变量
					if (clModel instanceof NominalClassifier ){
						trainingData=((NominalClassifier)clModel).processDataForNominalClassifier(trainingData,false);
					}
					System.out.println(" training data size , row : "
							+ trainingData.numInstances() + " column: "
							+ trainingData.numAttributes());
					if (clModel.m_saveArffInBacktest) {
						clModel.saveArffFile(trainingData,"train", splitMark, policy);
					}
				}
				// prepare testing data
				System.out.println("start to split testing set");
				testingRawData = InstanceUtility
						.getInstancesSubset(fullSetData, splitTestClause);
				
				
				//在做模型训练时释放内存，改为每次从硬盘加载的方式
				if (clModel.m_skipTrainInBacktest == false){
					fullSetData=null; //释放内存
					System.gc();
				}				
				

				
				
				String resultSummary = doOneModel(clModel, result,
						splitMark, policy, lower_limit, upper_limit,tp_fp_ratio,trainingData,testingRawData);
				evalResultSummary.append(resultSummary);
			}


			System.out.println("********************complete **************************** " + splitMark);
			System.out.println(" ");
		}


		FileUtility.write(BACKTEST_RESULT_DIR+clModel.classifierName+"-monthlySummary.csv", evalResultSummary.toString(), "GBK");

		saveBacktestResultFile(result,clModel.classifierName);

		System.out.println(clModel.classifierName+" test result file saved.");
		return result;
	}

	// paremeter result will be changed in the method! 
	protected static String doOneModel(BaseClassifier clModel,
			 Instances result, String yearSplit,
			String policySplit, double lower_limit, double upper_limit, double tp_fp_ratio,
			Instances trainingData, Instances testingData) throws Exception,
			IOException {

		
		System.out.println("-----------------start for " + yearSplit + "-----------均线策略: ------" + policySplit);
		clModel.generateModelAndEvalFileName(yearSplit,policySplit);

		Classifier model = null;
		
		//是否需要重做训练阶段
		if (clModel.m_skipTrainInBacktest == false) { 
			System.out.println("start to build model");
			model = clModel.trainData(trainingData);
		} else {
			model = clModel.loadModel(yearSplit,policySplit);
		}
		//是否需要重做评估阶段
		if (clModel.m_skipEvalInBacktest == false) {
			clModel.evaluateModel(trainingData, model, lower_limit,
					upper_limit,tp_fp_ratio);
		}
		trainingData=null;

		//处理testingData
		//对于二分类器，这里要把输入的收益率转换为分类变量
		if (clModel instanceof NominalClassifier ){
			testingData=((NominalClassifier)clModel).processDataForNominalClassifier(testingData,true);
		}
		testingData = InstanceUtility.removeAttribs(testingData, ArffFormat.YEAR_MONTH_INDEX);
		System.out.println("testing data size, row: "
				+ testingData.numInstances() + " column: "
				+ testingData.numAttributes());
		if (clModel.m_saveArffInBacktest) {
			clModel.saveArffFile(testingData,"test", yearSplit,policySplit);
		}
		
		String evalSummary=yearSplit+","+policySplit+",";
		evalSummary+=clModel.predictData(testingData, result);
		testingData=null;
		System.out.println("accumulated predicted rows: "
				+ result.numInstances());
		System.out.println("complete for " + yearSplit + "均线策略: " + policySplit);
		return evalSummary;
	}

	//这是对增量数据nominal label的处理 （因为增量数据中的nominal数据，label会可能不全）
	private static Instances calibrateAttributesForDailyData(String pathName,Instances incomingData,int formatType) throws Exception {
		
		//与本地格式数据比较，这地方基本上会有nominal数据的label不一致，临时处理办法就是先替换掉
		String formatFile=null;
		switch (formatType) {
		case ArffFormat.LEGACY_FORMAT:
			formatFile="trans20052016-legacy-format.arff";
			break;
		case ArffFormat.EXT_FORMAT:
			formatFile=ArffFormat.TRANSACTION_ARFF_PREFIX+"-format.arff";
			break;
		default:
			break;
		}

		Instances outputData=FileUtility.loadDataFromFile(C_ROOT_DIRECTORY+formatFile);
		outputData=InstanceUtility.removeAttribs(outputData, "2");
		System.out.println("!!!!!verifying input data format , you should read this .... "+ outputData.equalHeadersMsg(incomingData));
		InstanceUtility.calibrateAttributes(incomingData, outputData);
		return outputData;
	}

	protected static Instances mergeResultWithData(Instances resultData,Instances referenceData,String dataToAdd,int format) throws Exception{
		//读取磁盘上预先保存的左侧数据
		Instances left=null;
		
		//TODO 过渡期 有少量模型尚使用原有格式
		if (format==ArffFormat.EXT_FORMAT){
			left=FileUtility.loadDataFromFile(C_ROOT_DIRECTORY+ArffFormat.TRANSACTION_ARFF_PREFIX+"-left.arff");
		}else if (format==ArffFormat.LEGACY_FORMAT){ //LEGACY 有少量模型尚使用原有格式
			left=FileUtility.loadDataFromFile(C_ROOT_DIRECTORY+"AllTransaction20052016-left.arff");
		}
		System.out.println("incoming resultData size, row="+resultData.numInstances()+" column="+resultData.numAttributes());
		System.out.println("incoming referenceData size, row="+referenceData.numInstances()+" column="+referenceData.numAttributes());
		System.out.println("Left data loaded, row="+left.numInstances()+" column="+left.numAttributes());


	    // 创建输出结果
	    Instances mergedResult = new Instances(left, 0);
	    mergedResult=InstanceUtility.AddAttribute(mergedResult,ArffFormat.RESULT_PREDICTED_PROFIT, mergedResult.numAttributes());
	    mergedResult=InstanceUtility.AddAttribute(mergedResult,ArffFormat.RESULT_PREDICTED_WIN_RATE, mergedResult.numAttributes());
	    mergedResult=InstanceUtility.AddAttribute(mergedResult,ArffFormat.RESULT_SELECTED, mergedResult.numAttributes());

		
		Instance leftCurr;
		Instance resultCurr;
		Instance referenceCurr;
		Instance newData;
		Attribute leftMA=left.attribute(ArffFormat.SELECTED_AVG_LINE);
		Attribute resultMA=resultData.attribute(ArffFormat.SELECTED_AVG_LINE);
		Attribute leftBias5=left.attribute("bias5");
		Attribute resultBias5=resultData.attribute("bias5");
		Attribute resultSelectedAtt=resultData.attribute(ArffFormat.RESULT_SELECTED);
		Attribute outputSelectedAtt=mergedResult.attribute(ArffFormat.RESULT_SELECTED);
		Attribute outputPredictAtt=mergedResult.attribute(ArffFormat.RESULT_PREDICTED_PROFIT);
		Attribute outputWinrateAtt=mergedResult.attribute(ArffFormat.RESULT_PREDICTED_WIN_RATE);
				
		
		
		//传入的结果集result不是排序的,而left的数据是按tradeDate日期排序的， 所以都先按ID排序。
		left.sort(ArffFormat.ID_POSITION-1);
		resultData.sort(ArffFormat.ID_POSITION-1);
		referenceData.sort(ArffFormat.ID_POSITION-1);
		
		
		double idInResults=0;
		double idInLeft=0;
		double idInReference=0;
		int resultIndex=0;
		int leftIndex=0;
		int referenceIndex=0;
		int referenceDataNum=referenceData.numInstances();


		while (leftIndex<left.numInstances() && resultIndex<resultData.numInstances()){				
			resultCurr=resultData.instance(resultIndex);
			leftCurr=left.instance(leftIndex);
			idInResults=resultCurr.value(0);
			idInLeft=leftCurr.value(0);
			if (idInLeft<idInResults){ // 如果左边有未匹配的数据，这是正常的，因为left数据是从2005年开始的全量
				leftIndex++;
				continue;
			}else if (idInLeft>idInResults){ // 如果右边result有未匹配的数据，这个不大正常，需要输出
				System.out.println("!!!unmatched result===="+ resultCurr.toString());	
				System.out.println("!!!current left   ====="+ leftCurr.toString());
				resultIndex++;
				continue;
			}else if (idInLeft==idInResults ){//找到相同ID的记录了
				//去reference数据里查找相应的ID记录
				referenceCurr=referenceData.instance(referenceIndex);
				idInReference=referenceCurr.value(0);

				//这段代码是用于应对reference的数据与result的数据不一致情形的。
				//reference数据也是按ID排序的，所以可以按序查找
				int oldIndex=referenceIndex;//暂存一下
				while (idInReference<idInResults ){ 
					if (referenceIndex<referenceDataNum-1){
						referenceIndex++;
						referenceCurr=referenceData.instance(referenceIndex);
						idInReference=referenceCurr.value(0);
					}else { //当前ID比result的ID小，需要向后找，但向后找到最后一条也没找到
						referenceCurr=new DenseInstance(referenceData.numAttributes());
						referenceIndex=oldIndex; //这一条设为空，index恢复原状
						break;
					}
				}
				while (idInReference>idInResults ){
					if (referenceIndex>0){
						referenceIndex--;
						referenceCurr=referenceData.instance(referenceIndex);
						idInReference=referenceCurr.value(0);
					}else {  //当前ID比result的ID大，需要向前找，但向前找到第一条也没找到
						referenceCurr=new DenseInstance(referenceData.numAttributes());
						referenceIndex=oldIndex; //这一条设为空，index恢复原状
						break;
					}
				}
				
				
				//接下来做冗余字段的数据校验
				if ( ArffFormat.checkSumBeforeMerge(leftCurr, resultCurr, leftMA, resultMA,leftBias5, resultBias5)) {
					newData=new DenseInstance(mergedResult.numAttributes());
					newData.setDataset(mergedResult);
					int srcStartIndex=0;
					int srcEndIndex=leftCurr.numAttributes()-1;
					int targetStartIndex=0;
					InstanceUtility.copyToNewInstance(leftCurr, newData, srcStartIndex, srcEndIndex,targetStartIndex);

					//根据传入的参数判断需要当前有什么，需要补充的数据是什么
					double profit;
					double winrate;
					if (dataToAdd.equals(ArffFormat.RESULT_PREDICTED_WIN_RATE)){
						//当前结果集里有什么数据
						profit=resultCurr.value(resultData.attribute(ArffFormat.RESULT_PREDICTED_PROFIT));
						//需要添加参考集里的什么数据
						winrate=referenceCurr.value(referenceData.attribute(ArffFormat.RESULT_PREDICTED_WIN_RATE));
					}else{
						//当前结果集里有什么数据
						winrate=resultCurr.value(resultData.attribute(ArffFormat.RESULT_PREDICTED_WIN_RATE));
						//需要添加参考集里的什么数据
						profit=referenceCurr.value(referenceData.attribute(ArffFormat.RESULT_PREDICTED_PROFIT));
					}

					newData.setValue(outputPredictAtt, profit);
					newData.setValue(outputWinrateAtt, winrate);
					newData.setValue(outputSelectedAtt, resultCurr.value(resultSelectedAtt));
					mergedResult.add(newData);
					resultIndex++;
					leftIndex++;
					if (mergedResult.numInstances() % 100000 ==0){
						System.out.println("number of results processed:"+ mergedResult.numInstances());
					}
				}else {
					throw new Exception("data value in header data and result data does not equal left="+leftCurr.toString()+" /while result= "+resultCurr.toString());
				}
			}
		}// end left processed
		if (mergedResult.numInstances()!=resultData.numInstances()){
//			throw new Exception
			System.out.println("------Attention!!! not all data in result have been processed , processed= "+mergedResult.numInstances()+" ,while total result="+resultData.numInstances());
		}else {
			System.out.println("number of results merged and processed: "+ mergedResult.numInstances());
		}
		
		//返回结果之前需要按TradeDate重新排序
		int tradeDateIndex=InstanceUtility.findATTPosition(mergedResult, ArffFormat.TRADE_DATE);
		mergedResult.sort(tradeDateIndex-1);
		
		//TODO 给mergedResult瘦身。
		mergedResult=InstanceUtility.removeAttribs(mergedResult, "2,6,7,9");


		return mergedResult;
	}


	private static void saveBacktestResultFile(Instances result,String classiferName) throws IOException{
	FileUtility.SaveDataIntoFile(result, BACKTEST_RESULT_DIR+"回测结果-"+ classiferName+".arff" );
}
protected static Instances loadBackTestResultFromFile(String classiferName) throws Exception{
	Instances result=FileUtility.loadDataFromFile(BACKTEST_RESULT_DIR+"回测结果-"+ classiferName+".arff" );
	return result;
}

protected static void saveSelectedFileForMarkets(Instances fullOutput,String classiferName) throws Exception{
	//输出全市场结果
	Instances fullMarketSelected=InstanceUtility.getInstancesSubset(fullOutput, InstanceUtility.WEKA_ATT_PREFIX +fullOutput.numAttributes()+" = 1");
	FileUtility.saveCSVFile(fullMarketSelected, BACKTEST_RESULT_DIR+"选股-"+ classiferName+"-full" + RESULT_EXTENSION );
	//输出沪深300
	Instances subsetMarketSelected=InstanceUtility.filterDataForIndex(fullMarketSelected,ArffFormat.IS_HS300);
	FileUtility.saveCSVFile(subsetMarketSelected, BACKTEST_RESULT_DIR+"选股-"+ classiferName+"-hs300" + RESULT_EXTENSION );
	//输出中证300
	subsetMarketSelected=InstanceUtility.filterDataForIndex(fullMarketSelected,ArffFormat.IS_ZZ500);
	FileUtility.saveCSVFile(subsetMarketSelected, BACKTEST_RESULT_DIR+"选股-"+ classiferName+"-zz500" + RESULT_EXTENSION );
}



}