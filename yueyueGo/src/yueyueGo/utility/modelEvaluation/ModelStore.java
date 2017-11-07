package yueyueGo.utility.modelEvaluation;

import java.io.IOException;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.core.SerializationHelper;
import yueyueGo.dataProcessor.BaseInstanceProcessor;
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

	
	//模型文件所用数据的区段长度
	public static final int ONE_YEAR_DATA=1;
	public static final int TWO_YEAR_DATA=2;
	public static final int THREE_YEAR_DATA=3;
	public static final int FOUR_YEAR_DATA=4;
	public static final int FIVE_YEAR_DATA=5;
	
	protected Classifier m_model;
	protected GeneralInstances m_modelFormat;
	protected String m_modelFileName;
	
	protected String m_workFilePath;
	protected String m_classiferName;
	protected String m_policy;
	protected String m_modelYearSplit;          //构建模型数据的结束年月
	protected int m_useNyearData;               //使用多少年的数据 

	public String getModelFileName() {
		return m_modelFileName;
	}

	public String getModelYearSplit() {
		return m_modelYearSplit;
	}


	//统一常量
	public static final String MA_PREFIX = " MA ";
	

	/*
	 * 预测时调用的--从确定的评估文件中获得模型文件名，然后加载
	 * workfilePath= 模型的存储目录（绝对路径）
	 * model_filename= 模型文件名（不含目录名）
	 * modelYearSplit= 模型建立的年月Split （用做校验）
	 */
	public ModelStore(String workfilePath,String model_filename,String modelYearSplit) {
		this.m_workFilePath=workfilePath;
		this.m_modelFileName = model_filename;
		this.m_modelYearSplit=modelYearSplit; 
	}
	
	/*
	 * 回测或评估时调用
	 * 
	 * 回测时新建模型时设置model文件名称
	 * 评估时根据模型文件的数据年份查找到目标模型的文件。
	 */
	public ModelStore(String modelYearSplit,String policySplit, String modelFilePath,String modelFilePrefix, String classifierName,int useNyearData) {
		this.m_workFilePath=modelFilePath;
		this.m_modelYearSplit=modelYearSplit; 
		this.m_classiferName=classifierName;
		this.m_policy=policySplit;
		this.m_useNyearData=useNyearData;
		this.m_modelFileName = this.concatModelFileName(modelFilePrefix);
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
	
	
	public void setWorkFilePath(String m_workFilePath) {
		this.m_workFilePath = m_workFilePath;
	}

	public String getWorkFilePath() {
		return m_workFilePath;
	}

	//保存modelFile
	public void saveModelToFiles()	throws Exception {

	    String modelFileName=this.m_workFilePath+this.m_modelFileName;
		try{
			Vector<Object> v = new Vector<Object>();
			v.add(m_model);
			v.add(m_modelFormat);
			//写入构建model的数据时间，供日后校验
			v.add(m_modelYearSplit);
			SerializationHelper.write(modelFileName+ModelStore.MODEL_FILE_EXTENSION, v);
			FileUtility.write(modelFileName+ModelStore.TXT_EXTENSION, m_model.toString(), "utf-8");			
			System.out.println("models saved to :"+ modelFileName);
		} catch(Exception e){
			System.err.println("error when saving: "+modelFileName);
			throw e;
		}
	}

	private void initModelStoreFromFile(String a_targetYearSplit) throws Exception{
		

		String modelFileName=this.m_workFilePath+this.m_modelFileName+ ModelStore.MODEL_FILE_EXTENSION;
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
			
			if ( "".equals(a_targetYearSplit)==false ){ //每日预测时跳过
				//校验model文件里的构建model的数据时间段
				int savedYear=Integer.valueOf(savedModelYearSplit).intValue();
				int targetYear=Integer.valueOf(a_targetYearSplit).intValue();
				//比较不晚于目标年份即可
				if (savedYear>targetYear){
					throw new Exception(" savedModelYearSplit in model file="+savedModelYearSplit+" while target_year="+a_targetYearSplit);
				}
			}
			
//			if (this.m_modelYearSplit!=null){ //评估时跳过
			
			//校验model文件里的构建model的数据时间段 , 不管是评估还是预测时，这个都不能跳过，必须相等（因为这个值是在EvaluationStore里保存好的）
			if (this.m_modelYearSplit.equals(savedModelYearSplit)==false){
				throw new Exception(" savedModelYearSplit in model file="+savedModelYearSplit+" not equal to m_modelYearSplit="+m_modelYearSplit);
			}
//			}else{ //如果当前modelYear为空则直接设为从文件里读取的值
//				this.m_modelYearSplit=savedModelYearSplit;
//			}

		} catch(IOException e){
			System.err.println("error when loading: "+modelFileName);
			throw e;
		}
	}

	/**
	 * @param targetData 模型处理的目标数据集（做格式校验用）
	 * @param targetYearSplit 模型的目标年份（做校验用）
	 * @return
	 * @throws Exception
	 */
	public Classifier loadModelFromFile(GeneralInstances targetData, String targetYearSplit)
			throws Exception {
		// 从保存的数据文件中加载分类用的model and header，此加载方法内部有对modelYear的校验		
		this.initModelStoreFromFile(targetYearSplit);
		//获取model
		Classifier model =getModel();
		//模型数据的校验会在加载方法内部进行，此处下面仅校验正向模型的格式
		GeneralInstances header =getModelFormat();
	
		//验证数据格式是否一致
		String verify=BaseInstanceProcessor.compareInstancesFormat(targetData, header);
		if (verify!=null){
			throw new Exception("attention! model and testing data structure is not the same. Here is the difference: "+verify);
		}
		return model;
	}

	/*
	 * modelFile的命名规则
	 */
	private String concatModelFileName(String modelFilePrefix) {
		return  modelFilePrefix + "-" + this.m_classiferName + "-" + this.m_modelYearSplit +"(-"+m_useNyearData+")"+ MA_PREFIX + this.m_policy;
	}
	
	/*
	 * 以下为临时工具类
	 */
	
	public String getLegacyModelFileName() {
		return  "trans20052017(10)-" + this.m_classiferName + "-" + this.m_modelYearSplit + MA_PREFIX + this.m_policy;
	}
}
