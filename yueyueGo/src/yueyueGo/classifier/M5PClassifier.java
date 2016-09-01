package yueyueGo.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.trees.M5P;
import weka.core.Instances;
import yueyueGo.ClassifyUtility;
import yueyueGo.ContinousClassifier;
import yueyueGo.RuntimeParams;


//结论1： 5单元格的不可靠，偶然性因素太大， 应该在10-30单元格中间选择
//结论2： 这个分类器适用于中证500及全市场， 沪深300上大不合适（选股少）。
public class M5PClassifier extends ContinousClassifier {
	//方案1适合中证500
	// 1. 全市场 2008-2016最优10单元格年均14% （20单元格11%），中证500 10-20都是14%，30-50都是12%（均为不单独评估），hs300 10格有13% （20格9%） 29941/82650
	// 参数：  eval 0.1 / 沪深单独评估阀值/ TP——FP RATIO { 1.6, 1.4, 1.3, 1.1, 0.9 }, UPPer { 0.1, 0.1, 0.1, 0.1, 0.1 }TP_FP_BOTTOM_LINE=0.5
	
	// 2. 激进策略（收益率优先）全市场2008-2016 20单元格年均15% （不单独评估hs300）（30格15%，不单独评估14%），中证500 最优 20格14%，30格14%（均为不单独评估，保守策略更高），hs300不单独评估 20格7% （30格8%）  ----- 选择这个参数作为模型20160524
	// 参数：  eval 0.5 / 沪深单独评估阀值/ TP——FP RATIO { 1.6, 1.4, 1.3, 1.1, 0.9 }, UPPer { 0.07, 0.09, 0.1, 0.1, 0.1 }TP_FP_BOTTOM_LINE=0.5     
	//运行结果	Monthly selected_TPR mean: 26.9% standard deviation=23.9% Skewness=89.6% Kurtosis=32.8%
	//			Monthly selected_LIFT mean : 1.06
	//			Monthly selected_positive summary: 24,771
	//			Monthly selected_count summary: 71,015
	//			Monthly selected_shouyilv average: 1.1% standard deviation=8.6% Skewness=96.4% Kurtosis=41.2%
	//			Monthly total_shouyilv average: 1.0% standard deviation=6.2% Skewness=00.8% Kurtosis=09.1%
	//			mixed selected positive rate: 34.9%
	//			Monthly summary_judge_result summary: good number= 237 bad number=268

	//3.激进策略（收益率优先）全市场2008-2016 20单元格年均15% （不单独评估hs300）（30格15%，不单独评估14%），中证500 最优 20格14%，30格14%（均为不单独评估，保守策略更高），hs300不单独评估 20格7% （30格8%）  ----- 选择这个参数作为模型20160616
	// 参数：  eval 0.9 / 沪深单独评估阀值/ TP——FP RATIO { 1.6, 1.4, 1.3, 1.1, 0.9 }, UPPer { 0.1, 0.1, 0.1, 0.1, 0.1 }TP_FP_BOTTOM_LINE=0.5
	//	===============================output summary=====================================
	//			Monthly selected_TPR mean: 25.67% standard deviation=25.12% Skewness=0.95 Kurtosis=0.32
	//			Monthly selected_LIFT mean : 0.81
	//			Monthly selected_positive summary: 19,580
	//			Monthly selected_count summary: 55,336
	//			Monthly selected_shouyilv average: 1.18% standard deviation=9.06% Skewness=728.20% Kurtosis=166.04%
	//			Monthly total_shouyilv average: 1.00% standard deviation=6.18% Skewness=300.76% Kurtosis=509.10%
	//			mixed selected positive rate: 35.38%
	//			Monthly summary_judge_result summary: good number= 255 bad number=250
	//			===============================end of summary=====================================
	

	// 4.  中证5002008-2016 10单元格年均 18%（20格 13%），全市场10单元15%，hs300 10格7% （单独评估）  18357/47673----20060713 选择的model
	// 参数：  eval 0.9 / 沪深单独评估阀值/ TP——FP RATIO { 1.6, 1.4, 1.3, 1.1, 0.9 }, UPPer { 0.07, 0.09, 0.1, 0.1, 0.1 }TP_FP_BOTTOM_LINE=0.7
//	===============================output summary=====================================
//			Monthly selected_TPR mean: 20.46% standard deviation=26.04% Skewness=1.26 Kurtosis=0.83
//			Monthly selected_LIFT mean : 0.63
//			Monthly selected_positive summary: 20,463
//			Monthly selected_count summary: 56,140
//			Monthly selected_shouyilv average: 1.14% standard deviation=8.33% Skewness=8.52 Kurtosis=115.11
//			Monthly total_shouyilv average: 1.00% standard deviation=6.15% Skewness=3.02 Kurtosis=15.27
//			mixed selected positive rate: 36.45%
//			Monthly summary_judge_result summary: good number= 268 bad number=242
//			===============================end of summary=====================================
	
	//5.全市场20-30单元15-16%
//	EVAL_RECENT_PORTION = 0.9; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
//	SAMPLE_LOWER_LIMIT = new double[]{ 0.01, 0.01, 0.02, 0.02, 0.02 }; // 各条均线选择样本的下限 
//	SAMPLE_UPPER_LIMIT = new double[]  { 0.06, 0.07, 0.1, 0.11, 0.12 };
//	TP_FP_RATIO_LIMIT = new double[] { 1.8, 1.6, 1.4, 1.0, 0.75 }; 
//	===============================output summary=====================================
//			Monthly selected_TPR mean: 21.42% standard deviation=26.33% Skewness=1.21 Kurtosis=0.69
//			Monthly selected_LIFT mean : 0.67
//			Monthly selected_positive summary: 16,792
//			Monthly selected_count summary: 48,381
//			Monthly selected_shouyilv average: 1.09% standard deviation=8.52% Skewness=7.94 Kurtosis=104.74
//			Monthly total_shouyilv average: 1.00% standard deviation=6.15% Skewness=3.02 Kurtosis=15.27
//			mixed selected positive rate: 34.71%
//			Monthly summary_judge_result summary: good number= 273 bad number=237
//			===============================end of summary=====================================

	// 6 eval=0.3 其他同第5条 虽然统计结果很差，但整体收益率（用保守策略）跟5相差不多，这也说明当前算法下，收益率关键还是TPFP_ratio，因为大量排在后面的机会反正也买不进
//	===============================output summary=====================================
//			Monthly selected_TPR mean: 24.22% standard deviation=24.86% Skewness=1.06 Kurtosis=0.6
//			Monthly selected_LIFT mean : 0.78
//			Monthly selected_positive summary: 26,897
//			Monthly selected_count summary: 77,600
//			Monthly selected_shouyilv average: 0.99% standard deviation=7.18% Skewness=3.91 Kurtosis=26.55
//			Monthly total_shouyilv average: 1.00% standard deviation=6.15% Skewness=3.02 Kurtosis=15.27
//			mixed selected positive rate: 34.66%
//			Monthly summary_judge_result summary: good number= 254 bad number=256
//			===============================end of summary=====================================	
	
// 	8. 其他跟5一样，阀值加上了上下限。
//	EVAL_RECENT_PORTION = 0.9; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
//	SAMPLE_LOWER_LIMIT = new double[]{ 0.01, 0.01, 0.02, 0.02, 0.02 }; // 各条均线选择样本的下限 
//	SAMPLE_UPPER_LIMIT = new double[]  { 0.06, 0.07, 0.1, 0.11, 0.12 };
//	TP_FP_RATIO_LIMIT = new double[] { 1.8, 1.6, 1.4, 1.0, 0.75 }; 	
//	全市场20-30-50单元（收益率排序17%-16%-13%） （胜率排序17%-15%-12%） 
//	===============================output summary===================================== for : m5p
//			Monthly selected_TPR mean: 21.31% standard deviation=26.43% Skewness=1.22 Kurtosis=0.68
//			Monthly selected_LIFT mean : 0.66
//			Monthly selected_positive summary: 16,123
//			Monthly selected_count summary: 46,473
//			Monthly selected_shouyilv average: 1.05% standard deviation=8.47% Skewness=8.06 Kurtosis=107.1
//			Monthly total_shouyilv average: 1.00% standard deviation=6.15% Skewness=3.02 Kurtosis=15.27
//			mixed selected positive rate: 34.69%
//			Monthly summary_judge_result summary: good number= 274 bad number=236
//			===============================end of summary=====================================for : m5p
	

// 9. 与M5PAB做对比，用同样参数 阀值设上下限
// 收益率优先策略净值 全市场20/30格 2008-2016年均17%/15%，年最低净值0.85 （胜率优先策略差不多）	
//	EVAL_RECENT_PORTION = 0.9; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
//	SAMPLE_LOWER_LIMIT = new double[]{ 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限 
//	SAMPLE_UPPER_LIMIT = new double[]  { 0.06, 0.07, 0.1, 0.11, 0.12 };
//	TP_FP_RATIO_LIMIT = new double[] { 1.8, 1.7, 1.5, 1.2, 1}; //选择样本阀值时TP FP RATIO到了何种值就可以停止了。
//	TP_FP_BOTTOM_LINE=0.9; //TP/FP的下限
//	===============================output summary===================================== for : m5p
//			Monthly selected_TPR mean: 18.64% standard deviation=25.99% Skewness=1.38 Kurtosis=1.06
//			Monthly selected_LIFT mean : 0.57
//			Monthly selected_positive summary: 13,645
//			Monthly selected_count summary: 38,467
//			Monthly selected_shouyilv average: 0.96% standard deviation=8.24% Skewness=8.83 Kurtosis=120.86
//			Monthly total_shouyilv average: 1.00% standard deviation=6.15% Skewness=3.02 Kurtosis=15.27
//			mixed selected positive rate: 35.47%
//			Monthly summary_judge_result summary: good number= 274 bad number=236
//			===============================end of summary=====================================for : m5p	
	@Override
	protected void initializeParams() {
		classifierName = "m5p";
		setWorkPathAndCheck(RuntimeParams.getCONTINOUS_CLASSIFIER_DIR()+classifierName+"\\");
		m_noCaculationAttrib=false; //添加计算
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		m_skipTrainInBacktest = false;
		m_skipEvalInBacktest = false;
		EVAL_RECENT_PORTION = 0.9; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
		SAMPLE_LOWER_LIMIT = new double[]{ 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限 
		SAMPLE_UPPER_LIMIT = new double[]  { 0.06, 0.07, 0.1, 0.11, 0.12 };
		TP_FP_RATIO_LIMIT = new double[] { 1.8, 1.7, 1.5, 1.2, 1}; //{ 1.8, 1.7, 1.7, 1.0, 0.7 };//选择样本阀值时TP FP RATIO到了何种值就可以停止了。
		TP_FP_BOTTOM_LINE=0.9; //TP/FP的下限
	}

	protected int leafMinObjNum=300; //叶子节点最小的
	protected int divided=300; //将trainingData分成多少份
	@Override
	protected Classifier buildModel(Instances train) throws Exception {
		
	
		M5P model=ClassifyUtility.prepareM5P(train.numInstances(),leafMinObjNum,divided);

		model.buildClassifier(train);
		

		return model;
	}
	

}
