package yueyueGo.fullModel.classifier;

import yueyueGo.EnvConstants;
import yueyueGo.ModelStore;
import yueyueGo.classifier.BaggingM5P;
import yueyueGo.dataFormat.FullModelDataFormat;
import yueyueGo.utility.ClassifyUtility;

//result changed because of reference data not matched=15096 while good change number=5665
//good ratio=37.53% average changed shouyilv=4.58% @ winrate thredhold= /50.00% /
//number of records for full market=4225771
//shouyilv average for full market=0.29%
//selected shouyilv average for full market =1.58% count=145281
//selected shouyilv average for hs300 =0.98% count=12875
//selected shouyilv average for zz500 =1.10% count=32436


//***************************************CLASSIFY DATE=2017-01-19 
//ClassifyIdentity=BaggingM5PABFullModel-multiPCA
//m_skipTrainInBacktest=true
//m_skipEvalInBacktest=false
//m_noCaculationAttrib=true
//m_removeSWData=true
//m_positiveLine=0.0
//m_modelDataSplitMode=6
//m_modelEvalFileShareMode=3
//modelArffFormat=3
//SAMPLE_LOWER_LIMIT={0.02,}
//SAMPLE_UPPER_LIMIT={0.04,}
//LIFT_UP_TARGET=1.8
//***************************************
//......................
//===============================output summary===================================== for : BaggingM5PABFullModel-multiPCA
//Monthly selected_TPR mean: 56.36% standard deviation=15.29% Skewness=-0.19 Kurtosis=0.32
//Monthly selected_LIFT mean : 1.15
//Monthly selected_positive summary: 74,393
//Monthly selected_count summary: 133,624
//Monthly selected_shouyilv average: 2.27% standard deviation=4.61% Skewness=0.65 Kurtosis=5.6
//Monthly total_shouyilv average: 0.18% standard deviation=2.84% Skewness=-0.53 Kurtosis=1.63
//mixed selected positive rate: 55.67%
//Monthly summary_judge_result summary: good number= 83 bad number=24
//===============================end of summary=====================================for : BaggingM5PABFullModel-multiPCA
//......................
//-----now output continuous predictions----------BaggingM5PABFullModel-multiPCA
//incoming resultData size, row=4395123 column=5
//incoming referenceData size, row=4395123 column=5
//Left data loaded, row=5335793 column=11
//number of results merged and processed: 4395123
//###### Finally selected count=98529  ######
// result changed because of reference data not matched=35095 while good change number=15949
// good ratio=45.45% average changed shouyilv=2.40%
// @ WINRATE_FILTER_FOR_SHOUYILV={50.00%, }
//number of records for full market=4395123
//shouyilv average for full market=0.1883%
//selected shouyilv average for full market =0.9255% count=98529

public class BaggingM5PFullModel extends BaggingM5P {
	 
	/**
	 * 
	 */
	private static final long serialVersionUID = 8505755558382340493L;
	@Override
	protected void initializeParams() {
		m_skipTrainInBacktest = false;
		m_skipEvalInBacktest = false;
		m_policySubGroup = new String[]{""};
		modelArffFormat=FullModelDataFormat.FULLMODEL_FORMAT; //这个模型缺省是为FULLMODEL用的格式
		
		leafMinObjNum=1000;
		divided=EnvConstants.TRAINING_DATA_LIMIT/3000;	
		bagging_iteration=10;	//bagging特有参数
		
		classifierName= ClassifyUtility.BAGGING_M5P_FULLMODEL;
		m_modelFileShareMode=ModelStore.QUARTER_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式为半年
		m_evalDataSplitMode=ModelStore.USE_HALF_YEAR_DATA_FOR_EVAL;//覆盖父类，设定模型和评估间隔为6个月
		
		m_usePCA=true; //覆盖父类，使用PCA
		useMultiPCA=true; //bagging 内的每个模型自己有单独的PCA
	}
}
