package yueyueGo.fullModel;

import weka.core.Instances;
import weka.experiment.InstanceQuery;
import yueyueGo.ArffFormat;
import yueyueGo.utility.DBAccess;
import yueyueGo.utility.InstanceUtility;
// 全交易的非均线模型
public class DBAccessFullModel extends DBAccess {
	
	//取最新交易日的数据预测
	public static Instances LoadFullModelDataFromDB() throws Exception{
		return LoadFullModelDataFromDB(null);
	}
	
	//如果传入的参数 dateString==null; 则取数据库中的最新数据，否则取相应交易日的数据
	protected static Instances LoadFullModelDataFromDB(String dateString) throws Exception{
		String[] validateFormat=ArffFormatFullModel.DAILY_DATA_TO_PREDICT_FULL_MODEL;

		//load data from database that needs predicting
		InstanceQuery query = new InstanceQuery();
		query.setDatabaseURL(URL);
		query.setUsername(USER);
		query.setPassword(PASSWORD);
		String queryData=generateFullModelQueryData(dateString);
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
	
	 
	//如果传入的参数 dateString==null; 则取数据库中的最新数据，否则取相应交易日的数据
	private static String generateFullModelQueryData(String dateString){
		
		String queryData="SELECT ";
		String[] target_columns=null;
		String target_view=null;
		String date_cretiriaString="";
		
		target_columns=ArffFormatFullModel.DAILY_DATA_TO_PREDICT_FULL_MODEL;

		//如果传入的参数 dateString==null; 则取数据库中的最新数据，否则取相应交易日的数据
		if (dateString==null){
			target_view="t_stock_avgline_increment_zuixin_group5_optional";//"t_stock_avgline_increment_zuixin_group4_optional";
		}else{
			target_view="v_increment_all";
			date_cretiriaString=" where date='"+dateString+"'";
		}

		for (int i=0;i<target_columns.length;i++){
			queryData+= " `"+target_columns[i]+"`";
			if (i<target_columns.length-1){
				queryData+=", ";
			}
		}
		queryData+=" FROM "+target_view +date_cretiriaString;  // and zhangdieting>-1 and sw_zhishu_code>0"; //2016-07-25
		System.out.println(queryData);
		return queryData;
	}
}
