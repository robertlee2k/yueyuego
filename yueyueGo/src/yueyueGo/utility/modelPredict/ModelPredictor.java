package yueyueGo.utility.modelPredict;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Instance;
import yueyueGo.AbstractModel;
import yueyueGo.NominalModel;
import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.dataProcessor.InstanceHandler;
import yueyueGo.dataProcessor.WekaInstanceProcessor;
import yueyueGo.databeans.DataAttribute;
import yueyueGo.databeans.DataInstance;
import yueyueGo.databeans.DataInstances;
import yueyueGo.databeans.GeneralAttribute;
import yueyueGo.databeans.GeneralInstance;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaInstance;
import yueyueGo.databeans.WekaInstances;
import yueyueGo.utility.FormatUtility;
import yueyueGo.utility.modelEvaluation.ModelStore;
import yueyueGo.utility.modelEvaluation.ThresholdData;

public class ModelPredictor {

	public static final int VALUE_SELECTED = 1; // 模型预测结果为“选择”
	public static final int VALUE_NOT_SURE = 0; // 模型预测结果为“无法判断”
	public static final int VALUE_NEVER_SELECT = -1; // 模型预测结果为“坚决不选”

	private GeneralInstances m_shouyilvCache = null;

	
	private GeneralAttribute m_idAttInResult;
	private GeneralAttribute m_yearMonthAtt;
	private GeneralAttribute m_shouyilvAtt;
	private GeneralAttribute m_selectedAtt;
	private GeneralAttribute m_predAtt;
	private ArrayList<GeneralAttribute> m_attributesToCopy;

	private Classifier m_model;
	private Classifier m_reversedModel;
	private double m_thresholdMin;
	private double m_reversedThresholdMax;

	private PredictStatus m_predictStatus;

	// 为回测历史数据使用
	// result parameter will be changed in this method!
	public void predictData(AbstractModel clModel, GeneralInstances dataToPredict, GeneralInstances result,
			String yearSplit, String policy) throws Exception {
		// 生成存储预测中间结果的对象
		m_predictStatus = new PredictStatus(yearSplit, policy);

		// 第一步： 定义输出结果集的各种须特殊设置的Attribute属性
		m_idAttInResult = result.attribute(ArffFormat.ID);
		m_yearMonthAtt = result.attribute(ArffFormat.YEAR_MONTH);
		m_shouyilvAtt = result.attribute(ArffFormat.SHOUYILV);
		m_selectedAtt = result.attribute(ArffFormat.RESULT_SELECTED);
		// 找出输出结果集中须保留的校验字段 （将测试数据的这些值直接写入输出结果集）
		m_attributesToCopy = ModelPredictor.findAttributesToCopy(result);

		// 第二步： 从评估文件中获取模型，并校验之。
		// 先找对应的评估结果
		// 获取评估数据
		ThresholdData thresholdData = clModel.m_evaluationStore.loadDataFromFile();
		// 校验读入的thresholdData内容是否可以用于目前评估
		String msg = clModel.m_evaluationStore.validateThresholdData(thresholdData);
		if (msg == null) {
			System.out.println("ThresholdData verified for target yearsplit " + yearSplit);
		} else {
			throw new Exception(msg);
		}

		m_predAtt = null;
		if (clModel instanceof NominalModel) {
			// Nominal数据的格式需要额外处理
			m_shouyilvCache = this.cacheShouyilvData(dataToPredict);
			dataToPredict = ((NominalModel) clModel).processDataForNominalClassifier(dataToPredict);
			m_predAtt = result.attribute(ArffFormat.RESULT_PREDICTED_WIN_RATE);
		} else {
			m_predAtt = result.attribute(ArffFormat.RESULT_PREDICTED_PROFIT);
		}
		GeneralInstances predictDataFormat = new WekaInstances(dataToPredict, 0);

		// 删除已保存的ID列，让待分类数据与模型数据一致 （此处的index是从1开始）
		BaseInstanceProcessor instanceProcessor = InstanceHandler.getHandler(predictDataFormat);
		predictDataFormat = instanceProcessor.removeAttribs(predictDataFormat,
				Integer.toString(ArffFormat.ID_POSITION) + "-" + ArffFormat.YEAR_MONTH_INDEX);
//				Integer.toString(ArffFormat.ID_POSITION));

		// 获取预测文件中的应该用哪个modelYearSplit的模型
		String modelYearSplit = thresholdData.getModelYearSplit();
		// 从评估结果中找到正向模型文件。
		ModelStore modelStore = new ModelStore(clModel.m_evaluationStore.getWorkFilePath(),
				thresholdData.getModelFileName(), modelYearSplit);
		m_model = modelStore.loadModelFromFile(predictDataFormat, yearSplit);

		// 获取预测文件中的应该用哪个反向模型的模型
		String reversedModelYearSplit = thresholdData.getReversedModelYearSplit();

		// boolean usingOneModel=false;
		m_reversedModel = null;
		ModelStore reversedModelStore = null;
		if (reversedModelYearSplit.equals(modelYearSplit)) {// 正向模型和方向模型是同一模型的。
			m_reversedModel = m_model;
			reversedModelStore = modelStore;
		} else {
			// 从评估结果中找到反向模型文件。
			reversedModelStore = new ModelStore(clModel.m_evaluationStore.getWorkFilePath(),
					thresholdData.getReversedModelFileName(), reversedModelYearSplit);
			// 获取model
			m_reversedModel = reversedModelStore.loadModelFromFile(predictDataFormat, yearSplit);
		}

		double[] thresholds = thresholdData.getThresholds();
		double[] percentiles = thresholdData.getPercentiles();
		double targetPercentile = thresholdData.getDefaultPercentile(); //目标值
		int defaultIndex = thresholdData.getDefaultThresholdIndex(); //初始值

		

		//获取所有的不同tradeDate，并排序。
		ArrayList<Date> tradeDateList=getTradeDateList(dataToPredict);
		SimpleDateFormat sdFormat=new SimpleDateFormat(ArffFormat.ARFF_DATE_FORMAT);
		
		//循环处理每天的数据
		int tradeDateIndex=dataToPredict.attribute(ArffFormat.TRADE_DATE).index()+1; //下面过滤的地方index是从1开始
		String oneDay=null;
		for (Date date : tradeDateList) {
			//获取一日的数据
			oneDay=sdFormat.format(date);
			String split=WekaInstanceProcessor.WEKA_ATT_PREFIX + tradeDateIndex + " is '" + oneDay+"'";
			GeneralInstances oneDayData=instanceProcessor.getInstancesSubset(dataToPredict, split);
			int nextDataSize=oneDayData.size();
			System.out.println("got one day data. date="+oneDay+" number="+nextDataSize);
			
			//开始调整阈值
			m_thresholdMin=adjustThreshold(nextDataSize, thresholds, percentiles, targetPercentile, defaultIndex);
			System.out.println("adjusted threshold set to " + m_thresholdMin);

			m_reversedThresholdMax = thresholdData.getReversedThreshold(); // 判断为0的阈值，小于该值意味着该模型坚定认为其为0
																			// （这是合并多个模型预测时使用的）
			if (m_reversedThresholdMax > m_thresholdMin) {
				if (m_reversedModel == m_model) {
					throw new Exception("fatal error!!! reversedThreshold(" + m_reversedThresholdMax + ") > threshold("
							+ m_thresholdMin + ") using same model (modelyear=" + reversedModelYearSplit + ")");
				} else {
					System.err.println("使用不同模型时反向阀值大于正向阀值了" + "reversedThreshold(" + m_reversedThresholdMax + ")@"
							+ reversedModelYearSplit + " > threshold(" + m_thresholdMin + ")@" + modelYearSplit);
				}
			}
			// 具体预测
			predictMiniBatch(oneDayData, result, yearSplit);
		}

		// 第三步： 输出评估参数
		double[] modelAUC = thresholdData.getModelAUC();
		double reversedPercentile = thresholdData.getReversedPercent();

		if ("".equals(yearSplit)) { //TODO 预测的时候应该也传相应的yearSplit进来，不应该为空
			// 输出评估结果及所使用阀值及期望样本百分比
			String evalSummary = "\r\n\t ( with params: modelYearSplit=" + modelYearSplit + " threshold="
					+ FormatUtility.formatDouble(m_thresholdMin, 0, 3);
			// + " , percentile="+ FormatUtility.formatPercent(targetPercentile
			// / 100) ;
			evalSummary += " ,reversedModelYearSplit=" + reversedModelYearSplit + " ,reversedThreshold="
					+ FormatUtility.formatDouble(m_reversedThresholdMax, 0, 3) + " , reversedPercentile="
					+ FormatUtility.formatPercent(reversedPercentile / 100);
			evalSummary += " ,modelAUC@focusAreaRatio=";
			double[] focusAreaRatio = thresholdData.getFocosAreaRatio();
			for (int i = 0; i < focusAreaRatio.length; i++) {
				evalSummary += FormatUtility.formatDouble(modelAUC[i], 1, 4) + "@"
						+ FormatUtility.formatPercent(focusAreaRatio[i], 3, 0) + ", ";
			}
			evalSummary += " )\r\n";
			System.out.println("预测用模型文件:  " + modelStore.getWorkFilePath() + modelStore.getModelFileName());
			System.out.println(
					"预测用反向模型文件" + reversedModelStore.getWorkFilePath() + reversedModelStore.getModelFileName());
			clModel.classifySummaries.appendEvaluationSummary(evalSummary);

		} else {
			// 输出评估结果及所使用阀值及期望样本百分比
			String evalSummary = yearSplit + "," + policy + "," + modelYearSplit + ",";
			// + FormatUtility.formatDouble(m_thresholdMin, 0, 3) + ","
			// + FormatUtility.formatPercent(targetPercentile / 100) + ",";
			evalSummary += reversedModelYearSplit + "," + FormatUtility.formatDouble(m_reversedThresholdMax, 0, 3) + ","
					+ FormatUtility.formatPercent(reversedPercentile / 100) + ",";
			for (double d : modelAUC) {
				evalSummary += FormatUtility.formatDouble(d, 0, 4) + ",";
			}
			evalSummary += "\r\n";
			clModel.classifySummaries.appendEvaluationSummary(evalSummary);
		}

		// // 第三步： 输出各种统计值
		// if ("".equals(yearSplit)) {
		// // 这是预测每日数据时，没有实际收益率数据可以做评估 (上述逻辑会让所有的数据都进入negative的分支）
		// clModel.classifySummaries.savePredictSummaries(policySplit,
		// totalNegativeShouyilv,
		// selectedNegativeShouyilv);
		// } else {
		// // 这是进行历史回测数据时，根据历史收益率数据进行阶段评估
		// clModel.classifySummaries.computeClassifySummaries(yearSplit,
		// policySplit, totalPositiveShouyilv,
		// totalNegativeShouyilv, selectedPositiveShouyilv,
		// selectedNegativeShouyilv);
		// }
	}

	/**
	 * @param data
	 */
	private ArrayList<Date> getTradeDateList(GeneralInstances data) throws Exception{
		GeneralAttribute tradeDateAtt=data.attribute(ArffFormat.TRADE_DATE);
		ArrayList<Date> tradeDateList=new ArrayList<Date>();
		SimpleDateFormat sdFormat=new SimpleDateFormat(ArffFormat.ARFF_DATE_FORMAT);
		String current="2099/12/31";
		String next=null;
		for (int i=0;i<data.numInstances();i++){
			next=data.get(i).stringValue(tradeDateAtt);
			if (current.equals(next)==false){
				current=next;
				//转换为日期以便于排序，免得将字符串中的2017/10/5 排在2017/10/21之后了
				Date d = sdFormat.parse(next);
				tradeDateList.add(d);
			}
		}
		Collections.sort(tradeDateList);
		return tradeDateList;
	}

	/**
	 * 动态调整阈值
	 * 如果迄今为止已选股票的百分比已经大于threshold中的预期百分比，则提升阈值单位。
	 * @param nextBatchSize
	 * @param thresholds
	 * @param percentiles
	 * @param targetPercentile
	 * @param currentIndex
	 * @return
	 */
	private double adjustThreshold(int nextBatchSize, double[] thresholds, double[] percentiles, double targetPercentile,
			int currentIndex) {
		double adjustedThreshold=0.0;
		
		double currentPercentile = (1-m_predictStatus.getCummulativeSelectRatio())*100;
		System.out.println("targetPercentile=" + targetPercentile+", currentPercentile="+currentPercentile);

		if (Double.isNaN(currentPercentile)) { // 还未开始本批次预测时
			adjustedThreshold = thresholds[currentIndex]; // 缺省的判断为1的阈值，大于该值意味着该模型判断其为1
		} else {
			// 如果迄今为止已选股票的百分比已经大于threshold中的预期百分比，则提升阈值单位。
			double adjustedPercentile;

			// 找到调整的阈值数量
			double predictedCount=m_predictStatus.getCummulativePredicted();
			
			adjustedPercentile = (targetPercentile*(predictedCount+nextBatchSize)-currentPercentile*predictedCount)/nextBatchSize;

			if (adjustedPercentile<100){ //已选股不多还可以继续选
				// percentile目前假定为是个递减数组 TODO
				int adjustedInex = findNearestIndexInArray(percentiles, adjustedPercentile);
				adjustedThreshold = thresholds[adjustedInex]; // 判断为1的阈值，大于该值意味着该模型判断其为1
			}else {
				// 已选股太多时直接设该批次不选
				adjustedThreshold = Double.MAX_VALUE;
			}
		}
		return adjustedThreshold;
	}

	/**
	 * 根据当前选股计算调整的threshold值
	 * 
	 * @param thresholdData
	 * @return
	 */

	/**
	 * 给定一个从大到小排序的数组，查找数组内最接近某一数组的数值的下标
	 * 
	 * @param sortedValues
	 * @param targetValue
	 * @return
	 */
	public static int findNearestIndexInArray(double[] sortedValues, double targetValue) {
		int currentIndex;
		int high = 0;
		int low = sortedValues.length - 1;
		for (int i = 0; i < sortedValues.length; i++) {
			if (targetValue < sortedValues[i]) {
				high = i;
			} else {
				low = i;
				break;
			}
		}
		if ((sortedValues[high] - targetValue) > targetValue - sortedValues[low]) {
			currentIndex = low;
		} else {
			currentIndex = high;
		}
		System.out.println("find targetValue(" + targetValue + ") near" + sortedValues[currentIndex]);
		return currentIndex;
	}

	/**
	 * @param clModel
	 * @param miniBatchData
	 * @param result
	 * @param yearSplit
	 * @throws NumberFormatException
	 * @throws Exception
	 * @throws IllegalStateException
	 */
	private void predictMiniBatch(GeneralInstances miniBatchData, GeneralInstances result, String yearSplit)
			throws Exception {

		double pred;
		double reversedPred;
		double yearMonth = Double.valueOf(yearSplit).doubleValue();

		// There is additional ID attribute in test instances, so we should save
		// it and remove before doing prediction
		double[] ids = miniBatchData.attributeToDoubleArray(ArffFormat.ID_POSITION - 1);
		// 删除已保存的ID 列，让待分类数据与模型数据一致 （此处的index是从1开始）
		miniBatchData = InstanceHandler.getHandler(miniBatchData).removeAttribs(miniBatchData,
				Integer.toString(ArffFormat.ID_POSITION) + "-" + ArffFormat.YEAR_MONTH_INDEX);
//				Integer.toString(ArffFormat.ID_POSITION));
		// 开始循环，用分类模型和阈值对每一条数据进行预测，并存入输出结果集
		System.out.println("actual -> predicted....... ");

		int selectedCount = 0;
		int predictedCount = miniBatchData.numInstances();
		int startIndexInBatch=m_predictStatus.getCummulativePredicted(); // 本次预测整个预测中的起始位置（这个主要是为了查找cache用的）
		for (int i = 0; i < predictedCount; i++) {
			GeneralInstance currentTestRow = miniBatchData.instance(i);
			pred = ModelPredictor.classify(m_model, currentTestRow); // 调用分类函数
			if (m_reversedModel == m_model) { // 如果反向评估模型是同一个类
				reversedPred = pred;
			} else {
				reversedPred = ModelPredictor.classify(m_reversedModel, currentTestRow); // 调用反向评估模型的分类函数
			}

			// 准备输出结果
			DataInstance resultRow = new DataInstance(result.numAttributes());
			resultRow.setDataset(result);
			// 将相应的ID赋值回去
			resultRow.setValue(m_idAttInResult, ids[i]);
			// 将YearMonth赋值回去用于统计用途
			resultRow.setValue(m_yearMonthAtt, yearMonth);

			// Nominal数据的格式需要额外处理
			if (m_shouyilvCache != null) {
				// 获取原始数据中的实际收益率值
				double shouyilv = getShouyilv(i+startIndexInBatch, ids[i], currentTestRow.classValue());
				resultRow.setValue(m_shouyilvAtt, shouyilv);
			}
			// 将获得的预测值设定入结果集
			resultRow.setValue(m_predAtt, pred);

			// 计算选股结果
			double selected = ModelPredictor.VALUE_NOT_SURE;
			// 先用反向模型判断
			if (reversedPred < m_reversedThresholdMax) {
				selected = ModelPredictor.VALUE_NEVER_SELECT;// 反向模型坚定认为当前数据为0
																// （这是合并多个模型预测时使用的）
			}
			// 如果正向模型与方向模型得出的值矛盾（在使用不同模型时），则用正向模型的数据覆盖方向模型（因为毕竟正向模型的ratio比较小）
			if (pred >= m_thresholdMin) { // 本模型估计当前数据是1值
				selected = ModelPredictor.VALUE_SELECTED;
				selectedCount++;
			}
			resultRow.setValue(m_selectedAtt, selected);

			// 最后将那些无须结算的校验字段的值直接从测试数据集拷贝到输出结果集
			for (GeneralAttribute resultAttribute : m_attributesToCopy) {
				GeneralAttribute testAttribute = miniBatchData.attribute(resultAttribute.name());
				// test attribute which is be present in the result data set
				if (testAttribute != null) {
					if (testAttribute.isNominal()) {
						String label = currentTestRow.stringValue(testAttribute);
						int index = testAttribute.indexOfValue(label);
						if (index != -1) {
							resultRow.setValue(resultAttribute, index);
						}
					} else if (testAttribute.isNumeric()) {
						resultRow.setValue(resultAttribute, currentTestRow.value(testAttribute));
					} else {
						throw new IllegalStateException("Unhandled attribute type!");
					}
				}
			}

			result.add(resultRow);
		}
		m_predictStatus.addCummulativePredicted(predictedCount);
		m_predictStatus.addCummulativeSelected(selectedCount);
	}

	/**
	 * @param clModel
	 * @param fullSetData
	 * @return
	 * @throws Exception
	 */
	public static GeneralInstances prepareResultInstances(AbstractModel clModel, GeneralInstances fullSetData)
			throws Exception {
		GeneralInstances result;
		DataInstances header = new DataInstances(fullSetData, 0);
		// 去除不必要的字段，保留ID（第1），YEARMONTH（第二）均线策略（第3）、bias5（第4）、收益率（最后一列）、增加预测值、是否被选择。
		int removeFromIndex = BaseInstanceProcessor.findATTPosition(fullSetData, ArffFormat.BIAS5) + 1;
		BaseInstanceProcessor instanceProcessor = InstanceHandler.getHandler(header);
		// ArffFormat.YEAR_MONTH_INDEX + ","+
		result = instanceProcessor.removeAttribs(header, removeFromIndex + "-" + (header.numAttributes() - 1));
		if (clModel instanceof NominalModel) {
			result = instanceProcessor.AddAttribute(result, ArffFormat.RESULT_PREDICTED_WIN_RATE,
					result.numAttributes());
		} else {
			result = instanceProcessor.AddAttribute(result, ArffFormat.RESULT_PREDICTED_PROFIT, result.numAttributes());
		}
		result = instanceProcessor.AddAttribute(result, ArffFormat.RESULT_SELECTED, result.numAttributes());
		return result;

	}

	/**
	 * 
	 * 找出需要从测试数据中直接拷贝至结果集的属性列表（这是那些校验位）
	 * 
	 * @param result
	 *            结果集
	 */
	static ArrayList<GeneralAttribute> findAttributesToCopy(GeneralInstances result) {
		// 这是结果集中会特殊处理的字段，无须拷贝
		String[] processedAttNames = { ArffFormat.ID, ArffFormat.YEAR_MONTH,
				// ArffFormat.SHOUYILV,
				ArffFormat.RESULT_PREDICTED_WIN_RATE, ArffFormat.RESULT_PREDICTED_PROFIT, ArffFormat.RESULT_SELECTED };

		// 从结果集中筛除上述字段
		List<String> list = Arrays.asList(processedAttNames);
		ArrayList<GeneralAttribute> allAttributes = result.getAttributeList();
		ArrayList<GeneralAttribute> attributesToCopy = new ArrayList<GeneralAttribute>();
		for (GeneralAttribute attribute : allAttributes) {
			if (list.contains(attribute.name()) == false) {
				attributesToCopy.add(attribute);
			}
		}
		return attributesToCopy;
	}

	protected double getShouyilv(int index, double id, double newClassValue) throws Exception {
		if (m_shouyilvCache == null) {
			return Double.NaN;
		}
		if (index >= m_shouyilvCache.numInstances()) {
			throw new Exception("Old Class Value has not been cached for index: " + index);
		}
		double cachedID = m_shouyilvCache.instance(index).value(0);
		if (cachedID != id) {
			throw new Exception("Data inconsistent error! Cached old class value id = " + cachedID
					+ " while incoming id =" + id + " for index: " + index);
		}
		double shouyilv = m_shouyilvCache.instance(index).classValue();
		if (newClassValue == 0 && shouyilv > 0 || newClassValue == 1 && shouyilv <= 0) {
			throw new Exception("Data inconsistent error! Cached old class value id = " + shouyilv
					+ " while incoming newClassValue =" + newClassValue + " for index: " + index
					+ " @m_positiveLine=0");
		}
		return shouyilv;
	}

	protected GeneralInstances cacheShouyilvData(GeneralInstances inData) {
		GeneralInstances cache = CreateCachedOldClassInstances();
		double shouyilv = 0;
		for (int i = 0; i < inData.numInstances(); i++) {
			shouyilv = inData.instance(i).classValue();
			// 暂存收益率

			double id = inData.instance(i).value(0);
			DataInstance cacheRow = new DataInstance(cache.numAttributes());
			cacheRow.setDataset(cache);
			cacheRow.setValue(0, id);
			cacheRow.setValue(1, shouyilv);
			cache.add(cacheRow);
		}
		return cache;
	}

	// 创建暂存oldClassValue（目前情况下为暂存收益率）的arff结构（id-收益率）
	protected GeneralInstances CreateCachedOldClassInstances() {
		DataAttribute pred = new DataAttribute(ArffFormat.ID);
		DataAttribute shouyilv = new DataAttribute(ArffFormat.SHOUYILV);
		ArrayList<GeneralAttribute> fvWekaAttributes = new ArrayList<GeneralAttribute>(2);
		fvWekaAttributes.add(pred);
		fvWekaAttributes.add(shouyilv);
		GeneralInstances structure = new DataInstances("cachedOldClass", fvWekaAttributes, 0);
		structure.setClassIndex(structure.numAttributes() - 1);
		return structure;
	}

	/**
	 * classify the results
	 * 
	 */
	public static double classify(Classifier classifier, GeneralInstance targetInstance) throws Exception {
		Instance curr = WekaInstance.convertToWekaInstance(targetInstance);
		double[] dist = classifier.distributionForInstance(curr);
		if (curr.classAttribute().isNominal()) {
			return dist[NominalModel.CLASS_POSITIVE_INDEX];
		} else {
			return dist[0];
		}

	}
}
