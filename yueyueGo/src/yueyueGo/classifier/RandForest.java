package yueyueGo.classifier;

import weka.classifiers.Classifier;
import yueyueGo.ModelStore;
import yueyueGo.NominalClassifier;
import yueyueGo.ParrallelizedRunning;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaInstances;
import yueyueGo.utility.ClassifyUtility;
import yueyueGo.utility.MyRandomForestClassifer;

public class RandForest extends NominalClassifier implements ParrallelizedRunning{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7135604627890964460L;

	
	protected double leafMinObjNum; //叶子节点最小的大小

	@Override
	protected void initializeParams() {
		m_skipTrainInBacktest = false;
		m_skipEvalInBacktest = false;
		m_policySubGroup = new String[]{"5","10","20","30","60" };

		classifierName=ClassifyUtility.RANDOM_FOREST;	

		m_modelFileShareMode=ModelStore.QUARTER_SHARED_MODEL; //覆盖父类，设定模型和评估文件的共用模式
		m_evalDataSplitMode=ModelStore.USE_NINE_MONTHS_DATA_FOR_EVAL;//USE_YEAR_DATA_FOR_EVAL; //评估区间使用一年数据 （截止20170103，这个是效果最好的）		
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
			if (this.m_skipTrainInBacktest==false){ //如果要重新构建模型，那最多2个线程在外面
				recommendThreads=2;
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
