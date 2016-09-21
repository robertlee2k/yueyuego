package yueyueGo.fullModel.classifier;

import ext.WekaNeuralNetwork;
import weka.classifiers.Classifier;
import weka.core.Instances;
import yueyueGo.EnvConstants;
import yueyueGo.ModelStore;
import yueyueGo.classifier.MyNNClassifier;
import yueyueGo.fullModel.ArffFormatFullModel;
import yueyueGo.utility.AppContext;

public class MyNNFullModel extends MyNNClassifier {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3529781788896434664L;
	protected int m_thread; //NN的固有参数
	
	@Override
	protected void initializeParams() {

		m_skipTrainInBacktest = false;
		m_skipEvalInBacktest = false;
		m_policySubGroup = new String[]{"" };
		modelArffFormat=ArffFormatFullModel.FULLMODEL_FORMAT; //这个模型缺省是为FULLMODEL用的格式
		
		classifierName="myNNFullModel";
		setWorkPathAndCheck(AppContext.getNOMINAL_CLASSIFIER_DIR()+classifierName+"\\");
		m_modelEvalFileShareMode=ModelStore.YEAR_SHARED_MODEL_AND_EVAL; //覆盖父类，设定模型和评估文件的共用模式
		
		m_thread=EnvConstants.CPU_CORE_NUMBER-5;
		m_learningRate=0.2; //缺省用
		
		m_noCaculationAttrib=true; //不使用计算字段，注意这里尝试短格式了。
		EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
		SAMPLE_LOWER_LIMIT =new double[] { 0.02}; // 各条均线选择样本的下限
		SAMPLE_UPPER_LIMIT =new double[] { 0.04}; // 各条均线选择样本的上限
		TP_FP_RATIO_LIMIT=new double[] { 1.8};//选择样本阀值时TP FP RATIO从何开始
		TP_FP_BOTTOM_LINE=0.9; //TP/FP的下限
		DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
	}
		
	@Override
	protected Classifier buildModel(Instances train) throws Exception {

//		MyAttributionSelectorWithPCA classifier = new MyAttributionSelectorWithPCA();

		m_cachedOldClassInstances=null; 
		WekaNeuralNetwork model=new WekaNeuralNetwork();
		model.setNumDecimalPlaces(6);
		int minNumObj=train.numInstances()/1000;
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
}
