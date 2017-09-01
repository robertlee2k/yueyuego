package yueyueGo;

import java.util.concurrent.Callable;

import weka.classifiers.Classifier;
import yueyueGo.databeans.DataInstances;
import yueyueGo.databeans.GeneralDataTag;
import yueyueGo.databeans.GeneralInstances;

public class ProcessFlowExecutor implements Callable<String> {
	private BaseClassifier clModel;
	private GeneralInstances result;
	private String yearSplit;
	private String policySplit;
	private GeneralInstances trainingData;
	private GeneralInstances evalData;
	private GeneralInstances testingData;
	private GeneralDataTag[] dataTags;
	private String modelFilePath;
	private String modelFilePrefix;
	
	public ProcessFlowExecutor(BaseClassifier a_clModel,
			 GeneralInstances a_result, String a_yearSplit,
			String a_policySplit,GeneralInstances a_trainingData,
			GeneralInstances a_evalData, GeneralInstances a_testingData,GeneralDataTag[] a_dataTags,String a_modelFilePath, String a_modelFilePrefix){
		clModel=a_clModel;
		result=a_result;
		yearSplit=a_yearSplit;
		policySplit=a_policySplit;
		trainingData=a_trainingData;
		testingData=a_testingData;
		evalData=a_evalData;
		dataTags=a_dataTags;
		modelFilePath=a_modelFilePath;
		modelFilePrefix=a_modelFilePrefix;
	}
	

	public void doPredictProcess() throws Exception {

		
		System.out.println("----Entering ProcessFlowExecutor for " + yearSplit + "------policy=" + policySplit);

		Classifier model = null;

		
		

		//基于数据构建模型 
		if (clModel.is_skipTrainInBacktest() == false) {
			//初始化回测创建模型时使用的modelStore对象（按yearSplit和policysplit分割处理）
			ModelStore modelStore=new ModelStore(yearSplit,policySplit,modelFilePath,modelFilePrefix,clModel);
			
			System.out.println("start to build model");
			String msg=modelStore.validateTrainingData(dataTags[0]);
			if (msg!=null){
				throw new Exception(msg);
			}

			model=clModel.trainData(trainingData);
			// save model + header
			modelStore.setModel(model);
			modelStore.setModelFormat(new DataInstances(trainingData, 0));
			modelStore.saveModelToFiles();
			System.out.println("Training finished!");

			//TODO confusion matrix何时输出？
//			//输出模型的confusionMatrix
//			boolean isNominal=false;
//			if (clModel instanceof NominalClassifier){
//				isNominal=true;
//			}
//			
//			msg=clModel.validateEvalData(dataTags[1]);
//			if (msg!=null){
//				throw new Exception(msg);
//			}
//			ClassifyUtility.getConfusionMatrix(trainingData,evalData, model,isNominal);
		} 
		trainingData=null;//释放内存 （不管是不是用到了）
		model=null; //释放内存

		
		//设置评估或测试时所用的EvaluationStore
		clModel.locateEvalutationStore(yearSplit,policySplit,modelFilePath,modelFilePrefix);
		
		//是否需要重做评估阶段
		if (clModel.is_skipEvalInBacktest() == false) {
			String msg=clModel.validateEvalData(dataTags[1]);
			if (msg!=null){
				throw new Exception(msg);
			}
			clModel.evaluateModel(evalData);
		}
		
		evalData=null;//释放内存 （不管是不是用到了）
		
		//预测数据
		String msg=clModel.validateTestingData(dataTags[2]);
		if (msg!=null){
			throw new Exception(msg);
		}
		clModel.predictData(testingData, result,yearSplit,policySplit);
		testingData=null;//释放内存
		
		
		//清除相应的内部内存
		clModel.cleanUp();

	}
	
	 @Override
	 public String call() {
		 System.out.println(Thread.currentThread().getName() + "正在以线程方式执行。。。[yearsplit]="+yearSplit+" [policy]="+policySplit);
		 try {
			 this.doPredictProcess();
		 }catch (Exception e) {//某个线程出错误的时候把Exception加入对象中，不抛出。
				clModel.getClassifySummaries().appendExceptionSummary("\r\n [yearsplit]="+yearSplit+" [policy]="+policySplit+"\r\n"+e.toString());
				e.printStackTrace();
		 }
		 clModel=null;
		 trainingData=null;
		 testingData=null;
		 evalData=null;
		 return null; 
	 }
	 
	 
}
