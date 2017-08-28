package yueyueGo;

import java.io.IOException;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.core.SerializationHelper;
import yueyueGo.databeans.DataInstances;
import yueyueGo.databeans.GeneralDataTag;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.utility.FileUtility;

public class ModelStore {
	public static final String TXT_EXTENSION = ".txt";
	public static final String MODEL_FILE_EXTENSION = ".mdl";
	//模型文件共享的模式
	public static final int MONTHLY_MODEL=1; // 回测时按yearsplit和policysplit分割使用model和eval文件， 这是细分的做法
	public static final int YEAR_SHARED_MODEL=12; //回测时按年共享模型， eval文件则根据yearsplit自行分割。
	public static final int HALF_YEAR_SHARED_MODEL=6; //回测时按半年共享模型， eval文件则根据yearsplit自行分割。
	public static final int QUARTER_SHARED_MODEL=3; //回测时按季度共享模型， eval文件则根据yearsplit自行分割。

	protected Classifier m_model;
	protected GeneralInstances m_modelFormat;
	protected String m_modelFileName;

	
	protected String m_modelYearSplit;          //构建模型数据的结束年月


	public void setModelFileName(String m_modelFileName) {
		this.m_modelFileName = m_modelFileName;
	}

	public void setModelYearSplit(String m_modelYearSplit) {
		this.m_modelYearSplit = m_modelYearSplit;
	}

	public String getModelYearSplit() {
		return m_modelYearSplit;
	}

	public String getModelFileName() {
		return m_modelFileName;
	}

	//统一常量
	public static final String MA_PREFIX = " MA ";
	


	//从已有模型文件中加载时调用的
	public ModelStore(String model_filename,String modelYearSplit) {
		this.m_modelFileName = model_filename;
		this.m_modelYearSplit=modelYearSplit; 
	}

	//回测时调用的，新建模型时设置model文件名称
	public  ModelStore(String targetYearSplit,String policySplit,String modelFilepathPrefix, BaseClassifier clModel){
		String workFileFullPrefix=modelFilepathPrefix;
		String classifierName=clModel.classifierName;
		int modelFileShareMode=clModel.m_modelFileShareMode;
		int evalDataSplitMode=clModel.m_evalDataSplitMode;
		
		//根据modelDataSplitMode推算出评估数据的起始区间 （目前主要有三种： 最近6个月、9个月、12个月）
		String evalYearSplit=EvaluationStore.caculateEvalYearSplit(targetYearSplit,evalDataSplitMode);
		
		//历史数据切掉评估数据后再去getModelYearSplit看选多少作为模型构建数据（因为不是每月都有模型,要根据modelEvalFileShareMode来定）
		String modelYearSplit=caculateModelYearSplit(evalYearSplit,modelFileShareMode);

		this.m_modelYearSplit=modelYearSplit;//记录下用于构建的模型月份，以便校验输入的数据
		
//		modelYearSplit = legacyModelName(modelYearSplit);

		m_modelFileName=concatModeFilenameString(modelYearSplit, policySplit, workFileFullPrefix, classifierName);

	}

	@Deprecated
	/**
	 * @param modelYearSplit
	 * @return
	 * @throws NumberFormatException
	 */
	public static String legacyModelName(String modelYearSplit) throws NumberFormatException {
		//根据历史习惯， 如果模型是第一个月时将其文件名变换为年的形式（如应该把200801变为2008）
		if (modelYearSplit.length()==6){
			int inputMonth=Integer.parseInt(modelYearSplit.substring(4,6)); 
			if (inputMonth==1){
				modelYearSplit=modelYearSplit.substring(0,4);
			}
		}
		return modelYearSplit;
	}

	/*
	 * 历史数据切掉评估数据后再调用该函数看选多少作为模型构建数据
	 * （因为为了回测速度， 不是每月都有模型,要根据模型的modelEvalFileShareMode来定）
	 */
	public static String caculateModelYearSplit(String evalYearSplit,int modelFileShareMode){
		String modelYearSplit=null;
		switch (modelFileShareMode){//classifier.m_modelEvalFileShareMode) {
		case MONTHLY_MODEL:
			modelYearSplit=evalYearSplit;
			break;
		case YEAR_SHARED_MODEL:	//评估文件按yearsplit和policySplit切割
			//模型文件按年处理
			if (evalYearSplit.length()==6){
				modelYearSplit=evalYearSplit.substring(0,4)+"01";
			}else{
				modelYearSplit=evalYearSplit;
			}
			break;
		case QUARTER_SHARED_MODEL:
			//评估文件按yearsplit和policySplit切割
			String quarterString="";
			//模型文件按季度建设模型，提高准确度
			if (evalYearSplit.length()==6){
				//有月份时按季度获取模型
				modelYearSplit=evalYearSplit.substring(0,4);
				int inputQuarter=(Integer.parseInt(evalYearSplit.substring(4,6))-1)/3; //将月份按季度转化为0、1、2、3四个数字
				switch (inputQuarter){
				case 3://第四季度
					quarterString="10";
					break;
				default: //第一第二第三季度补0
					quarterString="0"+(inputQuarter*3+1);
					break;
				}
			}else{
				modelYearSplit=evalYearSplit;
			}
			modelYearSplit=modelYearSplit+quarterString;
			break;
		case HALF_YEAR_SHARED_MODEL:	
			String halfYearString=""; //缺省上半年是直接用年份的模型，比如2010
			//模型文件按半年建设模型，提高准确度
			if ( evalYearSplit.length()==6){
				modelYearSplit= evalYearSplit.substring(0,4);
				int inputMonth=Integer.parseInt( evalYearSplit.substring(4,6));
				if (inputMonth>=7){
					halfYearString="07";
				} else{
					halfYearString="01";
				}
			}else{
				modelYearSplit= evalYearSplit;
			}
			modelYearSplit=modelYearSplit+halfYearString;
			break;
		default:
			throw new RuntimeException("undefined m_modelEvalFileShareMode ");
		}
		
		return modelYearSplit;
	}	
	
	/*
	 * 取modelYearEndSplit的前noOfYears的yearmonth值
	 * 如201203的前5年为200703
	 */
	public static String modelDataStartYearSplit(String modelYearEndSplit, int noOfYears){
		int yearMonth=Integer.valueOf(modelYearEndSplit).intValue();
		yearMonth-=noOfYears*100;
		return String.valueOf(yearMonth);
	}

	/*
	 * 校验构建模型阶段准备用于Training的data是否符合要求
	 * 返回null的时候表示符合要求
	 */
	public String validateTrainingData(GeneralDataTag dataTag){
		String msg="";
		if (dataTag.getDataType()!=GeneralDataTag.TRAINING_DATA){
			msg+=" incoming dataType is not training data! ";
		}
		if (this.m_modelYearSplit.equals(dataTag.getToPeriod())==false){
			msg+=" incoming data toPeriod="+dataTag.getToPeriod()+" while expected modelYearSplit="+this.m_modelYearSplit;
		}
		if ("".equals(msg))
			return null;
		else
			return msg;
	}
	
	
	public static String concatModeFilenameString(String yearSplit,String policySplit, String workFileFullPrefix, String classifierName){//BaseClassifier classifier) {
		return workFileFullPrefix +"-"+classifierName+ "-" + yearSplit + ModelStore.MA_PREFIX + policySplit;
	}


	
	public void setModel(Classifier model) {
		this.m_model = model;
	}
	
	public Classifier getModel() {
		return this.m_model;
	}
	
	public void setModelFormat(GeneralInstances modelFormat) {
		this.m_modelFormat = modelFormat;
	}
	public GeneralInstances getModelFormat() {
		return m_modelFormat;
	}
	
	
	//保存modelFile
	public void saveModelToFiles()	throws Exception {

	    String modelFileName=this.m_modelFileName;
		try{
			FileUtility.write(modelFileName+ModelStore.TXT_EXTENSION, m_model.toString(), "utf-8");
			Vector<Object> v = new Vector<Object>();
			v.add(m_model);
			v.add(m_modelFormat);
			//写入构建model的数据时间，供日后校验
			v.add(m_modelYearSplit);
			SerializationHelper.write(modelFileName+ModelStore.MODEL_FILE_EXTENSION, v);
//			System.out.println("models saved to :"+ modelFileName);
		} catch(IOException e){
			System.err.println("error when saving: "+modelFileName);
			throw e;
		}
	}

	public void loadModelFromFile(String a_targetYearSplit) throws Exception{
		

		String modelFileName=this.m_modelFileName+ ModelStore.MODEL_FILE_EXTENSION;
		try{
			@SuppressWarnings("unchecked")
			Vector<Object> v = (Vector<Object>) SerializationHelper.read(modelFileName);
			Classifier model = (Classifier) v.get(0);
			
			Object savedHeaderObject=v.get(1);
			GeneralInstances header =null;
			header=(DataInstances)savedHeaderObject;
//			System.out.println("Classifier Model and Format Loaded from: "+ modelFileName);
			this.m_model=model;
			this.m_modelFormat=header;
			String savedModelYearSplit=null;
			savedModelYearSplit=(String)v.get(2);
			
			if ( a_targetYearSplit!=null ){ //每日预测时跳过
				//校验model文件里的构建model的数据时间段
				int savedYear=Integer.valueOf(savedModelYearSplit).intValue();
				int targetYear=Integer.valueOf(a_targetYearSplit).intValue();
				//比较不晚于目标年份即可
				if (savedYear>targetYear){
					throw new Exception(" savedModelYearSplit in model file="+savedModelYearSplit+" while target_year="+a_targetYearSplit);
				}
			}
			//校验model文件里的构建model的数据时间段
			if (this.m_modelYearSplit!=null){ //评估时跳过
				if (this.m_modelYearSplit.equals(savedModelYearSplit)==false){
					throw new Exception(" savedModelYearSplit in model file="+savedModelYearSplit+" not equal to m_modelYearSplit="+m_modelYearSplit);
				}
			}else{ //如果当前modelYear为空则直接设为从文件里读取的值
				this.m_modelYearSplit=savedModelYearSplit;
			}

		} catch(IOException e){
			System.err.println("error when loading: "+modelFileName);
			throw e;
		}
	}

}
