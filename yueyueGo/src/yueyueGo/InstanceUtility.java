package yueyueGo;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToString;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.SubsetByExpression;

public class InstanceUtility {
	public static final String WEKA_ATT_PREFIX = "ATT";

	// 转换numeric为nominal
	public static Instances numToNominal(Instances data, String attribPos)
			throws Exception {
		String[] options = new String[2];
		options[0] = "-R"; // "range"
		options[1] = attribPos; // attribute position
		NumericToNominal convert = new NumericToNominal();
		convert.setOptions(options);
		convert.setInputFormat(data);
		Instances newData = Filter.useFilter(data, convert); // apply filter
		return newData;
	}
	
	// 转换Nominal为String
	public static Instances NominalToString(Instances data, String attribPos)
			throws Exception {
		String[] options = new String[2];
		options[0] = "-C"; // "range"
		options[1] = attribPos; // attribute position
		NominalToString convert = new NominalToString();
		convert.setOptions(options);
		convert.setInputFormat(data);
		Instances newData = Filter.useFilter(data, convert); // apply filter
		return newData;
	}
	
	
	//删除指定的列（此处的index是从1开始）	
	public static Instances removeAttribs(Instances data, String attribPos) throws Exception{
		return removeAttribs(data,attribPos,false);
	}
	
	//保留指定的列，删除其他列（此处的index是从1开始）	
	public static Instances filterAttribs(Instances data, String attribPos) throws Exception{
		return removeAttribs(data,attribPos,true);
	}
	
	// 实际处理的方法
	private static Instances removeAttribs(Instances data, String attribPos, boolean invert)
			throws Exception {
		String[] options = new String[2];
		options[0] = "-R"; // "range"
		options[1] = attribPos; // first attribute
		Remove remove = new Remove(); // new instance of filter
		remove.setOptions(options); // set options
		remove.setInvertSelection(invert);
		remove.setInputFormat(data); // inform filter about dataset **AFTER** setting options
		Instances newData = Filter.useFilter(data, remove); // apply filter
		return newData;
	}

	
	// 根据给定公式，获取数据集的子集
	public static Instances getInstancesSubset(Instances data, String expression)
			throws Exception {
		//System.out.println(" get Instances subset using expression: "+expression);
		SubsetByExpression subset = new SubsetByExpression();
		String[] options = new String[2];
		options[0] = "-E"; // "range"
		options[1] = expression; // attribute position
		subset.setOptions(options);
		subset.setInputFormat(data);
		Instances output = Filter.useFilter(data, subset);
		return output;
	}

	
	//在position的位置插入新的属性 （position从0开始） ，这个方法会创建新的instances后再插入，所以似乎可以直接调用原有instances中的insertAttributeAt方法
	public static Instances AddAttribute(Instances data, String attributeName,
			int position) {
		Instances newData = new Instances(data);
		newData.insertAttributeAt(new Attribute(attributeName), position);
		return newData;
	}
	



	
	// 将给定记录集中属于特定指数的数据筛选出来
	public static Instances filterDataForIndex(Instances origin, String indexName) throws Exception{
		//找出所属指数的位置
		int pos = findATTPosition(origin,indexName);
		return getInstancesSubset(origin,WEKA_ATT_PREFIX+pos+" is '"+ ArffFormat.VALUE_YES+"'");
	}
	
	// 找到指定数据集中属性所处位置（从1开始）
	public static int findATTPosition(Instances origin,String attName) {
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
	public static void copyToNewInstance(Instance leftCurr, Instance newData,
			int srcStartIndex, int srcEndIndex,int targetStartIndex) throws IllegalStateException {
		for (int n = srcStartIndex; n <= srcEndIndex; n++) { 
			Attribute att = leftCurr.attribute(n);
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
	public static void calibrateAttributes(Instances input,
			Instances output) throws Exception, IllegalStateException {
		InstanceUtility.compareInstancesFormat(input,output);
		
		for (int m=0; m<input.numInstances();m++){
			Instance inst=new DenseInstance(output.numAttributes());
			inst.setDataset(output);
			for (int n = 0; n < input.numAttributes() ; n++) { 
				Attribute incomingAtt = input.attribute(n);
				Attribute formatAtt=output.attribute(n);
				
				String formatAttName=formatAtt.name();
				String incomingAttName=incomingAtt.name();
				Instance curr=input.instance(m);
				if (formatAttName.equals(incomingAttName)){
					if (formatAtt.isNominal()) {
						String label = curr.stringValue(incomingAtt);
						if ("?".equals(label)){
							System.out.println("Attribute value is empty. value= "+ label+" @ "+ incomingAttName + " current ID ="+curr.value(0));
						}else {
							int index = formatAtt.indexOfValue(label);
							if (index != -1) {
								inst.setValue(n, index);
							}else{
								throw new Exception("Attribute value is invalid. value= "+ label+" @ "+ incomingAttName + " & "+ formatAttName+ " current ID ="+curr.value(0));
							}
						}
					} else if (formatAtt.isString()) {
						String label = curr.stringValue(incomingAtt);
						inst.setValue(n, label);
					} else if (formatAtt.isNumeric()) {
						inst.setValue(n, input.instance(m).value(incomingAtt));
					} else {
						throw new IllegalStateException("Unhandled attribute type!");
					}
				}else {
					throw new Exception("Attribute order error! "+ incomingAttName + " vs. "+ formatAttName);
				}
			}
			output.add(inst);
		}
	}

	/**
	 * 合并两个Instances集 ，此处的合并是纵向合并，两个instances需要是同样格式的 
	 * @param extData
	 * @param extDataSecond
	 * @return
	 * @throws IllegalStateException
	 */
	public static Instances mergeTwoInstances(Instances extData,
			Instances extDataSecond) throws IllegalStateException {
		//如果不是用这种copy的方式和setDataSet的方式，String和nominal数据会全乱掉。
		Instance oldRow=null;
		int colSize=extData.numAttributes()-1;
		for (int i=0;i<extDataSecond.numInstances();i++){
			Instance newRow=new DenseInstance(extData.numAttributes());
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
	public static void compareInstancesFormat(Instances test, Instances header) {
		String result=header.equalHeadersMsg(test);
		if (result!=null){
			System.err.println("attention! model and testing data structure is not the same. Here is the difference: "+result);
		}else {
			System.out.println("model and testing data structure compared");
		}
	}


}
