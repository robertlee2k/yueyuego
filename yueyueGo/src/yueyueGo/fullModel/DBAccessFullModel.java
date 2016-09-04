package yueyueGo.fullModel;

import weka.core.Instances;
import weka.experiment.InstanceQuery;
import yueyueGo.ArffFormat;
import yueyueGo.utility.DBAccess;
import yueyueGo.utility.InstanceUtility;

public class DBAccessFullModel extends DBAccess {
	// 全交易的非均线模型
	protected static Instances LoadFullModelDataFromDB(String dateString) throws Exception{
		String[] validateFormat=ArffFormat.DAILY_DATA_TO_PREDICT_FORMAT_NEW;

		//load data from database that needs predicting
		InstanceQuery query = new InstanceQuery();
		query.setDatabaseURL(URL);
		query.setUsername(USER);
		query.setPassword(PASSWORD);
		String queryData=generateFullModelQueryData(dateString);
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
	
	@Deprecated 
	private static String generateFullModelQueryData(String dateString){
		
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
