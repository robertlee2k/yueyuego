package yueyueGo.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import yueyueGo.ClassifyUtility;
import yueyueGo.NominalClassifier;
import yueyueGo.RuntimeParams;

public class BaggingJ48 extends NominalClassifier {
	public static final String classifierName="baggingJ48";
	protected boolean useMultiPCA;
	protected int bagging_iteration;
	protected int leafMinObjNum;
	protected int divided;
	
	@Override
	protected void initializeParams() {
		m_skipTrainInBacktest = false;
		m_skipEvalInBacktest = false;
		m_policySubGroup = new String[]{"5","10","20","30","60" };


		useMultiPCA=true; //bagging 内的每个模型自己有单独的PCA
		setWorkPathAndCheck(RuntimeParams.getNOMINAL_CLASSIFIER_DIR()+this.getIdentifyName()+"\\");
		
		m_noCaculationAttrib=false; //使用计算字段
		bagging_iteration=10;	//bagging特有参数
		leafMinObjNum=300; 	//j48树最小节点叶子数
		divided=300; //将trainingData分成多少份
		
		EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本		
		SAMPLE_LOWER_LIMIT =new double[] { 0.04, 0.05, 0.06, 0.07, 0.08 }; // 各条均线选择样本的下限
		SAMPLE_UPPER_LIMIT =new double[] { 0.07, 0.08, 0.11, 0.12, 0.13 }; // 各条均线选择样本的上限
		TP_FP_RATIO_LIMIT=new double[] { 1.8, 1.7, 1.3, 1.1, 0.9};//选择样本阀值时TP FP RATIO从何开始
		TP_FP_BOTTOM_LINE=0.8; //TP/FP的下限
		DEFAULT_THRESHOLD=0.6; // 找不出threshold时缺省值。
	}

	
	@Override
	protected Classifier buildModel(Instances train) throws Exception {
		//设置基础的m5p classifier参数
		J48 model=ClassifyUtility.prepareJ48(train.numInstances(),leafMinObjNum,divided);

		if (useMultiPCA==true){
			int bagging_samplePercent=70;//bagging sample 取样率
			return ClassifyUtility.buildBaggingWithMultiPCA(train,model,bagging_iteration,bagging_samplePercent);
		}else{
			int bagging_samplePercent=100;// PrePCA算袋外误差时要求percent都为100
			return ClassifyUtility.buildBaggingWithSinglePCA(train,model,bagging_iteration,bagging_samplePercent);
		}
	}

	
	@Override
	public Classifier loadModel(String yearSplit, String policySplit) throws Exception{
		//这是单独准备的模型，模型文件是按年读取，但evaluation文件不变仍按月
		int inputYear=Integer.parseInt(yearSplit.substring(0,4));
		
		//为特定年份下半年增加一个模型，提高准确度
		String halfYearString="";
		if(yearSplit.length()==6){
			int inputMonth=Integer.parseInt(yearSplit.substring(4,6));
			//TODO 
			if ((inputYear==2016) && inputMonth>=6){
				halfYearString="06";
			}
		}
		String filename=this.WORK_PATH+this.WORK_FILE_PREFIX +"-"+classifierName+ "-" + inputYear +halfYearString+ MA_PREFIX + policySplit;//如果使用固定模型
		
		this.setModelFileName(filename);

	
		return loadModelFromFile();
	}	
	
	
	@Override
	public String getIdentifyName(){
		String idenString;
		if (useMultiPCA==true){
			idenString =classifierName+"-multiPCA";
		}else{
			idenString =classifierName+"-singlePCA";
		}

		return idenString;
	}
}
