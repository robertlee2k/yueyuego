package yueyueGo.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils.DataSource;
import yueyueGo.ArffFormat;

public class FileUtility {
	// load full set of data
	public static Instances loadDataFromFile(String fileName)
			throws Exception {
		DataSource source = new DataSource(fileName); 
		Instances data = source.getDataSet();

		// setting class attribute if the data format does not provide this
		// information
		if (data.classIndex() == -1)
		  data.setClassIndex(data.numAttributes() - 1);
		return data;
	}
	
	// 从CSV文件中加载数据，不做任何特殊操作及后续处理
	public static Instances loadNormalCSVFile(String fileName)
			throws Exception {
		CSVLoader loader = new CSVLoader();
		loader.setSource(new File(fileName));
		Instances datasrc = loader.getDataSet();
		return datasrc;
	}

//	// 从文件中加载每天的预测数据（该方法不常用，仅限于数据库加载失败时使用）
//	public static Instances loadDailyNewDataFromCSVFile(String fileName)
//			throws Exception {
//		CSVLoader loader = new CSVLoader();
//		loader.setSource(new File(fileName));
//
//		
////		 永远别把数据文件里的ID变成Nominal的，否则读出来的ID就变成相对偏移量了
//		Instances datasrc = loader.getDataSet();
//
//		//读入数据后最后一行加上为空的收益率
//		datasrc = InstanceUtility.AddAttribute(datasrc, ArffFormat.SHOUYILV,datasrc.numAttributes());
//		// 对读入的数据校验以适应内部训练的arff格式
//		datasrc=ArffFormat.validateAttributeNames(datasrc,ArffFormat.DAILY_DATA_TO_PREDICT_FORMAT_NEW);
//
//		//全部读进来之后再转nominal，这里读入的数据可能只是子集，所以nominal的index值会不对，所以后续会用calibrateAttributes处理
//		String nominalAttribString=ArffFormat.findNominalAttribs(datasrc);
//		datasrc=InstanceUtility.numToNominal(datasrc, nominalAttribString);// "2,48-56";
//		
//		if (datasrc.classIndex() == -1)
//			  datasrc.setClassIndex(datasrc.numAttributes() - 1);
//		return datasrc;
//	}
	
	
	// 从CSV文件中加载数据，用给定格式校验，并设置classIndex
	public static Instances loadDataWithFormatFromCSVFile(String fileName, String[] format)
				throws Exception {
			Instances datasrc=loadDataFromExtCSVFile(fileName, format); 
			if (datasrc.classIndex() == -1)
				  datasrc.setClassIndex(datasrc.numAttributes() - 1);
			return datasrc;
		}
	
	// 从CSV文件中加载数据，用给定格式校验，但设置classIndex
	public static Instances loadDataFromExtCSVFile(String fileName,String[] verifyFormat)	throws Exception {
			CSVLoader loader = new CSVLoader();
			loader.setSource(new File(fileName));
			Instances datasrc = loader.getDataSet();

			// 对读入的数据字段名称校验 确保其顺序完全和内部训练的arff格式一致
			datasrc=ArffFormat.validateAttributeNames(datasrc,verifyFormat);

			
			//数据先作为String全部读进来之后再看怎么转nominal，否则直接加载， nominal的值的顺序会和文件顺序有关，造成数据不对
			String nominalAttribString=ArffFormat.findNominalAttribs(datasrc);
			datasrc=InstanceUtility.numToNominal(datasrc, nominalAttribString);
			// I do the following according to a saying from the weka forum:
			//"You can't add a value to a nominal attribute once it has been created. 
			//If you want to do this, you need to use a string attribute instead."
			datasrc=InstanceUtility.NominalToString(datasrc, nominalAttribString);
			
			return datasrc;
		}	
	
	public static void SaveDataIntoFile(Instances dataSet, String fileName) throws IOException {

		ArffSaver saver = new ArffSaver();
		saver.setInstances(dataSet);
		saver.setFile(new File(fileName));
		saver.writeBatch();

	}
	public static void saveCSVFile(Instances data, String fileName) throws IOException {
		CSVSaver saver = new CSVSaver();
		saver.setInstances(data);
		saver.setFile(new File(fileName));
		saver.writeBatch();
	}
	
	public static void write(String path, String content, String encoding) throws IOException {
		File file = new File(path);
		file.delete();
		file.createNewFile();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
		writer.write(content);
		writer.close();
	}

	public static String read(String path, String encoding) throws IOException {
		String content = "";
		File file = new File(path);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
		String line = null;
		while ((line = reader.readLine()) != null) {
			content += line + "\n";
		}
		reader.close();
		return content;
	}
	
	public static void convertMyModelToWekaModel(String path,String modelFileName)
			throws Exception {
		
		@SuppressWarnings("unchecked")
		Vector<Object> v = (Vector<Object>) SerializationHelper.read(path+modelFileName);
		Classifier model = (Classifier) v.get(0);
		SerializationHelper.write(path+"WEKA-"+modelFileName, model);
		
	}
	
	public static void mkdirIfNotExist(String pathName){ 
		File file =new File(pathName);    
		//如果文件夹不存在则创建    
		if  (!file.exists() && !file.isDirectory())      
		{       
			System.out.println("mkdirs for "+ pathName+" as it doesn't exist...");  
			file.mkdirs();    
		} else {  
			System.out.println("dir found at "+pathName+"  seems ok.");  
		} 
	}
}
