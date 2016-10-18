package yueyueGo;

import java.util.HashMap;

import yueyueGo.classifier.AdaboostClassifier;
import yueyueGo.classifier.BaggingM5P;
import yueyueGo.databeans.BaseInstances;
import yueyueGo.databeans.WekaInstances;
import yueyueGo.fullModel.ArffFormatFullModel;
import yueyueGo.fullModel.DBAccessFullModel;
import yueyueGo.fullModel.classifier.BaggingM5PFullModel;
import yueyueGo.fullModel.classifier.MyNNFullModel;
import yueyueGo.utility.AppContext;
import yueyueGo.utility.ClassifySummaries;
import yueyueGo.utility.ClassifyUtility;
import yueyueGo.utility.DBAccess;
import yueyueGo.utility.FileUtility;
import yueyueGo.utility.FormatUtility;
import yueyueGo.utility.InstanceUtility;
import yueyueGo.utility.MergeClassifyResults;
import yueyueGo.utility.PredictModelData;

public class DailyPredict {

	private static String PREDICT_WORK_DIR=EnvConstants.PREDICT_WORK_DIR;
	private static String PREDICT_RESULT_DIR=PREDICT_WORK_DIR+"\\88-预测结果\\"; 
	private static HashMap<String, PredictModelData> PREDICT_MODELS;

	protected static double[] shouyilv_thresholds; //对于胜率优先算法的收益率筛选阀值
	protected static double[] winrate_thresholds; //对于收益率优先算法的胜率筛选阀值

	private static void definePredictModels(String type){
		PREDICT_MODELS=new HashMap<String, PredictModelData>();


		int format;
		String classifierName;
		if(EnvConstants.AVG_LINE_ROOT_DIR.equalsIgnoreCase(type)){

			//=========================LEGACY FORMAT 部分========================
			format=ArffFormat.LEGACY_FORMAT;

//			//经过主成分分析后的数据模型---
//			//M5PAB旧格式预测模型
//			classifierName=ClassifyUtility.M5PAB;
//			addModelData(classifierName,format,"\\extData2005-2016-m5pAB-201607 MA ","\\extData2005-2016-m5pAB-201607 MA ");

			//MLPAB旧格式预测模型
			classifierName=ClassifyUtility.MLPAB;
			addModelData(classifierName,format,"\\extData2005-2016-mlpAB-2016 MA ","\\extData2005-2016-mlpAB-2016 MA ");

//			//BaggingM5P旧格式预测模型
//			classifierName=ClassifyUtility.BAGGING_M5P;
//			addModelData(classifierName,format,"\\extData2005-2016-baggingM5P-201606 MA ","\\extData2005-2016-baggingM5P-201607 MA ");
//
//			//adaboost旧格式预测模型
//			classifierName=ClassifyUtility.ADABOOST;
//			addModelData(classifierName,format,"\\extData2005-2016-adaboost-201606 MA ","\\extData2005-2016-adaboost-201606 MA ");
			
			//=========================EXT FORMAT 部分========================
			format=ArffFormat.EXT_FORMAT;			
			//MLPAB当前使用的预测模型
			classifierName=ClassifyUtility.MLPAB;
			addModelData(classifierName,format,"\\extData2005-2016-mlpAB-2016 MA ","\\extData2005-2016-mlpAB-201608 MA ");

			//BaggingM5P当前使用的预测模型
			classifierName=ClassifyUtility.BAGGING_M5P;
//			addModelData(classifierName,format,"\\extData2005-2016-baggingM5P-201607 MA ","\\extData2005-2016-baggingM5P-201608 MA ");
			addModelData(classifierName,format,"\\extData2005-2016-baggingM5P-201507 MA ","\\extData2005-2016-baggingM5P-201508 MA ");
			//adaboost当前使用的预测模型
			classifierName=ClassifyUtility.ADABOOST;
//			addModelData(classifierName,format,"\\extData2005-2016-adaboost-201607 MA ","\\extData2005-2016-adaboost-201608 MA ");
			addModelData(classifierName,format,"\\extData2005-2016-adaboost-201507 MA ","\\extData2005-2016-adaboost-201508 MA ");
			
		}else if(EnvConstants.FULL_MODEL_ROOT_DIR.equals(type)){
			// fullmodel不保留legacy
			format=ArffFormatFullModel.FULLMODEL_FORMAT;
			//BaggingM5PFullModel当前使用的预测模型---------FullMODEL
			classifierName=ClassifyUtility.BAGGING_M5P_FULLMODEL;
			addModelData(classifierName,format,"\\extData2005-2016-BaggingM5PABFullModel-201507 MA ", "\\extData2005-2016-BaggingM5PABFullModel-201508 MA ");

			//BaggingJ48FullModel当前使用的预测模型---------FullMODEL
			classifierName=ClassifyUtility.MYNN_MLP_FULLMODEL;
			addModelData(classifierName,format,"\\extData2005-2016-myNNFullModel-201507 MA ", "\\extData2005-2016-myNNFullModel-201508 MA ");
		}
	}


	public static void main(String[] args) {
		try {



			//短线模型的每日预测
			System.out.println("==================================================");
			System.out.println("===============starting 短线模型预测===============");
			System.out.println("==================================================");
			AppContext.createContext(EnvConstants.FULL_MODEL_ROOT_DIR);
			//预先初始化各种模型文件的位置
			definePredictModels(EnvConstants.FULL_MODEL_ROOT_DIR);
			shouyilv_thresholds=new double[] {0.03};
			winrate_thresholds=new double[] {0.5};
			callFullModelPredict();

			//用均线模型预测每日增量数据
			System.out.println("==================================================");
			System.out.println("===============starting 均线模型预测===============");
			System.out.println("==================================================");
			AppContext.clearContext();
			AppContext.createContext(EnvConstants.AVG_LINE_ROOT_DIR);
			//预先初始化各种模型文件的位置
			definePredictModels(EnvConstants.AVG_LINE_ROOT_DIR);
			shouyilv_thresholds=new double[] {0.005,0.005,0.01,0.03,0.03}; // {0.01,0.02,0.03,0.03,0.04};
			winrate_thresholds=new double[]  {0.45,0.45,0.45,0.35,0.25};  //{0.3,0.3,0.3,0.25,0.25};
			callDailyPredict();

		} catch (Exception e) {

			e.printStackTrace();
		}
	}


	/**
	 * @throws Exception
	 */
	private static void callDailyPredict() throws Exception {


		//用旧的模型预测每日增量数据用于对比

//		//旧MLP主成分分析预测
//		MLPABClassifier nABModelLegacy=new MLPABClassifier();
//		nABModelLegacy.setModelArffFormat(ArffFormat.LEGACY_FORMAT);
//		predictWithDB(nABModelLegacy);
//
//		//旧M5P主成分分析预测
//		M5PABClassifier cABModelLegacy=new M5PABClassifier();
//		cABModelLegacy.setModelArffFormat(ArffFormat.LEGACY_FORMAT);
//		predictWithDB(cABModelLegacy);		
//
//		//旧BaggingM5P
//		BaggingM5P cBagModelLegacy=new BaggingM5P();
//		cBagModelLegacy.setModelArffFormat(ArffFormat.LEGACY_FORMAT);
//		predictWithDB(cBagModelLegacy);
//		
//		//旧Adaboost
//		AdaboostClassifier adaModelLegacy=new AdaboostClassifier();
//		adaModelLegacy.setModelArffFormat(ArffFormat.LEGACY_FORMAT);
//		predictWithDB(adaModelLegacy);
//		
//		
//		//新格式的MLP主成分分析预测
//		MLPABClassifier nABModel=new MLPABClassifier();
//		predictWithDB(nABModel);
		
		//新格式的bagging主成分分析预测
		BaggingM5P cBagModel=new BaggingM5P();
		BaseInstances baggingInstances=predictWithDB(cBagModel);

		//Adaboost
		AdaboostClassifier adaModel=new AdaboostClassifier();
		BaseInstances adaboostInstances=predictWithDB(adaModel);		

//		System.out.println("***************** now output legacy prediction results************************");
//		nABModelLegacy.outputClassifySummary();
//		nABModelLegacy=null;
//		cABModelLegacy.outputClassifySummary();
//		cABModelLegacy=null;
//		cBagModelLegacy.outputClassifySummary();
//		cBagModelLegacy=null;
//		adaModelLegacy.outputClassifySummary();
//		adaModelLegacy=null;
//		System.out.println("***************** end of output legacy prediction results************************");

//		//保留bagging结果
//		FileUtility.saveCSVFile(baggingInstances, PREDICT_RESULT_DIR+cBagModel.getIdentifyName()+"Selected Result"+FormatUtility.getDateStringFor(1)+".csv");	
//		//保留adaboost结果
//		FileUtility.saveCSVFile(adaboostInstances, PREDICT_RESULT_DIR+adaModel.getIdentifyName()+"Selected Result"+FormatUtility.getDateStringFor(1)+".csv");	

		System.out.println("***************** now output prediction results************************");
//		nABModel.outputClassifySummary();
		cBagModel.outputClassifySummary();
		adaModel.outputClassifySummary();
		System.out.println("***************** end of output prediction results************************");
		
		
		//以adaboost为主，合并bagging
		System.out.println("-----now output combined predictions----------"+adaModel.getIdentifyName());
		BaseInstances left=FileUtility.loadDataFromFile(getLeftArffFileName(adaModel)); //获取刚生成的左侧文件（主要存了CODE）
		MergeClassifyResults merge=new MergeClassifyResults(shouyilv_thresholds, winrate_thresholds);
		BaseInstances mergedOutput=merge.mergeResults(adaboostInstances,baggingInstances,ArffFormat.RESULT_PREDICTED_PROFIT,left);
		mergedOutput=InstanceUtility.removeAttribs(mergedOutput, new String[]{ArffFormat.IS_POSITIVE,ArffFormat.SHOUYILV}); // 去掉空的收益率或positive字段
		FileUtility.saveCSVFile(mergedOutput, PREDICT_RESULT_DIR+ "Merged Selected Result-"+adaModel.getIdentifyName()+"-"+FormatUtility.getDateStringFor(1)+".csv");
		mergedOutput=null;
		System.out.println(adaModel.getIdentifyName()+"----------prediction ends---------");
		//以bagging为主，合并adaboost
		System.out.println("-----now output combined predictions----------"+cBagModel.getIdentifyName());
		BaseInstances mergedOutputBagging=merge.mergeResults(baggingInstances,adaboostInstances,ArffFormat.RESULT_PREDICTED_WIN_RATE,left);
		mergedOutputBagging=InstanceUtility.removeAttribs(mergedOutputBagging, new String[]{ArffFormat.IS_POSITIVE,ArffFormat.SHOUYILV}); // 去掉空的收益率或positive字段
		FileUtility.saveCSVFile(mergedOutputBagging, PREDICT_RESULT_DIR+ "Merged Selected Result-"+cBagModel.getIdentifyName()+"-"+FormatUtility.getDateStringFor(1)+".csv");
		mergedOutputBagging=null;
		System.out.println(cBagModel.getIdentifyName()+"----------prediction ends--------");
	}


	/**
	 * @throws Exception
	 */
	private static void callFullModelPredict() throws Exception {

		//BaggingM5P
		BaggingM5PFullModel cFullModel=new BaggingM5PFullModel();
		BaseInstances cInstances=predictWithDB(cFullModel);		

		//BaggingJ48
//		BaggingJ48FullModel nFullModel=new BaggingJ48FullModel();
		MyNNFullModel nFullModel= new MyNNFullModel(); 
		BaseInstances nInstances=predictWithDB(nFullModel);		

		cFullModel.outputClassifySummary();
		nFullModel.outputClassifySummary();

		//合并baggingJ48和baggingM5P
		System.out.println("-----now output combined predictions----------"+cFullModel.getIdentifyName());
		BaseInstances left=FileUtility.loadDataFromFile(getLeftArffFileName(cFullModel)); //获取刚生成的左侧文件（主要存了CODE）

		MergeClassifyResults merge=new MergeClassifyResults(shouyilv_thresholds, winrate_thresholds);
		BaseInstances mergedOutput=merge.mergeResults(cInstances,nInstances,ArffFormat.RESULT_PREDICTED_WIN_RATE,left);
		mergedOutput=InstanceUtility.removeAttribs(mergedOutput, new String[]{ArffFormat.IS_POSITIVE,ArffFormat.SHOUYILV}); // 去掉空的收益率或positive字段
		FileUtility.saveCSVFile(mergedOutput, PREDICT_RESULT_DIR+ "FullModel Selected Result-"+cFullModel.getIdentifyName()+"-"+FormatUtility.getDateStringFor(1)+".csv");

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
	private static BaseInstances predictWithDB(BaseClassifier clModel) throws Exception {
		System.out.println("predict using classifier : "+clModel.getIdentifyName()+" @ prediction work path :"+PREDICT_WORK_DIR);
		System.out.println("-----------------------------");
		BaseInstances fullData = null;
		
		int dataFormat=clModel.getModelArffFormat();
		switch (dataFormat){
		case ArffFormat.LEGACY_FORMAT:
			fullData=DBAccess.LoadDataFromDB(dataFormat);
			break;
		case ArffFormat.EXT_FORMAT:
			fullData=DBAccess.LoadDataFromDB(dataFormat);
			break;
		case ArffFormatFullModel.FULLMODEL_FORMAT:
			fullData = DBAccessFullModel.LoadFullModelDataFromDB();//"2016-08-26");
			break;			
		default:
			throw new Exception("invalid arffFormat type");
		}
		//保留DAILY RESULT的LEFT部分在磁盘上
		BaseInstances left = new WekaInstances(fullData);
		left=InstanceUtility.keepAttributes(fullData, ArffFormat.DAILY_PREDICT_RESULT_LEFT);
		FileUtility.SaveDataIntoFile(left,  getLeftArffFileName(clModel));

		//去掉多读入的CODE部分
		fullData=InstanceUtility.removeAttribs(fullData, new String[]{ArffFormat.CODE});

		BaseInstances result=predict(clModel,  fullData);

		return result;
	}


	//用模型预测数据

	private static BaseInstances predict(BaseClassifier clModel, BaseInstances inData) throws Exception {
		BaseInstances newData = null;
		BaseInstances result = null;

		//创建存储评估结果的数据容器
		ClassifySummaries modelSummaries=new ClassifySummaries(clModel.getIdentifyName(),true);
		clModel.setClassifySummaries(modelSummaries);

		BaseInstances fullData=calibrateAttributesForDailyData(inData,clModel.getModelArffFormat());

		//如果模型需要计算字段，则把计算字段加上
		if (clModel.m_noCaculationAttrib==false){
			fullData=ArffFormat.addCalculateAttribute(fullData);		
		}


		//获得”均线策略"的位置属性, 如果数据集内没有“均线策略”（短线策略的fullmodel），MaIndex为-1
		int maIndex=InstanceUtility.findATTPosition(fullData,ArffFormat.SELECTED_AVG_LINE);

		if (clModel instanceof NominalClassifier ){
			fullData=((NominalClassifier)clModel).processDataForNominalClassifier(fullData,false);
		}


		for (int j = 0; j < clModel.m_policySubGroup.length; j++) {

			System.out.println("start to load data for policy : "	+ clModel.m_policySubGroup[j]);
			String expression=null;
			if (maIndex>0){// 均线策略
				expression=InstanceUtility.WEKA_ATT_PREFIX+ maIndex+" is '"+ clModel.m_policySubGroup[j] + "'";
				newData = InstanceUtility.getInstancesSubset(fullData, expression);
			}else{ //短线策略（fullmodel)				
				newData=fullData;
			}


			String modelFileName;
			String evalFileName;
			String id=clModel.classifierName+clModel.modelArffFormat;
			PredictModelData modelData=PREDICT_MODELS.get(id);
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
				BaseInstances header = new WekaInstances(newData, 0);
				result=InstanceUtility.keepAttributes(header, ArffFormat.DAILY_PREDICT_RESULT_LEFT);

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
	private static BaseInstances calibrateAttributesForDailyData(BaseInstances incomingData,int formatType) throws Exception {

		//与本地格式数据比较，这地方基本上会有nominal数据的label不一致，临时处理办法就是先替换掉
		BaseInstances outputData = getDailyPredictDataFormat(formatType);

		outputData=InstanceUtility.removeAttribs(outputData, ArffFormat.YEAR_MONTH_INDEX);


		InstanceUtility.calibrateAttributes(incomingData, outputData);
		return outputData;
	}


	/**
	 * @param format
	 */
	private static void addModelData(String classifier,int format,String modelFilePrefix,String evalFilePrefix) {
		String id;
		PredictModelData modelData;
		id=classifier+format;
		modelData=new PredictModelData();
		modelData.setIdentify(id);
		modelData.setModelFileName(modelFilePrefix); 
		modelData.setEvalFileName(evalFilePrefix);
		modelData.setModelFormatType(format);
		PREDICT_MODELS.put(id, modelData);
	}


	/**
	 * @param formatType
	 * @return
	 * @throws Exception
	 */
	protected static BaseInstances getDailyPredictDataFormat(int formatType)
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

		BaseInstances outputData=FileUtility.loadDataFromFile(PREDICT_WORK_DIR+formatFile); //C_ROOT_DIRECTORY+
		if (formatType==ArffFormat.LEGACY_FORMAT){//如果是原有模式，去掉扩展字段
			outputData=InstanceUtility.removeAttribs(outputData, ArffFormat.EXT_ARFF_COLUMNS);
		}
		return outputData;
	}


}
