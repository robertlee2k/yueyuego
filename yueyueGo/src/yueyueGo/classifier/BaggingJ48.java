package yueyueGo.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import yueyueGo.MyAttributionSelectorWithPCA;
import yueyueGo.NominalClassifier;

public class BaggingJ48 extends NominalClassifier {
	boolean useMultiPCA=true; //bagging 内的每个模型自己有单独的PCA
	int bagging_iteration=10;	//bagging特有参数
	int leafMinObjNum=300; 	//j48特有参数
	
	public BaggingJ48() {
		super();
		classifierName = "baggingJ48";
		WORK_PATH =WORK_PATH+this.getIdentifyName()+"\\";
		m_skipTrainInBacktest = false;
		m_skipEvalInBacktest = false;
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		
		m_noCaculationAttrib=false; //使用计算字段

		EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
		SAMPLE_LOWER_LIMIT =new double[] { 0.04, 0.05, 0.06, 0.07, 0.08 }; // 各条均线选择样本的下限
		SAMPLE_UPPER_LIMIT =new double[] { 0.07, 0.08, 0.11, 0.12, 0.13 }; // 各条均线选择样本的上限
		TP_FP_RATIO_LIMIT=new double[] { 1.8, 1.7, 1.3, 1.1, 0.9};//选择样本阀值时TP FP RATIO从何开始
		TP_FP_BOTTOM_LINE=0.8; //TP/FP的下限
		DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
	}

	@Override
	protected Classifier buildModel(Instances train) throws Exception {
		if (useMultiPCA==true){
			return buildModelWithMultiPCA(train);
		}else{
			return buildModelWithSinglePCA(train);
		}
	}

	//bagging 内的每个模型自己有单独的PCA
	private  Classifier buildModelWithMultiPCA(Instances train) throws Exception {
		int bagging_samplePercent=70;//bagging sample 取样率
		
		//设置基础的J48 classifier参数
		J48 model = new J48();
		int minNumObj=train.numInstances()/300;
		if (minNumObj<leafMinObjNum){
			minNumObj=leafMinObjNum; //防止树过大
		}
		String batchSize=Integer.toString(minNumObj);
		model.setBatchSize(batchSize);
		model.setMinNumObj(minNumObj);
		model.setNumDecimalPlaces(6);
		model.setDebug(true);
		
		MyAttributionSelectorWithPCA classifier = new MyAttributionSelectorWithPCA();
		classifier.setDebug(true);
		classifier.setClassifier(model);

	    // set up the bagger and build the classifier
	    Bagging bagger = new Bagging();
	    bagger.setClassifier(classifier);
	    bagger.setNumIterations(bagging_iteration);
	    bagger.setNumExecutionSlots(3);
	    bagger.setBagSizePercent(bagging_samplePercent);
	    bagger.setCalcOutOfBag(false); //不计算袋外误差
	    bagger.setDebug(true);
	    bagger.buildClassifier(train);
		return bagger;
	}
	
	//bagging 之前使用PCA，bagging大家用同一的
	private  Classifier buildModelWithSinglePCA(Instances train) throws Exception {
		int bagging_samplePercent=100; // PrePCA算袋外误差时要求percent都为100
		
		//设置基础的J48 classifier参数
		J48 model = new J48();
		int minNumObj=train.numInstances()/300;
		if (minNumObj<leafMinObjNum){
			minNumObj=leafMinObjNum; //防止树过大
		}
		String batchSize=Integer.toString(minNumObj);
		model.setBatchSize(batchSize);
		model.setMinNumObj(minNumObj);
		model.setNumDecimalPlaces(6);
		model.setDebug(true);
	
	    // set up the bagger and build the classifier
	    Bagging bagger = new Bagging();
		bagger.setDebug(true);
		bagger.setClassifier(model);
	    bagger.setNumIterations(bagging_iteration);
	    bagger.setNumExecutionSlots(2);
	    bagger.setBagSizePercent(bagging_samplePercent);
	    bagger.setCalcOutOfBag(true); //计算袋外误差
	    bagger.setDebug(true);
	    
		MyAttributionSelectorWithPCA classifier = new MyAttributionSelectorWithPCA();	    
	    classifier.setDebug(true);
	    classifier.setClassifier(bagger);
	    classifier.buildClassifier(train);
	    
		return classifier;
	}
	
	@Override
	public Classifier loadModel(String yearSplit, String policySplit) throws Exception{
		//这是单独准备的模型，模型文件是按年读取，但evaluation文件不变仍按月
		int inputYear=Integer.parseInt(yearSplit.substring(0,4));
		
		//为特定年份下半年增加一个模型，提高准确度
		String halfYearString="";
		if(yearSplit.length()==6){
			int inputMonth=Integer.parseInt(yearSplit.substring(4,6));
			//TODO 
			if ((inputYear==2016) && inputMonth>=6){
				halfYearString="06";
			}
		}
		String filename=this.WORK_PATH+this.WORK_FILE_PREFIX +"-"+this.classifierName+ "-" + inputYear +halfYearString+ MA_PREFIX + policySplit;//如果使用固定模型
		
		this.setModelFileName(filename);

	
		return loadModelFromFile();
	}	
	
	
	@Override
	public String getIdentifyName(){
		String idenString;
		if (useMultiPCA==true){
			idenString =classifierName+"-multiPCA";
		}else{
			idenString =classifierName+"-singlePCA";
		}

		return idenString;
	}
}
