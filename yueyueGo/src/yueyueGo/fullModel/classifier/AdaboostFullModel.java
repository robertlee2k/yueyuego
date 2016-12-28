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
		m_usePCA=true; //覆盖父类，使用PCA
		m_removeSWData=false; //覆盖父类，用申万行业数据
	}

	@Override
	//TODO 此类可以在子类中被覆盖（通过把yearsplit的值做处理，实现临时多年使用一个模型）
	public void locateModelStore(String aYearSplit,String policySplit) {
		//这里build model的数据已变为当前周期前推一段时间的数据，
		//比如若按年取评估数据，如果是2010XX.mdl 则取2009年XX月之前的数据build， 剩下的一年数据做评估用
		String modelYearSplit=getModelYearSplit(aYearSplit);
		int inputYear=Integer.parseInt(modelYearSplit.substring(0, 4));
		String inputMonth="";
		if (modelYearSplit.length()==6){
			inputMonth=modelYearSplit.substring(4, 6);
		}
		// 临时用2011模型替代2012、2013年的模型
		if (inputYear==2012 || inputYear==2013){
			modelYearSplit="2011"+inputMonth;
		}
		ModelStore modelStore=new ModelStore(modelYearSplit,policySplit,this.WORK_PATH+this.WORK_FILE_PREFIX, this.classifierName,this.m_modelEvalFileShareMode);
		m_modelStore=modelStore;
	}
}
