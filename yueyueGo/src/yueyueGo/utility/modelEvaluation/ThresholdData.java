package yueyueGo.utility.modelEvaluation;
import java.io.Serializable;


public class ThresholdData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6309018541536909105L;

	private String policySplit=null; //当前评估数据对应的Policy；
	private String targetYearSplit=null; //当前评估数据对应的目标测试月份
	private String evalYearSplit=null; //当前评估数据对应的评估起始月份
	private double[] focosAreaRatio;//当前评估数据的不同Top 比例
	private double[] modelAUC; //当前评估数据的不同Top 比例应用于对应Model所计算的AUC列表

	//正向评估数据
	private double[] thresholds;   //阈值列表
	private double[] percentiles;  //阈值对应的percentile列表
	private int defaultThresholdIndex; 	//缺省阈值在阈值列表中的索引（由0开始），大于该阈值意味着该模型判断其为1
	
	private String modelYearSplit; //当前评估数据下所选择的模型数据结束年月 
	private String modelFileName=null;//当前评估数据下所选择的模型文件名称
	
	
	//下面是反向评估的数据
	private String reversedModelYearSplit;
	private String reversedModelFileName=null;
	private double reversedThreshold=99999; //判断为0的阈值，小于该值意味着该模型坚定认为其为0 （这是合并多个模型预测时使用的）
	private double reversedPercent=99999; //阈值对应的percentile
	
	
	public String getReversedModelYearSplit() {
		return reversedModelYearSplit;
	}
	public void setReversedModelYearSplit(String reversedModelYearSplit) {
		this.reversedModelYearSplit = reversedModelYearSplit;
	}
	public String getReversedModelFileName() {
		return reversedModelFileName;
	}
	public void setReversedModelFileName(String reversedModelFileName) {
		this.reversedModelFileName = reversedModelFileName;
	}
	public double getReversedThreshold() {
		return reversedThreshold;
	}
	public void setReversedThreshold(double reversedThreshold) {
		this.reversedThreshold = reversedThreshold;
	}
	public String toString(){
		StringBuffer data=new StringBuffer();
		data.append("model AUC=");
		for (double auc : modelAUC) {
			data.append(auc);
			data.append(',');
		}
		data.append("@focus Area Ratio=");
		for (double d : focosAreaRatio) {
			data.append(d);
			data.append(',');
		}
		data.append("\r\n"+" policySplit="+policySplit+" targetYearSplit="+targetYearSplit+
				" evalYearSplit="+evalYearSplit);
		data.append("\r\n threshold=");
		for (double d : thresholds) {
			data.append(d);
			data.append(',');			
		}
		data.append("\r\n @percentile=");
		for (double d : percentiles) {
			data.append(d);
			data.append(',');			
		}
		data.append("\r\n modelYearsplit="+modelYearSplit+" modelFileName="+modelFileName);
		data.append("\r\n reversedthreshold="+reversedThreshold+"@reversedPercentile="+reversedPercent);
		data.append("\r\n reversedModelYearsplit="+reversedModelYearSplit+" reversedModelFileName="+reversedModelFileName);
		return data.toString();
	}
	
	
	public double getReversedPercent() {
		return reversedPercent;
	}
	public void setReversedPercent(double reversedStartPercent) {
		this.reversedPercent = reversedStartPercent;
	}
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
	
	public double getDefaultThreshold() {
		return thresholds[defaultThresholdIndex];
	}

	public double getDefaultPercentile() {
		return percentiles[defaultThresholdIndex];
	}
	public double[] getThresholds() {
		return thresholds;
	}
	
	public void setThresholds(double[] thresholds) {
		this.thresholds = thresholds;
	}
	public double[] getPercentiles() {
		return percentiles;
	}
	public void setPercentiles(double[] percentiles) {
		this.percentiles = percentiles;
	}
	public int getDefaultThresholdIndex() {
		return defaultThresholdIndex;
	}
	public void setDefaultThresholdIndex(int defaultThresholdIndex) {
		this.defaultThresholdIndex = defaultThresholdIndex;
	}
	


	
}

