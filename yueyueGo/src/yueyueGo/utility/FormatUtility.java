package yueyueGo.utility;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import weka.core.AttributeStats;
import weka.core.Utils;
import yueyueGo.dataFormat.ArffFormat;

public class FormatUtility {
	// 获取指定偏移量的日期String
	public static String getDateStringFor(int offset) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, offset);
		String dateString = new SimpleDateFormat("yyyy-MM-dd ").format(cal
				.getTime());
		return dateString;
	}

	// 获取当前日期的yyyyMM格式
	public static String getCurrentYearMonth() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 0);
		String dateString = new SimpleDateFormat("yyyyMM").format(cal.getTime());
		return dateString;
	}	
	
	/**
	 * 将double类型数据转换为百分比格式，并保留小数点前IntegerDigits位和小数点后FractionDigits位
	 * 
	 * @param d
	 * @param IntegerDigits
	 * @param FractionDigits
	 * @return
	 */
	public static String formatPercent(double d, int IntegerDigits,
			int FractionDigits) {
		NumberFormat nf = NumberFormat.getPercentInstance();
		nf.setMaximumIntegerDigits(IntegerDigits);// 小数点前保留几位
		nf.setMinimumFractionDigits(FractionDigits);// 小数点后保留几位
		String str = nf.format(d);
		return str;
	}

	// 缺省保留百分号小数点前3位，后2位
	public static String formatPercent(double d) {
		return formatPercent(d, 3, 2);
	}

	// 缺省保留小数点后二位
	public static String formatDouble(double d) {
		NumberFormat df = NumberFormat.getNumberInstance();
		df.setMaximumFractionDigits(2);
		return df.format(d);
	}

	public static String formatDouble(double d, int integerDigits,
			int fractionDigits) {
		NumberFormat df = NumberFormat.getNumberInstance();
		df.setMaximumIntegerDigits(integerDigits);// 小数点前保留几位
		df.setMaximumFractionDigits(fractionDigits);// 小数点后保留几位
		return df.format(d);
	}

	public static String convertDate(String tradeDate) throws ParseException {
		SimpleDateFormat input = new SimpleDateFormat(ArffFormat.INPUT_DATE_FORMAT);
		SimpleDateFormat output = new SimpleDateFormat(ArffFormat.ARFF_DATE_FORMAT);
		Date tDate = input.parse(tradeDate);
		String parsed=output.format(tDate);
		return parsed;
	}
	
	
	//比较两个double数据，如果二者差值小于预定义范围，则返回0；
	public static final double LOWER_LIMIT=-0.01;
	public static final double UPPER_LIMIT=0.01;
	public static double compareDouble(double first,double second) {
		return compareDouble(first, second,LOWER_LIMIT,UPPER_LIMIT);
	}
	
	//以任意精度比较double
	public static double compareDouble(double first,double second,double lower_limit,double upper_limit) {
		double compare=0;
		//如果first second都是NaN，认为二者相等
		if((Double.isNaN(first) && Double.isNaN(second))==false){
			if (second>=1 || second<=-1){ //绝对值大于1时按照比值比较
				compare=first/second-1;
			}else{//绝对值小于1时按照差值比较
				compare=first-second;
			}
			if (compare>=lower_limit && compare<=upper_limit){
				compare=0;
			}
		}
		return compare;		
	}

	public static String[] concatStrings(String[] first,String[] second){
		String[] resultString=new String[first.length+second.length];
		System.arraycopy(first, 0, resultString, 0, first.length);
		System.arraycopy(second, 0, resultString, first.length, second.length);
		return resultString;
	}
	
	public static String[] concatStrings(String[] first,String[] second,String[] third){
		String[] firstTwo=concatStrings(first,second);
		String[] resultString=concatStrings(firstTwo,third);
		return resultString;
	}
	
	/**
	   * Returns a human readable representation of this AttributeStats instance.
	   *
	   * @return a String represtinging these AttributeStats.
	   */
	  public static String printAttributeStatus(AttributeStats status) {

	    StringBuffer sb = new StringBuffer();
	    sb.append(Utils.padLeft("Type", 4)).append(Utils.padLeft("Nom", 7));
	    sb.append(Utils.padLeft("Int", 7)).append(Utils.padLeft("Real", 7));
	    sb.append(Utils.padLeft("Missing", 17));
	    sb.append(Utils.padLeft("Unique", 17));
	    sb.append(Utils.padLeft("Distinct", 9));
	    sb.append(' ');
	    if (status.nominalCounts != null) {
	      for (int i = 0; i < status.nominalCounts.length; i++) {
	        sb.append(Utils.padLeft("C[" + i + "]", 10));
	      }
	    }else if( status.numericStats!=null){
	    	sb.append(Utils.padLeft("Min",9));
	    	sb.append(Utils.padLeft("Max",9));
	    	sb.append(Utils.padLeft("Mean",9));
	    	sb.append(Utils.padLeft("StdDev",9));
	    }
	    sb.append('\n');

	    String percent;
	    percent = formatDouble((100.0 * status.intCount / status.totalCount),3,1);
	    if (status.nominalCounts != null) {
	      sb.append(Utils.padLeft("Nom", 4)).append(' ');
	      sb.append(Utils.padLeft("" + percent, 5)).append("% ");
	      sb.append(Utils.padLeft("" + 0, 5)).append("% ");
	    } else {
	      sb.append(Utils.padLeft("Num", 4)).append(' ');
	      sb.append(Utils.padLeft("" + 0, 5)).append("% ");
	      sb.append(Utils.padLeft("" + percent, 5)).append("% ");
	    }
	    percent = formatDouble((100.0 * status.realCount / status.totalCount),3,1);
	    sb.append(Utils.padLeft("" + percent, 5)).append("% ");
	    sb.append(Utils.padLeft("" + status.missingCount, 8)).append(" /");
	    percent = formatDouble((100.0 * status.missingCount / status.totalCount),3,1);
	    sb.append(Utils.padLeft("" + percent, 5)).append("% ");
	    sb.append(Utils.padLeft("" + status.uniqueCount, 8)).append(" /");
	    percent = formatDouble((100.0 * status.uniqueCount / status.totalCount),3,1);
	    sb.append(Utils.padLeft("" + percent, 5)).append("% ");
	    sb.append(Utils.padLeft("" + status.distinctCount, 8)).append(' ');
	    if (status.nominalCounts != null) {
	      for (int i = 0; i < status.nominalCounts.length; i++) {
	        sb.append(Utils.padLeft("" + status.nominalCounts[i], 10));
	      }
	    }else if( status.numericStats!=null){
	    	weka.experiment.Stats numericStatus=status.numericStats;
	    	sb.append("   ");
	    	sb.append(Utils.padLeft(""+numericStatus.min, 6));
	    	sb.append("   ");
	    	sb.append(Utils.padLeft(""+numericStatus.max, 6));
	    	sb.append("   ");
	    	sb.append(Utils.padLeft(""+numericStatus.mean, 6));
	    	sb.append("   ");
	    	sb.append(Utils.padLeft(""+numericStatus.stdDev, 6));

	    }
	    sb.append('\n');
	    return sb.toString();
	  }
	
}
