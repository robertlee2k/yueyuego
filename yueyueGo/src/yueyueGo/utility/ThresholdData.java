package yueyueGo.utility;
import java.io.Serializable;

import weka.core.SerializationHelper;


public class ThresholdData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6309018541536909105L;
	private double thresholdMin=99999;
	private double thresholdMax=99999;
	private double startPercent=99999;
	private double endPercent=99999;
	private boolean isGuessed=false; //阀值是使用的缺省值
	private String policySplit=null; //当前评估数据对应的Policy；
	private String targetYearSplit=null; //当前评估数据对应的目标测试月份
	private String evalYearSplit=null; //当前评估数据对应的评估起始月份
	private double[] focosAreaRatio;//当前评估数据的不同Top 比例
	private double[] modelAUC; //当前评估数据的不同Top 比例应用于对应Model所计算的AUC列表
	
	private String modelYearSplit; //当前评估数据下所选择的模型数据结束年月 （于下面的ModelFileName其实冗余了）
	private String modelFileName=null;//当前评估数据下所选择的模型文件名称
	
	
	public String getPolicySplit() {
		return policySplit;
	}

	public void setPolicySplit(String policySplit) {
		this.policySplit = policySplit;
	}

	public String getModelYearSplit() {
		return modelYearSplit;
	}

	public void setModelYearSplit(String modelYearSplit) {
		this.modelYearSplit = modelYearSplit;
	}

	public double[] getFocosAreaRatio() {
		return focosAreaRatio;
	}

	public void setFocosAreaRatio(double[] focosAreaRatio) {
		this.focosAreaRatio = focosAreaRatio;
	}

	public double[] getModelAUC() {
		return modelAUC;
	}

	public void setModelAUC(double[] modelAUC) {
		this.modelAUC = modelAUC;
	}

	public String getTargetYearSplit() {
		return targetYearSplit;
	}

	public void setTargetYearSplit(String targetYearSplit) {
		this.targetYearSplit = targetYearSplit;
	}

	/**
	 * @return the evalYearSplit
	 */
	public String getEvalYearSplit() {
		return evalYearSplit;
	}

	/**
	 * @param evalYearSplit the evalYearSplit to set
	 */
	public void setEvalYearSplit(String evalYearSplit) {
		this.evalYearSplit = evalYearSplit;
	}



	public String getModelFileName() {
		return modelFileName;
	}

	public void setModelFileName(String modelFileName) {
		this.modelFileName = modelFileName;
	}

	public static ThresholdData loadDataFromFile(String evalFileName) throws Exception{
		//读取Threshold数据文件
		ThresholdData thresholdData=(ThresholdData)SerializationHelper.read( evalFileName);
		return thresholdData;
	}
	
	public static void saveEvaluationToFile(String evalFileName,ThresholdData thresholdData) throws Exception {
		SerializationHelper.write( evalFileName, thresholdData);
		System.out.println("evaluation saved to :"+ evalFileName);
	}
	
//	public void loadDataFromFile(String evalFileName) throws Exception{
//		//读取Threshold数据文件
//		
//		@SuppressWarnings("unchecked")
//		Vector<Object> v_threshold = (Vector<Object>) SerializationHelper.read( evalFileName);
//		System.out.println("Classifier Threshold Loaded From: "+ evalFileName);
//
//		thresholdMin = ((Double) v_threshold.get(0)).doubleValue();
//		// 是否真的要设置上限需要评估8888
//		thresholdMax = ((Double) v_threshold.get(1)).doubleValue();
//		System.out.println("full market thresholding value：between "	+ thresholdMin + " , "+ thresholdMax);
//		try{
//			startPercent = ((Double) v_threshold.get(2)).doubleValue();
//			// 是否真的要设置上限需要评估8888
//			endPercent = ((Double) v_threshold.get(3)).doubleValue();
//			System.out.println("full market percentile value：between "	+ startPercent + " , "+ endPercent);
//
//			//		thresholdMin_hs300=-1;
//			//		thresholdMax_hs300=-1;
//			//		if (seperate_classify_HS300==true){
//			//			thresholdMin_hs300=((Double) v_threshold.get(2)).doubleValue();
//			//			// 是否真的要设置上限需要评估
//			//			thresholdMax_hs300=((Double) v_threshold.get(3)).doubleValue();
//			//			System.out.println("HS300 index thresholding value：between "	+ thresholdMin_hs300 + " , "+ thresholdMax_hs300);
//			//		}
//		} catch (java.lang.ArrayIndexOutOfBoundsException e){ //兼容旧的模型
//			System.err.println(e);
//			System.err.println("let's continue using threshold min="+thresholdMin+"  max="+thresholdMax);
//		}
//	}
	

	
//	public static void saveEvaluationToFile(String evalFileName,Vector<Double> v) throws Exception {
//		SerializationHelper.write( evalFileName, v);
//		System.out.println("evaluation saved to :"+ evalFileName);
//	}
	
	public double getThresholdMin() {
		return thresholdMin;
	}

	public void setStartPercent(double startPercent) {
		this.startPercent = startPercent;
	}

	public void setEndPercent(double endPercent) {
		this.endPercent = endPercent;
	}

	public void setThresholdMin(double thresholdMin) {
		this.thresholdMin = thresholdMin;
	}

	public double getThresholdMax() {
		return thresholdMax;
	}

	public void setThresholdMax(double thresholdMax) {
		this.thresholdMax = thresholdMax;
	}

	public double getStartPercent() {
		return startPercent;
	}

	public double getEndPercent() {
		return endPercent;
	}

	public boolean isGuessed() {
		return isGuessed;
	}

	public void setIsGuessed(boolean isGuessed) {
		this.isGuessed = isGuessed;
	}


	
}

