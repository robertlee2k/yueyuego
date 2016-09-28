package yueyueGo;

import java.util.concurrent.Callable;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.Instances;
import yueyueGo.utility.ClassifyUtility;
import yueyueGo.utility.EvaluationParams;
import yueyueGo.utility.FileUtility;

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
			
			//输出模型的confusionMatrix
			boolean isNominal=false;
			if (clModel instanceof NominalClassifier){
				isNominal=true;
			}
			ClassifyUtility.getConfusionMatrix(trainingData,evalData, model,isNominal);
		} 
		
		
		
		
		clModel.locateModelStore(actualYearSplit,policySplit);
		//是否需要重做评估阶段
		if (clModel.m_skipEvalInBacktest == false) {
			Evaluation eval = new Evaluation(trainingData);
			eval.evaluateModel(model, evalData); // evaluate on the sample data to get threshold
			ThresholdCurve tc = new ThresholdCurve();
			int classIndex = 1;
			Instances predictions=tc.getCurve(eval.predictions(), classIndex);
			FileUtility.SaveDataIntoFile(predictions, clModel.WORK_PATH+"\\ROCresult-withTrain.arff");
			
			EvaluationParams evalParams=clModel.getEvaluationInstance(policySplit);
			clModel.evaluateModel(evalData, model,evalParams);
		}
		trainingData=null;//释放内存 （不管是不是用到了）		
		evalData=null;//释放内存 （不管是不是用到了）
		model=null;//释放model，后面预测时会方法内是会重新加载的。
		
		clModel.predictData(testingData, result,yearSplit,policySplit);
		testingData=null;//释放内存
		System.out.println("complete for time " + yearSplit +" policy="+ policySplit);
		
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
