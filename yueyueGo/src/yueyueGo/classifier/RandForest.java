package yueyueGo.classifier;

import weka.classifiers.Classifier;
import yueyueGo.ModelStore;
import yueyueGo.NominalClassifier;
import yueyueGo.ParrallelizedRunning;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaInstances;
import yueyueGo.utility.ClassifyUtility;
import yueyueGo.utility.MyRandomForestClassifer;

//***************************************CLASSIFY DATE=2017-03-05 
//ClassifyIdentity=RandForest
//m_skipTrainInBacktest=true
//m_skipEvalInBacktest=false
//m_noCaculationAttrib=true
//m_removeSWData=true
//m_positiveLine=0.0
//m_modelDataSplitMode=6
//m_modelEvalFileShareMode=6
//modelArffFormat=2
//SAMPLE_LOWER_LIMIT={0.03,0.03,0.03,0.03,0.03,}
//SAMPLE_UPPER_LIMIT={0.1,0.1,0.1,0.1,0.1,}
//LIFT_UP_TARGET=1.8
//***************************************
//......................
//===============================output summary===================================== for : RandForest
//Monthly selected_TPR mean: 24.34% standard deviation=27.78% Skewness=0.99 Kurtosis=-0.1
//Monthly selected_LIFT mean : 0.76
//Monthly selected_positive summary: 36,265
//Monthly selected_count summary: 89,867
//Monthly selected_shouyilv average: 0.99% standard deviation=5.63% Skewness=3.36 Kurtosis=21.33
//Monthly total_shouyilv average: 0.77% standard deviation=5.96% Skewness=3.11 Kurtosis=16.17
//mixed selected positive rate: 40.35%
//Monthly summary_judge_result summary: good number= 317 bad number=228
//===============================end of summary=====================================for : RandForest
//-----now output nominal predictions----------RandForest
//incoming resultData size, row=1511065 column=6
//incoming referenceData size, row=1511065 column=6
//Left data loaded, row=1793927 column=12
//number of results merged and processed: 1511065
//###### Finally selected count=35943  ######
// result changed because of reference data not matched=53924 while good change number=37819
// good ratio=70.13% average changed shouyilv=1.30%
// @ SHOUYILV_FILTER_FOR_WINRATE={1.00%,2.00%,3.00%,3.00%,4.00%, }
//number of records for full market=1511065
//shouyilv average for full market=0.6076%
//selected shouyilv average for full market =1.9818% count=35943
//-----end of test backward------

public class RandForest extends NominalClassifier implements ParrallelizedRunning{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7135604627890964460L;

	
	protected double leafMinObjNum; //叶子节点最小的大小

	@Override
	protected void initializeParams() {
		m_skipTrainInBacktest = true;
		m_skipEvalInBacktest = false;
		m_policySubGroup = new String[]{"5","10","20","30","60" };

		classifierName=ClassifyUtility.RANDOM_FOREST;	

		m_modelFileShareMode=ModelStore.HALF_YEAR_SHARED_MODEL;//.QUARTER_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		m_evalDataSplitMode=ModelStore.USE_HALF_YEAR_DATA_FOR_EVAL;//USE_NINE_MONTHS_DATA_FOR_EVAL;// //评估区间使用一年数据 （截止20170103，这个是效果最好的）		
		m_noCaculationAttrib=true;//不使用计算字段 (20161215试过无计算字段，效果不如有计算字段好） 
		m_removeSWData=true; //20161222尝试不用申万行业数据

		leafMinObjNum=300;
	}


	@Override
	protected Classifier buildModel(GeneralInstances trainData) throws Exception {
		MyRandomForestClassifer rForest=new MyRandomForestClassifer();
		rForest.setSeed(888);
		rForest.setNumTrees(1000);
		int features=trainData.numAttributes();
		int threads=ClassifyUtility.calculateExecutionSlots(trainData.numInstances(),features,20,100);
		rForest.setNumExecutionSlots(threads);
		rForest.setNumFeatures(features/8);
		rForest.setMinNum(leafMinObjNum);

		rForest.setNumDecimalPlaces(6);
		rForest.setDebug(true);
		rForest.buildClassifier(WekaInstances.convertToWekaInstances(trainData));
		System.out.println("out of bagger error in RF="+rForest.measureOutOfBagError());
		return rForest;
		
	}
	
	//	将外部的并发线程根据算法内并发的计算强度折算出新的建议值
	public int recommendRunningThreads(int runningThreads){
		int recommendThreads=1; //缺省值
		if (runningThreads>1){ //如果外部调用者是多线程运行
			if (this.m_skipTrainInBacktest==false){ //如果要重新构建模型，那最多1个线程在外面
				recommendThreads=1;
			}else if (this.m_skipEvalInBacktest==false){ //如果不需要构建模型，但需要重新评估模型，那将并发数折半
				recommendThreads=runningThreads/2;
			}else{ //如果只需要回测，简单减一后返回。
				recommendThreads=runningThreads-1;
			}
		}else{//如果外部不是多线程返回1
			recommendThreads=1;
		}
		return recommendThreads;
	}
}
