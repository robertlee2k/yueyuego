package yueyueGo.databeans;

public abstract class GeneralDataTag {

	public static final int TRAINING_DATA = 1;
	public static final int EVALUATION_DATA = 2;
	public static final int TESTING_DATA = 3;
	public static final String LONG_LONG_AGO="190001";

	public abstract int getDataType();

	public abstract void setDataType(int dataType);

	public abstract String getFromPeriod();

	public abstract void setFromPeriod(String fromPeriod);

	public abstract String getToPeriod();

	public abstract void setToPeriod(String toPeriod);

	public abstract String getSplitClause();

	public abstract void setSplitClause(String splitClause);
	

	public String compareToTag(GeneralDataTag target){
		
		String msg="";
		if (this.getDataType()!=target.getDataType()){
			msg+=" dataType not equal. ";
		}
		if (this.getFromPeriod().equals(target.getFromPeriod())==false){
			msg+=" fromPeriod not equal. ";
		}
		if (this.getToPeriod().equals(target.getToPeriod())==false){
			msg+=" toPeriod not equal. ";
		}
		if ("".equals(msg))
			return null;
		else
			return msg;
	}

}