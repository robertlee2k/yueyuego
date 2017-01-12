package yueyueGo.utility;


public class PredictModelData {

	private String identify;
	private String modelFileName;
	private String evalFileName;
//	private String modelSplitYear;
//	private String evalSplitYear;
	private int modelFormatType;

	
	
	public PredictModelData() {

	}
	
	//这两个方法的返回方式不一样
	public String getModelFileName() {
		return modelFileName;
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

	public void setModelFileName(String modelFileName) {
		this.modelFileName = modelFileName;
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
