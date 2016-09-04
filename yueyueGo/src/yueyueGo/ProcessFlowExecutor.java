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
	public String doPredictProcess() throws Exception {

		
		System.out.println("-----------------start for " + yearSplit + "-----------------policy=" + policySplit);
		
		clModel.generateModelAndEvalFileName(yearSplit,policySplit);

		Classifier model = null;

		//是否需要重做训练阶段
		if (clModel.m_skipTrainInBacktest == false) { 
			System.out.println("start to build model");
			model = clModel.trainData(trainingData);
		} 
		
		//TODO 之所以这样load进来又释放，是因为loadModel里面有子类设置不同mdl和eval的方法，以后再改。
		if (model==null) {//如果model不是刚刚新建的，试着从已存在的文件里加载
			model = clModel.loadModel(yearSplit,policySplit);
		}		

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
		
		String evalSummary=yearSplit+","+policySplit+",";
		evalSummary+=clModel.predictData(testingData, result);
		testingData=null;//释放内存
		System.out.println("complete for time " + yearSplit +" policy="+ policySplit);
		return evalSummary;
	}
	
	 @Override
	 public String call() {
		 String resultSummary=null;
		 System.out.println(Thread.currentThread().getName() + "正在以线程方式执行。。。");
		 try {
			 resultSummary=this.doPredictProcess();
		 } catch (Exception e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }
		 clModel=null;
		 trainingData=null;
		 testingData=null;
		 return resultSummary;
	 }
	 
	 
}
