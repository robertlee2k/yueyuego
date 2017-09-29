package yueyueGo;


import java.io.Serializable;

import weka.classifiers.Classifier;
import weka.core.SerializedObject;
import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.databeans.GeneralDataTag;
import yueyueGo.databeans.GeneralInstance;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.utility.ClassifySummaries;
import yueyueGo.utility.FormatUtility;
import yueyueGo.utility.modelEvaluation.EvaluationConfDefinition;
import yueyueGo.utility.modelEvaluation.EvaluationStore;

/**
 * @author robert
 * 所有分类器的基类
 */
/**
 * @author robert
 * 基础通用分类器
 */
public abstract class AbstractModel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5895562408723104016L;
	
	
	
//	public final boolean m_noCaculationAttrib=true;  //加入的计算字段与否 ---拟取消
//	public final boolean m_removeSWData=true;  //是否需要删除行业数据--拟取消
	
	
	
	//各子分类器的可配置参数
	public String classifierName; //名称
    public String[] m_policySubGroup;//策略分组,在子类中赋值覆盖 = {"5","10","20","30","60" }或{""};
    public int m_modelFileShareMode; //model文件的共享模式1,3,6,12 （表示共享的月份）
    public int m_evalDataSplitMode;//切分构建模型和评估数据的模式 0、6、9、12 （表示倒推回去切分评估数据的月份）
    public int m_SkipRecentNMonthForEval=1; //为了保持和现实预测一致，需要跳过的最近N月评估数据（缺省跳过最近1个月）
    public int m_useRecentNYearForTraining=5; //用多少年的历史数据训练模型（缺省使用5年数据）
    
    public int modelArffFormat; //arff的格式
    public boolean m_normalize=false; //是否需要做normalize，缺省设为在进入分类器之前需要对数据做Normalize
    
    
	protected double m_positiveLine; // 用来定义收益率大于多少时算positive，缺省为0   
	private boolean m_skipTrainInBacktest = true; //回测中使用，是否跳过训练模型阶段
	private boolean m_skipEvalInBacktest = true;  //回测中使用，是否跳过评估模型阶段
	private boolean m_skipPredictInBacktest = true;  //回测中使用，是否跳过用模型预测阶段
	
	public boolean is_skipTrainInBacktest() {
		return m_skipTrainInBacktest;
	}

	public boolean is_skipEvalInBacktest() {
		return m_skipEvalInBacktest;
	}
	
	public boolean is_skipPredictInBacktest() {
		return m_skipPredictInBacktest;
	}


	public EvaluationStore m_evaluationStore;//eval的持久化封装类类
	public ClassifySummaries classifySummaries;//分类的统计信息

	public AbstractModel() {
		m_positiveLine=0; //缺省的以收益率正负为二分类的正负。
		modelArffFormat=ArffFormat.CURRENT_FORMAT; //缺省使用当前的arff Format
		overrideParams();		// 留给子类的覆盖初始化参数接口
	}
	

	protected void overrideParams(){
		
	}
	
	//一系列需要子类实现的抽象方法
	protected abstract Classifier buildModel(GeneralInstances trainData) throws Exception;
	
	
	public Classifier trainData(GeneralInstances train) throws Exception {
		Classifier model=buildModel(train);
		return model;
	}
	

	
	/*
	 * 评估模型
	 */
	public void evaluateModel(GeneralInstances evalData) throws Exception{

		m_evaluationStore.evaluateModels(evalData);


	}

	// 对于连续分类器， 收益率就是classvalue，缺省直接返回， 对于nominal分类器，调用子类的方法获取暂存的收益率
	protected double getShouyilv(int index,double id, double newClassValue) throws Exception{
		return newClassValue;
	}
//	
//	// 对于连续分类器，positiveLine就是0， 缺省直接返回0， 对于nominal分类器，调用子类的方法获取m_positiveLine
//	protected double getPositiveLine(){
//		return 0;
//	}

//	
//	public static String verifyDataFormat(GeneralInstances test, GeneralInstances header) throws Exception {
////		//在使用旧格式时，如果有使用旧字段名的模型，试着将其改名后使用
////		if (modelArffFormat==ArffFormat.LEGACY_FORMAT){
////			header=ArffFormat.renameOldArffName(header);
////		}
//		return BaseInstanceProcessor.compareInstancesFormat(test, header);
//	}

	
	//找到回测评估、预测时应该使用evaluationStore对象（主要为获取model文件和eval文件名称）
	public EvaluationStore locateEvalutationStore(String targetYearSplit,String policySplit,String modelFilePath, String modelFilePrefix) {
		EvaluationStore evaluationStore=new EvaluationStore(targetYearSplit,policySplit,modelFilePath, modelFilePrefix,this);
		m_evaluationStore=evaluationStore;
		return evaluationStore;
	}
	
	//为每日预测时使用
	public void setEvaluationStore(EvaluationStore m_evaluationStore) {
		this.m_evaluationStore = m_evaluationStore;
	}

	/**
	 * @param dataTag
	 * @return
	 * @see yueyueGo.utility.modelEvaluation.ModelStore#validateEvalData(yueyueGo.databeans.GeneralDataTag)
	 */
	public String validateEvalData(GeneralDataTag dataTag) {
		return m_evaluationStore.validateEvalData(dataTag,this.m_SkipRecentNMonthForEval);
	}

	/**
	 * @param dataTag
	 * @return
	 * @see yueyueGo.utility.modelEvaluation.ModelStore#validateTestingData(yueyueGo.databeans.GeneralDataTag)
	 */
	public String validateTestingData(GeneralDataTag dataTag) {
		return m_evaluationStore.validateTestingData(dataTag);
	}

	
	//缺省返回classifierName，某些子类（比如MultiPCA）可能会返回其他名字，这是为了保存文件时区分不同参数用
	public String getIdentifyName(){
		return classifierName;
	}


	/**
	 * Creates a deep copy of the given classifier using serialization.
	 *
	 * @return a deep copy of the classifier
	 * @exception Exception if an error occurs
	 */
	public static AbstractModel makeCopy(AbstractModel cl) throws Exception {

		return (AbstractModel) new SerializedObject(cl).getObject();
	}
	public int getModelArffFormat() {
		return modelArffFormat;
	}

	public void setModelArffFormat(int modelArffFormat) {
		this.modelArffFormat = modelArffFormat;
	}

	/*
	 * 输出分类器的参数设置
	 */
	private String getClassifyParametersInString() {
		StringBuffer output=new StringBuffer();
		output.append("***************************************CLASSIFY DATE="+FormatUtility.getDateStringFor(0));
		output.append("\r\n");
		output.append("ClassifyIdentity="+this.getIdentifyName());
		output.append("\r\n");
//		output.append("m_skipTrainInBacktest="+this.m_skipTrainInBacktest);
//		output.append("\r\n");
		output.append("m_skipEvalInBacktest="+this.m_skipEvalInBacktest);
		output.append("\r\n");
		output.append("m_policySubGroup={");
		for (String eachString : m_policySubGroup) {
			output.append(eachString);
			output.append("/");
		}
		output.append("}");
		output.append("\r\n");
		output.append("m_positiveLine="+m_positiveLine);
		output.append("\r\n");
		output.append("m_modelDataSplitMode="+m_evalDataSplitMode);
		output.append("\r\n");
		output.append("m_useRecentNYearForTraining="+this.m_useRecentNYearForTraining);
		output.append("\r\n");
		output.append("m_modelEvalFileShareMode="+m_modelFileShareMode);
		output.append("\r\n");
		output.append("m_SkipRecentNMonthForEval="+this.m_SkipRecentNMonthForEval);
		output.append("\r\n");
		output.append("modelArffFormat="+modelArffFormat);
		output.append("\r\n");
		output.append("compare AUC PREVIOUS_MODELS_NUM="+EvaluationStore.PREVIOUS_MODELS_NUM);
		output.append("\r\n");
		output.append("TOP AREA RATIO="+EvaluationStore.TOP_AREA_RATIO);
		output.append("\r\n");
		output.append("reversed TOP AREA RATIO="+EvaluationStore.REVERSED_TOP_AREA_RATIO);
		output.append("\r\n");
		EvaluationConfDefinition evalConf=new EvaluationConfDefinition(this.classifierName ,this.m_policySubGroup,null);
		output.append(evalConf.showEvaluationParameters());
		output.append("\r\n");
		output.append("***************************************");
		output.append("\r\n");
		return output.toString();
	}
	
	/*
	 * 输出分类器的分类结果
	 */
	public String outputClassifySummary() throws Exception{
		StringBuffer output=new StringBuffer();
		output.append(getClassifyParametersInString());
		String result=output.toString();
		System.out.println(result);
		
		ClassifySummaries c=getClassifySummaries();
		if (c!=null){
			if (c.isForPrediction()){
				c.outputPredictSummary();
			}else{
				c.outputClassifySummary();
			}
		}else{
			System.out.println("ClassifySummaries object for "+getIdentifyName()+ " is null, cannot output summary");
		}
		return result;
	}
	
	//用于清除分类器的内部缓存（如nominal 分类器的cache）
	// 注意这里仅限于清除内部的cache，外部设置的比如ClassifySummary不在此列	
	public void cleanUp(){
		
	}
	
	
	public static final int FOR_BUILD_MODEL=1;
	public static final int FOR_EVALUATE_MODEL=2;
	public static final int FOR_BACKTEST_MODEL=3;
	public static final int FOR_DAILY_PREDICT=0;

	public void initModelPurpose(int purpose){
		switch (purpose) {
		case FOR_BUILD_MODEL:
			m_skipTrainInBacktest=false;
			m_skipEvalInBacktest=true;
			m_skipPredictInBacktest=true;
			break;
		case FOR_EVALUATE_MODEL:
			m_skipTrainInBacktest=true;
			m_skipEvalInBacktest=false;
			m_skipPredictInBacktest=false;
			break;	
		case FOR_BACKTEST_MODEL:
			m_skipTrainInBacktest=true;
			m_skipEvalInBacktest=true;
			m_skipPredictInBacktest=false;			
		case FOR_DAILY_PREDICT:
			break;
		}
		
	}

	public ClassifySummaries getClassifySummaries() {
		return classifySummaries;
	}

	public void setClassifySummaries(ClassifySummaries classifySummaries) {
		this.classifySummaries = classifySummaries;
		
	}

	public void cleanUpClassifySummaries(){
		ClassifySummaries c=getClassifySummaries();
		if (c!=null){
			c.cleanUp();
			c=null;
		}
	}
	
	

}
