package yueyueGo.classifier;

import java.util.Vector;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import yueyueGo.NominalClassifier;
import yueyueGo.utility.AppContext;
import yueyueGo.utility.ThresholdData;

//效果不好
@Deprecated  
public class RandomForestClassifier extends NominalClassifier	 {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4354687069309611335L;

	@Override
	protected void initializeParams() {
		classifierName="randomForest";
		setWorkPathAndCheck(AppContext.getNOMINAL_CLASSIFIER_DIR()+classifierName+"\\");
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = false;
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		m_noCaculationAttrib=false; //添加计算字段
		EVAL_RECENT_PORTION = 0.9; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
		SAMPLE_LOWER_LIMIT = new double[]{ 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限 
		SAMPLE_UPPER_LIMIT = new double[]  { 0.06, 0.07, 0.1, 0.11, 0.12 };
		TP_FP_RATIO_LIMIT = new double[] { 1.8, 1.7, 1.5, 1.2, 1}; //选择样本阀值时TP FP RATIO到了何种值就可以提前停止了。
		TP_FP_BOTTOM_LINE=0.9; //TP/FP的下限
	}
	
	
	@Override
	protected Classifier buildModel(Instances train) throws Exception {

		RandomForest model = new RandomForest();
		int leafMinObjNum=300;
		
		int minNumObj=train.numInstances()/300;
		if (minNumObj<leafMinObjNum){
			minNumObj=leafMinObjNum; 
		}

		String batchSize=Integer.toString(minNumObj);
		model.setBatchSize(batchSize);
		model.setNumDecimalPlaces(6);

		model.setNumExecutionSlots(4);
		double numFeatures=Math.sqrt(new Integer(train.numAttributes()).doubleValue());
		model.setNumFeatures(new Double(numFeatures).intValue());
		model.setNumTrees(100);

		model.buildClassifier(train);
		System.out.println("finish buiding"+classifierName+" model.");

		return model;
	}
	
	@Override
	public Classifier loadModel(String yearSplit, String policySplit) throws Exception{
		//这是单独准备的模型，模型文件是按年读取，但evaluation文件不变仍按月
		int inputYear=Integer.parseInt(yearSplit.substring(0,4));

		String filename=this.WORK_PATH+this.WORK_FILE_PREFIX +"-"+classifierName+ "-" + inputYear + MA_PREFIX + policySplit;//如果使用固定模型
		
		this.setModelFileName(filename);

	
		return loadModelFromFile();
	}	
	
	//对模型进行评估
	@Override
	public Vector<Double> evaluateModel(Instances train, Classifier model,
			double sample_limit, double sample_upper, double tp_fp_ratio)
			throws Exception {
		
		cachedOldClassInstances=null; 
		RandomForest rModel=(RandomForest)model;
		double outofBagError=rModel.measureOutOfBagError();
		System.out.println(" -----------random Forest evaluation for FULL Market.... , out of bag error="+outofBagError);
		Vector<Double> v = new Vector<Double>();
		v.add(new Double(0.5+outofBagError));
		v.add(new Double(999));
		System.out.println(" *********** end evaluating for FULL Market....");		

		ThresholdData.saveEvaluationToFile(this.getEvaluationFilename(),v);
		return v;
		
	}
}
