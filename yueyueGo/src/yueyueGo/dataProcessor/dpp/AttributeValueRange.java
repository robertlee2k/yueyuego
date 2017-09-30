package yueyueGo.dataProcessor.dpp;

public class AttributeValueRange {

	private String attributeName;
	private double lowerLimit;
	private double upperLimit;
	
	public AttributeValueRange(String attributeName, double lowerLimit, double upperLimit) {
		this.attributeName = attributeName;
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public double getLowerLimit() {
		return lowerLimit;
	}

	public double getUpperLimit() {
		return upperLimit;
	}
	
	public StringBuffer getCSVString(){
		StringBuffer result=new StringBuffer();
		result.append(attributeName);
		result.append(',');
		result.append(lowerLimit);
		result.append(',');
		result.append(upperLimit);
		return result;
	}
	
	
}
