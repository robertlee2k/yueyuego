package yueyueGo.fullModel.classifier;

import weka.classifiers.Classifier;
import weka.core.Instances;
import yueyueGo.EnvConstants;
import yueyueGo.ModelStore;
import yueyueGo.classifier.MyNNClassifier;
import yueyueGo.fullModel.ArffFormatFullModel;
import yueyueGo.utility.ClassifyUtility;
import ext.WekaNeuralNetwork;

public class MyNNFullModel extends MyNNClassifier {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3529781788896434664L;
	protected int m_thread; //NN的固有参数
	
	@Override
	protected void initializeParams() {

		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = false;
		m_policySubGroup = new String[]{"" };
		modelArffFormat=ArffFormatFullModel.FULLMODEL_FORMAT; //这个模型缺省是为FULLMODEL用的格式
		
		classifierName=ClassifyUtility.MYNN_MLP_FULLMODEL;
//		setWorkPathAndCheck(AppContext.getNOMINAL_CLASSIFIER_DIR()+classifierName+"\\");
		m_modelEvalFileShareMode=ModelStore.HALF_YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		
		m_thread=EnvConstants.CPU_CORE_NUMBER-8;
		m_learningRate=0.3; //缺省用
		
		m_noCaculationAttrib=true; //不使用计算字段，注意这里尝试短格式了。

	}
		
	@Override
	protected Classifier buildModel(Instances train) throws Exception {

//		MyAttributionSelectorWithPCA classifier = new MyAttributionSelectorWithPCA();

		 
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
