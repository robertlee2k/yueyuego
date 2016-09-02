package yueyueGo.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import yueyueGo.ClassifyUtility;
import yueyueGo.MyAttributionSelectorWithPCA;
import yueyueGo.NominalClassifier;
import yueyueGo.RuntimeParams;

//1.按年使用模型和评估文件
//
////boost特有参数
//int boost_iteration=10;
////j48特有参数
//int leafMinObjNum=300;
//m_noCaculationAttrib=false; //使用计算字段
//m_policySubGroup = new String[]{"5","10","20","30","60" };
//m_skipTrainInBacktest = true;
//m_skipEvalInBacktest = true;
//m_sepeperate_eval_HS300=false;//不单独评估
//m_seperate_classify_HS300=false;
//EVAL_RECENT_PORTION = 0.9; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
//SAMPLE_LOWER_LIMIT =new double[] { 0.04, 0.05, 0.06, 0.07, 0.08 }; // 各条均线选择样本的下限
//SAMPLE_UPPER_LIMIT =new double[] { 0.07, 0.08, 0.11, 0.12, 0.13 }; // 各条均线选择样本的上限
//TP_FP_RATIO_LIMIT=new double[] { 1.8, 1.7, 1.3, 1.1, 0.9};//选择样本阀值时TP FP RATIO从何开始
//TP_FP_BOTTOM_LINE=0.8; //TP/FP的下限
//DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
//===============================output summary===================================== for : adaboost
//Monthly selected_TPR mean: 32.88% standard deviation=22.01% Skewness=0.47 Kurtosis=-0.64
//Monthly selected_LIFT mean : 1.1
//Monthly selected_positive summary: 15,673
//Monthly selected_count summary: 40,750
//Monthly selected_shouyilv average: 1.21% standard deviation=6.73% Skewness=2.9 Kurtosis=14.82
//Monthly total_shouyilv average: 0.98% standard deviation=6.13% Skewness=3.04 Kurtosis=15.43
//mixed selected positive rate: 38.46%
//Monthly summary_judge_result summary: good number= 296 bad number=219
//===============================end of summary=====================================for : adaboost
//thredhold----------0%
//result changed because of reference data not matched=5123 while good change number=3562
//good ratio=69.53% average changed shouyilv=0.89% @ thredhold=0.00%
//number of records for full market=1412480
//shouyilv average for full market=0.80%
//selected shouyilv average for full market =2.15% count=35740
//selected shouyilv average for hs300 =0.80% count=4230
//selected shouyilv average for zz500 =1.41% count=8276
//thredhold----------1%
//result changed because of reference data not matched=12659 while good change number=8750
//good ratio=69.12% average changed shouyilv=1.38% @ thredhold=1.00%
//number of records for full market=1412480
//shouyilv average for full market=0.80%
//selected shouyilv average for full market =2.26% count=28204
//selected shouyilv average for hs300 =1.14% count=3281
//selected shouyilv average for zz500 =1.54% count=6515
//thredhold----------2%
//result changed because of reference data not matched=21499 while good change number=15253
//good ratio=70.95% average changed shouyilv=1.76% @ thredhold=2.00%
//number of records for full market=1412480
//shouyilv average for full market=0.80%
//selected shouyilv average for full market =2.24% count=19364
//selected shouyilv average for hs300 =1.44% count=2231
//selected shouyilv average for zz500 =1.58% count=4345
public class AdaboostClassifier extends NominalClassifier {

	protected int leafMinObjNum; 	//j48树最小节点叶子数
	protected int divided; //将trainingData分成多少份
	protected int boost_iteration; 	//boost特有参数

	@Override
	protected void initializeParams() {
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = true;
		
		classifierName="adaboost";
		setWorkPathAndCheck(RuntimeParams.getNOMINAL_CLASSIFIER_DIR()+this.getIdentifyName()+"\\");

		leafMinObjNum=300; 	//j48树最小节点叶子数
		divided=300; //将trainingData分成多少份
		boost_iteration=10; 	//boost特有参数
		
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
		
		cachedOldClassInstances=null; 
		//设置基础的J48 classifier参数
		J48 model=ClassifyUtility.prepareJ48(train.numInstances(),leafMinObjNum,divided);

		AdaBoostM1 adaboost=new  AdaBoostM1();
		adaboost.setClassifier(model);
		adaboost.setNumIterations(boost_iteration);
		adaboost.setDebug(true);
		
		MyAttributionSelectorWithPCA classifier = new MyAttributionSelectorWithPCA();
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
		if(yearSplit.length()==6){
			int inputMonth=Integer.parseInt(yearSplit.substring(4,6));
			//TODO 
			if ((inputYear==2016) && inputMonth>=6){
				halfYearString="06";
			}
		}
		String filename=this.WORK_PATH+this.WORK_FILE_PREFIX +"-"+classifierName+ "-" + inputYear +halfYearString+ MA_PREFIX + policySplit;
		
		this.setModelFileName(filename);
		
//		// 全年用同一的eval
//		this.setEvaluationFilename(filename+".eval");
	
		return loadModelFromFile();
	}
}
