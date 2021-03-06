package yueyueGo.classifier.deprecated;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.SelectedTag;
import yueyueGo.ContinousModel;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.utility.ClassifyUtility;
import yueyueGo.utility.ParrallelizedRunning;
import yueyueGo.utility.modelEvaluation.ModelStore;


//===============================output summary===================================== for : BaggingLinearRegression
//Monthly selected_TPR mean: 27.19% standard deviation=25.71% Skewness=0.85 Kurtosis=0.06
//Monthly selected_LIFT mean : 0.93
//Monthly selected_positive summary: 41,602
//Monthly selected_count summary: 108,650
//Monthly selected_shouyilv average: 1.01% standard deviation=6.43% Skewness=3.49 Kurtosis=23.59
//Monthly total_shouyilv average: 0.98% standard deviation=6.09% Skewness=3.06 Kurtosis=15.62
//mixed selected positive rate: 38.29%
//Monthly summary_judge_result summary: good number= 287 bad number=233
//===============================end of summary=====================================for : BaggingLinearRegression
//###### Finally selected count=41348  ######
//result changed because of reference data not matched=67302 while good change number=42539
//good ratio=63.21% average changed shouyilv=2.27% @ winrate thredhold= /45.00% /45.00% /45.00% /35.00% /25.00% /
//number of records for full market=1436457
//shouyilv average for full market=0.79%
//selected shouyilv average for full market =2.29% count=41348


//20161017
//===============================output summary===================================== for : BaggingLinearRegression-multiPCA
//Monthly selected_TPR mean: 30.87% standard deviation=26.14% Skewness=0.7 Kurtosis=-0.13
//Monthly selected_LIFT mean : 0.97
//Monthly selected_positive summary: 38,376
//Monthly selected_count summary: 95,881
//Monthly selected_shouyilv average: 1.11% standard deviation=6.54% Skewness=1.91 Kurtosis=6.81
//Monthly total_shouyilv average: 0.96% standard deviation=6.07% Skewness=3.08 Kurtosis=15.81
//mixed selected positive rate: 40.02%
//Monthly summary_judge_result summary: good number= 319 bad number=206
//===============================end of summary=====================================for : BaggingLinearRegression-multiPCA
//###### Finally selected count=39216  ######
//result changed because of reference data not matched=56665 while good change number=34740
//good ratio=61.31% average changed shouyilv=3.74% @ winrate thredhold= /45.00% /45.00% /45.00% /35.00% /25.00% /
//number of records for full market=1454589
//shouyilv average for full market=0.77%
//selected shouyilv average for full market =3.17% count=39216
//-----now output nominal predictions----------adaboost
@Deprecated
public class BaggingLinearRegression extends ContinousModel implements ParrallelizedRunning {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4124135341808054675L;

	protected boolean useMultiPCA;
	protected int bagging_iteration;
	protected int divided;
	

	@Override
	protected void overrideParams() {

		m_policySubGroup = new String[]{"5","10","20","30","60" };
		classifierName=ClassifyUtility.BAGGING_LINEAR_REGRESSION;	
		useMultiPCA=true; //bagging 内的每个模型自己有单独的PCA
		m_modelFileShareMode=ModelStore.HALF_YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		bagging_iteration=10;	//bagging特有参数
		divided=300; //将trainingData分成多少份
		

	}

	


	@Override
	protected Classifier buildModel(GeneralInstances train) throws Exception {
		
		LinearRegression model=new LinearRegression();
		int count=train.numInstances()/divided;
		String batchSize=Integer.toString(count);
		model.setBatchSize(batchSize);
		model.setNumDecimalPlaces(6);
		model.setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_GREEDY, LinearRegression.TAGS_SELECTION));
		model.setDebug(false);
		
		
//		int bagging_samplePercent=70;//bagging sample 取样率
//		return ClassifyUtility.buildBaggingWithoutPCA(train,model,bagging_iteration,bagging_samplePercent);
		
//		if (useMultiPCA==true){
//			int bagging_samplePercent=70;//bagging sample 取样率
//			return ClassifyUtility.buildBaggingWithMultiPCA(train,model,bagging_iteration,bagging_samplePercent);
//		}else{
//			int bagging_samplePercent=100;// PrePCA算袋外误差时要求percent都为100
//			return ClassifyUtility.buildBaggingWithSinglePCA(train,model,bagging_iteration,bagging_samplePercent);
//		}
		return null;
	}
	
	@Override
	public String getIdentifyName(){
		String idenString;
		if (useMultiPCA==true){
			idenString =classifierName+ClassifyUtility.MULTI_PCA_SURFIX;
		}else{
			idenString =classifierName+"-singlePCA";
		}

		return idenString;
	}
	
	//	将外部的并发线程根据算法内并发的计算强度折算出新的建议值
	public int recommendRunningThreads(int runningThreads){
		int recommendThreads=0; 
		if (runningThreads>1){ //如果外部调用者是多线程运行
			if (this.is_skipTrainInBacktest()==false){ //如果要重新构建模型，外部线程
				recommendThreads=runningThreads/3;
			}else if (this.is_skipEvalInBacktest()==false){ //如果不需要构建模型，但需要重新评估模型，那将并发数折半
				recommendThreads=runningThreads/2;
			}else{ //如果只需要回测，简单减一后返回。
				recommendThreads=runningThreads-1;
			}
		}
		
		if(recommendThreads<1){ //若算出来的线程数小于1，则返回1
			recommendThreads=1;
		}
		return recommendThreads;
	}
}
