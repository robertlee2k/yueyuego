package yueyueGo.classifier;

import yueyueGo.ModelStore;
import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.dataFormat.MomentumDataFormat;
import yueyueGo.utility.EvaluationConfDefinition;

public class ClassiferInitFactory {
	public static final int FOR_BACK_TEST=1;
	public static final int FOR_PREDICT=2;

	public static BaggingM5P initBaggingM5P(ArffFormat format,int purpose){
		BaggingM5P model=null;

		//设置动量策略参数
		if (format instanceof MomentumDataFormat){
			model=new BaggingM5P();
			switch (purpose) {
			case FOR_BACK_TEST:
				model.m_skipTrainInBacktest=false;
				model.m_skipEvalInBacktest=false;
				break;
			case FOR_PREDICT:
				break;
			}

			model.m_policySubGroup = new String[]{"" };
			model.m_usePCA=true; 
			model.useMultiPCA=true;
			model.bagging_iteration=10;	//bagging特有参数
			model.leafMinObjNum=300; //叶子节点最小的
			model.divided=300; //将trainingData分成多少份
			model.m_modelFileShareMode=ModelStore.QUARTER_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
			model.m_evalDataSplitMode=ModelStore.USE_NINE_MONTHS_DATA_FOR_EVAL;//USE_YEAR_DATA_FOR_EVAL; //评估区间使用一年数据 （截止20170103，这个是效果最好的）

			//获取评估参数设置  
			EvaluationConfDefinition evalConf=new EvaluationConfDefinition(model.classifierName,format);
			model.m_evalConf=evalConf;			
		}
		return model;
	}
}
