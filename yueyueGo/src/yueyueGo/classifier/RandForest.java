package yueyueGo.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import yueyueGo.ModelStore;
import yueyueGo.NominalClassifier;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaInstances;
import yueyueGo.utility.ClassifyUtility;

public class RandForest extends NominalClassifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7135604627890964460L;

	protected int leafMinObjNum;
	

	@Override
	protected void initializeParams() {
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = false;
		m_policySubGroup = new String[]{"5","10","20","30","60" };

		classifierName=ClassifyUtility.RANDOM_FOREST;	

		m_modelFileShareMode=ModelStore.QUARTER_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		
		m_noCaculationAttrib=true;//不使用计算字段 (20161215试过无计算字段，效果不如有计算字段好） 
		m_removeSWData=true; //20161222尝试不用申万行业数据
		m_evalDataSplitMode=ModelStore.USE_NINE_MONTHS_DATA_FOR_EVAL;//USE_YEAR_DATA_FOR_EVAL; //评估区间使用一年数据 （截止20170103，这个是效果最好的）
	}


	@Override
	protected Classifier buildModel(GeneralInstances trainData) throws Exception {
		RandomForest rForest=new RandomForest();
		rForest.setSeed(888);
		rForest.setNumTrees(1000);
		rForest.setNumExecutionSlots(10);
		int features=trainData.numAttributes();
		rForest.setNumFeatures(features/8);
		rForest.setNumDecimalPlaces(6);
		rForest.setDebug(true);
		rForest.buildClassifier(WekaInstances.convertToWekaInstances(trainData));
		System.out.println("out of bagger error in RF="+rForest.measureOutOfBagError());
		return rForest;
		
	}

}
