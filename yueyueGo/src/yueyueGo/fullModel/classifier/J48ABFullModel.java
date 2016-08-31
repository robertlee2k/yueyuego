package yueyueGo.fullModel.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import yueyueGo.MyAttributionSelectorWithPCA;
import yueyueGo.NominalClassifier;

public class J48ABFullModel extends NominalClassifier {
	int leafMinObjNum=1000; 	//j48特有参数
	public J48ABFullModel() {

		classifierName = "J48ABFullModel";
		WORK_PATH =WORK_PATH+this.getIdentifyName()+"\\";

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
	
	
	@Override
	protected Classifier buildModel(Instances train) throws Exception {
		//设置基础的J48 classifier参数
		J48 model = new J48();
		int minNumObj=train.numInstances()/300;
		if (minNumObj<leafMinObjNum){
			minNumObj=leafMinObjNum; //防止树过大
		}
		String batchSize=Integer.toString(minNumObj);
		model.setBatchSize(batchSize);
		model.setMinNumObj(minNumObj);
		model.setNumDecimalPlaces(6);
		model.setDebug(true);
	
	    
		MyAttributionSelectorWithPCA classifier = new MyAttributionSelectorWithPCA();	    
	    classifier.setDebug(true);
	    classifier.setClassifier(model);
	    classifier.buildClassifier(train);
	    return classifier;
	}
}
