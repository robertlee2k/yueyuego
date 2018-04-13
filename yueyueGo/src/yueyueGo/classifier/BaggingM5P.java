package yueyueGo.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.trees.M5P;
import yueyueGo.ContinousModel;
import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.dataFormat.AvgLineDataFormat;
import yueyueGo.dataFormat.MomentumDataFormat;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.utility.ClassifyUtility;
import yueyueGo.utility.ParrallelizedRunning;
import yueyueGo.utility.classiferWrapper.MyAttributionSelectorWithPCA;
import yueyueGo.utility.modelEvaluation.EvaluationStore;
import yueyueGo.utility.modelEvaluation.ModelStore;



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

//2011年和2013年回撤比较大。
//***************************************CLASSIFY DATE=2017-01-04 
//ClassifyIdentity=baggingM5P-multiPCA
//m_skipTrainInBacktest=true
//m_skipEvalInBacktest=false
//m_noCaculationAttrib=true
//m_removeSWData=true
//m_positiveLine=0.0
//m_modelDataSplitMode=12
//m_modelEvalFileShareMode=3
//modelArffFormat=2
//SAMPLE_LOWER_LIMIT={0.03,0.03,0.04,0.04,0.04,}
//SAMPLE_UPPER_LIMIT={0.1,0.1,0.1,0.1,0.1,}
//LIFT_UP_TARGET=1.8
//***************************************
//......................
//===============================output summary===================================== for : baggingM5P-multiPCA
//Monthly selected_TPR mean: 28.85% standard deviation=23.80% Skewness=0.83 Kurtosis=0.26
//Monthly selected_LIFT mean : 0.97
//Monthly selected_positive summary: 25,985
//Monthly selected_count summary: 73,727
//Monthly selected_shouyilv average: 1.02% standard deviation=8.19% Skewness=6.49 Kurtosis=76.21
//Monthly total_shouyilv average: 0.83% standard deviation=6.01% Skewness=3.07 Kurtosis=15.82
//mixed selected positive rate: 35.24%
//Monthly summary_judge_result summary: good number= 289 bad number=246
//===============================end of summary=====================================for : baggingM5P-multiPCA


//***************************************CLASSIFY DATE=2017-01-05 
//ClassifyIdentity=baggingM5P-multiPCA
//m_skipTrainInBacktest=true
//m_skipEvalInBacktest=false
//m_noCaculationAttrib=true
//m_removeSWData=true
//m_positiveLine=0.0
//m_modelDataSplitMode=12
//m_modelEvalFileShareMode=3
//modelArffFormat=2
//SAMPLE_LOWER_LIMIT={0.03,0.03,0.03,0.03,0.03,}
//SAMPLE_UPPER_LIMIT={0.1,0.1,0.1,0.1,0.1,}
//LIFT_UP_TARGET=1.8
//***************************************
//......................
//===============================output summary===================================== for : baggingM5P-multiPCA
//Monthly selected_TPR mean: 28.56% standard deviation=24.12% Skewness=0.83 Kurtosis=0.23
//Monthly selected_LIFT mean : 0.95
//Monthly selected_positive summary: 24,406
//Monthly selected_count summary: 68,645
//Monthly selected_shouyilv average: 1.03% standard deviation=8.13% Skewness=6.53 Kurtosis=77.79
//Monthly total_shouyilv average: 0.83% standard deviation=6.01% Skewness=3.07 Kurtosis=15.82
//mixed selected positive rate: 35.55%
//Monthly summary_judge_result summary: good number= 290 bad number=245
//===============================end of summary=====================================for : baggingM5P-multiPCA
//......................
//-----now output continuous predictions----------baggingM5P-multiPCA
//incoming resultData size, row=1477574 column=6
//incoming referenceData size, row=1477574 column=6
//Left data loaded, row=1760436 column=12
//number of results merged and processed: 1477574
//###### Finally selected count=29736  ######
//WINRATE_FILTER_FOR_SHOUYILV={0.4,0.4,0.35,0.35,0.3,}
//SHOUYILV_FILTER_FOR_WINRATE={0.01,0.01,0.02,0.03,0.04,}
// result changed because of reference data not matched=38909 while good change number=26244
// good ratio=67.45% average changed shouyilv=1.58% @ winrate thredhold= /40.00% /40.00% /35.00% /35.00% /30.00% /
//number of records for full market=1477574
//shouyilv average for full market=0.6578%
//selected shouyilv average for full market =2.1422% count=29736

//***************************************CLASSIFY DATE=2017-01-05 
//ClassifyIdentity=baggingM5P-multiPCA
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
//===============================output summary===================================== for : baggingM5P-multiPCA
//Monthly selected_TPR mean: 29.34% standard deviation=23.90% Skewness=0.7 Kurtosis=-0.17
//Monthly selected_LIFT mean : 0.99
//Monthly selected_positive summary: 23,968
//Monthly selected_count summary: 67,028
//Monthly selected_shouyilv average: 1.05% standard deviation=7.45% Skewness=3.65 Kurtosis=23.18
//Monthly total_shouyilv average: 0.83% standard deviation=6.01% Skewness=3.07 Kurtosis=15.82
//mixed selected positive rate: 35.76%
//Monthly summary_judge_result summary: good number= 308 bad number=227
//===============================end of summary=====================================for : baggingM5P-multiPCA
//......................
//-----now output continuous predictions----------baggingM5P-multiPCA
//incoming resultData size, row=1477574 column=6
//incoming referenceData size, row=1477574 column=6
//Left data loaded, row=1760436 column=12
//number of results merged and processed: 1477574
//###### Finally selected count=21495  ######
//WINRATE_FILTER_FOR_SHOUYILV={0.5,0.5,0.45,0.45,0.4,}
//SHOUYILV_FILTER_FOR_WINRATE={0.01,0.02,0.03,0.04,0.05,}
// result changed because of reference data not matched=45533 while good change number=30432
// good ratio=66.84% average changed shouyilv=1.38% @ winrate thredhold= /50.00% /50.00% /45.00% /45.00% /40.00% /
//number of records for full market=1477574
//shouyilv average for full market=0.6578%
//selected shouyilv average for full market =2.2170% count=21495

//-----now output continuous predictions----------baggingM5P-multiPCA
//incoming resultData size, row=1477574 column=6
//incoming referenceData size, row=1477574 column=6
//Left data loaded, row=1760436 column=12
//number of results merged and processed: 1477574
//###### Finally selected count=29233  ######
//WINRATE_FILTER_FOR_SHOUYILV={0.4,0.4,0.35,0.35,0.3,}
//SHOUYILV_FILTER_FOR_WINRATE={0.01,0.01,0.02,0.03,0.04,}
//result changed because of reference data not matched=37795 while good change number=25570
//good ratio=67.65% average changed shouyilv=1.31% @ winrate thredhold= /40.00% /40.00% /35.00% /35.00% /30.00% /
//number of records for full market=1477574
//shouyilv average for full market=0.6578%
//selected shouyilv average for full market =2.0902% count=29233


//***************************************CLASSIFY DATE=2017-04-24 
//ClassifyIdentity=baggingM5P-multiPCA
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
//===============================output summary===================================== for : baggingM5P-multiPCA format=9
//Monthly selected_TPR mean: 29.21% standard deviation=23.88% Skewness=0.88 Kurtosis=0.46
//Monthly selected_LIFT mean : 1.01
//Monthly selected_positive summary: 25,038
//Monthly selected_count summary: 68,738
//Monthly selected_shouyilv average: 0.70% standard deviation=6.28% Skewness=2.44 Kurtosis=12.52
//Monthly total_shouyilv average: 0.74% standard deviation=5.92% Skewness=3.14 Kurtosis=16.5
//mixed selected positive rate: 36.43%
//Monthly summary_judge_result summary: good number= 316 bad number=239
//===============================end of summary=====================================for : baggingM5P-multiPCA format=9
//......................
//-----now output continuous predictions----------baggingM5P-multiPCA (filtered by nominal: adaboost)
//incoming resultData size, row=1548274 column=6
//incoming referenceData size, row=1548274 column=6
//Left data loaded, row=1831136 column=12
//number of results merged and processed: 1548274
//###### Finally selected count=30995  ######
// result changed because of reference data not matched=37743 while good change number=25012
// good ratio=66.27% average changed shouyilv=1.40%
// @ WINRATE_FILTER_FOR_SHOUYILV={40.00%,40.00%,35.00%,35.00%,30.00%, }
//number of records for full market=1548274
//shouyilv average for full market=0.5761%
//selected shouyilv average for full market =2.0438% count=30995
public class BaggingM5P extends ContinousModel implements ParrallelizedRunning{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6252159191030935801L;
 
	public boolean m_usePCA;
	public boolean useMultiPCA;
	public int bagging_iteration;
	public int leafMinObjNum;
	public int divided;
	public int m_preprocesingBeforePCA;

	public static BaggingM5P initModel(ArffFormat format,int purpose) throws Exception{
		BaggingM5P model=null;
		model=new BaggingM5P();
		model.classifierName=ClassifyUtility.BAGGING_M5P;	
		model.initModelPurpose(purpose);
		 if (format instanceof AvgLineDataFormat){
			 	model.m_policySubGroup = new String[]{"5","10","20-30-60" };		
			 	model.m_usePCA=true;  //20180413使用PCA //20180213尝试不使用PCA
			 	model.useMultiPCA=true; //bagging 内的每个模型自己有单独的PCA
			 	model.m_normalize=false; //在进入分类器之前不需要对数据做Normalize  （目前这个参数只有在PCA时有效，以后再改）  
			 	model.m_preprocesingBeforePCA=MyAttributionSelectorWithPCA.STANDARDIZE_DATA;//MyAttributionSelectorWithPCA.CENTER_DATA;
	
			 	model.bagging_iteration=10;	//bagging特有参数
			 	model.leafMinObjNum=300; //叶子节点最小的
			 	model.divided=500; //将trainingData分成多少份
	
			 	model.m_modelFileShareMode=ModelStore.QUARTER_SHARED_MODEL;    //20180213 使用三个月模型，覆盖父类，设定模型和评估文件的共用模式
			 	model.m_evalDataSplitMode=EvaluationStore.USE_HALF_YEAR_DATA_FOR_EVAL; //评估区间使用6个月数据 
			 	model.m_dataYearsToCompare=new int[]{ModelStore.FIVE_YEAR_DATA,ModelStore.THREE_YEAR_DATA,ModelStore.ONE_YEAR_DATA};	//在不同的数据周期中评估比较哪个模型文件数据更合适（此处比5年、3年和1年）
		 }else if (format instanceof MomentumDataFormat){
			//设置动量策略参数
			model.m_policySubGroup = new String[]{"" };
			model.m_usePCA=true; 
			model.useMultiPCA=true;
			model.bagging_iteration=10;	//bagging特有参数
			model.leafMinObjNum=300; //叶子节点最小的
			model.divided=300; //将trainingData分成多少份
			model.m_modelFileShareMode=ModelStore.QUARTER_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
			model.m_evalDataSplitMode=EvaluationStore.USE_NINE_MONTHS_DATA_FOR_EVAL;//USE_YEAR_DATA_FOR_EVAL; //评估区间使用一年数据 （截止20170103，这个是效果最好的）
		}else{
			throw new Exception ("undefined dataFormat!");
		}
		return model;
	}



	@Override
	protected Classifier buildModel(GeneralInstances train) throws Exception {
		//设置基础的m5p classifier参数
		M5P model=ClassifyUtility.prepareM5P(train.numInstances(),leafMinObjNum,divided);

		if (m_usePCA==true){ //使用PCA
			if (useMultiPCA==true){
				int bagging_samplePercent=70;//bagging sample 取样率
				return ClassifyUtility.buildBaggingWithMultiPCA(train,model,bagging_iteration,bagging_samplePercent,m_normalize,m_preprocesingBeforePCA);
			}else{
				int bagging_samplePercent=100;// PrePCA算袋外误差时要求percent都为100
				return ClassifyUtility.buildBaggingWithSinglePCA(train,model,bagging_iteration,bagging_samplePercent,m_normalize,m_preprocesingBeforePCA);
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
		int recommendThreads=0; //缺省值
		if (runningThreads>1){ //如果外部调用者是多线程运行
			if (this.is_skipTrainInBacktest()==false){ //如果要重新构建模型，那最多1个线程在外面
				recommendThreads=2;
			}else if (this.is_skipEvalInBacktest()==false){ //如果不需要构建模型，但需要重新评估模型，那将外部并发除以3
				recommendThreads=runningThreads/2;
			}else{ //如果只需要回测，简单除以2后返回。
				recommendThreads=runningThreads/3;
			}
		}else{//如果外部不是多线程返回1
			recommendThreads=1;
		}
		if (recommendThreads<1) recommendThreads=1;
		return recommendThreads;
	}
}
