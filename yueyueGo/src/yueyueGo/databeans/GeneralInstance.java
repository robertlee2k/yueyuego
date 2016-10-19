package yueyueGo.databeans;

public interface GeneralInstance {

	public abstract GeneralAttribute attribute(int index);

	public abstract int classIndex();

	public abstract double classValue();

	public abstract GeneralInstances dataset();

	public abstract int index(int position);

	public abstract void insertAttributeAt(int position);

	public abstract int numAttributes();

	public abstract void setClassValue(double value);

	public abstract void setClassValue(String value);

	public abstract void setDataset(GeneralInstances instances);

	public abstract void setValue(GeneralAttribute att, double value);

	public abstract void setValue(GeneralAttribute att, String value);

	public abstract void setValue(int attIndex, double value);

	public abstract void setValue(int attIndex, String value);

	public abstract String stringValue(GeneralAttribute att);

	public abstract String stringValue(int attIndex);

	public abstract String toString();

	public abstract double value(GeneralAttribute att);

	public abstract double value(int attIndex);

}