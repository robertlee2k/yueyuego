package yueyueGo.fullModel.classifier;

import ext.WekaNeuralNetwork;
import weka.classifiers.Classifier;
import yueyueGo.EnvConstants;
import yueyueGo.EvaluationStore;
import yueyueGo.ModelStore;
import yueyueGo.classifier.MyNNClassifier;
import yueyueGo.dataFormat.FullModelDataFormat;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaInstances;
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


//***************************************CLASSIFY DATE=2017-01-19 
//ClassifyIdentity=myNNFullModel
//m_skipTrainInBacktest=true
//m_skipEvalInBacktest=false
//m_noCaculationAttrib=true
//m_removeSWData=true
//m_positiveLine=0.0
//m_modelDataSplitMode=12
//m_modelEvalFileShareMode=12
//modelArffFormat=3
//SAMPLE_LOWER_LIMIT={0.02,}
//SAMPLE_UPPER_LIMIT={0.04,}
//LIFT_UP_TARGET=1.8
//***************************************
//......................
//===============================output summary===================================== for : myNNFullModel
//Monthly selected_TPR mean: 47.60% standard deviation=35.20% Skewness=-0.14 Kurtosis=-1.42
//Monthly selected_LIFT mean : 0.98
//Monthly selected_positive summary: 65,259
//Monthly selected_count summary: 119,563
//Monthly selected_shouyilv average: 1.15% standard deviation=7.45% Skewness=1.65 Kurtosis=14.27
//Monthly total_shouyilv average: 0.18% standard deviation=2.84% Skewness=-0.53 Kurtosis=1.63
//mixed selected positive rate: 54.58%
//Monthly summary_judge_result summary: good number= 65 bad number=42
//===============================end of summary=====================================for : myNNFullModel
//-----now output nominal predictions----------myNNFullModel
//incoming resultData size, row=4395123 column=5
//incoming referenceData size, row=4395123 column=5
//Left data loaded, row=5335793 column=11
//number of results merged and processed: 4395123
//###### Finally selected count=56295  ######
// result changed because of reference data not matched=63268 while good change number=36708
// good ratio=58.02% average changed shouyilv=0.87%
// @ SHOUYILV_FILTER_FOR_WINRATE={2.00%, }
//number of records for full market=4395123
//shouyilv average for full market=0.1883%
//selected shouyilv average for full market =-0.1775% count=56295

public class MyNNFullModel extends MyNNClassifier {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3529781788896434664L;
	protected int m_thread; //NN的固有参数
	
	@Override
	protected void initializeParams() {


		m_policySubGroup = new String[]{"" };
		modelArffFormat=FullModelDataFormat.FULLMODEL_FORMAT; //这个模型缺省是为FULLMODEL用的格式
		
		classifierName=ClassifyUtility.MYNN_MLP_FULLMODEL;
		m_modelFileShareMode=ModelStore.YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		m_evalDataSplitMode=EvaluationStore.USE_YEAR_DATA_FOR_EVAL;//覆盖父类，设定模型和评估间隔为12个月
		
		m_thread=EnvConstants.CPU_CORE_NUMBER/2;
		m_learningRate=0.03; //缺省用
		
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
