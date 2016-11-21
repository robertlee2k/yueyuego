package yueyueGo.datasource;

import yueyueGo.databeans.GeneralInstances;

public interface GeneralDataSupplier {

	// 从指定文件里加载全部的数据，并将数据格式处理成算法需要的形式
	public abstract GeneralInstances loadDataFromFile(String fileName)	throws Exception;

	// 从CSV文件中加载数据，不做任何特殊操作及后续处理
	public abstract GeneralInstances loadNormalCSVFile(String fileName)	throws Exception;

	// 从CSV文件中加载数据，用给定格式校验，并设置classIndex
	public abstract GeneralInstances loadDataWithFormatFromCSVFile(
			String fileName, String[] format) throws Exception;

	// 从CSV文件中加载扩展数据，用给定格式校验，并设置classIndex（这是用于未来增加数据列数的）
	public abstract GeneralInstances loadDataFromExtCSVFile(String fileName,
			String[] verifyFormat) throws Exception;

	//取最新的交易日短线策略数据（用于每日预测）
	public abstract GeneralInstances LoadFullModelDataFromDB() throws Exception;
	
	//取最新的交易日均线策略数据（用于每日预测）
	public abstract GeneralInstances LoadDataFromDB(int format)	throws Exception;

}