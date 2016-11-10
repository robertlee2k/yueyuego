package yueyueGo.fullModel.classifier;

import ext.WekaNeuralNetwork;
import weka.classifiers.Classifier;
import yueyueGo.EnvConstants;
import yueyueGo.ModelStore;
import yueyueGo.classifier.MyNNClassifier;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaInstances;
import yueyueGo.fullModel.ArffFormatFullModel;
import yueyueGo.utility.ClassifyUtility;

//===============================output summary===================================== for : myNNFullModel
//Monthly selected_TPR mean: 48.60% standard deviation=34.52% Skewness=-0.05 Kurtosis=-1.36
//Monthly selected_LIFT mean : 0.96
//Monthly selected_positive summary: 76,570
//Monthly selected_count summary: 150,888
//Monthly selected_shouyilv average: 1.26% standard deviation=7.22% Skewness=1.77 Kurtosis=10.92
//Monthly total_shouyilv average: 0.31% standard deviation=2.86% Skewness=-0.5 Kurtosis=1.45
//mixed selected positive rate: 50.75%
//Monthly summary_judge_result summary: good number= 57 bad number=47
//===============================end of summary=====================================for : myNNFullModel

public class MyNNFullModel extends MyNNClassifier {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3529781788896434664L;
	protected int m_thread; //NN的固有参数
	
	@Override
	protected void initializeParams() {

		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = true;
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
	protected Classifier buildModel(GeneralInstances train) throws Exception {

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
		
		
		model.buildClassifier(WekaInstances.convertToWekaInstances(train));
		return model;
//		classifier.setClassifier(model);
//		classifier.setDebug(true);
//		classifier.buildClassifier(train);
//
//		return classifier;
	}
}
