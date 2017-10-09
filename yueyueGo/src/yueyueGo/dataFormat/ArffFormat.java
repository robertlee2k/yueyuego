package yueyueGo.dataFormat;

import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.dataProcessor.InstanceHandler;
import yueyueGo.databeans.GeneralAttribute;
import yueyueGo.databeans.GeneralInstance;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.utility.FormatUtility;

public abstract class ArffFormat {
	
	// 常量定义
	public static final int LEGACY_FORMAT=9;
	public static final int CURRENT_FORMAT=10;
	
	
	public static final double DOUBLE_VALUE_YES = 1;
	public static final double DOUBLE_VALUE_NO = 0;
	public static final String STRING_VALUE_YES = "1";
	public static final String STRING_VALUE_NO = "0";
		
	public static final String RESULT_PREDICTED_PROFIT = "PredictedProfit";
	public static final String RESULT_PREDICTED_WIN_RATE="PredictedWinRate";
	public static final String RESULT_SELECTED = "selected";
	
	public static final String TRADE_DATE = "tradeDate"; // 之所以定义这个字段，是因为所有的数据都要以它排序
	public static final String SELL_DATE = "mc_date";
	public static final String DATA_DATE = "dataDate";
	public static final String BIAS5 = "bias5";
	public static final String YEAR_MONTH = "yearmonth";
	public static final String CODE = "code";
//	public static final String SW_ZHISHU_CODE="sw_zhishu_code";
	public static final String ZHISHU_CODE = "zhishu_code";

	public static final String IS_HS300 = "ishs300";
	public static final String IS_ZZ500 = "iszz500";
	public static final String IS_SZ50 = "issz50";
	public static final String SHOUYILV = "shouyilv";
	public static final String IS_POSITIVE = "positive";
	
	public static final String ID = "id";
	public static final int ID_POSITION = 1; // ID的位置
	public static final int YEAR_MONTH_INDEX = 3; // yearmonth所处位置，理论上说可以不用这个定义，用findAttPosition查找，暂时保留吧

	// 读取的数据源（每日预测数据和单次收益率数据）中的日期格式
	public static final String INPUT_DATE_FORMAT = "yyyy/M/d";
	// ARFF文件中的日期格式
	public static final String ARFF_DATE_FORMAT = "yyyy/MM/dd";//"M/d/yyyy"; 
	
//	//须去除的行业相关数据
//	protected static final String[] REMOVE_SW_DATA= {
//			SW_ZHISHU_CODE,
//			"isrzbd",
//			"sw_bias5", "sw_bias10","sw_bias20", "sw_bias30", "sw_bias60",
//			"sw_bias5_preday_dif",	"sw_bias10_preday_dif", "sw_bias20_preday_dif",	"sw_bias30_preday_dif", "sw_bias60_preday_dif",
//			"sw_bias5_pre2day_dif", "sw_bias10_pre2day_dif","sw_bias20_pre2day_dif", "sw_bias30_pre2day_dif","sw_bias60_pre2day_dif",
//			"sw_ma5_indicator","sw_ma10_indicator","sw_ma20_indicator","sw_ma30_indicator","sw_ma60_indicator",
//			"skewness5_shenwan","skewness10_shenwan","skewness20_shenwan","skewness30_shenwan","skewness60_shenwan",
//			"kurtosis5_shenwan","kurtosis10_shenwan","kurtosis20_shenwan","kurtosis30_shenwan","kurtosis60_shenwan",
//			"HV5_shenwan","HV10_shenwan","HV20_shenwan","HV30_shenwan","HV60_shenwan",
//			"leijizhangdiefu5_shenwan","leijizhangdiefu10_shenwan","leijizhangdiefu20_shenwan","leijizhangdiefu30_shenwan","leijizhangdiefu60_shenwan",
//	};
//	
//	// 从All Transaction Data中删除申万行业数据
//	public static GeneralInstances removeSWData(GeneralInstances allData)
//			throws Exception {
//		GeneralInstances result = InstanceHandler.getHandler(allData).removeAttribs(allData,REMOVE_SW_DATA);
//		return result;
//	}		
	//end of 常量定义
	
	public boolean convertNominalToNumeric=true; //缺省需要将Nominal转换为Numeric （其实目前就只有指数code这一个）

	public String m_data_root_directory; //这类数据模型的根目录（相对目录）
	public String m_data_file_prefix; //数据文件的前缀名

	
	public String m_policy_group; // 输入输出文件中的“策略分组”名称
	//当前模型用的训练字段 （在子类中定义）
	protected String[] m_model_attribute_format;
	//前一模型用的训练字段 （在子类中定义，一般用于预测对比）
	protected String[] m_model_attribute_format_legacy;
	
	//单次收益率数据中不用保存在ARFF文件中的字段（需要子类定义）
	protected  String[] m_arff_data_not_in_model;
	
	
	// 每日预测（旧模型数据格式）数据（数据库和数据文件都是如此)的旧格式
	public String[] m_daily_data_to_predict_format_legacy;
	
	// 每日预测扩展格式数据（数据库和数据文件都是如此)的格式
	public String[] m_daily_data_to_predict_format;
	
	//每日预测数据中的左侧字段，此处顺序无关（positive和收益率其实是二选一的）
	public String[] m_daily_predict_left_part;

	
	// 单次收益率增量全部数据的格式 （从数据库获得的数据，不包含计算字段如YearMonth）
	public  String[] m_arff_data_full;

	
	public ArffFormat() {
		//先调用子类的方法对相应数据赋值
		initializeFormat();
		m_daily_data_to_predict_format_legacy = FormatUtility.concatStrings(new String[]{ID},m_model_attribute_format_legacy,new String[]{CODE});
		m_daily_data_to_predict_format = FormatUtility.concatStrings(new String[]{ID},m_model_attribute_format,new String[]{CODE});
		String[] temp = FormatUtility.concatStrings(new String[]{ID},m_arff_data_not_in_model);
		m_arff_data_full=FormatUtility.concatStrings(temp,m_model_attribute_format, new String[]{SHOUYILV});
		
		m_daily_predict_left_part=new String[]{ID,m_policy_group,BIAS5,CODE,IS_POSITIVE,SHOUYILV};
	}

	protected abstract void initializeFormat();
	

	//从数据库中获取的各属性值中需要作为nominal或string处理的数据（这里是全量定义，与顺序无关）
	private static final String[] NOMINAL_ATTRIBS={
		TRADE_DATE,CODE, SELL_DATE, DATA_DATE, 
		ZHISHU_CODE, 
		//为保持和TensorFlow兼容，以下数据都作为double处理
//		AvgLineDataFormat.SELECTED_AVGLINE,
//		MomentumDataFormat.MOMENTUM_PERIOD
//		IS_POSITIVE,  
//		"zhangdieting",
//		IS_SZ50 ,IS_HS300 ,	IS_ZZ500, "is_st",
	};
	
	//返回给定数据集里与NOMINAL_ATTRIBS同名字段的位置字符串（从1开始），这主要是为filter使用
	public static String findNominalAttribs(GeneralInstances data){
		return BaseInstanceProcessor.returnAttribsPosition(data,NOMINAL_ATTRIBS);
	}
	
	// 从All Transaction Data中删除无关字段 (tradeDate到均线策略之前）
	public  GeneralInstances prepareTransData(GeneralInstances allData)
			throws Exception {
		GeneralInstances result = InstanceHandler.getHandler(allData).removeAttribs(allData,m_arff_data_not_in_model);// "3-9");
		return result;
	}

	// 此方法从All Transaction Data中保留计算收益率的相关字段，以及最后的收益率，删除其他计算字段
	public GeneralInstances getTransLeftPartFromAllTransaction(GeneralInstances allData)
			throws Exception {
		String[] TRANS_DATA_LEFT_PART = new String[] { ID,
				TRADE_DATE,YEAR_MONTH,  CODE, SELL_DATE,  
				DATA_DATE, IS_POSITIVE, m_policy_group,BIAS5,IS_SZ50 ,IS_HS300 , 
				IS_ZZ500,SHOUYILV };
		return InstanceHandler.getHandler(allData).filterAttribs(allData,TRANS_DATA_LEFT_PART);
	}
	


	// 将输入文件和standardFormat数据字段名称顺序对比 ，不一致则报错。
	public static GeneralInstances validateAttributeNames(GeneralInstances data,String[] standardFormat) throws Exception {
		String incomingColumnName=null;
		int ignoredColumns=0; //当需要忽略standardFormat中的某列时
		int validColumns=0;
		for (int i = 0; i < standardFormat.length; i++) {
			incomingColumnName=data.attribute(i + ignoredColumns).name();
			if (incomingColumnName.equals(standardFormat[i])){
//				System.out.println("PASSED. input data column name ["
//						+ incomingColumnName
//						+ "] equals to model attribuate name ["
//						+ standardFormat[i] + "]");
				validColumns++;
			}else {
				throw new Exception("input data column name is invalid! input column="+incomingColumnName+ " valid column should be:"+standardFormat[i]);
			}
		}
		System.out.println("column name verification PASSED. number of attributes="+validColumns);
		for (int j=standardFormat.length;j<data.numAttributes();j++){
			incomingColumnName=data.attribute(j).name();
			System.err.println("WARNING!!!! input data has additional column. name="+incomingColumnName);
		}
		return data;
	}


	// 判断是否为沪深300、中证500、上证50
	protected static boolean belongToIndex(GeneralInstance curr, String indexName) {
		GeneralAttribute hsAtt = curr.dataset().attribute(indexName);
//		String label = curr.stringValue(hsAtt);
		double value=curr.value(hsAtt);
//		if (label.equals(STRING_VALUE_YES)) { // 故意用这个顺序，如果label是null就扔exception出去
		if (value==DOUBLE_VALUE_YES){
			return true;
		} else {
			return false;
		}
	}

	// 判断是否为沪深300
	public static boolean isHS300(GeneralInstance curr) {
		return belongToIndex(curr, IS_HS300);
	}

	// 判断是否为中证500
	public static boolean isZZ500(GeneralInstance curr) {
		return belongToIndex(curr, IS_ZZ500);
	}

	// 判断是否为上证50
	public static boolean isSZ50(GeneralInstance curr) {
		return belongToIndex(curr, IS_SZ50);
	}

	/**
	  比较两个instances中的均线策略和bias5字段是否一致（数据冗余校验）
	 * @return
	 */
	public static boolean checkSumBeforeMerge(GeneralInstance leftCurr,
			GeneralInstance rightCurr, GeneralAttribute leftMA, GeneralAttribute rightMA,
			GeneralAttribute leftBias5, GeneralAttribute rightBias5) {

		boolean result=false;
		try {
			double leftMAValue=-666;
			double rightMAValue=-888;
			
			if (leftMA!=null && rightMA!=null) {
				//这是用于均线策略时的比较，两个MA都不为null时进行比较
				leftMAValue=leftCurr.value(leftMA);  
				rightMAValue=rightCurr.value(rightMA);
			}else if(leftMA==null && rightMA==null){
				//这里是用于短线FULLMODEL模型的比较的
				leftMAValue=-1;
				rightMAValue=-1;
			}else if(leftMA!=null && rightMA==null){
				//这里将短线FULLMODEL模型的应用于均线的ARFF数据测试时
				leftMAValue=leftCurr.value(leftMA);  
				rightMAValue=leftMAValue;
			}else{
				//不应该只有一边leftMA为null，抛出异常算了
				throw new Exception("leftMA="+leftMA+",while rightMA="+rightMA);
			}
			
			double leftBias5Value=leftCurr.value(leftBias5); //这里有可能为NaN
			double rightBias5Value=rightCurr.value(rightBias5);
			
			if (leftMAValue==rightMAValue){
				if ( (leftBias5Value==rightBias5Value) || Double.isNaN(leftBias5Value) && Double.isNaN(rightBias5Value))
					result=true;
			}
		} catch (Exception e){ // 这里吃掉exception是为了继续跑下去，并查错在哪里
			System.err.println(e);
			System.err.println("left instance= "+leftCurr.toString());
			System.err.println("right instance= "+rightCurr.toString());
		}
		return result;
		
	}


	
	public String getDailyFormatFileName(boolean useCurrentFormat){
		if (useCurrentFormat==true){
			return "DailyFormat-"+this.m_data_file_prefix+"("+ArffFormat.CURRENT_FORMAT+").arff";
		}else{
			return "DailyFormat-"+this.m_data_file_prefix+"("+ArffFormat.LEGACY_FORMAT+").arff";	
		}
	}
	
	public String getDailyFormatFileName(){
		return getDailyFormatFileName(true);
	}

	public String getFullFormatFileName(){
		return "FullFormat-"+this.m_data_file_prefix+"("+ArffFormat.CURRENT_FORMAT+").arff";
	}
	
//	public String getTrainingFormatFileName(){ 
//		return this.m_data_file_prefix+"("+ArffFormat.CURRENT_FORMAT+")-train-format.arff";
//	}

	public String getTrainingDataFileName(){ 
		return this.m_data_file_prefix+"("+ArffFormat.CURRENT_FORMAT+")-short.arff";
	}

	public String getLeftDataFileName(){ 
		return this.m_data_file_prefix+"("+ArffFormat.CURRENT_FORMAT+")-left.arff";
	}
	
	public String getFullArffFileName(){ 
		return this.m_data_file_prefix+"("+ArffFormat.CURRENT_FORMAT+").arff";
	}

}
