package yueyueGo.utility;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;

// changed to threadSafe for multi-threads to access 
public class ClassifySummaries {

	//ClassifySummary的表头部分
	public static final String SUMMARY_HEADER="时间段,均线策略,反向模型时间,反向阀值百分比,反向阀值,正向模型时间,正向目标百分比,正向AUC[TOP],正向AUC[ALL]";
	
	//DailySummary的表头部分
	public static final String DAILY_SUMMARY_HEADER="yearMonth,policy,date,目标百分位,累计百分位,当期选股阈值,当期机会数,当期选股数,当期选股比率\r\n";
	

	//统计信息
	protected SynchronizedDescriptiveStatistics summary_selected_TPR;
	protected SynchronizedDescriptiveStatistics summary_selected_positive;
	protected SynchronizedDescriptiveStatistics summary_lift;
	protected SynchronizedDescriptiveStatistics summary_selected_count;
	protected SynchronizedDescriptiveStatistics summary_judge_result;
	
	protected SynchronizedDescriptiveStatistics summary_selectedShouyilv;
	protected SynchronizedDescriptiveStatistics summary_totalShouyilv;
	
	protected String evaluationHeader=SUMMARY_HEADER;
	protected String evaluationSummary="";

	protected String dailyHeader=DAILY_SUMMARY_HEADER;
	protected String dailySummary="";
	
	protected String identityName;
	
	protected boolean isPredictionSummary=false; // 区分这是回测的summary还是预测的summary（输出格式不一样）
	
	protected String exceptionSummary=null;
	
	//向Header里添加字段
	public synchronized void appendHeader(String stringToAppend) {
		this.evaluationHeader = evaluationHeader+stringToAppend;
	}
	
	public String getEvaluationHeader() {
		return evaluationHeader+"\r\n";
	}
	
	public String getEvaluationSummary() {
		return evaluationSummary;
	}
	
	public String getDailyHeader() {
		return dailyHeader;
	}

	public String getDailySummary() {
		return dailySummary;
	}


	public synchronized void setEvaluationSummary(String evalSummary) {
		this.evaluationSummary = evalSummary;
	}
	
	public synchronized void appendEvaluationSummary(String stringToAppend) {
		
		this.evaluationSummary = evaluationSummary+stringToAppend;
	}

	public synchronized void appendDailySummary(String stringToAppend) {
		this.dailySummary = dailySummary+stringToAppend;
	}
	
	
	public synchronized void appendExceptionSummary(String stringToAppend) {
		
		this.exceptionSummary = exceptionSummary+"\r\n"+stringToAppend;
	}

	public boolean isForPrediction(){
		return isPredictionSummary;
	}
	
	public ClassifySummaries(String classifyIdentity, boolean forPrediction) {
		identityName=classifyIdentity;
		summary_selected_TPR= new SynchronizedDescriptiveStatistics();
		summary_selected_positive= new SynchronizedDescriptiveStatistics();
		summary_lift= new SynchronizedDescriptiveStatistics();
		summary_selected_count=new SynchronizedDescriptiveStatistics();
		summary_judge_result=new SynchronizedDescriptiveStatistics();
		
		summary_selectedShouyilv= new SynchronizedDescriptiveStatistics();
		summary_totalShouyilv= new SynchronizedDescriptiveStatistics();
		
		isPredictionSummary=forPrediction;

	}	

	@Deprecated
	public void computeClassifySummaries(String yearSplit, String policySplit,DescriptiveStatistics totalPositiveShouyilv,DescriptiveStatistics totalNegativeShouyilv,DescriptiveStatistics selectedPositiveShouyilv,DescriptiveStatistics selectedNegativeShouyilv) {
		
		double selected_TPR=0;
		double total_TPR=0;
		double tpr_lift=0;
		double selectedShouyilv=0.0;
		double totalShouyilv=0.0;
		double shouyilv_lift=0.0;
		long selectedPositive=selectedPositiveShouyilv.getN();
		long selectedNegative=selectedNegativeShouyilv.getN();
		long selectedCount=selectedPositive+selectedNegative;
		long positive=totalPositiveShouyilv.getN();
		long negative=totalNegativeShouyilv.getN();
		long totalCount=positive+negative;

		double selected_ratio=0.0;
		
		if (selectedCount>0) {
			selected_TPR=(double)selectedPositive/selectedCount;
			selectedShouyilv=(selectedPositiveShouyilv.getSum()+selectedNegativeShouyilv.getSum())/selectedCount;
		}
		if (totalCount>0) {
			total_TPR=(double)positive/totalCount;
			totalShouyilv=(totalPositiveShouyilv.getSum()+totalNegativeShouyilv.getSum())/totalCount;
			selected_ratio=(double)selectedCount/totalCount;
		}
		if (total_TPR>0) {
			tpr_lift=selected_TPR/total_TPR;
		}
		
		
		

		shouyilv_lift=selectedShouyilv-totalShouyilv;
		System.out.println("*** Prediction Summary for period " + yearSplit +" policy="+ policySplit);
		System.out.println("*** Overall selected ratio="+FormatUtility.formatPercent(selected_ratio));
		System.out.println("*** selected count= " + selectedCount + " selected positive: " +selectedPositive + "  selected negative: "+selectedNegative); 
		System.out.println("*** total    count= "	+ totalCount+ " actual positive: "+ positive + " actual negtive: "+ negative);
		System.out.println("*** selected TPR= " + FormatUtility.formatPercent(selected_TPR) + " total TPR= " +FormatUtility.formatPercent(total_TPR) + "  lift up= "+FormatUtility.formatDouble(tpr_lift));
		System.out.println("*** selected average Shouyilv= " + FormatUtility.formatPercent(selectedShouyilv) + " total average Shouyilv= " +FormatUtility.formatPercent(totalShouyilv)+ "  lift difference= "+FormatUtility.formatPercent(shouyilv_lift) );
		
		
		int resultJudgement = judgeResult(selectedShouyilv, totalShouyilv,
				selectedCount);
		
		
		this.summary_judge_result.addValue(resultJudgement);
		this.summary_selected_TPR.addValue(selected_TPR);
		this.summary_selected_positive.addValue(selectedPositive);
		this.summary_selected_count.addValue(selectedCount);
		
		if (total_TPR==0){//如果整体TPR为0则假定lift为1. 
			tpr_lift=1;
		}
		this.summary_lift.addValue(tpr_lift);
		this.summary_selectedShouyilv.addValue(selectedShouyilv);
		this.summary_totalShouyilv.addValue(totalShouyilv);
		
		
		//输出评估结果字符串
		//"整体正收益股数,整体股数,整体TPR,选股比率,所选正收益股数,所选总股数,所选股TPR,提升率,所选股平均收益率,整体平均收益率,收益率差,是否改善\r\n";
		StringBuffer evalSummary=new StringBuffer();
		
		evalSummary.append(yearSplit);
		evalSummary.append(",");
		evalSummary.append(policySplit);
		evalSummary.append(",");
		evalSummary.append(positive);
		evalSummary.append(",");
		evalSummary.append(totalCount);
		evalSummary.append(",");
		evalSummary.append(FormatUtility.formatPercent(total_TPR));
		evalSummary.append(",");
		evalSummary.append(FormatUtility.formatPercent(selected_ratio));
		evalSummary.append(",");
		evalSummary.append(selectedPositive);
		evalSummary.append(",");
		evalSummary.append(selectedCount);
		evalSummary.append(",");
		evalSummary.append(FormatUtility.formatPercent(selected_TPR));
		evalSummary.append(",");
		evalSummary.append(FormatUtility.formatDouble(tpr_lift));
		evalSummary.append(",");
		evalSummary.append(FormatUtility.formatPercent(selectedShouyilv));
		evalSummary.append(",");
		evalSummary.append(FormatUtility.formatPercent(totalShouyilv));
		evalSummary.append(",");
		evalSummary.append(FormatUtility.formatPercent(shouyilv_lift));
		evalSummary.append(",");
		evalSummary.append(resultJudgement);
		
		appendEvaluationSummary(evalSummary.toString());
		
		
	}

	/**
	 * @param selectedShouyilv
	 * @param totalShouyilv
	 * @param selectedCount
	 * 	用于评估单次分类的效果。 
//	对于回测来说，对于选股评估的规则，选收益率还是选liftup？：
	 * @return
	 */
	@Deprecated
	private int judgeResult(double selectedShouyilv, double totalShouyilv,	long selectedCount) {
		int resultJudgement=0;
		
		// 评估收益率是否有提升是按照选择平均收益率*可买入机会数 是否大于总体平均收益率*30（按30单元格单均线情况计算）
		long buyableCount=0;
		if (selectedCount>30){
			buyableCount=30;
		}else {
			buyableCount=selectedCount;
		}
		
		//评估此次成功与否
		if (selectedShouyilv*buyableCount>=totalShouyilv*30)
			resultJudgement=1;
		else 
			resultJudgement=0;

		System.out.println("*** evaluation result for this period :"+resultJudgement);
		return resultJudgement;
	}
	
	//每日预测时，因为没有收益率数据，不做评估，仅作存储
	public void savePredictSummaries(String policySplit,DescriptiveStatistics totalNegativeShouyilv,DescriptiveStatistics selectedNegativeShouyilv) {

		long selectedCount=selectedNegativeShouyilv.getN();;
		long totalCount=totalNegativeShouyilv.getN();
		double selected_ratio=0.0;
		if (totalCount>0) {
			selected_ratio=(double)selectedCount/totalCount;
		}
		System.out.println("****** prediction selected count= " + selectedCount+ " while  total count= "+ totalCount +". selected ratio="+FormatUtility.formatPercent(selected_ratio)); 
		this.summary_selected_count.addValue(selectedCount);

		
		String evalSummary="For Policy:( "+ policySplit+" ): selectedCount="+selectedCount+" out of totalCount="+totalCount;
		appendEvaluationSummary(evalSummary);

	}
	
	public void outputPredictSummary() throws Exception{
		
		String selected_count_sum=FormatUtility.formatDouble(summary_selected_count.getSum(),8,0);
		System.out.println("..............................");
		System.out.println("===============================output summary===================================== for : "+identityName);
		System.out.println(this.getEvaluationSummary());
		System.out.println("Total Selected Count: "+selected_count_sum);
		System.out.println(identityName+"===============================end of summary===================================== ");
		System.out.println("..............................");
		
	}
	
	public void outputClassifySummary() throws Exception{
		String selected_TPR_mean=FormatUtility.formatPercent(summary_selected_TPR.getMean());
		String selected_TPR_SD=FormatUtility.formatPercent(summary_selected_TPR.getStandardDeviation());
		String selected_TPR_SKW=FormatUtility.formatDouble(summary_selected_TPR.getSkewness());
		String selected_TPR_Kur=FormatUtility.formatDouble(summary_selected_TPR.getKurtosis());
		String lift_mean=FormatUtility.formatDouble(summary_lift.getMean());
		String selected_positive_sum=FormatUtility.formatDouble(summary_selected_positive.getSum(),8,0);
		String selected_count_sum=FormatUtility.formatDouble(summary_selected_count.getSum(),8,0);
		String selectedShouyilvMean = FormatUtility.formatPercent(summary_selectedShouyilv.getMean());
		String selectedShouyilvSD = FormatUtility.formatPercent(summary_selectedShouyilv.getStandardDeviation());
		String selectedShouyilvSKW = FormatUtility.formatDouble(summary_selectedShouyilv.getSkewness());
		String selectedShouyilvKUR = FormatUtility.formatDouble(summary_selectedShouyilv.getKurtosis());
		String totalShouyilvMean = FormatUtility.formatPercent(summary_totalShouyilv.getMean());
		String totalShouyilvSD = FormatUtility.formatPercent(summary_totalShouyilv.getStandardDeviation());
		String totalShouyilvSKW = FormatUtility.formatDouble(summary_totalShouyilv.getSkewness());
		String totalShouyilvKUR = FormatUtility.formatDouble(summary_totalShouyilv.getKurtosis());
		
		
		System.out.println("......................");
		System.out.println("===============================output summary===================================== for : "+identityName);
		System.out.println("Monthly selected_TPR mean: "+selected_TPR_mean+" standard deviation="+selected_TPR_SD+" Skewness="+selected_TPR_SKW+" Kurtosis="+selected_TPR_Kur);
		System.out.println("Monthly selected_LIFT mean : "+lift_mean);
		System.out.println("Monthly selected_positive summary: "+selected_positive_sum);
		System.out.println("Monthly selected_count summary: "+selected_count_sum);
		System.out.println("Monthly selected_shouyilv average: "+selectedShouyilvMean+" standard deviation="+selectedShouyilvSD+" Skewness="+selectedShouyilvSKW+" Kurtosis="+selectedShouyilvKUR);
		System.out.println("Monthly total_shouyilv average: "+totalShouyilvMean+" standard deviation="+totalShouyilvSD+" Skewness="+totalShouyilvSKW+" Kurtosis="+totalShouyilvKUR);
		if(summary_selected_count.getSum()>0){
			System.out.println("mixed selected positive rate: "+FormatUtility.formatPercent(summary_selected_positive.getSum()/summary_selected_count.getSum()));
		}
		System.out.println("Monthly summary_judge_result summary: good number= "+FormatUtility.formatDouble(summary_judge_result.getSum(),8,0) + " bad number=" +FormatUtility.formatDouble((summary_judge_result.getN()-summary_judge_result.getSum()),8,0));
		System.out.println("===============================end of summary=====================================for : "+identityName);
		System.out.println("......................");
		if (exceptionSummary!=null){
			System.err.println("Exception Summary"+exceptionSummary);
		}

	}
	
	public void cleanUp(){
		summary_selected_TPR=null;
		summary_selected_positive=null;
		summary_lift=null;
		summary_selected_count=null;
		summary_judge_result=null;
		summary_selectedShouyilv=null;
		summary_totalShouyilv=null;
		evaluationSummary=null;		
	}
}
