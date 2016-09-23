package yueyueGo.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import yueyueGo.ModelStore;
import yueyueGo.MyAttributionSelectorWithPCA;
import yueyueGo.NominalClassifier;
import yueyueGo.utility.AppContext;
import yueyueGo.utility.ClassifyUtility;

//1.  20160912结果
//  全市场10-50单元格 ，收益率优先年平均（非年化）21%/20%/18%/17%：累计净值 3.75、3.37、3.24、3.20
// 胜率优先的明显弱于收益率优先， 累计净值在2-3之间
//m_modelEvalFileShareMode=ModelStore.HALF_YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
//
//leafMinObjNum=300; 	//j48树最小节点叶子数
//divided=300; //将trainingData分成多少份
//boost_iteration=10; 	//boost特有参数
//
//m_noCaculationAttrib=false; //使用计算字段
//EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
//SAMPLE_LOWER_LIMIT =new double[] { 0.04, 0.05, 0.06, 0.07, 0.08 }; // 各条均线选择样本的下限
//SAMPLE_UPPER_LIMIT =new double[] { 0.07, 0.08, 0.11, 0.12, 0.13 }; // 各条均线选择样本的上限
//TP_FP_RATIO_LIMIT=new double[] { 1.8, 1.7, 1.3, 1.1, 0.9};//选择样本阀值时TP FP RATIO从何开始
//TP_FP_BOTTOM_LINE=0.8; //TP/FP的下限
//DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
//===============================output summary===================================== for : adaboost
//Monthly selected_TPR mean: 32.73% standard deviation=21.54% Skewness=0.49 Kurtosis=-0.6
//Monthly selected_LIFT mean : 1.1
//Monthly selected_positive summary: 17,505
//Monthly selected_count summary: 45,794
//Monthly selected_shouyilv average: 1.21% standard deviation=6.70% Skewness=2.88 Kurtosis=14.29
//Monthly total_shouyilv average: 0.98% standard deviation=6.13% Skewness=3.04 Kurtosis=15.43
//mixed selected positive rate: 38.23%
//Monthly summary_judge_result summary: good number= 289 bad number=226
//===============================end of summary=====================================for : adaboost
//result changed because of reference data not matched=5955 while good change number=4131
//good ratio=69.37% average changed shouyilv=1.11% @ shouyilv thredhold= /0.00% /0.00% /0.00% /0.00% /0.00% /
//number of records for full market=1412480
//shouyilv average for full market=0.80%
//selected shouyilv average for full market =2.06% count=39839
//selected shouyilv average for hs300 =0.75% count=4815
//selected shouyilv average for zz500 =1.35% count=9360

//2. 20160918 使用波动率后的新模型
//m_modelEvalFileShareMode=ModelStore.HALF_YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
//
//leafMinObjNum=300; 	//j48树最小节点叶子数
//divided=300; //将trainingData分成多少份
//boost_iteration=10; 	//boost特有参数
//
//m_noCaculationAttrib=false; //使用计算字段
//EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
//SAMPLE_LOWER_LIMIT =new double[] { 0.04, 0.05, 0.06, 0.07, 0.08 }; // 各条均线选择样本的下限
//SAMPLE_UPPER_LIMIT =new double[] { 0.07, 0.08, 0.11, 0.12, 0.13 }; // 各条均线选择样本的上限
//TP_FP_RATIO_LIMIT=new double[] { 1.8, 1.7, 1.3, 1.1, 0.9};//选择样本阀值时TP FP RATIO从何开始
//TP_FP_BOTTOM_LINE=0.8; //TP/FP的下限
//DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
//===============================output summary===================================== for : adaboost
//Monthly selected_TPR mean: 32.43% standard deviation=21.39% Skewness=0.62 Kurtosis=-0.22
//Monthly selected_LIFT mean : 1.07
//Monthly selected_positive summary: 17,050
//Monthly selected_count summary: 46,651
//Monthly selected_shouyilv average: 1.42% standard deviation=7.49% Skewness=4.39 Kurtosis=36.33
//Monthly total_shouyilv average: 0.98% standard deviation=6.09% Skewness=3.06 Kurtosis=15.62
//mixed selected positive rate: 36.55%
//Monthly summary_judge_result summary: good number= 285 bad number=235
//===============================end of summary=====================================for : adaboost
public class AdaboostClassifier extends NominalClassifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2704022707088213011L;
	protected int leafMinObjNum; 	//j48树最小节点叶子数
	protected int divided; //将trainingData分成多少份
	protected int boost_iteration; 	//boost特有参数

	@Override
	protected void initializeParams() {
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = false;
		
		classifierName=ClassifyUtility.ADABOOST;
		setWorkPathAndCheck(AppContext.getNOMINAL_CLASSIFIER_DIR()+this.getIdentifyName()+"\\");
		m_modelEvalFileShareMode=ModelStore.HALF_YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		
		leafMinObjNum=300; 	//j48树最小节点叶子数
		divided=300; //将trainingData分成多少份
		boost_iteration=10; 	//boost特有参数
		
		m_noCaculationAttrib=false; //使用计算字段
	}
		
	@Override
	protected Classifier buildModel(Instances train) throws Exception {
		
		m_cachedOldClassInstances=null; 
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

}
