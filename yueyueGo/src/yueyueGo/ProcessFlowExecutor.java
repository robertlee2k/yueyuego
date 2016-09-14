package yueyueGo;

import java.util.concurrent.Callable;

import weka.classifiers.Classifier;
import weka.core.Instances;
import yueyueGo.utility.InstanceUtility;

public class ProcessFlowExecutor implements Callable<String> {
	private BaseClassifier clModel;
	private Instances result;
	private String yearSplit;
	private String policySplit;
	private double lower_limit;
	private double upper_limit;
	private double tp_fp_ratio;
	private Instances trainingData;
	private Instances testingData;
	
	public ProcessFlowExecutor(BaseClassifier a_clModel,
			 Instances a_result, String a_yearSplit,
			String a_policySplit, double a_lower_limit, double a_upper_limit, double a_tp_fp_ratio,
			Instances a_trainingData, Instances a_testingData){
		clModel=a_clModel;
		result=a_result;
		yearSplit=a_yearSplit;
		policySplit=a_policySplit;
		lower_limit=a_lower_limit;
		upper_limit=a_upper_limit;
		tp_fp_ratio=a_tp_fp_ratio;
		trainingData=a_trainingData;
		testingData=a_testingData;
	}
	
	// paremeter result will be changed in the method! 
	public void doPredictProcess() throws Exception {

		
		System.out.println("-----------------start for " + yearSplit + "-----------------policy=" + policySplit);
		
		

		Classifier model = null;

		//是否需要重做训练阶段
		if (clModel.m_skipTrainInBacktest == false) {
			System.out.println("start to build model");
			//初始化回测创建模型时使用的modelStore对象（这里严格按yearsplit和policysplit分割处理）
			clModel.initModelStore(yearSplit,policySplit);
			model = clModel.trainData(trainingData);
		} 
		
		//找到回测评估、预测时应该使用modelStore对象（主要为获取model文件和eval文件名称）
		clModel.locateModelStore(yearSplit,policySplit);
		//是否需要重做评估阶段
		if (clModel.m_skipEvalInBacktest == false) {
			
			clModel.evaluateModel(trainingData, model, lower_limit,
					upper_limit,tp_fp_ratio);
		}
		
		trainingData=null;//释放内存
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
