package yueyueGo.databeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;

import weka.core.Attribute;
import weka.core.Instances;

public class DataInstances implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3704902897369927844L;
	Instances internalStore;

	//根据输入Instance格式创建instance的wrapper
	public DataInstances(Instances instances) {
		internalStore=instances;
	}
	
	//根据输入Instance格式创建空的内部instance
	public DataInstances(DataInstances instances,int size) {
		internalStore=new Instances(instances.getInternalStore(),size);
	}
	
	public DataInstances(DataInstances data,int from, int to){
		internalStore=new Instances(data.getInternalStore(),from,to);    
	}
	
	//clone一个新的DataInstances
	public DataInstances(DataInstances instances) {
		internalStore=new Instances(instances.getInternalStore());
	}
	
	public DataInstances(String name,ArrayList<DataAttribute> fvAttributes,int size){
		int length=fvAttributes.size();
		ArrayList<Attribute> fvWekaAttributes=new ArrayList<Attribute> (length);
		for (int i=0;i<length;i++){
			Attribute oneAttribute=fvAttributes.get(i).getAttribute();
			fvWekaAttributes.add(oneAttribute);
		}
		internalStore=new Instances(name,fvWekaAttributes,size);
	}
	
	public Instances getInternalStore() {
		return internalStore;
	}

	public void setInternalStore(Instances internalStore) {
		this.internalStore = internalStore;
	}

	public boolean add(DataInstance instance) {
		return internalStore.add(instance.getInstance());
	}


	public DataAttribute attribute(int index) {
		Attribute findAttribute=internalStore.attribute(index);
		if (findAttribute!=null){
			return new DataAttribute(findAttribute);
		}else{
			return null;
		}
	}

	public DataAttribute attribute(String name) {
		Attribute findAttribute=internalStore.attribute(name);
		if (findAttribute!=null){
			return new DataAttribute(findAttribute);
		}else{
			return null;
		}
	}


	public double[] attributeToDoubleArray(int arg0) {
		return internalStore.attributeToDoubleArray(arg0);
	}


	public DataAttribute classAttribute() {
		return new DataAttribute(internalStore.classAttribute());
	}

	public int classIndex() {
		return internalStore.classIndex();
	}


	
	//TODO: 这里还是临时性保持返回Attribute
	public Enumeration<Attribute> enumerateAttributes() {
		return internalStore.enumerateAttributes();
	}


	public boolean equalHeaders(DataInstances dataset) {
		return internalStore.equalHeaders(dataset.getInternalStore());
	}

	public String equalHeadersMsg(DataInstances arg0) {
		return internalStore.equalHeadersMsg(arg0.getInternalStore());
	}

	public DataInstance get(int index) {
		return new DataInstance(internalStore.get(index));
	}


	public void insertAttributeAt(DataAttribute arg0, int arg1) {
		internalStore.insertAttributeAt(arg0.getAttribute(), arg1);
	}

	public DataInstance instance(int index) {
		return new DataInstance(internalStore.instance(index));
	}


	public double meanOrMode(DataAttribute att) {
		return internalStore.meanOrMode(att.getAttribute());
	}

	public double meanOrMode(int arg0) {
		return internalStore.meanOrMode(arg0);
	}

	public int numAttributes() {
		return internalStore.numAttributes();
	}

	public int numClasses() {
		return internalStore.numClasses();
	}

	public int numInstances() {
		return internalStore.numInstances();
	}

	public String relationName() {
		return internalStore.relationName();
	}

	public DataInstance remove(int index) {
		return new DataInstance(internalStore.remove(index));
	}

	public void setClass(DataAttribute att) {
		internalStore.setClass(att.getAttribute());
	}

	public void setClassIndex(int classIndex) {
		internalStore.setClassIndex(classIndex);
	}

	public int size() {
		return internalStore.size();
	}

	public void sort(DataAttribute att) {
		internalStore.sort(att.getAttribute());
	}

	public void sort(int arg0) {
		internalStore.sort(arg0);
	}

	
}
