package yueyueGo;

import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.evaluation.ThresholdCurve;
import yueyueGo.databeans.DataInstances;
import yueyueGo.databeans.GeneralAttribute;
import yueyueGo.databeans.GeneralDataTag;
import yueyueGo.databeans.GeneralInstance;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaInstances;
import yueyueGo.utility.ClassifyUtility;
import yueyueGo.utility.EvaluationBenchmark;
import yueyueGo.utility.EvaluationConfDefinition;
import yueyueGo.utility.EvaluationParams;
import yueyueGo.utility.FormatUtility;
import yueyueGo.utility.NumericThresholdCurve;
import yueyueGo.utility.ThresholdData;

public class EvaluationStore {
	protected String m_evalFileName;
	protected String m_evalYearSplit;
	protected String m_targetYearSplit;
	protected String m_policySplit;
	protected String[] m_modelFilesToEval;
	protected String m_modelFilepathPrefix;
	protected boolean m_isNominal=false;
	protected double[] m_focusAreaRatio={0.05,1};//评估时关注评估数据的不同Top 比例（缺省为0.01、0.03、0.05、0.1、0.2、0.5、1);
	
	public static final int PREVIOUS_MODELS_NUM=5; 	//暂时选取之前的5个文件
	public static final int YEAR_SPLIT_LIMIT=2007; //回测模型的起始点， 在这之前无数据	
	
	//以下为不可配置参数，内部存储
	public EvaluationConfDefinition m_evalConf; //用于评估的对象
	
	
	public static final String THRESHOLD_EXTENSION = ".eval";
	//切分构建模型和评估数据的模式常量定义
	public static final int NO_SEPERATE_DATA_FOR_EVAL=0; //不需要评估数据（这是legacy的做法，目前不常用）
	public static final int USE_YEAR_DATA_FOR_EVAL=12; //使用倒推一年的数据作为模型评估数据，之前用于的构建模型（缺省值）
	public static final int USE_HALF_YEAR_DATA_FOR_EVAL=6;//使用倒推半年的数据作为模型评估数据，之前用于的构建模型
	public static final int USE_NINE_MONTHS_DATA_FOR_EVAL=9;//使用倒推半年的数据作为模型评估数据，之前用于的构建模型

	public void setEvalFileName(String evalFileName) {
		this.m_evalFileName = evalFileName;
	}

	public String getPolicySplit() {
		return m_policySplit;
	}

	public String getEvalFileName() {
		return m_evalFileName;
	}

	public String[] getModelFilesToEval() {
		return m_modelFilesToEval;
	}


	public String getModelFilepathPrefix() {
		return m_modelFilepathPrefix;
	}

	public String getTargetYearSplit() {
		return m_targetYearSplit;
	}
	
	public String getEvalYearSplit() {
		return m_evalYearSplit;
	}
	
	//预测时调用的
	//TODO 需要加上指定ModelFile的方法
	public EvaluationStore(String eval_filename,String modelFileName) {
		this.m_evalFileName=eval_filename;

		//预测时不校验，将这些都设为设为null
		this.m_evalYearSplit=null;
		this.m_targetYearSplit=null;
	}

	//回测时调用的，设置model文件和eval文件名称
	public  EvaluationStore(String targetYearSplit,String policySplit,String modelFilepathPrefix, BaseClassifier clModel){
		String workFileFullPrefix=modelFilepathPrefix;
		String classifierName=clModel.classifierName;
		int modelFileShareMode=clModel.m_modelFileShareMode;
		int evalDataSplitMode=clModel.m_evalDataSplitMode;
		
		//根据modelDataSplitMode推算出评估数据的起始区间 （目前主要有三种： 最近6个月、9个月、12个月）
		String evalYearSplit=caculateEvalYearSplit(targetYearSplit,evalDataSplitMode);
		
		if ( clModel instanceof NominalClassifier){
			this.m_isNominal=true; //记录评估的目标类型（是否为二分类）
		}
		this.m_targetYearSplit=targetYearSplit; //记录下用于评测的目标月份，以便日后校验
		this.m_evalYearSplit=evalYearSplit;//记录下用于评估的起始月份，以便校验输入的数据
		this.m_modelFilepathPrefix=modelFilepathPrefix; //记录下回测模型的目录路径，以便日后使用
		this.m_policySplit=policySplit; //记录下策略分类
		
		this.m_evalFileName=ModelStore.concatModeFilenameString(evalYearSplit, policySplit, workFileFullPrefix, classifierName)+EvaluationStore.THRESHOLD_EXTENSION;
		this.m_modelFilesToEval=findModelFilesToEvaluate(modelFileShareMode,evalYearSplit,  policySplit, workFileFullPrefix, classifierName);
		
		EvaluationConfDefinition evalConf=new EvaluationConfDefinition(classifierName,clModel.m_policySubGroup,null);
		this.m_evalConf=evalConf;
	}
	

	/*
	 * 根据当前评估数据的年份，倒推取N个历史模型用于比较
	 */
	protected String[] findModelFilesToEvaluate(int modelFileShareMode,String evalYearSplit,String policySplit,String workFileFullPrefix,String classifierName){
		 
		String[] modelYears=new String[PREVIOUS_MODELS_NUM];
		
		int numberofValidModels=0;
		//根据modelYear的Share情况，向前查找N个模型的年份。
		String startYear=evalYearSplit;
		int currentYearSplit=0;
		
		//尝试获得有效的前PREVIOUS_MODELS_NUM个用于评估的ModelYearSplit
		for (int i=0;i<PREVIOUS_MODELS_NUM;i++){
			modelYears[i]=ModelStore.caculateModelYearSplit(startYear,modelFileShareMode);
			if (modelYears[i].length()==6){ //201708格式
				currentYearSplit=Integer.valueOf(modelYears[i]).intValue();
			}else{// 2017格式
				currentYearSplit=Integer.valueOf(modelYears[i]).intValue()*100+1;
			}
			if (currentYearSplit<=YEAR_SPLIT_LIMIT*100+1){
				//这里把200701之前的去重				
				continue;
			}else{
				numberofValidModels++;
				startYear=getNMonthsForYearSplit(1, modelYears[i]); //向前推一个月循环找前面的模型
			}
			
		}
		//获得所有需要评估的模型文件列表
		String[] modelFiles=new String[numberofValidModels];
		for (int i=0;i<numberofValidModels;i++){
			modelYears[i]=ModelStore.legacyModelName(modelYears[i]); //TODO
			modelFiles[i]=ModelStore.concatModeFilenameString( modelYears[i], policySplit, workFileFullPrefix, classifierName);
		}
		return modelFiles;
		
	}
	
	/*
	 * 校验评估阶段准备用于Evaluating的data是否符合要求
	 * 返回null的时候表示符合要求
	 */	
	public String validateEvalData(GeneralDataTag dataTag){
		String msg="";
		if (dataTag.getDataType()!=GeneralDataTag.EVALUATION_DATA){
			msg+=" incoming dataType is not evaluation data! ";
		}
		if (this.m_evalYearSplit.equals(dataTag.getFromPeriod())==false){
			msg+=" incoming data FromPeriod="+dataTag.getFromPeriod()+" while expected m_m_evalYearSplit="+this.m_evalYearSplit;
		}
		
		if (this.m_targetYearSplit.equals(dataTag.getToPeriod())==false){
			msg+=" incoming data toPeriod="+dataTag.getToPeriod()+" while expected m_targetYearSplit="+this.m_targetYearSplit;
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
		if (m_targetYearSplit==null && m_evalYearSplit==null){// && m_modelYearSplit==null ){ //每日预测时跳过

		}else {
			if (m_targetYearSplit.equals(thresholdData.getTargetYearSplit())==false){
				msg+=" {ERROR}target m_targetYearSplit="+m_targetYearSplit+" while targetYearSplit in thresholdData is "+thresholdData.getTargetYearSplit();
			}
			if (m_evalYearSplit.equals(thresholdData.getEvalYearSplit())==false){
				msg+=" {ERROR}target m_evalYearSplit="+m_evalYearSplit+" while evalYearSplit in thresholdData is "+thresholdData.getEvalYearSplit();
			}
		}
		if ("".equals(msg))
			return null;
		else
			return msg;
	}
	

	/**
	 * 选择最大AUC的MODEL
	 * @param evalData
	 * @throws Exception
	 * @throws RuntimeException
	 */
	public ModelStore selectModelByAUC( GeneralInstances evalData)
			throws Exception, RuntimeException {
		String yearSplit=this.getTargetYearSplit();
		double[] modelsAUC=new double[m_modelFilesToEval.length];
		ModelStore[] modelStores=new ModelStore[m_modelFilesToEval.length];
		double maxModelAUC=0;
		int maxModelIndex=0;
		
		for (int i=0;i<m_modelFilesToEval.length;i++) {
			modelStores[i]=ModelStore.loadModelFromFile(m_modelFilesToEval[i], yearSplit);
			Classifier model =modelStores[i].getModel();
			GeneralInstances header =modelStores[i].getModelFormat();
			GeneralInstances evalFormat=new DataInstances(evalData,0);
			//验证评估数据格式是否一致
			String verify=BaseClassifier.verifyDataFormat(evalFormat, header);
			if (verify!=null){
				throw new Exception("attention! model and evaluation data structure is not the same. Here is the difference: "+verify);
			}
			
			/*
			 * 用ROC的方法评价模型质量
			 * 在实际问题域中，我们并不关心整体样本的ROC curve，我们只关心预测值排序在头部区间内的ROC表现（top前N%）
			 */
			ArrayList<Prediction> fullPredictions=ClassifyUtility.getEvalPreditions(evalData, model);
	

			ArrayList<Prediction> topPedictions;
			GeneralInstances result=null;
	
			topPedictions=ClassifyUtility.getTopPredictedValues(m_isNominal,fullPredictions,0.05);
			result=getROCInstances(topPedictions);
			modelsAUC[i]=ThresholdCurve.getROCArea( WekaInstances.convertToWekaInstances(result));
			System.out.println("thread:"+Thread.currentThread().getName()+" modelsAUC="+modelsAUC[i]+ " where modelFile="+m_modelFilesToEval[i]);
			if (modelsAUC[i]>maxModelAUC){
				maxModelAUC=modelsAUC[i];
				maxModelIndex=i;
			}
	
		}
		System.out.println("thread:"+Thread.currentThread().getName()+" MaxAUC selected="+maxModelAUC+"@"+m_modelFilesToEval[maxModelIndex]);
		if (maxModelIndex!=0){
			System.out.println("thread:"+Thread.currentThread().getName()+ " MaxAUC selected is not the latest one for TargetYearSplit("+yearSplit+") ModelYearSplit used="+modelStores[maxModelIndex].getModelYearSplit());
		}
		if (maxModelAUC<0.5){
			System.err.println(" MaxAUC selected is less than random classifer. MAXAUC="+maxModelAUC);
		}
		return modelStores[maxModelIndex];
	}

	//	当前周期前推N个月的分隔线，比如 如果N=9是201003 则返回200909
	public static String getNMonthsForYearSplit(int nMonths,String yearSplit){

		int lastPeriod=0;
		if (yearSplit.length()==4){ //最后一位-1 （2010-1=2009）再拼接一个12-nMonth+1
			lastPeriod=Integer.valueOf(yearSplit).intValue();
			lastPeriod=lastPeriod-1;
			if (lastPeriod<YEAR_SPLIT_LIMIT) {
				lastPeriod=YEAR_SPLIT_LIMIT;
			}
			lastPeriod=lastPeriod*100+12-nMonths+1;
		}else {//最后两位数（n）大于nMonths的话减nMonths，小于等于的话向年借位12
			int inputYear=Integer.parseInt(yearSplit.substring(0,4)); //输入的年份
			int inputMonth=Integer.parseInt(yearSplit.substring(4,6)); //输入的月份
			if (inputMonth>nMonths){
				inputMonth=inputMonth-nMonths;
			}else{
				inputMonth=12+inputMonth-nMonths;
				inputYear=inputYear-1;
			}
			lastPeriod=inputYear*100+inputMonth;
			if (lastPeriod<YEAR_SPLIT_LIMIT*100+1){ 
				lastPeriod=YEAR_SPLIT_LIMIT*100+1;
			}
		}
		return String.valueOf(lastPeriod);
	}

	/*
	 * 获取用于评估阀值的yearSplit（目前主要有三种： 最近6个月、9个月、12个月）
	 * 全量历史数据会分解为模型构建数据及其评估数据
	 * 比如，若是一年区间， 则取最近的1年间数据作为评估数据用。之前的数据再去getModelYearSplit看选多少作为模型构建数据（因为不是每月都有模型）
	 * 如果是2010.mdl，则取2009年01月之前的数据build，2009当年数据做评估用
	 */
	public static String caculateEvalYearSplit(String targetYearSplit,int evalDataSplitMode){
		//找到回测创建评估预测时应该使用modelStore对象（主要为获取model文件和eval文件名称）-- 评估时创建的mdl并不是当前年份的，而是前推一年的
		String evalYearSplit=null;
		switch (evalDataSplitMode) {
		case EvaluationStore.NO_SEPERATE_DATA_FOR_EVAL: //使用全量数据构建模型（不常用）
			evalYearSplit=targetYearSplit; 
			break;
		case EvaluationStore.USE_YEAR_DATA_FOR_EVAL:
		case EvaluationStore.USE_HALF_YEAR_DATA_FOR_EVAL:
		case EvaluationStore.USE_NINE_MONTHS_DATA_FOR_EVAL:
			evalYearSplit=EvaluationStore.getNMonthsForYearSplit(evalDataSplitMode,targetYearSplit);			
			break;
		}
		System.out.println("目标日期="+targetYearSplit+" 评估数据切分日期="+evalYearSplit+"（评估数据切分模式="+evalDataSplitMode+"）");
		return evalYearSplit;
	}
	
	/**
	 * 获取ROC的Instances
	 * @param predictions
	 * @param isNominal 是否是二分类变量
	 * @param classIndex 二分类器变量时目标CLass数值的下标（一般为1）
	 * @return
	 * @throws Exception
	 */
	protected GeneralInstances getROCInstances(ArrayList<Prediction> predictions)
			throws Exception {
		if (m_isNominal){
			ThresholdCurve tc = new ThresholdCurve();
			int classIndex = 1;
			GeneralInstances result = new DataInstances(tc.getCurve(predictions, classIndex));
			return result;
		}else{
			NumericThresholdCurve tc = new NumericThresholdCurve();
			GeneralInstances result = new DataInstances(tc.getCurve(predictions));
			return result;
		}
	}

	private ThresholdData computeDefaultThresholds(EvaluationParams evalParams, GeneralInstances result){
		double sample_limit=evalParams.getLower_limit(); 
		double sampleSize=1.0;  //SampleSize应该是倒序下来的
		double lastSampleSize=1.0;
		double threshold=-100;
		GeneralAttribute att_threshold = result.attribute(NumericThresholdCurve.THRESHOLD_NAME);
		GeneralAttribute att_samplesize = result.attribute(NumericThresholdCurve.SAMPLE_SIZE_NAME);
	
		for (int i = 0; i < result.numInstances(); i++) {
			GeneralInstance curr = result.instance(i);
			lastSampleSize=sampleSize;
			sampleSize = curr.value(att_samplesize); // to get sample range
			if (FormatUtility.compareDouble(sampleSize,sample_limit)==0) {
				threshold = curr.value(att_threshold);
				break;
			}
			//暂存转折点
			if ( lastSampleSize< sample_limit && sampleSize>sample_limit || lastSampleSize>sample_limit && sampleSize<sample_limit){
				threshold=curr.value(att_threshold);
				System.out.println("cannot get threshold at sample_limit="+sample_limit+ " use nearest SampleSize between"+sampleSize +" and "+lastSampleSize);
			}
			
		}
		if (threshold==-100){
			System.err.println("fatal error!!!!! cannot get threshold at sample_limit="+sample_limit);
		}else {
			System.err.println("got default threshold "+ threshold+" at sample_limit="+sample_limit);
		}
		ThresholdData thresholdData=new ThresholdData();
		thresholdData.setThresholdMin(threshold);
		double startPercent=100*(1-evalParams.getLower_limit()); //将sampleSize转换为percent
		thresholdData.setStartPercent(startPercent);
		//先将模型阀值上限设为100，以后找到合适的算法再计算。
		thresholdData.setThresholdMax(100);
		//先将模型end percent设为100，以后找到合适的算法再计算。
		thresholdData.setEndPercent(100);
		
		//使用缺省值时设置此标志位
		thresholdData.setIsGuessed(true);
	
		return thresholdData;
		
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
		GeneralAttribute att_tp = result.attribute(NumericThresholdCurve.TRUE_POS_NAME);
		GeneralAttribute att_fp = result.attribute(NumericThresholdCurve.FALSE_POS_NAME);
	//		GeneralAttribute att_lift = result.attribute(NumericThresholdCurve.LIFT_NAME);
		GeneralAttribute att_threshold = result.attribute(NumericThresholdCurve.THRESHOLD_NAME);
		GeneralAttribute att_samplesize = result.attribute(NumericThresholdCurve.SAMPLE_SIZE_NAME);
	
	
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
			thresholdData.setThresholdMin(thresholdBottom);
			double startPercent=100*(1-finalSampleSize); //将sampleSize转换为percent
			thresholdData.setStartPercent(startPercent);
			//先将模型阀值上限设为100，以后找到合适的算法再计算。
			thresholdData.setThresholdMax(100);
			//先将模型end percent设为100，以后找到合适的算法再计算。
			thresholdData.setEndPercent(100);
	
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

	public ThresholdData doModelEvaluation( GeneralInstances evalData, Classifier model)
			throws Exception {
		
		/*
		 * 用ROC的方法评价模型质量
		 * 在实际问题域中，我们并不关心整体样本的ROC curve，我们只关心预测值排序在头部区间内的ROC表现（top前N%）
		 */
		ArrayList<Prediction> fullPredictions=ClassifyUtility.getEvalPreditions(evalData, model);
		
		double[] modelAUC=new double[m_focusAreaRatio.length];

		ArrayList<Prediction> topPedictions;
		GeneralInstances result=null;
		for (int i=0;i<m_focusAreaRatio.length;i++){
			topPedictions=ClassifyUtility.getTopPredictedValues(m_isNominal,fullPredictions,m_focusAreaRatio[i]);
			result=getROCInstances(topPedictions);
			modelAUC[i]=ThresholdCurve.getROCArea( WekaInstances.convertToWekaInstances(result));
			System.out.println("thread:"+Thread.currentThread().getName()+" MoDELAUC="+modelAUC[i]+ " where focusAreaRatio="+m_focusAreaRatio[i]);
		}
	
	
		ThresholdData thresholdData=null;
	
		EvaluationBenchmark benchmark=new EvaluationBenchmark(evalData, m_isNominal);		 
		EvaluationParams evalParams=m_evalConf.getEvaluationInstance(m_policySplit);
		
		int round=1;
		double tp_fp_bottom_line=benchmark.getEval_tp_fp_ratio();  
		System.out.println("use the tp_fp_bottom_line based on training history data = "+tp_fp_bottom_line);
		double trying_tp_fp=benchmark.getEval_tp_fp_ratio()*evalParams.getLift_up_target();
		System.out.println("start from the trying_tp_fp based on training history data = "+trying_tp_fp + " / while  lift up target="+evalParams.getLift_up_target());
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
		
		//将focusAreaRatio及对应的ModelAUC保存
		thresholdData.setFocosAreaRatio(m_focusAreaRatio);
		thresholdData.setModelAUC(modelAUC);
	
		return thresholdData;
	}
	
	/*
	 * 输出当前的评估阀值定义
	 */
	public String showEvaluationParameters(){
		String result=null;
		result=m_evalConf.showEvaluationParameters();
		return result;
	}
}
