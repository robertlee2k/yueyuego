package yueyueGo.databeans;

import java.util.ArrayList;



public interface GeneralInstances {

	public abstract boolean add(GeneralInstance instance);

	public abstract GeneralAttribute attribute(int index);

	public abstract GeneralAttribute attribute(String name);

	public abstract double[] attributeToDoubleArray(int arg0);

	public abstract GeneralAttribute classAttribute();

	public abstract int classIndex();
	
	public abstract ArrayList<GeneralAttribute> getAttributeList() ;
	
	public abstract boolean equalHeaders(GeneralInstances dataset);

	public abstract String equalHeadersMsg(GeneralInstances arg0);

	public abstract GeneralInstance get(int index);

	public abstract void insertAttributeAt(GeneralAttribute arg0, int arg1);

	public abstract GeneralInstance instance(int index);

	public abstract double meanOrMode(GeneralAttribute att);

	public abstract double meanOrMode(int index);

	public abstract int numAttributes();

	public abstract int numClasses();

	public abstract int numInstances();

	public abstract String relationName();

	public abstract GeneralInstance remove(int index);

	public abstract void setClass(GeneralAttribute att);

	public abstract void setClassIndex(int classIndex);

	public abstract int size();

	public abstract void sort(GeneralAttribute att);

	public abstract void sort(int arg0);

}