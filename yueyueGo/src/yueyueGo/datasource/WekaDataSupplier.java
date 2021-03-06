package yueyueGo.datasource;

import java.io.File;

import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.experiment.InstanceQuery;
import yueyueGo.EnvConstants;
import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.dataFormat.FullModelDataFormat;
import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.dataProcessor.InstanceHandler;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaInstances;

public class WekaDataSupplier implements GeneralDataSupplier {

	// load full set of data
	/* (non-Javadoc)
	 * @see yueyueGo.datasource.GeneralDataSupplier#loadDataFromFile(java.lang.String)
	 */
	@Override
	public  GeneralInstances loadDataFromFile(String fileName)
			throws Exception {
		DataSource source = new DataSource(fileName); 
		GeneralInstances data = new WekaInstances(source.getDataSet());
	
		// setting class attribute if the data format does not provide this
		// information
		if (data.classIndex() == -1)
		  data.setClassIndex(data.numAttributes() - 1);
		return data;
	}

	// 从CSV文件中加载数据，不做任何特殊操作及后续处理
	/* (non-Javadoc)
	 * @see yueyueGo.datasource.GeneralDataSupplier#loadNormalCSVFile(java.lang.String)
	 */
	@Override
	public GeneralInstances loadNormalCSVFile(String fileName)
			throws Exception {
		CSVLoader loader = new CSVLoader();
		loader.setSource(new File(fileName));
		GeneralInstances datasrc = new WekaInstances(loader.getDataSet());
		return datasrc;
	}

	// 从CSV文件中加载数据，用给定格式校验，并设置classIndex
	/* (non-Javadoc)
	 * @see yueyueGo.datasource.GeneralDataSupplier#loadDataWithFormatFromCSVFile(java.lang.String, java.lang.String[])
	 */
	@Override
	public GeneralInstances loadDataWithFormatFromCSVFile(String fileName, String[] format)
				throws Exception {
			GeneralInstances datasrc=loadDataFromExtCSVFile(fileName, format); 
			if (datasrc.classIndex() == -1)
				  datasrc.setClassIndex(datasrc.numAttributes() - 1);
			return datasrc;
		}

	// 从CSV文件中加载数据，用给定格式校验，但设置classIndex
	/* (non-Javadoc)
	 * @see yueyueGo.datasource.GeneralDataSupplier#loadDataFromExtCSVFile(java.lang.String, java.lang.String[])
	 */
	@Override
	public  GeneralInstances loadDataFromExtCSVFile(String fileName,String[] verifyFormat)	throws Exception {
			CSVLoader loader = new CSVLoader();
			loader.setSource(new File(fileName));
			GeneralInstances datasrc = new WekaInstances(loader.getDataSet());
	
			// 对读入的数据字段名称校验 确保其顺序完全和内部训练的arff格式一致
			datasrc=ArffFormat.validateAttributeNames(datasrc,verifyFormat);
	
			
			//数据先作为String全部读进来之后再看怎么转nominal，否则直接加载， nominal的值的顺序会和文件顺序有关，造成数据不对
			String nominalAttribString=ArffFormat.findNominalAttribs(datasrc);
			BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(datasrc);
			datasrc= instanceProcessor.numToNominal(datasrc, nominalAttribString);
			// I do the following according to a saying from the weka forum:
			//"You can't add a value to a nominal attribute once it has been created. 
			//If you want to do this, you need to use a string attribute instead."
			datasrc=instanceProcessor.nominalToString(datasrc, nominalAttribString);
			
			return datasrc;
		}

	//取最新交易日的数据预测
	/* (non-Javadoc)
	 * @see yueyueGo.datasource.GeneralDataSupplier#LoadFullModelDataFromDB()
	 */
	@Override
	public GeneralInstances LoadFullModelDataFromDB(FullModelDataFormat formatFullModel) throws Exception{
		return LoadFullModelDataFromDB(null,formatFullModel);
	}

	//如果传入的参数 dateString==null; 则取数据库中的最新数据，否则取相应交易日的数据
	private GeneralInstances LoadFullModelDataFromDB(String dateString,FullModelDataFormat formatFullModel) throws Exception{
		String[] validateFormat=formatFullModel.m_daily_data_to_predict_format;
	
		//load data from database that needs predicting
		InstanceQuery query = new InstanceQuery();
		query.setDatabaseURL(EnvConstants.URL);
		query.setUsername(EnvConstants.USER);
		query.setPassword(EnvConstants.PASSWORD);
		String queryData=generateFullModelQueryData(dateString,formatFullModel);
		query.setQuery(queryData); 
		GeneralInstances data = new WekaInstances(query.retrieveInstances());
	
		
		//读入数据后最后一行加上为空的收益率
		data = InstanceHandler.getHandler(data).AddAttribute(data, ArffFormat.SHOUYILV,data.numAttributes());
		// 对读入的数据字段名称校验 确保其顺序完全和内部训练的arff格式一致
		data=ArffFormat.validateAttributeNames(data,validateFormat);
		//全部读进来之后再转nominal，这里读入的数据可能只是子集，所以nominal的index值会不对，所以后续会用calibrateAttributes处理
		String nominalAttribString=ArffFormat.findNominalAttribs(data);
		data=InstanceHandler.getHandler(data).numToNominal(data, nominalAttribString);//"2,48-56");
	
	
		data.setClassIndex(data.numAttributes()-1);
		System.out.println("records loaded from database: "+data.numInstances());
		return data;
	}

	//如果传入的参数 dateString==null; 则取数据库中的最新数据，否则取相应交易日的数据
	private String generateFullModelQueryData(String dateString,FullModelDataFormat formatFullModel){
		
		String queryData="SELECT ";
		String[] target_columns=null;
		String target_view=null;
		String date_cretiriaString="";
		
		target_columns=formatFullModel.m_daily_data_to_predict_format;
	
		//如果传入的参数 dateString==null; 则取数据库中的最新数据，否则取相应交易日的数据
		if (dateString==null){
			target_view="v_stock_increment_optional_zuixin_group9";//"t_stock_avgline_increment_zuixin_group8_optional";//"t_stock_avgline_increment_zuixin_group5_optional";//"t_stock_avgline_increment_zuixin_group4_optional";
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

	private String generateQueryData(int format,ArffFormat arffFormat){
		
		String queryData="SELECT ";
		String[] target_columns=null;
		String target_view=null;
		
		switch (format) {
		case ArffFormat.LEGACY_FORMAT:
			target_columns=arffFormat.m_daily_data_to_predict_format_legacy;
			target_view="t_stock_avgline_increment_zuixin_group10";//"t_stock_avgline_increment_zuixin_group5";//"t_stock_avgline_increment_zuixin_v";
			break;
		case ArffFormat.CURRENT_FORMAT:
			target_columns=arffFormat.m_daily_data_to_predict_format;
			target_view= "v_stock_increment_avgline_zuixin_group11";
			//"t_stock_avgline_increment_zuixin_group6";  //"t_stock_avgline_increment_zuixin_group5"; //"t_stock_avgline_increment_zuixin_group5_test";
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

	/* 
	 * 利用Weka的数据库加载函数创建Arff
	 * Weka的这个函数，会根据数据库字段的定义自动转换Arff类型（比如数据库里是string，则读出来就是nominal）
	 * @see yueyueGo.datasource.GeneralDataSupplier#LoadDataFromDB(int)
	 */
	@Override
	public GeneralInstances LoadDataFromDB(int format,ArffFormat arffFormat) throws Exception{
	
		String[] validateFormat=null;
		switch (format) {
		case ArffFormat.LEGACY_FORMAT:
			validateFormat=arffFormat.m_daily_data_to_predict_format_legacy;
			break;
		case ArffFormat.CURRENT_FORMAT:
			validateFormat=arffFormat.m_daily_data_to_predict_format;
			break;			
		default:
			break;
		}	
		//load data from database that needs predicting
		InstanceQuery query = new InstanceQuery();
		query.setDatabaseURL(EnvConstants.URL);
		query.setUsername(EnvConstants.USER);
		query.setPassword(EnvConstants.PASSWORD);
		String queryData=generateQueryData(format,arffFormat);
		query.setQuery(queryData); 
		GeneralInstances data = new WekaInstances(query.retrieveInstances()); 
	
	
		//读入数据后最后一行加上为空的收益率
		data = InstanceHandler.getHandler(data).AddAttribute(data, ArffFormat.SHOUYILV,data.numAttributes());
		// 对读入的数据字段名称校验 确保其顺序完全和内部训练的arff格式一致
		data=ArffFormat.validateAttributeNames(data,validateFormat);

		

		//全部读进来之后再转nominal，这里读入的数据可能只是子集，所以nominal的index值会不对，所以后续会用calibrateAttributes处理
		String nominalAttribString=ArffFormat.findNominalAttribs(data);
		BaseInstanceProcessor instanceProcessor=InstanceHandler.getHandler(data);
		data=instanceProcessor.numToNominal(data, nominalAttribString);
		// I do the following according to a saying from the weka forum:
		//"You can't add a value to a nominal attribute once it has been created. 
		//If you want to do this, you need to use a string attribute instead."
		data=instanceProcessor.nominalToString(data, nominalAttribString);
	
//		//对数据库取出来的值，我们要检查一下是不是所有的Nominal都已在arffFormat中定义，否则转换回numeric
//		//Weka的这个函数，会根据数据库字段的定义自动转换Arff类型（比如数据库里是string，则读出来就是nominal）
//		String[] nominalIndexArray=nominalAttribString.split(",");
//		//构建查找nominalIndex的数组
//		int[] nominalIndexes=new int[nominalIndexArray.length];
//		for (int i=0;i<nominalIndexArray.length;i++){
//			nominalIndexes[i]=Integer.parseInt(nominalIndexArray[i]);
//			nominalIndexes[i]--; //因为在wekaInstances的定义中，index是从0开始的，而上面查找的是从1开始，所以这里要再减1
//		}
//
//		ArrayList<GeneralAttribute> attributeList=data.getAttributeList();
//		for (GeneralAttribute generalAttribute : attributeList) {
//			if (generalAttribute.isNominal()==true){ //对于传入数据的Nominal字段，检查ArffFormat 的定义
//				if (Arrays.binarySearch(nominalIndexes, generalAttribute.index())<0){
//					//如果ArffFormat未定义，则将其转换为numeric
//					System.out.println("converting attribute["+generalAttribute.name()+"] to numberic, because it is not Nominal Attributes defined in Arffformat");
//					for (int i=0;i<data.numInstances();i++){
//						
//					}
//
//				}
//			}
//		}

	
		data.setClassIndex(data.numAttributes()-1);
		System.out.println("records loaded from database: "+data.numInstances());
		return data;
	}

}
