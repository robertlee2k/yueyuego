package yueyueGo;

import java.util.ArrayList;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import yueyueGo.utility.EvaluationBenchmark;
import yueyueGo.utility.EvaluationParams;
import yueyueGo.utility.FormatUtility;
import yueyueGo.utility.InstanceUtility;

public abstract class NominalClassifier extends BaseClassifier{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5570283670170193026L;

	private Instances m_cachedOldClassInstances=null;
	

	@Override
	//具体的模型评估方法

	protected Vector<Double> doModelEvaluation(EvaluationBenchmark benchmark ,Instances train,Classifier model,EvaluationParams evalParams)
			throws Exception {

		// generate curve
		Evaluation eval=benchmark.getEvalulation();
		ThresholdCurve tc = new ThresholdCurve();
		int classIndex = 1;
		Instances result = tc.getCurve(eval.predictions(), classIndex);

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
//		double tpr=0;
//		double fpr=0;
		double final_tp=0.0;
		double final_fp=0.0;
//		double final_deviation=-999999999.9;
		Attribute att_tp = result.attribute("True Positives");
		Attribute att_fp = result.attribute("False Positives");
//		Attribute att_tpr = result.attribute("True Positive Rate"); 
//		Attribute att_fpr= result.attribute("False Positive Rate");
		Attribute att_lift = result.attribute("Lift");
		Attribute att_threshold = result.attribute("Threshold");
		Attribute att_samplesize = result.attribute("Sample Size");


		for (int i = 0; i < result.numInstances(); i++) {
			Instance curr = result.instance(i);
			sampleSize = curr.value(att_samplesize); // to get sample range
			if (sampleSize >= sample_limit && sampleSize <=sample_upper) {
				tp = curr.value(att_tp);
				fp = curr.value(att_fp);
//				tpr = curr.value(att_tpr);
//				fpr = curr.value(att_fpr);
				if (tp>fp*tp_fp_ratio ){
//					TODO 试了求TPR-FPR的最大值(tpr-fpr)，效果差不多，先恢复原始的
//					if (tp-fp > final_deviation ) {
					thresholdBottom = curr.value(att_threshold);
					finalSampleSize = sampleSize;
					lift_max=curr.value(att_lift);
					final_tp=tp;
					final_fp=fp;
//						final_deviation=tp-fp;
//					}
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

	//对于二分类变量，返回分类1的预测可能性
	@Override
	protected  double classify(Classifier model,Instance curr) throws Exception {
		double[] problity =  model.distributionForInstance(curr);
		return problity[1];
	}
	
	//将原始数据变换为nominal Classifier需要的形式（更换class 变量等等）
	public Instances processDataForNominalClassifier(Instances inData, boolean cacheOldClassValue) throws Exception{

		int oldClassIndex=inData.classIndex();
		if (oldClassIndex!=(inData.numAttributes()-1)){
			throw new Exception("fatal error! class index should be at the last column");
		}
		ArrayList<String> values=new ArrayList<String>();
		values.add(ArffFormat.VALUE_NO);
		values.add(ArffFormat.VALUE_YES);
		Attribute newClassAtt=new Attribute(ArffFormat.IS_POSITIVE,values);
		//在classValue之前插入positive,然后记录下它的新位置index
		inData.insertAttributeAt(newClassAtt,inData.numAttributes()-1);
		int newClassIndex=inData.numAttributes()-2;
		double shouyilv=0;
		
		if (cacheOldClassValue==true){
			m_cachedOldClassInstances=CreateCachedOldClassInstances();
		}else{
			m_cachedOldClassInstances=null;
		}
		
		for (int i=0;i<inData.numInstances();i++){
			shouyilv=inData.instance(i).classValue();
			if (shouyilv>m_positiveLine){
				inData.instance(i).setValue(newClassIndex, ArffFormat.VALUE_YES);
			}else {
				inData.instance(i).setValue(newClassIndex, ArffFormat.VALUE_NO);
			}
			
			//暂存收益率
			if (cacheOldClassValue==true){
				double id=inData.instance(i).value(0);
				Instance cacheRow=new DenseInstance(m_cachedOldClassInstances.numAttributes());
				cacheRow.setDataset(m_cachedOldClassInstances);
				cacheRow.setValue(0, id);
				cacheRow.setValue(1, shouyilv);
				m_cachedOldClassInstances.add(cacheRow);
			}
		}
		//删除shouyilv
		inData=InstanceUtility.removeAttribs(inData, ""+inData.numAttributes());
		//设置新属性的位置
		inData.setClassIndex(inData.numAttributes()-1);
		System.out.println("class value replaced for nominal classifier. where m_positiveLine= "+m_positiveLine);
		return inData;
	}
	
	
	// 创建暂存oldClassValue（目前情况下为暂存收益率）的arff结构（id-收益率）
	protected Instances CreateCachedOldClassInstances() {
		Attribute pred = new Attribute(ArffFormat.ID);
		Attribute shouyilv = new Attribute(ArffFormat.SHOUYILV);
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>(2);
		fvWekaAttributes.add(pred);
		fvWekaAttributes.add(shouyilv);
		Instances structure = new Instances("cachedOldClass", fvWekaAttributes, 0);
		structure.setClassIndex(structure.numAttributes() - 1);
		return structure;
	}
	
	@Override
	protected double getShouyilv(int index,double id, double newClassValue) throws Exception{
		if (m_cachedOldClassInstances==null) {
			return Double.NaN;
		}
		if ( index>=m_cachedOldClassInstances.numInstances()){
			throw new Exception("Old Class Value has not been cached for index: "+ index );
		}
		double cachedID=m_cachedOldClassInstances.instance(index).value(0);
		if(cachedID!=id){
			throw new Exception("Data inconsistent error! Cached old class value id = "+cachedID+" while incoming id ="+id+" for index: "+ index );
		}
		double shouyilv=m_cachedOldClassInstances.instance(index).classValue();
		if (newClassValue== 0 && shouyilv>m_positiveLine || newClassValue==1 && shouyilv<=m_positiveLine){ 
			throw new Exception("Data inconsistent error! Cached old class value id = "+shouyilv+" while incoming newClassValue ="+newClassValue+" for index: "+ index +" @m_positiveLine="+m_positiveLine);
		}
		return shouyilv;
	}
	
	@Override
	 //这里覆盖父类方法，为二分类器classify后统计正负收益个数时提供分界值
	protected double getPositiveLine(){
	 return m_positiveLine;	
	}
	

	@Override
	public String getIdentifyName(){
		if (m_positiveLine==0){ //如果是正常的正负分类时就不用特别标记
			return classifierName;
		}else { //如果用自定义的标尺线区分Class的正负，则特别标记
			return (classifierName+"("+FormatUtility.formatDouble(m_positiveLine)+")");
		}
	}
	
	//用于清除分类器的内部缓存（如nominal 分类器的cache）
	// 注意这里仅限于清除内部的cache，外部设置的比如ClassifySummary不在此列
	@Override
	public void cleanUp(){
		m_cachedOldClassInstances=null;
	}
}
