package yueyueGo.dataProcessor;

import yueyueGo.databeans.GeneralAttribute;
import yueyueGo.databeans.GeneralInstance;
import yueyueGo.databeans.GeneralInstances;

public abstract class BaseInstanceProcessor {

	//如果string[]所定义的属性在将数据集中存在，将其删除（不存在的话不报错）
	public abstract GeneralInstances removeAttribs(GeneralInstances incomingData, String[] attributeToRemove)
			throws Exception;
	
	//删除数据集中指定位置的属性列集合
	public abstract GeneralInstances removeAttribs(GeneralInstances data, String attribPos)
			throws Exception;
	
	//如果string[]所定义的属性在将数据集中存在，将其保留，将其他属性删除（不存在的话不报错）
	public abstract GeneralInstances filterAttribs(GeneralInstances incomingData, String[] attributeToKeep)
			throws Exception;
	
	
	//保留数据集中指定位置的属性列集合，将其他的属性删除
	public abstract GeneralInstances filterAttribs(GeneralInstances data, String attribPos)
			throws Exception;

	
	//将两个数据集合并为一个（纵向合并）
	public abstract GeneralInstances mergeTwoInstances(GeneralInstances extData, GeneralInstances extDataSecond)
			throws IllegalStateException;

	//	从input中读取数据， 按照output的数据格式进行转换和校验，然后把数据输出到output中
	//	* output instances will be changed in this method!
	public abstract GeneralInstances calibrateAttributes(GeneralInstances input, GeneralInstances format,boolean convertNominalToNumeric)
			throws Exception;

	// 将给定记录集中名称为indexName的数据中等于1的数据筛选出来（这个主要用于筛选属于某种指数的数据）
	public abstract GeneralInstances filterDataForIndex(GeneralInstances origin, String indexName)
			throws Exception;
	
	//在数据集中插入新的属性 （缺省应该在倒数第二位上插入） 并返回新的数据集
	public abstract GeneralInstances AddAttributeWithValue(GeneralInstances data, String attributeName,
			String type, String value) throws Exception;

	//在数据集的指定位置上插入新的属性 （position计数从0开始），并返回新的数据集
	public abstract GeneralInstances AddAttribute(GeneralInstances data, String attributeName, int position);

	//将属性改名 
	public abstract GeneralInstances renameAttribute(GeneralInstances data, String attributeName,String newName);
	
	// 根据给定表达式公式，获取数据集的子集并返回
	public abstract GeneralInstances getInstancesSubset(GeneralInstances data, String expression)
			throws Exception;

	//将数据集中给定位置的枚举属性转为String类型（位置用,分割）
	public abstract GeneralInstances nominalToString(GeneralInstances data, String attribPos)
			throws Exception;
	
	//将数据集中给定位置的String属性转为Nomimal类型（位置用,分割）
	public abstract GeneralInstances stringToNominal(GeneralInstances data, String attribPos) throws Exception;
	
	//将数据集中给定位置的数值属性转为枚举类型（位置用,分割）
	public abstract GeneralInstances numToNominal(GeneralInstances data, String attribPos)
			throws Exception;
	
	
	public static int findATTPosition(GeneralInstances origin, String attName) {
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
	public static  void copyToNewInstance(GeneralInstance leftCurr, GeneralInstance newData,
			int srcStartIndex, int srcEndIndex, int targetStartIndex) throws IllegalStateException {
				for (int n = srcStartIndex; n <= srcEndIndex; n++) { 
					GeneralAttribute att = leftCurr.attribute(n);
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

	/*
	 * 将CopyFrom里copyFromAtt数据，拷贝到CopyToAtt中。
	 * 1. 如果format为空，则照常拷贝，如果CopyToAtt是nominal字段，要取出StringValue做转换 
	 * 2. 如果format不为空，表示需要转换nominal的值为numberic，则将copyFromAtt的值根据从format中取Nominal的index值转换至copyTo中
	 * 这个目标是为了和TensorFlow以及DL4J保持一致， 因为这两个框架中没有Nominal的类型
	 */
	public static void fullCopyAttribute(GeneralInstance copyFrom, GeneralInstance copyTo,
			GeneralAttribute copyFromAtt, GeneralAttribute copyToAtt,GeneralAttribute formatAtt) throws Exception {
				String copyFromAttName=copyFromAtt.name();
				String copyToAttName=copyToAtt.name();
			
				if (copyToAttName.equals(copyFromAttName)){
					if (copyToAtt.isNominal()) {
						if (formatAtt!=null){ 
							//如果传入的FormatAtt存在，那这说明有错误（因为我们必须要转换Nominal成Numeric）
							throw new Exception("Target Attribute should NOT be Norminal here because formatAtt exists, formatAttName="+ formatAtt.name() + " @ "+ copyToAttName+ " current ID ="+copyFrom.value(0));							
						}
						//这时候copyToAtt就是自身的format
						copyStringAttribute(copyFrom, copyTo, copyFromAtt, copyToAtt, copyToAtt, copyFromAttName,copyToAttName);
					} else if (copyToAtt.isString()) { 
						String label = copyFrom.stringValue(copyFromAtt);
						copyTo.setValue(copyToAtt, label);
					} else if (copyToAtt.isNumeric()) {
						if (formatAtt==null){ //原样拷贝的程序无须做转换
							copyTo.setValue(copyToAtt, copyFrom.value(copyFromAtt));
						}else{
							if (formatAtt.isNominal()){ //格式属性里这个是Nominal，需要转换
								//说明这里要nominalToNumeric
								String formatAttName=formatAtt.name();
								if (copyToAttName.equals(formatAttName)==false){
									throw new Exception("Attribute order error! data target Attribute="+ copyToAttName + " vs. format Attribute"+ formatAttName);
								}
								//将copyFromAtt的值根据从format中取Nominal的index值转换至copyTo中
								copyStringAttribute(copyFrom, copyTo, copyFromAtt, copyToAtt, formatAtt, copyFromAttName,copyToAttName);
							}else{ //不是nominal的直接原样拷贝无须转换
								copyTo.setValue(copyToAtt, copyFrom.value(copyFromAtt));	
							}
						}
					} else {
						throw new IllegalStateException("Unhandled attribute type in caliberation process!");
					}
				}else {
					throw new Exception("Attribute order error! input data Attribute="+ copyFromAttName + " vs. output Attribute "+ copyToAttName);
				}
			}

	/**
	 * @param copyFrom
	 * @param copyTo
	 * @param copyFromAtt
	 * @param copyToAtt
	 * @param formatAtt
	 * @param copyFromAttName
	 * @param copyToAttName
	 * @throws Exception
	 */
	private static void copyStringAttribute(GeneralInstance copyFrom, GeneralInstance copyTo,
			GeneralAttribute copyFromAtt, GeneralAttribute copyToAtt, GeneralAttribute formatAtt,
			String copyFromAttName, String copyToAttName) throws Exception {
		String label =null;
		try{
			label=copyFrom.stringValue(copyFromAtt);
		}catch(Exception e){
			System.err.println("Cannot get String value for Attribute="+ copyFromAttName + " current ID ="+copyFrom.value(0));
			System.err.println("try to get value ="+ copyFrom.value(copyFromAtt));
			throw e;
		}
		if ("?".equals(label)){
			System.out.println("Attribute value is empty. value= "+ label+" @ "+ copyFromAttName + " current ID ="+copyFrom.value(0));
		}else {
			int index = formatAtt.indexOfValue(label);
			if (index != -1) {
				copyTo.setValue(copyToAtt, index);
			}else{
				throw new Exception("Attribute value is invalid. value= "+ label+" @ "+ copyFromAttName + " & "+ copyToAttName+ " current ID ="+copyFrom.value(0));
			}
		}
	}

	/**
	 * @param test
	 * @param header
	 */
	public static String compareInstancesFormat(GeneralInstances test, GeneralInstances header) {
		String result=header.equalHeadersMsg(test);
		return result;
	}

	public static String returnAttribsPosition(GeneralInstances data, String[] searchAttributes) {
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

	public BaseInstanceProcessor() {
		super();
	}



}