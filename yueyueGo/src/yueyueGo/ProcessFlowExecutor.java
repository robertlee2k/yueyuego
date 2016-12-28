package yueyueGo;

import java.util.concurrent.Callable;

import weka.classifiers.Classifier;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.utility.ClassifyUtility;

public class ProcessFlowExecutor implements Callable<String> {
	private BaseClassifier clModel;
	private GeneralInstances result;
	private String yearSplit;
	private String policySplit;
	private GeneralInstances trainingData;
	private GeneralInstances evalData;
	private GeneralInstances testingData;
	
	public ProcessFlowExecutor(BaseClassifier a_clModel,
			 GeneralInstances a_result, String a_yearSplit,
			String a_policySplit,GeneralInstances a_trainingData,
			GeneralInstances a_evalData, GeneralInstances a_testingData){
		clModel=a_clModel;
		result=a_result;
		yearSplit=a_yearSplit;
		policySplit=a_policySplit;
		trainingData=a_trainingData;
		testingData=a_testingData;
		evalData=a_evalData;
	}
	

	public void doPredictProcess() throws Exception {

		
		System.out.println("----Entering ProcessFlowExecutor for " + yearSplit + "------policy=" + policySplit);

		Classifier model = null;

		//是否build model 
		if (clModel.m_skipTrainInBacktest == false) {
			
			System.out.println("start to build model");

			//初始化回测创建模型时使用的modelStore对象（按yearSplit和policysplit分割处理）
			clModel.initModelStore(yearSplit,policySplit);
			model=clModel.trainData(trainingData);
			
			//输出模型的confusionMatrix
			boolean isNominal=false;
			if (clModel instanceof NominalClassifier){
				isNominal=true;
			}
			ClassifyUtility.getConfusionMatrix(trainingData,evalData, model,isNominal);
		} 
//		Evaluation eval = new Evaluation(trainingData);
		trainingData=null;//释放内存 （不管是不是用到了）
		model=null; //释放内存
		
		//获取评估和预测用的模型及阀值数据
		clModel.locateModelStore(yearSplit,policySplit);
		//是否需要重做评估阶段
		if (clModel.m_skipEvalInBacktest == false) {
			clModel.evaluateModel(evalData, policySplit);
		}
		
		evalData=null;//释放内存 （不管是不是用到了）
		
		
		clModel.predictData(testingData, result,yearSplit,policySplit);
		testingData=null;//释放内存
		
		
		//清除相应的内部内存
		clModel.cleanUp();

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
