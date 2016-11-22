package yueyueGo;

import java.util.HashMap;

import yueyueGo.classifier.AdaboostClassifier;
import yueyueGo.classifier.BaggingLinearRegression;
import yueyueGo.classifier.BaggingM5P;
import yueyueGo.classifier.MyNNClassifier;
import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.dataProcessor.InstanceHandler;
import yueyueGo.dataProcessor.WekaInstanceProcessor;
import yueyueGo.databeans.DataInstances;
import yueyueGo.databeans.GeneralInstance;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.datasource.DataIOHandler;
import yueyueGo.fullModel.ArffFormatFullModel;
import yueyueGo.fullModel.classifier.BaggingM5PFullModel;
import yueyueGo.fullModel.classifier.MyNNFullModel;
import yueyueGo.utility.AppContext;
import yueyueGo.utility.ClassifySummaries;
import yueyueGo.utility.ClassifyUtility;
import yueyueGo.utility.FormatUtility;
import yueyueGo.utility.MergeClassifyResults;
import yueyueGo.utility.PredictModelData;

public class DailyPredict {

	private static String PREDICT_WORK_DIR=EnvConstants.PREDICT_WORK_DIR;
	private static String PREDICT_RESULT_DIR=PREDICT_WORK_DIR+"\\88-预测结果\\"; 
	private HashMap<String, PredictModelData> PREDICT_MODELS;
	private double[] shouyilv_thresholds; //对于胜率优先算法的收益率筛选阀值
	private double[] winrate_thresholds; //对于收益率优先算法的胜率筛选阀值
	private HashMap<String, GeneralInstances> cached_daily_data=new HashMap<String, GeneralInstances>(); //从数据库里加载的每日预测数据

	private void definePredictModels(String type){
		PREDICT_MODELS=new HashMap<String, PredictModelData>();


		int format;
		String classifierName;
		if(EnvConstants.AVG_LINE_ROOT_DIR.equalsIgnoreCase(type)){

			//=========================LEGACY FORMAT 不常用========================
			format=ArffFormat.LEGACY_FORMAT;
			//MLPAB旧格式预测模型
			classifierName=ClassifyUtility.MLPAB;
			addModelData(classifierName,format,"\\extData2005-2016-mlpAB-2016 MA ","\\extData2005-2016-mlpAB-2016 MA ");
			
			//=========================EXT FORMAT 部分========================
			format=ArffFormat.EXT_FORMAT;

			//BaggingLinearRegression当前使用的预测模型
			classifierName=ClassifyUtility.BAGGING_LINEAR_REGRESSION;
			addModelData(classifierName,format,"\\extData2005-2016-BaggingLinearRegression-201507 MA ","\\extData2005-2016-BaggingLinearRegression-201509 MA ");

			//MyNN当前使用模型
			classifierName=ClassifyUtility.MYNN_MLP;
			addModelData(classifierName,format,"\\extData2005-2016-myNNAB-201507 MA ","\\extData2005-2016-myNNAB-201509 MA ");
			
			//BaggingM5P当前使用的预测模型
			classifierName=ClassifyUtility.BAGGING_M5P;
			addModelData(classifierName,format,"\\extData2005-2016-baggingM5P-201507 MA ","\\extData2005-2016-baggingM5P-201509 MA ");

			//adaboost当前使用的预测模型
			classifierName=ClassifyUtility.ADABOOST;
			addModelData(classifierName,format,"\\extData2005-2016-adaboost-201507 MA ","\\extData2005-2016-adaboost-201509 MA ");
			
		}else if(EnvConstants.FULL_MODEL_ROOT_DIR.equals(type)){
			// fullmodel不保留legacy
			format=ArffFormatFullModel.FULLMODEL_FORMAT;
			//BaggingM5PFullModel当前使用的预测模型---------FullMODEL
			classifierName=ClassifyUtility.BAGGING_M5P_FULLMODEL;
			addModelData(classifierName,format,"\\extData2005-2016-BaggingM5PABFullModel-201507 MA ", "\\extData2005-2016-BaggingM5PABFullModel-201509 MA ");

			//BaggingJ48FullModel当前使用的预测模型---------FullMODEL
			classifierName=ClassifyUtility.MYNN_MLP_FULLMODEL;
			addModelData(classifierName,format,"\\extData2005-2016-myNNFullModel-201507 MA ", "\\extData2005-2016-myNNFullModel-201509 MA ");
		}
	}


	public static void main(String[] args) {
		try {
			callFullModelPredict();
			callDailyPredict();

		} catch (Exception e) {

			e.printStackTrace();
		}
	}


	/**
	 * @throws Exception
	 */
	public static String callDailyPredict() throws Exception {
		DailyPredict worker = new DailyPredict();
		//用均线模型预测每日增量数据
		System.out.println("==================================================");
		System.out.println("===============starting 均线模型预测===============");
		System.out.println("==================================================");
		AppContext.clearContext();
		AppContext.createContext(EnvConstants.AVG_LINE_ROOT_DIR);
		//预先初始化各种模型文件的位置
		worker.definePredictModels(EnvConstants.AVG_LINE_ROOT_DIR);
		worker.shouyilv_thresholds=new double[] {0.005,0.005,0.01,0.03,0.03}; // {0.01,0.02,0.03,0.03,0.04};
		worker.winrate_thresholds=new double[]  {0.45,0.45,0.45,0.35,0.25};  //{0.3,0.3,0.3,0.25,0.25};
		return worker.dailyPredict();
	}


	/**
	 * @return
	 * @throws Exception
	 */
	public static String callFullModelPredict() throws Exception {
		DailyPredict worker =new DailyPredict();

		//短线模型的每日预测
		System.out.println("==================================================");
		System.out.println("===============starting 短线模型预测===============");
		System.out.println("==================================================");
		AppContext.createContext(EnvConstants.FULL_MODEL_ROOT_DIR);
		//预先初始化各种模型文件的位置
		worker.definePredictModels(EnvConstants.FULL_MODEL_ROOT_DIR);
		worker.shouyilv_thresholds=new double[] {0.03};
		worker.winrate_thresholds=new double[] {0.5};
		
		return worker.fullModelPredict();
	}


	/**
	 * @throws Exception
	 */
	private String dailyPredict() throws Exception {


		//新格式的bagging线性回归预测
		BaggingLinearRegression lBagModel=new BaggingLinearRegression();
		GeneralInstances linearInstances=predictWithDB(lBagModel);
		
		//新格式的神经网络预测
		MyNNClassifier nnModel=new MyNNClassifier();
		GeneralInstances nnInstances=predictWithDB(nnModel);
		
		//新格式的bagging m5p预测
		BaggingM5P cBagModel=new BaggingM5P();
		GeneralInstances baggingInstances=predictWithDB(cBagModel);

		//Adaboost
		AdaboostClassifier adaModel=new AdaboostClassifier();
		GeneralInstances adaboostInstances=predictWithDB(adaModel);		

		System.out.println("***************** now output prediction results************************");
		lBagModel.outputClassifySummary();
		nnModel.outputClassifySummary();
		cBagModel.outputClassifySummary();
		adaModel.outputClassifySummary();
		System.out.println("***************** end of output prediction results************************");
		
		
		combinePreditions(lBagModel, linearInstances, nnModel, nnInstances);
		
		String savedResultFile=combinePreditions(cBagModel, baggingInstances, adaModel, adaboostInstances);
		return savedResultFile;
	}


	/**
	 * @param cModel 连续分类器（预测收益率）
	 * @param cInstances 连续分类器结果
	 * @param nModel 二分类器（预测胜率）
	 * @param nInstances 二分类器结果
	 * @throws Exception
	 * return 合并的文件名称（暂时返回连续分类器的值）
	 */
	public String combinePreditions(BaseClassifier cModel,
			GeneralInstances cInstances, BaseClassifier nModel,
			GeneralInstances nInstances) throws Exception {
		
		
		//以二分类器为主，合并连续分类器
		System.out.println("");
		System.out.println("-----now output combined predictions----------"+nModel.getIdentifyName()+" combined with："+cModel.getIdentifyName());
		GeneralInstances left=DataIOHandler.getSuppier().loadDataFromFile(getLeftArffFileName(nModel)); //获取刚生成的左侧文件（主要存了CODE）
		MergeClassifyResults merge=new MergeClassifyResults(this.shouyilv_thresholds, this.winrate_thresholds);
		GeneralInstances nMergedOutput=merge.mergeResults(nInstances,cInstances,ArffFormat.RESULT_PREDICTED_PROFIT,left);
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(nMergedOutput);
		nMergedOutput=instanceProcessor.removeAttribs(nMergedOutput, new String[]{ArffFormat.IS_POSITIVE,ArffFormat.SHOUYILV}); // 去掉空的收益率或positive字段
		String savedNFileName=PREDICT_RESULT_DIR+ "Merged Selected Result-"+nModel.getIdentifyName()+"-"+FormatUtility.getDateStringFor(1)+".csv";
		DataIOHandler.getSaver().saveCSVFile(nMergedOutput, savedNFileName);
		nMergedOutput=null;
		System.out.println(nModel.getIdentifyName()+"----------prediction ends---------");
		//以连续分类器为主，合并二分类器
		System.out.println("-----now output combined predictions----------"+cModel.getIdentifyName()+" combined with："+nModel.getIdentifyName());
		GeneralInstances cMergedOutput=merge.mergeResults(cInstances,nInstances,ArffFormat.RESULT_PREDICTED_WIN_RATE,left);
		cMergedOutput=instanceProcessor.removeAttribs(cMergedOutput, new String[]{ArffFormat.IS_POSITIVE,ArffFormat.SHOUYILV}); // 去掉空的收益率或positive字段
		String savedCFileName=PREDICT_RESULT_DIR+ "Merged Selected Result-"+cModel.getIdentifyName()+"-"+FormatUtility.getDateStringFor(1)+".csv";
		DataIOHandler.getSaver().saveCSVFile(cMergedOutput, savedCFileName);
		cMergedOutput=null;
		System.out.println(cModel.getIdentifyName()+"----------prediction ends--------");
		return savedCFileName;
	}


	/**
	 * @throws Exception
	 */
	private String fullModelPredict() throws Exception {

		//BaggingM5P
		BaggingM5PFullModel cFullModel=new BaggingM5PFullModel();
		GeneralInstances cInstances=predictWithDB(cFullModel);		

		//BaggingJ48
//		BaggingJ48FullModel nFullModel=new BaggingJ48FullModel();
		MyNNFullModel nFullModel= new MyNNFullModel(); 
		GeneralInstances nInstances=predictWithDB(nFullModel);		

		cFullModel.outputClassifySummary();
		nFullModel.outputClassifySummary();

		//合并baggingJ48和baggingM5P
		System.out.println("-----now output combined predictions----------"+cFullModel.getIdentifyName());
		GeneralInstances left=DataIOHandler.getSuppier().loadDataFromFile(getLeftArffFileName(cFullModel)); //获取刚生成的左侧文件（主要存了CODE）

		MergeClassifyResults merge=new MergeClassifyResults(shouyilv_thresholds, winrate_thresholds);
		GeneralInstances mergedOutput=merge.mergeResults(cInstances,nInstances,ArffFormat.RESULT_PREDICTED_WIN_RATE,left);
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(mergedOutput);
		mergedOutput=instanceProcessor.removeAttribs(mergedOutput, new String[]{ArffFormat.IS_POSITIVE,ArffFormat.SHOUYILV}); // 去掉空的收益率或positive字段
		String savedCFileName=PREDICT_RESULT_DIR+ "FullModel Selected Result-"+cFullModel.getIdentifyName()+"-"+FormatUtility.getDateStringFor(1)+".csv";
		DataIOHandler.getSaver().saveCSVFile(mergedOutput, savedCFileName);
		return savedCFileName;
	}




	private static String getLeftArffFileName(BaseClassifier clModel){
		return PREDICT_RESULT_DIR+"LEFT ("+clModel.getModelArffFormat()+") "+FormatUtility.getDateStringFor(1)+".arff";
	}

	//	//使用文件预测每天的增量数据
	//	private void predictWithFile(BaseClassifier clModel, String pathName,
	//			String dataFileName) throws Exception {
	//		System.out.println("-----------------------------");
	//		Instances fullData = FileUtility.loadDailyNewDataFromCSVFile(pathName + dataFileName
	//				+ ".txt");
	//
	//		predict(clModel, pathName, fullData);
	//
	//		System.out.println("file saved and mission completed.");
	//	}


	//直接访问数据库预测每天的自选股数据，不单独保存每个模型的选股
	private GeneralInstances predictWithDB(BaseClassifier clModel) throws Exception {
		int dataFormat=clModel.getModelArffFormat();
	
		GeneralInstances dailyData = null;
		//试着从缓存里加载
		String cacheKey=String.valueOf(dataFormat);
		dailyData=this.cached_daily_data.get(cacheKey);
		
		if(dailyData==null){ //缓存中没有，需要从数据库里加载
			switch (dataFormat){
			case ArffFormat.LEGACY_FORMAT:
				dailyData=DataIOHandler.getSuppier().LoadDataFromDB(dataFormat);
				break;
			case ArffFormat.EXT_FORMAT:
				dailyData=DataIOHandler.getSuppier().LoadDataFromDB(dataFormat);
				break;
			case ArffFormatFullModel.FULLMODEL_FORMAT:
				dailyData = DataIOHandler.getSuppier().LoadFullModelDataFromDB();
				break;			
			default:
				throw new Exception("invalid arffFormat type");
			}
			//保留DAILY RESULT的LEFT部分在磁盘上，主要为了保存股票代码
			GeneralInstances left = new DataInstances(dailyData);
			BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(left);
			left=instanceProcessor.filterAttribs(dailyData, ArffFormat.DAILY_PREDICT_RESULT_LEFT);
			//将LEFT中的CODE加上=""，避免输出格式中前导零消失。
			int codeIndex=BaseInstanceProcessor.findATTPosition(left,ArffFormat.CODE);
			left=instanceProcessor.nominalToString(left, String.valueOf(codeIndex));
			codeIndex-=1;  //以下的index是从0开始
			for (int i=0;i<left.size();i++){
				GeneralInstance originInstance=left.instance(i);
				String originValue=originInstance.stringValue(codeIndex);
				originInstance.setValue(codeIndex, " "+originValue);
			}
			
			DataIOHandler.getSaver().SaveDataIntoFile(left,  getLeftArffFileName(clModel));

			//去掉多读入的CODE部分
			instanceProcessor=InstanceHandler.getHandler(dailyData);
			dailyData=instanceProcessor.removeAttribs(dailyData, new String[]{ArffFormat.CODE});
			//将结果放入缓存
			this.cached_daily_data.put(cacheKey, dailyData);
		}
		
		GeneralInstances result=predict(clModel,  dailyData);

		return result;
	}


	//用模型预测数据

	private  GeneralInstances predict(BaseClassifier clModel, GeneralInstances inData) throws Exception {
		System.out.println("predict using classifier : "+clModel.getIdentifyName()+" @ prediction work path :"+PREDICT_WORK_DIR);
		System.out.println("-----------------------------");
		
		GeneralInstances newData = null;
		GeneralInstances result = null;

		//创建存储评估结果的数据容器
		ClassifySummaries modelSummaries=new ClassifySummaries(clModel.getIdentifyName(),true);
		clModel.setClassifySummaries(modelSummaries);

		GeneralInstances fullData=calibrateAttributesForDailyData(inData,clModel.getModelArffFormat());

		//如果模型需要计算字段，则把计算字段加上
		if (clModel.m_noCaculationAttrib==false){
			fullData=ArffFormat.addCalculateAttribute(fullData);		
		}


		//获得”均线策略"的位置属性, 如果数据集内没有“均线策略”（短线策略的fullmodel），MaIndex为-1
		int maIndex=BaseInstanceProcessor.findATTPosition(fullData,ArffFormat.SELECTED_AVG_LINE);

		if (clModel instanceof NominalClassifier ){
			fullData=((NominalClassifier)clModel).processDataForNominalClassifier(fullData,false);
		}


		for (int j = 0; j < clModel.m_policySubGroup.length; j++) {

			System.out.println("start to load data for policy : "	+ clModel.m_policySubGroup[j]);
			String expression=null;
			if (maIndex>0){// 均线策略
				expression=WekaInstanceProcessor.WEKA_ATT_PREFIX+ maIndex+" is '"+ clModel.m_policySubGroup[j] + "'";
				BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(fullData);
				newData = instanceProcessor.getInstancesSubset(fullData, expression);
			}else{ //短线策略（fullmodel)				
				newData=fullData;
			}


			String modelFileName;
			String evalFileName;
			String id=clModel.classifierName+clModel.modelArffFormat;
			PredictModelData modelData=this.PREDICT_MODELS.get(id);
			modelFileName=modelData.getModelFileName();
			evalFileName=modelData.getEvalFileName();
			int formatType=modelData.getModelFormatType();
			modelFileName = PREDICT_WORK_DIR+getDirPrefixByType(formatType)+"\\"+clModel.getIdentifyName()+ modelFileName
					+ clModel.m_policySubGroup[j]	;				
			evalFileName = PREDICT_WORK_DIR+getDirPrefixByType(formatType)+"\\"+clModel.getIdentifyName()+evalFileName
					+ clModel.m_policySubGroup[j]+ModelStore.THRESHOLD_EXTENSION	;				
			ModelStore modelStore=new ModelStore(modelFileName,evalFileName);
			clModel.setModelStore(modelStore);



			System.out.println(" new data size , row : "+ newData.numInstances() + " column: "	+ newData.numAttributes());
			if (result == null) {// initialize result instances
				// remove unnecessary data,leave 均线策略 & code alone
				GeneralInstances header = new DataInstances(newData, 0);
				BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(header);
				result=instanceProcessor.filterAttribs(header, ArffFormat.DAILY_PREDICT_RESULT_LEFT);

				if (clModel instanceof NominalClassifier ){
					result = instanceProcessor.AddAttribute(result, ArffFormat.RESULT_PREDICTED_WIN_RATE,
							result.numAttributes());
				}else{
					result = instanceProcessor.AddAttribute(result, ArffFormat.RESULT_PREDICTED_PROFIT,
							result.numAttributes());
				}
				result = instanceProcessor.AddAttribute(result, ArffFormat.RESULT_SELECTED,
						result.numAttributes());

			}

			clModel.predictData(newData, result,clModel.m_policySubGroup[j]);
			System.out.println("accumulated predicted rows: "+ result.numInstances());
			System.out.println("complete for : "+ clModel.m_policySubGroup[j]);
		}
		if (result.numInstances()!=inData.numInstances()) {
			throw new Exception("not all data have been processed!!!!! incoming Data number = " +inData.numInstances() + " while predicted number is "+result.numInstances());
		}

		return result;
	}

	private static String getDirPrefixByType(int modelFormatType){
		switch (modelFormatType) {
		case ArffFormat.LEGACY_FORMAT:
			return "\\00-legacy";
		case ArffFormat.EXT_FORMAT:
			return "";
		case ArffFormatFullModel.FULLMODEL_FORMAT:
			return "";
		default:
			return "";
		}
	}
	
	//这是对增量数据nominal label的处理 （因为增量数据中的nominal数据，label会可能不全）
	private static GeneralInstances calibrateAttributesForDailyData(GeneralInstances incomingData,int formatType) throws Exception {

		//与本地格式数据比较，这地方基本上会有nominal数据的label不一致，临时处理办法就是先替换掉
		GeneralInstances outputData = getDailyPredictDataFormat(formatType);
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(outputData);
		outputData=instanceProcessor.removeAttribs(outputData, ArffFormat.YEAR_MONTH_INDEX);

		instanceProcessor=InstanceHandler.getHandler(incomingData);
		instanceProcessor.calibrateAttributes(incomingData, outputData);
		return outputData;
	}


	/**
	 * @param format
	 */
	private void addModelData(String classifier,int format,String modelFilePrefix,String evalFilePrefix) {
		String id;
		PredictModelData modelData;
		id=classifier+format;
		modelData=new PredictModelData();
		modelData.setIdentify(id);
		modelData.setModelFileName(modelFilePrefix); 
		modelData.setEvalFileName(evalFilePrefix);
		modelData.setModelFormatType(format);
		this.PREDICT_MODELS.put(id, modelData);
	}


	/**
	 * @param formatType
	 * @return
	 * @throws Exception
	 */
	protected static GeneralInstances getDailyPredictDataFormat(int formatType)
			throws Exception {
		String formatFile=null;
		switch (formatType) {
		case ArffFormat.LEGACY_FORMAT: //可以使用同一个Format文件，只是需要将无关字段去掉
//			formatFile="trans20052016-legacy-format.arff";
//			break;
		case ArffFormat.EXT_FORMAT:
			formatFile=ArffFormat.TRANSACTION_ARFF_PREFIX+"-format.arff";
			break;
		case ArffFormatFullModel.FULLMODEL_FORMAT:
			formatFile=ArffFormatFullModel.FULL_MODEL_ARFF_PREFIX+"-format.arff";
			break;			
		default:
			throw new Exception("invalid arffFormat type");
		}

		GeneralInstances outputData=DataIOHandler.getSuppier().loadDataFromFile(PREDICT_WORK_DIR+formatFile); //C_ROOT_DIRECTORY+
		if (formatType==ArffFormat.LEGACY_FORMAT){//如果是原有模式，去掉扩展字段
			BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(outputData);
			outputData=instanceProcessor.removeAttribs(outputData, ArffFormat.EXT_ARFF_COLUMNS);
		}
		return outputData;
	}


}
