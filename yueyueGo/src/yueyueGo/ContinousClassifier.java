package yueyueGo;

import java.util.ArrayList;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.EvaluationUtils;
import weka.classifiers.evaluation.Prediction;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import yueyueGo.utility.EvaluationBenchmark;
import yueyueGo.utility.EvaluationParams;
import yueyueGo.utility.FormatUtility;
import yueyueGo.utility.NumericThresholdCurve;

public abstract class ContinousClassifier extends BaseClassifier {




	/**
	 * 
	 */
	private static final long serialVersionUID = 6815050020696161183L;


	@Override
	//具体的模型评估方法

	protected Vector<Double> doModelEvaluation(EvaluationBenchmark benchmark ,Instances evalData,Classifier model,EvaluationParams evalParams)
			throws Exception {

		// generate curve
		EvaluationUtils eUtils=new EvaluationUtils();
		ArrayList<Prediction> predictions=eUtils.getTestPredictions(model, evalData);
		NumericThresholdCurve tc = new NumericThresholdCurve();
		Instances result = tc.getCurve(predictions);

		double thresholdBottom = 0;
		double startPercent=100;
		int round=1;


		double tp_fp_bottom_line=benchmark.getEval_tp_fp_ratio();  
		System.out.println("use the tp_fp_bottom_line based on training history data = "+tp_fp_bottom_line);
		double trying_tp_fp=benchmark.getEval_tp_fp_ratio()*evalParams.getLift_up_target();
		System.out.println("start from the trying_tp_fp based on training history data = "+trying_tp_fp + " / while  lift up target="+evalParams.getLift_up_target());
		while (thresholdBottom == 0 && trying_tp_fp > tp_fp_bottom_line){

			Vector<Double> v_threshold = computeThresholds(trying_tp_fp,evalParams, result);
			thresholdBottom=v_threshold.get(0).doubleValue();
			startPercent=100*(1-v_threshold.get(1).doubleValue()); //第二行存的是sampleSize
			if (thresholdBottom>0){
				System.out.println(" threshold got at trying round No.: "+round);
				break;
			}else {
				trying_tp_fp=trying_tp_fp*0.95;
				round++;
			}
		}// end while;
		if (thresholdBottom==0){  //如果无法找到合适的阀值
			thresholdBottom=computeDefaultThresholds(evalParams,result);//设置下限
		}

		Vector<Double> v = new Vector<Double>();
		v.add(new Double(thresholdBottom));
		//TODO 先将模型阀值上限设为1，以后找到合适的算法再计算。
		double thresholdTop=1; 
		v.add(new Double(thresholdTop));
		v.add(new Double(startPercent));
		//先将模型end percent设为100，以后找到合适的算法再计算。
		v.add(new Double(100));
		double meanABError=benchmark.getEval_mean_ABS_error();
		v.add(new Double(meanABError));
		return v;
	}
	
	private double computeDefaultThresholds(EvaluationParams evalParams, Instances result){
		double sample_limit=evalParams.getLower_limit(); 
		double sampleSize;
		double threshold=-1;
		Attribute att_threshold = result.attribute("Threshold");
		Attribute att_samplesize = result.attribute("Sample Size");


		for (int i = 0; i < result.numInstances(); i++) {
			Instance curr = result.instance(i);
			sampleSize = curr.value(att_samplesize); // to get sample range
			if (FormatUtility.compareDouble(sampleSize,sample_limit)==0) {
				threshold = curr.value(att_threshold);
				break;
			}
		}
		if (threshold==-1){
			System.err.println("seems error! cannot get threshold at sample_limit="+sample_limit+" default threshold is used");
//			threshold=evalParams.getDefault_threshold();
		}else {
			System.err.println("got default threshold "+ threshold+" at sample_limit="+sample_limit);
		}
		return threshold;
		
	}
	
	//具体的模型阀值计算方法
	protected Vector<Double> computeThresholds(double tp_fp_ratio,EvaluationParams evalParams, Instances result) {

		double sample_limit=evalParams.getLower_limit(); 
		double sample_upper=evalParams.getUpper_limit();

		double thresholdBottom = 0.0;
		double lift_max = 0.0;
		double finalSampleSize = 0.0;
		double sampleSize = 0.0;
		double tp = 0.0;
		double fp = 0.0;
		//			double tpr=0;
		//			double fpr=0;
		double final_tp=0.0;
		double final_fp=0.0;
		//			double final_deviation=-999999999.9;
		Attribute att_tp = result.attribute("True Positives");
		Attribute att_fp = result.attribute("False Positives");
		//			Attribute att_tpr = result.attribute("True Positive Rate"); 
		//			Attribute att_fpr= result.attribute("False Positive Rate");
		Attribute att_lift = result.attribute("Lift");
		Attribute att_threshold = result.attribute("Threshold");
		Attribute att_samplesize = result.attribute("Sample Size");


		for (int i = 0; i < result.numInstances(); i++) {
			Instance curr = result.instance(i);
			sampleSize = curr.value(att_samplesize); // to get sample range
			if (sampleSize >= sample_limit && sampleSize <=sample_upper) {
				tp = curr.value(att_tp);
				fp = curr.value(att_fp);
				//					tpr = curr.value(att_tpr);
				//					fpr = curr.value(att_fpr);
				if (tp>fp*tp_fp_ratio ){
					//						TODO 试了求TPR-FPR的最大值(tpr-fpr)，效果差不多，先恢复原始的
					//						if (tp-fp > final_deviation ) {
					thresholdBottom = curr.value(att_threshold);
					finalSampleSize = sampleSize;
					lift_max=curr.value(att_lift);
					final_tp=tp;
					final_fp=fp;
					//							final_deviation=tp-fp;
					//						}
				}
			}
		}
		if (thresholdBottom>0){ //找到阀值时输出
			System.out.print("################################################thresholdBottom is : " + FormatUtility.formatDouble(thresholdBottom));
			System.out.print("/samplesize is : " + FormatUtility.formatPercent(finalSampleSize) );
			System.out.print("/True Positives is : " + final_tp);
			System.out.print("/False Positives is : " + final_fp);
			System.out.println("/lift max is : " + FormatUtility.formatDouble(lift_max));
		}

		Vector<Double> v = new Vector<Double>();
		v.add(new Double(thresholdBottom));
		v.add(new Double(finalSampleSize));
		return v;
	}

	//	//具体的模型评估方法
	//	protected Vector<Double> doModelEvaluation_old(EvaluationBenchmark benchmark ,Instances train,
	//			Classifier model, EvaluationParams evalParams) throws Exception {
	//
	//		
	//		double meanABError=benchmark.getEval_mean_ABS_error();
	//
	//		DescriptiveStatistics stat_pred = new DescriptiveStatistics();
	//		double pred = 0.0;
	//		//新创建的Instances对象，用于存放预测值和实际值
	//		Instances predictions = CreateEvalInstances();
	//
	//		System.out.println(" model evaluation classifying using evaluation data.....");
	//		
	//		//全量评估
	//		int startNum=0; 
	//		// Add the data from the evaluation result
	//		for (int i = startNum; i < train.numInstances(); i++) {
	//			Instance curr = train.instance(i);
	//			pred = model.classifyInstance(curr);
	//			
	//			//把预测值和实际值放入添加到instances对象中
	//			Instance OnePrediction = new DenseInstance(predictions.numAttributes());
	//			OnePrediction.setDataset(predictions);
	//			OnePrediction.setValue(0, pred);
	//			OnePrediction.setValue(1, curr.classValue());
	//			predictions.add(OnePrediction);
	//			//存入统计对象中，未来计算统计值
	//			stat_pred.addValue(pred);
	//
	//		}
	//
	//		Vector<Double> v = computeThresholds( benchmark ,evalParams, stat_pred, predictions);
	//		v.add(new Double(meanABError));
	//
	//		return v;
	//	}
	//	
	//	//计算给定预测结果集的最佳的阀值选择区间
	//	protected void computeThresholds_new( EvaluationBenchmark benchmark,EvaluationParams evalParams,
	//				DescriptiveStatistics stat_pred, Instances predictions)
	//				throws Exception {
	//			//获取预测值的对应百分位数值
	//			double tp_fp_ratio=benchmark.getEval_tp_fp_ratio()*evalParams.getLift_up_target();
	//			System.out.println("start from the trying_tp_fp based on training history data = "+tp_fp_ratio + " / while  lift up target="+evalParams.getLift_up_target());
	//			double currentPercent =(1-evalParams.getUpper_limit()) * 100;
	//			
	//			
	//			double currentTPFP=0;
	//			double currentThreshold=0;
	//			Instances result=null;
	//			
	//			Instances currentInstances=predictions;
	//			while (currentPercent<100){ // 从指定位置开始找收益率最大的位置，选择样本最大往最小的的方式找。
	//				System.out.println("-get TPFP for current percent : "+FormatUtility.formatDouble(currentPercent));
	//				currentThreshold= stat_pred.getPercentile(currentPercent);
	//				currentInstances=InstanceUtility.getInstancesSubset(currentInstances, "ATT1 >= "+FormatUtility.formatDouble(currentThreshold, 3, 4) );
	//				currentTPFP=calculateTPFP(currentInstances);
	//				
	//
	//				//在达到最大值之前持续寻找起点
	//				if(currentPercent<=100-evalParams.getLower_limit()*100){
	//				}
	//				currentPercent+=1;
	//			} 
	//
	//		}
	//
	//	//计算给定预测结果集的最佳的阀值选择区间
	//	protected Vector<Double> computeThresholds( EvaluationBenchmark benchmark,EvaluationParams evalParams,
	//			DescriptiveStatistics stat_pred, Instances predictions)
	//			throws Exception {
	//		//获取预测值的对应百分位数值
	//		double tp_fp_ratio=benchmark.getEval_tp_fp_ratio()*evalParams.getLift_up_target();
	//		System.out.println("start from the trying_tp_fp based on training history data = "+tp_fp_ratio + " / while  lift up target="+evalParams.getLift_up_target());
	//		double currentPercent =(1-evalParams.getUpper_limit()) * 100;
	//		
	//		double currentThreshold=0.15; 
	//		double thresholdBottom=0;
	//		double startPercent=0;
	//		double startTPFP=0;
	//		double thresholdTop=9999;
	//		double endPercent=100;
	//		double endTPFP=9999;
	//		double currentTPFP=0;
	//		double maxTPFP=-99999;
	//		boolean startFound=false;
	//		
	//		Instances currentInstances=predictions;
	//		while (currentPercent<100){ // 从指定位置开始找收益率最大的位置，选择样本最大往最小的的方式找。
	//			System.out.println("-trying current percent : "+FormatUtility.formatDouble(currentPercent));
	//			currentThreshold= stat_pred.getPercentile(currentPercent);
	//			currentInstances=InstanceUtility.getInstancesSubset(currentInstances, "ATT1 >= "+FormatUtility.formatDouble(currentThreshold, 3, 4) );
	//			currentTPFP=calculateTPFP(currentInstances);
	//			
	//
	//			//在达到最大值之前持续寻找起点
	//			if(currentPercent<=100-evalParams.getLower_limit()*100){
	//				if (startFound==false){// 如果还没找到起点
	//					//如果已经达到了lift的要求，起点threshold就不用再找了，区间终点要一直找下去
	//					if (currentTPFP >=tp_fp_ratio){
	//						thresholdBottom=currentThreshold;
	//						startPercent=currentPercent;
	//						startTPFP=currentTPFP;
	//						endPercent=100;
	//						thresholdTop=999;
	//						endTPFP=999;
	//						System.out.println("...... start threshold found in advance!");
	//						startFound=true;
	//					} else if (currentTPFP>maxTPFP ) {
	//						//如果这是个目前的好结果，但还未达标，暂时认为它可以，暂设起止区间找下去
	//						maxTPFP=currentTPFP;
	//						thresholdBottom=currentThreshold;
	//						startPercent=currentPercent;
	//						startTPFP=currentTPFP;
	//						endPercent=100;
	//						thresholdTop=999;
	//						endTPFP=999;
	//						System.out.println("......temporarilly selected.");
	//					}
	//				}
	//			}
	//			//找threshold区间终点，这里不管起点是否已是确定选出的，都是一直找，要找到最小的那个TPFP才结束(阀值是0.5，再小就产生负效果了）
	//			if (  currentTPFP<startTPFP && currentTPFP<endTPFP && currentTPFP>0.5){
	//				if (currentPercent-startPercent>=3){ //window size最小为3，防止找的间距太小
	//					thresholdTop=currentThreshold;
	//					endPercent=currentPercent;
	//					endTPFP=currentTPFP;
	//				}
	//			}
	//			currentPercent+=1;
	//		} 
	//
	//		// 如果找不到合适的值，似乎应该不可能出现这个情况
	//		if (thresholdBottom==0){ 
	//			thresholdBottom=currentThreshold; // 最小样本处的值
	//			startPercent=currentPercent;
	//		}
	//		System.out.println("----finally, we choose value at percentile: from "+startPercent+ " to "+endPercent);
	//		System.out.println("----threshold is between: "+ Double.toString(thresholdBottom)+" - "+Double.toString(thresholdTop));
	//		Vector<Double> v = new Vector<Double>();
	//		v.add(new Double(thresholdBottom));
	//		v.add(new Double(thresholdTop));
	//		v.add(new Double(startPercent));
	//		v.add(new Double(endPercent));
	//		return v;
	//	}

//	//计算给定预测结果集的TPFP值
//	protected double calculateTPFP(Instances predictions){
//		double result=0;
//		double actual=0;
//		int positive=0;
//		int negative=0;
//		for (int i = 0; i < predictions.numInstances(); i++) {
//			actual=predictions.instance(i).classValue();
//			result+=actual;
//			if (actual>0){
//				positive++;
//			}else {
//				negative++;
//			}
//		}
//		//result=result/(positive+negative);
//		if (negative==0) 
//			result=100;
//		else
//			result=(double)positive/negative;
//		System.out.println("current TP/FP= "+ FormatUtility.formatDouble(result)+ " positive number= "+positive+" negative number= "+negative);
//		return result;
//	}


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

//	// 创建评估模型的arff结构（预测值-实际值）
//	protected Instances CreateEvalInstances() {
//		Attribute pred = new Attribute(ArffFormat.RESULT_PREDICTED_PROFIT);
//		Attribute shouyilv = new Attribute(ArffFormat.SHOUYILV);
//		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>(2);
//		fvWekaAttributes.add(pred);
//		fvWekaAttributes.add(shouyilv);
//		Instances structure = new Instances("predictions", fvWekaAttributes, 0);
//
//		structure.setClassIndex(structure.numAttributes() - 1);
//		return structure;
//	}



}
