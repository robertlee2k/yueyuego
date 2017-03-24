package yueyueGo.utility;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.databeans.GeneralInstances;

public class EvaluationBenchmark {
	static public final int POSITIVE=1;
	static public final int NEGATIVE=0;

	boolean nominalClass;
//	double train_tp_fp_ratio=Double.NaN;
//    double train_avg_shouyilv=Double.NaN;
    double eval_tp_fp_ratio=Double.NaN;
    double eval_avg_shouyilv=Double.NaN;

	public boolean isNominalClass() {
		return nominalClass;
	}

//	public double getTrain_tp_fp_ratio() {
//		return train_tp_fp_ratio;
//	}
//
//	public double getTrain_avg_shouyilv() {
//		return train_avg_shouyilv;
//	}

	public double getEval_tp_fp_ratio() {
		return eval_tp_fp_ratio;
	}

	public double getEval_avg_shouyilv() {
		return eval_avg_shouyilv;
	}

	

	public EvaluationBenchmark(GeneralInstances evalData, boolean isNominal) {
		this.nominalClass=isNominal;
		int pos=-1;
		if (isNominal){
//			pos=InstanceUtility.findATTPosition(trainData, ArffFormat.IS_POSITIVE);
//			this.train_tp_fp_ratio=calculateTpFpRatio(trainData.attributeToDoubleArray(pos - 1));
			pos=BaseInstanceProcessor.findATTPosition(evalData, ArffFormat.IS_POSITIVE);
			this.eval_tp_fp_ratio=calculateTpFpRatio(evalData.attributeToDoubleArray(pos - 1));
		}else{
//			pos=InstanceUtility.findATTPosition(trainData, ArffFormat.SHOUYILV);
//			this.train_avg_shouyilv=calculateAvgShouyilv(trainData.attributeToDoubleArray(pos - 1));
			pos=BaseInstanceProcessor.findATTPosition(evalData, ArffFormat.SHOUYILV);
			double[] data=evalData.attributeToDoubleArray(pos - 1);
			this.eval_tp_fp_ratio=calculateTpFpRatioForNemeric(data,0);
			this.eval_avg_shouyilv=calculateAvgShouyilv(data);
		}
	}

	private static double calculateAvgShouyilv(double[] data){
		DescriptiveStatistics summary=new DescriptiveStatistics(data);
		return summary.getMean();
	}

	private static double calculateTpFpRatio(double[] data){
    	int length=data.length;
    	int positiveCount=0;
    	int negativeCount=0;
    	int input=0;
    	
    	for (int i=0;i<length;i++){
    		input=(int)data[i];
    		switch (input) {
			case POSITIVE:
    			positiveCount++;				
				break;
			case NEGATIVE:
    			negativeCount++;				
				break;
			default:
				throw new RuntimeException("undefined data type in trying to get evaluationBenchmark");
			}
    	}
    	double result;
    	if (negativeCount>0){
    		result=(double)positiveCount/negativeCount;
    	}else{
    		result=1; //100% positive
    	}
    	return result;
    }
	
	private static double calculateTpFpRatioForNemeric(double[] data,double judgeLine){
    	int length=data.length;
    	int positiveCount=0;
    	int negativeCount=0;

    	
    	for (int i=0;i<length;i++){
    		if (data[i]>judgeLine){
    			positiveCount++;				
    		}else{
    			negativeCount++;				
			}
    	}
    	double result;
    	if (negativeCount>0){
    		result=(double)positiveCount/negativeCount;
    	}else{
    		result=1; //100% positive
    	}
    	return result;
    }
}
