package yueyueGo;

import java.io.Serializable;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import weka.classifiers.Classifier;
import weka.core.SerializedObject;
import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.dataProcessor.InstanceHandler;
import yueyueGo.databeans.DataInstance;
import yueyueGo.databeans.DataInstances;
import yueyueGo.databeans.GeneralAttribute;
import yueyueGo.databeans.GeneralInstance;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.utility.AppContext;
import yueyueGo.utility.ClassifySummaries;
import yueyueGo.utility.EvaluationBenchmark;
import yueyueGo.utility.EvaluationConfDefinition;
import yueyueGo.utility.EvaluationParams;
import yueyueGo.utility.FileUtility;
import yueyueGo.utility.FormatUtility;
import yueyueGo.utility.NumericThresholdCurve;
import yueyueGo.utility.ThresholdData;

/**
 * @author robert
 * 所有分类器的基类
 */
/**
 * @author robert
 *
 */
public abstract class BaseClassifier implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5895562408723104016L;
	//统一常量
	public static final String MA_PREFIX = " MA ";
	public static final String ARFF_EXTENSION = ".arff";
	
	//切分构建模型和评估数据的模式常量定义
	public static final int NO_SEPERATE_DATA_FOR_EVAL=0; //不需要评估数据（这是legacy的做法，目前不常用）
	public static final int USE_YEAR_DATA_FOR_EVAL=12; //使用倒推一年的数据作为模型评估数据，之前用于的构建模型（缺省值）
	public static final int USE_HALF_YEAR_DATA_FOR_EVAL=6;//使用倒推半年的数据作为模型评估数据，之前用于的构建模型
	public static final int USE_NINE_MONTHS_DATA_FOR_EVAL=9;//使用倒推半年的数据作为模型评估数据，之前用于的构建模型
	
	//名称
	public String classifierName;
	
	//子类定义的工作路径
	protected String WORK_PATH ;
	protected String WORK_FILE_PREFIX;
	protected int m_modelEvalFileShareMode; //model文件和Eval的共享模式。
	protected int m_modelDataSplitMode;//切分构建模型和评估数据的模式
	
	protected ModelStore m_modelStore; //model 和 eval的持久化封装类类
	
	public boolean m_noCaculationAttrib;  //加入的计算字段与否
	public boolean m_removeSWData;  //是否需要删除行业数据
	
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
		m_removeSWData=false;  //缺省情况下，不删除申万行业数据（在子类中覆盖）
		modelArffFormat=ArffFormat.EXT_FORMAT; //缺省使用扩展arff
		m_modelEvalFileShareMode=ModelStore.SEPERATE_MODEL_AND_EVAL; //model文件和Eval的共享模式,缺省为 回测时按yearsplit和policysplit分割使用model和eval文件
		m_modelDataSplitMode=USE_YEAR_DATA_FOR_EVAL;//缺省使用倒推一年的数据作为模型评估数据，之前用于的构建模型
		WORK_FILE_PREFIX= "extData2005-2016";
		initializeParams();		// 留给子类的初始化参数函数
		setWorkPathThenCheck(); //根据参数设置工作路径

		initEvaluationConfDefinition(); //初始化evaluation的常量定义
	}
	
	//一系列需要子类实现的抽象方法
	protected abstract void initializeParams();
	protected abstract Classifier buildModel(GeneralInstances trainData) throws Exception;
	protected abstract GeneralInstances getROCInstances(GeneralInstances evalData, Classifier model) throws Exception; 
	protected abstract double classify(Classifier model,GeneralInstance curr) throws Exception ;
	
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
	
	public Classifier trainData(GeneralInstances train) throws Exception {
		Classifier model=buildModel(train);
		// save model + header
		m_modelStore.setModel(model);
		m_modelStore.setModelFormat(new DataInstances(train, 0));
		m_modelStore.saveModelToFiles();
		System.out.println("Training finished!");
		return model;
	}
	

	
	//评估模型
	public void evaluateModel(GeneralInstances evalData,String policySplit) throws Exception{

		
		Classifier model =m_modelStore.loadModelFromFile();
		GeneralInstances header =m_modelStore.getModelFormat();
		GeneralInstances evalFormat=new DataInstances(evalData,0);
		//验证评估数据格式是否一致
		String verify=verifyDataFormat(evalFormat, header);
		if (verify!=null){
			throw new Exception("attention! model and evaluation data structure is not the same. Here is the difference: "+verify);
		}
		

//		eval.evaluateModel(model, evalData); // evaluate on the sample data to get threshold
//		ThresholdCurve tc = new ThresholdCurve();
//		int classIndex = 1;
//		Instances predictions=tc.getCurve(eval.predictions(), classIndex);
//		FileUtility.SaveDataIntoFile(predictions, this.WORK_PATH+"\\ROCresult-withTrain.arff");
		
		//基于evalData 生成评估模型的基准值
		boolean isNominal=false;
		if (this instanceof NominalClassifier){
			isNominal=true;
		}
		EvaluationBenchmark benchmark=new EvaluationBenchmark(evalData, isNominal);
		
		System.out.println(" try to get best threshold for model...");
		EvaluationParams evalParams=getEvaluationInstance(policySplit);
		ThresholdData thresholdData = doModelEvaluation(benchmark,evalData, model, evalParams);
		thresholdData.setTargetYearSplit(m_modelStore.getTargetYearSplit());
		ThresholdData.saveEvaluationToFile(m_modelStore.getEvalFileName(), thresholdData);

	}
	
	//具体的模型评估方法
	private ThresholdData doModelEvaluation(EvaluationBenchmark benchmark ,GeneralInstances evalData,Classifier model,EvaluationParams evalParams)
			throws Exception {

		GeneralInstances result = getROCInstances(evalData, model);
//		FileUtility.SaveDataIntoFile(result, this.WORK_PATH+"\\ROCresult.arff");
		int round=1;

		ThresholdData thresholdData=null;
		
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

		return thresholdData;
	}
	
	//无法根据liftup获取阀值时，缺省用最小的sampleSize处阀值
	private ThresholdData computeDefaultThresholds(EvaluationParams evalParams, GeneralInstances result){
		double sample_limit=evalParams.getLower_limit(); 
		double sampleSize;
		double threshold=-100;
		GeneralAttribute att_threshold = result.attribute(NumericThresholdCurve.THRESHOLD_NAME);
		GeneralAttribute att_samplesize = result.attribute(NumericThresholdCurve.SAMPLE_SIZE_NAME);

		for (int i = 0; i < result.numInstances(); i++) {
			GeneralInstance curr = result.instance(i);
			sampleSize = curr.value(att_samplesize); // to get sample range
			if (FormatUtility.compareDouble(sampleSize,sample_limit)==0) {
				threshold = curr.value(att_threshold);
				break;
			}
		}
		if (threshold==-100){
			System.err.println("seems error! cannot get threshold at sample_limit="+sample_limit);
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
	
	//具体的模型阀值计算方法，找不到阀值的时候返回null对象
	private ThresholdData computeThresholds(double tp_fp_ratio,EvaluationParams evalParams, GeneralInstances result) {

		double sample_limit=evalParams.getLower_limit(); 
		double sample_upper=evalParams.getUpper_limit();

		double thresholdBottom = 0.0;
		double lift_max = 0.0;
		double lift_max_tp=0.0;
		double lift_max_fp=0.0;
		double lift_max_sample=0.0;
		
		double finalSampleSize = 0.0;
		double sampleSize = 0.0;
		double tp = 0.0;
		double fp = 0.0;
		double final_tp=0.0;
		double final_fp=0.0;
		GeneralAttribute att_tp = result.attribute(NumericThresholdCurve.TRUE_POS_NAME);
		GeneralAttribute att_fp = result.attribute(NumericThresholdCurve.FALSE_POS_NAME);
		GeneralAttribute att_lift = result.attribute(NumericThresholdCurve.LIFT_NAME);
		GeneralAttribute att_threshold = result.attribute(NumericThresholdCurve.THRESHOLD_NAME);
		GeneralAttribute att_samplesize = result.attribute(NumericThresholdCurve.SAMPLE_SIZE_NAME);


		for (int i = 0; i < result.numInstances(); i++) {
			GeneralInstance curr = result.instance(i);
			sampleSize = curr.value(att_samplesize); // to get sample range
			if (sampleSize >= sample_limit && sampleSize <=sample_upper) {
				tp = curr.value(att_tp);
				fp = curr.value(att_fp);
				
				//统计该范围内lift最大的值是多少（仅为输出用）
				double current_lift=curr.value(att_lift);
				if (current_lift>lift_max){
					lift_max=current_lift;
					lift_max_tp=tp;
					lift_max_fp=fp;
					lift_max_sample=sampleSize;
				}
				
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
			double max_tp_fp_ratio=Double.NaN;
			if (lift_max_fp>0){
				max_tp_fp_ratio=lift_max_tp/lift_max_fp;
			}
			System.out.println("###possible lift max in range is : " + FormatUtility.formatDouble(lift_max) + "@ sample="+FormatUtility.formatDouble(lift_max_sample)+" where tp="+lift_max_tp+" /fp="+lift_max_fp);
			System.out.println("### max tp fp ratio="+max_tp_fp_ratio+ " while trying threshold="+tp_fp_ratio+ " isNormal="+(max_tp_fp_ratio<tp_fp_ratio));
		}

		return thresholdData;
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

		
		ThresholdData thresholdData=ThresholdData.loadDataFromFile(m_modelStore.getEvalFileName());
		if (yearSplit.equals(thresholdData.getTargetYearSplit())==false){
			throw new Exception("error! threshold data is inconsistent, target yearSplit="+yearSplit+" while yearSplit in file is "+thresholdData.getTargetYearSplit()+" file name="+m_modelStore.getEvalFileName());
		}else{
			System.out.println("eval File verified for target yearsplit "+thresholdData.getTargetYearSplit());
		}
		double thresholdMin=thresholdData.getThresholdMin();
		double thresholdMax=thresholdData.getThresholdMax();


		// read classify model and header
		Classifier model =m_modelStore.loadModelFromFile();
		GeneralInstances header =m_modelStore.getModelFormat();
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
		
		//开始用分类模型和阀值进行预测
		System.out.println("actual -> predicted....... ");
		

		int testInstancesNum=test.numInstances();
		DescriptiveStatistics totalPositiveShouyilv=new DescriptiveStatistics();
		DescriptiveStatistics totalNegativeShouyilv=new DescriptiveStatistics();
		DescriptiveStatistics selectedPositiveShouyilv=new DescriptiveStatistics();
		DescriptiveStatistics selectedNegativeShouyilv=new DescriptiveStatistics();			
		
		
		
		for (int i = 0; i < testInstancesNum; i++) {
			GeneralInstance curr = test.instance(i);
			double pred=classify(model,curr);  //调用子类的分类函数
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
		
		
		double startPercent=thresholdData.getStartPercent();
		boolean isGuessed=thresholdData.isGuessed();
		String defaultThresholdUsed=" ";
		if (isGuessed){
			defaultThresholdUsed="Y";
		}		
		if ("".equals(yearSplit) ){
			//这是预测每日数据时，没有实际收益率数据可以做评估 (上述逻辑会让所有的数据都进入negative的分支）
			classifySummaries.savePredictSummaries(policySplit,totalNegativeShouyilv,selectedNegativeShouyilv);
			String evalSummary="( with params: thresholdMin="+FormatUtility.formatDouble(thresholdMin,0,3)+" , startPercent="+FormatUtility.formatPercent(startPercent/100)+" ,defaultThresholdUsed="+defaultThresholdUsed+" )\r\n";  //输出评估结果及所使用阀值及期望样本百分比
			classifySummaries.appendEvaluationSummary(evalSummary);

		}else{
			//这是进行历史回测数据时，根据历史收益率数据进行阶段评估
			classifySummaries.computeClassifySummaries(yearSplit,policySplit,totalPositiveShouyilv,totalNegativeShouyilv,selectedPositiveShouyilv,selectedNegativeShouyilv);
			String evalSummary=","+FormatUtility.formatDouble(thresholdMin,0,3)+","+FormatUtility.formatPercent(startPercent/100)+","+defaultThresholdUsed+"\r\n";  //输出评估结果及所使用阀值及期望样本百分比
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

	
	protected String verifyDataFormat(GeneralInstances test, GeneralInstances header) throws Exception {
//		//在使用旧格式时，如果有使用旧字段名的模型，试着将其改名后使用
//		if (modelArffFormat==ArffFormat.LEGACY_FORMAT){
//			header=ArffFormat.renameOldArffName(header);
//		}
		return BaseInstanceProcessor.compareInstancesFormat(test, header);
	}

	//初始化回测创建模型时使用的modelStore对象
	public void initModelStore(String targetYearSplit,String policySplit) {
		//这里build model的数据已变为当前周期前推一段时间的数据，
		//比如若按年取评估数据，如果是2010XX.mdl 则取2009年XX月之前的数据build， 剩下的一年数据做评估用
		String modelYearSplit=getModelYearSplit(targetYearSplit);
		//创建模型时应该把200801变为2008 （这是历史沿革习惯）
		if (modelYearSplit.length()==6){
			int inputMonth=Integer.parseInt(modelYearSplit.substring(4,6)); 
			if (inputMonth==1){
				modelYearSplit=modelYearSplit.substring(0,4);
			}
		}
		String modelFileName=ModelStore.concatModeFilenameString(modelYearSplit, policySplit, this.WORK_PATH+this.WORK_FILE_PREFIX, this.classifierName);
		ModelStore modelStore=new ModelStore(targetYearSplit,modelFileName,modelFileName+ModelStore.THRESHOLD_EXTENSION);
		m_modelStore=modelStore;
	}
	
	//找到回测评估、预测时应该使用modelStore对象（主要为获取model文件和eval文件名称）
	//此类可以在子类中被覆盖（通过把yearsplit的值做处理，实现临时指定使用某个模型，可以多年使用一个模型，也可以特殊指定某年月使用某模型）
	public void locateModelStore(String targetYearSplit,String policySplit) {
		//这里build model的数据已变为当前周期前推一段时间的数据，
		//比如若按年取评估数据，如果是2010XX.mdl 则取2009年XX月之前的数据build， 剩下的一年数据做评估用
		String modelYearSplit=getModelYearSplit(targetYearSplit);
		ModelStore modelStore=new ModelStore(targetYearSplit,modelYearSplit,policySplit,this.WORK_PATH+this.WORK_FILE_PREFIX, this.classifierName,this.m_modelEvalFileShareMode);
		m_modelStore=modelStore;
	}

	/*
	 * 获取用于评估阀值的yearSplit （缺省情况下前推一年）
	 * 这里将全量数据分解为model的构建数据及评测数据
	 * 即根据当前yearSplit前推一段时间，将其切分成构建模型数据和评测数据（具体前推多少时间由model定义决定）
	 * 比如，若是前推一年的， 如果是2010XX.mdl 则取2009年XX月之前的数据build，
	 * 剩下的一年数据（2009XX到2010XX，后者不包含）做评估用
	 * 如果是2010.mdl，则取2009年01月之前的数据build，2009当年数据做评估用
	 * 
	 */
	public String getModelYearSplit(String yearSplit){
		//找到回测创建评估预测时应该使用modelStore对象（主要为获取model文件和eval文件名称）-- 评估时创建的mdl并不是当前年份的，而是前推一年的
		String modelYearSplit=null;
		switch (this.m_modelDataSplitMode) {
		case NO_SEPERATE_DATA_FOR_EVAL: //使用全量数据构建模型（不常用）
			modelYearSplit=yearSplit; 
			break;
		case USE_YEAR_DATA_FOR_EVAL:
			modelYearSplit=getLastYearSplit(yearSplit);			
			break;
		case USE_HALF_YEAR_DATA_FOR_EVAL:
		case USE_NINE_MONTHS_DATA_FOR_EVAL:
			modelYearSplit=getNMonthsForYearSplit(this.m_modelDataSplitMode,yearSplit);			
			break;
		}
		System.out.println("目标日期="+yearSplit+" 模型数据切分日期="+modelYearSplit);
		return modelYearSplit;
	}
	
	//	当前周期前推一年的年分隔线，比如 如果是2010XX 则返回2009年XX月（这是为了取不在trainingData里的evalData）
	//TODO 可以用下面的函数代替
	private static String getLastYearSplit(String yearSplit){
		int lastPeriod=0;
		int limit=2007; //回测模型的起始点， 在这之前无数据
		lastPeriod=Integer.valueOf(yearSplit).intValue();
		if (yearSplit.length()==4){ //最后一位-1 （2010-1=2009）
			lastPeriod=lastPeriod-1;
			if (lastPeriod<limit) 
				lastPeriod=limit;
		}else {//最后三位-1 （201001-100=200901）
			lastPeriod=lastPeriod-100;
			if (lastPeriod<limit*100+1) 
				lastPeriod=limit*100+1;
		}
		return String.valueOf(lastPeriod);
	}
	
	//	当前周期前推六个月的分隔线，比如 如果是201003 则返回200909
	private static String getNMonthsForYearSplit(int nMonths,String yearSplit){
		int limit=2007; //回测模型的起始点， 在这之前无数据
		int lastPeriod=0;
		if (yearSplit.length()==4){ //最后一位-1 （2010-1=2009）再拼接一个07
			lastPeriod=Integer.valueOf(yearSplit).intValue();
			lastPeriod=lastPeriod-1;
			if (lastPeriod<limit) {
				lastPeriod=limit;
			}
			lastPeriod=lastPeriod*100+7;
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
			if (lastPeriod<limit*100+1){ 
				lastPeriod=limit*100+1;
			}
		}
		return String.valueOf(lastPeriod);
	}
	
	//生成日常预测时使用的model文件和eval文件名称
	public void setModelStore(ModelStore m){
		m_modelStore=m;
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
		System.out.println("*************************");
		System.out.println("ClassifyIdentity="+this.getIdentifyName());
		System.out.println("m_skipTrainInBacktest="+this.m_skipTrainInBacktest);
		System.out.println("m_skipEvalInBacktest="+this.m_skipEvalInBacktest);
		System.out.println("m_noCaculationAttrib="+m_noCaculationAttrib);
		System.out.println("m_removeSWData="+m_removeSWData);
		System.out.println("m_positiveLine="+m_positiveLine);
		

		System.out.println("m_modelDataSplitMode="+m_modelDataSplitMode);
		System.out.println("m_modelEvalFileShareMode="+m_modelEvalFileShareMode);
		System.out.println("modelArffFormat="+modelArffFormat);
		System.out.println(m_evalConf.showEvaluationParameters());
	    System.out.println("*************************");
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
}
