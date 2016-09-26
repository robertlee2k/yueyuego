package yueyueGo.classifier.deprecated;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import yueyueGo.ModelStore;
import yueyueGo.MyAttributionSelectorWithPCA;
import yueyueGo.NominalClassifier;
import yueyueGo.fullModel.ArffFormatFullModel;
import yueyueGo.utility.ClassifyUtility;
@Deprecated
public class J48ABFullModel extends NominalClassifier {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = -3650296432932510448L;
	protected int leafMinObjNum; 	//j48树最小节点叶子数
	protected int divided; //将trainingData分成多少份
	
	@Override
	protected void initializeParams() {
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = true;
		m_policySubGroup = new String[]{"" };
		modelArffFormat=ArffFormatFullModel.FULLMODEL_FORMAT; //这个模型缺省是为FULLMODEL用的格式
		
		classifierName= "J48ABFullModel";
//		setWorkPathAndCheck(AppContext.getNOMINAL_CLASSIFIER_DIR()+this.getIdentifyName()+"\\");
		m_modelEvalFileShareMode=ModelStore.YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式

		leafMinObjNum=1000; 	//j48树最小节点叶子数
		divided=800; //将trainingData分成多少份

		m_noCaculationAttrib=false; //使用计算字段

//		EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
//		SAMPLE_LOWER_LIMIT =new double[] { 0.04 }; // 各条均线选择样本的下限
//		SAMPLE_UPPER_LIMIT =new double[] { 0.07 }; // 各条均线选择样本的上限
//		TP_FP_RATIO_LIMIT=new double[] { 1.8};//选择样本阀值时TP FP RATIO从何开始
//		TP_FP_BOTTOM_LINE=0.8; //TP/FP的下限
//		DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
	}
	
	
	@Override
	protected Classifier buildModel(Instances train) throws Exception {
		m_cachedOldClassInstances=null; 
		//设置基础的J48 classifier参数
		J48 model=ClassifyUtility.prepareJ48(train.numInstances(),leafMinObjNum,divided);
	
	    
		MyAttributionSelectorWithPCA classifier = new MyAttributionSelectorWithPCA();	    
	    classifier.setDebug(true);
	    classifier.setClassifier(model);
	    classifier.buildClassifier(train);
	    return classifier;
	}

}
