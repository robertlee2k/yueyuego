package yueyueGo;

import java.io.Serializable;
import java.util.Vector;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializedObject;
import yueyueGo.utility.AppContext;
import yueyueGo.utility.ClassifySummaries;
import yueyueGo.utility.EvaluationBenchmark;
import yueyueGo.utility.EvaluationConfDefinition;
import yueyueGo.utility.EvaluationParams;
import yueyueGo.utility.FileUtility;
import yueyueGo.utility.FormatUtility;
import yueyueGo.utility.InstanceUtility;
import yueyueGo.utility.ThresholdData;

/**
 * @author robert
 * 所有分类器的基类
 */
public abstract class BaseClassifier implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5895562408723104016L;
	//统一常量
	public static final String MA_PREFIX = " MA ";
	public static final String ARFF_EXTENSION = ".arff";
	//名称
	public String classifierName;
	
	//子类定义的工作路径
	protected String WORK_PATH ;
	protected String WORK_FILE_PREFIX;
	protected int m_modelEvalFileShareMode; //model文件和Eval的共享模式。
	
	protected ModelStore m_modelStore; //model 和 eval的持久化封装类类
	
	public boolean m_noCaculationAttrib;  //加入的计算字段与否
	protected int modelArffFormat; //arff的格式


	//用于策略分组
    public String[] m_policySubGroup;//在子类构造函数中赋值覆盖 = {"5","10","20","30","60" }或{""};
    public EvaluationConfDefinition m_evalConf; //用于评估的对象

    
    //用于回测中使用
	public boolean m_skipTrainInBacktest = true; //在子类构造函数中赋值覆盖
	public boolean m_skipEvalInBacktest = true;  //在子类构造函数中赋值覆盖
	
	
	protected double m_positiveLine; // 用来定义收益率大于多少时算positive，缺省为0

	
	protected ClassifySummaries classifySummaries;//分类的统计信息
	
	public BaseClassifier() {
		m_positiveLine=0; //缺省的以收益率正负为二分类的正负。
		m_noCaculationAttrib=false;  //缺省情况下，加入的计算字段 （在子类中覆盖）
		modelArffFormat=ArffFormat.EXT_FORMAT; //缺省使用扩展arff
		m_modelEvalFileShareMode=ModelStore.SEPERATE_MODEL_AND_EVAL; //model文件和Eval的共享模式,缺省为 回测时按yearsplit和policysplit分割使用model和eval文件
		WORK_FILE_PREFIX= "extData2005-2016";
		initializeParams();		// 留给子类的初始化参数函数
		setWorkPathThenCheck(); //根据参数设置工作路径

		initEvaluationConfDefinition(); //初始化evaluation的常量定义
	}
	
	//一系列需要子类实现的抽象方法
	protected abstract void initializeParams();
	protected abstract Classifier buildModel(Instances trainData) throws Exception;
	protected abstract Vector<Double> doModelEvaluation(EvaluationBenchmark benchmark ,Instances evalData,Classifier model, EvaluationParams evalParams) throws Exception;
	protected abstract double classify(Classifier model,Instance curr) throws Exception ;
	
	//可以在子类中被覆盖
	protected void initEvaluationConfDefinition(){
		EvaluationConfDefinition evalConf=new EvaluationConfDefinition(this.classifierName);
		this.m_evalConf=evalConf;
	}
	
	protected EvaluationParams getEvaluationInstance(String policy){
		int pos=-1;
		for (int i=0;i<m_policySubGroup.length;i++){
			if (m_policySubGroup[i].equals(policy)){
				pos=i;
				break;
			}
		}
		if (pos==-1) {
			throw new RuntimeException("cannot find policy ["+policy+"]in m_policySubGroup");
		}
		return this.m_evalConf.getEvaluationInstance(pos);
	}
	
	public Classifier trainData(Instances train) throws Exception {
		Classifier model=buildModel(train);
		// save model + header
		m_modelStore.setModel(model);
		m_modelStore.setModelFormat(new Instances(train, 0));
		m_modelStore.saveModelToFiles();
		System.out.println("Training finished!");
		return model;
	}
	

	
	//评估模型
	public void evaluateModel(Instances trainData,Instances evalData,Classifier model,EvaluationParams evalParams) throws Exception{
		if (model==null){ // 跳过建模直接做评估时，重新加载文件
			model =m_modelStore.loadModelFromFile();
			Instances header =m_modelStore.getModelFormat();
			Instances trainFormat=new Instances(trainData,0);
			Instances evalFormat=new Instances(evalData,0);
			//验证原有训练数据格式是否一致
			String verify=verifyDataFormat(trainFormat, header);
			if (verify!=null){
				throw new Exception("attention! model and training data structure is not the same. Here is the difference: "+verify);
			}			
			//验证评估数据格式是否一致
			verify=verifyDataFormat(evalFormat, header);
			if (verify!=null){
				throw new Exception("attention! model and evaluation data structure is not the same. Here is the difference: "+verify);
			}
		}
		System.out.println(" -----------evaluating for FULL Market....");
		//评估模型
		EvaluationBenchmark benchmark = getEvaluation(trainData,evalData, model,1-evalParams.getEval_recent_portion());

		System.out.println("finish evaluating model, try to get best threshold for model...");
		Vector<Double> v = doModelEvaluation(benchmark,evalData, model, evalParams);
		System.out.println(" *********** end of evaluating for FULL Market....");		

		ThresholdData.saveEvaluationToFile(m_modelStore.getEvalFileName(), v);

	}

	
	//评估模型，eval_start_portion为0到1的值， 为0时表示利用全部Instances做评估，否则取其相应比例评估
	protected EvaluationBenchmark getEvaluation(Instances trainData,Instances evalData, Classifier model, double eval_start_portion)
			throws Exception {
		Instances evalTrain;
		Instances evalSamples;

//		if (eval_start_portion==0){ //全样本评估
		System.out.println("evluation with full incoming dataset, size: "+evalData.numInstances());
		evalTrain=trainData;
		evalSamples=evalData;
//		}else{
//			int evaluateFrom=new Double(evalData.numInstances()*eval_start_portion).intValue(); //选取开始评估的点。
//			int evaluateCount=evalData.numInstances()-evaluateFrom;
//			System.out.println("evluation Sample starts From : " + evaluateFrom+" evaluation sample size: "+evaluateCount);
//			evalTrain=new Instances(evalData,0,evaluateFrom);  //前部分作为训练样本
//			evalSamples=new Instances(evalData,evaluateFrom,evaluateCount);  //后部分作为评估样本
//		}
			
		Evaluation eval = new Evaluation(evalTrain);
		
		System.out.println("evaluating.....");
		eval.evaluateModel(model, evalSamples); // evaluate on the training data to get threshold
		System.out.println(eval.toSummaryString("\nEvaluate Model Results\n\n", true));
		boolean isNominal=false;
		if (this instanceof NominalClassifier){
			isNominal=true;
			System.out.println(eval.toMatrixString ("\nEvaluate Confusion Matrix\n\n"));
			System.out.println(eval.toClassDetailsString("\nEvaluate Class Details\n\n"));
		}

		EvaluationBenchmark benchmark=new EvaluationBenchmark(eval, trainData, evalData, isNominal);
		return benchmark;
	}	

	//为每日预测用，这时候没有yearSplit （policySplit是存在的）
	// result parameter will be changed in this method!
	public void predictData(Instances test, Instances result,String policySplit)		throws Exception {
		predictData(test, result,"",policySplit);
	}
	
	//为回测历史数据使用
	// result parameter will be changed in this method!
	public  void predictData(Instances test, Instances result,String yearSplit,String policySplit)
			throws Exception {

		
		ThresholdData evalData=new ThresholdData();
		evalData.loadDataFromFile(m_modelStore.getEvalFileName());
		
		evalData=processThresholdData(evalData);
		
		double thresholdMin=evalData.getThresholdMin();
		double thresholdMax=evalData.getThresholdMax();

		predictWithThresHolds(test, result,thresholdMin, thresholdMax,yearSplit,policySplit);

	}

	/**
	 * @param test
	 * @param result
	 * @param thresholdMin
	 * @param thresholdMax
	 * @param thresholdMin_hs300
	 * @param thresholdMax_hs300
	 * @return
	 * @throws Exception
	 * @throws IllegalStateException
	 */
	private void predictWithThresHolds(Instances test, Instances result,
			double thresholdMin, double thresholdMax, String yearSplit,String policySplit)
			throws Exception, IllegalStateException {
		// read classify model and header
		Classifier model =m_modelStore.loadModelFromFile();
		Instances header =m_modelStore.getModelFormat();
		// There is additional ID attribute in test instances, so we should save it and remove before doing prediction
		double[] ids=test.attributeToDoubleArray(ArffFormat.ID_POSITION - 1);  
		//删除已保存的ID 列，让待分类数据与模型数据一致 （此处的index是从1开始）
		test=InstanceUtility.removeAttribs(test,  Integer.toString(ArffFormat.ID_POSITION));
		//验证数据格式是否一致
		String verify=verifyDataFormat(test, header);
		if (verify!=null){
			System.err.println("attention! model and testing data structure is not the same. Here is the difference: "+verify);
			//如果不一致，试着Calibrate一下。
			Instances outTemp=new Instances(header,0);
			InstanceUtility.calibrateAttributes(test, outTemp);
			test=outTemp;
			//再比一次
			InstanceUtility.compareInstancesFormat(test, header);
		}
		
		//开始用分类模型和阀值进行预测
		System.out.println("actual -> predicted....... ");
		

		int testInstancesNum=test.numInstances();
		DescriptiveStatistics totalPositiveShouyilv=new DescriptiveStatistics();
		DescriptiveStatistics totalNegativeShouyilv=new DescriptiveStatistics();
		DescriptiveStatistics selectedPositiveShouyilv=new DescriptiveStatistics();
		DescriptiveStatistics selectedNegativeShouyilv=new DescriptiveStatistics();			
		
		
		
		for (int i = 0; i < testInstancesNum; i++) {
			Instance curr = (Instance) test.instance(i).copy();
			double pred=classify(model,curr);  //调用子类的分类函数
			Instance inst = new DenseInstance(result.numAttributes());
			inst.setDataset(result);
			//将相应的ID赋值回去
			inst.setValue(ArffFormat.ID_POSITION - 1, ids[i]);
			for (int n = 1; n < inst.numAttributes() - 3; n++) { // ignore the
																	// first ID.
				Attribute att = test.attribute(inst.attribute(n).name());
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


			double t_min=thresholdMin;
			double t_max=thresholdMax;
			double selected = 0.0;
			
			if (pred >=t_min  && pred <= t_max) {
				selected = 1.0;

				if (shouyilv>getPositiveLine()){ //这里的positive是个相对于positiveLine的相对概念
					selectedPositiveShouyilv.addValue(shouyilv);
				}else {
					selectedNegativeShouyilv.addValue(shouyilv);
				}
			}
			
			inst.setValue(result.numAttributes() - 1, selected);
			result.add(inst);
		}
		

		if ("".equals(yearSplit) ){
			//这是预测每日数据时，没有实际收益率数据可以做评估 (上述逻辑会让所有的数据都进入negative的分支）
			classifySummaries.savePredictSummaries(policySplit,totalNegativeShouyilv,selectedNegativeShouyilv);
		}else{
			//这是进行历史回测数据时，根据历史收益率数据进行阶段评估
			classifySummaries.computeClassifySummaries(yearSplit,policySplit,totalPositiveShouyilv,totalNegativeShouyilv,selectedPositiveShouyilv,selectedNegativeShouyilv);
		}
		String evalSummary=","+FormatUtility.formatDouble(thresholdMin)+","+FormatUtility.formatDouble(thresholdMax)+"\r\n";  //输出评估结果及所使用阀值上、下限
		classifySummaries.appendEvaluationSummary(evalSummary);
		
	}

	// 对于连续分类器， 收益率就是classvalue，缺省直接返回， 对于nominal分类器，调用子类的方法获取暂存的收益率
	protected double getShouyilv(int index,double id, double newClassValue) throws Exception{
		return newClassValue;
	}
	
	// 对于连续分类器，positiveLine就是0， 缺省直接返回0， 对于nominal分类器，调用子类的方法获取m_positiveLine
	protected double getPositiveLine(){
		return 0;
	}

	
	protected String verifyDataFormat(Instances test, Instances header) throws Exception {
//		//在使用旧格式时，如果有使用旧字段名的模型，试着将其改名后使用
//		if (modelArffFormat==ArffFormat.LEGACY_FORMAT){
//			header=ArffFormat.renameOldArffName(header);
//		}
		return InstanceUtility.compareInstancesFormat(test, header);
	}

	//初始化回测创建模型时使用的modelStore对象（这里严格按yearsplit和policysplit分割处理）
	public void initModelStore(String yearSplit,String policySplit) {
		String modelFileName=ModelStore.concatModeFilenameString(yearSplit, policySplit, this.WORK_PATH+this.WORK_FILE_PREFIX, this.classifierName);
		ModelStore modelStore=new ModelStore(modelFileName,modelFileName+ModelStore.THRESHOLD_EXTENSION);
		m_modelStore=modelStore;
	}
	
	//找到回测评估、预测时应该使用modelStore对象（主要为获取model文件和eval文件名称）
	//此类可以在子类中被覆盖（通过把yearsplit的值做处理，实现临时指定使用某个模型，可以多年使用一个模型，也可以特殊指定某年月使用某模型）
	public void locateModelStore(String yearSplit,String policySplit) {
		ModelStore modelStore=new ModelStore(yearSplit,policySplit,this.WORK_PATH+this.WORK_FILE_PREFIX, this.classifierName,this.m_modelEvalFileShareMode);
		m_modelStore=modelStore;
	}
	//生成日常预测时使用的model文件和eval文件名称
	public void setModelStore(ModelStore m){
		m_modelStore=m;
	}
	

	//对于父类来说，do nothing
	protected ThresholdData processThresholdData(ThresholdData eval){
		return eval;
	}
	
	//缺省返回classifierName，某些子类（比如MultiPCA）可能会返回其他名字，这是为了保存文件时区分不同参数用
	public String getIdentifyName(){
		return classifierName;
	}

	private void setWorkPathThenCheck(){
		if (this instanceof ContinousClassifier){
			setWorkPath(AppContext.getCONTINOUS_CLASSIFIER_DIR()+getIdentifyName()+"\\");
		}else if (this instanceof NominalClassifier){
			setWorkPath(AppContext.getNOMINAL_CLASSIFIER_DIR()+getIdentifyName()+"\\");
		}
		FileUtility.mkdirIfNotExist(WORK_PATH);
	}
	
	public void setWorkPath(String apath){
		WORK_PATH=apath;
	}
	/**
	 * Creates a deep copy of the given classifier using serialization.
	 *
	 * @param model the classifier to copy
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
	public void outputClassifySummary() throws Exception{
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
}
