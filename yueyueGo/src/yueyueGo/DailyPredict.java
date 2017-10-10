package yueyueGo;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import weka.core.SerializationHelper;
import yueyueGo.classifier.AdaboostClassifier;
import yueyueGo.classifier.BaggingM5P;
import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.dataFormat.AvgLineDataFormat;
import yueyueGo.dataFormat.FullModelDataFormat;
import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.dataProcessor.InstanceHandler;
import yueyueGo.dataProcessor.WekaInstanceProcessor;
import yueyueGo.databeans.DataInstances;
import yueyueGo.databeans.GeneralInstance;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.datasource.DataIOHandler;
import yueyueGo.fullModel.classifier.BaggingM5PFullModel;
import yueyueGo.fullModel.classifier.MyNNFullModel;
import yueyueGo.utility.AppContext;
import yueyueGo.utility.ClassifySummaries;
import yueyueGo.utility.ClassifyUtility;
import yueyueGo.utility.FileUtility;
import yueyueGo.utility.FormatUtility;
import yueyueGo.utility.MergeClassifyResults;
import yueyueGo.utility.modelEvaluation.EvaluationStore;
import yueyueGo.utility.modelEvaluation.ModelStore;
import yueyueGo.utility.modelPredict.ModelPredictor;
import yueyueGo.utility.modelPredict.PredictModelData;
import yueyueGo.utility.modelPredict.PredictStatus;

public class DailyPredict {

	private static String PREDICT_MODEL_DIR=EnvConstants.PREDICT_WORK_DIR+"\\66-模型\\";
	protected ArffFormat ARFF_FORMAT; //当前所用数据文件格式 
	private static String PREDICT_RESULT_DIR=EnvConstants.PREDICT_WORK_DIR+"\\88-预测结果\\"; 
	private HashMap<String, PredictModelData> PREDICT_MODELS;
	
	private HashMap<String, GeneralInstances> cached_daily_data=new HashMap<String, GeneralInstances>(); //从数据库里加载的每日预测数据

	private void definePredictModels(){
		PREDICT_MODELS=new HashMap<String, PredictModelData>();


		int format;
		String classifierName;

		//=========================LEGACY FORMAT 不常用========================
		//			format=ArffFormat.LEGACY_FORMAT;
		//			//旧格式预测模型
		//			//BaggingM5P上次使用的预测模型
		//			classifierName=ClassifyUtility.BAGGING_M5P+ClassifyUtility.MULTI_PCA_SURFIX;
		//			addModelData(classifierName,format,"\\extData2005-2016-baggingM5P-201606 MA ");
		//			
		//			//adaboost上次使用的预测模型
		//			classifierName=ClassifyUtility.ADABOOST;
		//			addModelData(classifierName,format,"\\extData2005-2016-adaboost-201606 MA ");

		//=========================EXT FORMAT 部分========================
		format=ArffFormat.CURRENT_FORMAT;

		//BaggingM5P当前使用的预测模型
		classifierName=ClassifyUtility.BAGGING_M5P+ClassifyUtility.MULTI_PCA_SURFIX;
		addModelData(classifierName,format,"\\trans20052017(9)-baggingM5P-201707 MA ","201707");

		//adaboost当前使用的预测模型
		classifierName=ClassifyUtility.ADABOOST;
		addModelData(classifierName,format,"\\trans20052017(9)-adaboost-201707 MA ","201707");


		// fullmodel不保留legacy
		//			format=FullModelDataFormat.FULLMODEL_FORMAT;
		//			//BaggingM5PFullModel当前使用的预测模型---------FullMODEL
		//			classifierName=ClassifyUtility.BAGGING_M5P_FULLMODEL+ClassifyUtility.MULTI_PCA_SURFIX;
		////			addModelData(classifierName,format,"\\extData2005-2016-BaggingM5PABFullModel-201607 MA ", "\\extData2005-2016-BaggingM5PABFullModel-201607 MA ");
		//			addModelData(classifierName,format, "\\fullModel20052017(1009)-BaggingM5PABFullModel-201609 MA ");
		//			//BaggingJ48FullModel当前使用的预测模型---------FullMODEL
		//			classifierName=ClassifyUtility.MYNN_MLP_FULLMODEL;
		////			addModelData(classifierName,format,"\\extData2005-2016-myNNFullModel-2016 MA ", "\\extData2005-2016-myNNFullModel-201603 MA ");
		//			addModelData(classifierName,format, "\\fullModel20052017(1009)-myNNFullModel-201603 MA ");
			
	}


	public static void main(String[] args) {
		try {
			System.out.println("Database URL in USE : "+EnvConstants.URL + " Please ensure this is the correct environment you want to use.....");
//			callFullModelPredict();
//			callDailyPredict();
			
			copyPredictModels();

		} catch (Exception e) {

			e.printStackTrace();
		}
	}
	
	/*
	 * 从回测模型中选取最新的模型拷贝至目标目录
	 */
	public static void copyPredictModels() throws Exception {
		String currentMonth="201707"; //这是评估有的最新数据的月份
		HashMap<String, String> fileMap;
		BackTest worker=new BackTest();
		worker.init();
		
		//BaggingM5P
		BaggingM5P cBagModel=new BaggingM5P();
		//Adaboost
		AdaboostClassifier adaModel=new AdaboostClassifier();
		AbstractModel[] models=new AbstractModel[2];
		models[0]=cBagModel;
		models[1]=adaModel;

		int fileNum=0;
		for (AbstractModel model : models) {
			fileMap=worker.findModelFiles(model, currentMonth);
			String targetPath=getPredictPath(model);
			FileUtility.mkdirIfNotExist(targetPath);

			String filename;
			String originalPath;
			for (Entry<String, String> entry : fileMap.entrySet()) {
				filename=entry.getKey();
				originalPath=entry.getValue();
				File src=new File(originalPath+filename);
				File dest=new File(targetPath+filename);
				Files.copy(src.toPath(),dest.toPath());
				System.out.println("\t src file="+src.getName());
				System.out.println("\t dest file="+dest.getName());
				fileNum++;
			}
		}

		System.out.println("Number of files copied:"+fileNum);
	}
	


	/**
	 * 每日预测
	 * @throws Exception
	 */
	public static String callDailyPredict() throws Exception {
		DailyPredict worker = new DailyPredict();
		//用均线模型预测每日增量数据
		System.out.println("==================================================");
		System.out.println("===============starting 均线模型预测===============");
		System.out.println("==================================================");
		//预先初始化各种模型文件的位置
		worker.ARFF_FORMAT=new AvgLineDataFormat();
		AppContext.clearContext();
		AppContext.createContext(worker.ARFF_FORMAT.m_data_root_directory);

		worker.definePredictModels();
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
		
		//预先初始化各种模型文件的位置
		worker.ARFF_FORMAT=new FullModelDataFormat();
		AppContext.clearContext();
		AppContext.createContext(worker.ARFF_FORMAT.m_data_root_directory);
		worker.definePredictModels();
		return worker.fullModelPredict();
	}


	/**
	 * @throws Exception
	 */
	private String dailyPredict() throws Exception {


//		//新格式的bagging线性回归预测
//		BaggingLinearRegression lBagModel=new BaggingLinearRegression();
//		GeneralInstances linearInstances=predictWithDB(lBagModel);
//		
//		//新格式的神经网络预测
//		MyNNClassifier nnModel=new MyNNClassifier();
//		GeneralInstances nnInstances=predictWithDB(nnModel);
		
//		//数据库迁移，暂时停止第八组数据的预测
//		System.out.println("******LEGACY*********** output LEGACY  prediction results**************LEGACY**********");
//		//旧格式的bagging m5p预测 
//		BaggingM5P cOldBagModel=new BaggingM5P();
//		cOldBagModel.m_usePCA=true;
//		cOldBagModel.setModelArffFormat(ArffFormat.LEGACY_FORMAT);
//		GeneralInstances baggingOldInstances=predictWithDB(cOldBagModel);
//
//		//Adaboost(使用PCA版本和计算字段）
//		AdaboostClassifier adaOldModel=new AdaboostClassifier();
//		adaOldModel.m_usePCA=true;
//		adaOldModel.m_positiveLine=0;
//		adaOldModel.setModelArffFormat(ArffFormat.LEGACY_FORMAT);
//		GeneralInstances adaboostOldInstances=predictWithDB(adaOldModel);		
//		combinePreditions(cOldBagModel, baggingOldInstances, adaOldModel, adaboostOldInstances);
//		System.out.println("******LEGACY*********** end of output LEGACY prediction results**********LEGACY**************");
		
		
		//新格式的bagging m5p预测  (使用PCA版本和计算字段）
		BaggingM5P cBagModel=new BaggingM5P();
		GeneralInstances baggingInstances=predictWithDB(cBagModel);

		//Adaboost(使用PCA版本和计算字段）
		AdaboostClassifier adaModel=new AdaboostClassifier();
		GeneralInstances adaboostInstances=predictWithDB(adaModel);		

//		System.out.println("******LEGACY*********** output LEGACY  prediction results**************LEGACY**********");
//		cOldBagModel.outputClassifySummary();
//		adaOldModel.outputClassifySummary();
//		System.out.println("******LEGACY*********** end of output LEGACY prediction results**********LEGACY**************");

		System.out.println("***************** now output prediction results************************");
//		lBagModel.outputClassifySummary();
//		nnModel.outputClassifySummary();
		cBagModel.outputClassifySummary();
		adaModel.outputClassifySummary();
		System.out.println("***************** end of output prediction results************************");
		
		
		
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
	public String combinePreditions(AbstractModel cModel,
			GeneralInstances cInstances, AbstractModel nModel,
			GeneralInstances nInstances) throws Exception {
		
		
		//以二分类器为主，合并连续分类器
		System.out.println("");
		System.out.println("-----now output combined predictions----------"+nModel.getIdentifyName()+" (merged with："+cModel.getIdentifyName()+")");
		GeneralInstances left=DataIOHandler.getSuppier().loadDataFromFile(getLeftArffFileName(nModel)); //获取刚生成的左侧文件（主要存了CODE）
		MergeClassifyResults merge=new MergeClassifyResults(ARFF_FORMAT.m_policy_group);
		GeneralInstances nMergedOutput=merge.mergeResults(nInstances,cInstances,ArffFormat.RESULT_PREDICTED_PROFIT,left);
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(nMergedOutput);
		nMergedOutput=instanceProcessor.removeAttribs(nMergedOutput, new String[]{ArffFormat.IS_POSITIVE,ArffFormat.SHOUYILV}); // 去掉空的收益率或positive字段
		String savedNFileName=PREDICT_RESULT_DIR+ "Merged Selected Result-"+nModel.getIdentifyName()+"-"+FormatUtility.getDateStringFor(1)+".csv";
		DataIOHandler.getSaver().saveCSVFile(nMergedOutput, savedNFileName);
		nMergedOutput=null;
		System.out.println(nModel.getIdentifyName()+"----------prediction ends---------");
		//以连续分类器为主，合并二分类器
		System.out.println("-----now output combined predictions----------"+cModel.getIdentifyName()+" (merged with："+nModel.getIdentifyName()+")");
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

		MergeClassifyResults merge=new MergeClassifyResults(ARFF_FORMAT.m_policy_group);
		GeneralInstances mergedOutput=merge.mergeResults(cInstances,nInstances,ArffFormat.RESULT_PREDICTED_WIN_RATE,left);
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(mergedOutput);
		mergedOutput=instanceProcessor.removeAttribs(mergedOutput, new String[]{ArffFormat.IS_POSITIVE,ArffFormat.SHOUYILV}); // 去掉空的收益率或positive字段
		String savedCFileName=PREDICT_RESULT_DIR+ "FullModel Selected Result-"+cFullModel.getIdentifyName()+"-"+FormatUtility.getDateStringFor(1)+".csv";
		DataIOHandler.getSaver().saveCSVFile(mergedOutput, savedCFileName);
		return savedCFileName;
	}




	private static String getLeftArffFileName(AbstractModel clModel){
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
	private GeneralInstances predictWithDB(AbstractModel clModel) throws Exception {
		int dataFormat=clModel.getModelArffFormat();
	
		GeneralInstances dailyData = null;
		//试着从缓存里加载
		String cacheKey=String.valueOf(dataFormat);
		dailyData=this.cached_daily_data.get(cacheKey);
		
		if(dailyData==null){ //缓存中没有，需要从数据库里加载
			switch (dataFormat){
			case ArffFormat.LEGACY_FORMAT:
				dailyData=DataIOHandler.getSuppier().LoadDataFromDB(dataFormat,ARFF_FORMAT);
				break;
			case ArffFormat.CURRENT_FORMAT:
				dailyData=DataIOHandler.getSuppier().LoadDataFromDB(dataFormat,ARFF_FORMAT);
				break;
			case FullModelDataFormat.FULLMODEL_FORMAT:
				dailyData = DataIOHandler.getSuppier().LoadFullModelDataFromDB((FullModelDataFormat)ARFF_FORMAT);
				break;			
			default:
				throw new Exception("invalid arffFormat type");
			}
			//保留DAILY RESULT的LEFT部分在磁盘上，主要为了保存股票代码
			GeneralInstances left = new DataInstances(dailyData);
			BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(left);
			left=instanceProcessor.filterAttribs(dailyData, ARFF_FORMAT.m_daily_predict_left_part);
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
//			//决定是否删除申万行业数据
//			if (clModel.m_removeSWData==true){
//				dailyData=ArffFormat.removeSWData(dailyData);
//				System.out.println("removed SW Data based on model definition. now column="+ dailyData.numAttributes());
//			}
			//将结果放入缓存
			this.cached_daily_data.put(cacheKey, dailyData);
		}
		
		GeneralInstances result=predict(clModel,  dailyData);

		return result;
	}


	//用模型预测数据

	private  GeneralInstances predict(AbstractModel clModel, GeneralInstances inData) throws Exception {
		System.out.println("predict using classifier : "+clModel.getIdentifyName()+" @ prediction work path :"+EnvConstants.PREDICT_WORK_DIR);
		System.out.println("-----------------------------");
		
		GeneralInstances newData = null;
		GeneralInstances result = null;

		//创建存储评估结果的数据容器
		ClassifySummaries modelSummaries=new ClassifySummaries(clModel.getIdentifyName()+" format="+clModel.modelArffFormat,true);
		clModel.setClassifySummaries(modelSummaries);

		GeneralInstances fullData=calibrateAttributesForDailyData(inData,clModel);

//		//如果模型需要计算字段，则把计算字段加上---20170109取消
//		if (clModel.m_noCaculationAttrib==false){
////			fullData=ArffFormat.addCalculateAttribute(fullData);
//			throw new RuntimeException("we don't support caculated attributes since 20170109");
//		}


		//获得”均线策略"的位置属性, 如果数据集内没有“均线策略”（短线策略的fullmodel），MaIndex为-1
		int maIndex=BaseInstanceProcessor.findATTPosition(fullData,ARFF_FORMAT.m_policy_group);

//		------预测数据中不需要这个了
//		if (clModel instanceof NominalModel ){
//			fullData=((NominalModel)clModel).processDataForNominalClassifier(fullData,false);
//		}

		//获取预定义的model文件
		String id=clModel.getIdentifyName()+clModel.modelArffFormat;
		PredictModelData modelData=this.PREDICT_MODELS.get(id);
//		int formatType=modelData.getModelFormatType();
		String evalTargetSplitYear=modelData.getTargetYearSplit(); 
		String predictPath=getPredictPath(clModel);
		String evalPredefined=modelData.getEvalFileName();
		
		//分策略组预测
		for (int j = 0; j < clModel.m_policySubGroup.length; j++) {
			String policy=clModel.m_policySubGroup[j];
			System.out.println("start to load data for policy : "	+ policy);
			String expression=null;
			if (maIndex>0){// 均线策略、动量策略
				expression=WekaInstanceProcessor.WEKA_ATT_PREFIX+ maIndex+" = "+ policy ;
				BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(fullData);
				newData = instanceProcessor.getInstancesSubset(fullData, expression);
			}else{ //短线策略（fullmodel)				
				newData=fullData;
			}

				
			String evalFileName =  evalPredefined + policy+EvaluationStore.THRESHOLD_EXTENSION;
			EvaluationStore evaluation=new EvaluationStore(clModel,predictPath,evalFileName,evalTargetSplitYear,policy);
			clModel.setEvaluationStore(evaluation);



			System.out.println(" new data size , row : "+ newData.numInstances() + " column: "	+ newData.numAttributes());
			if (result == null) {// initialize result instances
				// remove unnecessary data,leave 均线策略 & code alone
				GeneralInstances header = new DataInstances(newData, 0);
				BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(header);
				result=instanceProcessor.filterAttribs(header, ARFF_FORMAT.m_daily_predict_left_part);

				if (clModel instanceof NominalModel ){
					result = instanceProcessor.AddAttribute(result, ArffFormat.RESULT_PREDICTED_WIN_RATE,
							result.numAttributes());
				}else{
					result = instanceProcessor.AddAttribute(result, ArffFormat.RESULT_PREDICTED_PROFIT,
							result.numAttributes());
				}
				result = instanceProcessor.AddAttribute(result, ArffFormat.RESULT_SELECTED,
						result.numAttributes());

			}

			//尝试获取保存的本月预测数据统计值
			String modelID=clModel.getIdentifyName();
			
			String predictStatusFile=predictPath+DailyPredict.concatStatusFileName(policy,modelID);
			
			ModelPredictor predictor;
 			//初始化ModelPredictor，如果文件不存在则新建status对象，否则从文件里读取    
			File file =new File(predictStatusFile);   
			if  (file.exists()) {       
				PredictStatus predictorStatus=DailyPredict.loadPredictStatusFromFile(predictStatusFile, modelID,policy);
				predictor=new ModelPredictor(predictorStatus);
			}else{
				predictor=new ModelPredictor(modelID,"",policy);
			}
			 
			predictor.predictData(clModel,newData,result,"",clModel.m_policySubGroup[j]);
			System.out.println("accumulated predicted rows: "+ result.numInstances());
			System.out.println("complete for : "+ clModel.m_policySubGroup[j]);
			
		}
		System.out.println("本次预测所使用目录： "+predictPath);
		System.out.println("本次预测所使用评估文件： "+evalPredefined);

		if (result.numInstances()!=inData.numInstances()) {
			throw new Exception("not all data have been processed!!!!! incoming Data number = " +inData.numInstances() + " while predicted number is "+result.numInstances());
		}

		return result;
	}


	/**
	 * @param clModel
	 * @param formatType
	 * @return
	 */
	private static String getPredictPath(AbstractModel clModel) {
//		switch (modelFormatType) {
//		case ArffFormat.LEGACY_FORMAT:
//			return "\\00-legacy\\";
//		case ArffFormat.CURRENT_FORMAT:
//			return "";
//		case FullModelDataFormat.FULLMODEL_FORMAT:
//			return "";
//		default:
//			return "";
//		}
		//legacy与current model 合并到统一目录下，用文件名区分
		return PREDICT_MODEL_DIR+"\\"+clModel.getIdentifyName()+"\\";
	}


	
	//这是对增量数据nominal label的处理 （因为增量数据中的nominal数据，label会可能不全）
	private  GeneralInstances calibrateAttributesForDailyData(GeneralInstances incomingData,AbstractModel clModel) throws Exception {
		int formatType=clModel.getModelArffFormat();
		//与本地格式数据比较，这地方基本上会有nominal数据的label不一致，临时处理办法就是先替换掉
		GeneralInstances dataFormat = getDailyPredictDataFormat(formatType);
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(dataFormat);
		dataFormat=instanceProcessor.removeAttribs(dataFormat, ""+ArffFormat.YEAR_MONTH_INDEX);

		instanceProcessor=InstanceHandler.getHandler(incomingData);
		
		//需要根据格式的定义，做相应的数据转换（比如转换Nominal为Numberic）
		GeneralInstances output=instanceProcessor.calibrateAttributes(incomingData, dataFormat,ARFF_FORMAT.convertNominalToNumeric); 
		return output;
	}


	/**
	 * @param format
	 */
	private void addModelData(String classifier,int format,String evalFilePrefix,String lastYearSplit) {
		String id;
		PredictModelData modelData;
		id=classifier+format;
		modelData=new PredictModelData();
		modelData.setIdentify(id);
		modelData.setEvalFileName(evalFilePrefix);
		modelData.setModelFormatType(format);
		modelData.setTargetYearSplit(lastYearSplit);
		this.PREDICT_MODELS.put(id, modelData);
	}


	/**
	 * @param formatType
	 * @return
	 * @throws Exception
	 */
	protected  GeneralInstances getDailyPredictDataFormat(int formatType)
			throws Exception {
		String formatFile=null;
		switch (formatType) {
		case ArffFormat.LEGACY_FORMAT: //可以使用同一个Format文件，只是需要将无关字段去掉
			formatFile=ARFF_FORMAT.getDailyFormatFileName(false);
			break;
		case ArffFormat.CURRENT_FORMAT:
			formatFile=ARFF_FORMAT.getDailyFormatFileName(true);
			break;
		case FullModelDataFormat.FULLMODEL_FORMAT:
			//TODO 这个地方要修改
			formatFile=((FullModelDataFormat)ARFF_FORMAT).m_data_file_prefix+"("+FullModelDataFormat.FULLMODEL_FORMAT+")-format.arff";
			break;			
		default:
			throw new Exception("invalid arffFormat type");
		}

		GeneralInstances outputData=DataIOHandler.getSuppier().loadDataFromFile(EnvConstants.PREDICT_WORK_DIR+formatFile); //C_ROOT_DIRECTORY+

		return outputData;
	}

	
	/*
	 * Predict Status 文件的命名规则
	 */
	public static String concatStatusFileName(String policySplit, String modelID) {
		return PredictStatus.FILE_PREFIX + "-" + modelID +  "-" + policySplit+".sav";
	}


	/*
	 * 每日预测时使用
	 * 从文件中反序列化数据
	 */
	public static PredictStatus loadPredictStatusFromFile(String filename,String a_modelID, String a_policy,String tradeDate) throws Exception {
		@SuppressWarnings("unchecked")
		ArrayList<PredictStatus> statusList = (ArrayList<PredictStatus>) SerializationHelper.read(filename);
		if (statusList.size()!=2){
			System.err.println("请注意：Status文件中保存的对象数不为2");
		}
		
		if (status.verifyStatusData(a_modelID, a_policy)==false){
			throw new Exception("Serialized predictStatus file data mismatch");
		}
		
		return statusList;
	}
	
	/*
	 * 每日预测时使用
	 * 因为每日预测会重复运行（目前至少晚上和凌晨各一次），为了保障重复运行时统计数据不变化，需要保留最近两日的数据
	 * 保存下来当前的status至文件
	 */
	public void saveToFile(String filename,ArrayList<PredictStatus> statusList) throws Exception {
		if (statusList.size()!=2){
			System.err.println("请注意：Status文件中保存的对象数不为2");
		}
		SerializationHelper.write(filename, statusList);
		StringBuffer statusListBuffer=new StringBuffer();
		for (PredictStatus predictStatus : statusList) {
			statusListBuffer.append(predictStatus.toTXTString());
			statusListBuffer.append("\r\n");
		}
		FileUtility.write(filename + ModelStore.TXT_EXTENSION, statusListBuffer.toString(), "utf-8");
		System.out.println("predict status saved to :"+ filename);
	}

}
