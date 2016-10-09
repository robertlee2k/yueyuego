package yueyueGo.fullModel.classifier;

import yueyueGo.EnvConstants;
import yueyueGo.ModelStore;
import yueyueGo.classifier.BaggingM5P;
import yueyueGo.fullModel.ArffFormatFullModel;
import yueyueGo.utility.ClassifyUtility;

//result changed because of reference data not matched=15096 while good change number=5665
//good ratio=37.53% average changed shouyilv=4.58% @ winrate thredhold= /50.00% /
//number of records for full market=4225771
//shouyilv average for full market=0.29%
//selected shouyilv average for full market =1.58% count=145281
//selected shouyilv average for hs300 =0.98% count=12875
//selected shouyilv average for zz500 =1.10% count=32436




public class BaggingM5PFullModel extends BaggingM5P {
	 
	/**
	 * 
	 */
	private static final long serialVersionUID = 8505755558382340493L;
	@Override
	protected void initializeParams() {
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = false;
		m_policySubGroup = new String[]{""};
		modelArffFormat=ArffFormatFullModel.FULLMODEL_FORMAT; //这个模型缺省是为FULLMODEL用的格式
		
		leafMinObjNum=1000;
		divided=EnvConstants.TRAINING_DATA_LIMIT/3000;	
		bagging_iteration=10;	//bagging特有参数
		
		classifierName= ClassifyUtility.BAGGING_M5P_FULLMODEL;
		useMultiPCA=true; //bagging 内的每个模型自己有单独的PCA
//		setWorkPathAndCheck(AppContext.getCONTINOUS_CLASSIFIER_DIR()+getIdentifyName()+"\\");
		m_modelEvalFileShareMode=ModelStore.HALF_YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		
		m_noCaculationAttrib=false; //添加计算字段

	}
}
