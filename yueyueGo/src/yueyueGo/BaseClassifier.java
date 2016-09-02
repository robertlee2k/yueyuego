package yueyueGo;

import java.io.IOException;
import java.util.Vector;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public abstract class BaseClassifier {
	//统一常量
	public static final String MA_PREFIX = " MA ";
	public static final String ARFF_EXTENSION = ".arff";
	public static final String THRESHOLD_EXTENSION = ".eval";
	public static final String TXT_EXTENSION = ".txt";
	public static final String MODEL_FILE_EXTENSION = ".mdl";
	
	//名称
	public String classifierName;
	
	//子类定义的工作路径
	public String WORK_PATH ;
	public String WORK_FILE_PREFIX;
	

	public boolean m_noCaculationAttrib=true;  //缺省情况下，限制输入文件中的计算字段 （在子类中覆盖）
	public int arff_format=ArffFormat.EXT_FORMAT; //缺省使用扩展arff

	//用于策略分组
    public String[] m_policySubGroup;//在子类构造函数中赋值覆盖 = {"5","10","20","30","60" }或{""};

    
    //用于回测中使用
	public boolean m_skipTrainInBacktest = true; //在子类构造函数中赋值覆盖
	public boolean m_skipEvalInBacktest = true;  //在子类构造函数中赋值覆盖
	public boolean m_saveArffInBacktest = false; //缺省为false

	
	//无须由外界函数设置的，在各子类中近乎常量的值
	protected double EVAL_RECENT_PORTION;// 计算最近数据阀值从历史记录中选取多少比例的最近样本
	public double[] SAMPLE_LOWER_LIMIT; // 各条均线选择样本的下限
	public double[] SAMPLE_UPPER_LIMIT; // 各条均线选择样本的上限
	public double[] TP_FP_RATIO_LIMIT; //各条均线TP/FP选择阀值比例上限
	protected double TP_FP_BOTTOM_LINE=0.5; //TP/FP的缺省下限
	

	
	protected String model_filename;
	protected String evaluation_filename;

	//统计信息
	protected DescriptiveStatistics summary_selected_TPR;
	protected DescriptiveStatistics summary_selected_positive;
	protected DescriptiveStatistics summary_lift;
	protected DescriptiveStatistics summary_selected_count;
	protected DescriptiveStatistics summary_judge_result;
	
	protected DescriptiveStatistics summary_selectedShouyilv;
	protected DescriptiveStatistics summary_totalShouyilv;	
	public String getEvaluationFilename() {
		return evaluation_filename;
	}

	public void setEvaluationFilename(String evaluation_filename) {
		this.evaluation_filename = evaluation_filename;
	}


	public BaseClassifier() {
		summary_selected_TPR= new DescriptiveStatistics();
		summary_selected_positive= new DescriptiveStatistics();
		summary_lift= new DescriptiveStatistics();
		summary_selected_count=new DescriptiveStatistics();
		summary_judge_result=new DescriptiveStatistics();
		
		summary_selectedShouyilv= new DescriptiveStatistics();
		summary_totalShouyilv= new DescriptiveStatistics();

		WORK_FILE_PREFIX = "extData2005-2016";
		initializeParams();
	}
	
	//一系列需要子类实现的抽象方法
	protected abstract void initializeParams();
	protected abstract Classifier buildModel(Instances trainData) throws Exception;
	public abstract Vector<Double> evaluateModel(Instances train,Classifier model,double sample_limit, double sample_upper,double tp_fp_ratio) throws Exception;
	protected abstract double classify(Classifier model,Instance curr) throws Exception ;
	
	public Classifier trainData(Instances train) throws Exception {
		Classifier model=buildModel(train);
		// save model + header
		Vector<Object> v = new Vector<Object>();
		v.add(model);
		v.add(new Instances(train, 0));
		saveModelToFiles(model, v);
		System.out.println("Training finished!");
		return model;
	}

	
	//评估模型，eval_start_portion为0到1的值， 为0时表示利用全部Instances做评估，否则取其相应比例评估
	protected Evaluation getEvaluation(Instances train, Classifier model, double eval_start_portion)
			throws Exception {
		Instances evalTrain;
		Instances evalSamples;

		if (eval_start_portion==0){ //全样本评估
			System.out.println("evluation with full dataset, size: "+train.numInstances());
			evalTrain=train;
			evalSamples=train;
		}else{
			int evaluateFrom=new Double(train.numInstances()*eval_start_portion).intValue(); //选取开始评估的点。
			int evaluateCount=train.numInstances()-evaluateFrom;
			System.out.println("evluation Sample starts From : " + evaluateFrom+" evaluation sample size: "+evaluateCount);
			evalTrain=new Instances(train,0,evaluateFrom);  //前部分作为训练样本
			evalSamples=new Instances(train,evaluateFrom,evaluateCount);  //后部分作为评估样本
		}
			
		Evaluation eval = new Evaluation(evalTrain);
		
		System.out.println("evaluating.....");
		eval.evaluateModel(model, evalSamples); // evaluate on the training data to get threshold
		System.out.println(eval.toSummaryString("\nEvaluate Model Results\n\n", true));
		if (this instanceof NominalClassifier){
			System.out.println(eval.toMatrixString ("\nEvaluate Confusion Matrix\n\n"));
			System.out.println(eval.toClassDetailsString("\nEvaluate Class Details\n\n"));
		}
		return eval;
	}	
	
	// result parameter will be changed in this method!
	// return the evaluation summary string for prediction
	public  String predictData(Instances test, Instances result)
			throws Exception {

		
		ThresholdData evalData=new ThresholdData();
		evalData.loadDataFromFile(this.getEvaluationFilename());
		
		evalData=processThresholdData(evalData);
		
		double thresholdMin=evalData.getThresholdMin();
		double thresholdMax=evalData.getThresholdMax();
		
//		//20160711临时放开预测阀值
//		if (thresholdMin>=0.16 && thresholdMin<=0.161) thresholdMin=0.12;

		return predictWithThresHolds(test, result,thresholdMin, thresholdMax);

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
	private String predictWithThresHolds(Instances test, Instances result,
			double thresholdMin, double thresholdMax)
			throws Exception, IllegalStateException {
		// read classify model and header
		String modelFileName=this.getModelFileName()+MODEL_FILE_EXTENSION;
		@SuppressWarnings("unchecked")
		Vector<Object> v = (Vector<Object>) SerializationHelper.read(modelFileName);
		Classifier model = (Classifier) v.get(0);
		Instances header = (Instances) v.get(1);
		System.out.println("Classifier Model Loaded From: "+ modelFileName);


		// There is additional ID attribute in test instances, so we should save it and remove before doing prediction
		double[] ids=test.attributeToDoubleArray(ArffFormat.ID_POSITION - 1);  
		//删除已保存的ID 列，让待分类数据与模型数据一致 （此处的index是从1开始）
		test=InstanceUtility.removeAttribs(test,  Integer.toString(ArffFormat.ID_POSITION));
		//验证数据格式是否一致
		verifyDataFormat(test, header);
		
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
			
			if (shouyilv>0){
				totalPositiveShouyilv.addValue(shouyilv);
			}else {
				totalNegativeShouyilv.addValue(shouyilv);
			}


			double t_min=thresholdMin;
			double t_max=thresholdMax;
			double selected = 0.0;
			
			if (pred >=t_min  && pred <= t_max) {
				selected = 1.0;

				if (shouyilv>0){
					selectedPositiveShouyilv.addValue(shouyilv);
				}else {
					selectedNegativeShouyilv.addValue(shouyilv);
				}
			}
			
			inst.setValue(result.numAttributes() - 1, selected);
			result.add(inst);
		}
		String evaluationSummary=evaluateResults(totalPositiveShouyilv,totalNegativeShouyilv,selectedPositiveShouyilv,selectedNegativeShouyilv);
		evaluationSummary+=","+FormatUtility.formatDouble(thresholdMin)+","+FormatUtility.formatDouble(thresholdMax)+"\r\n";  //输出评估结果及所使用阀值上、下限
		return evaluationSummary;
	}

	// 对于连续分类器， 收益率就是classvalue，缺省直接返回， 对于nominal分类器，调用子类的方法获取暂存的收益率
	protected double getShouyilv(int index,double id, double newClassValue) throws Exception{
		return newClassValue;
	}

	@SuppressWarnings("deprecation")
	protected void verifyDataFormat(Instances test, Instances header) throws Exception {
		//在使用旧格式时，如果有使用旧字段名的模型，试着将其改名后使用
		if (arff_format==ArffFormat.LEGACY_FORMAT){
			header=ArffFormat.renameOldArffName(header);
		}
		InstanceUtility.compareInstancesFormat(test, header);
	}

	//用于评估单次分类的效果。 对于回测来说，评估的规则有以下几条：
	//1. 市场牛市时（量化定义为total_TPR>0.5)， 应保持绝对胜率（selected_TPR>0.5）且选择足够多的机会， 以20单元格5均线为例。单月机会(selectedCount）应该大于2*20/5
	//2. 市场小牛市时（量化定义为total_TPR介于0.33与0.5之间)， 应提升胜率（final_lift>1），且保持机会， 以20单元格5均线为例。单月机会(selectedCount）应该大于20/5
	//3. 市场小熊市时（量化定义为total_TPR介于0.2到0.33之间)，  应提升绝对胜率（selected_TPR>0.33）或 选择少于半仓 selectedCount小于20/4/2
	//3. 市场小熊市时（量化定义为total_TPR<0.2)，  应提升绝对胜率（selected_TPR>0.33）或 选择少于2成仓 selectedCount小于20/4/5
	protected String evaluateResults(DescriptiveStatistics totalPositiveShouyilv,DescriptiveStatistics totalNegativeShouyilv,DescriptiveStatistics selectedPositiveShouyilv,DescriptiveStatistics selectedNegativeShouyilv) {
		double selected_TPR=0;
		double total_TPR=0;
		double tpr_lift=0;
		double selectedShouyilv=0.0;
		double totalShouyilv=0.0;
		double shouyilv_lift=0.0;
		long selectedPositive=selectedPositiveShouyilv.getN();
		long selectedNegative=selectedNegativeShouyilv.getN();
		long selectedCount=selectedPositive+selectedNegative;
		long positive=totalPositiveShouyilv.getN();
		long negative=totalNegativeShouyilv.getN();
		long totalCount=positive+negative;

		
		if (selectedCount>0) {
			selected_TPR=(double)selectedPositive/selectedCount;
			selectedShouyilv=(selectedPositiveShouyilv.getSum()+selectedNegativeShouyilv.getSum())/selectedCount;
		}
		if (totalCount>0) {
			total_TPR=(double)positive/totalCount;
			totalShouyilv=(totalPositiveShouyilv.getSum()+totalNegativeShouyilv.getSum())/totalCount;
		}
		if (total_TPR>0) {
			tpr_lift=selected_TPR/total_TPR;
		}

		shouyilv_lift=selectedShouyilv-totalShouyilv;

		System.out.println("*** selected count= " + selectedCount + " selected positive: " +selectedPositive + "  selected negative: "+selectedNegative); 
		System.out.println("*** total    count= "	+ totalCount+ " actual positive: "+ positive + " actual negtive: "+ negative);
		System.out.println("*** selected TPR= " + FormatUtility.formatPercent(selected_TPR) + " total TPR= " +FormatUtility.formatPercent(total_TPR) + "  lift up= "+FormatUtility.formatDouble(tpr_lift));
		
		System.out.println("*** selected average Shouyilv= " + FormatUtility.formatPercent(selectedShouyilv) + " total average Shouyilv= " +FormatUtility.formatPercent(totalShouyilv)+ "  lift difference= "+FormatUtility.formatPercent(shouyilv_lift) );
		
		
		int resultJudgement=0;
		
		// 评估收益率是否有提升是按照选择平均收益率*可买入机会数 是否大于总体平均收益率*20（按20单元格单均线情况计算）
		long buyableCount=0;
		if (selectedCount>20){
			buyableCount=20;
		}else {
			buyableCount=selectedCount;
		}
		
		//评估此次成功与否
		if (selectedShouyilv*buyableCount>=totalShouyilv*20)
			resultJudgement=1;
		else 
			resultJudgement=0;

		System.out.println("*** evaluation result for this period :"+resultJudgement);
		summary_judge_result.addValue(resultJudgement);
		summary_selected_TPR.addValue(selected_TPR);
		summary_selected_positive.addValue(selectedPositive);
		summary_selected_count.addValue(selectedCount);
		
		if (total_TPR==0){//如果整体TPR为0则假定lift为1. 
			tpr_lift=1;
		}
		summary_lift.addValue(tpr_lift);
		summary_selectedShouyilv.addValue(selectedShouyilv);
		summary_totalShouyilv.addValue(totalShouyilv);
		System.out.println("Predicting finished!");
		
		//输出评估结果字符串
		//"整体正收益股数,整体股数,整体TPR,所选正收益股数,所选总股数,所选股TPR,提升率,所选股平均收益率,整体平均收益率,收益率差,是否改善\r\n";
		StringBuffer evalSummary=new StringBuffer();
		evalSummary.append(positive);
		evalSummary.append(",");
		evalSummary.append(totalCount);
		evalSummary.append(",");
		evalSummary.append(FormatUtility.formatPercent(total_TPR));
		evalSummary.append(",");
		evalSummary.append(selectedPositive);
		evalSummary.append(",");
		evalSummary.append(selectedCount);
		evalSummary.append(",");
		evalSummary.append(FormatUtility.formatPercent(selected_TPR));
		evalSummary.append(",");
		evalSummary.append(FormatUtility.formatDouble(tpr_lift));
		evalSummary.append(",");
		evalSummary.append(FormatUtility.formatPercent(selectedShouyilv));
		evalSummary.append(",");
		evalSummary.append(FormatUtility.formatPercent(totalShouyilv));
		evalSummary.append(",");
		evalSummary.append(FormatUtility.formatPercent(shouyilv_lift));
		evalSummary.append(",");
		evalSummary.append(resultJudgement);
		return evalSummary.toString();
	}


	public void outputClassifySummary() throws Exception{
		String selected_TPR_mean=FormatUtility.formatPercent(summary_selected_TPR.getMean());
		String selected_TPR_SD=FormatUtility.formatPercent(summary_selected_TPR.getStandardDeviation());
		String selected_TPR_SKW=FormatUtility.formatDouble(summary_selected_TPR.getSkewness());
		String selected_TPR_Kur=FormatUtility.formatDouble(summary_selected_TPR.getKurtosis());
		String lift_mean=FormatUtility.formatDouble(summary_lift.getMean());
		String selected_positive_sum=FormatUtility.formatDouble(summary_selected_positive.getSum(),8,0);
		String selected_count_sum=FormatUtility.formatDouble(summary_selected_count.getSum(),8,0);
		String selectedShouyilvMean = FormatUtility.formatPercent(summary_selectedShouyilv.getMean());
		String selectedShouyilvSD = FormatUtility.formatPercent(summary_selectedShouyilv.getStandardDeviation());
		String selectedShouyilvSKW = FormatUtility.formatDouble(summary_selectedShouyilv.getSkewness());
		String selectedShouyilvKUR = FormatUtility.formatDouble(summary_selectedShouyilv.getKurtosis());
		String totalShouyilvMean = FormatUtility.formatPercent(summary_totalShouyilv.getMean());
		String totalShouyilvSD = FormatUtility.formatPercent(summary_totalShouyilv.getStandardDeviation());
		String totalShouyilvSKW = FormatUtility.formatDouble(summary_totalShouyilv.getSkewness());
		String totalShouyilvKUR = FormatUtility.formatDouble(summary_totalShouyilv.getKurtosis());
		
		
		System.out.println("......................");
		System.out.println("......................");
		System.out.println("......................");
		System.out.println("===============================output summary===================================== for : "+getIdentifyName());
		System.out.println("Monthly selected_TPR mean: "+selected_TPR_mean+" standard deviation="+selected_TPR_SD+" Skewness="+selected_TPR_SKW+" Kurtosis="+selected_TPR_Kur);
		System.out.println("Monthly selected_LIFT mean : "+lift_mean);
		System.out.println("Monthly selected_positive summary: "+selected_positive_sum);
		System.out.println("Monthly selected_count summary: "+selected_count_sum);
		System.out.println("Monthly selected_shouyilv average: "+selectedShouyilvMean+" standard deviation="+selectedShouyilvSD+" Skewness="+selectedShouyilvSKW+" Kurtosis="+selectedShouyilvKUR);
		System.out.println("Monthly total_shouyilv average: "+totalShouyilvMean+" standard deviation="+totalShouyilvSD+" Skewness="+totalShouyilvSKW+" Kurtosis="+totalShouyilvKUR);
		if(summary_selected_count.getSum()>0){
			System.out.println("mixed selected positive rate: "+FormatUtility.formatPercent(summary_selected_positive.getSum()/summary_selected_count.getSum()));
		}
		System.out.println("Monthly summary_judge_result summary: good number= "+FormatUtility.formatDouble(summary_judge_result.getSum(),8,0) + " bad number=" +FormatUtility.formatDouble((summary_judge_result.getN()-summary_judge_result.getSum()),8,0));
		System.out.println("===============================end of summary=====================================for : "+getIdentifyName());
		System.out.println("......................");
		System.out.println("......................");
		System.out.println("......................");

	}
	
	public String getModelFileName() {
		return model_filename;
	}
	
	public void setModelFileName(String modelFileName) {
		model_filename=modelFileName;
	}
	
	//生成回测时使用的model文件和eval文件名称
	public void generateModelAndEvalFileName(String yearSplit,String policySplit) {
		String modelFile=this.WORK_PATH+this.WORK_FILE_PREFIX +"-"+classifierName+ "-" + yearSplit + MA_PREFIX + policySplit;
		setModelFileName(modelFile);
		setEvaluationFilename(modelFile+THRESHOLD_EXTENSION);
	}
	
	
	//缺省读取model_filename的文件模型，但对于某些无法每月甚至每年生成模型的分类算法，在子类里override这个方法
	public Classifier loadModel(String yearSplit, String policySplit) throws Exception{
		return loadModelFromFile();
	}


	protected Classifier loadModelFromFile() throws Exception{
		String modelFileName=this.getModelFileName()+ MODEL_FILE_EXTENSION;
		try{
			@SuppressWarnings("unchecked")
			Vector<Object> v = (Vector<Object>) SerializationHelper.read(modelFileName);
			Classifier model = (Classifier) v.get(0);
			System.out.println("Classifier Model Loaded: "+ modelFileName);
			return model;
		} catch(IOException e){
			System.err.println("error when loading: "+modelFileName);
			throw e;
		}
	}	

	protected void saveModelToFiles(Classifier model, Vector<Object> v)
			throws Exception {
		String modelFileName=this.getModelFileName();

		try{
			FileUtility.write(modelFileName+TXT_EXTENSION, model.toString(), "utf-8");
			SerializationHelper.write(modelFileName+MODEL_FILE_EXTENSION, v);
			//		SerializationHelper.write(modelFileName+WEKA_MODEL_EXTENSION, model);
			System.out.println("models saved to :"+ modelFileName);
		} catch(IOException e){
			System.err.println("error when saving: "+modelFileName);
			throw e;
		}
	}
	
	// arffType="train" or "test" or "eval"
	public void saveArffFile(Instances trainingData,String arffType,String yearSplit,String policySplit) throws IOException{
		String trainingFileName = this.WORK_PATH+this.WORK_FILE_PREFIX + " "+arffType+" " + yearSplit + MA_PREFIX+ policySplit + ARFF_EXTENSION;
		FileUtility.SaveDataIntoFile(trainingData, trainingFileName);
	}
	

	//对于父类来说，do nothing
	protected ThresholdData processThresholdData(ThresholdData eval){
		return eval;
	}
	
	//缺省返回classifierName，某些子类（比如MultiPCA）可能会返回其他名字，这是为了保存文件时区分不同参数用
	public String getIdentifyName(){
		return classifierName;
	}

	public void setWorkPathAndCheck(String apath){
		WORK_PATH=apath;
		FileUtility.mkdirIfNotExist(WORK_PATH);
	}
}
