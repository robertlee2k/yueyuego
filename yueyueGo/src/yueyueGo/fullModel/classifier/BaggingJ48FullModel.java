package yueyueGo.fullModel.classifier;

import yueyueGo.EnvConstants;
import yueyueGo.ModelStore;
import yueyueGo.classifier.BaggingJ48;
import yueyueGo.fullModel.ArffFormatFullModel;
import yueyueGo.utility.AppContext;
//result changed because of reference data not matched=81892 while good change number=49134
//good ratio=60.00% average changed shouyilv=0.94% @ shouyilv thredhold= /3.00% /
//number of records for full market=4225771
//shouyilv average for full market=0.29%
//selected shouyilv average for full market =2.27% count=44204
//selected shouyilv average for hs300 =1.63% count=2753
//selected shouyilv average for zz500 =1.12% count=14042
public class BaggingJ48FullModel extends BaggingJ48 {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = -3884731573181625031L;
	@Override
	protected void initializeParams() {
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = true;
		m_policySubGroup = new String[]{""};
		modelArffFormat=ArffFormatFullModel.FULLMODEL_FORMAT; //这个模型缺省是为FULLMODEL用的格式

		leafMinObjNum=1000; 	//j48树最小节点叶子数
		divided=EnvConstants.TRAINING_DATA_LIMIT/3000; //将trainingData分成多少份
		bagging_iteration=10;	//bagging特有参数
		
		classifierName= "BaggingJ48ABFullModel";
		useMultiPCA=true; //bagging 内的每个模型自己有单独的PCA
		setWorkPathAndCheck(AppContext.getNOMINAL_CLASSIFIER_DIR()+this.getIdentifyName()+"\\");
		m_modelEvalFileShareMode=ModelStore.HALF_YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		
		
		m_noCaculationAttrib=false; //使用计算字段
		EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
		SAMPLE_LOWER_LIMIT =new double[] { 0.03}; // 各条均线选择样本的下限
		SAMPLE_UPPER_LIMIT =new double[] { 0.05}; // 各条均线选择样本的上限
		TP_FP_RATIO_LIMIT=new double[] { 1.8};//选择样本阀值时TP FP RATIO从何开始
		TP_FP_BOTTOM_LINE=0.8; //TP/FP的下限
		DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
	}

}
