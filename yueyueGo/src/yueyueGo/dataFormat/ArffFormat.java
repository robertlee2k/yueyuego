package yueyueGo.dataFormat;

import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.dataProcessor.InstanceHandler;
import yueyueGo.databeans.GeneralAttribute;
import yueyueGo.databeans.GeneralInstance;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.utility.FormatUtility;

public abstract class ArffFormat {
	
	// 常量定义
	public static final int LEGACY_FORMAT=7;
	public static final int CURRENT_FORMAT=8;

	public static final String SELECTED_AVG_LINE = "selected_avgline"; // 输入输出文件中的“均线策略”名称
	public static final String IS_HS300 = "ishs300";
	public static final String IS_ZZ500 = "iszz500";
	public static final String IS_SZ50 = "issz50";
	public static final String SHOUYILV = "shouyilv";
	public static final String IS_POSITIVE = "positive";
	public static final String VALUE_YES = "1";
	public static final String VALUE_NO = "0";
		
	public static final String RESULT_PREDICTED_PROFIT = "PredictedProfit";
	public static final String RESULT_SELECTED = "selected";
	public static final String RESULT_PREDICTED_WIN_RATE="PredictedWinRate";

	public static final String TRADE_DATE = "tradeDate"; // 之所以定义这个字段，是因为所有的数据都要以它排序
	public static final String SELL_DATE = "mc_date";
	public static final String DATA_DATE = "dataDate";
	public static final String BIAS5 = "bias5";
	public static final String YEAR_MONTH = "yearmonth";
	public static final String CODE = "code";
	public static final String SW_ZHISHU_CODE="sw_zhishu_code";
	
	public static final String ID = "id";
	public static final int ID_POSITION = 1; // ID的位置
	public static final String YEAR_MONTH_INDEX = "2"; // yearmonth所处位置，理论上说可以不用这个定义，用findAttPosition查找，暂时保留吧

	// 读取的数据源（每日预测数据和单次收益率数据）中的日期格式
	public static final String INPUT_DATE_FORMAT = "yyyy/M/d";
	// ARFF文件中的日期格式
	public static final String ARFF_DATE_FORMAT = "M/d/yyyy";
	
	//须去除的行业相关数据
	protected static final String[] REMOVE_SW_DATA= {
			SW_ZHISHU_CODE,
			"isrzbd",
			"sw_bias5", "sw_bias10","sw_bias20", "sw_bias30", "sw_bias60",
			"sw_bias5_preday_dif",	"sw_bias10_preday_dif", "sw_bias20_preday_dif",	"sw_bias30_preday_dif", "sw_bias60_preday_dif",
			"sw_bias5_pre2day_dif", "sw_bias10_pre2day_dif","sw_bias20_pre2day_dif", "sw_bias30_pre2day_dif","sw_bias60_pre2day_dif",
			"sw_ma5_indicator","sw_ma10_indicator","sw_ma20_indicator","sw_ma30_indicator","sw_ma60_indicator",
			"skewness5_shenwan","skewness10_shenwan","skewness20_shenwan","skewness30_shenwan","skewness60_shenwan",
			"kurtosis5_shenwan","kurtosis10_shenwan","kurtosis20_shenwan","kurtosis30_shenwan","kurtosis60_shenwan",
			"HV5_shenwan","HV10_shenwan","HV20_shenwan","HV30_shenwan","HV60_shenwan",
			"leijizhangdiefu5_shenwan","leijizhangdiefu10_shenwan","leijizhangdiefu20_shenwan","leijizhangdiefu30_shenwan","leijizhangdiefu60_shenwan",
	};
	
	// 从All Transaction Data中删除申万行业数据
	public static GeneralInstances removeSWData(GeneralInstances allData)
			throws Exception {
		GeneralInstances result = InstanceHandler.getHandler(allData).removeAttribs(allData,REMOVE_SW_DATA);
		return result;
	}		
	//end of 常量定义


	public String TRANSACTION_ARFF_PREFIX;
	public String SHORT_ARFF_FILE;
	protected String[] MODEL_ATTRIB_FORMAT_BASE;
	protected String[] MODEL_ATTRIB_FORMAT_LEGACY;

//	//每次新扩展ARFF格式的校验位
//	public  final String[] EXT_ARFF_CRC= {
//		ID,TRADE_DATE,CODE,SELL_DATE,DATA_DATE,SELECTED_AVG_LINE,"bias5_preday_dif","zhishu_code"
//	};
//	//每次新扩展ARFF格式增加的数据
//	public  final String[] EXT_ARFF_COLUMNS= {
//	};
//	//每次新的扩展ARFF文件整体格式
//	public  String[] EXT_ARFF_FILE_FORMAT;
	
	//模型用的训练字段 （基础+扩展部分）
	public String[] MODEL_ATTRIB_FORMAT_NEW;
	
	// 每日预测（旧模型数据格式）数据（数据库和数据文件都是如此)的旧格式
	public String[] DAILY_DATA_TO_PREDICT_FORMAT_LEGACY;
	
	// 每日预测扩展格式数据（数据库和数据文件都是如此)的格式
	public String[] DAILY_DATA_TO_PREDICT_FORMAT_NEW;
	
	//每日预测数据中的左侧字段，此处顺序无关（positive和收益率其实是二选一的）
	public String[] DAILY_PREDICT_RESULT_LEFT={ID,SELECTED_AVG_LINE,BIAS5,CODE,IS_POSITIVE,SHOUYILV};


	//单次收益率数据中不用保存在ARFF文件中的字段
	protected  String[] TRANS_DATA_NOT_SAVED_IN_ARFF;
	
	// 单次收益率增量数据的格式 （从ID到均线策略之前的字段），后面都和dailyArff的相同了
	public  String[] TRANS_DATA_FORMAT_NEW;

	
	public ArffFormat() {
		//先调用子类的方法对相应数据赋值
		initializeFormat();
		MODEL_ATTRIB_FORMAT_NEW=MODEL_ATTRIB_FORMAT_BASE;
				//FormatUtility.concatStrings(MODEL_ATTRIB_FORMAT_BASE,EXT_ARFF_COLUMNS);
//		EXT_ARFF_FILE_FORMAT= FormatUtility.concatStrings(EXT_ARFF_CRC,EXT_ARFF_COLUMNS);
		DAILY_DATA_TO_PREDICT_FORMAT_LEGACY = FormatUtility.concatStrings(new String[]{ID},MODEL_ATTRIB_FORMAT_LEGACY,new String[]{CODE});
		DAILY_DATA_TO_PREDICT_FORMAT_NEW = FormatUtility.concatStrings(new String[]{ID},MODEL_ATTRIB_FORMAT_NEW,new String[]{CODE});
		String[] temp = FormatUtility.concatStrings(new String[]{ID},TRANS_DATA_NOT_SAVED_IN_ARFF);
		TRANS_DATA_FORMAT_NEW=FormatUtility.concatStrings(temp,MODEL_ATTRIB_FORMAT_NEW, new String[]{SHOUYILV});
	}

	protected abstract void initializeFormat();
	

	//所有数据中需要作为nominal 处理的数据
	private static final String[] NOMINAL_ATTRIBS={
		TRADE_DATE,CODE, SELL_DATE, 
		DATA_DATE, SELECTED_AVG_LINE, IS_POSITIVE,
		"zhangdieting",
		"zhishu_code", SW_ZHISHU_CODE,IS_SZ50 ,IS_HS300 , "iszz100",
		IS_ZZ500, "issz100", "ishgtb", "isrzbd","is_st"
	};
	
	//返回给定数据集里与NOMINAL_ATTRIBS同名字段的位置字符串（从1开始），这主要是为filter使用
	public static String findNominalAttribs(GeneralInstances data){
		return BaseInstanceProcessor.returnAttribsPosition(data,NOMINAL_ATTRIBS);
	}
	
	// 从All Transaction Data中删除无关字段 (tradeDate到均线策略之前）
	public  GeneralInstances prepareTransData(GeneralInstances allData)
			throws Exception {
		GeneralInstances result = InstanceHandler.getHandler(allData).removeAttribs(allData,TRANS_DATA_NOT_SAVED_IN_ARFF);// "3-9");
		return result;
	}

	// 交易ARFF数据全集数据的格式 （从ID到均线策略之前，后面都和trainingarff的相同了）
	private String[] TRANS_DATA_LEFT_PART = { ID,
			YEAR_MONTH, TRADE_DATE, CODE, SELL_DATE,  
			DATA_DATE, IS_POSITIVE, SELECTED_AVG_LINE,BIAS5,IS_SZ50 ,IS_HS300 , 
			IS_ZZ500,SHOUYILV };
	
	// 此方法从All Transaction Data中保留计算收益率的相关字段，以及最后的收益率，删除其他计算字段
	public GeneralInstances getTransLeftPartFromAllTransaction(GeneralInstances allData)
			throws Exception {
		return InstanceHandler.getHandler(allData).filterAttribs(allData,TRANS_DATA_LEFT_PART);
	}
	
//	// 为原始的Arff文件加上计算属性
//	@Deprecated
//	public static GeneralInstances addCalculateAttribute(GeneralInstances data) throws Exception {
//		GeneralInstances result = new DataInstances(data, 0);
//
//		int row = data.numInstances();
//		double[][] bias5to60 = { { 0.0, 0.0, 0.0, 0.0, 0.0 },
//				{ 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0 } };
////		String[][] biasAttName = {
////				{ BIAS5, "bias10", "bias20", "bias30", "bias60" },
////				{ "sw行业bias5", "sw行业bias10", "sw行业bias20", "sw行业bias30",
////						"sw行业bias60" },
////				{ "指数bias5", "指数bias10", "指数bias20", "指数bias30", "指数bias60" } };
//		 String[][] biasAttName = {{ BIAS5, "bias10", "bias20",
//		 "bias30","bias60"},{"sw_bias5","sw_bias10","sw_bias20","sw_bias30","sw_bias60"},{"zhishu_bias5","zhishu_bias10","zhishu_bias20","zhishu_bias30","zhishu_bias60"}
//		 };
//		for (int x = 0; x < bias5to60.length; x++) {
//			for (int m = 0; m < bias5to60[x].length; m++) {
//				for (int k = m + 1; k < bias5to60[x].length; k++) {
//					// insert before class value
//					result.insertAttributeAt(new DataAttribute(biasAttName[x][m]+ "-" + biasAttName[x][k]),result.numAttributes() - 1);
//				}
//			}
//		}
//
//		// 为每一行数据处理
//		for (int i = 0; i < row; i++) {
//			GeneralInstance oneRow = data.instance(i);
//			for (int x = 0; x < biasAttName.length; x++) {
//				for (int j = 0; j < biasAttName[x].length; j++) {
//					GeneralAttribute attribute = data.attribute(biasAttName[x][j]);
//					bias5to60[x][j] = oneRow.value(attribute);
//				}
//			}
//			DataInstance newRow = new DataInstance(result.numAttributes());
//			newRow.setDataset(result);
//
//			// copy same values
//
//			for (int n = 0; n < data.numAttributes() - 1; n++) {
//				GeneralAttribute att = data.attribute(n);
//				GeneralAttribute newRowAtt=result.attribute(n);
//				BaseInstanceProcessor.fullCopyAttribute(oneRow, newRow, att, newRowAtt);
////				if (att != null) {
////					if (att.isNominal()) {
////						String label = oneRow.stringValue(att);
////						int index = att.indexOfValue(label);
////						if (index != -1) {
////							newRow.setValue(n, index);
////						}
////					} else if (att.isNumeric()) {
////						newRow.setValue(n, oneRow.value(att));
////					} else {
////						throw new IllegalStateException(
////								"Unhandled attribute type!");
////					}
////				}
//			}
//			// 添加bias相减的部分
//			int addColumn = 0;
//			int insertPosition = data.numAttributes() - 1;
//			for (int x = 0; x < bias5to60.length; x++) {
//				for (int m = 0; m < bias5to60[x].length; m++) {
//					for (int k = m + 1; k < bias5to60[x].length; k++) {
//						newRow.setValue(insertPosition + addColumn,
//								bias5to60[x][m] - bias5to60[x][k]);
//						addColumn++;
//					}
//				}
//			}
//			// 添加最后的classvalue
//			newRow.setValue(result.numAttributes() - 1,
//					oneRow.value(data.numAttributes() - 1));
//			result.add(newRow);
//
//		}
//		return result;
//	}

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
		String label = curr.stringValue(hsAtt);
		if (label.equals(VALUE_YES)) { // 故意用这个顺序，如果label是null就扔exception出去
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
			String leftMAValue=null;
			String rightMAValue=null;
			
			if (leftMA!=null && rightMA!=null) {
				//这是用于均线策略时的比较，两个MA都不为null时进行比较
				leftMAValue=leftCurr.stringValue(leftMA);  
				rightMAValue=rightCurr.stringValue(rightMA);
			}else if(leftMA==null && rightMA==null){
				//这里是用于短线FULLMODEL模型的比较的
				leftMAValue="";
				rightMAValue="";
			}else if(leftMA!=null && rightMA==null){
				//这里将短线FULLMODEL模型的应用于均线的ARFF数据测试时
				leftMAValue=leftCurr.stringValue(leftMA);  
				rightMAValue=leftMAValue;
			}else{
				//不应该只有一边leftMA为null，抛出异常算了
				throw new Exception("leftMA="+leftMA+",while rightMA="+rightMA);
			}
			
			double leftBias5Value=leftCurr.value(leftBias5); //这里有可能为NaN
			double rightBias5Value=rightCurr.value(rightBias5);
			
			if (leftMAValue.equals(rightMAValue)){
				if ( (leftBias5Value==rightBias5Value) || Double.isNaN(leftBias5Value) && Double.isNaN(rightBias5Value))
					result=true;
			}
		} catch (Exception e){ // 这里吃掉exception是为了查错在哪里
			System.err.println(e);
			System.err.println("left instance= "+leftCurr.toString());
			System.err.println("right instance= "+rightCurr.toString());
		}
		return result;
		
	}



}
