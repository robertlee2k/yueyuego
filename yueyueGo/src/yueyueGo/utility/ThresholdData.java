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
//		//TODO 是否真的要设置上限需要评估8888
//		thresholdMax = ((Double) v_threshold.get(1)).doubleValue();
//		System.out.println("full market thresholding value：between "	+ thresholdMin + " , "+ thresholdMax);
//		try{
//			startPercent = ((Double) v_threshold.get(2)).doubleValue();
//			//TODO 是否真的要设置上限需要评估8888
//			endPercent = ((Double) v_threshold.get(3)).doubleValue();
//			System.out.println("full market percentile value：between "	+ startPercent + " , "+ endPercent);
//
//			//		thresholdMin_hs300=-1;
//			//		thresholdMax_hs300=-1;
//			//		if (seperate_classify_HS300==true){
//			//			thresholdMin_hs300=((Double) v_threshold.get(2)).doubleValue();
//			//			//TODO 是否真的要设置上限需要评估
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


	
}

