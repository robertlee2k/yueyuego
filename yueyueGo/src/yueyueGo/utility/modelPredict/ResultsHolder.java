package yueyueGo.utility.modelPredict;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import weka.core.SerializedObject;
import yueyueGo.AbstractModel;
import yueyueGo.NominalModel;
import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.dataProcessor.InstanceHandler;
import yueyueGo.databeans.DataInstances;
import yueyueGo.databeans.GeneralAttribute;
import yueyueGo.databeans.GeneralInstance;
import yueyueGo.databeans.GeneralInstances;


/*
 * 用于存放预测结果或选股结果的容器（内部有DataInstances）
 */
public class ResultsHolder implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6983932599879423339L;
	
	public static final String RESULT_PREDICTED_PROFIT = "PredictedProfit";
	public static final String RESULT_REVERSE_PREDICTED_PROFIT = "ReversedProfit";
	public static final String RESULT_PREDICTED_WIN_RATE="PredictedWinRate";
	public static final String RESULT_REVERSE_PREDICTED_WIN_RATE = "ReversedWinRate";
	public static final String RESULT_SELECTED = "selected";
	
	private GeneralInstances m_result_instances; //结果集的容器
	private HashMap<GeneralAttribute,GeneralAttribute> m_attribsToCopy; //结果集中需要从输入数据中直接拷贝过来的数据
	
	//结果集的属性定义
	private GeneralAttribute m_selectedAtt; //选股属性
	private GeneralAttribute m_predAtt;     //正向预测值属性
	private GeneralAttribute m_reversedPredAtt; //反向预测值属性
	
	
	/*
	 * 正常初始化构造函数
	 */
	public ResultsHolder(AbstractModel clModel, GeneralInstances fullSetData,ArffFormat currentArff) throws Exception {
		

		// 根据输入数据，构建空的结果集
		DataInstances header = new DataInstances(fullSetData, 0);
		// 去掉不必要的字段，保留在Arff定义中的Result左半边数据
		BaseInstanceProcessor instanceProcessor = InstanceHandler.getHandler(header);
		m_result_instances = instanceProcessor.filterAttribs(header, currentArff.m_result_left_part);
		
		//然后再添加相应的预测结果字段，并暂存其属性值
		if (clModel instanceof NominalModel) {
			m_result_instances = instanceProcessor.AddAttribute(m_result_instances, ResultsHolder.RESULT_PREDICTED_WIN_RATE,
					m_result_instances.numAttributes());
			m_result_instances = instanceProcessor.AddAttribute(m_result_instances, ResultsHolder.RESULT_REVERSE_PREDICTED_WIN_RATE,
					m_result_instances.numAttributes());

			m_predAtt = m_result_instances.attribute(ResultsHolder.RESULT_PREDICTED_WIN_RATE);
			m_reversedPredAtt = m_result_instances.attribute(ResultsHolder.RESULT_REVERSE_PREDICTED_WIN_RATE);

		} else {
			m_result_instances = instanceProcessor.AddAttribute(m_result_instances, ResultsHolder.RESULT_PREDICTED_PROFIT, m_result_instances.numAttributes());
			m_result_instances = instanceProcessor.AddAttribute(m_result_instances, ResultsHolder.RESULT_REVERSE_PREDICTED_PROFIT,
					m_result_instances.numAttributes());
			m_predAtt = m_result_instances.attribute(ResultsHolder.RESULT_PREDICTED_PROFIT);
			m_reversedPredAtt = m_result_instances.attribute(ResultsHolder.RESULT_REVERSE_PREDICTED_PROFIT);

		}
		m_result_instances = instanceProcessor.AddAttribute(m_result_instances, ResultsHolder.RESULT_SELECTED, m_result_instances.numAttributes());
		m_selectedAtt = m_result_instances.attribute(ResultsHolder.RESULT_SELECTED);

		
		//查找出需要从输入数据中拷贝的属性
		findAttributesToCopy(fullSetData,currentArff);
	}
	
	/*
	 * 用当前的格式创建一个新的包含格式但数据为空的Result
	 */
	public ResultsHolder(ResultsHolder src){
		m_result_instances=new DataInstances(src.m_result_instances, 0);
		m_attribsToCopy=src.m_attribsToCopy;
	}
	
	/**
	 * 
	 * 找出需要从测试数据中直接拷贝至结果集的属性列表（包括校验字段和训练模型时须舍弃的字段）
	 */
	private void findAttributesToCopy(GeneralInstances fullSetData,ArffFormat currentArff) {

		// 从输入数据中查找上述定义的直接拷贝的字段
		List<String> toCopyList = Arrays.asList(currentArff.m_result_left_part);

		ArrayList<GeneralAttribute> allAttributes = fullSetData.getAttributeList();
		m_attribsToCopy=new HashMap<GeneralAttribute,GeneralAttribute> ();
		GeneralAttribute resultAttribute;
		for (GeneralAttribute srcAttribute : allAttributes) {
			String attribuateName=srcAttribute.name();
			if (toCopyList.contains(attribuateName) == true) { //如果在输入数据中有这个数据
				resultAttribute=m_result_instances.attribute(attribuateName);
				if (resultAttribute!=null){
					//如果在结果数据中也有
					m_attribsToCopy.put(srcAttribute, resultAttribute);
				}
			}
		}

	}
	
	/*
	 * 合并第二个结果集到当前结果集的尾部
	 * 此方法非线程安全
	 */
	public void addResults(ResultsHolder another) {
		BaseInstanceProcessor instanceProcessor = InstanceHandler.getHandler(m_result_instances);
		m_result_instances = instanceProcessor.mergeTwoInstances(m_result_instances, another.m_result_instances);
	}
	
	/**
	 * Creates a deep copy of the given result using serialization.
	 *
	 * @return a deep copy of the result
	 * @exception Exception if an error occurs
	 */
	public static ResultsHolder makeCopy(ResultsHolder result) throws Exception {
		return (ResultsHolder) new SerializedObject(result).getObject();
	}

	public GeneralInstances getResultInstances() {
		return m_result_instances;
	}


//	public HashMap<GeneralAttribute, GeneralAttribute> getAttribsToCopy() {
//		return m_attribsToCopy;
//	}
	
	/*
	 * 这个应该是多余的，因为下面的add会setDataSet
	 */
	public void setInstanceDateSet(GeneralInstance row){
		row.setDataset(m_result_instances);
	}
	
	/*
	 * 结果集中增加一列
	 */
	public void addInstance(GeneralInstance instance) {
		m_result_instances.add(instance);
	}
	
	public void copyDefinedAttributes(GeneralInstance srcData,GeneralInstance targetData){
		for (Entry<GeneralAttribute, GeneralAttribute>  entry : m_attribsToCopy.entrySet()) {
			GeneralAttribute from=entry.getKey();
			GeneralAttribute to=entry.getValue();
			if (from.isNominal()) {
				String label = srcData.stringValue(from);
				int index = from.indexOfValue(label);
				//TODO 是否需要这么做，直接设StringValue是否可以？ （可能这是处理null）
				if (index != -1) {
					targetData.setValue(to, index);
				}
			} else if (from.isNumeric()) {
				targetData.setValue(to, srcData.value(from));
			} else if (from.isString()) {
				targetData.setValue(to, srcData.stringValue(from));
			} else {
				throw new IllegalStateException("Unhandled attribute type!");
			}
		}
	}

	public GeneralAttribute selectedAttribute() {
		return m_selectedAtt;
	}

	public GeneralAttribute predictAttribute() {
		return m_predAtt;
	}

	public GeneralAttribute reversedPredAttribute() {
		return m_reversedPredAtt;
	}

}
