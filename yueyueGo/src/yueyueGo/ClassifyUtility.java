package yueyueGo;

import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.M5P;

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
	
	public static Bagging createBaggingRunner(int dataNum,int attribNum,int bagging_iteration,int bagging_samplePercent){
	    // set up the bagger and build the classifier
	    Bagging bagger = new Bagging();
	    bagger.setNumIterations(bagging_iteration);
	    int threads=calculateExecutionSlots(dataNum,attribNum,bagging_iteration,bagging_samplePercent);
	    bagger.setNumExecutionSlots(threads);
	    bagger.setDebug(true);
	    return bagger;

	}
	//根据内存大小，估算bagging的并发可行数目
	//	内存估算依据： 56G内存时， J48FullModelBagging 2000000个 instances 152个attributes,bagging_samplePercent=70， 10个Iteration会OOM，6个可以正常。
	// 假设 56G的80%内存都被bagging使用，可以认为（56G*0.8)除以  200万 ÷ 152 ÷ 6 是每个iteration 所需的内存数的系数
	private static int calculateExecutionSlots(int dataNum,int attribNum,int bagging_iteration,int bagging_samplePercent){
		double factor=(2000000*0.7*152*6)/(56*0.8*1024*1024*1024);
				// factor= (dataNum*attribNum*threads)/(EnvConstants.HEAP_SIZE*0.8*1024*1024*1024);
		double threads=(factor*EnvConstants.HEAP_SIZE*0.8*1024*1024*1024)/(dataNum*attribNum*bagging_samplePercent/100);
		System.out.println("caculation threads number="+threads + " while factor="+factor );
		int executionSlots=new Double(threads).intValue();
	    if (executionSlots>bagging_iteration){ //无须超过iteration个数
	    	executionSlots=bagging_iteration;
	    }		
	    if (executionSlots>EnvConstants.CPU_CORE_NUMBER-1){ //不要超过CPU个数
	    	executionSlots=EnvConstants.CPU_CORE_NUMBER-1;
	    }
	    System.out.println("final executionSlots="+executionSlots );
		return executionSlots; 
		
	}
	

}
