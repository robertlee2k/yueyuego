package yueyueGo.fullModel;

import java.io.IOException;

import yueyueGo.AbstractModel;
import yueyueGo.BackTest;
import yueyueGo.ContinousModel;
import yueyueGo.NominalModel;
import yueyueGo.dataFormat.AvgLineDataFormat;
import yueyueGo.dataFormat.FullModelDataFormat;
import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.dataProcessor.InstanceHandler;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.datasource.DataIOHandler;
import yueyueGo.fullModel.classifier.BaggingM5PFullModel;
import yueyueGo.fullModel.classifier.MyNNFullModel;
import yueyueGo.utility.AppContext;
import yueyueGo.utility.FileUtility;

@Deprecated
public class BackTestFullModel extends BackTest {
	private boolean applyToMaModelInTestBack=false; //default is false
	protected FullModelDataFormat ARFF_FORMAT_FULLMODEL; //当前所用FULLMODEL数据文件格式 
	
	//覆盖父类
	public void init() {
		m_handSetSplitYear=new String[] {
				//手工指定短线模型所回测的范围
			"201703"
		};
		
		m_currentPolicy="短线策略";

		//初始化相应的Model文件数据格式
		ARFF_FORMAT_FULLMODEL=new FullModelDataFormat();
		if (applyToMaModelInTestBack==true){
			m_currentArffFormat=new AvgLineDataFormat();
		}else{
			m_currentArffFormat=ARFF_FORMAT_FULLMODEL; //以免误用
		}
		
		AppContext.clearContext();
		AppContext.createContext(ARFF_FORMAT_FULLMODEL.m_data_root_directory);	
		m_backtest_result_dir=AppContext.getBACKTEST_RESULT_DIR();
		
		
	}


	public static void main(String[] args) {
		try {
			BackTestFullModel fullModelWorker=new BackTestFullModel();
			fullModelWorker.init();
			
			//短线模型的历史回测
			fullModelWorker.callFullModelTestBack();
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据最新这个月的增量数据刷新模型
	 * @throws Exception
	 */
	protected void callRefreshFullModelUseLatestData() throws Exception{
		AbstractModel model=null;
		m_handSetSplitYear=new String[] {"201609"};
		
		//逐次刷新数据
		
		model=new BaggingM5PFullModel();
		model.initModelPurpose(AbstractModel.FOR_EVALUATE_MODEL);
		testBackward(model);
		
		model=new MyNNFullModel();
		model.initModelPurpose(AbstractModel.FOR_EVALUATE_MODEL);
		testBackward(model);
	}
	/**
	 * @throws Exception
	 * @throws IOException
	 */
	protected void callFullModelTestBack() throws Exception, IOException {
//		//按连续分类器回测历史数据
//		BaggingM5PFullModel cModel=new BaggingM5PFullModel();
////		BaggingRegressionFullModel cModel=new BaggingRegressionFullModel();
//		if (applyToMaModelInTestBack==true){//用fullModel模型来测试均线模型时不用重新build和评估
//			cModel.initModelPurpose(AbstractModel.FOR_BACKTEST_MODEL);
//		}
//
//		GeneralInstances continuousResult=testBackward(cModel);
//		//不真正回测了，直接从以前的结果文件中加载
////		GeneralInstances continuousResult=loadBackTestResultFromFile(cModel.getIdentifyName());
//
//		//按二分类器回测历史数据
////		BaggingJ48FullModel nModel=new BaggingJ48FullModel();
////		AdaboostFullModel nModel=new AdaboostFullModel();
//		MyNNFullModel nModel=new MyNNFullModel();
//		if (applyToMaModelInTestBack==true){//用fullModel模型来测试均线模型时不用重新build和评估
//			nModel.initModelPurpose(AbstractModel.FOR_BACKTEST_MODEL);
//		}	
//		
//		GeneralInstances nominalResult=testBackward(nModel);
//		//不真正回测了，直接从以前的结果文件中加载
////		GeneralInstances nominalResult=loadBackTestResultFromFile(nModel.getIdentifyName());
//
//		
//		saveResultsAndStatistics(nModel, nominalResult, cModel, continuousResult);
	}

//	/**
//	 * 隐藏父类的函数。
//	 * @param splitTrainYearClause
//	 * @param policy
//	 * @return
//	 */
//	protected String getSplitClause(int policyIndex,String splitYearClause,	String policy) {
//		return splitYearClause;
//	}
	
	

	/**
	 * 子类覆盖
	 * @param clModel
	 * @return
	 * @throws Exception
	 */
	@Override
	protected GeneralInstances getBacktestInstances(AbstractModel clModel)
			throws Exception {

		String arffFullFileName = null;
		if (applyToMaModelInTestBack==true){//用fullModel模型来测试均线模型时加载均线模型的arff
			arffFullFileName=getMaArffFileName(clModel);
		}else{
			arffFullFileName=getFullModelArffFileName(clModel);
		}
		GeneralInstances fullSetData;
		System.out.println("start to load File for fullset from File: "+ arffFullFileName  );
		fullSetData = DataIOHandler.getSuppier().loadDataFromFile( arffFullFileName);
		if (applyToMaModelInTestBack==true){//用fullModel模型来测试均线模型时加载均线模型的arff
			int pos = BaseInstanceProcessor.findATTPosition(fullSetData,m_currentArffFormat.m_policy_group);
			fullSetData = InstanceHandler.getHandler(fullSetData).removeAttribs(fullSetData,""+pos );
		}
		System.out.println("finish loading fullset Data. row : "+fullSetData.numInstances() + " column:"+ fullSetData.numAttributes());
		
//		//决定是否删除申万行业数据
//		if (clModel.m_removeSWData==true){
//			fullSetData=ArffFormat.removeSWData(fullSetData);
//			System.out.println("removed SW Data based on model definition. now column="+ fullSetData.numAttributes());
//		}
		return fullSetData;
	}


	/**
	 * 这是加载fullModel的arff文件，用于训练模型或为Fullmodel做评估
	 * @param clModel
	 * @return
	 */
	private String getFullModelArffFileName(AbstractModel clModel) {
		// 根据模型来决定是否要使用有计算字段的ARFF
//		String arffFile=null;
//		if (clModel.m_noCaculationAttrib==true){
//			arffFile=ARFF_FORMAT_FULLMODEL.m_arff_ext;
//		}else{
////			arffFile=ArffFormatFullModel.FULL_MODEL_LONG_ARFF_FILE;
//			throw new RuntimeException("we don't support Calculation fields any more");
//		}
//		String arffFullFileName=C_ROOT_DIRECTORY+ARFF_FORMAT_FULLMODEL.m_arff_ext;

//		return arffFullFileName;
		return null;
	}
	
	/**
	 * 这是加载原始的arff文件，仅用于回测，不用于训练。
	 * @param clModel
	 * @return
	 */
	private String getMaArffFileName(AbstractModel clModel) {
		// 根据模型来决定是否要使用有计算字段的ARFF
//		String arffFile=null;
//		if (clModel.m_noCaculationAttrib==true){
//			arffFile=ARFF_FORMAT.m_arff_ext;
//		}else{
////			arffFile=ArffFormat.LONG_ARFF_FILE;
//			throw new RuntimeException("we don't support Calculation fields any more");
//		}
//		String arffFullFileName=EnvConstants.AVG_LINE_ROOT_DIR+ARFF_FORMAT.m_arff_ext;
//		return arffFullFileName;
		return null;
	}


	
	protected GeneralInstances mergeResultWithData(GeneralInstances resultData,GeneralInstances referenceData,String dataToAdd,int format) throws Exception{
//		GeneralInstances left=null;		
//		//读取磁盘上预先保存的左侧数据
//		if (applyToMaModelInTestBack==true){
//			//TODO 这个地方要修改
////			left=DataIOHandler.getSuppier().loadDataFromFile(EnvConstants.AVG_LINE_ROOT_DIR+ARFF_FORMAT.m_data_file_prefix+"-left.arff");
//		}else{
//			left=DataIOHandler.getSuppier().loadDataFromFile(AppContext.getC_ROOT_DIRECTORY()+ARFF_FORMAT_FULLMODEL.m_data_file_prefix+"-left.arff");
//		}
		
//		MergeClassifyResults merge=new MergeClassifyResults(m_currentArffFormat.m_policy_group);
//		GeneralInstances mergedResult = merge.mergeResults(resultData, referenceData,dataToAdd, left);
//		
//		//返回结果之前需要按TradeDate重新排序
//		int tradeDateIndex=BaseInstanceProcessor.findATTPosition(mergedResult, ArffFormat.TRADE_DATE);
//		mergedResult.sort(tradeDateIndex-1);
//		// 给mergedResult瘦身。 2=yearmonth, 6=datadate,7=positive,8=bias
//		String[] attributeToRemove=new String[]{ArffFormat.YEAR_MONTH,ArffFormat.DATA_DATE,ArffFormat.IS_POSITIVE,ArffFormat.BIAS5};
//		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(mergedResult);
//		mergedResult=instanceProcessor.removeAttribs(mergedResult, attributeToRemove);
//		
//		if (applyToMaModelInTestBack==false){
//			//插入一列“均线策略”为计算程序使用
//			mergedResult=instanceProcessor.AddAttributeWithValue(mergedResult, AvgLineDataFormat.SELECTED_AVGLINE,"numeric","0");
//		}
//		return mergedResult;
		return null;

	}

	
	//override 父类，设置历史回测的目录
	protected String prepareModelWorkPath(AbstractModel clModel){
		String workPath=null;
		if (clModel instanceof ContinousModel){
			workPath=AppContext.getCONTINOUS_CLASSIFIER_DIR()+clModel.getIdentifyName()+"\\";
		}else if (clModel instanceof NominalModel){
			workPath=AppContext.getNOMINAL_CLASSIFIER_DIR()+clModel.getIdentifyName()+"\\";
		}
		//根据不同的原始数据（策略）设置不同的模型工作目录
//		workPath+=ARFF_FORMAT.m_arff_file_prefix+"\\";
		FileUtility.mkdirIfNotExist(workPath);
		

		String modelPrefix=m_currentArffFormat.getModelDataPrefix()+"("+FullModelDataFormat.FULLMODEL_FORMAT+")"; //"extData2005-2016";
		return workPath+modelPrefix;
	}
}
