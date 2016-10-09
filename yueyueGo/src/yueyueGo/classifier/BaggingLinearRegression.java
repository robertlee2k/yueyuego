package yueyueGo.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instances;
import weka.core.SelectedTag;
import yueyueGo.ContinousClassifier;
import yueyueGo.ModelStore;
import yueyueGo.ParrallelizedRunning;
import yueyueGo.utility.ClassifyUtility;

public class BaggingLinearRegression extends ContinousClassifier implements ParrallelizedRunning {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4124135341808054675L;

//	protected boolean useMultiPCA;
	protected int bagging_iteration;
	protected int divided;
	

	@Override
	protected void initializeParams() {
		m_skipTrainInBacktest = false;
		m_skipEvalInBacktest = false;
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		classifierName=ClassifyUtility.BAGGING_LINEAR_REGRESSION;	
//		useMultiPCA=true; //bagging 内的每个模型自己有单独的PCA
		m_modelEvalFileShareMode=ModelStore.YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		bagging_iteration=3;	//bagging特有参数
		divided=300; //将trainingData分成多少份
		
		m_noCaculationAttrib=false; //添加计算字段!

	}

	


	@Override
	protected Classifier buildModel(Instances train) throws Exception {
		
		LinearRegression model=new LinearRegression();
		int count=train.numInstances()/divided;
		String batchSize=Integer.toString(count);
		model.setBatchSize(batchSize);
		model.setNumDecimalPlaces(6);
		model.setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_M5, LinearRegression.TAGS_SELECTION));
		model.setDebug(false);
		
		
		int bagging_samplePercent=80;//bagging sample 取样率
		return ClassifyUtility.buildBaggingWithoutPCA(train,model,bagging_iteration,bagging_samplePercent);
		
//		if (useMultiPCA==true){
//			int bagging_samplePercent=80;//bagging sample 取样率
//			return ClassifyUtility.buildBaggingWithMultiPCA(train,model,bagging_iteration,bagging_samplePercent);
//		}else{
//			int bagging_samplePercent=100;// PrePCA算袋外误差时要求percent都为100
//			return ClassifyUtility.buildBaggingWithSinglePCA(train,model,bagging_iteration,bagging_samplePercent);
//		}
	}
	
//	@Override
//	public String getIdentifyName(){
//		String idenString;
//		if (useMultiPCA==true){
//			idenString =classifierName+ClassifyUtility.MULTI_PCA_SURFIX;
//		}else{
//			idenString =classifierName+"-singlePCA";
//		}
//
//		return idenString;
//	}
	
	//	将外部的并发线程根据算法内并发的计算强度折算出新的建议值
	public int recommendRunningThreads(int runningThreads){
		int recommendThreads=0; 
		if (runningThreads>1){ //如果外部调用者是多线程运行
			if (this.m_skipTrainInBacktest==false){ //如果要重新构建模型，外部线程
				recommendThreads=runningThreads/3;
			}else if (this.m_skipEvalInBacktest==false){ //如果不需要构建模型，但需要重新评估模型，那将并发数折半
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
