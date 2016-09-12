package yueyueGo.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import yueyueGo.ModelStore;
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


//1 20160912 
//===============================output summary===================================== for : baggingJ48(0.03)-multiPCA
//Monthly selected_TPR mean: 17.40% standard deviation=21.86% Skewness=1.57 Kurtosis=2.21
//Monthly selected_LIFT mean : 1.11
//Monthly selected_positive summary: 10,709
//Monthly selected_count summary: 41,764
//Monthly selected_shouyilv average: 0.18% standard deviation=6.18% Skewness=2.71 Kurtosis=13.71
//Monthly total_shouyilv average: 0.98% standard deviation=6.13% Skewness=3.04 Kurtosis=15.43
//mixed selected positive rate: 25.64%
//Monthly summary_judge_result summary: good number= 243 bad number=272
//===============================end of summary=====================================for : baggingJ48(0.03)-multiPCA
//m_modelEvalFileShareMode=ModelStore.YEAR_SHARED_MODEL_AND_EVAL; //覆盖父类，设定模型和评估文件的共用模式
//EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
//SAMPLE_LOWER_LIMIT =new double[] { 0.04, 0.05, 0.06, 0.07, 0.08 }; // 各条均线选择样本的下限
//SAMPLE_UPPER_LIMIT =new double[] { 0.07, 0.08, 0.11, 0.12, 0.13 }; // 各条均线选择样本的上限
//TP_FP_RATIO_LIMIT=new double[] { 1.2, 1.1, 1.0, 0.9, 0.8};//选择样本阀值时TP FP RATIO从何开始
//TP_FP_BOTTOM_LINE=0.5; //TP/FP的下限
//DEFAULT_THRESHOLD=0.5; // 找不出threshold时缺省值。
//result changed because of reference data not matched=6288 while good change number=4195
//good ratio=66.71% average changed shouyilv=1.77% @ shouyilv thredhold= /0.00% /0.00% /0.00% /0.00% /0.00% /
//number of records for full market=1412480
//shouyilv average for full market=0.80%
//selected shouyilv average for full market =1.55% count=35476
//selected shouyilv average for hs300 =-0.60% count=4504
//selected shouyilv average for zz500 =0.12% count=7940

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
			m_modelEvalFileShareMode=ModelStore.YEAR_SHARED_MODEL_AND_EVAL; //覆盖父类，设定模型和评估文件的共用模式
			EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
			SAMPLE_LOWER_LIMIT =new double[] { 0.04, 0.05, 0.06, 0.07, 0.08 }; // 各条均线选择样本的下限
			SAMPLE_UPPER_LIMIT =new double[] { 0.07, 0.08, 0.11, 0.12, 0.13 }; // 各条均线选择样本的上限
			TP_FP_RATIO_LIMIT=new double[] { 1.2, 1.1, 1.0, 0.9, 0.8};//选择样本阀值时TP FP RATIO从何开始
			TP_FP_BOTTOM_LINE=0.5; //TP/FP的下限
			DEFAULT_THRESHOLD=0.5; // 找不出threshold时缺省值。
		}else {// 缺省值
			m_modelEvalFileShareMode=ModelStore.YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
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
