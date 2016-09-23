package yueyueGo;

import java.util.concurrent.Callable;

import weka.classifiers.Classifier;
import weka.core.Instances;
import yueyueGo.utility.ClassifyUtility;
import yueyueGo.utility.EvaluationParams;
import yueyueGo.utility.InstanceUtility;

public class ProcessFlowExecutor implements Callable<String> {
	private BaseClassifier clModel;
	private Instances result;
	private String yearSplit;
	private String policySplit;
	private Instances trainingData;
	private Instances evalData;
	private Instances testingData;
	
	public ProcessFlowExecutor(BaseClassifier a_clModel,
			 Instances a_result, String a_yearSplit,
			String a_policySplit,Instances a_trainingData,
			Instances a_evalData, Instances a_testingData){
		clModel=a_clModel;
		result=a_result;
		yearSplit=a_yearSplit;
		policySplit=a_policySplit;
		trainingData=a_trainingData;
		testingData=a_testingData;
		evalData=a_evalData;
	}
	

	public void doPredictProcess() throws Exception {

		
		System.out.println("-----------------start for " + yearSplit + "-----------------policy=" + policySplit);
		
		

		Classifier model = null;
		//找到回测创建评估预测时应该使用modelStore对象（主要为获取model文件和eval文件名称）-- 评估时创建的mdl并不是当前年份的，而是前推一年的
		String actualYearSplit=ClassifyUtility.getLastYearSplit(yearSplit);
		//是否build model （注意，这里build model的数据已变为当前周期前推一年的数据 如果是2010XX.mdl 则取2009年XX月之前的数据build， 剩下的一年数据做评估用）
		if (clModel.m_skipTrainInBacktest == false) {
			System.out.println("start to build model");
			//初始化回测创建模型时使用的modelStore对象（这里严格按yearsplit和policysplit分割处理）
			clModel.initModelStore(actualYearSplit,policySplit);
			model = clModel.trainData(trainingData);
		} 
		trainingData=null;//释放内存 （不管是不是用到了）
		
		
		clModel.locateModelStore(actualYearSplit,policySplit);
		//是否需要重做评估阶段
		if (clModel.m_skipEvalInBacktest == false) {
			EvaluationParams evalParams=clModel.getEvaluationInstance(policySplit);
			clModel.evaluateModel(evalData, model,evalParams);
		}
		evalData=null;//释放内存 （不管是不是用到了）
		model=null;//释放model，后面预测时会方法内是会重新加载的。

		//处理testingData
		//对于二分类器，这里要把输入的收益率转换为分类变量
		if (clModel instanceof NominalClassifier ){
			testingData=((NominalClassifier)clModel).processDataForNominalClassifier(testingData,true);
		}
		testingData = InstanceUtility.removeAttribs(testingData, ArffFormat.YEAR_MONTH_INDEX);

		System.out.println("testing data size, row: "
				+ testingData.numInstances() + " column: "
				+ testingData.numAttributes());
		if (clModel.m_saveArffInBacktest) {
			clModel.saveArffFile(testingData,"test", yearSplit,policySplit);
		}
		
		clModel.predictData(testingData, result,yearSplit,policySplit);
		testingData=null;//释放内存
		System.out.println("complete for time " + yearSplit +" policy="+ policySplit);

	}
	
	 @Override
	 public String call() {
//		 String resultSummary=null;
		 System.out.println(Thread.currentThread().getName() + "正在以线程方式执行。。。");
		 try {
//			 resultSummary=
					 this.doPredictProcess();
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 clModel=null;
		 trainingData=null;
		 testingData=null;
		 return null; //resultSummary;
	 }
	 
	 
}
