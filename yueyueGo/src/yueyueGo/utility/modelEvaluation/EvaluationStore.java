package yueyueGo.utility.modelEvaluation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.NumericPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.SerializationHelper;
import yueyueGo.AbstractModel;
import yueyueGo.NominalModel;
import yueyueGo.databeans.DataInstances;
import yueyueGo.databeans.GeneralAttribute;
import yueyueGo.databeans.GeneralDataTag;
import yueyueGo.databeans.GeneralInstance;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaInstances;
import yueyueGo.datasource.DataIOHandler;
import yueyueGo.datasource.GeneralDataSaver;
import yueyueGo.utility.ClassifyUtility;
import yueyueGo.utility.FileUtility;
import yueyueGo.utility.FormatUtility;
import yueyueGo.utility.TpFpStatistics;
import yueyueGo.utility.YearMonthProcessor;

public class EvaluationStore {
	protected String m_evalFileName;
	protected String m_evalYearSplit;
	protected String m_targetYearSplit;
	protected String m_policySplit;
	protected String m_classifierName;
	protected String m_workFilePath;
	protected String m_filePrefix;
	
	protected int m_modelFileShareMode;
	protected int m_evalDataSplitMode;

	protected boolean m_isNominal=false;

	protected double[] m_focusAreaRatio={EvaluationConfDefinition.TOP_AREA_RATIO,1};//评估时关注评估数据的不同Top 比例;

	public static final int YEAR_SPLIT_LIMIT=2007; //回测模型的起始点， 在这之前无数据	

	//以下为不可配置参数，内部存储
	public EvaluationConfDefinition m_evalConf; //用于评估的对象


	public static final String THRESHOLD_EXTENSION = ".eval";
	//切分构建模型和评估数据的模式常量定义
	public static final int NO_SEPERATE_DATA_FOR_EVAL=0; //不需要评估数据（这是legacy的做法，目前不常用）
	public static final int USE_YEAR_DATA_FOR_EVAL=12; //使用倒推一年的数据作为模型评估数据，之前用于的构建模型（缺省值）
	public static final int USE_HALF_YEAR_DATA_FOR_EVAL=6;//使用倒推半年的数据作为模型评估数据，之前用于的构建模型
	public static final int USE_NINE_MONTHS_DATA_FOR_EVAL=9;//使用倒推半年的数据作为模型评估数据，之前用于的构建模型

	public String getWorkFilePath() {
		return m_workFilePath;
	}

	public void setEvalFileName(String evalFileName) {
		this.m_evalFileName = evalFileName;
	}

	public String getPolicySplit() {
		return m_policySplit;
	}

	public String getEvalFileName() {
		return m_evalFileName;
	}


	public String getTargetYearSplit() {
		return m_targetYearSplit;
	}

	public String getEvalYearSplit() {
		return m_evalYearSplit;
	}

	//预测时调用的
	public EvaluationStore(AbstractModel clModel,String workpath,String eval_filename,String a_targetYearSplit, String policySplit) {
		this.m_workFilePath=workpath;
		this.m_evalFileName=eval_filename;
		//每日预测时用最后的训练数据月做校验
		this.m_targetYearSplit=a_targetYearSplit; 
		
		//根据modelDataSplitMode推算出评估数据的起始区间 （目前主要有三种： 最近6个月、9个月、12个月）
		this.m_modelFileShareMode=clModel.m_modelFileShareMode;
		this.m_evalDataSplitMode=clModel.m_evalDataSplitMode;
		this.m_policySplit=policySplit;
		m_evalYearSplit=YearMonthProcessor.caculateEvalYearSplit(m_targetYearSplit,m_evalDataSplitMode);
	}

	//回测时调用的，设置model文件和eval文件名称
	public  EvaluationStore(String targetYearSplit,String policySplit,String modelFilePath, String modelFilePrefix, AbstractModel clModel){
		
		
		this.m_modelFileShareMode=clModel.m_modelFileShareMode;
		this.m_evalDataSplitMode=clModel.m_evalDataSplitMode;

		//根据modelDataSplitMode推算出评估数据的起始区间 （目前主要有三种： 最近6个月、9个月、12个月）
		String evalYearSplit=YearMonthProcessor.caculateEvalYearSplit(targetYearSplit,m_evalDataSplitMode);

		if ( clModel instanceof NominalModel){
			this.m_isNominal=true; //记录评估的目标类型（是否为二分类）
		}
		this.m_targetYearSplit=targetYearSplit; //记录下用于评测的目标月份，以便日后校验
		this.m_evalYearSplit=evalYearSplit;//记录下用于评估的起始月份，以便校验输入的数据
		this.m_workFilePath=modelFilePath; //记录下回测模型的目录路径，以便日后使用
		this.m_filePrefix=modelFilePrefix;//记录下回测模型的文件头，以便日后使用
		this.m_policySplit=policySplit; //记录下策略分类
		this.m_classifierName=clModel.classifierName;
		
		// 这里的fileName用TargetYearSplit来做，而不是evalYearSplit来做
		this.m_evalFileName=EvaluationStore.concatFileName(m_filePrefix,m_targetYearSplit, m_policySplit,  m_classifierName)+EvaluationStore.THRESHOLD_EXTENSION;
		

		//TODO ArffFormat 不能这么设NULL
		EvaluationConfDefinition evalConf=new EvaluationConfDefinition(m_classifierName,clModel.m_policySubGroup,null);
		this.m_evalConf=evalConf;
	}


	/*
	 * 校验评估阶段准备用于Evaluating的data是否符合要求
	 * 返回null的时候表示符合要求
	 */	
	public String validateEvalData(GeneralDataTag dataTag,int skipRecentNMonthForEval){
		String msg="";
		if (dataTag.getDataType()!=GeneralDataTag.EVALUATION_DATA){
			msg+=" incoming dataType is not evaluation data! ";
		}
		if (this.m_evalYearSplit.equals(dataTag.getFromPeriod())==false){
			msg+=" incoming data FromPeriod="+dataTag.getFromPeriod()+" while expected m_m_evalYearSplit="+this.m_evalYearSplit;
		}

		String evalEndYearSplit=YearMonthProcessor.backNMonthsForYearSplit(skipRecentNMonthForEval, m_targetYearSplit);
		if (evalEndYearSplit.equals(dataTag.getToPeriod())==false){
			msg+=" incoming data toPeriod="+dataTag.getToPeriod()+" while expected m_targetYearSplit="+this.m_targetYearSplit +" skipRecentNMonthForEval="+skipRecentNMonthForEval;
		}
		if ("".equals(msg))
			return null;
		else
			return msg;
	}

	/*
	 * 校验回测阶段准备用于Testing的data是否符合要求
	 * 返回null的时候表示符合要求
	 */	
	public String validateTestingData(GeneralDataTag dataTag){
		String msg="";
		if (dataTag.getDataType()!=GeneralDataTag.TESTING_DATA){
			msg+=" incoming dataType is not testing data! ";
		}
		if (this.m_targetYearSplit.equals(dataTag.getToPeriod())==false){
			msg+=" incoming data toPeriod="+dataTag.getToPeriod()+" while expected targetYearSplit="+this.m_targetYearSplit;
		}
		if ("".equals(msg))
			return null;
		else
			return msg;
	}

	/*
	 * 校验从文件中读取的Threshold值是否可以用于当前的模型
	 */
	public String validateThresholdData(ThresholdData thresholdData){
		String msg="";
		//不仅回测时，每日预测时也要做这个校验
		if (m_targetYearSplit.equals(thresholdData.getTargetYearSplit())==false){
			msg+=" {ERROR}target m_targetYearSplit="+m_targetYearSplit+" while targetYearSplit in thresholdData is "+thresholdData.getTargetYearSplit();
		}
		if (m_evalYearSplit.equals(thresholdData.getEvalYearSplit())==false){
			msg+=" {ERROR}target m_evalYearSplit="+m_evalYearSplit+" while evalYearSplit in thresholdData is "+thresholdData.getEvalYearSplit();
		}
		if (m_policySplit.equals(thresholdData.getPolicySplit())==false){
			msg+=" {ERROR}target m_policySplit="+m_policySplit+" while getPolicySplit in thresholdData is "+thresholdData.getPolicySplit();
		}
		if ("".equals(msg))
			return null;
		else
			return msg;
	}


	/**
	 * @param evalData
	 * @throws Exception
	 * @throws RuntimeException
	 */
	public void evaluateModels(GeneralInstances evalData) throws Exception, RuntimeException {
	
		/*
		  用ROC的方法比较不同模型的质量，选择表现最好的那个模型
		 在实际问题域中，我们并不关心整体样本的ROC curve，我们只关心预测值排序在头部区间内的ROC表现（top前N%）
		 */
		ModelStore[] modelStores=findModelsToEvaluate();
		ModelStore selectedModel=selectModelByAUC(modelStores,evalData,false);

		
		//确定好模型后，先查正向评估
		ArrayList<Prediction> fullPredictions=ClassifyUtility.getEvalPreditions(evalData, selectedModel.getModel());
		double[] modelAUC=new double[m_focusAreaRatio.length];
		//获取确定模型在不同阈值下的表现，目前这个只是作为打印输出使用
		for (int i=0;i<m_focusAreaRatio.length;i++){
			GeneralInstances result=getROCInstances(fullPredictions,m_focusAreaRatio[i],false);
			modelAUC[i]=caculateAUC(result);
			System.out.println("thread:"+Thread.currentThread().getName()+" MoDELAUC="+modelAUC[i]+ " @ focusAreaRatio="+m_focusAreaRatio[i]);
		}

		//以下过程为获取正向评估的Threshold
		ThresholdData thresholdData=null;

		//获取正向全部的评估结果（要全部的原因是评估的sample_rate是占全部数据的rate）
		GeneralInstances result=getROCInstances(fullPredictions,1,false);


		//TODO ：利用EvalData数据统计bottomLine还是固定值0.6？
		TpFpStatistics benchmark=new TpFpStatistics(evalData, m_isNominal);	
		double tp_fp_bottom_line=benchmark.getEval_tp_fp_ratio();
		if (tp_fp_bottom_line<0.4){ //too small
			tp_fp_bottom_line=0.4;
		}
		EvaluationParams evalParams=m_evalConf.getEvaluationInstance(m_policySplit);
		thresholdData=doModelEvaluation(result,evalParams,tp_fp_bottom_line);

		//将focusAreaRatio及对应的ModelAUC保存
		thresholdData.setFocosAreaRatio(m_focusAreaRatio);
		thresholdData.setModelAUC(modelAUC);

		//将相应的数据区段值存入评估数据文件中，以备日后校验
		thresholdData.setTargetYearSplit(getTargetYearSplit());
		thresholdData.setEvalYearSplit(getEvalYearSplit());
		thresholdData.setPolicySplit(getPolicySplit());
		thresholdData.setModelYearSplit(selectedModel.getModelYearSplit());
		thresholdData.setModelFileName(selectedModel.getModelFileName());

		//再查反向评估
		ModelStore reversedModel=selectModelByAUC(modelStores,evalData,true);
		//获取反向评估结果
		fullPredictions=ClassifyUtility.getEvalPreditions(evalData, reversedModel.getModel());

		//获取反向全部的评估结果（要全部的原因是评估的sample_rate是占全部数据的rate）
		GeneralInstances reversedResult=getROCInstances(fullPredictions,1,true);

		//测试用，暂存文件
//		outputFilesForDebug(selectedModel, fullPredictions, result, reversedModel, reversedResult);

		
		EvaluationParams reversedEvalParams=new EvaluationParams(EvaluationConfDefinition.REVERSED_TOP_AREA_RATIO, EvaluationConfDefinition.REVERSED_TOP_AREA_RATIO*1.1, 1.2);
		ThresholdData reversedThresholdData=doModelEvaluation(reversedResult,reversedEvalParams,1/tp_fp_bottom_line);

		//将反向评估结果的阈值恢复取反前的值
		double reversedThreshold;
		if (m_isNominal){
			reversedThreshold=1-reversedThresholdData.getThreshold();
		}else{
			reversedThreshold=reversedThresholdData.getThreshold()*-1;
		}
		if (reversedThreshold>thresholdData.getThreshold()){
			String reversedModelYear=reversedModel.getModelYearSplit();
			String selectdModelYear=selectedModel.getModelYearSplit();
			if (reversedModelYear.equals(selectdModelYear)){
				throw new Exception("thread:"+Thread.currentThread().getName()+"fatal error!!! reversedThreshold("+reversedThreshold+") > threshold("+thresholdData.getThreshold()+") using same model (modelyear="+reversedModelYear+")" );
			}else{
				System.out.println("thread:"+Thread.currentThread().getName()+"使用不同模型时反向阀值大于正向阀值了"+"reversedThreshold("+reversedThreshold+")@"+reversedModelYear + " > threshold("+thresholdData.getThreshold()+")@"+selectdModelYear);
			}
		}
		double reversedPercentile=100-reversedThresholdData.getPercent();
		//将反向评估结果存入数据中
		thresholdData.setReversedThreshold(reversedThreshold);
		thresholdData.setReversedPercent(reversedPercentile);
		thresholdData.setReversedModelYearSplit(reversedModel.getModelYearSplit());
		thresholdData.setReversedModelFileName(reversedModel.getModelFileName());


		//保存包含正向和反向的ThresholdData到数据文件中
		this.saveEvaluationToFile(thresholdData);
		System.out.println(thresholdData.toString());
		

	}

	/**
	 * 测试输出用
	 * @param selectedModel
	 * @param fullPredictions
	 * @param result
	 * @param reversedModel
	 * @param reversedResult
	 * @throws IOException
	 */
protected void outputFilesForDebug(ModelStore selectedModel, ArrayList<Prediction> fullPredictions,
			GeneralInstances result, ModelStore reversedModel, GeneralInstances reversedResult) throws IOException {
		GeneralDataSaver dataSaver=DataIOHandler.getSaver();
		String filePrefix=m_workFilePath+this.m_targetYearSplit+"["+this.m_policySplit+"]-";
		dataSaver.SaveDataIntoFile(result, filePrefix+selectedModel.getModelYearSplit()+"-ROC.arff");
		dataSaver.SaveDataIntoFile(reversedResult, filePrefix+reversedModel.getModelYearSplit()+"-ROC.reversed.arff");
		StringBuffer predictionString=new StringBuffer();
		if (m_isNominal==true){
			for (Iterator<Prediction> iterator = fullPredictions.iterator(); iterator.hasNext();) {
				NominalPrediction prediction = (NominalPrediction) iterator.next();
				predictionString.append(prediction.toString());
				predictionString.append("\r\n");
			}
			FileUtility.write(filePrefix+"-full predictions", predictionString.toString(), "utf-8");
		}
	}

	private ThresholdData doModelEvaluation( GeneralInstances result,EvaluationParams evalParams,double tp_fp_bottom_line)
			throws Exception {
		ThresholdData thresholdData=null;

		int round=1;
		//		System.out.println("use the tp_fp_bottom_line based on training history data = "+tp_fp_bottom_line);
		double trying_tp_fp=tp_fp_bottom_line*evalParams.getLift_up_target();
		System.out.println("thread:"+Thread.currentThread().getName()+"start from the trying_tp_fp = "+trying_tp_fp + " / while  lift up target="+evalParams.getLift_up_target());
		while (thresholdData == null && trying_tp_fp > tp_fp_bottom_line){
			thresholdData= computeThresholds(trying_tp_fp,evalParams, result);
			if (thresholdData!=null){
				System.out.println(" threshold got at trying round No.: "+round);
				break;
			}else {
				trying_tp_fp=trying_tp_fp*0.95;
				round++;
			}
		}// end while;
		if (thresholdData==null){  //如果已达到TPFP_BOTTOM_LINE但无法找到合适的阀值
			thresholdData=computeDefaultThresholds(evalParams,result);//设置下限
		}

		return thresholdData;
	}

	/*
	 * 根据当前评估数据的年份，倒推取N个历史模型用于比较
	 */
	private ModelStore[] findModelsToEvaluate(){

		ArrayList<String> modelYears= new ArrayList<String>();//new String[PREVIOUS_MODELS_NUM];

		
		//根据modelYear的Share情况，向前查找N个模型的年份。
		String startYear=m_evalYearSplit;
		int currentYearSplit=0;

		//尝试获得有效的前PREVIOUS_MODELS_NUM个用于评估的ModelYearSplit
		
		for (int i=0;i<EvaluationConfDefinition.PREVIOUS_MODELS_NUM;i++){
			String modelYearSplit=YearMonthProcessor.caculateModelYearSplit(startYear,this.m_modelFileShareMode);
			if (modelYearSplit.length()==6){ //201708格式
				currentYearSplit=Integer.valueOf(modelYearSplit).intValue();
			}else{// 2017格式
				currentYearSplit=Integer.valueOf(modelYearSplit).intValue()*100+1;
			}
			if (currentYearSplit<=YEAR_SPLIT_LIMIT*100+1){
				//这里把200701之前的去重				
				continue;
			}else{
				modelYears.add(modelYearSplit);
				startYear=YearMonthProcessor.backNMonthsForYearSplit(1, modelYearSplit); //向前推一个月循环找前面的模型
			}

		}
		
		//获得所有需要评估的模型文件列表及模型年份年份
		int numberofValidModels=modelYears.size();
		ModelStore[] modelStores=new ModelStore[numberofValidModels];
		for (int i=0;i<numberofValidModels;i++){
			String modelFile=EvaluationStore.concatFileName(m_filePrefix, modelYears.get(i), m_policySplit, m_classifierName);
			modelStores[i]=new ModelStore(m_workFilePath, modelFile,modelYears.get(i));
		}
		return modelStores;

	}

	/**
	 * 
	 * 	用ROC的方法比较不同模型的质量，选择表现最好的那个模型（即选择最大AUC的MODEL）
	 *  在实际问题域中，我们并不关心整体样本的ROC curve，我们只关心预测值排序在头部区间内的ROC表现（top前N%）
	 *  isReversed 参数是选取反向的最好表现 （即对0值有最准确顶部预测的模型）
	 * @throws Exception
	 */
	private ModelStore selectModelByAUC(ModelStore[] modelStores, GeneralInstances evalData,boolean isReversed)
			throws Exception{

		String yearSplit=this.getTargetYearSplit();
		double[] modelsAUC=new double[modelStores.length];
		double maxModelAUC=0;
		int maxModelIndex=0;


		for (int i=0;i<modelStores.length;i++) {
//			modelStores[i].loadModelFromFile(yearSplit);
			Classifier model =modelStores[i].loadModelFromFile(evalData, yearSplit); 
					
//			GeneralInstances header =modelStores[i].getModelFormat();
//			GeneralInstances evalFormat=new DataInstances(evalData,0);
//			//验证评估数据格式是否一致
//			String verify=BaseClassifier.verifyDataFormat(evalFormat, header);
//			if (verify!=null){
//				throw new Exception("attention! model and evaluation data structure is not the same. Here is the difference: "+verify);
//			}

			ArrayList<Prediction> fullPredictions=ClassifyUtility.getEvalPreditions(evalData, model);

			//根据reversed与否决定是取正向还是反向的AUC
			double ratio;
			if (isReversed==false){
				ratio=EvaluationConfDefinition.TOP_AREA_RATIO;
			}else{
				ratio=EvaluationConfDefinition.REVERSED_TOP_AREA_RATIO;
			}
			/*
			 * 用ROC的方法评价模型质量
			 * 在实际问题域中，我们并不关心整体样本的ROC curve，我们只关心预测值排序在头部区间内的ROC表现（top前N%）
			 */
			GeneralInstances result=getROCInstances(fullPredictions,ratio,isReversed);
			modelsAUC[i]=caculateAUC(result);
			String msg="thread:"+Thread.currentThread().getName()+" modelsAUC="+modelsAUC[i]+ " @"+modelStores[i].getModelYearSplit()+" policy["+ m_policySplit+"]";
			if (isReversed){
				msg+=" [reversed]";
			}
			System.out.println(msg);

			//不管正向还是反向，都是取最大的AUC
			if (modelsAUC[i]>maxModelAUC){
				maxModelAUC=modelsAUC[i];
				maxModelIndex=i;
			}

		}
		String outMsg="thread:"+Thread.currentThread().getName()+" MaxAUC selected="+maxModelAUC+" @"+modelStores[maxModelIndex].getModelYearSplit()+" policy["+ m_policySplit+"]";
		if (isReversed){
			outMsg+=" [reversed]";
		}
		
		System.out.println(outMsg);
		if (maxModelIndex!=0){
			System.out.println("thread:"+Thread.currentThread().getName()+ " MaxAUC selected is not the latest one for TargetYearSplit("+yearSplit+") ModelYearSplit used="+modelStores[maxModelIndex].getModelYearSplit()+" policy["+ m_policySplit+"]");
		}
		if (maxModelAUC<0.5 ){
			System.err.println("thread:"+Thread.currentThread().getName()+" MaxAUC selected is less than random classifer. MAXAUC="+maxModelAUC+" isReversed="+isReversed);
		}
		return modelStores[maxModelIndex];
	}

	/**
		
	 * @throws Exception
	 */
	private double caculateAUC(GeneralInstances result) throws Exception{
		double auc=ThresholdCurve.getROCArea( WekaInstances.convertToWekaInstances(result));

		return auc;
	}

	private ThresholdData computeThresholds(double tp_fp_ratio, EvaluationParams evalParams, GeneralInstances result) {
	
		double sample_limit=evalParams.getLower_limit(); 
		double sample_upper=evalParams.getUpper_limit();
	
		double thresholdBottom = 0.0;
		//		double lift_max = 0.0;
		//		double lift_max_tp=0.0;
		//		double lift_max_fp=0.0;
		//		double lift_max_sample=0.0;
	
		double finalSampleSize = 0.0;
		double sampleSize = 0.0;
		double tp = 0.0;
		double fp = 0.0;
		double final_tp=0.0;
		double final_fp=0.0;
		GeneralAttribute att_tp = result.attribute(ThresholdCurve.TRUE_POS_NAME);
		GeneralAttribute att_fp = result.attribute(ThresholdCurve.FALSE_POS_NAME);
		//		GeneralAttribute att_lift = result.attribute(ThresholdCurve.LIFT_NAME);
		GeneralAttribute att_threshold = result.attribute(ThresholdCurve.THRESHOLD_NAME);
		GeneralAttribute att_samplesize = result.attribute(ThresholdCurve.SAMPLE_SIZE_NAME);
	
	
		for (int i = 0; i < result.numInstances(); i++) {
			GeneralInstance curr = result.instance(i);
			sampleSize = curr.value(att_samplesize); // to get sample range
			if (sampleSize >= sample_limit && sampleSize <=sample_upper) {
				tp = curr.value(att_tp);
				fp = curr.value(att_fp);
	
				//统计该范围内lift最大的值是多少（仅为输出用）
				//				double current_lift=curr.value(att_lift);
				//				if (current_lift>lift_max){
				//					lift_max=current_lift;
				//					lift_max_tp=tp;
				//					lift_max_fp=fp;
				//					lift_max_sample=sampleSize;
				//				}
	
				//查找合适的阀值
				if (tp>fp*tp_fp_ratio ){
					thresholdBottom = curr.value(att_threshold);
					finalSampleSize = sampleSize;
					final_tp=tp;
					final_fp=fp;
				}
			}
		}
	
	
		ThresholdData thresholdData=null;
		if (thresholdBottom>0){ //找到阀值时输出并设置对象的值
			System.out.print("#############################thresholdBottom is : " + FormatUtility.formatDouble(thresholdBottom));
			System.out.print("/samplesize is : " + FormatUtility.formatPercent(finalSampleSize) );
			System.out.print("/True Positives is : " + final_tp);
			System.out.println("/False Positives is : " + final_fp);
	
			thresholdData=new ThresholdData();
			thresholdData.setThreshold(thresholdBottom);
			double percentile=100*(1-finalSampleSize); //将sampleSize转换为percent
			thresholdData.setPercent(percentile);
	
	
		}else{
			//			double max_tp_fp_ratio=Double.NaN;
			//			if (lift_max_fp>0){
			//				max_tp_fp_ratio=lift_max_tp/lift_max_fp;
			//			}
			//			System.out.println("###possible lift max in range is : " + FormatUtility.formatDouble(lift_max) + "@ sample="+FormatUtility.formatDouble(lift_max_sample)+" where tp="+lift_max_tp+" /fp="+lift_max_fp);
			//			System.out.println("### max tp fp ratio="+max_tp_fp_ratio+ " while trying threshold="+tp_fp_ratio+ " isNormal="+(max_tp_fp_ratio<tp_fp_ratio));
		}
	
		return thresholdData;
	}

	private ThresholdData computeDefaultThresholds(EvaluationParams evalParams, GeneralInstances result) throws Exception{
		double sample_limit=evalParams.getLower_limit(); 
		double sampleSize=1.0;  //SampleSize应该是倒序下来的
		double lastSampleSize=1.0;
		double threshold=-100;
		GeneralAttribute att_threshold = result.attribute(ThresholdCurve.THRESHOLD_NAME);
		GeneralAttribute att_samplesize = result.attribute(ThresholdCurve.SAMPLE_SIZE_NAME);
	
		for (int i = 0; i < result.numInstances(); i++) {
			GeneralInstance curr = result.instance(i);
			lastSampleSize=sampleSize;
			sampleSize = curr.value(att_samplesize); // to get sample range
			//以五位精度比较double（ROC里是取到6位小数的）
			if (FormatUtility.compareDouble(sampleSize,sample_limit,-0.00001,0.00001)==0) {
//			if (sampleSize==sample_limit){
				threshold = curr.value(att_threshold);
				break;
			}
			//暂存转折点
			if ( lastSampleSize< sample_limit && sampleSize>sample_limit || lastSampleSize>sample_limit && sampleSize<sample_limit){
				threshold=curr.value(att_threshold);
				System.out.println("thread:"+Thread.currentThread().getName()+"cannot get threshold at sample_limit="+sample_limit+ " use nearest SampleSize between "+FormatUtility.formatDouble(sampleSize,1,6) +" and "+FormatUtility.formatDouble(lastSampleSize,1,6));
				break;
			}
	
		}
		if (threshold==-100){
			throw new Exception("thread:"+Thread.currentThread().getName()+"fatal error!!!!! cannot get threshold at sample_limit="+sample_limit);
		}else {
			System.err.println("thread:"+Thread.currentThread().getName()+"got default threshold "+ threshold+" at sample_limit="+sample_limit +" actual sampleSize="+FormatUtility.formatDouble(sampleSize,1,6));
		}
		ThresholdData thresholdData=new ThresholdData();
		thresholdData.setThreshold(threshold);
		double startPercent=100*(1-evalParams.getLower_limit()); //将sampleSize转换为percent
		thresholdData.setPercent(startPercent);
	
	
		//使用缺省值时设置此标志位
		thresholdData.setIsGuessed(true);
	
		return thresholdData;
	
	}

	

//	/*
//	 * 输出当前的评估的数据
//	 */
//	public String showEvaluationParameters(){
//		String result=null;
//		
//		result+=m_evalConf.showEvaluationParameters();
//		return result;
//	}

	/**
	 * 获取ROC的Instances
	 * 根据reverse的值，取fullPreditions的TOP ratio（reverse=false)数据  
	 * 或bottom ratio(reverse=true) 数据
	 * @param predictions
	 * @param isReversed 根据是否反转来决定目标CLass数值的下标取值
	 * @return
	 * @throws Exception
	 */
	public static GeneralInstances getROCInstances(ArrayList<Prediction> fullPredictions,double ratio,boolean isReversed)
			throws Exception {
		//先根据ratio截取预测的数据范围
		ArrayList<Prediction> topPedictions=getTopPredictedValues(fullPredictions,ratio,isReversed);
		ThresholdCurve tc = new ThresholdCurve();
		int classIndex = NominalModel.CLASS_POSITIVE_INDEX;
		if (isReversed){
			classIndex=NominalModel.CLASS_NEGATIVE_INDEX;
		}
		GeneralInstances result = new DataInstances(tc.getCurve(topPedictions, classIndex));
		return result;
	
	}

	/*
	 * 正分布选为1最大的ratio，负分布选为0的最大ratio
	 * 特别地，对连续分类变量，重新构建一个Nominal的预测列表，以利于绘制ROC图线
	 */
	public static ArrayList<Prediction> getTopPredictedValues(ArrayList<Prediction> predictions,double ratio,boolean reverse) {

		//先判断是否是连续变量
		boolean isNominalPred=true;
		if ( predictions.get(0) instanceof NumericPrediction) {
			isNominalPred=false;
		}
		//如果是二分类变量，且需要取全部值则直接返回全量数据
		if (ratio==1 && isNominalPred==true){
			return predictions;
		}
		
		//否则，需要进行数据处理
		DescriptiveStatistics probs=new DescriptiveStatistics();
		double predicted=0.0;
		int targetClassIndex=NominalModel.CLASS_POSITIVE_INDEX;
		if (reverse==true){
			targetClassIndex=NominalModel.CLASS_NEGATIVE_INDEX;
		}

		ArrayList<Prediction> convertedPrections; 
		if (isNominalPred){
			convertedPrections=predictions;
		}else{
			//对于连续分类变量，需要构建一个Nominal的预测为绘制ROC处理 （将预测值作为Postive的可能性，将预测值取反设为Negative的可能性）
			convertedPrections=new ArrayList<Prediction>(predictions.size());
		}
		
		//第一次遍历，加入所有的数据，查找分界点
		for (int i = 0; i < predictions.size(); i++) {
			Prediction pred =  predictions.get(i);

			if (isNominalPred){
				predicted=((NominalPrediction)pred).distribution()[targetClassIndex];				
			}else{
				//为连续型变量构建Nominal的预测表
				double[] distribution=new double[2];
				double actual;
				//根据连续变量的实际值大于0与否，构建预测实际值的分类
				if (pred.actual()>0){
					actual=NominalModel.CLASS_POSITIVE_INDEX;
				}else{
					actual=NominalModel.CLASS_NEGATIVE_INDEX;
				}
				distribution[NominalModel.CLASS_NEGATIVE_INDEX]=pred.predicted()*-1; //简单取反
				distribution[NominalModel.CLASS_POSITIVE_INDEX]=pred.predicted();
				
				NominalPrediction nominalPrediction=new NominalPrediction(actual, distribution);
				convertedPrections.add(nominalPrediction);
				//获取刚刚构建的预测分类
				predicted=nominalPrediction.distribution()[targetClassIndex];
			}
			
			probs.addValue(predicted);
		}

		double targetPercentile=(1-ratio)*100;
		//如果是全部则返回全量数据
		if (targetPercentile==0){
			return convertedPrections;
		}else{
			double judgePoint=probs.getPercentile(targetPercentile);		

			// 第二次遍历，根据阈值截取数据
			ArrayList<Prediction> topPredictions=new ArrayList<Prediction>();
			for (int i = 0; i < convertedPrections.size(); i++) {
				Prediction pred =  convertedPrections.get(i);
				//对于二分类变量，根据targetClassIndex来使用目标分类的预测可能性
				predicted=((NominalPrediction)pred).distribution()[targetClassIndex];
				if (predicted>=judgePoint) {
					topPredictions.add(pred);
				}
			}
			String msg="thread:"+Thread.currentThread().getName()+"rows selected="+topPredictions.size()+" from total ("+convertedPrections.size()+") where predicted > threshold("+FormatUtility.formatDouble(judgePoint,2,6)+") and top ratio="+ratio;
			if (reverse){
				msg+=" [reversed]";
			}
			System.out.println(msg);
			return topPredictions;
		}
	}

	public void saveEvaluationToFile(ThresholdData thresholdData) throws Exception {
			String evalFileName=m_workFilePath+m_evalFileName;
			SerializationHelper.write( evalFileName, thresholdData);
			FileUtility.write(evalFileName+ModelStore.TXT_EXTENSION, thresholdData.toString(), "utf-8");
	//		System.out.println("evaluation saved to :"+ evalFileName);
		}

	public ThresholdData loadDataFromFile() throws Exception{
		String evalFileName=m_workFilePath+m_evalFileName;
		//读取Threshold数据文件
		ThresholdData thresholdData=(ThresholdData)SerializationHelper.read( evalFileName);
		return thresholdData;
	}

	public static String concatFileName(String filePrefix, String yearSplit,String policySplit, String classifierName){//BaseClassifier classifier) {
		return filePrefix +"-"+classifierName+ "-" + yearSplit + ModelStore.MA_PREFIX + policySplit;
	}
}
