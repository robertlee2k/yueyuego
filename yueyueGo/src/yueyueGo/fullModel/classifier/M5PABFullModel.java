package yueyueGo.fullModel.classifier;

import weka.classifiers.Classifier;
import yueyueGo.RuntimeParams;
import yueyueGo.classifier.M5PABClassifier;

@Deprecated
public class M5PABFullModel extends M5PABClassifier {

	@Override
	protected void initializeParams() {
	
		classifierName = "m5pABFullModel";
		setWorkPathAndCheck(RuntimeParams.getCONTINOUS_CLASSIFIER_DIR()+getIdentifyName()+"\\");

		m_skipTrainInBacktest = false;
		m_skipEvalInBacktest = false;
		m_policySubGroup = new String[]{""};
		
		leafMinObjNum=1000;
		divided=800;		
		m_noCaculationAttrib=false; //添加计算字段
		EVAL_RECENT_PORTION = 1; // 计算最近数据阀值从历史记录中选取多少比例的最近样本
		SAMPLE_LOWER_LIMIT = new double[]{ 0.03, 0.03, 0.03, 0.03, 0.03 }; // 各条均线选择样本的下限 
		SAMPLE_UPPER_LIMIT = new double[]  { 0.06, 0.07, 0.1, 0.11, 0.12 };
		TP_FP_RATIO_LIMIT = new double[] { 1.8, 1.7, 1.5, 1.2, 1}; //选择样本阀值时TP FP RATIO到了何种值就可以提前停止了。
		TP_FP_BOTTOM_LINE=0.9; //TP/FP的下限
	}
	
	@Override
	public Classifier loadModel(String yearSplit, String policySplit) throws Exception{

		int inputYear=Integer.parseInt(yearSplit.substring(0,4));
//		//这是为Fullmodel单独准备的模型，模型文件是按年分阶段读取
//		if (inputYear>2014){
//			inputYear=2014;
//		}else if (inputYear>2009){
//			inputYear=2009;
//		}
		String filename=this.WORK_PATH+this.WORK_FILE_PREFIX +"-"+this.classifierName+ "-" + inputYear + MA_PREFIX + policySplit;//如果使用固定模型
		
		this.setModelFileName(filename);

	
		return loadModelFromFile();
	}	
}
