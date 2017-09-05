package yueyueGo.utility;
/*
 * 定义市场的情况 
 * 
 大牛市 200601-200711
 大熊市  200712-200810
小牛市  200811-200907
慢熊市   200908-201306
慢反弹  201307-201409
大牛市  201410-201505
大熊市   201506-201508
小反弹  201509-201512
小熊市    201601-201609
慢反弹  201610-201708

 */

public class MarketDefinition {

	
	private int startYearMonth;
	private int endYearMonth;
	private String explain;
	private int marketTrend;
	
	
	
	public MarketDefinition(String explain,int startYearMonth, int endYearMonth) {
		this.startYearMonth = startYearMonth;
		this.endYearMonth = endYearMonth;
		this.explain = explain;
//		this.marketTrend = marketTrend;
	}

	public String toString(){
		return "市场："+explain+" @"+startYearMonth+"-"+endYearMonth;
	}
	public int getStartYearMonth() {
		return startYearMonth;
	}
	public void setStartYearMonth(int startYearMonth) {
		this.startYearMonth = startYearMonth;
	}
	public int getEndYearMonth() {
		return endYearMonth;
	}
	public void setEndYearMonth(int endYearMonth) {
		this.endYearMonth = endYearMonth;
	}
	public String getExplain() {
		return explain;
	}
	public void setExplain(String explain) {
		this.explain = explain;
	}
	public int getMarketTrend() {
		return marketTrend;
	}
	public void setMarketTrend(int marketTrend) {
		this.marketTrend = marketTrend;
	}
	
	
}
