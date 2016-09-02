package yueyueGo.classifier;

import weka.classifiers.Classifier;
import yueyueGo.NominalClassifier;
import yueyueGo.RuntimeParams;

//结论1： 5单元格的不可靠，偶然性因素太大， 应该在10-30单元格中间选择
//结论2： 这个分类器适用沪深300, 全市场不大合适大熊市（因为2008年亏损大收益率偏低）

public class MLPClassifier extends NominalClassifier {
	// 1. HS300 2008-2016 20-30单元格年均10% 中证500 20/30/50  8%;全市场20/30/50  8-9% 但全市场因为2008年起步亏损大(2008净值最差0.55），累计净值不高;  全市场整体胜率 38183/98804
	// 参数：  eval 0.5 / 单独评估阀值/ TP——FP RATIO { 1.8, 1.5, 1.2, 1.0, 1.0 }, UPPer { 0.07, 0.09, 0.11, 0.15, 0.2 } TP_FP_BOTTOM_LINE=0.5
	//lower_limit  { 0.01, 0.01, 0.02, 0.02,0.02 } DEFAULT_THRESHOLD=0.6

	//2. HS300 2008-2016最优10-20单元格 12%（中证500 20/30/50 9%-11%） 全市场整体胜率61709/158507  
	// 参数：  eval 0.9 / 单独评估阀值/ TP——FP RATIO { 1.8, 1.5, 1.2, 1.0, 1.0 }, UPPer{ 0.2, 0.2, 0.2, 0.2, 0.2 } TP_FP_BOTTOM_LINE=0.8
	
	//3. HS300 2008-2016 10单元格10%  20格8%；中证500 20/30/50  9%-11%; 全市场20/30/50  13-10% 但全市场因为2008年起步亏损大(2008净值最差0.58），累计净值不高；  全市场整体胜率39967/102262
	// 参数：  eval 0.9 / 单独评估阀值/ TP——FP RATIO { 1.8, 1.5, 1.2, 1.0, 1.0 }, UPPer{ 0.07, 0.09, 0.11, 0.15, 0.18 } TP_FP_BOTTOM_LINE=0.8
	
	
	//*******当前选择的模型
	//4. HS300 2008-2016 10-20单元格13%;中证500 20/30/50  6%;全市场20/30/50  9-8% 但全市场因为2008年起步亏损大(2008净值最差0.6），累计净值不高;  全市场整体胜率 38,105/98,447
	// 参数：  eval 0.7 / 单独评估阀值/ TP——FP RATIO { 1.8, 1.5, 1.2, 1.2, 1.2 }, UPPer{ 0.07, 0.09, 0.1, 0.13, 0.15 } TP_FP_BOTTOM_LINE=0.8
	// lower_limit  { 0.01, 0.01, 0.01, 0.01,0.01 } DEFAULT_THRESHOLD=0.7
	
	//5. HS300 2008-2016 10-20单元格10%;中证500 20/30/50 7%-8%;全市场20/30/50 6%-8% ;  全市场整体胜率 37849/97947
	// 参数：  eval 0.5 / 单独评估阀值/ TP——FP RATIO { 1.8, 1.5, 1.2, 1.0, 1.0 }, UPPer { 0.07, 0.09, 0.11, 0.15, 0.2 } TP_FP_BOTTOM_LINE=0.6
	// lower_limit  { 0.01, 0.01, 0.01, 0.01,0.01 } DEFAULT_THRESHOLD=0.7
	
	// 6
	//SAMPLE_UPPER_LIMIT  {0.07, 0.09, 0.1, 0.1, 0.1 }; TP_FP_RATIO_LIMIT {  1.6, 1.4, 1.3, 1.1, 0.9 };TP_FP_BOTTOM_LINE=0.7
	//DEFAULT_THRESHOLD=0.6;EVAL_RECENT_PORTION = 0.7; 
	//	===============================output summary=====================================
	//			Monthly selected_TPR mean: 32.17% standard deviation=25.79% Skewness=0.75 Kurtosis=-0.14
	//			Monthly selected_LIFT mean : 1.16
	//			Monthly selected_positive summary: 33,380
	//			Monthly selected_count summary: 83,342
	//			Monthly selected_shouyilv average: 1.19% standard deviation=8.70% Skewness=736.71% Kurtosis=705.85%
	//			Monthly total_shouyilv average: 1.00% standard deviation=6.18% Skewness=300.76% Kurtosis=509.10%
	//			mixed selected positive rate: 40.05%
	//			Monthly summary_judge_result summary: good number= 265 bad number=240
	//			===============================end of summary=====================================	
	
	//7 ext model  ---当前选择
	//全市场20-30-50单元格 胜率优先(收益率优先没啥差别） 14%-13%-13% （年最低净值0.8， 出现在2008/2013/2016)
	//HS300 20-30-50单元格 胜率优先(收益率优先没啥差别） 15%-14%-13%
//	EVAL_RECENT_PORTION = 0.7; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
//	m_sepeperate_eval_HS300=true;//单独为HS300评估阀值
//	m_seperate_classify_HS300=true;
//	
//	SAMPLE_LOWER_LIMIT =new double[] { 0.01, 0.01, 0.02, 0.02, 0.02 }; // 各条均线选择样本的下限
//	SAMPLE_UPPER_LIMIT =new double[] {0.07, 0.09, 0.1, 0.1, 0.1 }; // 各条均线选择样本的上限
//	TP_FP_RATIO_LIMIT=new double[] {  1.6, 1.4, 1.3, 1.1, 0.9 };//选择样本阀值时TP FP RATIO从何开始
//	TP_FP_BOTTOM_LINE=0.7; //TP/FP的下限
//	DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。	
//	===============================output summary=====================================
//			Monthly selected_TPR mean: 31.93% standard deviation=26.69% Skewness=0.76 Kurtosis=-0.22
//			Monthly selected_LIFT mean : 1.14
//			Monthly selected_positive summary: 24,999
//			Monthly selected_count summary: 67,858
//			Monthly selected_shouyilv average: 1.31% standard deviation=9.30% Skewness=8.42 Kurtosis=119.45
//			Monthly total_shouyilv average: 1.00% standard deviation=6.15% Skewness=3.02 Kurtosis=15.27
//			mixed selected positive rate: 36.84%
//			Monthly summary_judge_result summary: good number= 280 bad number=230
//			===============================end of summary=====================================
	public static final String classifierName="mlp";
	@Override
	protected void initializeParams() {
		
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = true;
		
		setWorkPathAndCheck(RuntimeParams.getNOMINAL_CLASSIFIER_DIR()+classifierName+"\\");
		
		m_noCaculationAttrib=true; //这个模型是用短格式的
		EVAL_RECENT_PORTION = 0.7; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
		SAMPLE_LOWER_LIMIT =new double[] { 0.01, 0.01, 0.02, 0.02, 0.02 }; // 各条均线选择样本的下限
		SAMPLE_UPPER_LIMIT =new double[] {0.07, 0.09, 0.1, 0.1, 0.1 }; // 各条均线选择样本的上限
		TP_FP_RATIO_LIMIT=new double[] {  1.6, 1.4, 1.3, 1.1, 0.9 };//选择样本阀值时TP FP RATIO从何开始
		TP_FP_BOTTOM_LINE=0.7; //TP/FP的下限
		DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
	}
	
	@Override
	public Classifier loadModel(String yearSplit, String policySplit) throws Exception{
		//这是为MLP单独准备的模型，模型文件是按年读取，但evaluation文件不变仍按月
		int inputYear=Integer.parseInt(yearSplit.substring(0,4));

//		//TODO 为2014和2016年增加一个模型
//		int inputMonth=Integer.parseInt(yearSplit.substring(4,6));
//		if (inputYear==2014 && inputMonth>6){
//			inputYear=201407;
//		}else if (inputYear==2016 && inputMonth==5) {
//			inputYear=201605;
//		}
//		if (inputYear>=2014)
//			inputYear=2014;
		String filename=this.WORK_PATH+this.WORK_FILE_PREFIX +"-"+classifierName+ "-" + inputYear + MA_PREFIX + policySplit;//如果使用固定模型
		
		this.setModelFileName(filename);

	
		return loadModelFromFile();
	}
}
