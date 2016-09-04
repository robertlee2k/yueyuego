package yueyueGo;

import weka.classifiers.Classifier;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.M5P;
import weka.core.Instances;

// 设置一些基础分类器属性的方法组
public class ClassifyUtility {

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
		
		System.out.println("caculation threads number="+threads + " while factor="+factor );
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

	//bagging 内的每个模型自己有单独的PCA
	public static Classifier buildBaggingWithMultiPCA(Instances train,Classifier model,int bagging_iteration, int bagging_samplePercent) throws Exception {
		
		MyAttributionSelectorWithPCA classifier = new MyAttributionSelectorWithPCA();
		classifier.setDebug(true);
		classifier.setClassifier(model);
	
		Bagging bagger=createBaggingRunner(train.numInstances(), train.numAttributes(),bagging_iteration,bagging_samplePercent);
	    
	    bagger.setClassifier(classifier);
	    bagger.setCalcOutOfBag(false); //不计算袋外误差
	    bagger.buildClassifier(train);
		return bagger;
	}

	//bagging 之前使用PCA，bagging大家用同一的
	public static  Classifier buildBaggingWithSinglePCA(Instances train,Classifier model,int bagging_iteration, int bagging_samplePercent) throws Exception {
	
	    // set up the bagger and build the classifier
		Bagging bagger=createBaggingRunner(train.numInstances(), train.numAttributes(),bagging_iteration,bagging_samplePercent);
		bagger.setClassifier(model);
	    bagger.setCalcOutOfBag(true); //计算袋外误差
	    
		MyAttributionSelectorWithPCA classifier = new MyAttributionSelectorWithPCA();	    
	    classifier.setDebug(true);
	    classifier.setClassifier(bagger);
	    classifier.buildClassifier(train);
	    
		return classifier;
	}
	

}
