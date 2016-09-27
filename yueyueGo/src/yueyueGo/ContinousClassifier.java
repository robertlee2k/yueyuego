package yueyueGo;

import java.util.ArrayList;
import java.util.Vector;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import yueyueGo.utility.EvaluationBenchmark;
import yueyueGo.utility.EvaluationParams;
import yueyueGo.utility.FormatUtility;
import yueyueGo.utility.InstanceUtility;

public abstract class ContinousClassifier extends BaseClassifier {
	

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 6815050020696161183L;




	@Override
	//具体的模型评估方法
	protected Vector<Double> doModelEvaluation(EvaluationBenchmark benchmark ,Instances train,
			Classifier model, EvaluationParams evalParams) throws Exception {

		
		
		double meanABError=benchmark.getEval_mean_ABS_error();

		DescriptiveStatistics stat_pred = new DescriptiveStatistics();
		double pred = 0.0;
		//新创建的Instances对象，用于存放预测值和实际值
		Instances predictions = CreateEvalInstances();

		System.out.println(" model evaluation classifying using evaluation data.....");
		
		//全量评估
		int startNum=0; 
		// Add the data from the evaluation result
		for (int i = startNum; i < train.numInstances(); i++) {
			Instance curr = train.instance(i);
			pred = model.classifyInstance(curr);
			
			//把预测值和实际值放入添加到instances对象中
			Instance OnePrediction = new DenseInstance(predictions.numAttributes());
			OnePrediction.setDataset(predictions);
			OnePrediction.setValue(0, pred);
			OnePrediction.setValue(1, curr.classValue());
			predictions.add(OnePrediction);
			//存入统计对象中，未来计算统计值
			stat_pred.addValue(pred);

		}

		Vector<Double> v = computeThresholds( benchmark ,evalParams, stat_pred, predictions);
		v.add(new Double(meanABError));

		return v;
	}

	//计算给定预测结果集的最佳的阀值选择区间
	protected Vector<Double> computeThresholds( EvaluationBenchmark benchmark,EvaluationParams evalParams,
			DescriptiveStatistics stat_pred, Instances predictions)
			throws Exception {
		//获取预测值的对应百分位数值
		double tp_fp_ratio=benchmark.getEval_tp_fp_ratio()*evalParams.getLift_up_target();
		System.out.println("start from the trying_tp_fp based on training history data = "+tp_fp_ratio + " / while  lift up target="+evalParams.getLift_up_target());
		double currentPercent =(1-evalParams.getUpper_limit()) * 100;
		
		double currentThreshold=0.15; 
		double thresholdBottom=0;
		double startPercent=0;
		double startTPFP=0;
		double thresholdTop=9999;
		double endPercent=100;
		double endTPFP=9999;
		double currentTPFP=0;
		double maxTPFP=-99999;
		boolean startFound=false;
		
		Instances currentInstances=predictions;
		while (currentPercent<100){ // 从指定位置开始找收益率最大的位置，选择样本最大往最小的的方式找。
			System.out.println("-trying current percent : "+FormatUtility.formatDouble(currentPercent));
			currentThreshold= stat_pred.getPercentile(currentPercent);
			currentInstances=InstanceUtility.getInstancesSubset(currentInstances, "ATT1 >= "+FormatUtility.formatDouble(currentThreshold, 3, 4) );
			currentTPFP=calculateTPFP(currentInstances);
			

			//在达到最大值之前持续寻找起点
			if(currentPercent<=100-evalParams.getLower_limit()*100){
				if (startFound==false){// 如果还没找到起点
					//如果已经达到了lift的要求，起点threshold就不用再找了，区间终点要一直找下去
					if (currentTPFP >=tp_fp_ratio){
						thresholdBottom=currentThreshold;
						startPercent=currentPercent;
						startTPFP=currentTPFP;
						endPercent=100;
						thresholdTop=999;
						endTPFP=999;
						System.out.println("...... start threshold found in advance!");
						startFound=true;
					} else if (currentTPFP>maxTPFP ) {
						//如果这是个目前的好结果，但还未达标，暂时认为它可以，暂设起止区间找下去
						maxTPFP=currentTPFP;
						thresholdBottom=currentThreshold;
						startPercent=currentPercent;
						startTPFP=currentTPFP;
						endPercent=100;
						thresholdTop=999;
						endTPFP=999;
						System.out.println("......temporarilly selected.");
					}
				}
			}
			//找threshold区间终点，这里不管起点是否已是确定选出的，都是一直找，要找到最小的那个TPFP才结束(阀值是0.5，再小就产生负效果了）
			if (  currentTPFP<startTPFP && currentTPFP<endTPFP && currentTPFP>0.5){
				if (currentPercent-startPercent>=3){ //window size最小为3，防止找的间距太小
					thresholdTop=currentThreshold;
					endPercent=currentPercent;
					endTPFP=currentTPFP;
				}
			}
			currentPercent+=1;
		} 

		// 如果找不到合适的值，似乎应该不可能出现这个情况
		if (thresholdBottom==0){ 
			thresholdBottom=currentThreshold; // 最小样本处的值
			startPercent=currentPercent;
		}
		System.out.println("----finally, we choose value at percentile: from "+startPercent+ " to "+endPercent);
		System.out.println("----threshold is between: "+ Double.toString(thresholdBottom)+" - "+Double.toString(thresholdTop));
		Vector<Double> v = new Vector<Double>();
		v.add(new Double(thresholdBottom));
		v.add(new Double(thresholdTop));
		v.add(new Double(startPercent));
		v.add(new Double(endPercent));
		return v;
	}

	//计算给定预测结果集的TPFP值
	protected double calculateTPFP(Instances predictions){
		double result=0;
		double actual=0;
		int positive=0;
		int negative=0;
		for (int i = 0; i < predictions.numInstances(); i++) {
			actual=predictions.instance(i).classValue();
			result+=actual;
			if (actual>0){
				positive++;
			}else {
				negative++;
			}
		}
		//result=result/(positive+negative);
		if (negative==0) 
			result=100;
		else
			result=(double)positive/negative;
		System.out.println("current TP/FP= "+ FormatUtility.formatDouble(result)+ " positive number= "+positive+" negative number= "+negative);
		return result;
	}
	
	
	//已废弃
//	protected double computeThreshold(double sample_limit,double meanAbsoluteError,
//			DescriptiveStatistics stat_pred, DescriptiveStatistics stat_actual) {
//		// Compute 实际收益率的percentile
//		double startPercent = (1-sample_limit) * 100;
//		double profit = 0.0;
//		while (true) {
//			profit = stat_actual.getPercentile(startPercent);
//			if (profit < PROFIT_LOW_LIMIT) // 给定百分位收益率小于可参与阀值，继续查找
//				startPercent += 1;
//			else
//				break;
//			if (startPercent > 100) {
//				startPercent = 100;
//				break;
//			}
//		}
//
//		System.out.println(" model selected percent(estimation) is : "
//				+ Double.toString(startPercent));
//		System.out.println(" model selected profit(estimation) is : "
//				+ Double.toString(profit));
//		// 根据查找到的startPercent来取得预测值的百分位阀值
//		double threshold = stat_pred.getPercentile(startPercent);
//		//profit+meanAbsoluteError;
//		return threshold;
//	}


	//对于连续变量，返回预测值
	protected  double classify(Classifier model,Instance curr) throws Exception {
		double pred =  model.classifyInstance(curr);
		return pred;
	}

	// 创建评估模型的arff结构（预测值-实际值）
	protected Instances CreateEvalInstances() {
		Attribute pred = new Attribute(ArffFormat.RESULT_PREDICTED_PROFIT);
		Attribute shouyilv = new Attribute(ArffFormat.SHOUYILV);
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>(2);
		fvWekaAttributes.add(pred);
		fvWekaAttributes.add(shouyilv);
		Instances structure = new Instances("predictions", fvWekaAttributes, 0);

		structure.setClassIndex(structure.numAttributes() - 1);
		return structure;
	}


}
