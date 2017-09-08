package yueyueGo.utility.analysis;
/*
 * 定义市场的情况 
 * 
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

	public String getExplain(){
		return explain;
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
