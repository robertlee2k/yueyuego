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
import weka.core.SerializationHelper;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils.DataSource;
import yueyueGo.ArffFormat;
import yueyueGo.databeans.BaseInstances;
import yueyueGo.databeans.WekaInstances;

public class FileUtility {
	// load full set of data
	public static BaseInstances loadDataFromFile(String fileName)
			throws Exception {
		DataSource source = new DataSource(fileName); 
		BaseInstances data = new WekaInstances(source.getDataSet());

		// setting class attribute if the data format does not provide this
		// information
		if (data.classIndex() == -1)
		  data.setClassIndex(data.numAttributes() - 1);
		return data;
	}
	
	// 从CSV文件中加载数据，不做任何特殊操作及后续处理
	public static BaseInstances loadNormalCSVFile(String fileName)
			throws Exception {
		CSVLoader loader = new CSVLoader();
		loader.setSource(new File(fileName));
		BaseInstances datasrc = new WekaInstances(loader.getDataSet());
		return datasrc;
	}

	
	// 从CSV文件中加载数据，用给定格式校验，并设置classIndex
	public static BaseInstances loadDataWithFormatFromCSVFile(String fileName, String[] format)
				throws Exception {
			BaseInstances datasrc=loadDataFromExtCSVFile(fileName, format); 
			if (datasrc.classIndex() == -1)
				  datasrc.setClassIndex(datasrc.numAttributes() - 1);
			return datasrc;
		}
	
	// 从CSV文件中加载数据，用给定格式校验，但设置classIndex
	public static BaseInstances loadDataFromExtCSVFile(String fileName,String[] verifyFormat)	throws Exception {
			CSVLoader loader = new CSVLoader();
			loader.setSource(new File(fileName));
			BaseInstances datasrc = new WekaInstances(loader.getDataSet());

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
	
	public static void SaveDataIntoFile(BaseInstances dataSet, String fileName) throws IOException {

		ArffSaver saver = new ArffSaver();
		saver.setInstances(WekaInstances.convertToWekaInstances(dataSet));
		saver.setFile(new File(fileName));
		saver.writeBatch();

	}
	public static void saveCSVFile(BaseInstances data, String fileName) throws IOException {
		CSVSaver saver = new CSVSaver();
		saver.setInstances(WekaInstances.convertToWekaInstances(data));
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
