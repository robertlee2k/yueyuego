package yueyueGo.classifier;

import yueyueGo.NominalClassifier;


//这个模型60日线不适合，模型评估ROC基本上接近0.5，所以选不出60日线的票

//*******当前选择的模型
//1. HS300 2008-2016 20单元格7%;中证500 20/30/50  8%;全市场20/30/50  13%-12%-11% 净值平稳
// 参数：  eval 0.7 / 单独评估阀值/ TP——FP RATIO { 1.6, 1.4, 1.3, 1.1, 1.0 }, UPPer{ 0.07, 0.09, 0.1, 0.12, 0.15 } TP_FP_BOTTOM_LINE=0.5
// lower_limit  { 0.01, 0.01,0.02, 0.02, 0.02 } DEFAULT_THRESHOLD=0.6
//===============================output summary=====================================
//Monthly selected_TPR mean: 15.37% standard deviation=22.89% Skewness=1.44 Kurtosis=1.16
//Monthly selected_LIFT mean : 0.47
//Monthly selected_positive summary: 20,248
//Monthly selected_count summary: 50,704
//Monthly selected_shouyilv average: 0.71% standard deviation=4.57% Skewness=2.96 Kurtosis=13.58
//Monthly total_shouyilv average: 1.00% standard deviation=6.18% Skewness=3.01 Kurtosis=15.09
//mixed selected positive rate: 39.93%
//Monthly summary_judge_result summary: good number= 272 bad number=233
//===============================end of summary=====================================

//2. 
//参数：  eval 0.9 / 单独评估阀值/ TP——FP RATIO { 1.6, 1.4, 1.3, 1.1, 1.0 }, UPPer{ 0.07, 0.09, 0.12, 0.12, 0.2 } TP_FP_BOTTOM_LINE=0.5
//lower_limit  { 0.01, 0.01,0.02, 0.02, 0.02 } DEFAULT_THRESHOLD=0.6
//===============================output summary=====================================
//Monthly selected_TPR mean: 16.27% standard deviation=23.91% Skewness=1.41 Kurtosis=1
//Monthly selected_LIFT mean : 0.49
//Monthly selected_positive summary: 22,273
//Monthly selected_count summary: 53,959
//Monthly selected_shouyilv average: 0.82% standard deviation=5.24% Skewness=3.91 Kurtosis=23.43
//Monthly total_shouyilv average: 1.00% standard deviation=6.18% Skewness=3.01 Kurtosis=15.09
//mixed selected positive rate: 41.28%
//Monthly summary_judge_result summary: good number= 275 bad number=230
//===============================end of summary=====================================

//3. 
//参数：  eval 0.9 / 单独评估阀值/ TP——FP RATIO {1.6, 1.4, 1.3, 1.2, 1.1 }, UPPer{ 0.07, 0.09, 0.12, 0.15, 0.2 } TP_FP_BOTTOM_LINE=0.5
//lower_limit  { 0.01, 0.01,0.02, 0.02, 0.02 } DEFAULT_THRESHOLD=0.6
//===============================output summary=====================================
//Monthly selected_TPR mean: 16.21% standard deviation=23.87% Skewness=1.42 Kurtosis=1.03
//Monthly selected_LIFT mean : 0.5
//Monthly selected_positive summary: 22,222
//Monthly selected_count summary: 53,861
//Monthly selected_shouyilv average: 0.81% standard deviation=5.24% Skewness=3.93 Kurtosis=23.47
//Monthly total_shouyilv average: 1.00% standard deviation=6.18% Skewness=3.01 Kurtosis=15.09
//mixed selected positive rate: 41.26%
//Monthly summary_judge_result summary: good number= 275 bad number=230
//===============================end of summary=====================================
public class REPTreeClassifier extends NominalClassifier {

	public REPTreeClassifier() {
		super();
		classifierName="rep";
		WORK_PATH =WORK_PATH+classifierName+"\\";
	
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = true;
		
		EVAL_RECENT_PORTION = 0.9; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
		m_sepeperate_eval_HS300=true;//单独为HS300评估阀值
		m_seperate_classify_HS300=true;
		
		SAMPLE_LOWER_LIMIT =new double[] { 0.01, 0.01, 0.02, 0.02, 0.02 }; // 各条均线选择样本的下限
		SAMPLE_UPPER_LIMIT =new double[] {0.07, 0.09, 0.12, 0.15, 0.2 }; // 各条均线选择样本的上限
		TP_FP_RATIO_LIMIT=new double[] {  1.6, 1.4, 1.3, 1.2, 1.1 };//选择样本阀值时TP FP RATIO从何开始
		TP_FP_BOTTOM_LINE=0.5; //TP/FP的下限
		DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。

	}

}
