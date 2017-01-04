package yueyueGo.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.trees.M5P;
import yueyueGo.ContinousClassifier;
import yueyueGo.ModelStore;
import yueyueGo.ParrallelizedRunning;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.utility.ClassifyUtility;



//1新模型. 2006/9/12版本
// 从mixed selected TPR相同来看，这个模型比较稳定有所提升
//2008-2016 全市场 收益率优先10-20-30-50，年平均（非年化）收益率为29%-22%-20%-16%（累计净值5.81/4.35/3.86/3.05)
//胜率优先（J48 0.3）全市场平均收益率10、20、30、50单元格，年平均（非年化收益率）为 25%、19%、17%、16%累计净值在（4.94、3.96、3.45、3.04）
//useMultiPCA=true; //bagging 内的每个模型自己有单独的PCA
//adjustThresholdBottom=false;//不用MeanABSError调整threshold
//bagging_iteration=10;	//bagging特有参数
//leafMinObjNum=300; //叶子节点最小的
//divided=300; //将trainingData分成多少份
//m_noCaculationAttrib=false; //添加计算字段!
//EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
//SAMPLE_LOWER_LIMIT = new double[]{ 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限 
//SAMPLE_UPPER_LIMIT = new double[]  { 0.06, 0.07, 0.1, 0.11, 0.12 };
//TP_FP_RATIO_LIMIT = new double[] { 1.8, 1.7, 1.5, 1.2, 1}; //选择样本阀值时TP FP RATIO到了何种值就可以提前停止了。
//TP_FP_BOTTOM_LINE=0.9; //TP/FP的下限
//===============================output summary===================================== for : baggingM5P-multiPCA
//Monthly selected_TPR mean: 24.35% standard deviation=29.32% Skewness=1.08 Kurtosis=0.22
//Monthly selected_LIFT mean : 0.74
//Monthly selected_positive summary: 7,979
//Monthly selected_count summary: 19,768
//Monthly selected_shouyilv average: 1.54% standard deviation=9.49% Skewness=4.99 Kurtosis=35.44
//Monthly total_shouyilv average: 0.98% standard deviation=6.13% Skewness=3.04 Kurtosis=15.43
//mixed selected positive rate: 40.36%
//Monthly summary_judge_result summary: good number= 283 bad number=232
//===============================end of summary=====================================for : baggingM5P-multiPCA
//result changed because of reference data not matched=0 while good change number=0
//@ winrate thredhold= /0.00% /0.00% /0.00% /0.00% /0.00% /
//number of records for full market=1412480
//shouyilv average for full market=0.80%
//selected shouyilv average for full market =2.87% count=19768
//selected shouyilv average for hs300 =0.17% count=1889
//selected shouyilv average for zz500 =1.82% count=3383

// 20160918新模型（加入波动率）结果
//useMultiPCA=true; //bagging 内的每个模型自己有单独的PCA
//m_modelEvalFileShareMode=ModelStore.HALF_YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
//adjustThresholdBottom=false;//不用MeanABSError调整threshold
//bagging_iteration=10;	//bagging特有参数
//leafMinObjNum=300; //叶子节点最小的
//divided=300; //将trainingData分成多少份
//
//m_noCaculationAttrib=false; //添加计算字段!
//EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
//SAMPLE_LOWER_LIMIT = new double[]{ 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限 
//SAMPLE_UPPER_LIMIT = new double[]  { 0.06, 0.07, 0.1, 0.11, 0.12 };
//TP_FP_RATIO_LIMIT = new double[] { 1.8, 1.7, 1.5, 1.2, 1}; //选择样本阀值时TP FP RATIO到了何种值就可以提前停止了。
//TP_FP_BOTTOM_LINE=0.9; //TP/FP的下限
//===============================output summary===================================== for : baggingM5P-multiPCA
//Monthly selected_TPR mean: 24.87% standard deviation=26.70% Skewness=1.01 Kurtosis=0.34
//Monthly selected_LIFT mean : 0.82
//Monthly selected_positive summary: 11,350
//Monthly selected_count summary: 27,992
//Monthly selected_shouyilv average: 1.03% standard deviation=8.97% Skewness=6.15 Kurtosis=63.98
//Monthly total_shouyilv average: 0.98% standard deviation=6.09% Skewness=3.06 Kurtosis=15.62
//mixed selected positive rate: 40.55%
//Monthly summary_judge_result summary: good number= 281 bad number=239
//===============================end of summary=====================================for : baggingM5P-multiPCA

//20161010(调整阀值评估方法后）
//===============================output summary===================================== for : baggingM5P-multiPCA
//Monthly selected_TPR mean: 28.42% standard deviation=22.67% Skewness=0.56 Kurtosis=-0.34
//Monthly selected_LIFT mean : 0.87
//Monthly selected_positive summary: 37,718
//Monthly selected_count summary: 99,605
//Monthly selected_shouyilv average: 1.00% standard deviation=6.05% Skewness=2.66 Kurtosis=12.83
//Monthly total_shouyilv average: 0.98% standard deviation=6.09% Skewness=3.06 Kurtosis=15.62
//mixed selected positive rate: 37.87%
//Monthly summary_judge_result summary: good number= 293 bad number=227
//===============================end of summary=====================================for : baggingM5P-multiPCA
//###### Finally selected count=30451  ######
//result changed because of reference data not matched=69154 while good change number=45169
//good ratio=65.32% average changed shouyilv=1.68% @ winrate thredhold= /45.00% /45.00% /45.00% /35.00% /25.00% /
//number of records for full market=1436457
//shouyilv average for full market=0.79%
//selected shouyilv average for full market =3.02% count=30451
//-----now output nominal predictions----------myNNAB

//20161017 
//===============================output summary===================================== for : baggingM5P-multiPCA
//Monthly selected_TPR mean: 28.37% standard deviation=22.79% Skewness=0.6 Kurtosis=-0.23
//Monthly selected_LIFT mean : 0.87
//Monthly selected_positive summary: 37,699
//Monthly selected_count summary: 99,647
//Monthly selected_shouyilv average: 1.00% standard deviation=6.03% Skewness=2.66 Kurtosis=12.89
//Monthly total_shouyilv average: 0.96% standard deviation=6.07% Skewness=3.08 Kurtosis=15.81
//mixed selected positive rate: 37.83%
//Monthly summary_judge_result summary: good number= 295 bad number=230
//===============================end of summary=====================================for : baggingM5P-multiPCA
//###### Finally selected count=38867  ######
//result changed because of reference data not matched=60780 while good change number=38788
//good ratio=63.82% average changed shouyilv=1.96% @ winrate thredhold= /45.00% /45.00% /45.00% /35.00% /25.00% /
//number of records for full market=1454589
//shouyilv average for full market=0.77%
//selected shouyilv average for full market =2.29% count=38867
//-----now output nominal predictions----------adaboost
public class BaggingM5P extends ContinousClassifier implements ParrallelizedRunning{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6252159191030935801L;
 
	public boolean m_usePCA;
	protected boolean useMultiPCA;
	protected int bagging_iteration;
	protected int leafMinObjNum;
	protected int divided;
	

	@Override
	protected void initializeParams() {
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = false;
		m_policySubGroup = new String[]{"5","10","20","30","60" };

		classifierName=ClassifyUtility.BAGGING_M5P;	
		

		useMultiPCA=true; //bagging 内的每个模型自己有单独的PCA
		

		m_modelEvalFileShareMode=ModelStore.QUARTER_YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		bagging_iteration=10;	//bagging特有参数
		leafMinObjNum=300; //叶子节点最小的
		divided=300; //将trainingData分成多少份
		
		m_noCaculationAttrib=true;//不使用计算字段 (20161215试过无计算字段，效果不如有计算字段好） 
		m_usePCA=true; //20121223尝试不使用PCA，效果不佳，恢复PCA
		m_removeSWData=true; //20161222尝试不用申万行业数据
		m_modelDataSplitMode=USE_YEAR_DATA_FOR_EVAL; //评估区间使用一年数据 （截止20170103，这个是效果最好的）
	}

	


	@Override
	protected Classifier buildModel(GeneralInstances train) throws Exception {
		//设置基础的m5p classifier参数
		M5P model=ClassifyUtility.prepareM5P(train.numInstances(),leafMinObjNum,divided);

		if (m_usePCA==true){ //使用PCA
			if (useMultiPCA==true){
				int bagging_samplePercent=70;//bagging sample 取样率
				return ClassifyUtility.buildBaggingWithMultiPCA(train,model,bagging_iteration,bagging_samplePercent);
			}else{
				int bagging_samplePercent=100;// PrePCA算袋外误差时要求percent都为100
				return ClassifyUtility.buildBaggingWithSinglePCA(train,model,bagging_iteration,bagging_samplePercent);
			}
		}else{ //不用主成分分析
			int bagging_samplePercent=70;//bagging sample 取样率
			return ClassifyUtility.buildBaggingWithoutPCA(train,model,bagging_iteration,bagging_samplePercent);
		}
	}


	

	
	@Override
	public String getIdentifyName(){
		String idenString;
		if (m_usePCA==true){ //使用PCA
			if (useMultiPCA==true){
				idenString =classifierName+ClassifyUtility.MULTI_PCA_SURFIX;
			}else{
				idenString =classifierName+ClassifyUtility.SINGLE_PCA_SURFIX;
			}
		}else{
			idenString =classifierName+ClassifyUtility.NO_PCA_SURFIX;
		}
		return idenString;
	}
	
	//	将外部的并发线程根据算法内并发的计算强度折算出新的建议值
	public int recommendRunningThreads(int runningThreads){
		int recommendThreads=1; //缺省值
		if (runningThreads>1){ //如果外部调用者是多线程运行
			if (this.m_skipTrainInBacktest==false){ //如果要重新构建模型，那最多2个线程在外面
				recommendThreads=2;
			}else if (this.m_skipEvalInBacktest==false){ //如果不需要构建模型，但需要重新评估模型，那将并发数折半
				recommendThreads=runningThreads/2;
			}else{ //如果只需要回测，简单减一后返回。
				recommendThreads=runningThreads-1;
			}
		}else{//如果外部不是多线程返回1
			recommendThreads=1;
		}
		return recommendThreads;
	}
}
