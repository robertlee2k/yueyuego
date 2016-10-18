package yueyueGo.databeans;

import java.util.ArrayList;



public interface BaseInstances {

	public abstract boolean add(BaseInstance instance);

	public abstract BaseAttribute attribute(int index);

	public abstract BaseAttribute attribute(String name);

	public abstract double[] attributeToDoubleArray(int arg0);

	public abstract BaseAttribute classAttribute();

	public abstract int classIndex();
	
	public abstract ArrayList<BaseAttribute> getAttributeList() ;
	
	public abstract boolean equalHeaders(BaseInstances dataset);

	public abstract String equalHeadersMsg(BaseInstances arg0);

	public abstract BaseInstance get(int index);

	public abstract void insertAttributeAt(BaseAttribute arg0, int arg1);

	public abstract BaseInstance instance(int index);

	public abstract double meanOrMode(BaseAttribute att);

	public abstract double meanOrMode(int arg0);

	public abstract int numAttributes();

	public abstract int numClasses();

	public abstract int numInstances();

	public abstract String relationName();

	public abstract BaseInstance remove(int index);

	public abstract void setClass(BaseAttribute att);

	public abstract void setClassIndex(int classIndex);

	public abstract int size();

	public abstract void sort(BaseAttribute att);

	public abstract void sort(int arg0);

}