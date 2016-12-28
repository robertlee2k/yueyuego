package yueyueGo;

import java.io.IOException;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;
import yueyueGo.databeans.DataInstances;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.utility.FileUtility;

public class ModelStore {
	public static final String TXT_EXTENSION = ".txt";
	public static final String MODEL_FILE_EXTENSION = ".mdl";
	public static final String THRESHOLD_EXTENSION = ".eval";
	
	public static final int SEPERATE_MODEL_AND_EVAL=1; // 回测时按yearsplit和policysplit分割使用model和eval文件， 这是细分的做法
	public static final int YEAR_SHARED_MODEL=2; //回测时按年共享模型， eval文件则根据yearsplit自行分割。
	public static final int HALF_YEAR_SHARED_MODEL=3; //回测时按半年共享模型， eval文件则根据yearsplit自行分割。
	public static final int YEAR_SHARED_MODEL_AND_EVAL=4; //回测时按年共享模型和评估文件， 这个最不常见
	public static final int QUARTER_YEAR_SHARED_MODEL=5; //回测时按季度共享模型， eval文件则根据yearsplit自行分割。

	protected String m_modelFileName;
	protected String m_evalFileName;
	protected Classifier m_model;
	protected GeneralInstances m_modelFormat;

	

	public ModelStore(String model_filename,String eval_filename) {
		this.m_modelFileName = model_filename;
		this.m_evalFileName=eval_filename;
	}

	//回测时调用的，设置model文件和eval文件名称
	public  ModelStore(String yearMonthSplit,String policySplit,String workFileFullPrefix, String classifierName,int modelEvalFileShareMode){//BaseClassifier classifier) {
		
		String modelFile=null;
		String evalFile=null;
		String convertedYear=null;
		switch (modelEvalFileShareMode){//classifier.m_modelEvalFileShareMode) {
		case SEPERATE_MODEL_AND_EVAL:
			modelFile=concatModeFilenameString(yearMonthSplit, policySplit, workFileFullPrefix, classifierName);
			evalFile=modelFile+ModelStore.THRESHOLD_EXTENSION;
			break;
		case YEAR_SHARED_MODEL:	
			//评估文件按yearsplit和policySplit切割
			evalFile=concatModeFilenameString(yearMonthSplit, policySplit, workFileFullPrefix, classifierName)+ModelStore.THRESHOLD_EXTENSION;
			//模型文件按年处理
			if (yearMonthSplit.length()==6){
				convertedYear=yearMonthSplit.substring(0,4);
			}else{
				convertedYear=yearMonthSplit;
			}
			modelFile=concatModeFilenameString(convertedYear, policySplit, workFileFullPrefix, classifierName);
			break;
		case QUARTER_YEAR_SHARED_MODEL:
			//评估文件按yearsplit和policySplit切割
			evalFile=concatModeFilenameString(yearMonthSplit, policySplit, workFileFullPrefix, classifierName)+ModelStore.THRESHOLD_EXTENSION;;
			String quarterString="";
			//模型文件按季度建设模型，提高准确度
			if (yearMonthSplit.length()==6){
				//有月份时按季度获取模型
				convertedYear=yearMonthSplit.substring(0,4);
				int inputQuarter=(Integer.parseInt(yearMonthSplit.substring(4,6))-1)/3; //将月份按季度转化为0、1、2、3四个数字
				switch (inputQuarter){
				case 0: //第一季度用年的模型（这是历史沿革习惯）
					quarterString="";
					break;
				case 3://第四季度
					quarterString="10";
					break;
				default: //第二第三季度补0
					quarterString="0"+(inputQuarter*3+1);
					break;
				}
			}else{
				convertedYear=yearMonthSplit;
			}
			modelFile=concatModeFilenameString(convertedYear+quarterString, policySplit, workFileFullPrefix, classifierName);

			break;
		case HALF_YEAR_SHARED_MODEL:	
			//评估文件按yearsplit和policySplit切割
			evalFile=concatModeFilenameString(yearMonthSplit, policySplit, workFileFullPrefix, classifierName)+ModelStore.THRESHOLD_EXTENSION;;
			String halfYearString=""; //缺省上半年是直接用年份的模型，比如2010
			//模型文件按半年建设模型，提高准确度
			if (yearMonthSplit.length()==6){
				convertedYear=yearMonthSplit.substring(0,4);
				//为下半年增加一个模型，提高准确度
				int inputMonth=Integer.parseInt(yearMonthSplit.substring(4,6));
				if (inputMonth>=7){
					halfYearString="07";
				} 
			}else{
				convertedYear=yearMonthSplit;
			}
			modelFile=concatModeFilenameString(convertedYear+halfYearString, policySplit, workFileFullPrefix, classifierName);

			break;
		
		case YEAR_SHARED_MODEL_AND_EVAL:	
			//模型文件按年处理,评估文件也相同
			if (yearMonthSplit.length()==6){
				convertedYear=yearMonthSplit.substring(0,4);
			}else{
				convertedYear=yearMonthSplit;
			}			
			modelFile=concatModeFilenameString(convertedYear, policySplit, workFileFullPrefix, classifierName);
			evalFile=modelFile+ModelStore.THRESHOLD_EXTENSION;
			break;
		
		default:
			throw new RuntimeException("undefined m_modelEvalFileShareMode ");
		}
		
		m_modelFileName=modelFile;
		m_evalFileName=evalFile;
	}

	public static String concatModeFilenameString(String yearSplit,String policySplit, String workFileFullPrefix, String classifierName){//BaseClassifier classifier) {
		return workFileFullPrefix +"-"+classifierName+ "-" + yearSplit + BaseClassifier.MA_PREFIX + policySplit;
	}

	public String getEvalFileName() {
		return m_evalFileName;
	}

	public void setEvalFileName(String evalFileName) {
		this.m_evalFileName = evalFileName;
	}

	
	private String getModelFileName() {
		return m_modelFileName;
	}
	
	public void setModel(Classifier model) {
		this.m_model = model;
	}

	public void setModelFormat(GeneralInstances modelFormat) {
		this.m_modelFormat = modelFormat;
	}
	public GeneralInstances getModelFormat() {
		return m_modelFormat;
	}
	
	
	public void saveModelToFiles()	throws Exception {

	    String modelFileName=this.getModelFileName();
		try{
			FileUtility.write(modelFileName+ModelStore.TXT_EXTENSION, m_model.toString(), "utf-8");
			Vector<Object> v = new Vector<Object>();
			v.add(m_model);
			v.add(m_modelFormat);
			SerializationHelper.write(modelFileName+ModelStore.MODEL_FILE_EXTENSION, v);
			//		SerializationHelper.write(modelFileName+WEKA_MODEL_EXTENSION, model);
			System.out.println("models saved to :"+ modelFileName);
		} catch(IOException e){
			System.err.println("error when saving: "+modelFileName);
			throw e;
		}
	}

	public Classifier loadModelFromFile() throws Exception{
		String modelFileName=this.getModelFileName()+ ModelStore.MODEL_FILE_EXTENSION;
		try{
			@SuppressWarnings("unchecked")
			Vector<Object> v = (Vector<Object>) SerializationHelper.read(modelFileName);
			Classifier model = (Classifier) v.get(0);
			
			Object savedHeaderObject=v.get(1);
			GeneralInstances header =null;
			if (savedHeaderObject instanceof DataInstances){
				header=(DataInstances)savedHeaderObject;
			}else{ //TODO  interface化之前的旧模型的legacy数据强转为Instances
				header=new DataInstances((Instances)savedHeaderObject);
			}
			System.out.println("Classifier Model and Format Loaded from: "+ modelFileName);
			m_model=model;
			m_modelFormat=header;
			return m_model;
		} catch(IOException e){
			System.err.println("error when loading: "+modelFileName);
			throw e;
		}
	}

}
