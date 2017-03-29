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
	public abstract void calibrateAttributes(GeneralInstances input, GeneralInstances output)
			throws Exception, IllegalStateException;

	// 将给定记录集中名称为indexName的数据中等于1的数据筛选出来（这个主要用于筛选属于某种指数的数据）
	public abstract GeneralInstances filterDataForIndex(GeneralInstances origin, String indexName)
			throws Exception;
	
	//在数据集中插入新的属性 （缺省应该在倒数第二位上插入） 并返回新的数据集
	public abstract GeneralInstances AddAttributeWithValue(GeneralInstances data, String attributeName,
			String type, String value) throws Exception;

	//在数据集的指定位置上插入新的属性 （position计数从0开始），并返回新的数据集
	public abstract GeneralInstances AddAttribute(GeneralInstances data, String attributeName, int position);

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

	public static void fullCopyInstance(GeneralInstance copyFrom, GeneralInstance copyTo)
			throws Exception {
				for (int n = 0; n < copyFrom.numAttributes() ; n++) { 
					GeneralAttribute copyFromAtt =copyFrom.attribute(n);
					GeneralAttribute copyToAtt=copyTo.attribute(n);
					fullCopyAttribute(copyFrom, copyTo, copyFromAtt, copyToAtt);
				}
			}

	public static void fullCopyAttribute(GeneralInstance copyFrom, GeneralInstance copyTo,
			GeneralAttribute copyFromAtt, GeneralAttribute copyToAtt) throws Exception {
				String copyFromAttName=copyFromAtt.name();
				String copyToAttName=copyToAtt.name();
			
				if (copyToAttName.equals(copyFromAttName)){
					if (copyToAtt.isNominal()) {
						String label =null;
						try{
							label=copyFrom.stringValue(copyFromAtt);
						}catch(Exception e){
							System.out.println("Cannot get String value for Attribute="+ copyFromAttName + " current ID ="+copyFrom.value(0));
							System.out.println("try to get value ="+ copyFrom.value(copyFromAtt));
							throw e;
						}
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
					throw new Exception("Attribute order error! data Attribute="+ copyFromAttName + " vs. format Attribute"+ copyToAttName);
				}
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