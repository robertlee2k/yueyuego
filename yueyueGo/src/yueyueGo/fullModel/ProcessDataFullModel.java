package yueyueGo.fullModel;

import weka.core.Instances;
import yueyueGo.ArffFormat;
import yueyueGo.BaseClassifier;
import yueyueGo.FileUtility;
import yueyueGo.FormatUtility;
import yueyueGo.InstanceUtility;
import yueyueGo.ProcessData;
import yueyueGo.classifier.AdaboostClassifier;
import yueyueGo.classifier.BaggingM5P;

public class ProcessDataFullModel extends ProcessData {
	public static String C_ROOT_DIRECTORY = "C:\\trend\\fullModel\\";
	
	public static void main(String[] args) {
		try {
			
			UpdateHistoryArffFullModel.createFullModelInstances();
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @throws Exception
	 */
	protected static void callFullModelPredict() throws Exception {

		//BaggingM5P
		BaggingM5P cBagModel=new BaggingM5P();
		Instances baggingInstances=predictFullModelWithDB(cBagModel,PREDICT_WORK_DIR);		
		
		//Adaboost
		AdaboostClassifier adaModel=new AdaboostClassifier();
		Instances adaboostInstances=predictFullModelWithDB(adaModel,PREDICT_WORK_DIR);		
		
		//合并adaboost和bagging
		System.out.println("-----now output combined predictions----------"+adaModel.getIdentifyName());
		Instances left=InstanceUtility.removeAttribs(adaboostInstances, "5,6"); //只是为了使用下面的方法
		Instances mergedOutput=mergeResults(adaboostInstances,baggingInstances,ArffFormat.RESULT_PREDICTED_PROFIT,left);
		FileUtility.saveCSVFile(mergedOutput, PREDICT_WORK_DIR + "FullModel Selected Result-"+adaModel.getIdentifyName()+"-"+FormatUtility.getDateStringFor(1)+".csv");
		
	}	
	
	
	//直接访问数据库预测每天的自选股数据，不单独保存每个模型的选股
	protected static Instances predictFullModelWithDB(BaseClassifier clModel, String pathName) throws Exception {
		System.out.println("-----------------------------");
		Instances fullData = DBAccessFullModel.LoadFullModelDataFromDB(FormatUtility.getDateStringFor(0));
		Instances result=predict(clModel, pathName, fullData);
		return result;
	}	
	
	
	/**
	 * 隐藏父类的函数。
	 * @param splitTrainYearClause
	 * @param policy
	 * @return
	 */
	protected static String getSplitClause(String splitYearClause,	String policy) {
		return splitYearClause;
	}
}
