package yueyueGo;

import java.io.IOException;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;
import yueyueGo.utility.FileUtility;

public class ModelStore {
	public static final String TXT_EXTENSION = ".txt";
	public static final String MODEL_FILE_EXTENSION = ".mdl";
	
	public static final int SEPERATE_MODEL_AND_EVAL=1; // 回测时按yearsplit和policysplit分割使用model和eval文件， 这是最普遍的做法
	public static final int YEAR_SHARED_MODEL=2; //回测时按年共享模型， eval文件则根据yearsplit自行分割。
	public static final int HALF_YEAR_SHARED_MODEL=3; //为特殊年份（2016年）半年处增加一个模型评估文件（这个比较不常见）
	public static final int YEAR_SHARED_MODEL_AND_EVAL=4; //回测时按年共享模型和评估文件， 这个最不常见


	protected String m_modelFileName;
	protected String m_evalFileName;
	protected Classifier m_model;
	protected Instances m_modelFormat;
	

	public ModelStore(String model_filename,String eval_filename) {
		super();
		this.m_modelFileName = model_filename;
		this.m_evalFileName=eval_filename;
	}

	//回测时调用的，设置model文件和eval文件名称
	public  ModelStore(String yearSplit,String policySplit,BaseClassifier classifier) {
		
		String modelFile=null;
		String evalFile=null;
		switch (classifier.m_modelEvalFileShareMode) {
		case SEPERATE_MODEL_AND_EVAL:
			modelFile=classifier.WORK_PATH+classifier.WORK_FILE_PREFIX +"-"+classifier.classifierName+ "-" + yearSplit + BaseClassifier.MA_PREFIX + policySplit;
			evalFile=modelFile+BaseClassifier.THRESHOLD_EXTENSION;
			break;
		case YEAR_SHARED_MODEL:	
			//评估文件按yearsplit和policySplit切割
			evalFile=classifier.WORK_PATH+classifier.WORK_FILE_PREFIX +"-"+classifier.classifierName+ "-" + yearSplit + BaseClassifier.MA_PREFIX + policySplit+BaseClassifier.THRESHOLD_EXTENSION;
			//模型文件按年处理
			int inputYear=Integer.parseInt(yearSplit.substring(0,4));
			modelFile=classifier.WORK_PATH+classifier.WORK_FILE_PREFIX +"-"+classifier.classifierName+ "-" + inputYear + BaseClassifier.MA_PREFIX + policySplit;
			break;
		case HALF_YEAR_SHARED_MODEL:	
			//评估文件按yearsplit和policySplit切割
			evalFile=classifier.WORK_PATH+classifier.WORK_FILE_PREFIX +"-"+classifier.classifierName+ "-" + yearSplit + BaseClassifier.MA_PREFIX + policySplit+BaseClassifier.THRESHOLD_EXTENSION;
			//模型文件按年处理，为特定年份下半年（2016）增加一个模型，提高准确度
			inputYear=Integer.parseInt(yearSplit.substring(0,4));
			//为特定年份下半年增加一个模型，提高准确度
			String halfYearString="";
			if(yearSplit.length()==6){
				int inputMonth=Integer.parseInt(yearSplit.substring(4,6));
				if ((inputYear==2016) && inputMonth>=6){
					halfYearString="06";
				}
			}
			modelFile=classifier.WORK_PATH+classifier.WORK_FILE_PREFIX +"-"+classifier.classifierName+ "-" + inputYear +halfYearString+ BaseClassifier.MA_PREFIX + policySplit;
			break;
		
		case YEAR_SHARED_MODEL_AND_EVAL:	
			//模型文件按年处理,评估文件也相同
			inputYear=Integer.parseInt(yearSplit.substring(0,4));
			modelFile=classifier.WORK_PATH+classifier.WORK_FILE_PREFIX +"-"+classifier.classifierName+ "-" + inputYear + BaseClassifier.MA_PREFIX + policySplit;
			evalFile=modelFile+BaseClassifier.THRESHOLD_EXTENSION;
			break;
		
		default:
			throw new RuntimeException("undefined m_modelEvalFileShareMode ");
		}
		
		m_modelFileName=modelFile;
		m_evalFileName=evalFile;
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

	public void setModelFormat(Instances modelFormat) {
		this.m_modelFormat = modelFormat;
	}
	public Instances getModelFormat() {
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
			System.out.println("Classifier Model Loaded: "+ modelFileName);
			
			Instances header = (Instances) v.get(1);
			System.out.println("Classifier Model Header Loaded From: "+ modelFileName);
			m_model=model;
			m_modelFormat=header;
			return m_model;
		} catch(IOException e){
			System.err.println("error when loading: "+modelFileName);
			throw e;
		}
	}

}
