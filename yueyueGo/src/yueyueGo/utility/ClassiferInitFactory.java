package yueyueGo.utility;


import yueyueGo.EvaluationStore;
import yueyueGo.ModelStore;
import yueyueGo.classifier.AdaboostClassifier;
import yueyueGo.classifier.BaggingM5P;
import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.dataFormat.MomentumDataFormat;

public class ClassiferInitFactory {

	
	public static BaggingM5P initBaggingM5P(ArffFormat format,int purpose){
		BaggingM5P model=null;
		model=new BaggingM5P();


		model.initModelPurpose(purpose);

		if (format instanceof MomentumDataFormat){
			//设置动量策略参数
			model.m_policySubGroup = new String[]{"" };
			model.m_usePCA=true; 
			model.useMultiPCA=true;
			model.bagging_iteration=10;	//bagging特有参数
			model.leafMinObjNum=300; //叶子节点最小的
			model.divided=300; //将trainingData分成多少份
			model.m_modelFileShareMode=ModelStore.QUARTER_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
			model.m_evalDataSplitMode=EvaluationStore.USE_NINE_MONTHS_DATA_FOR_EVAL;//USE_YEAR_DATA_FOR_EVAL; //评估区间使用一年数据 （截止20170103，这个是效果最好的）
		}else{
			//否则返回缺省值
			
			model.bagging_iteration=5;
			model.divided=500; //将trainingData分成多少份
			model.m_modelFileShareMode=ModelStore.HALF_YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
			model.m_evalDataSplitMode=EvaluationStore.USE_HALF_YEAR_DATA_FOR_EVAL;

		}
		return model;
	}
	
	public static AdaboostClassifier initAdaboost(ArffFormat format,int purpose){
		AdaboostClassifier model=null;
		model=new AdaboostClassifier();
	

		model.initModelPurpose(purpose);
		if (format instanceof MomentumDataFormat){
			//设置动量策略参数
			model.m_policySubGroup = new String[]{"" };

			model.m_modelFileShareMode=ModelStore.QUARTER_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
			
			model.leafMinObjNum=300; 	//j48树最小节点叶子数
			model.divided=300; //将trainingData分成多少份
			model.boost_iteration=10; 	//boost特有参数
			model.m_usePCA=true; //20121223尝试不使用PCA，效果一般且建模非常慢，所以放弃
			model.m_evalDataSplitMode=EvaluationStore.USE_NINE_MONTHS_DATA_FOR_EVAL; //尝试评估区间使用9个月数据（效果还不错）
		}else{
			//否则返回缺省值
			
			model.boost_iteration=5;
			model.divided=500; //将trainingData分成多少份
			model.m_modelFileShareMode=ModelStore.HALF_YEAR_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
			model.m_evalDataSplitMode=EvaluationStore.USE_HALF_YEAR_DATA_FOR_EVAL;
		}
		return model;
	}
}
