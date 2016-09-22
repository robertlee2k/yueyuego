package yueyueGo.classifier;

import weka.classifiers.Classifier;
import weka.core.Instances;
import yueyueGo.EnvConstants;
import yueyueGo.ModelStore;
import yueyueGo.NominalClassifier;
import yueyueGo.ParrallelizedRunning;
import yueyueGo.utility.AppContext;
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
		
		classifierName="myNNAB";
		setWorkPathAndCheck(AppContext.getNOMINAL_CLASSIFIER_DIR()+classifierName+"\\");
		m_modelEvalFileShareMode=ModelStore.YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		
		
		m_thread=EnvConstants.CPU_CORE_NUMBER-1;
		m_learningRate=0.1; //缺省用
//		m_dropOutRate=0;
		
		m_noCaculationAttrib=true; //不使用计算字段
		EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
		SAMPLE_LOWER_LIMIT =new double[] { 0.03, 0.04, 0.05, 0.06, 0.07 }; // 各条均线选择样本的下限
		SAMPLE_UPPER_LIMIT =new double[] { 0.06, 0.07, 0.1, 0.11, 0.12 }; // 各条均线选择样本的上限
		TP_FP_RATIO_LIMIT=new double[] { 1.8, 1.7, 1.3, 1.1, 0.9};//选择样本阀值时TP FP RATIO从何开始
		TP_FP_BOTTOM_LINE=0.6; //TP/FP的下限
		DEFAULT_THRESHOLD=0.5; // 找不出threshold时缺省值。
	}
		
	@Override
	protected Classifier buildModel(Instances train) throws Exception {

//		MyAttributionSelectorWithPCA classifier = new MyAttributionSelectorWithPCA();

		m_cachedOldClassInstances=null; 
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
	
}
