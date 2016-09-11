package yueyueGo.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import yueyueGo.NominalClassifier;
import yueyueGo.utility.ClassifyUtility;
import yueyueGo.utility.AppContext;
import yueyueGo.utility.FormatUtility;

// 按月分析（全年使用同一模型和评估值）
//number of results merged and processed: 1412480
//result changed because of reference data not matched=18985 while good change number=13372
// good ratio=70.43% average changed shouyilv=2.25% @ shouyilv thredhold= /1.00% /2.00% /3.00% /3.00% /3.00% /
//number of records for full market=1412480
//shouyilv average for full market=0.80%
//selected shouyilv average for full market =1.77% count=26492
//selected shouyilv average for hs300 =-0.19% count=2900
//selected shouyilv average for zz500 =0.55% count=5545
public class BaggingJ48 extends NominalClassifier {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4539124562517081057L;
	protected boolean useMultiPCA;
	protected int bagging_iteration;
	protected int leafMinObjNum;
	protected int divided;
	
	@Override
	protected void initializeParams() {
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = true;
		m_policySubGroup = new String[]{"5","10","20","30","60" };
				
		classifierName="baggingJ48";
		
		m_positiveLine=0.03; //二分类class的分界值，这个需要在workpath之前设

		
		useMultiPCA=true; //bagging 内的每个模型自己有单独的PCA
		setWorkPathAndCheck(AppContext.getNOMINAL_CLASSIFIER_DIR()+this.getIdentifyName()+"\\");
		
		m_noCaculationAttrib=false; //使用计算字段
		bagging_iteration=10;	//bagging特有参数
		leafMinObjNum=300; 	//j48树最小节点叶子数
		divided=300; //将trainingData分成多少份
		
		if (m_positiveLine==0.03) { //收益率3%为正负阀值时的参数
			EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
			SAMPLE_LOWER_LIMIT =new double[] { 0.04, 0.05, 0.06, 0.07, 0.08 }; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.07, 0.08, 0.11, 0.12, 0.13 }; // 各条均线选择样本的上限
			TP_FP_RATIO_LIMIT=new double[] { 1.2, 1.1, 1.0, 0.9, 0.8};//选择样本阀值时TP FP RATIO从何开始
			TP_FP_BOTTOM_LINE=0.5; //TP/FP的下限
			DEFAULT_THRESHOLD=0.5; // 找不出threshold时缺省值。
		}else {// 缺省值
			EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
			SAMPLE_LOWER_LIMIT =new double[] { 0.04, 0.05, 0.06, 0.07, 0.08 }; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.07, 0.08, 0.11, 0.12, 0.13 }; // 各条均线选择样本的上限
			TP_FP_RATIO_LIMIT=new double[] { 1.8, 1.7, 1.3, 1.1, 0.9};//选择样本阀值时TP FP RATIO从何开始
			TP_FP_BOTTOM_LINE=0.8; //TP/FP的下限
			DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
		}
		
	}

	
	@Override
	protected Classifier buildModel(Instances train) throws Exception {
		m_cachedOldClassInstances=null; 
		
		//设置基础的m5p classifier参数
		J48 model=ClassifyUtility.prepareJ48(train.numInstances(),leafMinObjNum,divided);

		if (useMultiPCA==true){
			int bagging_samplePercent=70;//bagging sample 取样率
			return ClassifyUtility.buildBaggingWithMultiPCA(train,model,bagging_iteration,bagging_samplePercent);
		}else{
			int bagging_samplePercent=100;// PrePCA算袋外误差时要求percent都为100
			return ClassifyUtility.buildBaggingWithSinglePCA(train,model,bagging_iteration,bagging_samplePercent);
		}
	}

	
	@Override
	public Classifier loadModel(String yearSplit, String policySplit) throws Exception{
		//这是单独准备的模型，模型文件是按年读取，但evaluation文件不变仍按月
		int inputYear=Integer.parseInt(yearSplit.substring(0,4));
		
		//为特定年份下半年增加一个模型，提高准确度
		String halfYearString="";
//		if(yearSplit.length()==6){
//			int inputMonth=Integer.parseInt(yearSplit.substring(4,6));
//			//TODO 
//			if ((inputYear==2016) && inputMonth>=6){
//				halfYearString="06";
//			}
//		}
		String filename=this.WORK_PATH+this.WORK_FILE_PREFIX +"-"+classifierName+ "-" + inputYear +halfYearString+ MA_PREFIX + policySplit;//如果使用固定模型
		
		this.setModelFileName(filename);
//		// 全年用同一的eval
//		this.setEvaluationFilename(filename+".eval");
	
		return loadModelFromFile();
	}	
	
	
	@Override
	public String getIdentifyName(){
		String idenString=classifierName;
		
		if (m_positiveLine!=0){ //如果用自定义的标尺线区分Class的正负，则特别标记
			idenString+="("+FormatUtility.formatDouble(m_positiveLine)+")";
		}
		
		if (useMultiPCA==true){
			idenString +="-multiPCA";
		}else{
			idenString +="-singlePCA";
		}

		return idenString;
	}
}
