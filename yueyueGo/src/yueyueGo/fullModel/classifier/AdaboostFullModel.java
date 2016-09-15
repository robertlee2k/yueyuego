package yueyueGo.fullModel.classifier;

import yueyueGo.EnvConstants;
import yueyueGo.ModelStore;
import yueyueGo.classifier.AdaboostClassifier;
import yueyueGo.fullModel.ArffFormatFullModel;
import yueyueGo.utility.AppContext;

public class AdaboostFullModel extends AdaboostClassifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3357697798648390329L;

	@Override
	protected void initializeParams() {
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = true;
		m_policySubGroup = new String[]{""};
		modelArffFormat=ArffFormatFullModel.FULLMODEL_FORMAT; //这个模型缺省是为FULLMODEL用的格式
		
		leafMinObjNum=1000; 	//j48树最小节点叶子数
		divided=EnvConstants.TRAINING_DATA_LIMIT/3000; //将trainingData分成多少份
		boost_iteration=10; 	//boost特有参数
		
		classifierName="adaboostFullModel";
		setWorkPathAndCheck(AppContext.getNOMINAL_CLASSIFIER_DIR()+this.getIdentifyName()+"\\");
		
		m_modelEvalFileShareMode=ModelStore.YEAR_SHARED_MODEL_AND_EVAL; //覆盖父类，设定模型和评估文件的共用模式
		
		m_noCaculationAttrib=false; //使用计算字段
		EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
		SAMPLE_LOWER_LIMIT =new double[] { 0.02}; // 各条均线选择样本的下限
		SAMPLE_UPPER_LIMIT =new double[] { 0.04}; // 各条均线选择样本的上限
		TP_FP_RATIO_LIMIT=new double[] { 1.8};//选择样本阀值时TP FP RATIO从何开始
		TP_FP_BOTTOM_LINE=0.9; //TP/FP的下限
		DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
	}

	@Override
	//此类可以在子类中被覆盖（通过把yearsplit的值做处理，实现临时多年使用一个模型）
	public void locateModelStore(String yearSplit,String policySplit) {
		int inputYear=Integer.parseInt(yearSplit.substring(0, 4));
		
		//TODO 临时用2010和2012模型处理
		if (inputYear==2011 ){
			yearSplit="2010";
		}else if(inputYear==2013){
			yearSplit="2012";
		}
		ModelStore modelStore=new ModelStore(yearSplit,policySplit,this);
		m_modelStore=modelStore;
	}
}
