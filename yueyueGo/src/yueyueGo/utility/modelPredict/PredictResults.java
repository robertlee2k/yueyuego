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

public class PredictResults implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6983932599879423339L;
	
	public static final String RESULT_PREDICTED_PROFIT = "PredictedProfit";
	public static final String RESULT_REVERSE_PREDICTED_PROFIT = "ReversedProfit";
	public static final String RESULT_PREDICTED_WIN_RATE="PredictedWinRate";
	public static final String RESULT_REVERSE_PREDICTED_WIN_RATE = "ReversedWinRate";
	public static final String RESULT_SELECTED = "selected";
	
	private GeneralInstances m_result_instances;
	private HashMap<GeneralAttribute,GeneralAttribute> m_attribsToCopy;
	
	public PredictResults(AbstractModel clModel, GeneralInstances fullSetData,ArffFormat currentArff) throws Exception {
		
	
		DataInstances header = new DataInstances(fullSetData, 0);
		// 去掉不必要的字段，保留在Arff定义中的Result左半边数据
		BaseInstanceProcessor instanceProcessor = InstanceHandler.getHandler(header);
		m_result_instances = instanceProcessor.filterAttribs(header, currentArff.m_result_left_part);

		if (clModel instanceof NominalModel) {
			m_result_instances = instanceProcessor.AddAttribute(m_result_instances, PredictResults.RESULT_PREDICTED_WIN_RATE,
					m_result_instances.numAttributes());
			m_result_instances = instanceProcessor.AddAttribute(m_result_instances, PredictResults.RESULT_REVERSE_PREDICTED_WIN_RATE,
					m_result_instances.numAttributes());
		} else {
			m_result_instances = instanceProcessor.AddAttribute(m_result_instances, PredictResults.RESULT_PREDICTED_PROFIT, m_result_instances.numAttributes());
			m_result_instances = instanceProcessor.AddAttribute(m_result_instances, PredictResults.RESULT_REVERSE_PREDICTED_PROFIT,
					m_result_instances.numAttributes());
		}
		m_result_instances = instanceProcessor.AddAttribute(m_result_instances, PredictResults.RESULT_SELECTED, m_result_instances.numAttributes());

		//把需要拷贝的属性定义清楚
		findAttributesToCopy(fullSetData,currentArff);
	}
	
	/**
	 * 
	 * 找出需要从测试数据中直接拷贝至结果集的属性列表（包括校验字段和训练模型时须舍弃的字段）
	 * 
	 * @param result
	 *            结果集
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

	
	/**
	 * Creates a deep copy of the given result using serialization.
	 *
	 * @return a deep copy of the result
	 * @exception Exception if an error occurs
	 */
	public static PredictResults makeCopy(PredictResults result) throws Exception {
		return (PredictResults) new SerializedObject(result).getObject();
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
	
	public void add(GeneralInstance instance) {
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

}
