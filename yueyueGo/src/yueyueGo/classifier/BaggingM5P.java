package yueyueGo.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.trees.M5P;
import weka.core.Instances;
import yueyueGo.ContinousClassifier;
import yueyueGo.ModelStore;
import yueyueGo.utility.ClassifyUtility;
import yueyueGo.utility.AppContext;
import yueyueGo.utility.ThresholdData;



//1新模型. 2006/9/12版本
// 从mixed selected TPR相同来看，这个模型比较稳定,比按年评估模型5有所提升
//2008-2016 全市场 收益率优先10-20-30-50，年平均收益率为22%-25%-23%-18%（累计净值3.9/4.8/4.1/3.1)
//胜率优先收益率（累计净值在2.5-3.6之间)
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

public class BaggingM5P extends ContinousClassifier {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6252159191030935801L;
	protected boolean adjustThresholdBottom; 
	protected boolean useMultiPCA;
	protected int bagging_iteration;
	protected int leafMinObjNum;
	protected int divided;
	
	@Override
	protected void initializeParams() {
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = true;
		m_policySubGroup = new String[]{"5","10","20","30","60" };

		classifierName="baggingM5P";	
		useMultiPCA=true; //bagging 内的每个模型自己有单独的PCA
		setWorkPathAndCheck(AppContext.getCONTINOUS_CLASSIFIER_DIR()+this.getIdentifyName()+"\\");
		m_modelEvalFileShareMode=ModelStore.HALF_YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		
		
		adjustThresholdBottom=false;//不用MeanABSError调整threshold
		bagging_iteration=10;	//bagging特有参数
		leafMinObjNum=300; //叶子节点最小的
		divided=300; //将trainingData分成多少份
		
		m_noCaculationAttrib=false; //添加计算字段!
		EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
		SAMPLE_LOWER_LIMIT = new double[]{ 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限 
		SAMPLE_UPPER_LIMIT = new double[]  { 0.06, 0.07, 0.1, 0.11, 0.12 };
		TP_FP_RATIO_LIMIT = new double[] { 1.8, 1.7, 1.5, 1.2, 1}; //选择样本阀值时TP FP RATIO到了何种值就可以提前停止了。
		TP_FP_BOTTOM_LINE=0.9; //TP/FP的下限
	}

	


	@Override
	protected Classifier buildModel(Instances train) throws Exception {
		//设置基础的m5p classifier参数
		M5P model=ClassifyUtility.prepareM5P(train.numInstances(),leafMinObjNum,divided);

		if (useMultiPCA==true){
			int bagging_samplePercent=70;//bagging sample 取样率
			return ClassifyUtility.buildBaggingWithMultiPCA(train,model,bagging_iteration,bagging_samplePercent);
		}else{
			int bagging_samplePercent=100;// PrePCA算袋外误差时要求percent都为100
			return ClassifyUtility.buildBaggingWithSinglePCA(train,model,bagging_iteration,bagging_samplePercent);
		}
	}


	
	@Override
	protected ThresholdData processThresholdData(ThresholdData eval){
		if (adjustThresholdBottom==true){
			double adjustedBottom=(eval.getThresholdMin()+eval.getMeanABError())/2;
			System.out.println("----adjusted threshold bottom is :"+Double.toString(adjustedBottom)+ " because meanABError="+Double.toString(eval.getMeanABError()));
			eval.setThresholdMin(adjustedBottom);
		}
		return eval;
	}
	
	@Override
	public String getIdentifyName(){
		String idenString;
		if (useMultiPCA==true){
			idenString =classifierName+"-multiPCA";
		}else{
			idenString =classifierName+"-singlePCA";
		}

		return idenString;
	}
}
