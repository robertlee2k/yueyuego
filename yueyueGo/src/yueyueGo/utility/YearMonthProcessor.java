package yueyueGo.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import yueyueGo.AbstractModel;
import yueyueGo.dataFormat.ArffFormat;
import yueyueGo.databeans.GeneralDataTag;
import yueyueGo.databeans.WekaDataTag;
import yueyueGo.utility.modelEvaluation.EvaluationStore;
import yueyueGo.utility.modelEvaluation.ModelStore;

/*
 * 用于处理回测中时间段yearmonth相关的工具类
 */
public class YearMonthProcessor {

	/*
	 * 历史数据切掉评估数据后再调用该函数看选多少作为模型构建数据
	 * （因为为了回测速度， 不是每月都有模型,要根据模型的modelEvalFileShareMode来定）
	 */
	public static String caculateModelYearSplit(String evalYearSplit,int modelFileShareMode){
		String modelYearSplit=null;
		switch (modelFileShareMode){//classifier.m_modelEvalFileShareMode) {
		case ModelStore.MONTHLY_MODEL:
			modelYearSplit=evalYearSplit;
			break;
		case ModelStore.YEAR_SHARED_MODEL:	//评估文件按yearsplit和policySplit切割
			//模型文件按年处理
			if (evalYearSplit.length()==6){
				modelYearSplit=evalYearSplit.substring(0,4)+"01";
			}else{
				modelYearSplit=evalYearSplit;
			}
			break;
		case ModelStore.QUARTER_SHARED_MODEL:
			//评估文件按yearsplit和policySplit切割
			String quarterString="";
			//模型文件按季度建设模型，提高准确度
			if (evalYearSplit.length()==6){
				//有月份时按季度获取模型
				modelYearSplit=evalYearSplit.substring(0,4);
				int inputQuarter=(Integer.parseInt(evalYearSplit.substring(4,6))-1)/3; //将月份按季度转化为0、1、2、3四个数字
				switch (inputQuarter){
				case 3://第四季度
					quarterString="10";
					break;
				default: //第一第二第三季度补0
					quarterString="0"+(inputQuarter*3+1);
					break;
				}
			}else{
				modelYearSplit=evalYearSplit;
			}
			modelYearSplit=modelYearSplit+quarterString;
			break;
		case ModelStore.HALF_YEAR_SHARED_MODEL:	
			String halfYearString=""; //缺省上半年是直接用年份的模型，比如2010
			//模型文件按半年建设模型，提高准确度
			if ( evalYearSplit.length()==6){
				modelYearSplit= evalYearSplit.substring(0,4);
				int inputMonth=Integer.parseInt( evalYearSplit.substring(4,6));
				if (inputMonth>=7){
					halfYearString="07";
				} else{
					halfYearString="01";
				}
			}else{
				modelYearSplit= evalYearSplit;
			}
			modelYearSplit=modelYearSplit+halfYearString;
			break;
		default:
			throw new RuntimeException("undefined m_modelEvalFileShareMode ");
		}
		
		return modelYearSplit;
	}

	/*
	 * 取modelYearEndSplit的前noOfYears的yearmonth值
	 * 如201203的前5年为200703
	 */
	public static String modelDataStartYearSplit(String modelYearEndSplit, int noOfYears){
		int yearMonth=Integer.valueOf(modelYearEndSplit).intValue();
		yearMonth-=noOfYears*100;
		return String.valueOf(yearMonth);
	}

//	@Deprecated
//	/**
//	 * @param modelYearSplit
//	 * @return
//	 * @throws NumberFormatException
//	 */
//	public static String legacyModelName(String modelYearSplit) throws NumberFormatException {
//		//根据历史习惯， 如果模型是第一个月时将其文件名变换为年的形式（如应该把200801变为2008）
//		if (modelYearSplit.length()==6){
//			int inputMonth=Integer.parseInt(modelYearSplit.substring(4,6)); 
//			if (inputMonth==1){
//				modelYearSplit=modelYearSplit.substring(0,4);
//			}
//		}
//		return modelYearSplit;
//	}

	//	当前周期前推N个月的分隔线，比如 如果N=9是201003 则返回200909
	public static String backNMonthsForYearSplit(int nMonths,String yearSplit){
	
		int lastPeriod=0;
		if (yearSplit.length()==4){ //最后一位-1 （2010-1=2009）再拼接一个12-nMonth+1
			lastPeriod=Integer.valueOf(yearSplit).intValue();
			lastPeriod=lastPeriod-1;
			if (lastPeriod<EvaluationStore.YEAR_SPLIT_LIMIT) {
				lastPeriod=EvaluationStore.YEAR_SPLIT_LIMIT;
			}
			lastPeriod=lastPeriod*100+12-nMonths+1;
		}else {//最后两位数（n）大于nMonths的话减nMonths，小于等于的话向年借位12
			int inputYear=Integer.parseInt(yearSplit.substring(0,4)); //输入的年份
			int inputMonth=Integer.parseInt(yearSplit.substring(4,6)); //输入的月份
			if (inputMonth>nMonths){
				inputMonth=inputMonth-nMonths;
			}else{
				inputMonth=12+inputMonth-nMonths;
				inputYear=inputYear-1;
			}
			lastPeriod=inputYear*100+inputMonth;
			if (lastPeriod<EvaluationStore.YEAR_SPLIT_LIMIT*100+1){ 
				lastPeriod=EvaluationStore.YEAR_SPLIT_LIMIT*100+1;
			}
		}
		return String.valueOf(lastPeriod);
	}

	/*
	 * 获取用于评估阀值的yearSplit（目前主要有三种： 最近6个月、9个月、12个月）
	 * 全量历史数据会分解为模型构建数据及其评估数据
	 * 比如，若是一年区间， 则取最近的1年间数据作为评估数据用。之前的数据再去getModelYearSplit看选多少作为模型构建数据（因为不是每月都有模型）
	 * 如果是2010.mdl，则取2009年01月之前的数据build，2009当年数据做评估用
	 */
	public static String caculateEvalYearSplit(String targetYearSplit,int evalDataSplitMode){
		//找到回测创建评估预测时应该使用modelStore对象（主要为获取model文件和eval文件名称）-- 评估时创建的mdl并不是当前年份的，而是前推一年的
		String evalYearSplit=null;
		switch (evalDataSplitMode) {
		case EvaluationStore.NO_SEPERATE_DATA_FOR_EVAL: //使用全量数据构建模型（不常用）
			evalYearSplit=targetYearSplit; 
			break;
		case EvaluationStore.USE_YEAR_DATA_FOR_EVAL:
		case EvaluationStore.USE_HALF_YEAR_DATA_FOR_EVAL:
		case EvaluationStore.USE_NINE_MONTHS_DATA_FOR_EVAL:
			evalYearSplit=backNMonthsForYearSplit(evalDataSplitMode,targetYearSplit);			
			break;
		}
		System.out.println("目标日期="+targetYearSplit+" 评估数据切分日期="+evalYearSplit+"（评估数据切分模式="+evalDataSplitMode+"）");
		return evalYearSplit;
	}

	/**
	 * 	从全量数据中获取分割training和eval以及test的clause， test数据比较简单，就是当月的。
	 * train和eval的逻辑由ModelStore定义:
	 */
	public static GeneralDataTag[] getDataSplitTags(AbstractModel clModel,String targetYearSplit) {
		String evalYearSplit=caculateEvalYearSplit(targetYearSplit, clModel.m_evalDataSplitMode);
		String modelYearSplit=caculateModelYearSplit(evalYearSplit, clModel.m_modelFileShareMode);
	
		//用N年的训练数据
		String modelDataStartYearSplit=modelDataStartYearSplit(modelYearSplit, clModel.m_useRecentNYearForTraining);
	
		GeneralDataTag[] dataTags=new WekaDataTag[3];
		
		dataTags[0]=new WekaDataTag(GeneralDataTag.TRAINING_DATA,modelDataStartYearSplit,modelYearSplit);
		//此处减掉最近N个月的数据，因为实际每日预测时，最近的数据往往并不准确（收益率还未更新）
		String evalEndYearSplit=backNMonthsForYearSplit(clModel.m_SkipRecentNMonthForEval, targetYearSplit);
		dataTags[1]=new WekaDataTag(GeneralDataTag.EVALUATION_DATA,evalYearSplit,evalEndYearSplit);
		dataTags[2]=new WekaDataTag(GeneralDataTag.TESTING_DATA,targetYearSplit,targetYearSplit);
	
		System.out.println("模型构建数据 from "+modelDataStartYearSplit+" before "+modelYearSplit+"评估数据 from"+evalYearSplit+" before "+evalEndYearSplit+" 测试数据时间="+targetYearSplit);
	
		return dataTags;
	}

	/**
	 从日期输入日期变量中获取 yearmonth 并以数值返回(YYYYMM)类型
	 */
	public static double parseYearMonth(String tradeDate) throws ParseException {
		SimpleDateFormat ft = new SimpleDateFormat(ArffFormat.INPUT_DATE_FORMAT);
		Date tDate = ft.parse(tradeDate);
	
		Calendar cal = Calendar.getInstance();
		cal.setTime(tDate);
		int y = cal.get(Calendar.YEAR);
		int m = cal.get(Calendar.MONTH)+1;
		double ym = y * 100 + m;
		return ym;
	}

}
