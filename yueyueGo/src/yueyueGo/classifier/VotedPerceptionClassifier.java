package yueyueGo.classifier;

//适合保守类型下的全市场操作
import weka.classifiers.Classifier;
import yueyueGo.NominalClassifier;
import yueyueGo.utility.AppContext;

//1. 全市场20/30/50  14%-13%-11% 净值表现平稳
//参数：  eval 0.7 / 不单独评估阀值/ TP——FP RATIO {  1.6, 1.4, 1.3, 1.2, 1.1 }, UPPer{0.07, 0.09, 0.12, 0.15, 0.2 } TP_FP_BOTTOM_LINE=0.5
//lower_limit  { 0.01, 0.01,0.02, 0.02, 0.02 } DEFAULT_THRESHOLD=0.5
//===============================output summary=====================================
//Monthly selected_TPR mean: 17.54% standard deviation=28.09% Skewness=1.57 Kurtosis=1.45
//Monthly selected_LIFT mean : 0.58
//Monthly selected_positive summary: 4,037
//Monthly selected_count summary: 11,385
//Monthly selected_shouyilv average: 0.97% standard deviation=8.51% Skewness=6.44 Kurtosis=60.42
//Monthly total_shouyilv average: 1.00% standard deviation=6.18% Skewness=3.01 Kurtosis=15.09
//mixed selected positive rate: 35.46%
//Monthly summary_judge_result summary: good number= 285 bad number=220
//===============================end of summary=====================================

public class VotedPerceptionClassifier extends NominalClassifier {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6603382331120982691L;
	@Override
	protected void initializeParams() {
		classifierName="voted";
		setWorkPathAndCheck(AppContext.getNOMINAL_CLASSIFIER_DIR()+classifierName+"\\");
		
		m_policySubGroup = new String[]{"5","10","20","30","60" };
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = true;
		
		EVAL_RECENT_PORTION = 0.7; // 计算最近数据阀值从历史记录中选取多少比例的最近样本

		
		SAMPLE_LOWER_LIMIT =new double[] { 0.01, 0.01, 0.02, 0.02, 0.02 }; // 各条均线选择样本的下限
		SAMPLE_UPPER_LIMIT =new double[] {0.07, 0.09, 0.12, 0.15, 0.2 }; // 各条均线选择样本的上限
		TP_FP_RATIO_LIMIT=new double[] {  1.6, 1.4, 1.3, 1.2, 1.1 };//选择样本阀值时TP FP RATIO从何开始
		TP_FP_BOTTOM_LINE=0.5; //TP/FP的下限
		DEFAULT_THRESHOLD=0.5; // 找不出threshold时缺省值。

	}
	@Override
	public Classifier loadModel(String yearSplit, String policySplit) throws Exception{
		//这是单独准备的模型，模型文件和evaluation都是按年读取
		int inputYear=Integer.parseInt(yearSplit.substring(0,4));
		String filename=this.WORK_PATH+this.WORK_FILE_PREFIX +"-"+classifierName+ "-" + inputYear + MA_PREFIX + policySplit;
//		String evalFileName=this.WORK_PATH+this.WORK_FILE_PREFIX +"-"+this.classifierName+ "-" + inputYear + MA_PREFIX + policySplit+THRESHOLD_EXTENSION;
		
		this.setModelFileName(filename);
//		this.setEvaluationFilename(evalFileName);
	
		return loadModelFromFile();
	}
}
