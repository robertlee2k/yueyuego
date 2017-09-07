package yueyueGo.classifier.deprecated;

import yueyueGo.dataFormat.FullModelDataFormat;
import yueyueGo.utility.modelEvaluation.ModelStore;

public class MLPABFullModel extends MLPABClassifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1563573368577553379L;
	
	
	@Override
	protected void initializeParams() {

		m_policySubGroup = new String[]{"" };
		modelArffFormat=FullModelDataFormat.FULLMODEL_FORMAT; //这个模型缺省是为FULLMODEL用的格式
		
		classifierName= "mlpABFullModel";
		

		m_modelFileShareMode=ModelStore.YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		
		m_hiddenLayer="a"; //MLP的固有参数


	}
	

}
