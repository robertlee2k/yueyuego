package yueyueGo;

import weka.core.Instances;
import weka.experiment.InstanceQuery;

public class DBAccess  {
	public final static String URL = "jdbc:mysql://uts.simu800.com/develop?characterEncoding=utf8&autoReconnect=true";
	public final static String USER = "root";
	public final static String PASSWORD = "data@2014";
	//protected final static String QUERY_DATA = "SELECT  `id`, `selected_avgline`, `bias5`,  `bias10`,  `bias20`,  `bias30`,  `bias60`, `bias5_preday_dif`,  `bias10_preday_dif`,  `bias20_preday_dif`,  `bias30_preday_dif`,  `bias60_preday_dif`,  `bias5_pre2day_dif`,  `bias10_pre2day_dif`,  `bias20_pre2day_dif`,  `bias30_pre2day_dif`,  `bias60_pre2day_dif`, `ma5_preday_perc`,  `ma10_preday_perc`, `ma20_preday_perc`,`ma30_preday_perc`,`ma60_preday_perc`,`ma5_pre2day_perc`, `ma10_pre2day_perc`,`ma20_pre2day_perc`,`ma30_pre2day_perc`,`ma60_pre2day_perc`,`ma5_pre3day_perc`, `ma10_pre3day_perc`,`ma20_pre3day_perc`,`ma30_pre3day_perc`,`ma60_pre3day_perc`,`ma5_pre4day_perc`, `ma10_pre4day_perc`,`ma20_pre4day_perc`,`ma30_pre4day_perc`,`ma60_pre4day_perc`,`ma5_pre5day_perc`, `ma10_pre5day_perc`,`ma20_pre5day_perc`,`ma30_pre5day_perc`,`ma60_pre5day_perc`, `zhangdiefu`,  `huanshoulv`, `huanshoulv_preday_perc`,  `huanshoulv_pre2day_perc`,  `huanshoulv_pre3day_perc`, `zhishu_code`,   `sw_zhishu_code`, `issz50`,  `ishs300`, `iszz100`, `iszz500`, `issz100`,  `ishgtb`,   `isrzbd`, `sw_bias5`, `sw_bias10`, `sw_bias20`, `sw_bias30`, `sw_bias60`, `sw_bias5_preday_dif`, `sw_bias10_preday_dif`, `sw_bias20_preday_dif`, `sw_bias30_preday_dif`,  `sw_bias60_preday_dif`, `sw_bias5_pre2day_dif`, `sw_bias10_pre2day_dif`,   `sw_bias20_pre2day_dif`,   `sw_bias30_pre2day_dif`,   `sw_bias60_pre2day_dif`, `zhishu_bias5`, `zhishu_bias10`, `zhishu_bias20`,  `zhishu_bias30`,   `zhishu_bias60`,  `zhishu_bias5_preday_dif`, `zhishu_bias10_preday_dif`,  `zhishu_bias20_preday_dif`, `zhishu_bias30_preday_dif`,  `zhishu_bias60_preday_dif`,  `zhishu_bias5_pre2day_dif`,   `zhishu_bias10_pre2day_dif`,  `zhishu_bias20_pre2day_dif`, `zhishu_bias30_pre2day_dif`, `zhishu_bias60_pre2day_dif`,  `shouyilv` FROM t_stock_avgline_increment_zuixin_v ";
			
	
	
	public DBAccess() {

	}
	
	private static String generateQueryData(int format){
		
		String queryData="SELECT ";
		String[] target_columns=null;
		String target_view=null;
		
		switch (format) {
		case ArffFormat.LEGACY_FORMAT:
			target_columns=ArffFormat.DAILY_DATA_TO_PREDICT_FORMAT_LEGACY;
			target_view="t_stock_avgline_increment_zuixin_v";
			break;
		case ArffFormat.EXT_FORMAT:
			target_columns=ArffFormat.DAILY_DATA_TO_PREDICT_FORMAT_NEW;
			target_view="t_stock_avgline_increment_zuixin_group4"; //"t_stock_avgline_increment_zuixin_group3";
			break;			
		default:
			break;
		}
		for (int i=0;i<target_columns.length;i++){
			queryData+= " `"+target_columns[i]+"`";
			if (i<target_columns.length-1){
				queryData+=", ";
			}
		}
		queryData+=" FROM "+target_view;
		System.out.println(queryData);
		return queryData;
	}
	
	public static Instances LoadDataFromDB(int format) throws Exception{

		String[] validateFormat=null;
		switch (format) {
		case ArffFormat.LEGACY_FORMAT:
			validateFormat=ArffFormat.DAILY_DATA_TO_PREDICT_FORMAT_LEGACY;
			break;
		case ArffFormat.EXT_FORMAT:
			validateFormat=ArffFormat.DAILY_DATA_TO_PREDICT_FORMAT_NEW;
			break;			
		default:
			break;
		}	
		//load data from database that needs predicting
		InstanceQuery query = new InstanceQuery();
		query.setDatabaseURL(URL);
		query.setUsername(USER);
		query.setPassword(PASSWORD);
		String queryData=generateQueryData(format);
		query.setQuery(queryData); 
		Instances data = query.retrieveInstances();

		//读入数据后最后一行加上为空的收益率
		data = InstanceUtility.AddAttribute(data, ArffFormat.SHOUYILV,data.numAttributes());
		// 对读入的数据字段名称校验 确保其顺序完全和内部训练的arff格式一致
		data=ArffFormat.validateAttributeNames(data,validateFormat);
		//全部读进来之后再转nominal，这里读入的数据可能只是子集，所以nominal的index值会不对，所以后续会用calibrateAttributes处理
		String nominalAttribString=ArffFormat.findNominalAttribs(data);
		data=InstanceUtility.numToNominal(data, nominalAttribString);//"2,48-56");


		data.setClassIndex(data.numAttributes()-1);
		System.out.println("records loaded from database: "+data.numInstances());
		return data;
	}

	
	
	// 全交易的非均线模型
	public static Instances LoadZiXuanDataFromDB(String dateString) throws Exception{
		String[] validateFormat=ArffFormat.DAILY_DATA_TO_PREDICT_FORMAT_NEW;

		//load data from database that needs predicting
		InstanceQuery query = new InstanceQuery();
		query.setDatabaseURL(URL);
		query.setUsername(USER);
		query.setPassword(PASSWORD);
		String queryData=generateZiXuanQueryData(dateString);
		query.setQuery(queryData); 
		Instances data = query.retrieveInstances();

//		//插入均线
//		data = InstanceUtility.AddAttribute(data, ArffFormat.SELECTED_AVG_LINE,1);
		//读入数据后最后一行加上为空的收益率
		data = InstanceUtility.AddAttribute(data, ArffFormat.SHOUYILV,data.numAttributes());
		// 对读入的数据字段名称校验 确保其顺序完全和内部训练的arff格式一致
		data=ArffFormat.validateAttributeNames(data,validateFormat);
		//全部读进来之后再转nominal，这里读入的数据可能只是子集，所以nominal的index值会不对，所以后续会用calibrateAttributes处理
		String nominalAttribString=ArffFormat.findNominalAttribs(data);
		data=InstanceUtility.numToNominal(data, nominalAttribString);//"2,48-56");


		data.setClassIndex(data.numAttributes()-1);
		System.out.println("records loaded from database: "+data.numInstances());
		return data;
	}
	
	private static String generateZiXuanQueryData(String dateString){
		
		String queryData="SELECT ";
		String[] target_columns=null;
		String target_view=null;
		
		target_columns=ArffFormat.DAILY_DATA_TO_PREDICT_FORMAT_NEW;
		target_view="t_stock_avgline_increment_zuixin_group4_optional"; 

		for (int i=0;i<target_columns.length;i++){
			if (i==1){// 均线策略单独处理
				queryData+= " '5' as `"+target_columns[i]+"`, ";
				continue; 
			}
			queryData+= " `"+target_columns[i]+"`";
			if (i<target_columns.length-1){
				queryData+=", ";
			}
		}
		//TODO 后两个条件是临时加的
		queryData+=" FROM "+target_view +" where date='"+dateString+"' and zhangdieting>-1 and sw_zhishu_code>0"; //2016-07-25
		System.out.println(queryData);
		return queryData;
	}
}
