package yueyueGo.fullModel.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import yueyueGo.ClassifyUtility;
import yueyueGo.MyAttributionSelectorWithPCA;
import yueyueGo.NominalClassifier;
import yueyueGo.RuntimeParams;
@Deprecated
public class J48ABFullModel extends NominalClassifier {
 
	protected int leafMinObjNum; 	//j48树最小节点叶子数
	protected int divided; //将trainingData分成多少份
	
	@Override
	protected void initializeParams() {
		m_skipTrainInBacktest = false;
		m_skipEvalInBacktest = false;
		m_policySubGroup = new String[]{"" };
		
		classifierName= "J48ABFullModel";
		setWorkPathAndCheck(RuntimeParams.getNOMINAL_CLASSIFIER_DIR()+this.getIdentifyName()+"\\");


		leafMinObjNum=1000; 	//j48树最小节点叶子数
		divided=800; //将trainingData分成多少份

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
		J48 model=ClassifyUtility.prepareJ48(train.numInstances(),leafMinObjNum,divided);
	
	    
		MyAttributionSelectorWithPCA classifier = new MyAttributionSelectorWithPCA();	    
	    classifier.setDebug(true);
	    classifier.setClassifier(model);
	    classifier.buildClassifier(train);
	    return classifier;
	}
	
	@Override
	public Classifier loadModel(String yearSplit, String policySplit) throws Exception{

		int inputYear=Integer.parseInt(yearSplit.substring(0,4));
//		//这是为Fullmodel单独准备的模型，模型文件是按年分阶段读取
//		if (inputYear>2015){
//			inputYear=2015;
//		}else if (inputYear>2009){
//			inputYear=2009;
//		}
		String filename=this.WORK_PATH+this.WORK_FILE_PREFIX +"-"+this.classifierName+ "-" + inputYear + MA_PREFIX + policySplit;//如果使用固定模型
		
		this.setModelFileName(filename);

	
		return loadModelFromFile();
	}	
}
