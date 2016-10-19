package yueyueGo.fullModel;

import yueyueGo.ArffFormat;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.utility.FormatUtility;
import yueyueGo.utility.InstanceUtility;

public class ArffFormatFullModel extends ArffFormat {
	
	public static final int FULLMODEL_FORMAT=3;
	
	
	//父类中不包括的两个常量定义
	public final static String FULL_MODEL_ARFF_PREFIX="all20052016-ext";//"AllTransaction20052016-ext";
	protected static final String FULL_MODEL_LONG_ARFF_FILE = FULL_MODEL_ARFF_PREFIX+"-new.arff"; // 包含计算字段的ARFF格式，这是提供给各输入属性独立的分类器使用的，如分类树
	protected static final String FULL_MODEL_SHORT_ARFF_FILE = FULL_MODEL_ARFF_PREFIX+"-short.arff";// 不包含计算字段的ARFF格式，这是提供给各输入属性独立的分类器使用的，如神经网络
	
//	protected final static String FULL_MODEL_AVG_LINE = "chicang_days"; // 输入输出文件中的“持仓天数”名称
	
	//全模型用的训练字段 
	protected static final String[] FULL_MODEL_ATTRIB_FORMAT_NEW={
		 BIAS5, "bias10", "bias20", "bias30",
		"bias60", "bias5_preday_dif", "bias10_preday_dif",
		"bias20_preday_dif", "bias30_preday_dif", "bias60_preday_dif",
		"bias5_pre2day_dif", "bias10_pre2day_dif", "bias20_pre2day_dif",
		"bias30_pre2day_dif", "bias60_pre2day_dif", "ma5_preday_perc",
		"ma10_preday_perc", "ma20_preday_perc", "ma30_preday_perc",
		"ma60_preday_perc", "ma5_pre2day_perc", "ma10_pre2day_perc",
		"ma20_pre2day_perc", "ma30_pre2day_perc", "ma60_pre2day_perc",
		"ma5_pre3day_perc", "ma10_pre3day_perc", "ma20_pre3day_perc",
		"ma30_pre3day_perc", "ma60_pre3day_perc", "ma5_pre4day_perc",
		"ma10_pre4day_perc", "ma20_pre4day_perc", "ma30_pre4day_perc",
		"ma60_pre4day_perc", "ma5_pre5day_perc", "ma10_pre5day_perc",
		"ma20_pre5day_perc", "ma30_pre5day_perc", "ma60_pre5day_perc",
		"zhangdiefu", "huanshoulv", "huanshoulv_preday_perc",
		"huanshoulv_pre2day_perc", "huanshoulv_pre3day_perc",
		"zhishu_code", "sw_zhishu_code",IS_SZ50 ,IS_HS300 , "iszz100",
		IS_ZZ500, "issz100",
		"isrzbd", "sw_bias5", "sw_bias10",
		"sw_bias20", "sw_bias30", "sw_bias60", "sw_bias5_preday_dif",
		"sw_bias10_preday_dif", "sw_bias20_preday_dif",
		"sw_bias30_preday_dif", "sw_bias60_preday_dif",
		"sw_bias5_pre2day_dif", "sw_bias10_pre2day_dif",
		"sw_bias20_pre2day_dif", "sw_bias30_pre2day_dif",
		"sw_bias60_pre2day_dif", "zhishu_bias5", "zhishu_bias10",
		"zhishu_bias20", "zhishu_bias30", "zhishu_bias60",
		"zhishu_bias5_preday_dif", "zhishu_bias10_preday_dif",
		"zhishu_bias20_preday_dif", "zhishu_bias30_preday_dif",
		"zhishu_bias60_preday_dif", "zhishu_bias5_pre2day_dif",
		"zhishu_bias10_pre2day_dif", "zhishu_bias20_pre2day_dif",
		"zhishu_bias30_pre2day_dif", "zhishu_bias60_pre2day_dif",
		"zhishu_quantity_preday_perc","zhishu_quantity_pre2day_perc","zhishu_quantity_pre3day_perc","zhishu_ma5_indicator","zhishu_ma10_indicator","zhishu_ma20_indicator","zhishu_ma30_indicator","zhishu_ma60_indicator","sw_ma5_indicator","sw_ma10_indicator","sw_ma20_indicator","sw_ma30_indicator","sw_ma60_indicator","ma5_signal_scale","ma10_signal_scale","ma20_signal_scale","ma30_signal_scale","ma60_signal_scale"
		,"zhangdieting","shangying","xiaying","index_shangying","index_xiaying","yearhighbias","yearlowbias","monthhighbias","monthlowbias","index_yearhighbias","index_yearlowbias","index_monthhighbias","index_monthlowbias"
		,"circulation_marketVal_gears","PE_TTM","PE_TTM_gears","PE_LYR","PE_LYR_gears","listed_days_gears","is_st",
		"skewness5_gupiao","skewness10_gupiao","skewness20_gupiao","skewness30_gupiao","skewness60_gupiao",
		"skewness5_zhishu","skewness10_zhishu","skewness20_zhishu","skewness30_zhishu","skewness60_zhishu",
		"skewness5_shenwan","skewness10_shenwan","skewness20_shenwan","skewness30_shenwan","skewness60_shenwan",
		"kurtosis5_gupiao","kurtosis10_gupiao","kurtosis20_gupiao","kurtosis30_gupiao","kurtosis60_gupiao",
		"kurtosis5_zhishu","kurtosis10_zhishu","kurtosis20_zhishu","kurtosis30_zhishu","kurtosis60_zhishu",
		"kurtosis5_shenwan","kurtosis10_shenwan","kurtosis20_shenwan","kurtosis30_shenwan","kurtosis60_shenwan",
		"HV5_gupiao","HV10_gupiao","HV20_gupiao","HV30_gupiao","HV60_gupiao",
		"HV5_zhishu","HV10_zhishu","HV20_zhishu","HV30_zhishu","HV60_zhishu",
		"HV5_shenwan","HV10_shenwan","HV20_shenwan","HV30_shenwan","HV60_shenwan"
	};
	
	//每次新扩展ARFF格式的校验位
	protected static final String[] FULL_MODEL_EXT_ARFF_CRC= {
		ID,TRADE_DATE,CODE,SELL_DATE,DATA_DATE,"bias5_preday_dif","zhishu_code"
	};
	//每次新扩展ARFF格式增加的数据
	protected static final String[] FULL_MODEL_EXT_ARFF_COLUMNS= {
		"skewness5_gupiao","skewness10_gupiao","skewness20_gupiao","skewness30_gupiao","skewness60_gupiao",
		"skewness5_zhishu","skewness10_zhishu","skewness20_zhishu","skewness30_zhishu","skewness60_zhishu",
		"skewness5_shenwan","skewness10_shenwan","skewness20_shenwan","skewness30_shenwan","skewness60_shenwan",
		"kurtosis5_gupiao","kurtosis10_gupiao","kurtosis20_gupiao","kurtosis30_gupiao","kurtosis60_gupiao",
		"kurtosis5_zhishu","kurtosis10_zhishu","kurtosis20_zhishu","kurtosis30_zhishu","kurtosis60_zhishu",
		"kurtosis5_shenwan","kurtosis10_shenwan","kurtosis20_shenwan","kurtosis30_shenwan","kurtosis60_shenwan",
		"HV5_gupiao","HV10_gupiao","HV20_gupiao","HV30_gupiao","HV60_gupiao",
		"HV5_zhishu","HV10_zhishu","HV20_zhishu","HV30_zhishu","HV60_zhishu",
		"HV5_shenwan","HV10_shenwan","HV20_shenwan","HV30_shenwan","HV60_shenwan"
	};
	
	//每次新的扩展ARFF文件整体格式
	protected static final String[] FULL_MODEL_EXT_ARFF_FILE_FORMAT= FormatUtility.concatStrings(FULL_MODEL_EXT_ARFF_CRC,FULL_MODEL_EXT_ARFF_COLUMNS);
		
	
	//全模型单次收益率数据中不用保存在ARFF文件中的字段
	private static final String[] FULL_MODEL_DATA_NOT_SAVED_IN_ARFF={ 
		TRADE_DATE,CODE, SELL_DATE, DATA_DATE, "chicang_days", IS_POSITIVE
	};
	// 全模型单次收益率增量数据的格式 （从ID到bias5之前的字段），后面都和dailyArff的相同了
	private static final String[] FULL_MODEL_DATA_LEFT = FormatUtility.concatStrings(new String[]{ID},FULL_MODEL_DATA_NOT_SAVED_IN_ARFF);
	protected static final String[] FULL_MODEL_DATA_FORMAT_NEW=FormatUtility.concatStrings(FULL_MODEL_DATA_LEFT,FULL_MODEL_ATTRIB_FORMAT_NEW);

	
	// 交易ARFF数据全集数据的格式 （从ID到均线策略之前，后面都和trainingarff的相同了）
	private static final String[] FULL_MODEL_DATA_LEFT_PART = { ID,
			YEAR_MONTH, TRADE_DATE, CODE, SELL_DATE,  
			DATA_DATE, IS_POSITIVE, SELECTED_AVG_LINE,BIAS5,IS_SZ50 ,IS_HS300 , 
			IS_ZZ500,SHOUYILV };
	
	// 每日预测扩展格式数据（数据库和数据文件都是如此)的格式
	public static String[] DAILY_DATA_TO_PREDICT_FULL_MODEL = FormatUtility.concatStrings(new String[]{ID},FULL_MODEL_ATTRIB_FORMAT_NEW,new String[]{CODE});
		

	// 此方法从All Transaction Data中保留计算收益率的相关字段，以及最后的收益率，删除其他计算字段
	protected static GeneralInstances getTransLeftPartForFullModel(GeneralInstances allData)
			throws Exception {
		String saveString=InstanceUtility.returnAttribsPosition(allData,FULL_MODEL_DATA_LEFT_PART);
		GeneralInstances result = InstanceUtility.filterAttribs(allData,saveString);
		return result;
	}
	
	// 从All Transaction Data中删除无关字段 (tradeDate到均线策略之前）
	protected static GeneralInstances prepareTransDataForFullModel(GeneralInstances allData)
			throws Exception {
		GeneralInstances result = InstanceUtility.removeAttribs(allData,FULL_MODEL_DATA_NOT_SAVED_IN_ARFF);
		return result;
	}
}
