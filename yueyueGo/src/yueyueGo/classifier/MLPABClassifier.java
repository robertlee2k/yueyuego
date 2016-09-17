package yueyueGo.classifier;
import weka.classifiers.Classifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instances;
import yueyueGo.ModelStore;
import yueyueGo.MyAttributionSelectorWithPCA;
import yueyueGo.NominalClassifier;
import yueyueGo.utility.AppContext;


// NO.1 选股太多全市场收益率只有7%-8%
//EVAL_RECENT_PORTION = 0.7; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
//m_sepeperate_eval_HS300=false;//太耗时间了，就不单独评估了
//m_seperate_classify_HS300=false;
//
//SAMPLE_LOWER_LIMIT =new double[] { 0.01, 0.01, 0.02, 0.02, 0.02 }; // 各条均线选择样本的下限
//SAMPLE_UPPER_LIMIT =new double[] {0.07, 0.09, 0.1, 0.1, 0.1 }; // 各条均线选择样本的上限
//TP_FP_RATIO_LIMIT=new double[] {  1.6, 1.4, 1.3, 1.1, 0.9 };//选择样本阀值时TP FP RATIO从何开始
//TP_FP_BOTTOM_LINE=0.7; //TP/FP的下限
//DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
//===============================output summary===================================== for : mlpAB
//Monthly selected_TPR mean: 31.97% standard deviation=26.75% Skewness=0.64 Kurtosis=-0.38
//Monthly selected_LIFT mean : 1.09
//Monthly selected_positive summary: 30,754
//Monthly selected_count summary: 78,628
//Monthly selected_shouyilv average: 1.08% standard deviation=7.39% Skewness=3.63 Kurtosis=25.41
//Monthly total_shouyilv average: 1.00% standard deviation=6.15% Skewness=3.02 Kurtosis=15.27
//mixed selected positive rate: 39.11%
//Monthly summary_judge_result summary: good number= 278 bad number=232
//===============================end of summary=====================================for : mlpAB

//NO.2 全市场20-30格收益率10%-8% （主要是2009年收益率没跟上，其他年份又没突出）
//EVAL_RECENT_PORTION = 0.7; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
//SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限
//SAMPLE_UPPER_LIMIT =new double[] {0.07, 0.09, 0.1, 0.1, 0.1 }; // 各条均线选择样本的上限
//TP_FP_RATIO_LIMIT=new double[] { 1.8, 1.7, 1.5, 1.2, 1};//选择样本阀值时TP FP RATIO从何开始
//TP_FP_BOTTOM_LINE=0.8; //TP/FP的下限
//DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
//===============================output summary===================================== for : mlpAB
//Monthly selected_TPR mean: 23.47% standard deviation=29.71% Skewness=1.1 Kurtosis=0.09
//Monthly selected_LIFT mean : 0.79
//Monthly selected_positive summary: 13,840
//Monthly selected_count summary: 34,639
//Monthly selected_shouyilv average: 1.00% standard deviation=8.82% Skewness=7.5 Kurtosis=94.56
//Monthly total_shouyilv average: 1.00% standard deviation=6.15% Skewness=3.02 Kurtosis=15.27
//mixed selected positive rate: 39.95%
//Monthly summary_judge_result summary: good number= 263 bad number=247
//===============================end of summary=====================================for : mlpAB

//No. 3 全市场7%-8%， 这个策略2009年和2014跟不上，2010-2013年表现一般， 2008和2016年跌幅也不小，所以没优势
//EVAL_RECENT_PORTION = 0.7; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
//SAMPLE_LOWER_LIMIT =new double[] { 0.02, 0.02, 0.02, 0.03, 0.03 }; // 各条均线选择样本的下限
//SAMPLE_UPPER_LIMIT =new double[] { 0.06, 0.07, 0.1, 0.11, 0.12 }; // 各条均线选择样本的上限
//TP_FP_RATIO_LIMIT=new double[] { 1.8, 1.7, 1.5, 1.2, 1};//选择样本阀值时TP FP RATIO从何开始
//TP_FP_BOTTOM_LINE=0.9; //TP/FP的下限
//DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
//===============================output summary===================================== for : mlpAB
//Monthly selected_TPR mean: 21.83% standard deviation=30.39% Skewness=1.27 Kurtosis=0.45
//Monthly selected_LIFT mean : 0.71
//Monthly selected_positive summary: 10,986
//Monthly selected_count summary: 28,369
//Monthly selected_shouyilv average: 0.92% standard deviation=8.76% Skewness=7.6 Kurtosis=97.15
//Monthly total_shouyilv average: 1.00% standard deviation=6.15% Skewness=3.02 Kurtosis=15.27
//mixed selected positive rate: 38.73%
//Monthly summary_judge_result summary: good number= 264 bad number=246
//===============================end of summary=====================================for : mlpAB


//No. 4 单独评估HS300
// 全市场20-30单元格 年均13% （2008和2016在0.78左右）， HS300 20-30单元格 14%-15% (2013表现很好1.2-1.3）
//EVAL_RECENT_PORTION = 0.9; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
//SAMPLE_LOWER_LIMIT =new double[] { 0.04, 0.05, 0.06, 0.07, 0.08 }; // 各条均线选择样本的下限
//SAMPLE_UPPER_LIMIT =new double[] { 0.07, 0.08, 0.11, 0.12, 0.13 }; // 各条均线选择样本的上限
//TP_FP_RATIO_LIMIT=new double[] { 1.8, 1.7, 1.3, 1.1, 0.9};//选择样本阀值时TP FP RATIO从何开始
//TP_FP_BOTTOM_LINE=0.8; //TP/FP的下限
//DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
//===============================output summary===================================== for : mlpAB
//Monthly selected_TPR mean: 29.82% standard deviation=29.08% Skewness=0.8 Kurtosis=-0.33
//Monthly selected_LIFT mean : 1.01
//Monthly selected_positive summary: 20,674
//Monthly selected_count summary: 54,635
//Monthly selected_shouyilv average: 1.30% standard deviation=8.14% Skewness=3.62 Kurtosis=22.27
//Monthly total_shouyilv average: 1.00% standard deviation=6.15% Skewness=3.02 Kurtosis=15.27
//mixed selected positive rate: 37.84%
//Monthly summary_judge_result summary: good number= 272 bad number=238
//===============================end of summary=====================================for : mlpAB

//5. 新模型
//===============================output summary===================================== for : mlpAB
//Monthly selected_TPR mean: 28.06% standard deviation=28.29% Skewness=0.83 Kurtosis=-0.31
//Monthly selected_LIFT mean : 0.94
//Monthly selected_positive summary: 16,813
//Monthly selected_count summary: 43,898
//Monthly selected_shouyilv average: 0.88% standard deviation=7.62% Skewness=3.11 Kurtosis=15.19
//Monthly total_shouyilv average: 0.98% standard deviation=6.13% Skewness=3.04 Kurtosis=15.43
//mixed selected positive rate: 38.30%
//Monthly summary_judge_result summary: good number= 265 bad number=250
//===============================end of summary=====================================for : mlpAB

//6. 新模型按月评估（为什么会和上面的4有差异？ 是因为4是按年评估模型，这个是按月评估模型）
//===============================output summary===================================== for : mlpAB
//Monthly selected_TPR mean: 29.29% standard deviation=28.06% Skewness=0.76 Kurtosis=-0.41
//Monthly selected_LIFT mean : 1
//Monthly selected_positive summary: 17,521
//Monthly selected_count summary: 46,048
//Monthly selected_shouyilv average: 1.09% standard deviation=7.70% Skewness=2.92 Kurtosis=13.8
//Monthly total_shouyilv average: 0.98% standard deviation=6.13% Skewness=3.04 Kurtosis=15.43
//mixed selected positive rate: 38.05%
//Monthly summary_judge_result summary: good number= 267 bad number=248
//===============================end of summary=====================================for : mlpAB

//7. 新模型，用长格式，按年使用评估文件
// 收益率明显增长，全市场胜率优先10/20/30/50单元，2008-2016 年均18%/16%/14%/12%， 
//        这个算法下2014年收益能跟上，但弱点是2015年收益幅度没那么多（50%左右，上半年选股不多，股灾中选股不少）
// 如果用收益率优先会更高10-50单元 23%/19%/18%/18% （这个是不是说明选股准确后可以激进？还只是偶然结果？）
//HS300 胜率优先组合净值10-50 在14%/15%/15%/12%，净值很平稳至少在0.85以上，大多在0.9以上（可能选股少）
//   HS300收益率优先很好，在23%/19%/18%/18%
//===============================output summary===================================== for : mlpAB
//Monthly selected_TPR mean: 27.09% standard deviation=28.28% Skewness=0.89 Kurtosis=-0.21
//Monthly selected_LIFT mean : 0.86
//Monthly selected_positive summary: 18,995
//Monthly selected_count summary: 50,594
//Monthly selected_shouyilv average: 0.69% standard deviation=6.87% Skewness=2.78 Kurtosis=13.16
//Monthly total_shouyilv average: 0.98% standard deviation=6.13% Skewness=3.04 Kurtosis=15.43
//mixed selected positive rate: 37.54%
//Monthly summary_judge_result summary: good number= 277 bad number=238
//===============================end of summary=====================================for : mlpAB

public class MLPABClassifier extends NominalClassifier {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2078976101080790784L;
	@Override
	protected void initializeParams() {
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		m_skipTrainInBacktest = false;
		m_skipEvalInBacktest = false;
		
		classifierName="mlpAB";
		setWorkPathAndCheck(AppContext.getNOMINAL_CLASSIFIER_DIR()+classifierName+"\\");
		m_modelEvalFileShareMode=ModelStore.YEAR_SHARED_MODEL_AND_EVAL; //覆盖父类，设定模型和评估文件的共用模式
		
		m_noCaculationAttrib=false; //不使用计算字段
		EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
		SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.04, 0.05, 0.06, 0.07 }; // 各条均线选择样本的下限
		SAMPLE_UPPER_LIMIT =new double[] { 0.06, 0.07, 0.1, 0.11, 0.12 }; // 各条均线选择样本的上限
		TP_FP_RATIO_LIMIT=new double[] { 1.8, 1.7, 1.3, 1.1, 0.9};//选择样本阀值时TP FP RATIO从何开始
		TP_FP_BOTTOM_LINE=0.8; //TP/FP的下限
		DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
	}
		
	@Override
	protected Classifier buildModel(Instances train) throws Exception {

		MyAttributionSelectorWithPCA classifier = new MyAttributionSelectorWithPCA();

		m_cachedOldClassInstances=null; 
		int minNumObj=train.numInstances()/300;
		String batchSize=Integer.toString(minNumObj);
		MultilayerPerceptron model=new MultilayerPerceptron();
		model.setBatchSize(batchSize);
		model.setNumDecimalPlaces(6);
		model.setHiddenLayers("a");
		model.setDebug(true);
		classifier.setClassifier(model);
		classifier.setDebug(true);
		classifier.buildClassifier(train);
		System.out.println("finish buiding mlp-AB model.");

		return classifier;
	}

}
