package yueyueGo.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import yueyueGo.MyAttributionSelectorWithPCA;
import yueyueGo.NominalClassifier;

public class AdaboostClassifier extends NominalClassifier {
	

	public AdaboostClassifier() {
		super();
		classifierName="adaboost";
		WORK_PATH =WORK_PATH+classifierName+"\\";
		
		m_noCaculationAttrib=false; //使用计算字段
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		m_skipTrainInBacktest = false;
		m_skipEvalInBacktest = false;
		m_sepeperate_eval_HS300=false;//单独评估
		m_seperate_classify_HS300=false;
		
		EVAL_RECENT_PORTION = 0.9; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
		SAMPLE_LOWER_LIMIT =new double[] { 0.04, 0.05, 0.06, 0.07, 0.08 }; // 各条均线选择样本的下限
		SAMPLE_UPPER_LIMIT =new double[] { 0.07, 0.08, 0.11, 0.12, 0.13 }; // 各条均线选择样本的上限
		TP_FP_RATIO_LIMIT=new double[] { 1.8, 1.7, 1.3, 1.1, 0.9};//选择样本阀值时TP FP RATIO从何开始
		TP_FP_BOTTOM_LINE=0.8; //TP/FP的下限
		DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
	}
		
	@Override
	protected Classifier buildModel(Instances train) throws Exception {
		//boost特有参数
		int boost_iteration=10;
		//j48特有参数
		int leafMinObjNum=300;
		MyAttributionSelectorWithPCA classifier = new MyAttributionSelectorWithPCA();

		cachedOldClassInstances=null; 
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
		
		AdaBoostM1 adaboost=new  AdaBoostM1();
		adaboost.setClassifier(model);
		adaboost.setNumIterations(boost_iteration);
		adaboost.setDebug(true);

		classifier.setClassifier(adaboost);
		classifier.setDebug(true);
		classifier.buildClassifier(train);
		System.out.println("finish buiding adaboost model.");

		return classifier;
	}
	@Override
	public Classifier loadModel(String yearSplit, String policySplit) throws Exception{
		//这是为MLP单独准备的模型，模型文件是按年读取，但evaluation文件不变仍按月
		int inputYear=Integer.parseInt(yearSplit.substring(0,4));

		//为特定年份下半年增加一个模型，提高准确度
		String halfYearString="";

		String filename=this.WORK_PATH+this.WORK_FILE_PREFIX +"-"+this.classifierName+ "-" + inputYear +halfYearString+ MA_PREFIX + policySplit;
		
		this.setModelFileName(filename);
		
//		// 全年用同一的eval
//		this.setEvaluationFilename(filename+".eval");
	
		return loadModelFromFile();
	}
}
