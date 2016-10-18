package yueyueGo.databeans;

public interface BaseInstance {

	public abstract BaseAttribute attribute(int index);

	public abstract int classIndex();

	public abstract double classValue();

	public abstract BaseInstances dataset();

	public abstract int index(int position);

	public abstract void insertAttributeAt(int position);

	public abstract int numAttributes();

	public abstract void setClassValue(double value);

	public abstract void setClassValue(String value);

	public abstract void setDataset(BaseInstances instances);

	public abstract void setValue(BaseAttribute att, double value);

	public abstract void setValue(BaseAttribute att, String value);

	public abstract void setValue(int attIndex, double value);

	public abstract void setValue(int attIndex, String value);

	public abstract String stringValue(BaseAttribute att);

	public abstract String stringValue(int attIndex);

	public abstract String toString();

	public abstract double value(BaseAttribute att);

	public abstract double value(int attIndex);

}