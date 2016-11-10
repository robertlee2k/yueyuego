package yueyueGo.fullModel.classifier;

import yueyueGo.EnvConstants;
import yueyueGo.ModelStore;
import yueyueGo.classifier.AdaboostClassifier;
import yueyueGo.fullModel.ArffFormatFullModel;
import yueyueGo.utility.ClassifyUtility;

public class AdaboostFullModel extends AdaboostClassifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3357697798648390329L;

	@Override
	protected void initializeParams() {
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = true;
		m_policySubGroup = new String[]{""};
		modelArffFormat=ArffFormatFullModel.FULLMODEL_FORMAT; //这个模型缺省是为FULLMODEL用的格式
		
		leafMinObjNum=1000; 	//j48树最小节点叶子数
		divided=EnvConstants.TRAINING_DATA_LIMIT/3000; //将trainingData分成多少份
		boost_iteration=10; 	//boost特有参数
		
		classifierName=ClassifyUtility.ADABOOST_FULLMODEL;
		
		m_modelEvalFileShareMode=ModelStore.YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		
		m_noCaculationAttrib=false; //使用计算字段

	}

	@Override
	//TODO 此类可以在子类中被覆盖（通过把yearsplit的值做处理，实现临时多年使用一个模型）
	public void locateModelStore(String yearSplit,String policySplit) {
		int inputYear=Integer.parseInt(yearSplit.substring(0, 4));
		String inputMonth=yearSplit.substring(4, 2);
		// 临时用2011模型替代2012、2013年的模型
		if (inputYear==2012 || inputYear==2013){
			yearSplit="2011"+inputMonth;
		}
		ModelStore modelStore=new ModelStore(yearSplit,policySplit,this.WORK_PATH+this.WORK_FILE_PREFIX, this.classifierName,this.m_modelEvalFileShareMode);
		m_modelStore=modelStore;
	}
}
