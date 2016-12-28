package yueyueGo.utility;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.M5P;
import yueyueGo.EnvConstants;
import yueyueGo.MyAttributionSelectorWithPCA;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaInstances;

// 设置一些基础分类器属性的方法组
public class ClassifyUtility {
	
	public static final String MULTI_PCA_SURFIX="-multiPCA";
	public static final String SINGLE_PCA_SURFIX="-singlePCA";
	public static final String NO_PCA_SURFIX="-noPCA";
	
	public static final String MLPAB="mlpAB";
	public static final String M5PAB="m5pAB";
	public static final String BAGGING_M5P="baggingM5P";
	public static final String ADABOOST="adaboost";
	public static final String MYNN_MLP="myNNAB"; //名字有AB但实际上没有做PCA
	public static final String BAGGING_LINEAR_REGRESSION="BaggingLinearRegression";
	
	
	public static final String MYNN_MLP_FULLMODEL="myNNFullModel";
	public static final String BAGGING_M5P_FULLMODEL="BaggingM5PABFullModel";
	public static final String BAGGING_J48_FULLMODEL="BaggingJ48ABFullModel";
	public static final String ADABOOST_FULLMODEL="adaboostFullModel";
	public static final String BAGGING_REGRESSION_FULLMODEL="BaggingRegessionFullModel";
	

		//设置M5P的相关参数
	public static M5P prepareM5P(int trainDataCount,int minNumObj,int divide){
		M5P model = new M5P();
		int count=trainDataCount/divide;
		if (count<minNumObj){
			count=minNumObj; //防止树过大
		}
	
		String batchSize=Integer.toString(count);
		model.setBatchSize(batchSize);
		model.setMinNumInstances(count);
		model.setNumDecimalPlaces(6);
		System.out.println(" preparing m5p model.actual minNumObj value:"+model.getMinNumInstances());
		return model;
	}

	public static J48 prepareJ48(int trainDataCount,int minNumObj,int divide){
		//设置基础的J48 classifier参数
		J48 model = new J48();
		int count=trainDataCount/divide;
		if (count<minNumObj){
			count=minNumObj; //防止树过大
		}
		String batchSize=Integer.toString(count);
		model.setBatchSize(batchSize);
		model.setMinNumObj(count);
		model.setNumDecimalPlaces(6);
		model.setDebug(true);
		System.out.println(" preparing j48 model.actual minNumObj value:"+model.getMinNumObj());
		return model;
	}
	
	private static Bagging createBaggingRunner(int dataNum,int attribNum,int bagging_iteration,int bagging_samplePercent){
	    // set up the bagger and build the classifier
	    Bagging bagger = new Bagging();
	    bagger.setNumIterations(bagging_iteration);
	    int threads=calculateExecutionSlots(dataNum,attribNum,bagging_iteration,bagging_samplePercent);
	    bagger.setNumExecutionSlots(threads);
	    bagger.setBagSizePercent(bagging_samplePercent);
	    bagger.setDebug(true);
	    return bagger;

	}
	//根据内存大小，估算bagging的并发可行数目
	//	内存估算依据：
	// 在云端： 56G内存， J48FullModelBagging 2000000个 instances 153个attributes,bagging_samplePercent=70， 10个Iteration,同时10个会OOM，6个可以正常。
	// 在本地： 6G内存,， J48FullModelBagging 550000个 instances 153个attributes,bagging_samplePercent=70， 10个Iteration 3个会OOM，2个可以正常。
	private static int calculateExecutionSlots(int dataNum,int attribNum,int bagging_iteration,int bagging_samplePercent){
		
		double threads; 
		double factor;
		int mode=EnvConstants.HEAP_SIZE;
		if (mode>=32) { //云端大计算模式
			factor=((double)EnvConstants.HEAP_SIZE/56)*((double)bagging_samplePercent/70)*(153/(double)attribNum)*(2000000/(double)dataNum);
			threads=factor*6;
		} else { //本地机器小计算模式
			factor=((double)EnvConstants.HEAP_SIZE/6)*((double)bagging_samplePercent/70)*(153/(double)attribNum)*(600000/(double)dataNum);
			threads=factor*2;
		}
		
		System.out.println("###caculation threads number="+threads + " while factor="+factor );
		int executionSlots=(int)threads;
	    if (executionSlots>bagging_iteration){ //无须超过iteration个数
	    	executionSlots=bagging_iteration;
	    }		
	    if (executionSlots>EnvConstants.CPU_CORE_NUMBER-1){ //不要超过CPU个数
	    	executionSlots=EnvConstants.CPU_CORE_NUMBER-1;
	    }
	    if (executionSlots<1){//至少应该有一个执行线程（哪怕内存不够也可以试试）
	    	System.err.println("WARNING! momeory may not enough for this bagging, anyway we try executionSlot=1");
	    	executionSlots=1;
	    }
	    
	    System.out.println("final executionSlots="+executionSlots );
		return executionSlots; 
		
	}

	//直接bagging不使用PCA
	public static Classifier buildBaggingWithoutPCA(GeneralInstances train,Classifier model,int bagging_iteration, int bagging_samplePercent) throws Exception {
		
	
		Bagging bagger=createBaggingRunner(train.numInstances(), train.numAttributes(),bagging_iteration,bagging_samplePercent);
	    
	    bagger.setClassifier(model);
	    bagger.setCalcOutOfBag(false); //不计算袋外误差
	    bagger.buildClassifier(WekaInstances.convertToWekaInstances(train));
		return bagger;
	}
	
	
	//bagging 内的每个模型自己有单独的PCA
	public static Classifier buildBaggingWithMultiPCA(GeneralInstances train,Classifier model,int bagging_iteration, int bagging_samplePercent) throws Exception {
		
		MyAttributionSelectorWithPCA classifier = new MyAttributionSelectorWithPCA();
		classifier.setDebug(true);
		classifier.setClassifier(model);
	
		Bagging bagger=createBaggingRunner(train.numInstances(), train.numAttributes(),bagging_iteration,bagging_samplePercent);
	    
	    bagger.setClassifier(classifier);
	    bagger.setCalcOutOfBag(false); //不计算袋外误差
	    
	    bagger.buildClassifier(WekaInstances.convertToWekaInstances(train));
	    
		return bagger;
	}



	//bagging 之前使用PCA，bagging大家用同一的
	public static  Classifier buildBaggingWithSinglePCA(GeneralInstances train,Classifier model,int bagging_iteration, int bagging_samplePercent) throws Exception {
	
	    // set up the bagger and build the classifier
		Bagging bagger=createBaggingRunner(train.numInstances(), train.numAttributes(),bagging_iteration,bagging_samplePercent);
		bagger.setClassifier(model);
	    bagger.setCalcOutOfBag(true); //计算袋外误差
	    
		MyAttributionSelectorWithPCA classifier = new MyAttributionSelectorWithPCA();	    
	    classifier.setDebug(true);
	    classifier.setClassifier(bagger);
	    classifier.buildClassifier(WekaInstances.convertToWekaInstances(train));
	    
		return classifier;
	}
	


	/**
	 * @param train
	 * @param layerNum
	 * 	计算多层神经网络隐藏层节点个数。
	 *  神经网络参数=输入层节点数*第一层节点数+第一层节点数*第二层节点数+第二层节点数*输出层节点数
	 *   （为了计算简便， 因为输出层节点数少，暂时可以忽略）
	 *  计算原则如下：
	 *   	1. 实例数目必须大于参数总和，一般应该十倍以上
	 *   	2. 参数的平方一般应该远小于实例数目 （十分之一）。
	 *      3. 同等情况下，我希望尝试多一层（node数大于输入层数量2倍时多分一层）
	 * @return
	 */
	public static String estimateHiddenLayerNodes(GeneralInstances train, boolean usePCA){
		int instanceNum=train.numInstances();
		double upperLimit=(double)instanceNum/10;
		int inputNodeNum=train.numAttributes();
		if (usePCA){
			inputNodeNum=inputNodeNum/2; // 假设PCA消除了一半的Attributes
		}
		
		int outputNodeNum=train.numClasses();
		
		int first;		
		int second=0;
		double temp;
		double parameterNum=0;
		String result=null;
		temp=upperLimit/inputNodeNum;
		if (temp<=inputNodeNum*2){
			first=(int)temp;
			result=String.valueOf(first);
			parameterNum=inputNodeNum*first+first*outputNodeNum;
		}else{ //当节点数超过一定量时，考虑多分一层，为计算简便起见第一层起始时就保持inputNodeNum这样多，迭代计算
			double factor=1;
			do{
				first=(int)(inputNodeNum*factor);
				temp= (upperLimit- inputNodeNum*first)/first;  
				second=(int)temp;
				result=String.valueOf(first)+","+String.valueOf(second);
				parameterNum=inputNodeNum*first+first*second+second*outputNodeNum;
				factor*=1.05; //将第一层递增20%试验
			} while (second>first); //如果second>first，持续迭代计算
		}
		
		System.out.println("estimated hidden layer parameters=  "+result + " while parameterNum= "+parameterNum +" instance/paramNum= "+FormatUtility.formatDouble(instanceNum/parameterNum));
		return result;
		
	}
	
	


	//评估时输出confusionMatrix
	public static Evaluation getConfusionMatrix(GeneralInstances trainData,GeneralInstances evalData, Classifier model,boolean isNominal)
			throws Exception {
	
		System.out.println("evluation with full incoming dataset, size: "+evalData.numInstances());
		Evaluation eval = new Evaluation(WekaInstances.convertToWekaInstances(trainData));
		eval.evaluateModel(model, WekaInstances.convertToWekaInstances(evalData)); // evaluate on the sample data to get threshold
		System.out.println(eval.toSummaryString("\nEvaluate Model Results\n\n", true));
	
		if (isNominal==true){
			System.out.println(eval.toMatrixString ("\nEvaluate Confusion Matrix\n\n"));
			System.out.println(eval.toClassDetailsString("\nEvaluate Class Details\n\n"));
		}
	
		return eval;
	}
}
