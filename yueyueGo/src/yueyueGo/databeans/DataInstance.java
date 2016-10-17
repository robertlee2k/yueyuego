
package yueyueGo.databeans;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;


public class DataInstance {
	DenseInstance instance;

	public DataInstance(DenseInstance denseInstance) {
		this.instance = denseInstance;
	}
	
	public DataInstance(Instance instance) {
		this.instance = (DenseInstance)instance;
	}
	public DataInstance(int numAttributes) {
		this.instance=new DenseInstance(numAttributes);
	}

	public DataInstance(DataInstance instance2) {
		this.instance=(DenseInstance)(instance2.getInstance().copy());
	}

	public DenseInstance getInstance() {
		return instance;
	}
	
	public DataAttribute attribute(int index) {
		Attribute findAttribute=instance.attribute(index);
		if (findAttribute!=null){
			return new DataAttribute(findAttribute);
		}else{
			return null;
		}
	}


	public Attribute classAttribute() {
		return instance.classAttribute();
	}

	public int classIndex() {
		return instance.classIndex();
	}

//	public boolean classIsMissing() {
//		return instance.classIsMissing();
//	}

	public double classValue() {
		return instance.classValue();
	}

//	public Object copy() {
//		return instance.copy();
//	}

	public DataInstances dataset() {
		return new DataInstances(instance.dataset());
	}

//	public void deleteAttributeAt(int position) {
//		instance.deleteAttributeAt(position);
//	}

//	public Enumeration<Attribute> enumerateAttributes() {
//		return instance.enumerateAttributes();
//	}
//
//	public boolean equalHeaders(Instance inst) {
//		return instance.equalHeaders(inst);
//	}
//
//	public String equalHeadersMsg(Instance inst) {
//		return instance.equalHeadersMsg(inst);
//	}

//	public boolean equals(Object obj) {
//		return instance.equals(obj);
//	}
//
//	public String getRevision() {
//		return instance.getRevision();
//	}
//
//	public boolean hasMissingValue() {
//		return instance.hasMissingValue();
//	}

//	public int hashCode() {
//		return instance.hashCode();
//	}

	public int index(int position) {
		return instance.index(position);
	}

	public void insertAttributeAt(int position) {
		instance.insertAttributeAt(position);
	}

//	public boolean isMissing(Attribute att) {
//		return instance.isMissing(att);
//	}
//
//	public boolean isMissing(int attIndex) {
//		return instance.isMissing(attIndex);
//	}
//
//	public boolean isMissingSparse(int indexOfIndex) {
//		return instance.isMissingSparse(indexOfIndex);
//	}

//	public Instance mergeInstance(Instance arg0) {
//		return instance.mergeInstance(arg0);
//	}

	public int numAttributes() {
		return instance.numAttributes();
	}

//	public int numClasses() {
//		return instance.numClasses();
//	}

//	public int numValues() {
//		return instance.numValues();
//	}
//
//	public final Instances relationalValue(Attribute att) {
//		return instance.relationalValue(att);
//	}
//
//	public final Instances relationalValue(int attIndex) {
//		return instance.relationalValue(attIndex);
//	}
//
//	public void replaceMissingValues(double[] arg0) {
//		instance.replaceMissingValues(arg0);
//	}
//
//	public void setClassMissing() {
//		instance.setClassMissing();
//	}

	public void setClassValue(double value) {
		instance.setClassValue(value);
	}

	public final void setClassValue(String value) {
		instance.setClassValue(value);
	}

	public final void setDataset(DataInstances instances) {
		instance.setDataset(instances.getInternalStore());
	}
//
//	public final void setMissing(Attribute att) {
//		instance.setMissing(att);
//	}
//
//	public final void setMissing(int attIndex) {
//		instance.setMissing(attIndex);
//	}

	public final void setValue(DataAttribute att, double value) {
		instance.setValue(att.getAttribute(), value);
	}

	public final void setValue(DataAttribute att, String value) {
		instance.setValue(att.getAttribute(), value);
	}

	public void setValue(int attIndex, double value) {
		instance.setValue(attIndex, value);
	}

	public final void setValue(int attIndex, String value) {
		instance.setValue(attIndex, value);
	}


	public final String stringValue(DataAttribute att) {
		return instance.stringValue(att.getAttribute());
	}

	public final String stringValue(int attIndex) {
		return instance.stringValue(attIndex);
	}

//	public double[] toDoubleArray() {
//		return instance.toDoubleArray();
//	}

	public String toString() {
		return instance.toString();
	}

//	public final String toString(DataAttribute att, int afterDecimalPoint) {
//		return instance.toString(att.getAttribute(), afterDecimalPoint);
//	}
//
//	public final String toString(Attribute att) {
//		return instance.toString(att);
//	}
//
//	public final String toString(int attIndex, int afterDecimalPoint) {
//		return instance.toString(attIndex, afterDecimalPoint);
//	}

//	public final String toString(int attIndex) {
//		return instance.toString(attIndex);
//	}
//
//	public final String toStringMaxDecimalDigits(int afterDecimalPoint) {
//		return instance.toStringMaxDecimalDigits(afterDecimalPoint);
//	}
//
//	public String toStringNoWeight() {
//		return instance.toStringNoWeight();
//	}
//
//	public String toStringNoWeight(int arg0) {
//		return instance.toStringNoWeight(arg0);
//	}

	public double value(DataAttribute att) {
		return instance.value(att.getAttribute());
	}

	public double value(int attIndex) {
		return instance.value(attIndex);
	}

//	public double valueSparse(int indexOfIndex) {
//		return instance.valueSparse(indexOfIndex);
//	}

//	public final double weight() {
//		return instance.weight();
//	}

 }
