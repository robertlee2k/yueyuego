package yueyueGo.dataFormat;

public class AvgLineDataFormat extends ArffFormat {

	@Override
	protected void initializeFormat() {
		TRANSACTION_ARFF_PREFIX="trans20052016-ext";
		SHORT_ARFF_FILE = TRANSACTION_ARFF_PREFIX+"-short.arff";// 不包含计算字段的ARFF格式，这是提供给各输入属性独立的分类器使用的，如神经网络
		
		//模型用的训练字段 （当前使用模型的基础部分）
		MODEL_ATTRIB_FORMAT_BASE=new String[] {
				SELECTED_AVG_LINE, BIAS5, "bias10", "bias20", "bias30",
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
				"zhangdiefu", "huanshoulv",
				"huanshoulv_preday_perc","huanshoulv_pre2day_perc", "huanshoulv_pre3day_perc",
				"zhishu_code", 
//				SW_ZHISHU_CODE,
				IS_HS300 ,
				IS_ZZ500, 
//				"isrzbd", 
//				"sw_bias5", "sw_bias10","sw_bias20", "sw_bias30", "sw_bias60", 
//				"sw_bias5_preday_dif","sw_bias10_preday_dif", "sw_bias20_preday_dif",	"sw_bias30_preday_dif", "sw_bias60_preday_dif",	
//				"sw_bias5_pre2day_dif", "sw_bias10_pre2day_dif","sw_bias20_pre2day_dif", "sw_bias30_pre2day_dif","sw_bias60_pre2day_dif", 
				"zhishu_bias5", "zhishu_bias10","zhishu_bias20", "zhishu_bias30", "zhishu_bias60",
				"zhishu_bias5_preday_dif", "zhishu_bias10_preday_dif","zhishu_bias20_preday_dif", "zhishu_bias30_preday_dif","zhishu_bias60_preday_dif",
				"zhishu_bias5_pre2day_dif","zhishu_bias10_pre2day_dif", "zhishu_bias20_pre2day_dif","zhishu_bias30_pre2day_dif", "zhishu_bias60_pre2day_dif",
				"zhishu_quantity_preday_perc","zhishu_quantity_pre2day_perc","zhishu_quantity_pre3day_perc",
				"zhishu_ma5_indicator","zhishu_ma10_indicator","zhishu_ma20_indicator","zhishu_ma30_indicator","zhishu_ma60_indicator",
//				"sw_ma5_indicator","sw_ma10_indicator","sw_ma20_indicator","sw_ma30_indicator","sw_ma60_indicator",
//				"ma5_signal_scale","ma10_signal_scale","ma20_signal_scale","ma30_signal_scale","ma60_signal_scale"
				"ma5_new_signal_scale","ma10_new_signal_scale","ma20_new_signal_scale","ma30_new_signal_scale","ma60_new_signal_scale"
				,"zhangdieting","shangying","xiaying","index_shangying","index_xiaying","yearhighbias","yearlowbias","monthhighbias","monthlowbias",
				"index_yearhighbias","index_yearlowbias","index_monthhighbias","index_monthlowbias"
				,"circulation_marketVal_gears","PE_TTM","PE_TTM_gears","PE_LYR","PE_LYR_gears","listed_days_gears","is_st",
				"skewness5_gupiao","skewness10_gupiao","skewness20_gupiao","skewness30_gupiao","skewness60_gupiao",
				"skewness5_zhishu","skewness10_zhishu","skewness20_zhishu","skewness30_zhishu","skewness60_zhishu",
//				"skewness5_shenwan","skewness10_shenwan","skewness20_shenwan","skewness30_shenwan","skewness60_shenwan",
				"kurtosis5_gupiao","kurtosis10_gupiao","kurtosis20_gupiao","kurtosis30_gupiao","kurtosis60_gupiao",
				"kurtosis5_zhishu","kurtosis10_zhishu","kurtosis20_zhishu","kurtosis30_zhishu","kurtosis60_zhishu",
//				"kurtosis5_shenwan","kurtosis10_shenwan","kurtosis20_shenwan","kurtosis30_shenwan","kurtosis60_shenwan",
				"HV5_gupiao","HV10_gupiao","HV20_gupiao","HV30_gupiao","HV60_gupiao",
				"HV5_zhishu","HV10_zhishu","HV20_zhishu","HV30_zhishu","HV60_zhishu",
//				"HV5_shenwan","HV10_shenwan","HV20_shenwan","HV30_shenwan","HV60_shenwan",
				"leijizhangdiefu5_gupiao","leijizhangdiefu10_gupiao","leijizhangdiefu20_gupiao","leijizhangdiefu30_gupiao","leijizhangdiefu60_gupiao",
				"leijizhangdiefu5_zhishu","leijizhangdiefu10_zhishu","leijizhangdiefu20_zhishu","leijizhangdiefu30_zhishu","leijizhangdiefu60_zhishu",
//				"leijizhangdiefu5_shenwan","leijizhangdiefu10_shenwan","leijizhangdiefu20_shenwan","leijizhangdiefu30_shenwan","leijizhangdiefu60_shenwan",
				"jun_huanhoulv_bilv5_gupiao","jun_huanhoulv_bilv10_gupiao","jun_huanhoulv_bilv20_gupiao","jun_huanhoulv_bilv30_gupiao",	"jun_huanhoulv_bilv60_gupiao",
				"junliang_bilv5_zhishu","junliang_bilv10_zhishu","junliang_bilv20_zhishu","junliang_bilv30_zhishu","junliang_bilv60_zhishu"

		};
		
		//上一次模型用的训练字段 （用于对比新旧模型时使用）
		MODEL_ATTRIB_FORMAT_LEGACY= new String[] {
				SELECTED_AVG_LINE, BIAS5, "bias10", "bias20", "bias30",
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
				"zhangdiefu", "huanshoulv",
				"huanshoulv_preday_perc","huanshoulv_pre2day_perc", "huanshoulv_pre3day_perc",
				"zhishu_code", 
				SW_ZHISHU_CODE,
				IS_HS300 ,
				IS_ZZ500, 
				"isrzbd", 
				"sw_bias5", "sw_bias10","sw_bias20", "sw_bias30", "sw_bias60", 
				"sw_bias5_preday_dif","sw_bias10_preday_dif", "sw_bias20_preday_dif",	"sw_bias30_preday_dif", "sw_bias60_preday_dif",	
				"sw_bias5_pre2day_dif", "sw_bias10_pre2day_dif","sw_bias20_pre2day_dif", "sw_bias30_pre2day_dif","sw_bias60_pre2day_dif", 
				"zhishu_bias5", "zhishu_bias10","zhishu_bias20", "zhishu_bias30", "zhishu_bias60",
				"zhishu_bias5_preday_dif", "zhishu_bias10_preday_dif","zhishu_bias20_preday_dif", "zhishu_bias30_preday_dif","zhishu_bias60_preday_dif",
				"zhishu_bias5_pre2day_dif","zhishu_bias10_pre2day_dif", "zhishu_bias20_pre2day_dif","zhishu_bias30_pre2day_dif", "zhishu_bias60_pre2day_dif",
				"zhishu_quantity_preday_perc","zhishu_quantity_pre2day_perc","zhishu_quantity_pre3day_perc",
				"zhishu_ma5_indicator","zhishu_ma10_indicator","zhishu_ma20_indicator","zhishu_ma30_indicator","zhishu_ma60_indicator",
				"sw_ma5_indicator","sw_ma10_indicator","sw_ma20_indicator","sw_ma30_indicator","sw_ma60_indicator",
				"ma5_signal_scale","ma10_signal_scale","ma20_signal_scale","ma30_signal_scale","ma60_signal_scale"
				,"zhangdieting","shangying","xiaying","index_shangying","index_xiaying","yearhighbias","yearlowbias","monthhighbias","monthlowbias",
				"index_yearhighbias","index_yearlowbias","index_monthhighbias","index_monthlowbias"
				,"circulation_marketVal_gears","PE_TTM","PE_TTM_gears","PE_LYR","PE_LYR_gears","listed_days_gears","is_st",
				"skewness5_gupiao","skewness10_gupiao","skewness20_gupiao","skewness30_gupiao","skewness60_gupiao",
				"skewness5_zhishu","skewness10_zhishu","skewness20_zhishu","skewness30_zhishu","skewness60_zhishu",
				"skewness5_shenwan","skewness10_shenwan","skewness20_shenwan","skewness30_shenwan","skewness60_shenwan",
				"kurtosis5_gupiao","kurtosis10_gupiao","kurtosis20_gupiao","kurtosis30_gupiao","kurtosis60_gupiao",
				"kurtosis5_zhishu","kurtosis10_zhishu","kurtosis20_zhishu","kurtosis30_zhishu","kurtosis60_zhishu",
				"kurtosis5_shenwan","kurtosis10_shenwan","kurtosis20_shenwan","kurtosis30_shenwan","kurtosis60_shenwan",
				"HV5_gupiao","HV10_gupiao","HV20_gupiao","HV30_gupiao","HV60_gupiao",
				"HV5_zhishu","HV10_zhishu","HV20_zhishu","HV30_zhishu","HV60_zhishu",
				"HV5_shenwan","HV10_shenwan","HV20_shenwan","HV30_shenwan","HV60_shenwan",
				"leijizhangdiefu5_gupiao","leijizhangdiefu10_gupiao","leijizhangdiefu20_gupiao","leijizhangdiefu30_gupiao","leijizhangdiefu60_gupiao",
				"leijizhangdiefu5_zhishu","leijizhangdiefu10_zhishu","leijizhangdiefu20_zhishu","leijizhangdiefu30_zhishu","leijizhangdiefu60_zhishu",
				"leijizhangdiefu5_shenwan","leijizhangdiefu10_shenwan","leijizhangdiefu20_shenwan","leijizhangdiefu30_shenwan","leijizhangdiefu60_shenwan",
				"jun_huanhoulv_bilv5_gupiao","jun_huanhoulv_bilv10_gupiao","jun_huanhoulv_bilv20_gupiao","jun_huanhoulv_bilv30_gupiao",	"jun_huanhoulv_bilv60_gupiao",
				"junliang_bilv5_zhishu","junliang_bilv10_zhishu","junliang_bilv20_zhishu","junliang_bilv30_zhishu","junliang_bilv60_zhishu"

		};
		
	}	
}
