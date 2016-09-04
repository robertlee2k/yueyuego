package yueyueGo.fullModel.classifier;

import weka.classifiers.Classifier;
import yueyueGo.EnvConstants;
import yueyueGo.classifier.BaggingJ48;
import yueyueGo.utility.RuntimeParams;

public class BaggingJ48FullModel extends BaggingJ48 {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = -3884731573181625031L;
	@Override
	protected void initializeParams() {
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = false;
		m_policySubGroup = new String[]{""};

		leafMinObjNum=1000; 	//j48树最小节点叶子数
		divided=EnvConstants.TRAINING_DATA_LIMIT/3000; //将trainingData分成多少份
		bagging_iteration=10;	//bagging特有参数
		
		classifierName= "BaggingJ48ABFullModel";
		useMultiPCA=true; //bagging 内的每个模型自己有单独的PCA
		setWorkPathAndCheck(RuntimeParams.getNOMINAL_CLASSIFIER_DIR()+this.getIdentifyName()+"\\");
		
		m_noCaculationAttrib=false; //使用计算字段
		EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
		SAMPLE_LOWER_LIMIT =new double[] { 0.03}; // 各条均线选择样本的下限
		SAMPLE_UPPER_LIMIT =new double[] { 0.05}; // 各条均线选择样本的上限
		TP_FP_RATIO_LIMIT=new double[] { 1.8};//选择样本阀值时TP FP RATIO从何开始
		TP_FP_BOTTOM_LINE=0.8; //TP/FP的下限
		DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
	}
	@Override
	public Classifier loadModel(String yearSplit, String policySplit) throws Exception{
		//这是单独准备的模型，模型文件是按年读取，但evaluation文件不变仍按月
		int inputYear=Integer.parseInt(yearSplit.substring(0,4));
		
		//为特定年份下半年增加一个模型，提高准确度
		String halfYearString="";
//		if(yearSplit.length()==6){
//			int inputMonth=Integer.parseInt(yearSplit.substring(4,6));
//			//TODO 
//			if ((inputYear==2016) && inputMonth>=6){
//				halfYearString="06";
//			}
//		}
		String filename=this.WORK_PATH+this.WORK_FILE_PREFIX +"-"+classifierName+ "-" + inputYear +halfYearString+ MA_PREFIX + policySplit;//如果使用固定模型
		
		this.setModelFileName(filename);
		//全年使用一个thresHold
//		this.setEvaluationFilename(filename+THRESHOLD_EXTENSION);

	
		return loadModelFromFile();
	}	
}
