package yueyueGo.utility;


public class PredictModelData {

	private String identify;
//	private String modelFileName;
	private String evalFileName;
	
	private String targetYearSplit; //用于校验
	
//	private String modelSplitYear;
//	private String evalSplitYear;
	private int modelFormatType;

	
	
	public String getTargetYearSplit() {
		return targetYearSplit;
	}


	public void setTargetYearSplit(String targetYearSplit) {
		this.targetYearSplit = targetYearSplit;
	}


	public PredictModelData() {

	}
	

	public String getEvalFileName() {
		return evalFileName;
	}
	
	public String getIdentify() {
		return identify;
	}
	public void setIdentify(String identify) {
		this.identify = identify;
	}

	public void setEvalFileName(String evalFileName) {
		this.evalFileName = evalFileName;
	}
	public int getModelFormatType() {
		return modelFormatType;
	}
	public void setModelFormatType(int modelFormatType) {
		this.modelFormatType = modelFormatType;
	}

//	public String getModelSplitYear() {
//		return modelSplitYear;
//	}
//
//	public void setModelSplitYear(String modelSplitYear) {
//		this.modelSplitYear = modelSplitYear;
//	}
//
//	public String getEvalSplitYear() {
//		return evalSplitYear;
//	}
//
//	public void setEvalSplitYear(String evalSplitYear) {
//		this.evalSplitYear = evalSplitYear;
//	}
	
}
