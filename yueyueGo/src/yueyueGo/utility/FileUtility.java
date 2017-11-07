package yueyueGo.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileUtility {
	public static void write(String path, String content, String encoding) throws IOException {
		File file = new File(path);
//		file.delete();
		// if file doesn't exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
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
	
//	public static void convertMyModelToWekaModel(String path,String modelFileName)
//			throws Exception {
//		
//		@SuppressWarnings("unchecked")
//		Vector<Object> v = (Vector<Object>) SerializationHelper.read(path+modelFileName);
//		Classifier model = (Classifier) v.get(0);
//		SerializationHelper.write(path+"WEKA-"+modelFileName, model);
//		
//	}
	
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

	/**
	 * @param newName
	 * @param legacyName
	 */
	public static void renameFile(String newName, String legacyName) {
		System.out.println("rename file from="+legacyName+" to="+newName);
		// Old file  
		File oldFile = new File(legacyName);  
		// new file  
		File newFile = new File(newName);  
		boolean flag = oldFile.renameTo(newFile);  
		if (flag) {  
			System.out.println("File renamed successfully");  
		} else {  
			System.out.println("Rename operation failed");  
		}
	}
	
}
