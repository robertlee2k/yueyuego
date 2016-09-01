package yueyueGo.fullModel.classifier;

import yueyueGo.RuntimeParams;
import yueyueGo.classifier.BaggingM5P;

public class BaggingM5PFullModel extends BaggingM5P {

	public BaggingM5PFullModel() {
		super();
		
		leafMinObjNum=1000;
		divided=800;	
		adjustThresholdBottom=false; //不用MeanABSError调整threshold
		useMultiPCA=false; //bagging 内的每个模型自己没有单独的PCA
		bagging_iteration=10;	//bagging特有参数
		classifierName = "BaggingM5PABFullModel";
		WORK_PATH =RuntimeParams.getCONTINOUS_CLASSIFIER_DIR()+getIdentifyName()+"\\";

		m_skipTrainInBacktest = false;
		m_skipEvalInBacktest = false;
		m_policySubGroup = new String[]{""};
		
	
		m_noCaculationAttrib=false; //添加计算字段
		EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
		SAMPLE_LOWER_LIMIT = new double[]{ 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限 
		SAMPLE_UPPER_LIMIT = new double[]  { 0.06, 0.07, 0.1, 0.11, 0.12 };
		TP_FP_RATIO_LIMIT = new double[] { 1.8, 1.7, 1.5, 1.2, 1}; //选择样本阀值时TP FP RATIO到了何种值就可以提前停止了。
		TP_FP_BOTTOM_LINE=0.9; //TP/FP的下限
	}

}
