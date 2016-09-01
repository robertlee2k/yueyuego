package yueyueGo.fullModel.classifier;

import yueyueGo.RuntimeParams;
import yueyueGo.classifier.BaggingJ48;

public class BaggingJ48FullModel extends BaggingJ48 {

	public BaggingJ48FullModel() {
		super();
		leafMinObjNum=1000; 	//j48树最小节点叶子数
		divided=800; //将trainingData分成多少份
		useMultiPCA=false; //bagging 内的每个模型自己没有单独的PCA
		bagging_iteration=10;	//bagging特有参数
		
		classifierName = "BaggingJ48ABFullModel";
		WORK_PATH =RuntimeParams.getNOMINAL_CLASSIFIER_DIR()+this.getIdentifyName()+"\\";

		m_skipTrainInBacktest = false;
		m_skipEvalInBacktest = false;
		m_policySubGroup = new String[]{"" };
		
		m_noCaculationAttrib=false; //使用计算字段

		EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
		SAMPLE_LOWER_LIMIT =new double[] { 0.04, 0.05, 0.06, 0.07, 0.08 }; // 各条均线选择样本的下限
		SAMPLE_UPPER_LIMIT =new double[] { 0.07, 0.08, 0.11, 0.12, 0.13 }; // 各条均线选择样本的上限
		TP_FP_RATIO_LIMIT=new double[] { 1.8, 1.7, 1.3, 1.1, 0.9};//选择样本阀值时TP FP RATIO从何开始
		TP_FP_BOTTOM_LINE=0.8; //TP/FP的下限
		DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
	}

}
