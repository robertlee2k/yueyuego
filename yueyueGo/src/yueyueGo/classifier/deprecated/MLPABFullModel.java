package yueyueGo.classifier.deprecated;

import yueyueGo.ModelStore;
import yueyueGo.dataFormat.FullModelDataFormat;

public class MLPABFullModel extends MLPABClassifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1563573368577553379L;
	
	
	@Override
	protected void initializeParams() {
		m_skipTrainInBacktest = false;
		m_skipEvalInBacktest = false;
		m_policySubGroup = new String[]{"" };
		modelArffFormat=FullModelDataFormat.FULLMODEL_FORMAT; //这个模型缺省是为FULLMODEL用的格式
		
		classifierName= "mlpABFullModel";
		

		m_modelFileShareMode=ModelStore.YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		
		m_hiddenLayer="a"; //MLP的固有参数

		m_noCaculationAttrib=true; //不使用计算字段，注意这里尝试短格式了。
	}
	

}
