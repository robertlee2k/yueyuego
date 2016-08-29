package yueyueGo.classifier;
//attribution Selection for M5P 用主成份分析法


import weka.classifiers.Classifier;
import weka.classifiers.trees.M5P;
import weka.core.Instances;
import yueyueGo.ContinousClassifier;
import yueyueGo.MyAttributionSelectorWithPCA;


// 2016-07-19 选择 
//1. 20单元格胜率优先年化16%
//EVAL_RECENT_PORTION = 0.9; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
//SAMPLE_LOWER_LIMIT = new double[]{ 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限 
//SAMPLE_UPPER_LIMIT = new double[]  { 0.06, 0.07, 0.1, 0.11, 0.12 };
//TP_FP_RATIO_LIMIT = new double[] { 1.8, 1.7, 1.5, 1.2, 1}; //选择样本阀值时TP FP RATIO到了何种值就可以停止了。
//TP_FP_BOTTOM_LINE=0.9; //TP/FP的下限
//===============================output summary===================================== for : m5pAB
//Monthly selected_TPR mean: 25.24% standard deviation=26.90% Skewness=0.93 Kurtosis=0.08
//Monthly selected_LIFT mean : 0.78
//Monthly selected_positive summary: 9,700
//Monthly selected_count summary: 25,232
//Monthly selected_shouyilv average: 1.12% standard deviation=7.54% Skewness=4.43 Kurtosis=30.96
//Monthly total_shouyilv average: 1.00% standard deviation=6.15% Skewness=3.02 Kurtosis=15.27
//mixed selected positive rate: 38.44%
//Monthly summary_judge_result summary: good number= 281 bad number=229
//===============================end of summary=====================================for : m5pAB

//2. 新模型 未引入meanabserr，minobj为1000时
//===============================output summary===================================== for : m5pAB
//Monthly selected_TPR mean: 24.80% standard deviation=25.64% Skewness=0.99 Kurtosis=0.39
//Monthly selected_LIFT mean : 0.79
//Monthly selected_positive summary: 9,008
//Monthly selected_count summary: 23,894
//Monthly selected_shouyilv average: 0.69% standard deviation=8.01% Skewness=9.06 Kurtosis=133.61
//Monthly total_shouyilv average: 0.98% standard deviation=6.13% Skewness=3.04 Kurtosis=15.43
//mixed selected positive rate: 37.70%
//Monthly summary_judge_result summary: good number= 260 bad number=255
//===============================end of summary=====================================for : m5pAB

//3. 20160819新模型（按年评估，meanabserr引入阀值）
//2008-2016 全市场 收益率优先20-30-50， 15%-17%之间 2009，2014，2015年净值不错 （如果采用胜率优先可做到18%）
//沪深300收益率和全市场分布基本一致
//===============================output summary===================================== for : m5pAB
//Monthly selected_TPR mean: 31.50% standard deviation=12.20% Skewness=-0.05 Kurtosis=0.54
//Monthly selected_LIFT mean : 1.04
//Monthly selected_positive summary: 10,922
//Monthly selected_count summary: 29,287
//Monthly selected_shouyilv average: 1.29% standard deviation=3.48% Skewness=0.47 Kurtosis=0.7
//Monthly total_shouyilv average: 1.17% standard deviation=2.60% Skewness=1.43 Kurtosis=1.34
//mixed selected positive rate: 37.29%
//Monthly summary_judge_result summary: good number= 21 bad number=24
//===============================end of summary=====================================for : m5pAB
//m_noCaculationAttrib=false; //添加计算字段
//m_sepeperate_eval_HS300=false;//单独为HS300评估阀值
//m_seperate_classify_HS300=false; //M5P不适用沪深300，缺省不单独评估HS300
//EVAL_RECENT_PORTION = 0.9; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
//SAMPLE_LOWER_LIMIT = new double[]{ 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限 
//SAMPLE_UPPER_LIMIT = new double[]  { 0.06, 0.07, 0.1, 0.11, 0.12 };
//TP_FP_RATIO_LIMIT = new double[] { 1.8, 1.7, 1.5, 1.2, 1}; //选择样本阀值时TP FP RATIO到了何种值就可以提前停止了。
//TP_FP_BOTTOM_LINE=0.9; //TP/FP的下限
//m5p特有参数 int leafMinObjNum=300;

//4.其他参数与上面相同，只是改成按月评估
//2008-2016 全市场 收益率优先20-30-50， 16%-18%-17%之间 2009，2014，2015年净值不错 (胜率优先也差不多）
//===============================output summary===================================== for : m5pAB
//Monthly selected_TPR mean: 29.01% standard deviation=25.75% Skewness=0.83 Kurtosis=0.2
//Monthly selected_LIFT mean : 1.06
//Monthly selected_positive summary: 10,187
//Monthly selected_count summary: 27,830
//Monthly selected_shouyilv average: 1.22% standard deviation=8.62% Skewness=6.84 Kurtosis=84.68
//Monthly total_shouyilv average: 0.98% standard deviation=6.13% Skewness=3.04 Kurtosis=15.43
//mixed selected positive rate: 36.60%
//Monthly summary_judge_result summary: good number= 280 bad number=235
//===============================end of summary=====================================for : m5pAB

public class M5PABClassifier extends ContinousClassifier {
	public M5PABClassifier() {
		super();
		classifierName = "m5pAB";
		WORK_PATH =WORK_PATH+classifierName+"\\";

		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = false;
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		
		m_noCaculationAttrib=false; //添加计算字段
		EVAL_RECENT_PORTION = 0.9; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
		SAMPLE_LOWER_LIMIT = new double[]{ 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限 
		SAMPLE_UPPER_LIMIT = new double[]  { 0.06, 0.07, 0.1, 0.11, 0.12 };
		TP_FP_RATIO_LIMIT = new double[] { 1.8, 1.7, 1.5, 1.2, 1}; //选择样本阀值时TP FP RATIO到了何种值就可以提前停止了。
		TP_FP_BOTTOM_LINE=0.9; //TP/FP的下限
	}
	
	
	@Override
	protected Classifier buildModel(Instances train) throws Exception {

		//m5p特有参数
		int leafMinObjNum=300;
		
		//设置基础的m5p classifier参数
		MyAttributionSelectorWithPCA classifier = new MyAttributionSelectorWithPCA();

		M5P model = new M5P();
		int minNumObj=train.numInstances()/300;
		if (minNumObj<leafMinObjNum){
			minNumObj=leafMinObjNum; //防止树过大
		}
		String batchSize=Integer.toString(minNumObj);
		model.setBatchSize(batchSize);
		model.setMinNumInstances(minNumObj);
		model.setNumDecimalPlaces(6);

		classifier.setClassifier(model);
		classifier.buildClassifier(train);
		System.out.println("finish buiding m5p-AB model. minNumObj value:"+minNumObj);
		
		return classifier;
	}
	
	@Override
	public Classifier loadModel(String yearSplit, String policySplit) throws Exception{
		//这是为M5PAP单独准备的模型，模型文件是按年读取，但evaluation文件不变仍按月
		int inputYear=Integer.parseInt(yearSplit.substring(0,4));

		String filename=this.WORK_PATH+this.WORK_FILE_PREFIX +"-"+this.classifierName+ "-" + inputYear + MA_PREFIX + policySplit;//如果使用固定模型
		
		this.setModelFileName(filename);

	
		return loadModelFromFile();
	}	
}
