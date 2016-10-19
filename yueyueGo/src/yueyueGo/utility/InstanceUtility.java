package yueyueGo.utility;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.AddUserFields;
import weka.filters.unsupervised.attribute.NominalToString;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.SubsetByExpression;
import yueyueGo.ArffFormat;
import yueyueGo.databeans.GeneralAttribute;
import yueyueGo.databeans.GeneralInstance;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaAttribute;
import yueyueGo.databeans.WekaInstance;
import yueyueGo.databeans.WekaInstances;

public class InstanceUtility {
	public static final String WEKA_ATT_PREFIX = "ATT";

	// 转换numeric为nominal
	public static GeneralInstances numToNominal(GeneralInstances data, String attribPos)
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
	public static GeneralInstances NominalToString(GeneralInstances data, String attribPos)
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
	
	
	//删除指定的列（此处的index是从1开始）	
	public static GeneralInstances removeAttribs(GeneralInstances data, String attribPos) throws Exception{
		return removeAttribs(data,attribPos,false);
	}
	
	//保留指定的列，删除其他列（此处的index是从1开始）	
	public static GeneralInstances filterAttribs(GeneralInstances data, String attribPos) throws Exception{
		return removeAttribs(data,attribPos,true);
	}
	
	// 实际处理的方法
	private static GeneralInstances removeAttribs(GeneralInstances data, String attribPos, boolean invert)
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
	
	
	// 根据给定公式，获取数据集的子集
	public static GeneralInstances getInstancesSubset(GeneralInstances data, String expression)
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
	public static GeneralInstances AddAttribute(GeneralInstances data, String attributeName,
			int position) {
//		Instances newData = new Instances(data);
		data.insertAttributeAt(new WekaAttribute(attributeName), position);
		return data;
	}
	
	//在position的位置插入新的属性 （position从0开始） 
	//这个方法会改变原有的instances。
	public static GeneralInstances AddAttributeWithValue(GeneralInstances data, String attributeName,String type,String value) throws Exception {
//		AddUserFields.AttributeSpec aSpec=new AddUserFields.AttributeSpec();
//		aSpec.setName(attributeName);
//		aSpec.setType(type);
//		aSpec.setValue(value);
//		
//		Vector<AddUserFields.AttributeSpec> aSpecList=new Vector<AddUserFields.AttributeSpec>();
//		aSpecList.add(aSpec);
		AddUserFields filter=new AddUserFields();
//		filter.setAttributeSpecs(aSpecList);
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
	public static GeneralInstances filterDataForIndex(GeneralInstances origin, String indexName) throws Exception{
		//找出所属指数的位置
		int pos = findATTPosition(origin,indexName);
		return getInstancesSubset(origin,WEKA_ATT_PREFIX+pos+" is '"+ ArffFormat.VALUE_YES+"'");
	}
	
	// 找到指定数据集中属性所处位置（从1开始）
	public static int findATTPosition(GeneralInstances origin,String attName) {
		int pos=-1;
		for (int i=0;i<origin.numAttributes();i++){
			if (origin.attribute(i).name().equals(attName)){
				pos=i+1; // 找到指数所属第几个参数（从1开始），用于过滤时用
				break;
			}
		}
		return pos;
	}

	/**
	 * @param leftCurr
	 * @param newData
	 * @param startIndex
	 * @param endIndex
	 * @throws IllegalStateException
	 */
	public static void copyToNewInstance(GeneralInstance leftCurr, GeneralInstance newData,
			int srcStartIndex, int srcEndIndex,int targetStartIndex) throws IllegalStateException {
		for (int n = srcStartIndex; n <= srcEndIndex; n++) { 
			WekaAttribute att = (WekaAttribute)leftCurr.attribute(n);
			if (att != null) {
				if (att.isNominal()) {
					String label = leftCurr.stringValue(att);
					int index = att.indexOfValue(label);
					if (index != -1) {
						newData.setValue(targetStartIndex+n-srcStartIndex, index);
					} //这里如果left里面的数据没有值，也就不必设值了
				} else if (att.isNumeric()) {
					newData.setValue(targetStartIndex+n-srcStartIndex, leftCurr.value(att));
				} else if (att.isString()) {
					String label = leftCurr.stringValue(att);
					newData.setValue(targetStartIndex+n-srcStartIndex, label);
				} else {
					throw new IllegalStateException("Unhandled attribute type!");
				}
			}
		}
	}

	/**
	 * read all data from input , add them to the output instances using output format
	 * output instances will be changed in this method!
	 */
	public static void calibrateAttributes(GeneralInstances input,
			GeneralInstances output) throws Exception, IllegalStateException {
		InstanceUtility.compareInstancesFormat(input,output);
			
		System.out.println("start to calibrate Attributes");	
		
		for (int m=0; m<input.numInstances();m++){
			WekaInstance copyTo=new WekaInstance(output.numAttributes());
			copyTo.setDataset(output);
			GeneralInstance copyFrom=input.instance(m);
			fullCopyInstance(copyFrom,copyTo);
			output.add(copyTo);
		}
	}

	//perform full copy(with String and nominal attributes) from copyFrom to CopyTo
	public static void fullCopyInstance(GeneralInstance copyFrom,GeneralInstance copyTo)	throws Exception {
		for (int n = 0; n < copyFrom.numAttributes() ; n++) { 
			WekaAttribute copyFromAtt =(WekaAttribute) copyFrom.attribute(n);
			WekaAttribute copyToAtt=(WekaAttribute)copyTo.attribute(n);
			fullCopyAttribute(copyFrom, copyTo, copyFromAtt, copyToAtt);
		}
	}

	//perform full copy for one Attribute.
	public static void fullCopyAttribute(GeneralInstance copyFrom, GeneralInstance copyTo,
			GeneralAttribute copyFromAtt, GeneralAttribute copyToAtt) throws Exception {
		String copyFromAttName=copyFromAtt.name();
		String copyToAttName=copyToAtt.name();

		if (copyToAttName.equals(copyFromAttName)){
			if (copyToAtt.isNominal()) {
				String label = copyFrom.stringValue(copyFromAtt);
				if ("?".equals(label)){
					System.out.println("Attribute value is empty. value= "+ label+" @ "+ copyFromAttName + " current ID ="+copyFrom.value(0));
				}else {
					int index = copyToAtt.indexOfValue(label);
					if (index != -1) {
						copyTo.setValue(copyToAtt, index);
					}else{
						if ("0".equals(label) && "sw_zhishu_code".equals(copyFromAttName)){
							System.err.println("sw_zhishu_code is 0, leave it as blank"+ " current ID ="+copyFrom.value(0));
						}else{
							throw new Exception("Attribute value is invalid. value= "+ label+" @ "+ copyFromAttName + " & "+ copyToAttName+ " current ID ="+copyFrom.value(0));
						}
					}
				}
			} else if (copyToAtt.isString()) {
				String label = copyFrom.stringValue(copyFromAtt);
				copyTo.setValue(copyToAtt, label);
			} else if (copyToAtt.isNumeric()) {
				copyTo.setValue(copyToAtt, copyFrom.value(copyFromAtt));
			} else {
				throw new IllegalStateException("Unhandled attribute type!");
			}
		}else {
			throw new Exception("Attribute order error! "+ copyFromAttName + " vs. "+ copyToAttName);
		}
	}

	/**
	 * 合并两个Instances集 ，此处的合并是纵向合并，两个instances需要是同样格式的 
	 * @param extData
	 * @param extDataSecond
	 * @return
	 * @throws IllegalStateException
	 */
	public static GeneralInstances mergeTwoInstances(GeneralInstances extData,
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

	/**
	 * @param test
	 * @param header
	 */
	public static String compareInstancesFormat(GeneralInstances test, GeneralInstances header) {
		String result=header.equalHeadersMsg(test);
		if (result==null){
			System.out.println("model and testing data structure compared,everything is just the same");
		}
		return result;
	}

	//如果输入instances中的包含有string[]所定义的attributes，将其保留，将其他的属性删除。
	public static GeneralInstances keepAttributes(GeneralInstances incomingData, String[] attributeToKeep) throws Exception{
		String saveString=InstanceUtility.returnAttribsPosition(incomingData,attributeToKeep);
		GeneralInstances result = filterAttribs(incomingData,saveString);
		return result;
	}

	//如果输入instances中的包含有string[]所定义的attributes，将其删除，将其他的属性保留。
	public static GeneralInstances removeAttribs(GeneralInstances incomingData, String[] attributeToRemove) throws Exception{
		String removeString=InstanceUtility.returnAttribsPosition(incomingData,attributeToRemove);
		if (removeString==null){
			System.out.println("Warning! found nothing to remove from the attributesToRemove, returning the original dataset");
			return incomingData;
		}else{
			return removeAttribs(incomingData,removeString,false);
		}
	}

	//返回给定数据集里与searchAttribues内同名字段的位置字符串（从1开始），这主要是为filter使用
	public static String returnAttribsPosition(GeneralInstances data, String[] searchAttributes){
		String nominalAttribPosition=null;
		GeneralAttribute incomingAttribue=null;
		for (int i = 0; i < searchAttributes.length; i++) {
			incomingAttribue=data.attribute(searchAttributes[i]);
			if (incomingAttribue!=null){
				int pos=incomingAttribue.index()+1;//在内部的attribute index是0开始的
				if (nominalAttribPosition==null){ //找到的第一个
					nominalAttribPosition=new Integer(pos).toString(); 
				}else{
					nominalAttribPosition+=","+pos;
				}
			}
		}
		return nominalAttribPosition;
	}
}
