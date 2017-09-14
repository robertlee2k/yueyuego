package yueyueGo.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.J48;
import yueyueGo.MyAttributionSelectorWithPCA;
import yueyueGo.NominalClassifier;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaInstances;
import yueyueGo.utility.ClassifyUtility;
import yueyueGo.utility.FormatUtility;
import yueyueGo.utility.modelEvaluation.EvaluationStore;
import yueyueGo.utility.modelEvaluation.ModelStore;


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

// 20161017
//===============================output summary===================================== for : adaboost
//Monthly selected_TPR mean: 31.25% standard deviation=21.37% Skewness=0.49 Kurtosis=-0.5
//Monthly selected_LIFT mean : 1.02
//Monthly selected_positive summary: 24,385
//Monthly selected_count summary: 61,586
//Monthly selected_shouyilv average: 1.22% standard deviation=6.84% Skewness=3.21 Kurtosis=17.25
//Monthly total_shouyilv average: 0.96% standard deviation=6.07% Skewness=3.08 Kurtosis=15.81
//mixed selected positive rate: 39.60%
//Monthly summary_judge_result summary: good number= 310 bad number=215
//===============================end of summary=====================================for : adaboost
//###### Finally selected count=40538  ######
//result changed because of reference data not matched=21048 while good change number=14933
//good ratio=70.95% average changed shouyilv=1.08% @ shouyilv thredhold= /0.50% /0.50% /1.00% /3.00% /3.00% /
//number of records for full market=1454589
//shouyilv average for full market=0.77%
//selected shouyilv average for full market =2.47% count=40538
//-----end of test backward------ (with baggingM5P)


//***************************************CLASSIFY DATE=2017-01-05 
//ClassifyIdentity=adaboost
//m_skipTrainInBacktest=true
//m_skipEvalInBacktest=false
//m_noCaculationAttrib=true
//m_removeSWData=true
//m_positiveLine=0.0
//m_modelDataSplitMode=9
//m_modelEvalFileShareMode=3
//modelArffFormat=2
//SAMPLE_LOWER_LIMIT={0.03,0.03,0.03,0.03,0.03,}
//SAMPLE_UPPER_LIMIT={0.1,0.1,0.1,0.1,0.1,}
//LIFT_UP_TARGET=1.8
//***************************************
//......................
//===============================output summary===================================== for : adaboost
//Monthly selected_TPR mean: 31.75% standard deviation=21.47% Skewness=0.7 Kurtosis=-0.09
//Monthly selected_LIFT mean : 1.1
//Monthly selected_positive summary: 22,480
//Monthly selected_count summary: 58,044
//Monthly selected_shouyilv average: 1.25% standard deviation=7.33% Skewness=3.79 Kurtosis=27.14
//Monthly total_shouyilv average: 0.83% standard deviation=6.01% Skewness=3.07 Kurtosis=15.82
//mixed selected positive rate: 38.73%
//Monthly summary_judge_result summary: good number= 312 bad number=223
//===============================end of summary=====================================for : adaboost
//-----now output nominal predictions----------adaboost
//incoming resultData size, row=1477574 column=6
//incoming referenceData size, row=1477574 column=6
//Left data loaded, row=1760436 column=12
//number of results merged and processed: 1477574
//###### Finally selected count=25381  ######
//WINRATE_FILTER_FOR_SHOUYILV={0.4,0.4,0.35,0.35,0.3,}
//SHOUYILV_FILTER_FOR_WINRATE={0.01,0.01,0.02,0.03,0.04,}
// result changed because of reference data not matched=32663 while good change number=23057
// good ratio=70.59% average changed shouyilv=1.94% @ shouyilv thredhold= /1.00% /1.00% /2.00% /3.00% /4.00% /
//number of records for full market=1477574
//shouyilv average for full market=0.6578%
//selected shouyilv average for full market =2.7544% count=25381

//-----now output nominal predictions----------adaboost
//incoming resultData size, row=1477574 column=6
//incoming referenceData size, row=1477574 column=6
//Left data loaded, row=1760436 column=12
//number of results merged and processed: 1477574
//###### Finally selected count=20877  ######
//WINRATE_FILTER_FOR_SHOUYILV={0.5,0.5,0.45,0.45,0.4,}
//SHOUYILV_FILTER_FOR_WINRATE={0.01,0.02,0.03,0.04,0.05,}
// result changed because of reference data not matched=37167 while good change number=26908
// good ratio=72.40% average changed shouyilv=2.16% @ shouyilv thredhold= /1.00% /2.00% /3.00% /4.00% /5.00% /
//number of records for full market=1477574
//shouyilv average for full market=0.6578%
//selected shouyilv average for full market =2.5265% count=20877

//-----now output nominal predictions----------adaboost
//incoming resultData size, row=1477574 column=6
//incoming referenceData size, row=1477574 column=6
//Left data loaded, row=1760436 column=12
//number of results merged and processed: 1477574
//###### Finally selected count=22125  ######
// result changed because of reference data not matched=35919 while good change number=25790
// good ratio=71.80% average changed shouyilv=2.15%
// @ SHOUYILV_FILTER_FOR_WINRATE={1.00%,2.00%,3.00%,3.00%,4.00%, }
//number of records for full market=1477574
//shouyilv average for full market=0.6578%
//selected shouyilv average for full market =2.5252% count=22125


//***************************************CLASSIFY DATE=2017-03-07 
//ClassifyIdentity=adaboost
//m_skipTrainInBacktest=true
//m_skipEvalInBacktest=false
//m_noCaculationAttrib=true
//m_removeSWData=true
//m_positiveLine=0.0
//m_modelDataSplitMode=9
//m_modelEvalFileShareMode=3
//modelArffFormat=8
//SAMPLE_LOWER_LIMIT={0.03,0.03,0.03,0.03,0.03,}
//SAMPLE_UPPER_LIMIT={0.06,0.06,0.07,0.08,0.09,}
//LIFT_UP_TARGET=1.8
//***************************************
//......................
//===============================output summary===================================== for : adaboost format=8
//Monthly selected_TPR mean: 31.89% standard deviation=21.62% Skewness=0.57 Kurtosis=-0.31
//Monthly selected_LIFT mean : 1.07
//Monthly selected_positive summary: 21,075
//Monthly selected_count summary: 54,726
//Monthly selected_shouyilv average: 1.15% standard deviation=6.88% Skewness=3 Kurtosis=14.3
//Monthly total_shouyilv average: 0.77% standard deviation=5.96% Skewness=3.11 Kurtosis=16.17
//mixed selected positive rate: 38.51%
//Monthly summary_judge_result summary: good number= 310 bad number=235
//===============================end of summary=====================================for : adaboost format=8
//-----now output nominal predictions----------adaboost (filtered by continuous: baggingM5P-multiPCA)
//incoming resultData size, row=1511065 column=6
//incoming referenceData size, row=1511065 column=6
//Left data loaded, row=1793927 column=12
//number of results merged and processed: 1511065
//###### Finally selected count=20088  ######
// result changed because of reference data not matched=34638 while good change number=25199
// good ratio=72.75% average changed shouyilv=1.77%
// @ SHOUYILV_FILTER_FOR_WINRATE={1.00%,2.00%,3.00%,3.00%,4.00%, }
//number of records for full market=1511065
//shouyilv average for full market=0.6076%
//selected shouyilv average for full market =2.5346% count=20088
//-----end of test backward------


//***************************************CLASSIFY DATE=2017-04-24 
//ClassifyIdentity=adaboost
//m_skipTrainInBacktest=true
//m_skipEvalInBacktest=false
//m_noCaculationAttrib=true
//m_removeSWData=true
//m_positiveLine=0.0
//m_modelDataSplitMode=9
//m_modelEvalFileShareMode=3
//modelArffFormat=9
//SAMPLE_LOWER_LIMIT={0.03,0.03,0.03,0.03,0.03,}
//SAMPLE_UPPER_LIMIT={0.06,0.06,0.07,0.08,0.09,}
//LIFT_UP_TARGET=1.8
//***************************************
//......................
//===============================output summary===================================== for : adaboost format=9
//Monthly selected_TPR mean: 31.33% standard deviation=21.55% Skewness=0.64 Kurtosis=-0.15
//Monthly selected_LIFT mean : 1.1
//Monthly selected_positive summary: 21,062
//Monthly selected_count summary: 57,789
//Monthly selected_shouyilv average: 1.01% standard deviation=6.55% Skewness=2.71 Kurtosis=11.99
//Monthly total_shouyilv average: 0.74% standard deviation=5.92% Skewness=3.14 Kurtosis=16.5
//mixed selected positive rate: 36.45%
//Monthly summary_judge_result summary: good number= 321 bad number=234
//===============================end of summary=====================================for : adaboost format=9
//......................
//-----now output nominal predictions----------adaboost (filtered by continuous: baggingM5P-multiPCA)
//incoming resultData size, row=1548274 column=6
//incoming referenceData size, row=1548274 column=6
//Left data loaded, row=1831136 column=12
//number of results merged and processed: 1548274
//###### Finally selected count=21742  ######
// result changed because of reference data not matched=36047 while good change number=26995
// good ratio=74.89% average changed shouyilv=1.13%
// @ SHOUYILV_FILTER_FOR_WINRATE={1.00%,2.00%,3.00%,3.00%,4.00%, }
//number of records for full market=1548274
//shouyilv average for full market=0.5761%
//selected shouyilv average for full market =1.9868% count=21742
//-----end of test backward------
public class AdaboostClassifier extends NominalClassifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2704022707088213011L;
	public int leafMinObjNum; 	//j48树最小节点叶子数
	public int divided; //将trainingData分成多少份
	public int boost_iteration; 	//boost特有参数
	public boolean m_usePCA;
	public int m_preprocesingBeforePCA;
	
	@Override
	protected void initializeParams() {
		m_policySubGroup = new String[]{"5","10","20","30","60" };

		classifierName=ClassifyUtility.ADABOOST;
		m_modelFileShareMode=ModelStore.QUARTER_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		
		leafMinObjNum=300; 	//j48树最小节点叶子数
		divided=300; //将trainingData分成多少份
		boost_iteration=10; 	//boost特有参数
		
 
		m_usePCA=true; //20121223尝试不使用PCA，效果一般且建模非常慢，所以放弃
		m_normalize=false; //在进入分类器之前需要对数据做Normalize
		m_preprocesingBeforePCA=MyAttributionSelectorWithPCA.CENTER_DATA;
				//MyAttributionSelectorWithPCA.STANDARDIZE_DATA; 
		
		m_evalDataSplitMode=EvaluationStore.USE_NINE_MONTHS_DATA_FOR_EVAL; //尝试评估区间使用9个月数据（效果还不错）
//		m_positiveLine=0.03; //尝试3%的阀值
	}
		
	@Override
	protected Classifier buildModel(GeneralInstances train) throws Exception {
		
 
		//设置基础的J48 classifier参数
		J48 model=ClassifyUtility.prepareJ48(train.numInstances(),leafMinObjNum,divided);

		AdaBoostM1 adaboost=new  AdaBoostM1();
		adaboost.setClassifier(model);
		adaboost.setNumIterations(boost_iteration);
		adaboost.setDebug(true);
		if (m_usePCA==true){
			MyAttributionSelectorWithPCA classifier = new MyAttributionSelectorWithPCA(m_normalize,m_preprocesingBeforePCA);
			classifier.setClassifier(adaboost);
			classifier.setDebug(true);
			classifier.buildClassifier(WekaInstances.convertToWekaInstances(train));
			System.out.println("finish buiding adaboost model using PCA");
			return classifier;

		}else {
			adaboost.buildClassifier(WekaInstances.convertToWekaInstances(train));
			System.out.println("finish buiding adaboost model without PCA");
			return adaboost;
		}
	}
	
	@Override
	public String getIdentifyName(){
		String idenString;
		if (m_usePCA==true){ //使用PCA
			if (m_positiveLine==0){ //如果是正常的正负分类时就不用特别标记
				idenString =classifierName;
			}else { //如果用自定义的标尺线区分Class的正负，则特别标记
				idenString =classifierName+"("+FormatUtility.formatDouble(m_positiveLine)+")";
			}
		}else{
			idenString =classifierName+ClassifyUtility.NO_PCA_SURFIX;
		}
		
		return idenString;
	}
}
