package yueyueGo.fullModel.classifier;

import yueyueGo.EnvConstants;
import yueyueGo.ModelStore;
import yueyueGo.classifier.deprecated.BaggingJ48;
import yueyueGo.dataFormat.FullModelDataFormat;
import yueyueGo.utility.ClassifyUtility;
//result changed because of reference data not matched=81892 while good change number=49134
//good ratio=60.00% average changed shouyilv=0.94% @ shouyilv thredhold= /3.00% /
//number of records for full market=4225771
//shouyilv average for full market=0.29%
//selected shouyilv average for full market =2.27% count=44204
//selected shouyilv average for hs300 =1.63% count=2753
//selected shouyilv average for zz500 =1.12% count=14042
@Deprecated
public class BaggingJ48FullModel extends BaggingJ48 {
 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3884731573181625031L;
	@Override
	protected void initializeParams() {

		m_policySubGroup = new String[]{""};
		modelArffFormat=FullModelDataFormat.FULLMODEL_FORMAT; //这个模型缺省是为FULLMODEL用的格式

		leafMinObjNum=1000; 	//j48树最小节点叶子数
		divided=EnvConstants.TRAINING_DATA_LIMIT/3000; //将trainingData分成多少份
		bagging_iteration=10;	//bagging特有参数
		
		classifierName= ClassifyUtility.BAGGING_J48_FULLMODEL;
		useMultiPCA=true; //bagging 内的每个模型自己有单独的PCA
		m_modelFileShareMode=ModelStore.HALF_YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		
		

	}

}
