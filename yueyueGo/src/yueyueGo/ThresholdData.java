package yueyueGo;
import java.util.Vector;

import weka.core.SerializationHelper;


public class ThresholdData {
	private double thresholdMin=99999;
	private double thresholdMax=99999;
//	private double thresholdMin_hs300=99999;
//	private double thresholdMax_hs300=99999;
	private double meanABError=99999;

	
	public void loadDataFromFile(String evalFileName) throws Exception{
		//读取Threshold数据文件
		
		@SuppressWarnings("unchecked")
		Vector<Object> v_threshold = (Vector<Object>) SerializationHelper.read( evalFileName);
		System.out.println("Classifier Threshold Loaded From: "+ evalFileName);

		thresholdMin = ((Double) v_threshold.get(0)).doubleValue();
		//TODO 是否真的要设置上限需要评估8888
		thresholdMax = ((Double) v_threshold.get(1)).doubleValue();
		System.out.println("full market thresholding value：between "	+ thresholdMin + " , "+ thresholdMax);
		meanABError=((Double) v_threshold.get(2)).doubleValue();
		System.out.println("full market meanABError value：between= "	+ meanABError);
//		thresholdMin_hs300=-1;
//		thresholdMax_hs300=-1;
//		if (seperate_classify_HS300==true){
//			thresholdMin_hs300=((Double) v_threshold.get(2)).doubleValue();
//			//TODO 是否真的要设置上限需要评估
//			thresholdMax_hs300=((Double) v_threshold.get(3)).doubleValue();
//			System.out.println("HS300 index thresholding value：between "	+ thresholdMin_hs300 + " , "+ thresholdMax_hs300);
//		}
		
	}
	

	
	public static void saveEvaluationToFile(String evalFileName,Vector<Double> v) throws Exception {
		SerializationHelper.write( evalFileName, v);
		System.out.println("evaluation saved to :"+ evalFileName);
	}
	
	public double getThresholdMin() {
		return thresholdMin;
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


	public double getMeanABError() {
		return meanABError;
	}

	public void setMeanABError(double meanABError) {
		this.meanABError = meanABError;
	}
	
}

