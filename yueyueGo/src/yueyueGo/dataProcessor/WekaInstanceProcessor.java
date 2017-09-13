package yueyueGo.dataProcessor;

import java.util.ArrayList;

import weka.core.AttributeStats;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.AddUserFields;
import weka.filters.unsupervised.attribute.NominalToString;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToNominal;
import weka.filters.unsupervised.instance.SubsetByExpression;
import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.databeans.GeneralAttribute;
import yueyueGo.databeans.GeneralInstance;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaAttribute;
import yueyueGo.databeans.WekaInstance;
import yueyueGo.databeans.WekaInstances;
import yueyueGo.datasource.DataIOHandler;
import yueyueGo.utility.FormatUtility;

public class WekaInstanceProcessor extends BaseInstanceProcessor {
	public static final String WEKA_ATT_PREFIX = "ATT";

	// 转换numeric为nominal
	@Override
	public  GeneralInstances numToNominal(GeneralInstances data, String attribPos)
			throws Exception {
		String[] options = new String[2];
		options[0] = "-R"; // "range"
		options[1] = attribPos; // attribute position
		NumericToNominal convert = new NumericToNominal();
		convert.setOptions(options);
		Instances wdata=WekaInstances.convertToWekaInstances(data);
		convert.setInputFormat(wdata);
		Instances newData = Filter.useFilter(wdata, convert); // apply filter
		return new WekaInstances(newData);
	}
	
	// 转换Nominal为String
	@Override
	public  GeneralInstances nominalToString(GeneralInstances data, String attribPos)
			throws Exception {
		String[] options = new String[2];
		options[0] = "-C"; // "range"
		options[1] = attribPos; // attribute position
		NominalToString convert = new NominalToString();
		convert.setOptions(options);
		Instances wdata=WekaInstances.convertToWekaInstances(data);
		convert.setInputFormat(wdata);
		Instances newData = Filter.useFilter(wdata, convert); // apply filter
		return new WekaInstances(newData);
	}
	
	// 转换String为Nominal
	@Override
	public  GeneralInstances stringToNominal(GeneralInstances data, String attribPos)
			throws Exception {
		String[] options = new String[2];
		options[0] = "-R"; // "range"
		options[1] = attribPos; // attribute position
		StringToNominal convert = new StringToNominal();
		convert.setOptions(options);
		Instances wdata=WekaInstances.convertToWekaInstances(data);
		convert.setInputFormat(wdata);
		Instances newData = Filter.useFilter(wdata, convert); // apply filter
		return new WekaInstances(newData);
	}
	
	//删除指定的列（此处的index是从1开始）	
	@Override
	public  GeneralInstances removeAttribs(GeneralInstances data, String attribPos) throws Exception{
		return removeAttribs(data,attribPos,false);
	}
	
	//保留指定的列，删除其他列（此处的index是从1开始）	
	@Override
	public  GeneralInstances filterAttribs(GeneralInstances data, String attribPos) throws Exception{
		return removeAttribs(data,attribPos,true);
	}
	
	// 根据给定公式，获取数据集的子集
	@Override
	public  GeneralInstances getInstancesSubset(GeneralInstances data, String expression)
			throws Exception {
		//System.out.println(" get Instances subset using expression: "+expression);
		try {
			SubsetByExpression subset = new SubsetByExpression();
			String[] options = new String[2];
			options[0] = "-E"; // "range"
			options[1] = expression; // attribute position
			subset.setOptions(options);
			Instances wdata=WekaInstances.convertToWekaInstances(data);
			subset.setInputFormat(wdata);
			Instances output = Filter.useFilter(wdata, subset);
			return new WekaInstances(output);
		}catch(Exception e){
			System.err.println("error when getting subset using expression :"+expression);
			throw e;
		}

	}

	
	//在position的位置插入新的属性 （position从0开始） 
	//这个方法会改变原有的instances。
	@Override
	public GeneralInstances AddAttribute(GeneralInstances data, String attributeName,
			int position) {
//		Instances newData = new Instances(data);
		data.insertAttributeAt(new WekaAttribute(attributeName), position);
		return data;
	}
	
	
	//将属性改名 
	@Override
	public GeneralInstances renameAttribute(GeneralInstances data, String attributeName,String newName) {
		data.renameAttribute(data.attribute(attributeName), newName);
		return data;
	}
	
	//在position的位置插入新的属性 （position从0开始） 
	//这个方法会改变原有的instances。
	@Override
	public  GeneralInstances AddAttributeWithValue(GeneralInstances data, String attributeName,String type,String value) throws Exception {
		AddUserFields filter=new AddUserFields();
		String[] options = new String[2];
		options[0] = "-A"; 
		options[1] = attributeName+"@"+type+"@"+value; 
		filter.setOptions(options);
		Instances wdata=WekaInstances.convertToWekaInstances(data);
		filter.setInputFormat(wdata);
		Instances output=Filter.useFilter(wdata, filter);
		return new WekaInstances(output);
		
	}



	
	// 将给定记录集中属于特定指数的数据筛选出来
	@Override
	public GeneralInstances filterDataForIndex(GeneralInstances origin, String indexName) throws Exception{
		//找出所属指数的位置
		int pos = findATTPosition(origin,indexName);
		return getInstancesSubset(origin,WEKA_ATT_PREFIX+pos+" is '"+ ArffFormat.VALUE_YES+"'");
	}
	
	/**
	 * read all data from input , based on format  
	 * add them to the output instances using output format
	 */
	@Override
	public GeneralInstances calibrateAttributes(GeneralInstances input, GeneralInstances format,boolean convertNominalToNumeric)
				throws Exception{		
		
		GeneralInstances output;
		
		if (convertNominalToNumeric==false){ 
			//如果无须转换nominal，则直接使用format的格式
			output=new WekaInstances(format,0);
		}else{ 
			//如果需要转换nominal成numeric，则需要将format里的所有Nominal Attribute改为numberic
			ArrayList<GeneralAttribute> originalAttributes=format.getAttributeList();
			ArrayList<GeneralAttribute> outputAttributes=new ArrayList<GeneralAttribute>();
			for (GeneralAttribute originalAttribute : originalAttributes) {
				WekaAttribute newAttribute;
				if (originalAttribute.isNominal()==true){
					//如果需要转换Nominal，创建一个同名的新的numberic变量
					newAttribute=new WekaAttribute(originalAttribute.name());
				}else{
					//如果无须转换nominal 直接拷贝
					newAttribute=((WekaAttribute)originalAttribute).copy();
				}
				outputAttributes.add(newAttribute);
			}
			//用新的AttributeList创建新的output
			output=new WekaInstances(format.relationName(), outputAttributes, 0);
			
		}
		//比较一下input和output（主要是看看有没有不一致的名字）
		String compareResult=compareInstancesFormat(input,output);	
		if (compareResult!=null){
			System.out.println("input and output structure difference in Caliberation process is:"+compareResult);
		}
		System.out.println("start to calibrate Attributes....");	
		
		//遍历每一行
		for (int m=0; m<input.numInstances();m++){
			WekaInstance copyTo=new WekaInstance(output.numAttributes());
			copyTo.setDataset(output);
			GeneralInstance copyFrom=input.instance(m);
			//遍历每一列
			for (int n = 0; n < copyFrom.numAttributes() ; n++) { 
				GeneralAttribute copyFromAtt =copyFrom.attribute(n);
				GeneralAttribute copyToAtt=copyTo.attribute(n);
				if (convertNominalToNumeric==false){ 
					//如果无须转换nominal，则直接拷贝
					fullCopyAttribute(copyFrom, copyTo, copyFromAtt, copyToAtt,null);
				}else{
					//须转换nominal，则需要把formatAtt传给拷贝函数
					GeneralAttribute formatAtt =format.attribute(n);
					fullCopyAttribute(copyFrom, copyTo, copyFromAtt, copyToAtt,formatAtt);
					
				}
			}
			output.add(copyTo);
		}
		return output;
	}

	/**
	 * 合并两个Instances集 ，此处的合并是纵向合并，两个instances需要是同样格式的 
	 * @param extData
	 * @param extDataSecond
	 * @return
	 * @throws IllegalStateException
	 */
	@Override
	public GeneralInstances mergeTwoInstances(GeneralInstances extData,
			GeneralInstances extDataSecond) throws IllegalStateException {
		//如果不是用这种copy的方式和setDataSet的方式，String和nominal数据会全乱掉。
		GeneralInstance oldRow=null;
		int colSize=extData.numAttributes()-1;
		for (int i=0;i<extDataSecond.numInstances();i++){
			WekaInstance newRow=new WekaInstance(extData.numAttributes());
			newRow.setDataset(extData);
			oldRow=extDataSecond.instance(i);
			copyToNewInstance(oldRow,newRow,0,colSize,0);
			extData.add(newRow);
		}
	
		return extData;
	}

	//如果输入instances中的包含有string[]所定义的attributes，将其保留，将其他的属性删除。
	@Override
	public GeneralInstances filterAttribs(GeneralInstances incomingData, String[] attributeToKeep) throws Exception{
		String saveString=returnAttribsPosition(incomingData,attributeToKeep);
		GeneralInstances result = filterAttribs(incomingData,saveString);
		return result;
	}

	//如果输入instances中的包含有string[]所定义的attributes，将其删除，将其他的属性保留。
	@Override
	public GeneralInstances removeAttribs(GeneralInstances incomingData, String[] attributeToRemove) throws Exception{
		String removeString=returnAttribsPosition(incomingData,attributeToRemove);
		if (removeString==null){
			System.out.println("Warning! found nothing to remove from the attributesToRemove, returning the original dataset");
			return incomingData;
		}else{
			return removeAttribs(incomingData,removeString,false);
		}
	}

	private GeneralInstances removeAttribs(GeneralInstances data, String attribPos, boolean invert)
			throws Exception {
				String[] options = new String[2];
				options[0] = "-R"; // "range"
				options[1] = attribPos; // first attribute
				Remove remove = new Remove(); // new instance of filter
				remove.setOptions(options); // set options
				remove.setInvertSelection(invert);
				Instances wdata=WekaInstances.convertToWekaInstances(data);
				remove.setInputFormat(wdata); // inform filter about dataset **AFTER** setting options
				Instances newData = Filter.useFilter(wdata, remove); // apply filter
				return new WekaInstances(newData);
			}

	/*
	 * 分析数据文件的每一个属性
	 */
	public static void analyzeDataAttributes(String arffFile) throws Exception{
		
		System.out.println("start to load Arff File from File: "+ arffFile  );
		GeneralInstances fullSetData =DataIOHandler.getSuppier().loadDataFromFile(arffFile);
		System.out.println("finish loading fullset Data. row : "+ fullSetData.numInstances() + " column:"+ fullSetData.numAttributes());
		
		Instances  data=WekaInstances.convertToWekaInstances(fullSetData);
		
		for (int index=0; index<data.numAttributes();index++){
			String name=data.attribute(index).name();
			AttributeStats status= data.attributeStats(index);
			System.out.println("====output attribute status for attribute: "+name + " @Column "+(index+1));
			System.out.print(FormatUtility.printAttributeStatus(status));
			System.out.println("====end of attribute status for attribute: "+name + " @Column "+(index+1));
			System.out.println("");
		}
		
		
	}
}
