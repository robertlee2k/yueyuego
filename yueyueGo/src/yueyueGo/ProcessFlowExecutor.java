package yueyueGo;

import java.util.concurrent.Callable;

import weka.classifiers.Classifier;
import yueyueGo.databeans.GeneralDataTag;
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
	private GeneralDataTag[] dataTags;
	private String modelFilepathPrefix;
	
	public ProcessFlowExecutor(BaseClassifier a_clModel,
			 GeneralInstances a_result, String a_yearSplit,
			String a_policySplit,GeneralInstances a_trainingData,
			GeneralInstances a_evalData, GeneralInstances a_testingData,GeneralDataTag[] a_dataTags,String a_modelFilepathPrefix){
		clModel=a_clModel;
		result=a_result;
		yearSplit=a_yearSplit;
		policySplit=a_policySplit;
		trainingData=a_trainingData;
		testingData=a_testingData;
		evalData=a_evalData;
		dataTags=a_dataTags;
		modelFilepathPrefix=a_modelFilepathPrefix;
	}
	

	public void doPredictProcess() throws Exception {

		
		System.out.println("----Entering ProcessFlowExecutor for " + yearSplit + "------policy=" + policySplit);

		Classifier model = null;
		//初始化回测创建模型时使用的modelStore对象（按yearSplit和policysplit分割处理）
		clModel.locateModelStore(yearSplit,policySplit,modelFilepathPrefix);
		
		

		//是否build model 
		if (clModel.m_skipTrainInBacktest == false) {
			System.out.println("start to build model");
			String msg=clModel.validateTrainingData(dataTags[0]);
			if (msg!=null){
				throw new Exception(msg);
			}
			model=clModel.trainData(trainingData);
			
			//输出模型的confusionMatrix
			boolean isNominal=false;
			if (clModel instanceof NominalClassifier){
				isNominal=true;
			}
			
			msg=clModel.validateEvalData(dataTags[1]);
			if (msg!=null){
				throw new Exception(msg);
			}
			ClassifyUtility.getConfusionMatrix(trainingData,evalData, model,isNominal);
		} 
		trainingData=null;//释放内存 （不管是不是用到了）
		model=null; //释放内存

		//是否需要重做评估阶段
		if (clModel.m_skipEvalInBacktest == false) {
			String msg=clModel.validateEvalData(dataTags[1]);
			if (msg!=null){
				throw new Exception(msg);
			}
			clModel.evaluateModel(evalData, policySplit);
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
