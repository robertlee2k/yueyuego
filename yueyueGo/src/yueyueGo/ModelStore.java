package yueyueGo;

import java.io.IOException;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.core.SerializationHelper;
import yueyueGo.databeans.DataInstances;
import yueyueGo.databeans.GeneralDataTag;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.utility.FileUtility;
import yueyueGo.utility.ThresholdData;

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
	
	protected String m_modelYearSplit;
	protected String m_evalYearSplit;
	protected String m_targetYearSplit;
	
	public String getTargetYearSplit() {
		return m_targetYearSplit;
	}

	public String getModelYearSplit() {
		return m_modelYearSplit;
	}

	public String getEvalYearSplit() {
		return m_evalYearSplit;
	}


	//预测时调用的
	public ModelStore(String model_filename,String eval_filename) {
		this.m_modelFileName = model_filename;
		this.m_evalFileName=eval_filename;

		//预测时不校验，将这些都设为设为null
		this.m_evalYearSplit=null;
		this.m_modelYearSplit=null;
		this.m_targetYearSplit=null;
	}

	//回测时调用的，设置model文件和eval文件名称
	public  ModelStore(String targetYearSplit,String policySplit,BaseClassifier clModel){
		String workFileFullPrefix=clModel.WORK_PATH+clModel.WORK_FILE_PREFIX;
		String classifierName=clModel.classifierName;
		int modelFileShareMode=clModel.m_modelFileShareMode;
		int evalDataSplitMode=clModel.m_evalDataSplitMode;
		
		//根据modelDataSplitMode推算出评估数据的起始区间 （目前主要有三种： 最近6个月、9个月、12个月）
		String evalYearSplit=caculateEvalYearSplit(targetYearSplit,evalDataSplitMode);
		
		//历史数据切掉评估数据后再去getModelYearSplit看选多少作为模型构建数据（因为不是每月都有模型,要根据modelEvalFileShareMode来定）
		String modelYearSplit=caculateModelYearSplit(evalYearSplit,modelFileShareMode);

		this.m_targetYearSplit=targetYearSplit; //记录下用于评测的目标月份，以便日后校验
		this.m_modelYearSplit=modelYearSplit;//记录下用于构建的模型月份，以便校验输入的数据
		this.m_evalYearSplit=evalYearSplit;//记录下用于评估的月份，以便校验输入的数据
		
		//根据历史习惯， 如果模型是第一个月时将其文件名变换为年的形式（如应该把200801变为2008）
		if (modelYearSplit.length()==6){
			int inputMonth=Integer.parseInt(modelYearSplit.substring(4,6)); 
			if (inputMonth==1){
				modelYearSplit=modelYearSplit.substring(0,4);
			}
		}


		m_modelFileName=concatModeFilenameString(modelYearSplit, policySplit, workFileFullPrefix, classifierName);
		m_evalFileName=concatModeFilenameString(evalYearSplit, policySplit, workFileFullPrefix, classifierName)+ModelStore.THRESHOLD_EXTENSION;

	}

	/*
	 * 获取用于评估阀值的yearSplit（目前主要有三种： 最近6个月、9个月、12个月）
	 * 全量历史数据会分解为模型构建数据及其评估数据
	 * 比如，若是一年区间， 则取最近的1年间数据作为评估数据用。之前的数据再去getModelYearSplit看选多少作为模型构建数据（因为不是每月都有模型）
	 * 如果是2010.mdl，则取2009年01月之前的数据build，2009当年数据做评估用
	 */
	public static String caculateEvalYearSplit(String targetYearSplit,int evalDataSplitMode){
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
		System.out.println("模型构建数据切分日期="+modelYearSplit+"（评估数据切分日期="+evalYearSplit+" 模型共享模式="+modelFileShareMode+"）");
		return modelYearSplit;
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
	
	/*
	 * 校验评估阶段准备用于Evaluating的data是否符合要求
	 * 返回null的时候表示符合要求
	 */	
	public String validateEvalData(GeneralDataTag dataTag){
		String msg="";
		if (dataTag.getDataType()!=GeneralDataTag.EVALUATION_DATA){
			msg+=" incoming dataType is not evaluation data! ";
		}
		if (this.m_evalYearSplit.equals(dataTag.getFromPeriod())==false){
			msg+=" incoming data FromPeriod="+dataTag.getFromPeriod()+" while expected m_m_evalYearSplit="+this.m_evalYearSplit;
		}
		
		if (this.m_targetYearSplit.equals(dataTag.getToPeriod())==false){
			msg+=" incoming data toPeriod="+dataTag.getToPeriod()+" while expected m_targetYearSplit="+this.m_targetYearSplit;
		}
		if ("".equals(msg))
			return null;
		else
			return msg;
	}
	
	/*
	 * 校验回测阶段准备用于Testing的data是否符合要求
	 * 返回null的时候表示符合要求
	 */	
	public String validateTestingData(GeneralDataTag dataTag){
		String msg="";
		if (dataTag.getDataType()!=GeneralDataTag.TESTING_DATA){
			msg+=" incoming dataType is not testing data! ";
		}
		if (this.m_targetYearSplit.equals(dataTag.getToPeriod())==false){
			msg+=" incoming data toPeriod="+dataTag.getToPeriod()+" while expected targetYearSplit="+this.m_targetYearSplit;
		}
		if ("".equals(msg))
			return null;
		else
			return msg;
	}
	
	/*
	 * 校验从文件中读取的Threshold值是否可以用于当前的模型
	 */
	public String validateThresholdData(ThresholdData thresholdData){
		String msg="";
		if (m_targetYearSplit==null && m_evalYearSplit==null && m_modelYearSplit==null ){ //每日预测时跳过

		}else {
			if (m_targetYearSplit.equals(thresholdData.getTargetYearSplit())==false){
				msg+=" {ERROR}target m_targetYearSplit="+m_targetYearSplit+" while targetYearSplit in thresholdData is "+thresholdData.getTargetYearSplit();
			}
			if (m_evalYearSplit.equals(thresholdData.getEvalYearSplit())==false){
				msg+=" {ERROR}target m_evalYearSplit="+m_evalYearSplit+" while evalYearSplit in thresholdData is "+thresholdData.getEvalYearSplit();
			}
			if (m_modelYearSplit.equals(thresholdData.getModelYearSplit())==false){
				msg+=" {ERROR}target m_modelYearSplit="+m_modelYearSplit+" while modelYearSplit in thresholdData is "+thresholdData.getModelYearSplit();
			}
		}
		if ("".equals(msg))
			return null;
		else
			return msg;
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
			//写入构建model的数据时间，供日后校验
			v.add(m_modelYearSplit);
			SerializationHelper.write(modelFileName+ModelStore.MODEL_FILE_EXTENSION, v);
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
//			if (savedHeaderObject instanceof DataInstances){
			header=(DataInstances)savedHeaderObject;
//			}else{ // interface化之前的旧模型的legacy数据强转为Instances
//				header=new DataInstances((Instances)savedHeaderObject);
//			}
			System.out.println("Classifier Model and Format Loaded from: "+ modelFileName);
			m_model=model;
			m_modelFormat=header;
			if ( m_modelYearSplit!=null ){ //每日预测时跳过
				//如果model文件里存有构建model的数据时间段，则校验之
				String savedModelYearSplit=null;
				try {
					savedModelYearSplit=(String)v.get(2);
				}catch (Exception ee){
					//TODO 因为201701之前的构建模型里没有存入这个数据，对旧数据兼容的目的来说，可以忽略
					//ignore it;
				}
				if (savedModelYearSplit!=null){ //如果有数据，则校验之
					if (savedModelYearSplit.equals(this.m_modelYearSplit)==false){
						throw new Exception(" savedModelYearSplit in model file="+savedModelYearSplit+" while m_modelYearSplit="+m_modelYearSplit);
					}else{
						System.out.println(" modelYearSplit verified for loaded model ");
					}
				}
			}
			return m_model;
		} catch(IOException e){
			System.err.println("error when loading: "+modelFileName);
			throw e;
		}
	}

}
