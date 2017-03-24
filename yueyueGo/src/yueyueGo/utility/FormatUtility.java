package yueyueGo.utility;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
		df.setMinimumFractionDigits(fractionDigits);// 小数点后保留几位
		return df.format(d);
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
}
