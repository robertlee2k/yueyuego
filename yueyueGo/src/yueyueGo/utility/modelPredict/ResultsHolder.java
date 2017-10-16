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
import yueyueGo.dataProcessor.WekaInstanceProcessor;
import yueyueGo.databeans.DataInstance;
import yueyueGo.databeans.DataInstances;
import yueyueGo.databeans.GeneralAttribute;
import yueyueGo.databeans.GeneralInstance;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.utility.FormatUtility;


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
	
	/*
	 * 数据存储变量区
	 */
	private GeneralInstances m_result_instances; //结果集的容器
	private String m_policyGroup;	//分组策略
	private int m_resultType; //当前结果集的类型（见下面常量定义）

	/*
	 * 工作常量区
	 */
	private HashMap<GeneralAttribute,GeneralAttribute> m_attribsToCopy; //结果集中需要从输入数据中直接拷贝过来的数据
	
	
	//结果集的属性定义
	private GeneralAttribute m_selectedAtt; //选股属性
	private GeneralAttribute m_predAtt;     //正向预测值属性
	private GeneralAttribute m_reversedPredAtt; //反向预测值属性

	/*
	 * 常量定义区域
	 */
	public static final int VALUE_SELECTED = 1; // 模型预测结果为“选择”
	public static final int VALUE_NOT_SURE = 0; // 模型预测结果为“无法判断”
	public static final int VALUE_NEVER_SELECT = -1; // 模型预测结果为“坚决不选”
	
	
	public static final int NOMINAL_RESULT = 666; // 当前结果类型为二分类器结果
	public static final int CONTINIOUS_RESULT = 888; // 当前结果类型为连续分类器结果

	
	/*
	 * 正常初始化构造函数
	 */
	public ResultsHolder(AbstractModel clModel, GeneralInstances fullSetData,ArffFormat currentArff) throws Exception {
		
		m_policyGroup=currentArff.m_policy_group;

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
			
			m_resultType=ResultsHolder.NOMINAL_RESULT;

		} else {
			m_result_instances = instanceProcessor.AddAttribute(m_result_instances, ResultsHolder.RESULT_PREDICTED_PROFIT, m_result_instances.numAttributes());
			m_result_instances = instanceProcessor.AddAttribute(m_result_instances, ResultsHolder.RESULT_REVERSE_PREDICTED_PROFIT,
					m_result_instances.numAttributes());
			m_predAtt = m_result_instances.attribute(ResultsHolder.RESULT_PREDICTED_PROFIT);
			m_reversedPredAtt = m_result_instances.attribute(ResultsHolder.RESULT_REVERSE_PREDICTED_PROFIT);
			
			m_resultType=ResultsHolder.CONTINIOUS_RESULT;
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
		m_predAtt=src.m_predAtt;
		m_reversedPredAtt=src.m_reversedPredAtt;
		m_selectedAtt=src.m_selectedAtt;
		m_policyGroup=src.m_policyGroup;
		m_resultType=src.m_resultType;
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
				String label = srcData.stringValue(from);
				targetData.setValue(to, label);
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

	/**
	 * 根据当前选股计算调整的threshold值
	 * 
	 * @param thresholdData
	 * @return
	 */
	
	//	protected double getShouyilv(GeneralInstances a_shouyilvCache, int index, double id, double newClassValue)
	//			throws Exception {
	//		if (a_shouyilvCache == null) {
	//			return Double.NaN;
	//		}
	//		if (index >= a_shouyilvCache.numInstances()) {
	//			throw new Exception("Old Class Value has not been cached for index: " + index);
	//		}
	//		double cachedID = a_shouyilvCache.instance(index).value(0);
	//		if (cachedID != id) {
	//			throw new Exception("Data inconsistent error! Cached old class value id = " + cachedID
	//					+ " while incoming id =" + id + " for index: " + index);
	//		}
	//		double shouyilv = a_shouyilvCache.instance(index).classValue();
	//		if (newClassValue == 0 && shouyilv > 0 || newClassValue == 1 && shouyilv <= 0) {
	//			throw new Exception("Data inconsistent error! Cached old class value id = " + shouyilv
	//					+ " while incoming newClassValue =" + newClassValue + " for index: " + index
	//					+ " @m_positiveLine=0");
	//		}
	//		return shouyilv;
	//	}
	//
	//	/*
	//	 * 暂存收益率 （可以用于将来的校验）
	//	 */
	//	protected GeneralInstances cacheShouyilvData(GeneralInstances inData) {
	//		GeneralInstances cache = CreateCachedOldClassInstances();
	//		double shouyilv = 0;
	//		for (int i = 0; i < inData.numInstances(); i++) {
	//			shouyilv = inData.instance(i).classValue();
	//			// 暂存收益率
	//
	//			double id = inData.instance(i).value(0);
	//			DataInstance cacheRow = new DataInstance(cache.numAttributes());
	//			cacheRow.setDataset(cache);
	//			cacheRow.setValue(0, id);
	//			cacheRow.setValue(1, shouyilv);
	//			cache.add(cacheRow);
	//		}
	//		return cache;
	//	}
	//
	//	// 创建暂存oldClassValue（目前情况下为暂存收益率）的arff结构（id-收益率）
	//	protected GeneralInstances CreateCachedOldClassInstances() {
	//		DataAttribute pred = new DataAttribute(ArffFormat.ID);
	//		DataAttribute shouyilv = new DataAttribute(ArffFormat.SHOUYILV);
	//		ArrayList<GeneralAttribute> fvWekaAttributes = new ArrayList<GeneralAttribute>(2);
	//		fvWekaAttributes.add(pred);
	//		fvWekaAttributes.add(shouyilv);
	//		GeneralInstances structure = new DataInstances("cachedOldClass", fvWekaAttributes, 0);
	//		structure.setClassIndex(structure.numAttributes() - 1);
	//		return structure;
	//	}
	
	/**
	 * 按水平合并referenceResult，缺失字段从left中取
	 * @param referenceData
	 * @param dataToAdd
	 * @param left
	 * @return
	 * @throws Exception
	 */
	public GeneralInstances mergeResults(ResultsHolder reference, GeneralInstances left) throws Exception {
	
		String policyGroup=this.m_policyGroup;
		
		// 创建输出结果
		GeneralInstances mergedResult = new DataInstances(left, 0);
		BaseInstanceProcessor instanceProcessor = InstanceHandler.getHandler(mergedResult);
		mergedResult = instanceProcessor.AddAttribute(mergedResult, ResultsHolder.RESULT_PREDICTED_PROFIT,
				mergedResult.numAttributes());
		mergedResult = instanceProcessor.AddAttribute(mergedResult, ResultsHolder.RESULT_PREDICTED_WIN_RATE,
				mergedResult.numAttributes());
		mergedResult = instanceProcessor.AddAttribute(mergedResult, ResultsHolder.RESULT_SELECTED,
				mergedResult.numAttributes());
	
		GeneralInstances resultData = this.getResultInstances();
		GeneralInstances referenceData = reference.getResultInstances();
	
		System.out.println(
				"incoming resultData size, row=" + resultData.numInstances() + " column=" + resultData.numAttributes());
		System.out.println("incoming referenceData size, row=" + referenceData.numInstances() + " column="
				+ referenceData.numAttributes());
		System.out.println("Left data loaded, row=" + left.numInstances() + " column=" + left.numAttributes());
	
		GeneralInstance leftCurr;
		GeneralInstance resultCurr;
		GeneralInstance referenceCurr;
		DataInstance newData;
	
		// 左侧冗余信息文件属性
		GeneralAttribute leftMA = left.attribute(policyGroup);// ArffFormat.SELECTED_AVG_LINE);
		GeneralAttribute shouyilvAtt = left.attribute(ArffFormat.SHOUYILV);
		GeneralAttribute leftBias5 = left.attribute(ArffFormat.BIAS5);
	
		// 结果文件属性
		GeneralAttribute resultMA = resultData.attribute(policyGroup);// ArffFormat.SELECTED_AVG_LINE);
		GeneralAttribute resultBias5 = resultData.attribute(ArffFormat.BIAS5);
		GeneralAttribute resultSelectedAtt = resultData.attribute(ResultsHolder.RESULT_SELECTED);
	
		// 输出文件的属性
		GeneralAttribute outputSelectedAtt = mergedResult.attribute(ResultsHolder.RESULT_SELECTED);
		GeneralAttribute outputPredictAtt = mergedResult.attribute(ResultsHolder.RESULT_PREDICTED_PROFIT);
		GeneralAttribute outputWinrateAtt = mergedResult.attribute(ResultsHolder.RESULT_PREDICTED_WIN_RATE);
	
		// 传入的结果集result不是排序的,而left的数据是按tradeDate日期排序的， 所以都先按ID排序。
		left.sort(ArffFormat.ID_POSITION - 1);
		resultData.sort(ArffFormat.ID_POSITION - 1);
		referenceData.sort(ArffFormat.ID_POSITION - 1);
	
		double idInResults = 0;
		double idInLeft = 0;
		double idInReference = 0;
		int resultIndex = 0;
		int leftIndex = 0;
		int referenceIndex = 0;
		int referenceDataNum = referenceData.numInstances();
	
		// 以下变量是为合并时修改选择结果而设
		int finalSelected = 0;
		int resultChanged = 0;
		int goodChangeNum = 0;
		double changedShouyilv = 0;
	
		while (leftIndex < left.numInstances() && resultIndex < resultData.numInstances()) {
			resultCurr = resultData.instance(resultIndex);
			leftCurr = left.instance(leftIndex);
			idInResults = resultCurr.value(0);
			idInLeft = leftCurr.value(0);
			if (idInLeft < idInResults) { // 如果左边有未匹配的数据，这是正常的，因为left数据是从2005年开始的全量
				leftIndex++;
				continue;
			} else if (idInLeft > idInResults) { // 如果右边result有未匹配的数据，这个不大正常，需要输出
				System.out.println("!!!unmatched result====" + resultCurr.toString());
				System.out.println("!!!current left   =====" + leftCurr.toString());
				resultIndex++;
				continue;
			} else if (idInLeft == idInResults) {// 找到相同ID的记录了
				// 去reference数据里查找相应的ID记录
				referenceCurr = referenceData.instance(referenceIndex);
				idInReference = referenceCurr.value(0);
	
				// 这段代码是用于应对reference的数据与result的数据不一致情形的。
				// reference数据也是按ID排序的，所以可以按序查找
				int oldIndex = referenceIndex;// 暂存一下
				while (idInReference < idInResults) {
					if (referenceIndex < referenceDataNum - 1) {
						referenceIndex++;
						referenceCurr = referenceData.instance(referenceIndex);
						idInReference = referenceCurr.value(0);
					} else { // 当前ID比result的ID小，需要向后找，但向后找到最后一条也没找到
						referenceCurr = new DataInstance(referenceData.numAttributes());
						referenceIndex = oldIndex; // 这一条设为空，index恢复原状
						break;
					}
				}
				while (idInReference > idInResults) {
					if (referenceIndex > 0) {
						referenceIndex--;
						referenceCurr = referenceData.instance(referenceIndex);
						idInReference = referenceCurr.value(0);
					} else { // 当前ID比result的ID大，需要向前找，但向前找到第一条也没找到
						referenceCurr = new DataInstance(referenceData.numAttributes());
						referenceIndex = oldIndex; // 这一条设为空，index恢复原状
						break;
					}
				}
	
				// 接下来做冗余字段的数据校验
				if (ArffFormat.checkSumBeforeMerge(leftCurr, resultCurr, leftMA, resultMA, leftBias5, resultBias5)) {
					newData = new DataInstance(mergedResult.numAttributes());
					newData.setDataset(mergedResult);
					int srcStartIndex = 0;
					int srcEndIndex = leftCurr.numAttributes() - 1;
					int targetStartIndex = 0;
					BaseInstanceProcessor.copyToNewInstance(leftCurr, newData, srcStartIndex, srcEndIndex,
							targetStartIndex);
	
					// 根据目前ResultHolder的属性判断当前有什么，需要补充的数据是什么
					double profit;
					double winrate;
					if (this.m_resultType==ResultsHolder.CONTINIOUS_RESULT) {
						// 当前结果集里有什么数据
						profit = resultCurr.value(resultData.attribute(ResultsHolder.RESULT_PREDICTED_PROFIT));
						// 需要考虑参考结果集里的数据
						winrate = referenceCurr.value(referenceData.attribute(ResultsHolder.RESULT_PREDICTED_WIN_RATE));
					} else {
						// 当前结果集里有什么数据
						winrate = resultCurr.value(resultData.attribute(ResultsHolder.RESULT_PREDICTED_WIN_RATE));
						// 需要添加参考集里的什么数据
						profit = referenceCurr.value(referenceData.attribute(ResultsHolder.RESULT_PREDICTED_PROFIT));
					}
	
					// 当前结果集的选股结果
					double selected = resultCurr.value(resultSelectedAtt);
					// 参考结果的选股结果（-1.0的排除）
					double referenceSelected = referenceCurr
							.value(referenceData.attribute(ResultsHolder.RESULT_SELECTED));
					if (selected == ResultsHolder.VALUE_SELECTED) {
						// 当合并数据时，如果参照的二分类器的选择值为-1 则不选择该条记录
						if (referenceSelected == ResultsHolder.VALUE_NEVER_SELECT) {
							selected = ResultsHolder.VALUE_NOT_SURE;
							resultChanged++;
							if (shouyilvAtt != null) {
								double shouyilv = leftCurr.value(shouyilvAtt);
								changedShouyilv += shouyilv;
								if (shouyilv <= 0) {
									// 如果变化的实际收益率小于0，说明这是一次正确的变换
									goodChangeNum++;
								} // end if shouyilv<=
							}
						} else { // 不需要修改选股结果
							finalSelected++;
						}
					} else if (selected == ResultsHolder.VALUE_NEVER_SELECT) {
						// 这个是因为要兼容交易程序（只接受0和1两个值，不接受-1）
						selected = ResultsHolder.VALUE_NOT_SURE;
					}
	
					newData.setValue(outputPredictAtt, profit);
					newData.setValue(outputWinrateAtt, winrate);
					newData.setValue(outputSelectedAtt, selected);
	
					mergedResult.add(newData);
					resultIndex++;
					leftIndex++;
	
				} else {
					throw new Exception("data value in header data and result data does not equal left="
							+ leftCurr.toString() + " /while result= " + resultCurr.toString());
				} // end else of ArffFormat.checkSumBeforeMerge
			} // end else if (idInLeft==idInResults )
		} // end left processed
		if (mergedResult.numInstances() != resultData.numInstances()) {
			System.err.println("------Attention!!! not all data in result have been processed , processed= "
					+ mergedResult.numInstances() + " ,while total result=" + resultData.numInstances());
		} else {
			System.out.println("number of results merged and processed: " + mergedResult.numInstances());
		}
		System.out.println("###### Finally selected count=" + finalSelected + "  ######");
		// System.out.println(EvaluationConfDefinition.showMergeParameters());
		System.out.println(" result changed because of reference data not matched=" + resultChanged
				+ " while good change number=" + goodChangeNum);
		if (resultChanged > 0) {
			double goodRatio = new Double(goodChangeNum).doubleValue() / resultChanged;
			System.out.print(" good ratio=" + FormatUtility.formatPercent(goodRatio));
			System.out.println(
					" average changed shouyilv=" + FormatUtility.formatPercent(changedShouyilv / resultChanged));
		}
	
		return mergedResult;
	}

	/**
	 * 
	 * 返回选股结果
	 * 
	 */
	public static GeneralInstances returnSelectedInstances(GeneralInstances selectResultInstances) throws Exception {
		// 返回选股结果
		int pos = BaseInstanceProcessor.findATTPosition(selectResultInstances, RESULT_SELECTED);
		BaseInstanceProcessor instanceProcessor = InstanceHandler.getHandler(selectResultInstances);
		GeneralInstances fullMarketSelected = instanceProcessor.getInstancesSubset(selectResultInstances,
				WekaInstanceProcessor.WEKA_ATT_PREFIX + pos + " = " + ResultsHolder.VALUE_SELECTED);
		return fullMarketSelected;
		
	}

}
