package yueyueGo;

import java.io.File;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import yueyueGo.databeans.DataInstances;
import yueyueGo.databeans.GeneralAttribute;
import yueyueGo.databeans.GeneralInstance;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.datasource.DataIOHandler;
import yueyueGo.utility.AppContext;
import yueyueGo.utility.ClassifySummaries;
import yueyueGo.utility.ClassifyUtility;
import yueyueGo.utility.FileUtility;
import yueyueGo.utility.FormatUtility;
import yueyueGo.utility.modelEvaluation.EvaluationStore;
import yueyueGo.utility.modelEvaluation.ModelStore;
import yueyueGo.utility.modelPredict.ModelPredictor;
import yueyueGo.utility.modelPredict.PredictModelData;
import yueyueGo.utility.modelPredict.PredictStatus;
import yueyueGo.utility.modelPredict.ResultsHolder;

public class DailyPredict {

	private static String PREDICT_MODEL_DIR=EnvConstants.PREDICT_WORK_DIR+"\\66-模型\\";
	protected ArffFormat ARFF_FORMAT; //当前所用数据文件格式 
	private static String PREDICT_RESULT_DIR=EnvConstants.PREDICT_WORK_DIR+"\\88-预测结果\\"; 
	private HashMap<String, PredictModelData> PREDICT_MODELS;
	private Date m_tradeDate=null;
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
		addModelData(classifierName,format,"\\trans20052017(10)-baggingM5P-201709 MA ","201709");

		//adaboost当前使用的预测模型
		classifierName=ClassifyUtility.ADABOOST;
		addModelData(classifierName,format,"\\trans20052017(10)-adaboost-201709 MA ","201709");


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
//			copyPredictModels();

			callDailyPredict();
			

		} catch (Exception e) {

			e.printStackTrace();
		}
	}
	
	/*
	 * 从回测模型中选取最新的模型拷贝至目标目录
	 */
	public static void copyPredictModels() throws Exception {
		String currentMonth="201709"; //这是评估有的最新数据的月份
		HashMap<String, String> fileMap;
		BackTest worker=new BackTest();
		worker.init();
		
		//BaggingM5P
		BaggingM5P cBagModel=BaggingM5P.initModel(new AvgLineDataFormat(), AbstractModel.FOR_DAILY_PREDICT);
		//Adaboost
		AdaboostClassifier adaModel=AdaboostClassifier.initModel(new AvgLineDataFormat(), AbstractModel.FOR_DAILY_PREDICT);
		AbstractModel[] models=new AbstractModel[2];
		models[0]=cBagModel;
		models[1]=adaModel;

		int fileNum=0;
		for (AbstractModel model : models) {
			fileMap=worker.findModelFiles(model, currentMonth);
			String targetPath= PREDICT_MODEL_DIR+"\\new copied\\"+model.getIdentifyName()+"\\";  // getPredictPath(model)+"\\new copied\\";
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
		
		
		//新格式的bagging m5p预测  (使用PCA版本和计算字段）
		BaggingM5P cBagModel=BaggingM5P.initModel(this.ARFF_FORMAT, AbstractModel.FOR_DAILY_PREDICT);
		ResultsHolder  baggingInstances=predictWithDB(cBagModel);

		//Adaboost(使用PCA版本和计算字段）
		AdaboostClassifier adaModel=AdaboostClassifier.initModel(this.ARFF_FORMAT, AbstractModel.FOR_DAILY_PREDICT);
		ResultsHolder  adaboostInstances=predictWithDB(adaModel);		

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
	 * @param cResultHolder 连续分类器结果
	 * @param nModel 二分类器（预测胜率）
	 * @param nResultHolder 二分类器结果
	 * @throws Exception
	 * return 合并的文件名称（暂时返回连续分类器的值）
	 */
	public String combinePreditions(AbstractModel cModel,
			ResultsHolder cResultHolder, AbstractModel nModel,
			ResultsHolder nResultHolder) throws Exception {
		
		
		//以二分类器为主，合并连续分类器
		System.out.println("");
		System.out.println("-----now output combined predictions----------"+nModel.getIdentifyName()+" (merged with："+cModel.getIdentifyName()+")");
		GeneralInstances left=DataIOHandler.getSuppier().loadDataFromFile(getLeftArffFileName(nModel)); //获取刚生成的左侧文件（主要存了CODE）
		
		GeneralInstances nMergedOutput=nResultHolder.mergeResults(cResultHolder,left);
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(nMergedOutput);
		nMergedOutput=instanceProcessor.removeAttribs(nMergedOutput, new String[]{ArffFormat.IS_POSITIVE,ArffFormat.SHOUYILV}); // 去掉空的收益率或positive字段
		String savedNFileName=PREDICT_RESULT_DIR+ "Merged Selected Result-"+nModel.getIdentifyName()+"-"+FormatUtility.getDateStringFor(1)+".csv";
		DataIOHandler.getSaver().saveCSVFile(nMergedOutput, savedNFileName);
		nMergedOutput=null;
		System.out.println(nModel.getIdentifyName()+"----------prediction ends---------");
		//以连续分类器为主，合并二分类器
		System.out.println("-----now output combined predictions----------"+cModel.getIdentifyName()+" (merged with："+nModel.getIdentifyName()+")");
		GeneralInstances cMergedOutput=cResultHolder.mergeResults(nResultHolder,left);
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

		return null;
//		//BaggingM5P
//		BaggingM5PFullModel cFullModel=new BaggingM5PFullModel();
//		GeneralInstances cInstances=predictWithDB(cFullModel);		
//
//		//BaggingJ48
////		BaggingJ48FullModel nFullModel=new BaggingJ48FullModel();
//		MyNNFullModel nFullModel= new MyNNFullModel(); 
//		GeneralInstances nInstances=predictWithDB(nFullModel);		
//
//		cFullModel.outputClassifySummary();
//		nFullModel.outputClassifySummary();
//
//		//合并baggingJ48和baggingM5P
//		System.out.println("-----now output combined predictions----------"+cFullModel.getIdentifyName());
//		GeneralInstances left=DataIOHandler.getSuppier().loadDataFromFile(getLeftArffFileName(cFullModel)); //获取刚生成的左侧文件（主要存了CODE）
//
//		MergeClassifyResults merge=new MergeClassifyResults(ARFF_FORMAT.m_policy_group);
//		GeneralInstances mergedOutput=merge.mergeResults(cInstances,nInstances,ResultsHolder.RESULT_PREDICTED_WIN_RATE,left);
//		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(mergedOutput);
//		mergedOutput=instanceProcessor.removeAttribs(mergedOutput, new String[]{ArffFormat.IS_POSITIVE,ArffFormat.SHOUYILV}); // 去掉空的收益率或positive字段
//		String savedCFileName=PREDICT_RESULT_DIR+ "FullModel Selected Result-"+cFullModel.getIdentifyName()+"-"+FormatUtility.getDateStringFor(1)+".csv";
//		DataIOHandler.getSaver().saveCSVFile(mergedOutput, savedCFileName);
//		return savedCFileName;
	}




	private static String getLeftArffFileName(AbstractModel clModel){
		return PREDICT_RESULT_DIR+"LEFT ("+clModel.getModelArffFormat()+") "+FormatUtility.getDateStringFor(1)+".arff";
	}


	//直接访问数据库预测每天的自选股数据，不单独保存每个模型的选股
	private ResultsHolder predictWithDB(AbstractModel clModel) throws Exception {
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
			
			if (dailyData.numInstances()>0){ //如果当日有数据则处理数据
				//从dailyData中获取tradeDate并校验
				ArrayList<Date> datelist=getTradeDateList(dailyData);
				if (datelist.size()!=1){
					System.err.println("Warning!! tradeDate is not unique in daily data!!!");
				}
				m_tradeDate=datelist.get(0);			
				//校验一下tradeDate应该不能小于当前日期
				Date today=FormatUtility.getCurrentDate();
				if (m_tradeDate.compareTo(today)<0) {
					throw new Exception("ERROR!!! tradeDate in daily data(" +m_tradeDate+") < currentDate:"+today);
				}


				//保留DAILY RESULT的LEFT部分在磁盘上，主要为了获取股票代码，同时其他字段用于预测后的校验用
				GeneralInstances left = new DataInstances(dailyData);
				BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(left);
				left=instanceProcessor.filterAttribs(dailyData, ARFF_FORMAT.m_result_left_part);
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

				//在送入模型之前，去掉多读入的CODE部分
				instanceProcessor=InstanceHandler.getHandler(dailyData);
				dailyData=instanceProcessor.removeAttribs(dailyData, new String[]{ArffFormat.CODE});

				//将结果放入缓存
				this.cached_daily_data.put(cacheKey, dailyData);
			}
		}
		

		//调用预测方法，此函数如果当日预测数据为空，会返回一个空的对象
		ResultsHolder result=predict(clModel,dailyData,m_tradeDate);
		return result;
	}


	/*
	 * 用给定的模型预测当日数据
	 */
	private  ResultsHolder  predict(AbstractModel clModel, GeneralInstances inData, Date tradeDate) throws Exception {
		System.out.println("predict using classifier : "+clModel.getIdentifyName()+" @ prediction work path :"+EnvConstants.PREDICT_WORK_DIR);
		System.out.println("-----------------------------");
		



		//创建存储评估结果的数据容器
		ClassifySummaries modelSummaries=new ClassifySummaries(clModel.getIdentifyName()+" format="+clModel.modelArffFormat,true);
		clModel.setClassifySummaries(modelSummaries);

		GeneralInstances fullData=calibrateAttributesForDailyData(inData,clModel);


		//结果文件
		ResultsHolder result=new ResultsHolder(clModel, fullData,ARFF_FORMAT);
		if (m_tradeDate==null){ //这应该仅发生当日数据为空的时候
			if (inData.numInstances()==0){
				System.err.println("there are no data to predict today");
				return result; //返回空的结果集
			}else {
				throw new Exception("tradeDate is null while there are data to predict today ");
			}
			
		}
		
		//获得”均线策略"的位置属性, 如果数据集内没有“均线策略”（短线策略的fullmodel），MaIndex为-1
		int maIndex=BaseInstanceProcessor.findATTPosition(fullData,ARFF_FORMAT.m_policy_group);


		//获取预定义的model文件
		String id=clModel.getIdentifyName()+clModel.modelArffFormat;
		PredictModelData modelData=this.PREDICT_MODELS.get(id);
		String evalTargetSplitYear=modelData.getTargetYearSplit(); 
		String predictPath=getPredictPath(clModel);
		String evalPredefined=modelData.getEvalFileName();
		
		

		GeneralInstances newData = null;
		//分策略组预测
		for (int j = 0; j < clModel.m_policySubGroup.length; j++) {
			String policy=clModel.m_policySubGroup[j];
			System.out.println("start to load data for policy : "	+ policy);
			String expression=null;
			if (maIndex>0){// 均线策略、动量策略
				expression = BackTest.appendSplitClause("", maIndex, policy);
				BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(fullData);
				newData = instanceProcessor.getInstancesSubset(fullData, expression);
			}else{ //短线策略（fullmodel)				
				newData=fullData;
			}
				
			String evalFileName =  evalPredefined + policy+EvaluationStore.THRESHOLD_EXTENSION;
			EvaluationStore evaluation=new EvaluationStore(clModel,predictPath,evalFileName,evalTargetSplitYear,policy);
			clModel.setEvaluationStore(evaluation);
			System.out.println(" new data size , row : "+ newData.numInstances() + " column: "	+ newData.numAttributes());

			//尝试获取保存的本月预测数据统计值
			String modelID=clModel.getIdentifyName();
			
			String predictStatusFile=predictPath+DailyPredict.concatStatusFileName(policy,modelID);
			
			ModelPredictor predictor;
			PredictStatus predictorStatus=null;
 			//初始化ModelPredictor，如果文件不存在则新建status对象，否则从文件里读取    
			File file =new File(predictStatusFile);   
			if  (file.exists()) {       
				predictorStatus=loadPredictStatusFromFile(predictStatusFile, modelID,policy,tradeDate);
			}
			if (predictorStatus!=null){ //如果获取到历史的status则接着它运行
				//复制一个新的status对象，因为原有的status对象也要放入List中
				PredictStatus newStatus=PredictStatus.makeCopy(predictorStatus);
				predictor=new ModelPredictor(newStatus);
			}else{ //否则就直接开始预测
				predictor=new ModelPredictor(modelID,"",policy);
			}
			
			//储存序列化的列表
			ArrayList<PredictStatus> statusList=new ArrayList<PredictStatus>();
			//将预测前的旧状态数据加入（为了重复多次运行）
			statusList.add(predictorStatus);
			//进行预测
			predictor.predictData(clModel,newData,result,"",clModel.m_policySubGroup[j]);
			//将预测后的新状态数据加入
			PredictStatus newPredictorStatus=predictor.getPredictStatus();
			//记录这一天的tradeDate
			newPredictorStatus.setTradeDate(tradeDate);
			statusList.add(newPredictorStatus);
			//序列化statusList
			savePredictStatusToFile(predictStatusFile, statusList);
			
			System.out.println("accumulated predicted rows: "+ result.getResultInstances().numInstances());
			System.out.println("complete for : "+ clModel.m_policySubGroup[j]);
			
		}
		System.out.println("本次预测所使用目录： "+predictPath);
		System.out.println("本次预测所使用评估文件： "+evalPredefined);

		if (result.getResultInstances().numInstances()!=inData.numInstances()) {
			throw new Exception("not all data have been processed!!!!! incoming Data number = " +inData.numInstances() + " while predicted number is "+result.getResultInstances().numInstances());
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
	 * 从文件中反序列化数据，取离tradeDate最近的一个，但不能等于TradeDate（因为可能重复运行）
	 * 有可能返回null的（比如第一天运行时的重复运行时）
	 */
	public static PredictStatus loadPredictStatusFromFile(String filename,String a_modelID, String a_policy,Date tradeDate) throws Exception {
		@SuppressWarnings("unchecked")
		ArrayList<PredictStatus> statusList = (ArrayList<PredictStatus>) SerializationHelper.read(filename);
		if (statusList.size()!=2){
			System.err.println("请注意：Status文件中保存的对象数不为2");
		}
		// Arraylist中是时间正序保存的，最近的TradeDate在最后面，我们的目标是选取最近的且不等于TradeDate的status返回去
		PredictStatus status=null;
		//倒序从最新的日期开始遍历
		for(int i=statusList.size()-1;i>=0;i--){
			PredictStatus predictStatus=statusList.get(i);
			if (predictStatus!=null){
				int compareResult=tradeDate.compareTo(predictStatus.getTradeDate());
				if (compareResult>0){
					status=predictStatus;
				}else if (compareResult<0){
					System.err.println("Warning!!! date in predictStatus is newer than current tradeDate");
				}
			}
		}
		if (status==null){
			System.err.println("No suitable predictStatus in serialized file, you will start over predictor");
		}else if (status.verifyStatusData(a_modelID, a_policy)==false){
			throw new Exception("Serialized predictStatus file data mismatch");
		}
		
		return status;
	}
	
	/*
	 * 每日预测时使用
	 * 因为每日预测会重复运行（目前至少晚上和凌晨各一次），为了保障重复运行时统计数据不变化，需要保留最近两日的数据
	 * 保存下来当前的status至文件
	 */
	public static void savePredictStatusToFile(String filename,ArrayList<PredictStatus> statusList) throws Exception {
		if (statusList.size()!=2){
			System.err.println("请注意：Status文件中保存的对象数不为2");
		}
		SerializationHelper.write(filename, statusList);
		StringBuffer statusListBuffer=new StringBuffer();
		
		PredictStatus predictStatus=null; 
		//倒序从最新的日期开始遍历
		for(int i=statusList.size()-1;i>=0;i--){
			predictStatus=statusList.get(i);
			if (predictStatus!=null){
				statusListBuffer.append(predictStatus.toTXTString());
				statusListBuffer.append("\r\n");
			}else {
				statusListBuffer.append("---reach the very beginning of the predict status\r\n");
			}
		}
		FileUtility.write(filename + ModelStore.TXT_EXTENSION, statusListBuffer.toString(), "utf-8");
		System.out.println("predict status saved to :"+ filename);
	}
	
	
	/**
	 * 该函数会处理两个问题
	 * 1. 将输入数据的所有日期转换为训练用的标准模式（参数的数值data会产生改变）
	 * 2. 获取输入数据中的所有日期升序排列，并单独返回LIST
	 * @param data
	 */
	private ArrayList<Date> getTradeDateList(GeneralInstances data) throws Exception{
		GeneralAttribute tradeDateAtt=data.attribute(ArffFormat.TRADE_DATE);
		ArrayList<Date> tradeDateList=new ArrayList<Date>();
		SimpleDateFormat sdFormat=new SimpleDateFormat(ArffFormat.ARFF_DATE_FORMAT);
		String current="2099/12/31";
		String next=null;
		for (int i=0;i<data.numInstances();i++){
			//获取日期
			next=data.get(i).stringValue(tradeDateAtt);
			//转换为内部标准形式
			next=convertDate(next);
			//更新回Arff数据中
			data.get(i).setValue(tradeDateAtt, next);
			//对转换后的日期数据进行处理，获取所有不同的日期，加入列表并返回
			if (current.equals(next)==false){
				current=next;
				//转换为日期以便于排序，免得将字符串中的2017/10/5 排在2017/10/21之后了
				Date d = sdFormat.parse(next);
				tradeDateList.add(d);
			}
			
		}
		Collections.sort(tradeDateList);
		return tradeDateList;
	}
	
	private static String convertDate(String tradeDate) throws ParseException{
		return FormatUtility.convertDate(tradeDate,ArffFormat.DB_DATE_FORMAT,ArffFormat.ARFF_DATE_FORMAT);
	}
}
