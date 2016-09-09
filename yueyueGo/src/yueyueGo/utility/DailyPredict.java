package yueyueGo.utility;

import java.util.HashMap;

import weka.core.Instances;
import yueyueGo.ArffFormat;
import yueyueGo.BaseClassifier;
import yueyueGo.EnvConstants;
import yueyueGo.NominalClassifier;
import yueyueGo.ProcessData;
import yueyueGo.classifier.AdaboostClassifier;
import yueyueGo.classifier.BaggingM5P;
import yueyueGo.classifier.M5PABClassifier;
import yueyueGo.classifier.M5PClassifier;
import yueyueGo.classifier.MLPABClassifier;
import yueyueGo.classifier.MLPClassifier;
import yueyueGo.fullModel.ArffFormatFullModel;
import yueyueGo.fullModel.DBAccessFullModel;
import yueyueGo.fullModel.classifier.BaggingJ48FullModel;
import yueyueGo.fullModel.classifier.BaggingM5PFullModel;

public class DailyPredict {
	private static final String EVAL_FILE="-EVAL";
	private static String PREDICT_WORK_DIR=EnvConstants.PREDICT_WORK_DIR;
	private static String PREDICT_RESULT_DIR=PREDICT_WORK_DIR+"\\88-预测结果\\"; 
	private static HashMap<String, String> PREDICT_MODELS;

	
	private static void definePredictModels(){
		PREDICT_MODELS=new HashMap<String, String>();

		String classifierName;
	
		//M5P当前使用的预测模型
		classifierName=new M5PClassifier().classifierName;
		PREDICT_MODELS.put(classifierName, "\\extData2005-2016-m5p-201607 MA ");
		PREDICT_MODELS.put(classifierName+EVAL_FILE, "\\extData2005-2016-m5p-201607 MA ");
	
		//MLP当前使用的预测模型
		classifierName=new MLPClassifier().classifierName;
		PREDICT_MODELS.put(classifierName, "\\extData2005-2016 month-new-mlp-2016 MA ");
		PREDICT_MODELS.put(classifierName+EVAL_FILE, "\\extData2005-2016 month-new-mlp-201606 MA ");
	
		//经过主成分分析后的数据模型---
	
		//M5PAB当前使用的预测模型
		classifierName=new M5PABClassifier().classifierName;
		PREDICT_MODELS.put(classifierName, "\\extData2005-2016-m5pAB-201607 MA ");
		PREDICT_MODELS.put(classifierName+EVAL_FILE, "\\extData2005-2016-m5pAB-201607 MA ");
	
		//MLPAB当前使用的预测模型
		classifierName=new MLPABClassifier().classifierName;
		PREDICT_MODELS.put(classifierName, "\\extData2005-2016-mlpAB-2016 MA ");
		PREDICT_MODELS.put(classifierName+EVAL_FILE, "\\extData2005-2016-mlpAB-2016 MA ");
	
		//BaggingM5P当前使用的预测模型
		classifierName=new BaggingM5P().classifierName;
		PREDICT_MODELS.put(classifierName, "\\extData2005-2016-baggingM5P-201606 MA ");
		PREDICT_MODELS.put(classifierName+EVAL_FILE, "\\extData2005-2016-baggingM5P-201607 MA ");
	
		//adaboost当前使用的预测模型
		classifierName=new AdaboostClassifier().classifierName;
		PREDICT_MODELS.put(classifierName, "\\extData2005-2016-adaboost-201606 MA ");
		PREDICT_MODELS.put(classifierName+EVAL_FILE, "\\extData2005-2016-adaboost-201606 MA ");
	
		//BaggingM5PFullModel当前使用的预测模型---------FullMODEL
		classifierName=new BaggingM5PFullModel().classifierName;
		PREDICT_MODELS.put(classifierName, "\\extData2005-2016-BaggingM5PABFullModel-201606 MA ");
		PREDICT_MODELS.put(classifierName+EVAL_FILE, "\\extData2005-2016-BaggingM5PABFullModel-201607 MA ");
	
		//BaggingJ48FullModel当前使用的预测模型---------FullMODEL
		classifierName=new BaggingJ48FullModel().classifierName;
		PREDICT_MODELS.put(classifierName, "\\extData2005-2016-BaggingJ48ABFullModel-201606 MA ");
		PREDICT_MODELS.put(classifierName+EVAL_FILE, "\\extData2005-2016-BaggingJ48ABFullModel-201607 MA ");
	}


	public static void main(String[] args) {
		try {



			//短线模型的每日预测
			System.out.println("==================================================");
			System.out.println("===============starting 短线模型预测===============");
			System.out.println("==================================================");
			AppContext.createContext(EnvConstants.FULL_MODEL_ROOT_DIR);
			//预先初始化各种模型文件的位置
			definePredictModels();
			
			callFullModelPredict();

			//用均线模型预测每日增量数据
			System.out.println("==================================================");
			System.out.println("===============starting 均线模型预测===============");
			System.out.println("==================================================");
			AppContext.clearContext();
			AppContext.createContext(EnvConstants.AVG_LINE_ROOT_DIR);
			//预先初始化各种模型文件的位置
			definePredictModels();

			callDailyPredict();

		} catch (Exception e) {

			e.printStackTrace();
		}
	}


	/**
	 * @throws Exception
	 */
	private static void callDailyPredict() throws Exception {
	
	
		//用旧的M5P模型预测每日增量数据用于对比
		//		String classifierName=new M5PClassifier().classifierName;
		//		PREDICT_MODELS.put(classifierName, "\\交易分析2005-2016 by month-new-m5p-201605 MA ");
		//		PREDICT_MODELS.put(classifierName+"-EVAL", "\\交易分析2005-2016 by month-new-m5p-201605 MA ");		
		//		M5PClassifier cModel=new M5PClassifier();
		//		cModel.setModelArffFormat(ArffFormat.LEGACY_FORMAT); 
		//		predictWithDB(cModel,PREDICT_WORK_DIR);
	
		//MLP主成分分析预测
		MLPABClassifier nABModel=new MLPABClassifier();
		predictWithDB(nABModel,PREDICT_WORK_DIR);
	
		//M5P主成分分析预测
		M5PABClassifier cABModel=new M5PABClassifier();
		predictWithDB(cABModel,PREDICT_WORK_DIR);		
	
		//BaggingM5P
		BaggingM5P cBagModel=new BaggingM5P();
		Instances baggingInstances=predictWithDB(cBagModel,PREDICT_WORK_DIR);
		//保留一下结果
		FileUtility.saveCSVFile(baggingInstances, PREDICT_RESULT_DIR+cBagModel.getIdentifyName()+"Selected Result"+FormatUtility.getDateStringFor(1)+".csv");	

		//Adaboost
		AdaboostClassifier adaModel=new AdaboostClassifier();
		Instances adaboostInstances=predictWithDB(adaModel,PREDICT_WORK_DIR);		
		//保留一下结果
		FileUtility.saveCSVFile(adaboostInstances, PREDICT_RESULT_DIR+adaModel.getIdentifyName()+"Selected Result"+FormatUtility.getDateStringFor(1)+".csv");	
	
		//		cModel.outputClassifySummary();
		nABModel.outputClassifySummary();
		cABModel.outputClassifySummary();
		cBagModel.outputClassifySummary();
		adaModel.outputClassifySummary();
	
		//以adaboost为主，合并bagging
		System.out.println("-----now output combined predictions----------"+adaModel.getIdentifyName());
		Instances left=FileUtility.loadDataFromFile(PREDICT_RESULT_DIR+ "LEFT "+FormatUtility.getDateStringFor(1)+".arff"); //获取刚生成的左侧文件（主要存了CODE）
		Instances mergedOutput=ProcessData.mergeResults(adaboostInstances,baggingInstances,ArffFormat.RESULT_PREDICTED_PROFIT,left);
		mergedOutput=InstanceUtility.removeAttribs(mergedOutput, new String[]{ArffFormat.IS_POSITIVE,ArffFormat.SHOUYILV}); // 去掉空的收益率或positive字段
		FileUtility.saveCSVFile(mergedOutput, PREDICT_RESULT_DIR+ "Merged Selected Result-"+adaModel.getIdentifyName()+"-"+FormatUtility.getDateStringFor(1)+".csv");
		mergedOutput=null;
	
		//以bagging为主，合并adaboost
		System.out.println("-----now output combined predictions----------"+cBagModel.getIdentifyName());
		Instances mergedOutputBagging=ProcessData.mergeResults(baggingInstances,adaboostInstances,ArffFormat.RESULT_PREDICTED_WIN_RATE,left);
		mergedOutputBagging=InstanceUtility.removeAttribs(mergedOutputBagging, new String[]{ArffFormat.IS_POSITIVE,ArffFormat.SHOUYILV}); // 去掉空的收益率或positive字段
		FileUtility.saveCSVFile(mergedOutputBagging, PREDICT_RESULT_DIR+ "Merged Selected Result-"+cBagModel.getIdentifyName()+"-"+FormatUtility.getDateStringFor(1)+".csv");
		mergedOutputBagging=null;
	}


	/**
	 * @throws Exception
	 */
	private static void callFullModelPredict() throws Exception {
	
		//BaggingM5P
		BaggingM5PFullModel cBagModel=new BaggingM5PFullModel();
		Instances cBagInstances=predictFullModelWithDB(cBagModel,PREDICT_WORK_DIR);		
	
		//BaggingJ48
		BaggingJ48FullModel nBagModel=new BaggingJ48FullModel();
		Instances nBagInstances=predictFullModelWithDB(nBagModel,PREDICT_WORK_DIR);		
	
		cBagModel.outputClassifySummary();
		nBagModel.outputClassifySummary();
	
		//合并baggingJ48和baggingM5P
		System.out.println("-----now output combined predictions----------"+cBagModel.getIdentifyName());
		Instances left=FileUtility.loadDataFromFile(PREDICT_RESULT_DIR+ "LEFT "+FormatUtility.getDateStringFor(1)+".arff"); //获取刚生成的左侧文件（主要存了CODE）
		//				InstanceUtility.keepAttributes(cBagInstances, ArffFormat.DAILY_PREDICT_RESULT_LEFT) ; //为了使用下面的合并文件方法造出一个LEFT来
		Instances mergedOutput=ProcessData.mergeResults(cBagInstances,nBagInstances,ArffFormat.RESULT_PREDICTED_WIN_RATE,left);
		mergedOutput=InstanceUtility.removeAttribs(mergedOutput, new String[]{ArffFormat.IS_POSITIVE,ArffFormat.SHOUYILV}); // 去掉空的收益率或positive字段
		FileUtility.saveCSVFile(mergedOutput, PREDICT_RESULT_DIR+ "FullModel Selected Result-"+cBagModel.getIdentifyName()+"-"+FormatUtility.getDateStringFor(1)+".csv");
	
	}


	//直接访问数据库预测每天的自选股数据，不单独保存每个模型的选股
	private static Instances predictFullModelWithDB(BaseClassifier clModel, String pathName) throws Exception {
		System.out.println("predict using classifier : "+clModel.getIdentifyName()+" @ prediction work path :"+pathName);
		System.out.println("-----------------------------");
		Instances fullData = DBAccessFullModel.LoadFullModelDataFromDB();//"2016-08-26");
		//保留DAILY RESULT的LEFT部分在磁盘上
		Instances left = new Instances(fullData);
		left=InstanceUtility.keepAttributes(fullData, ArffFormat.DAILY_PREDICT_RESULT_LEFT);
		FileUtility.SaveDataIntoFile(left, pathName + "LEFT ("+clModel.getModelArffFormat()+") "+FormatUtility.getDateStringFor(1)+".arff");

		//去掉多读入的CODE部分
		fullData=InstanceUtility.removeAttribs(fullData, new String[]{ArffFormat.CODE});

		Instances result=predict(clModel, pathName, fullData);

		return result;
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


	//直接访问数据库预测每天的增量数据
	private static Instances predictWithDB(BaseClassifier clModel, String pathName) throws Exception {
		System.out.println("predict using classifier : "+clModel.getIdentifyName()+" @ prediction work path :"+pathName);
		System.out.println("-----------------------------");
		Instances fullData = DBAccess.LoadDataFromDB(clModel.getModelArffFormat());
		//保留DAILY RESULT的LEFT部分在磁盘上
		Instances left = new Instances(fullData);
		left=InstanceUtility.keepAttributes(fullData, ArffFormat.DAILY_PREDICT_RESULT_LEFT);
		FileUtility.SaveDataIntoFile(left, pathName + "LEFT ("+clModel.getModelArffFormat()+") "+FormatUtility.getDateStringFor(1)+".arff");

		//去掉多读入的CODE部分
		fullData=InstanceUtility.removeAttribs(fullData, new String[]{ArffFormat.CODE});

		Instances result=predict(clModel, pathName, fullData);

		return result;
	}


	//用模型预测数据

	private static Instances predict(BaseClassifier clModel, String pathName, Instances inData) throws Exception {
		Instances newData = null;
		Instances result = null;

		//创建存储评估结果的数据容器
		ClassifySummaries modelSummaries=new ClassifySummaries(clModel.getIdentifyName());
		clModel.setClassifySummaries(modelSummaries);

		Instances fullData=calibrateAttributesForDailyData(inData,clModel.getModelArffFormat());

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

			System.out.println("start to load data for " + ArffFormat.SELECTED_AVG_LINE+"  : "	+ clModel.m_policySubGroup[j]);
			String expression=null;
			if (maIndex>0){// 均线策略
				expression=InstanceUtility.WEKA_ATT_PREFIX+ maIndex+" is '"+ clModel.m_policySubGroup[j] + "'";
				newData = InstanceUtility.getInstancesSubset(fullData, expression);
			}else{ //短线策略（fullmodel)				
				newData=fullData;
			}


			String modelFileName;
			String evalFileName;
			modelFileName=PREDICT_MODELS.get(clModel.classifierName);
			evalFileName=PREDICT_MODELS.get(clModel.classifierName+EVAL_FILE);
			modelFileName = pathName+"\\"+clModel.getIdentifyName()+ modelFileName
					+ clModel.m_policySubGroup[j]	;				
			evalFileName = pathName+"\\"+clModel.getIdentifyName()+evalFileName
					+ clModel.m_policySubGroup[j]+BaseClassifier.THRESHOLD_EXTENSION	;				

			clModel.setModelFileName(modelFileName);
			clModel.setEvaluationFilename(evalFileName);

			System.out.println(" new data size , row : "+ newData.numInstances() + " column: "	+ newData.numAttributes());
			if (result == null) {// initialize result instances
				// remove unnecessary data,leave 均线策略 & code alone
				Instances header = new Instances(newData, 0);
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

			clModel.predictData(newData, result);
			System.out.println("accumulated predicted rows: "+ result.numInstances());
			System.out.println("complete for : "+ clModel.m_policySubGroup[j]);
		}
		if (result.numInstances()!=inData.numInstances()) {
			throw new Exception("not all data have been processed!!!!! incoming Data number = " +inData.numInstances() + " while predicted number is "+result.numInstances());
		}

		return result;
	}


	//这是对增量数据nominal label的处理 （因为增量数据中的nominal数据，label会可能不全）
	private static Instances calibrateAttributesForDailyData(Instances incomingData,int formatType) throws Exception {

		//与本地格式数据比较，这地方基本上会有nominal数据的label不一致，临时处理办法就是先替换掉
		Instances outputData = getDailyPredictDataFormat(formatType);

		outputData=InstanceUtility.removeAttribs(outputData, ArffFormat.YEAR_MONTH_INDEX);


		InstanceUtility.calibrateAttributes(incomingData, outputData);
		return outputData;
	}


	/**
	 * @param formatType
	 * @return
	 * @throws Exception
	 */
	protected static Instances getDailyPredictDataFormat(int formatType)
			throws Exception {
		String formatFile=null;
		switch (formatType) {
		case ArffFormat.LEGACY_FORMAT:
			formatFile="trans20052016-legacy-format.arff";
			break;
		case ArffFormat.EXT_FORMAT:
			formatFile=ArffFormat.TRANSACTION_ARFF_PREFIX+"-format.arff";
			break;
		case ArffFormatFullModel.FULLMODEL_FORMAT:
			formatFile=ArffFormatFullModel.FULL_MODEL_ARFF_PREFIX+"-format.arff";
			break;			
		default:
			throw new Exception("invalid arffFormat type");
		}

		Instances outputData=FileUtility.loadDataFromFile(PREDICT_WORK_DIR+formatFile); //C_ROOT_DIRECTORY+
		return outputData;
	}


}
