/**
 * 
 */
package yueyueGo.databeans;

import yueyueGo.ArffFormat;
import yueyueGo.dataProcessor.WekaInstanceProcessor;

/**
 * @author robert
 * 描述数据的种类及时间段
 *
 */
public class WekaDataTag extends GeneralDataTag {
	
	private int dataType=-999; //unset
	private String fromPeriod=null;
	private String toPeriod=null;
	private String splitClause=null;

	
	/*
	 * 定义数据标签，参数按顺序分别为：
	 * 1. 数据的类型
	 * 2. 数据从何时间点开始（包含该点）
	 * 2. 数据到何时间点结束（除了检验数据之外，都不包含该点）
	 */
	public WekaDataTag(int type,String a_fromPeriod,String a_toPeriod){
		this.fromPeriod=a_fromPeriod;
		this.toPeriod=a_toPeriod;
		this.dataType=type;
		String attPos = WekaInstanceProcessor.WEKA_ATT_PREFIX + ArffFormat.YEAR_MONTH_INDEX;
		
		switch (dataType) {
		case TRAINING_DATA:
			this.splitClause="(" + attPos + " < "+ toPeriod + ") ";
			break;
		case EVALUATION_DATA:
			this.splitClause="(" + attPos + " >= "+ fromPeriod + ") and (" + attPos + " < "	+ toPeriod + ") ";
			break;
		case TESTING_DATA:
			this.splitClause="(" + attPos + " = "+ toPeriod + ") ";
			break;	
			
		default:
			throw new RuntimeException("undefined dataTag type");
		}
	}
	
	/* (non-Javadoc)
	 * @see yueyueGo.databeans.GeneralDataTag#getDataType()
	 */
	@Override
	public int getDataType() {
		return dataType;
	}
	/* (non-Javadoc)
	 * @see yueyueGo.databeans.GeneralDataTag#setDataType(int)
	 */
	@Override
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	/* (non-Javadoc)
	 * @see yueyueGo.databeans.GeneralDataTag#getFromPeriod()
	 */
	@Override
	public String getFromPeriod() {
		return fromPeriod;
	}
	/* (non-Javadoc)
	 * @see yueyueGo.databeans.GeneralDataTag#setFromPeriod(java.lang.String)
	 */
	@Override
	public void setFromPeriod(String fromPeriod) {
		this.fromPeriod = fromPeriod;
	}
	/* (non-Javadoc)
	 * @see yueyueGo.databeans.GeneralDataTag#getToPeriod()
	 */
	@Override
	public String getToPeriod() {
		return toPeriod;
	}
	/* (non-Javadoc)
	 * @see yueyueGo.databeans.GeneralDataTag#setToPeriod(java.lang.String)
	 */
	@Override
	public void setToPeriod(String toPeriod) {
		this.toPeriod = toPeriod;
	}
	/* (non-Javadoc)
	 * @see yueyueGo.databeans.GeneralDataTag#getSplitClause()
	 */
	@Override
	public String getSplitClause() {
		return splitClause;
	}
	/* (non-Javadoc)
	 * @see yueyueGo.databeans.GeneralDataTag#setSplitClause(java.lang.String)
	 */
	@Override
	public void setSplitClause(String splitClause) {
		this.splitClause = splitClause;
	}


	
	
	
}
