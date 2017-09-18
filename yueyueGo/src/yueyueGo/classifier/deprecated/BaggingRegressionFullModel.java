package yueyueGo.classifier.deprecated;

import yueyueGo.EnvConstants;
import yueyueGo.dataFormat.FullModelDataFormat;
import yueyueGo.utility.ClassifyUtility;
import yueyueGo.utility.modelEvaluation.ModelStore;
@Deprecated
public class BaggingRegressionFullModel extends BaggingLinearRegression {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4153988848299621988L;
	@Override
	protected void overrideParams() {

		m_policySubGroup = new String[]{""};
		modelArffFormat=FullModelDataFormat.FULLMODEL_FORMAT; //这个模型缺省是为FULLMODEL用的格式
		
		classifierName=ClassifyUtility.BAGGING_REGRESSION_FULLMODEL;	
		useMultiPCA=true; //bagging 内的每个模型自己有单独的PCA
		m_modelFileShareMode=ModelStore.YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		bagging_iteration=10;	//bagging特有参数
		divided=EnvConstants.TRAINING_DATA_LIMIT/3000; //将trainingData分成多少份
		

	}
}
