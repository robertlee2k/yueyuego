package yueyueGo.classifier.deprecated;

import yueyueGo.ModelStore;
import yueyueGo.classifier.MLPABClassifier;
import yueyueGo.fullModel.ArffFormatFullModel;

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
		modelArffFormat=ArffFormatFullModel.FULLMODEL_FORMAT; //这个模型缺省是为FULLMODEL用的格式
		
		classifierName= "mlpABFullModel";
		
//		setWorkPathAndCheck(AppContext.getNOMINAL_CLASSIFIER_DIR()+this.getIdentifyName()+"\\");
		m_modelEvalFileShareMode=ModelStore.YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		
		m_hiddenLayer="a"; //MLP的固有参数

		m_noCaculationAttrib=true; //不使用计算字段，注意这里尝试短格式了。
//		EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
//		SAMPLE_LOWER_LIMIT =new double[] { 0.02}; // 各条均线选择样本的下限
//		SAMPLE_UPPER_LIMIT =new double[] { 0.04}; // 各条均线选择样本的上限
//		TP_FP_RATIO_LIMIT=new double[] { 1.8};//选择样本阀值时TP FP RATIO从何开始
//		TP_FP_BOTTOM_LINE=0.9; //TP/FP的下限
//		DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
	}
	
	@Override
	//覆盖父类，恢复baseClassifier的调用
	public void locateModelStore(String yearSplit,String policySplit) {

		int modelShareMode=this.m_modelEvalFileShareMode;
		
//		//TODO 临时增加一个8月的模型
//		if ("201608".equals(yearSplit)){
//			modelShareMode=ModelStore.SEPERATE_MODEL_AND_EVAL;
//		}
		ModelStore modelStore=new ModelStore(yearSplit,policySplit,this.WORK_PATH+this.WORK_FILE_PREFIX, this.classifierName,modelShareMode);
		m_modelStore=modelStore;
	}
}
