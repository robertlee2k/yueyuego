package yueyueGo.fullModel.classifier;

import yueyueGo.ModelStore;
import yueyueGo.classifier.M5PABClassifier;
import yueyueGo.utility.AppContext;

@Deprecated
public class M5PABFullModel extends M5PABClassifier {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 774038761035568952L;

	@Override
	protected void initializeParams() {
	
		classifierName="m5pABFullModel";
		setWorkPathAndCheck(AppContext.getCONTINOUS_CLASSIFIER_DIR()+getIdentifyName()+"\\");
		m_modelEvalFileShareMode=ModelStore.YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = true;
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
	
}
