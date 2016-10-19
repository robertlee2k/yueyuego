package yueyueGo.datasource;

import yueyueGo.databeans.GeneralInstances;

public interface GeneralDataSupplier {

	public final static String URL = "jdbc:mysql://uts.simu800.com/develop?characterEncoding=utf8&autoReconnect=true";
	public final static String USER = "root";
	public final static String PASSWORD = "data@2014";

	// load full set of data
	public abstract GeneralInstances loadDataFromFile(String fileName)
			throws Exception;

	// 从CSV文件中加载数据，不做任何特殊操作及后续处理
	public abstract GeneralInstances loadNormalCSVFile(String fileName)
			throws Exception;

	// 从CSV文件中加载数据，用给定格式校验，并设置classIndex
	public abstract GeneralInstances loadDataWithFormatFromCSVFile(
			String fileName, String[] format) throws Exception;

	// 从CSV文件中加载数据，用给定格式校验，但设置classIndex
	public abstract GeneralInstances loadDataFromExtCSVFile(String fileName,
			String[] verifyFormat) throws Exception;

	//取最新交易日的数据预测
	public abstract GeneralInstances LoadFullModelDataFromDB() throws Exception;

	public abstract GeneralInstances LoadDataFromDB(int format)
			throws Exception;

}