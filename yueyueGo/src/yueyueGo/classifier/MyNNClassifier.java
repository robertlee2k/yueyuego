package yueyueGo.classifier;

import weka.classifiers.Classifier;
import weka.core.Instances;
import yueyueGo.EnvConstants;
import yueyueGo.ModelStore;
import yueyueGo.NominalClassifier;
import yueyueGo.ParrallelizedRunning;
import yueyueGo.utility.ClassifyUtility;
import ext.WekaNeuralNetwork;

public class MyNNClassifier extends NominalClassifier implements ParrallelizedRunning {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4545814686082746827L;

//	protected String m_hiddenLayer; //NN的隐藏层参数
	protected int m_thread; //NN的并发线程数参数
	protected double m_learningRate; //NN的learningRate
//	protected double m_dropOutRate; // NN的dropoutrate参数
	
	@Override
	protected void initializeParams()  {
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = false;
		
		classifierName=ClassifyUtility.MYNN_MLP;
//		setWorkPathAndCheck(AppContext.getNOMINAL_CLASSIFIER_DIR()+classifierName+"\\");
		m_modelEvalFileShareMode=ModelStore.HALF_YEAR_SHARED_MODEL;//SEPERATE_MODEL_AND_EVAL;// YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		
		
		m_thread=EnvConstants.CPU_CORE_NUMBER-1;
		m_learningRate=0.1; //缺省用
//		m_dropOutRate=0;
		
		m_noCaculationAttrib=true; //不使用计算字段

	}
		
	@Override
	protected Classifier buildModel(Instances train) throws Exception {

//		MyAttributionSelectorWithPCA classifier = new MyAttributionSelectorWithPCA();

 
		WekaNeuralNetwork model=new WekaNeuralNetwork();
		model.setNumDecimalPlaces(6);
		int minNumObj=train.numInstances()/500;
		String batchSize=Integer.toString(minNumObj);
		model.setBatchSize(batchSize);
		model.setLearningRate(m_learningRate); 
		model.setHiddenLayers(estimateHiddenLayer(train));
		model.setThreads(m_thread);
		model.setDebug(true);
		
		
		model.buildClassifier(train);
		return model;
//		classifier.setClassifier(model);
//		classifier.setDebug(true);
//		classifier.buildClassifier(train);
//
//		return classifier;
	}
	
	protected String estimateHiddenLayer(Instances trainingData){
		return ClassifyUtility.estimateHiddenLayerNodes(trainingData, false);
	}
	
	//	将外部的并发线程根据算法内并发的计算强度折算出新的建议值
	public int recommendRunningThreads(int runningThreads){
		int recommendThreads=1; //缺省值
		if (runningThreads>1){ //如果外部调用者是多线程运行
			if (this.m_skipTrainInBacktest==false){ //如果要重新构建模型，那最多1个线程在外面
				recommendThreads=1;
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
