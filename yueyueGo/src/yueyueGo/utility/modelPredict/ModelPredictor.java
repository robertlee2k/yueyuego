package yueyueGo.utility.modelPredict;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import weka.classifiers.Classifier;
import weka.core.Instance;
import yueyueGo.AbstractModel;
import yueyueGo.NominalModel;
import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.dataProcessor.BaseInstanceProcessor;
import yueyueGo.dataProcessor.InstanceHandler;
import yueyueGo.dataProcessor.WekaInstanceProcessor;
import yueyueGo.databeans.DataInstance;
import yueyueGo.databeans.GeneralAttribute;
import yueyueGo.databeans.GeneralInstance;
import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaInstance;
import yueyueGo.databeans.WekaInstances;
import yueyueGo.utility.FormatUtility;
import yueyueGo.utility.modelEvaluation.ModelStore;
import yueyueGo.utility.modelEvaluation.ThresholdData;

public class ModelPredictor {

	/*
	 * 存储月度预测状态的变量（用以控制月度阈值）
	 */
	private PredictStatus m_predictStatus;

	/*
	 * 初始化新的predictStatus对象，这个适合于回测时或预测的第一天
	 */
	public ModelPredictor(String modelID, String yearSplit, String policy) {
		// 生成存储预测中间结果的对象
		m_predictStatus = new PredictStatus(modelID, yearSplit, policy);
	}

	/*
	 * 在既存predictStatus对象上生成status，这个适合于平时预测时
	 */
	public ModelPredictor(PredictStatus predictStatus) {
		m_predictStatus = predictStatus;
	}

	public PredictStatus getPredictStatus() {
		return m_predictStatus;
	}

	/*
	 * 预测数据 globalResultsHolder parameter will be changed in this method!
	 */
	public void predictData(AbstractModel clModel, GeneralInstances dataToPredict, ResultsHolder globalResultsHolder,
			String yearSplit, String policy) throws Exception {

		// 以防万一，校验predictStatus对象的有效性
		if (m_predictStatus.verifyStatusData(clModel.getIdentifyName(), policy) == false) {
			throw new Exception(
					"predictStatus data mismatch for policy=" + policy + " modelID=" + clModel.getIdentifyName());
		}

		// 第二步： 从评估文件中获取模型，并校验之。
		// 先找对应的评估结果
		// 获取评估数据
		ThresholdData thresholdData = clModel.m_evaluationStore.loadDataFromFile();
		// 校验读入的thresholdData内容是否可以用于目前评估
		String msg = clModel.m_evaluationStore.validateThresholdData(thresholdData);
		if (msg == null) {
			System.out.println("ThresholdData verified for target yearsplit " + yearSplit+ " using thredholdFile="+clModel.m_evaluationStore.getWorkFilePath()+clModel.m_evaluationStore.getEvalFileName());
		} else {
			throw new Exception(msg);
		}

		// 先new出一个格式对象进行格式校验
		GeneralInstances predictDataFormat = new WekaInstances(dataToPredict, 0);
		if (clModel instanceof NominalModel) {
			// Nominal数据的格式需要额外处理
			predictDataFormat = ((NominalModel) clModel).processDataForNominalClassifier(predictDataFormat);
		}

		// 删除已保存的ID列，让待分类数据与模型数据一致 （此处的index是从1开始）
		BaseInstanceProcessor instanceProcessor = InstanceHandler.getHandler(predictDataFormat);
		// 删除已知的比模型更多的数据列，准备校验（下面的函数这三个列不存在也不会报错，比如每日预测数据里没有YEARMONTH）
		predictDataFormat = instanceProcessor.removeAttribs(predictDataFormat,
				new String[] { ArffFormat.ID, ArffFormat.TRADE_DATE, ArffFormat.YEAR_MONTH });

		// 获取预测文件中的应该用哪个modelYearSplit的模型
		String modelYearSplit = thresholdData.getModelYearSplit();
		// 从评估结果中找到正向模型文件。
		ModelStore modelStore = new ModelStore(clModel.m_evaluationStore.getWorkFilePath(),
				thresholdData.getModelFileName(), modelYearSplit);
		Classifier model = modelStore.loadModelFromFile(predictDataFormat, yearSplit);
		System.out.println("预测用模型文件:  " + modelStore.getWorkFilePath() + modelStore.getModelFileName());

		// 获取预测文件中的应该用哪个反向模型的模型
		String reversedModelYearSplit = thresholdData.getReversedModelYearSplit();

		Classifier reversedModel = null;
		ModelStore reversedModelStore = null;
		if (reversedModelYearSplit.equals(modelYearSplit)) {// 正向模型和方向模型是同一模型的。
			reversedModel = model;
			reversedModelStore = modelStore;
		} else {
			// 从评估结果中找到反向模型文件。
			reversedModelStore = new ModelStore(clModel.m_evaluationStore.getWorkFilePath(),
					thresholdData.getReversedModelFileName(), reversedModelYearSplit);
			// 获取model
			reversedModel = reversedModelStore.loadModelFromFile(predictDataFormat, yearSplit);
		}
		System.out.println("预测用反向模型文件" + reversedModelStore.getWorkFilePath() + reversedModelStore.getModelFileName());




		// 输出分类器的参数 TODO 须清理这一段逻辑，并不是很必要输出这么多
		double[] modelAUC = thresholdData.getModelAUC();
		double reversedThreshold = thresholdData.getReversedThreshold();
		double reversedPercentile = thresholdData.getReversedPercent();
		//获取选股的目标比率
		TargetSelectRatio selectRatio=clModel.m_selectRatioConfig.getEvaluationInstance(policy);
		double targetPercentile = selectRatio.getTargetPercentile();
		
		if ("".equals(yearSplit)) { 
			// 输出评估结果及所使用阈值及期望样本百分比
			String evalSummary = "\r\n\t ( with params: modelYearSplit=" + modelYearSplit
			 + " , targetPercentile="+ FormatUtility.formatPercent(targetPercentile / 100) ;
			
			evalSummary += " ,reversedModelYearSplit=" + reversedModelYearSplit
			 +" ,reversedThreshold="+ FormatUtility.formatDouble(reversedThreshold, 0, 3)
					+ " , reversedPercentile=" + FormatUtility.formatPercent(reversedPercentile / 100);
			evalSummary += " ,modelAUC@focusAreaRatio=";
			double[] focusAreaRatio = thresholdData.getFocosAreaRatio();
			for (int i = 0; i < focusAreaRatio.length; i++) {
				evalSummary += FormatUtility.formatDouble(modelAUC[i], 1, 4) + "@"
						+ FormatUtility.formatPercent(focusAreaRatio[i], 3, 0) + ", ";
			}
			evalSummary += " )\r\n";
			clModel.m_classifySummaries.appendEvaluationSummary(evalSummary);

		} else {

			// 输出评估结果及所使用阀值及期望样本百分比
			String evalSummary = yearSplit + "," + policy + ",";
			evalSummary += reversedModelYearSplit + "," + FormatUtility.formatPercent(reversedPercentile / 100) + ","
					+ FormatUtility.formatDouble(reversedThreshold, 0, 3) + ",";
			evalSummary += modelYearSplit + "," + FormatUtility.formatPercent(targetPercentile / 100) + ",";
			for (double d : modelAUC) {
				evalSummary += FormatUtility.formatDouble(d, 0, 4) + ",";
			}
			evalSummary += "\r\n";
			clModel.m_classifySummaries.appendEvaluationSummary(evalSummary);
		}		
		
		// 按照globalResultsHolder的格式创建一个空的预测结果对象
		ResultsHolder predictResults = new ResultsHolder(globalResultsHolder);

		// 调用分类器进行预测
		predictUsingModels(clModel, dataToPredict, predictResults, model, reversedModel);



		// 根据预测结果完成选股
		ResultsHolder judgeResults = judgePredictResults(clModel, predictResults, yearSplit, policy, thresholdData);
		// 将选股结果加入到全局预测容器内
		globalResultsHolder.addResults(judgeResults);

	}

	/**
	 * 使用模型输出预测值（这里的预测值只是预测概率，并未做选股）
	 * 
	 * @param clModel
	 * @param incomingData
	 * @param result
	 * @param classifier
	 * @param reversedClassifier
	 * @throws Exception
	 */
	protected void predictUsingModels(AbstractModel clModel, GeneralInstances orignalData, ResultsHolder result,
			Classifier classifier, Classifier reversedClassifier) throws Exception {

		// 从输入数据中删除不必要的列，让待分类数据与模型数据一致
		// 而此处的orignalData还保留（结果数据里有不少还要其中取出）
		GeneralInstances dataForModel = InstanceHandler.getHandler(orignalData).removeAttribs(orignalData,
				new String[] { ArffFormat.ID, ArffFormat.TRADE_DATE, ArffFormat.YEAR_MONTH });

		if (clModel instanceof NominalModel) {
			// 二分类器数据的格式需要额外处理，要收益率转换为二分类器的正、负值
			dataForModel = ((NominalModel) clModel).processDataForNominalClassifier(dataForModel);
		}

		// 留下一个校验位
		double[] ids = orignalData.attributeToDoubleArray(ArffFormat.ID_POSITION - 1);
		

		// 开始循环，用分类模型和阈值对每一条数据进行预测，并存入输出结果集
		double pred;
		double reversedPred;
		
		//找出要拷贝的字段定义
		HashMap<GeneralAttribute,GeneralAttribute> attribsToCopy=result.findAttributesToCopy(orignalData);

		for (int i = 0; i < dataForModel.numInstances(); i++) {
			GeneralInstance originalRow = orignalData.instance(i); // 原始的数据
			GeneralInstance currentRowForModel = dataForModel.instance(i);
			pred = ModelPredictor.classify(classifier, currentRowForModel); // 调用分类函数
			if (reversedClassifier == classifier) { // 如果反向评估模型是同一个类
				reversedPred = pred;
			} else {
				reversedPred = ModelPredictor.classify(reversedClassifier, currentRowForModel); // 调用反向评估模型的分类函数
			}

			// 准备输出结果
			DataInstance resultRow = new DataInstance(result.getResultInstances().numAttributes());
			result.setInstanceDateSet(resultRow);
			// 那些无须计算的校验字段的值直接从测试数据集拷贝到输出结果集
			ResultsHolder.copyDefinedAttributes(attribsToCopy,originalRow, resultRow);

			// 将获得的预测值设定入结果集
			resultRow.setValue(result.predictAttribute(), pred);
			resultRow.setValue(result.reversedPredAttribute(), reversedPred);
			if (resultRow.value(ArffFormat.ID_POSITION - 1) != ids[i]) {
				throw new Exception(" ID verfifications in predict process failed!");
			}
			result.addInstance(resultRow);
		}
	}

	/**
	 * 
	 * 根据分类器的预测结果，结合阈值数据，判断最终的选股结果并输出
	 * 
	 * @param clModel
	 * @param predictResultHolder
	 * @param yearSplit
	 * @param policy
	 * @param thresholdData
	 * @return
	 * @throws Exception
	 */
	protected ResultsHolder judgePredictResults(AbstractModel clModel, ResultsHolder predictResultHolder, String yearSplit,
			String policy, ThresholdData thresholdData) throws Exception {
		double[] thresholds = thresholdData.getThresholds();
		double[] percentiles = thresholdData.getPercentiles();
		
		//查找出缺省值
		TargetSelectRatio selectRatio=clModel.m_selectRatioConfig.getEvaluationInstance(policy);
		double targetPercentile = selectRatio.getTargetPercentile();
		//调用binarySearch之前，需要先把percentile升序排列
		Arrays.sort(thresholds);
		Arrays.sort(percentiles);
		int defaultIndex = Arrays.binarySearch(percentiles, targetPercentile);
		if (defaultIndex<0){
			defaultIndex=-1*defaultIndex-1;
			System.out.println("cannot find exactly matched targetPercentile("+targetPercentile+") in thredshold Data. Use nearest percentile="+percentiles[defaultIndex]);
		}

		// 初始值（下面的循环内函数内也会重设）
		double thresholdMin = thresholds[defaultIndex];
		// 判断为0的阈值，小于该值意味着该模型坚定认为其为0 （这是合并多个模型预测时使用的）
		double reversedThresholdMax = thresholdData.getReversedThreshold();

		// 取出预测值的结果集作为输入
		GeneralInstances predictedResultInstances = predictResultHolder.getResultInstances();
		// 按照输入的预测数值result的格式创建一个空的对象
		ResultsHolder judgeResults = new ResultsHolder(predictResultHolder);

		// 获取所有的不同tradeDate，并排序。
		ArrayList<Date> tradeDateList = getTradeDateList(predictedResultInstances);
		SimpleDateFormat sdFormat = new SimpleDateFormat(ArffFormat.ARFF_DATE_FORMAT);

		// 循环处理每天的数据
		StringBuffer dailyStatusBuffer = new StringBuffer();

		int tradeDateIndex = predictedResultInstances.attribute(ArffFormat.TRADE_DATE).index() + 1; // 下面过滤的地方index是从1开始
		String oneDay = null;
		for (Date date : tradeDateList) {
			// 获取一日的数据
			oneDay = sdFormat.format(date);
			String split = WekaInstanceProcessor.WEKA_ATT_PREFIX + tradeDateIndex + " is '" + oneDay + "'";
			GeneralInstances oneDayData = InstanceHandler.getHandler(predictedResultInstances)
					.getInstancesSubset(predictedResultInstances, split);
			int nextDataSize = oneDayData.size();

			// System.out.println("got one day data. date="+oneDay+"
			// number="+nextDataSize);

			// 输出统计值
			dailyStatusBuffer.append(yearSplit);
			dailyStatusBuffer.append(',');
			dailyStatusBuffer.append(policy);
			dailyStatusBuffer.append(',');
			dailyStatusBuffer.append(oneDay);
			dailyStatusBuffer.append(',');
			dailyStatusBuffer.append(1 - targetPercentile / 100);
			dailyStatusBuffer.append(',');
			dailyStatusBuffer.append(m_predictStatus.getCummulativePredicted());
			dailyStatusBuffer.append(',');
			dailyStatusBuffer.append(m_predictStatus.getCummulativeSelected());
			dailyStatusBuffer.append(',');
			dailyStatusBuffer.append(m_predictStatus.getAverageSelectedRatio());
			dailyStatusBuffer.append(',');

			// 开始调整阈值
			thresholdMin = adjustThreshold(nextDataSize, thresholds, percentiles, targetPercentile, defaultIndex,
					dailyStatusBuffer);

//			if (reversedThreshold > thresholdData.getDefaultThreshold()) {
//				String reversedModelYear = reversedModel.getModelYearSplit();
//				String selectdModelYear = selectedModel.getModelYearSplit();
//				if (reversedModelYear.equals(selectdModelYear)) {
//					throw new Exception("thread:" + Thread.currentThread().getName() + "fatal error!!! reversedThreshold("
//							+ reversedThreshold + ") > threshold(" + thresholdData.getThresholds()
//							+ ") using same model (modelyear=" + reversedModelYear + ")");
//				} else {
//					System.out.println("thread:" + Thread.currentThread().getName() + "使用不同模型时反向阀值大于正向阀值了"
//							+ "reversedThreshold(" + reversedThreshold + ")@" + reversedModelYear + " > threshold("
//							+ thresholdData.getThresholds() + ")@" + selectdModelYear);
//				}
//			}

			// 具体预测
			judgeResultByMiniBatch(judgeResults, oneDayData, dailyStatusBuffer, thresholdMin, reversedThresholdMax);
		}
		clModel.m_classifySummaries.appendDailySummary(dailyStatusBuffer.toString());
		return judgeResults;
	}

	/**
	 * 按天预测
	 */
	private void judgeResultByMiniBatch(ResultsHolder judgeResults, GeneralInstances predectedValueInstances,
			StringBuffer dailyStatus, double thresholdMin, double reversedThresholdMax) throws Exception {

		int selectedCount = 0;
		int predictedCount = predectedValueInstances.numInstances();
		// 用阈值对每一条数据进行选股判定
		double pred;
		double reversedPred;
		for (int i = 0; i < predictedCount; i++) {
			// 从输入中获取一行
			GeneralInstance predictedRow = predectedValueInstances.instance(i);
			// 获取预测的数据结果
			pred = predictedRow.value(judgeResults.predictAttribute());
			reversedPred = predictedRow.value(judgeResults.reversedPredAttribute());
			// 计算选股结果
			double selected = ResultsHolder.VALUE_NOT_SURE;
			// 先用反向模型判断
			if (reversedPred < reversedThresholdMax) {
				selected = ResultsHolder.VALUE_NEVER_SELECT;// 反向模型坚定认为当前数据为0
				// （这是合并多个模型预测时使用的）
			}
			// 如果正向模型与方向模型得出的值矛盾（在使用不同模型时），则用正向模型的数据覆盖方向模型（因为毕竟正向模型的ratio比较小）
			if (pred >= thresholdMin) { // 本模型估计当前数据是1值
				selected = ResultsHolder.VALUE_SELECTED;
				selectedCount++;
			}
			// 将该行格式挂载至输出结果集中
			judgeResults.setInstanceDateSet(predictedRow);
			// 设置选择与否
			predictedRow.setValue(judgeResults.selectedAttribute(), selected);
			// 将该行数据记入输出结果中
			judgeResults.addInstance(predictedRow);

		}
		// 更新统计
		m_predictStatus.updateAfterCurrentPeriod(predictedCount, selectedCount);

		// 记录统计数值至CSV
		dailyStatus.append(thresholdMin);
		dailyStatus.append(',');
		dailyStatus.append(predictedCount);
		dailyStatus.append(',');
		dailyStatus.append(selectedCount);
		dailyStatus.append(',');
		double currentRatio=0;
		if (predictedCount>0){
			currentRatio=((double) selectedCount) / predictedCount;
		}
		dailyStatus.append(currentRatio);
		dailyStatus.append(',');
		dailyStatus.append(m_predictStatus.getAverageSelectedRatio());
		dailyStatus.append("\r\n");
	}

	/**
	 * 获取输入数据中的所有日期，并升序排列
	 * 
	 * @param data
	 */
	private ArrayList<Date> getTradeDateList(GeneralInstances data) throws Exception {
		GeneralAttribute tradeDateAtt = data.attribute(ArffFormat.TRADE_DATE);
		ArrayList<Date> tradeDateList = new ArrayList<Date>();
		SimpleDateFormat sdFormat = new SimpleDateFormat(ArffFormat.ARFF_DATE_FORMAT);
		String current = "2099/12/31";
		String next = null;
		for (int i = 0; i < data.numInstances(); i++) {
			next = data.get(i).stringValue(tradeDateAtt);
			if (current.equals(next) == false) {
				current = next;
				// 转换为日期以便于排序，免得将字符串中的2017/10/5 排在2017/10/21之后了
				Date d = sdFormat.parse(next);
				tradeDateList.add(d);
			}
		}
		Collections.sort(tradeDateList);
		return tradeDateList;
	}

	/**
	 * 动态调整阈值 如果迄今为止已选股票的百分比已经大于threshold中的预期百分比，则提升阈值单位。 
	 * 之前版本是按总数调整似乎不尽合理（如果某天大涨选了很多票，会影响后续天数的选股）
	 * 目前改为日平均的方式（但也要排除日100%但选股极少的情况）
	 * 是否每日分母的机会数用上月的日平均？或中位数？
	 * @param thresholds
	 * @param percentiles
	 * @param targetPercentile
	 * @param currentIndex
	 * @return
	 */
	private double adjustThreshold(int nextBatchSize,double[] thresholds, double[] percentiles,
			double targetPercentile, int currentIndex, StringBuffer dailyStatusBuffer) {
		double adjustedThreshold = 0.0;

		double currentPercentile = (1 - m_predictStatus.getAverageSelectedRatio()) * 100;
		// 如果迄今为止已选股票的百分比已经大于threshold中的预期百分比，则提升阈值单位。否则降低它。
		double adjustedPercentile;
		if (Double.isNaN(currentPercentile)) { // 还未开始本批次预测时
			adjustedThreshold = thresholds[currentIndex]; // 缺省的判断为1的阈值，大于该值意味着该模型判断其为1
			adjustedPercentile = targetPercentile;
		} else {

			// 找到调整的阈值数量
			int nonZeroPredictedDays=m_predictStatus.getNonZeroPredictedDays();

			adjustedPercentile= targetPercentile*(nonZeroPredictedDays+1)-currentPercentile*nonZeroPredictedDays;
//			double predictedCount = m_predictStatus.getCummulativePredicted();
//			adjustedPercentile = (targetPercentile * (predictedCount + nextBatchSize)
//					- currentPercentile * predictedCount) / nextBatchSize;


			// 根据需要调节的Percentile找相应的threshold，因为threshold数组的关系，选股比例最少也是0.001，最大是0.1
			// percentile目前假定为是个递减数组 TODO （应该可以改成binarySearch方式实现）
			int adjustedInex = findNearestIndexInArray(percentiles, adjustedPercentile);
			adjustedThreshold = thresholds[adjustedInex]; // 判断为1的阈值，大于该值意味着该模型判断其为1

		}
		dailyStatusBuffer.append(adjustedPercentile);
		dailyStatusBuffer.append(',');
		return adjustedThreshold;
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

	/**
	 * 给定一个从大到小排序的数组，查找数组内最接近某一数组的数值的下标
	 * （可以考虑用BinarySearch代替）
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
		// System.out.println("find targetValue(" + targetValue + ") near" +
		// sortedValues[currentIndex]);
		return currentIndex;
	}

	// protected double getShouyilv(GeneralInstances a_shouyilvCache, int index,
	// double id, double newClassValue)
	// throws Exception {
	// if (a_shouyilvCache == null) {
	// return Double.NaN;
	// }
	// if (index >= a_shouyilvCache.numInstances()) {
	// throw new Exception("Old Class Value has not been cached for index: " +
	// index);
	// }
	// double cachedID = a_shouyilvCache.instance(index).value(0);
	// if (cachedID != id) {
	// throw new Exception("Data inconsistent error! Cached old class value id =
	// " + cachedID
	// + " while incoming id =" + id + " for index: " + index);
	// }
	// double shouyilv = a_shouyilvCache.instance(index).classValue();
	// if (newClassValue == 0 && shouyilv > 0 || newClassValue == 1 && shouyilv
	// <= 0) {
	// throw new Exception("Data inconsistent error! Cached old class value id =
	// " + shouyilv
	// + " while incoming newClassValue =" + newClassValue + " for index: " +
	// index
	// + " @m_positiveLine=0");
	// }
	// return shouyilv;
	// }
	//
	// /*
	// * 暂存收益率 （可以用于将来的校验）
	// */
	// protected GeneralInstances cacheShouyilvData(GeneralInstances inData) {
	// GeneralInstances cache = CreateCachedOldClassInstances();
	// double shouyilv = 0;
	// for (int i = 0; i < inData.numInstances(); i++) {
	// shouyilv = inData.instance(i).classValue();
	// // 暂存收益率
	//
	// double id = inData.instance(i).value(0);
	// DataInstance cacheRow = new DataInstance(cache.numAttributes());
	// cacheRow.setDataset(cache);
	// cacheRow.setValue(0, id);
	// cacheRow.setValue(1, shouyilv);
	// cache.add(cacheRow);
	// }
	// return cache;
	// }
	//
	// // 创建暂存oldClassValue（目前情况下为暂存收益率）的arff结构（id-收益率）
	// protected GeneralInstances CreateCachedOldClassInstances() {
	// DataAttribute pred = new DataAttribute(ArffFormat.ID);
	// DataAttribute shouyilv = new DataAttribute(ArffFormat.SHOUYILV);
	// ArrayList<GeneralAttribute> fvWekaAttributes = new
	// ArrayList<GeneralAttribute>(2);
	// fvWekaAttributes.add(pred);
	// fvWekaAttributes.add(shouyilv);
	// GeneralInstances structure = new DataInstances("cachedOldClass",
	// fvWekaAttributes, 0);
	// structure.setClassIndex(structure.numAttributes() - 1);
	// return structure;
	// }

}
