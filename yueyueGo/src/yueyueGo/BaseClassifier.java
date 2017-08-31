package yueyueGo;


import java.io.Serializable;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import weka.classifiers.Classifier;
import weka.core.SerializedObject;
import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.dataProcessor.InstanceHandler;
import yueyueGo.databeans.DataInstance;
import yueyueGo.databeans.DataInstances;
import yueyueGo.databeans.GeneralAttribute;
import yueyueGo.databeans.GeneralDataTag;
import yueyueGo.databeans.GeneralInstance;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.utility.ClassifySummaries;
import yueyueGo.utility.FormatUtility;
import yueyueGo.utility.ThresholdData;

/**
 * @author robert
 * 所有分类器的基类
 */
/**
 * @author robert
 * 基础通用分类器
 */
public abstract class BaseClassifier implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5895562408723104016L;
	
	public static final int VALUE_SELECTED = 1; //模型预测结果为“选择”
	public static final int VALUE_NOT_SURE = 0; //模型预测结果为“无法判断”
	public static final int VALUE_NEVER_SELECT = -1; //模型预测结果为“坚决不选”
	
//	public final boolean m_noCaculationAttrib=true;  //加入的计算字段与否 ---拟取消
//	public final boolean m_removeSWData=true;  //是否需要删除行业数据--拟取消
	
	
	
	//各子分类器的可配置参数
	public String classifierName; //名称
    public String[] m_policySubGroup;//策略分组,在子类中赋值覆盖 = {"5","10","20","30","60" }或{""};
    public int m_modelFileShareMode; //model文件的共享模式1,3,6,12 （表示共享的月份）
    public int m_evalDataSplitMode;//切分构建模型和评估数据的模式 0、6、9、12 （表示评估数据的月份）
    public int modelArffFormat; //arff的格式
	
	protected double m_positiveLine; // 用来定义收益率大于多少时算positive，缺省为0   
	private boolean m_skipTrainInBacktest = true; //回测中使用，是否跳过训练模型阶段
	private boolean m_skipEvalInBacktest = true;  //回测中使用，是否跳过评估模型阶段
	
	public boolean is_skipTrainInBacktest() {
		return m_skipTrainInBacktest;
	}

	public boolean is_skipEvalInBacktest() {
		return m_skipEvalInBacktest;
	}



	protected EvaluationStore m_evaluationStore;//eval的持久化封装类类
	protected ClassifySummaries classifySummaries;//分类的统计信息
	
	public BaseClassifier() {
		m_positiveLine=0; //缺省的以收益率正负为二分类的正负。

		modelArffFormat=ArffFormat.CURRENT_FORMAT; //缺省使用当前的arff Format
		m_modelFileShareMode=ModelStore.MONTHLY_MODEL; //model文件和Eval的共享模式,缺省为 回测时按yearsplit和policysplit分割使用model和eval文件
		m_evalDataSplitMode=EvaluationStore.USE_YEAR_DATA_FOR_EVAL;//缺省使用倒推一年的数据作为模型评估数据，之前用于的构建模型
		initializeParams();		// 留给子类的初始化参数函数
	}
	
	//一系列需要子类实现的抽象方法
	protected abstract void initializeParams();
	protected abstract Classifier buildModel(GeneralInstances trainData) throws Exception;
	protected abstract double classify(Classifier model,GeneralInstance curr) throws Exception ;
	
	
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

	//为每日预测用，这时候没有yearSplit （policySplit是存在的）
	// result parameter will be changed in this method!
	public void predictData(GeneralInstances test, GeneralInstances result,String policySplit) throws Exception {
		predictData(test, result,"",policySplit);
	}
	
	//为回测历史数据使用
	// result parameter will be changed in this method!
	public  void predictData(GeneralInstances test, GeneralInstances result,String yearSplit,String policySplit)
			throws Exception {

		//先找对应的评估结果
		//获取评估数据
		ThresholdData thresholdData=m_evaluationStore.loadDataFromFile();
		//校验读入的thresholdData内容是否可以用于目前评估
		String msg=m_evaluationStore.validateThresholdData(thresholdData);
		if (msg==null){
			System.out.println("ThresholdData verified for target yearsplit "+yearSplit);
		}
		else 
			throw new Exception(msg);


		//获取预测文件中的应该用哪个modelYearSplit的模型
		String modelYearSplit=thresholdData.getModelYearSplit();
		//从评估结果中找到正向模型文件。		
		ModelStore modelStore=new ModelStore(m_evaluationStore.getWorkFilePath(),thresholdData.getModelFileName(), modelYearSplit);

		// 从保存的数据文件中加载分类用的model and header，此加载方法内部有对modelYear的校验		
		modelStore.loadModelFromFile(yearSplit);
		//获取model
		Classifier model =modelStore.getModel();
		//模型数据的校验会在加载方法内部进行，此处下面仅校验正向模型的格式
		GeneralInstances header =modelStore.getModelFormat();
		
		
		// There is additional ID attribute in test instances, so we should save it and remove before doing prediction
		double[] ids=test.attributeToDoubleArray(ArffFormat.ID_POSITION - 1);  
		//删除已保存的ID 列，让待分类数据与模型数据一致 （此处的index是从1开始）
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(test);
		test=instanceProcessor.removeAttribs(test,  Integer.toString(ArffFormat.ID_POSITION));
		//验证数据格式是否一致
		String verify=verifyDataFormat(test, header);
		if (verify!=null){
			System.err.println("attention! model and testing data structure is not the same. Here is the difference: "+verify);
			//如果不一致，试着Calibrate一下。
			DataInstances outTemp=new DataInstances(header,0);
			instanceProcessor.calibrateAttributes(test, outTemp);
			test=outTemp;
			//再比一次
			BaseInstanceProcessor.compareInstancesFormat(test, header);
		}

		//获取预测文件中的应该用哪个反向模型的模型
		String reversedModelYearSplit=thresholdData.getReversedModelYearSplit();

		boolean usingOneModel=false; 
		Classifier reversedModel=null;
		if (reversedModelYearSplit.equals(modelYearSplit)){//正向模型和方向模型是同一模型的。
			usingOneModel=true;
		}else{

			//从评估结果中找到反向模型文件。		
			ModelStore reversedModelStore=new ModelStore(m_evaluationStore.getWorkFilePath(),thresholdData.getReversedModelFileName(), reversedModelYearSplit);

			// 从保存的数据文件中加载分类用的model,此加载方法内部有对modelYear的校验		
			reversedModelStore.loadModelFromFile(yearSplit);
			//获取model
			reversedModel=reversedModelStore.getModel();
		}
		double thresholdMin=thresholdData.getThreshold(); //判断为1的阈值，大于该值意味着该模型判断其为1
		double reversedThresholdMax=thresholdData.getReversedThreshold(); //判断为0的阈值，小于该值意味着该模型坚定认为其为0 （这是合并多个模型预测时使用的）
		if (reversedThresholdMax>thresholdMin){
			if(usingOneModel==true){
				throw new Exception("fatal error!!! reversedThreshold("+reversedThresholdMax+") > threshold("+thresholdMin+") using same model (modelyear="+reversedModelYearSplit+")" );
			}else{
				System.out.println("使用不同模型时反向阀值大于正向阀值了"+"reversedThreshold("+reversedThresholdMax+")@"+reversedModelYearSplit + " > threshold("+thresholdMin+")@"+modelYearSplit);
			}
		}

		//开始用分类模型和阀值进行预测
		System.out.println("actual -> predicted....... ");
		

		int testInstancesNum=test.numInstances();
		DescriptiveStatistics totalPositiveShouyilv=new DescriptiveStatistics();
		DescriptiveStatistics totalNegativeShouyilv=new DescriptiveStatistics();
		DescriptiveStatistics selectedPositiveShouyilv=new DescriptiveStatistics();
		DescriptiveStatistics selectedNegativeShouyilv=new DescriptiveStatistics();			
		
		double pred;
		double reversedPred;
		
		
		for (int i = 0; i < testInstancesNum; i++) {
			GeneralInstance curr = test.instance(i);
			pred=classify(model,curr);  //调用子类的分类函数
			if (usingOneModel==true){
				reversedPred=pred;
			}else{
				reversedPred=classify(reversedModel, curr); //调用反向评估模型的分类函数
			}
			DataInstance inst = new DataInstance(result.numAttributes());
			inst.setDataset(result);
			//将相应的ID赋值回去
			inst.setValue(ArffFormat.ID_POSITION - 1, ids[i]);
			for (int n = 1; n < inst.numAttributes() - 3; n++) { // ignore the
																	// first ID.
				GeneralAttribute att = test.attribute(inst.attribute(n).name());
				// original attribute is also present in the current data set
				if (att != null) {
					if (att.isNominal()) {
						String label = test.instance(i).stringValue(att);
						int index = att.indexOfValue(label);
						if (index != -1) {
							inst.setValue(n, index);
						}
					} else if (att.isNumeric()) {
						inst.setValue(n, test.instance(i).value(att));
					} else {
						throw new IllegalStateException("Unhandled attribute type!");
					}
				}
			}

			inst.setValue(result.numAttributes() - 3, curr.classValue());
			inst.setValue(result.numAttributes() - 2, pred);
			
			double shouyilv=getShouyilv(i,ids[i],curr.classValue());
			
			if (shouyilv>getPositiveLine()){ //这里的positive是个相对于positiveLine的相对概念
				totalPositiveShouyilv.addValue(shouyilv);
			}else {
				totalNegativeShouyilv.addValue(shouyilv);
			}

			double selected = BaseClassifier.VALUE_NOT_SURE;

			//先用反向模型判断
			if (reversedPred<reversedThresholdMax){
				selected=BaseClassifier.VALUE_NEVER_SELECT;//反向模型坚定认为当前数据为0 （这是合并多个模型预测时使用的）
			}

			//如果正向模型与方向模型得出的值矛盾（在使用不同模型时），则用正向模型的数据覆盖方向模型（因为毕竟正向模型的ratio比较小）
			if (pred >=thresholdMin ) { //本模型估计当前数据是1值
				selected = BaseClassifier.VALUE_SELECTED;  

				if (shouyilv>getPositiveLine()){ //这里的positive是个相对于positiveLine的相对概念
					selectedPositiveShouyilv.addValue(shouyilv);
				}else {
					selectedNegativeShouyilv.addValue(shouyilv);
				}
			}
			
			inst.setValue(result.numAttributes() - 1, selected);
			result.add(inst);
		}
		
		

		double percentile=thresholdData.getPercent();
		boolean isGuessed=thresholdData.isGuessed();
		String defaultThresholdUsed=" ";
		if (isGuessed){
			defaultThresholdUsed="Y";
		}		
		
		double[] modelAUC=thresholdData.getModelAUC();
		double reversedPercentile=thresholdData.getReversedPercent();
		
		
		
		if ("".equals(yearSplit) ){
			//这是预测每日数据时，没有实际收益率数据可以做评估 (上述逻辑会让所有的数据都进入negative的分支）
			classifySummaries.savePredictSummaries(policySplit,totalNegativeShouyilv,selectedNegativeShouyilv);
			//输出评估结果及所使用阀值及期望样本百分比
			String evalSummary="( with params: modelYearSplit="+modelYearSplit+"threshold="+FormatUtility.formatDouble(thresholdMin,0,3)+" , percentile="+FormatUtility.formatPercent(percentile/100)+" ,defaultThresholdUsed="+defaultThresholdUsed;
			evalSummary+=" ,reversedModelYearSplit="+reversedModelYearSplit+" ,reversedThreshold="+FormatUtility.formatDouble(reversedThresholdMax,0,3)+" , reversedPercentile="+FormatUtility.formatPercent(reversedPercentile/100);
			evalSummary+=" ,modelAUC@focusAreaRatio=";
			double[] focusAreaRatio=thresholdData.getFocosAreaRatio();
			for (int i=0;i<focusAreaRatio.length;i++) {
				evalSummary+=FormatUtility.formatDouble(modelAUC[i],0,4)+"@"+FormatUtility.formatPercent(focusAreaRatio[i], 2, 0)+", " ;	
			}
			evalSummary+=" )\r\n";
			classifySummaries.appendEvaluationSummary(evalSummary);

		}else{
			//这是进行历史回测数据时，根据历史收益率数据进行阶段评估
			classifySummaries.computeClassifySummaries(yearSplit,policySplit,totalPositiveShouyilv,totalNegativeShouyilv,selectedPositiveShouyilv,selectedNegativeShouyilv);
			
			
			 //输出评估结果及所使用阀值及期望样本百分比
			String evalSummary=","+modelYearSplit+","+FormatUtility.formatDouble(thresholdMin,0,3)+","+FormatUtility.formatPercent(percentile/100)+","+defaultThresholdUsed+",";
			evalSummary+=reversedModelYearSplit+","+FormatUtility.formatDouble(reversedThresholdMax,0,3)+","+FormatUtility.formatPercent(reversedPercentile/100)+",";
			for (double d : modelAUC) {
				evalSummary+=FormatUtility.formatDouble(d,0,4)+","; 
			}
			evalSummary+="\r\n";
			classifySummaries.appendEvaluationSummary(evalSummary);
		}
	}

	// 对于连续分类器， 收益率就是classvalue，缺省直接返回， 对于nominal分类器，调用子类的方法获取暂存的收益率
	protected double getShouyilv(int index,double id, double newClassValue) throws Exception{
		return newClassValue;
	}
	
	// 对于连续分类器，positiveLine就是0， 缺省直接返回0， 对于nominal分类器，调用子类的方法获取m_positiveLine
	protected double getPositiveLine(){
		return 0;
	}

	
	public static String verifyDataFormat(GeneralInstances test, GeneralInstances header) throws Exception {
//		//在使用旧格式时，如果有使用旧字段名的模型，试着将其改名后使用
//		if (modelArffFormat==ArffFormat.LEGACY_FORMAT){
//			header=ArffFormat.renameOldArffName(header);
//		}
		return BaseInstanceProcessor.compareInstancesFormat(test, header);
	}

	
	//找到回测评估、预测时应该使用evaluationStore对象（主要为获取model文件和eval文件名称）
	public void locateEvalutationStore(String targetYearSplit,String policySplit,String modelFilePath, String modelFilePrefix) {
		EvaluationStore evaluationStore=new EvaluationStore(targetYearSplit,policySplit,modelFilePath, modelFilePrefix,this);
		m_evaluationStore=evaluationStore;
	}
	
	//为每日预测时使用
	public void setEvaluationStore(EvaluationStore m_evaluationStore) {
		this.m_evaluationStore = m_evaluationStore;
	}

	/**
	 * @param dataTag
	 * @return
	 * @see yueyueGo.ModelStore#validateEvalData(yueyueGo.databeans.GeneralDataTag)
	 */
	public String validateEvalData(GeneralDataTag dataTag) {
		return m_evaluationStore.validateEvalData(dataTag);
	}

	/**
	 * @param dataTag
	 * @return
	 * @see yueyueGo.ModelStore#validateTestingData(yueyueGo.databeans.GeneralDataTag)
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
	public static BaseClassifier makeCopy(BaseClassifier cl) throws Exception {

		return (BaseClassifier) new SerializedObject(cl).getObject();
	}
	public int getModelArffFormat() {
		return modelArffFormat;
	}

	public void setModelArffFormat(int modelArffFormat) {
		this.modelArffFormat = modelArffFormat;
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
	
	/*
	 * 输出分类器的参数设置
	 */
	public void outputClassifyParameter() {
		System.out.println("***************************************CLASSIFY DATE="+FormatUtility.getDateStringFor(0));
		System.out.println("ClassifyIdentity="+this.getIdentifyName());
		System.out.println("m_skipTrainInBacktest="+this.m_skipTrainInBacktest);
		System.out.println("m_skipEvalInBacktest="+this.m_skipEvalInBacktest);
		System.out.println("m_positiveLine="+m_positiveLine);
		System.out.println("m_modelDataSplitMode="+m_evalDataSplitMode);
		System.out.println("m_modelEvalFileShareMode="+m_modelFileShareMode);
		System.out.println("modelArffFormat="+modelArffFormat);
//		System.out.println(m_evaluationStore.showEvaluationParameters());
		System.out.println("TOP AREA RATIO="+EvaluationStore.TOP_AREA_RATIO);
		System.out.println("reversed TOP AREA RATIO="+EvaluationStore.REVERSED_TOP_AREA_RATIO);
	    System.out.println("***************************************");
	}
	
	/*
	 * 输出分类器的分类结果
	 */
	public void outputClassifySummary() throws Exception{
		outputClassifyParameter();
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
			m_skipEvalInBacktest=false;
			break;
		case FOR_EVALUATE_MODEL:
			m_skipTrainInBacktest=true;
			m_skipEvalInBacktest=false;
			break;	
		case FOR_BACKTEST_MODEL:
			m_skipTrainInBacktest=true;
			m_skipEvalInBacktest=true;
		case FOR_DAILY_PREDICT:
			break;
		}
		
	}
	
	

}
