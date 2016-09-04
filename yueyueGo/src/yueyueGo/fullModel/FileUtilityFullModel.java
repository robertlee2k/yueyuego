package yueyueGo.fullModel;

import weka.core.Instances;
import yueyueGo.utility.FileUtility;

public class FileUtilityFullModel extends FileUtility {
	// 从增量的fullmodel交易CSV文件中加载数据
	protected static Instances loadDataFromFullModelCSVFile(String fileName) throws Exception{ 
		return loadDataWithFormatFromCSVFile(fileName,ArffFormatFullModel.FULL_MODEL_DATA_FORMAT_NEW);
	}
}
