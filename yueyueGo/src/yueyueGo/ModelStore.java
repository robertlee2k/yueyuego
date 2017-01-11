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
	
	//模型文件共享的模式
	public static final int MONTHLY_MODEL=1; // 回测时按yearsplit和policysplit分割使用model和eval文件， 这是细分的做法
	public static final int YEAR_SHARED_MODEL=12; //回测时按年共享模型， eval文件则根据yearsplit自行分割。
	public static final int HALF_YEAR_SHARED_MODEL=6; //回测时按半年共享模型， eval文件则根据yearsplit自行分割。
	public static final int QUARTER_SHARED_MODEL=3; //回测时按季度共享模型， eval文件则根据yearsplit自行分割。

	protected Classifier m_model;
	protected GeneralInstances m_modelFormat;
	//切分构建模型和评估数据的模式常量定义
	public static final int NO_SEPERATE_DATA_FOR_EVAL=0; //不需要评估数据（这是legacy的做法，目前不常用）
	public static final int USE_YEAR_DATA_FOR_EVAL=12; //使用倒推一年的数据作为模型评估数据，之前用于的构建模型（缺省值）
	public static final int USE_HALF_YEAR_DATA_FOR_EVAL=6;//使用倒推半年的数据作为模型评估数据，之前用于的构建模型
	public static final int USE_NINE_MONTHS_DATA_FOR_EVAL=9;//使用倒推半年的数据作为模型评估数据，之前用于的构建模型

	protected String m_modelFileName;
	protected String m_evalFileName;
	protected String m_targetYearSplit;
	
	public String getTargetYearSplit() {
		return m_targetYearSplit;
	}
	

	public ModelStore(String targetYearSplit,String model_filename,String eval_filename) {
		this.m_targetYearSplit=targetYearSplit; //记录下用于评测的目标月份，以便日后校验
		this.m_modelFileName = model_filename;
		this.m_evalFileName=eval_filename;
	}

	//回测时调用的，设置model文件和eval文件名称
	public  ModelStore(String targetYearSplit,String policySplit,BaseClassifier clModel){
		String workFileFullPrefix=clModel.WORK_PATH+clModel.WORK_FILE_PREFIX;
		String classifierName=clModel.classifierName;
		int modelFileShareMode=clModel.m_modelFileShareMode;
		int evalDataSplitMode=clModel.m_evalDataSplitMode;
		
		//根据modelDataSplitMode推算出评估数据的起始区间 （目前主要有三种： 最近6个月、9个月、12个月）
		String evalYearSplit=getEvalYearSplit(targetYearSplit,evalDataSplitMode);
		
		//历史数据切掉评估数据后再去getModelYearSplit看选多少作为模型构建数据（因为不是每月都有模型,要根据modelEvalFileShareMode来定）
		String modelYearSplit=getEvalYearSplit(evalYearSplit,modelFileShareMode);
		
		//根据历史习惯， 如果模型是第一个月时将其变换为年的形式（如应该把200801变为2008）
		if (modelYearSplit.length()==6){
			int inputMonth=Integer.parseInt(modelYearSplit.substring(4,6)); 
			if (inputMonth==1){
				modelYearSplit=modelYearSplit.substring(0,4);
			}
		}

		this.m_targetYearSplit=targetYearSplit; //记录下用于评测的目标月份，以便日后校验
		m_modelFileName=concatModeFilenameString(modelYearSplit, policySplit, workFileFullPrefix, classifierName);
		m_evalFileName=concatModeFilenameString(evalYearSplit, policySplit, workFileFullPrefix, classifierName)+ModelStore.THRESHOLD_EXTENSION;

	}

	/*
	 * 获取用于评估阀值的yearSplit（目前主要有三种： 最近6个月、9个月、12个月）
	 * 全量历史数据会分解为模型构建数据及其评估数据
	 * 比如，若是一年区间， 则取最近的1年间数据作为评估数据用。之前的数据再去getModelYearSplit看选多少作为模型构建数据（因为不是每月都有模型）
	 * 如果是2010.mdl，则取2009年01月之前的数据build，2009当年数据做评估用
	 */
	public static String getEvalYearSplit(String targetYearSplit,int evalDataSplitMode){
		//找到回测创建评估预测时应该使用modelStore对象（主要为获取model文件和eval文件名称）-- 评估时创建的mdl并不是当前年份的，而是前推一年的
		String evalYearSplit=null;
		switch (evalDataSplitMode) {
		case NO_SEPERATE_DATA_FOR_EVAL: //使用全量数据构建模型（不常用）
			evalYearSplit=targetYearSplit; 
			break;
		case USE_YEAR_DATA_FOR_EVAL:
		case USE_HALF_YEAR_DATA_FOR_EVAL:
		case USE_NINE_MONTHS_DATA_FOR_EVAL:
			evalYearSplit=getNMonthsForYearSplit(evalDataSplitMode,targetYearSplit);			
			break;
		}
		System.out.println("目标日期="+targetYearSplit+" 评估数据切分日期="+evalYearSplit+"（评估数据切分模式="+evalDataSplitMode+"）");
		return evalYearSplit;
	}
	
	//	当前周期前推N个月的分隔线，比如 如果N=9是201003 则返回200909
	private static String getNMonthsForYearSplit(int nMonths,String yearSplit){
		int limit=2007; //回测模型的起始点， 在这之前无数据
		int lastPeriod=0;
		if (yearSplit.length()==4){ //最后一位-1 （2010-1=2009）再拼接一个12-nMonth+1
			lastPeriod=Integer.valueOf(yearSplit).intValue();
			lastPeriod=lastPeriod-1;
			if (lastPeriod<limit) {
				lastPeriod=limit;
			}
			lastPeriod=lastPeriod*100+12-nMonths+1;
		}else {//最后两位数（n）大于nMonths的话减nMonths，小于等于的话向年借位12
			int inputYear=Integer.parseInt(yearSplit.substring(0,4)); //输入的年份
			int inputMonth=Integer.parseInt(yearSplit.substring(4,6)); //输入的月份
			if (inputMonth>nMonths){
				inputMonth=inputMonth-nMonths;
			}else{
				inputMonth=12+inputMonth-nMonths;
				inputYear=inputYear-1;
			}
			lastPeriod=inputYear*100+inputMonth;
			if (lastPeriod<limit*100+1){ 
				lastPeriod=limit*100+1;
			}
		}
		return String.valueOf(lastPeriod);
	}
	
	/*
	 * 历史数据切掉评估数据后再调用该函数看选多少作为模型构建数据
	 * （因为为了回测速度， 不是每月都有模型,要根据模型的modelEvalFileShareMode来定）
	 */
	public static String getModelYearSplit(String evalYearSplit,int modelFileShareMode){
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
		System.out.println("模型构建数据切分日期="+modelYearSplit+"（评估数据切分日期="+evalYearSplit+" 模型共享模式="+modelFileShareMode+"）");
		return modelYearSplit;
	}	
	
//	//	当前周期前推一年的年分隔线，比如 如果是2010XX 则返回2009年XX月（这是为了取不在trainingData里的evalData）
//	private static String getLastYearSplit(String yearSplit){
//		int lastPeriod=0;
//		int limit=2007; //回测模型的起始点， 在这之前无数据
//		lastPeriod=Integer.valueOf(yearSplit).intValue();
//		if (yearSplit.length()==4){ //最后一位-1 （2010-1=2009）
//			lastPeriod=lastPeriod-1;
//			if (lastPeriod<limit) 
//				lastPeriod=limit;
//		}else {//最后三位-1 （201001-100=200901）
//			lastPeriod=lastPeriod-100;
//			if (lastPeriod<limit*100+1) 
//				lastPeriod=limit*100+1;
//		}
//		return String.valueOf(lastPeriod);
//	}
	

	
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
