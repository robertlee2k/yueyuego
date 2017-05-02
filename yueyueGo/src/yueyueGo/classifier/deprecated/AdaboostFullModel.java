package yueyueGo.classifier.deprecated;

import yueyueGo.EnvConstants;
import yueyueGo.ModelStore;
import yueyueGo.classifier.AdaboostClassifier;
import yueyueGo.dataFormat.FullModelDataFormat;
import yueyueGo.utility.ClassifyUtility;

@Deprecated
public class AdaboostFullModel extends AdaboostClassifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3357697798648390329L;

	@Override
	protected void initializeParams() {

		m_policySubGroup = new String[]{""};
		modelArffFormat=FullModelDataFormat.FULLMODEL_FORMAT; //这个模型缺省是为FULLMODEL用的格式
		
		leafMinObjNum=1000; 	//j48树最小节点叶子数
		divided=EnvConstants.TRAINING_DATA_LIMIT/3000; //将trainingData分成多少份
		boost_iteration=10; 	//boost特有参数
		
		classifierName=ClassifyUtility.ADABOOST_FULLMODEL;
		
		m_modelFileShareMode=ModelStore.YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		
		m_usePCA=true; //覆盖父类，使用PCA
	}

	@Override
	//TODO 此类可以在子类中被覆盖（通过把yearsplit的值做处理，实现临时多年使用一个模型）
	public void locateModelStore(String targetYearSplit,String policySplit,String modelFilepathPrefix) {
		int inputYear=Integer.parseInt(targetYearSplit.substring(0, 4));
		String inputMonth="";
		if (targetYearSplit.length()==6){
			inputMonth=targetYearSplit.substring(4, 6);
		}
		// 临时用2011模型替代2012、2013年的模型
		if (inputYear==2012 || inputYear==2013){
			targetYearSplit="2011"+inputMonth;
		}
		ModelStore modelStore=new ModelStore(targetYearSplit,policySplit,modelFilepathPrefix,this);
		m_modelStore=modelStore;
	}
}
