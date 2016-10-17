package yueyueGo.databeans;

import java.util.ArrayList;
import java.util.Enumeration;

import weka.core.Attribute;
import weka.core.Instances;

public class DataInstances implements DataBean{

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

//	public void add(int index, Instance instance) {
//		internalStore.add(index, instance);
//	}

//	public boolean addAll(int index, Collection<? extends Instance> c) {
//		return internalStore.addAll(index, c);
//	}
//
//	public boolean addAll(Collection<? extends Instance> c) {
//		return internalStore.addAll(c);
//	}

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

//	public AttributeStats attributeStats(int arg0) {
//		return internalStore.attributeStats(arg0);
//	}

	public double[] attributeToDoubleArray(int arg0) {
		return internalStore.attributeToDoubleArray(arg0);
	}

//	public boolean checkForAttributeType(int attType) {
//		return internalStore.checkForAttributeType(attType);
//	}
//
//	public boolean checkForStringAttributes() {
//		return internalStore.checkForStringAttributes();
//	}
//
//	public boolean checkInstance(Instance arg0) {
//		return internalStore.checkInstance(arg0);
//	}

	public DataAttribute classAttribute() {
		return new DataAttribute(internalStore.classAttribute());
	}

	public int classIndex() {
		return internalStore.classIndex();
	}

//	public boolean contains(Object o) {
//		return internalStore.contains(o);
//	}
//
//	public void clear() {
//		internalStore.clear();
//	}
//
//	public void compactify() {
//		internalStore.compactify();
//	}
//
//	public boolean containsAll(Collection<?> c) {
//		return internalStore.containsAll(c);
//	}

//	public void delete() {
//		internalStore.delete();
//	}
//
//	public void delete(int index) {
//		internalStore.delete(index);
//	}
//
//	public void deleteAttributeAt(int arg0) {
//		internalStore.deleteAttributeAt(arg0);
//	}
//
//	public void deleteAttributeType(int attType) {
//		internalStore.deleteAttributeType(attType);
//	}

//	public void deleteStringAttributes() {
//		internalStore.deleteStringAttributes();
//	}
//
//	public void deleteWithMissing(Attribute att) {
//		internalStore.deleteWithMissing(att);
//	}
//
//	public void deleteWithMissing(int arg0) {
//		internalStore.deleteWithMissing(arg0);
//	}
//
//	public void deleteWithMissingClass() {
//		internalStore.deleteWithMissingClass();
//	}
//
	
	//TODO: 这里还是临时性保持返回Attribute
	public Enumeration<Attribute> enumerateAttributes() {
		return internalStore.enumerateAttributes();
	}
//
//	public Enumeration<Instance> enumerateInstances() {
//		return internalStore.enumerateInstances();
//	}

	public boolean equalHeaders(DataInstances dataset) {
		return internalStore.equalHeaders(dataset.getInternalStore());
	}

	public String equalHeadersMsg(DataInstances arg0) {
		return internalStore.equalHeadersMsg(arg0.getInternalStore());
	}


//	public Instance firstInstance() {
//		return internalStore.firstInstance();
//	}

//	public default void forEach(Consumer<? super Instance> action) {
//		internalStore.forEach(action);
//	}

	public DataInstance get(int index) {
		return new DataInstance(internalStore.get(index));
	}

//	public Random getRandomNumberGenerator(long seed) {
//		return internalStore.getRandomNumberGenerator(seed);
//	}
//
//	public String getRevision() {
//		return internalStore.getRevision();
//	}
//
//	public boolean isEmpty() {
//		return internalStore.isEmpty();
//	}

//	public int indexOf(Object o) {
//		return internalStore.indexOf(o);
//	}
//
//	public Iterator<Instance> iterator() {
//		return internalStore.iterator();
//	}
//
//	public int hashCode() {
//		return internalStore.hashCode();
//	}

	public void insertAttributeAt(DataAttribute arg0, int arg1) {
		internalStore.insertAttributeAt(arg0.getAttribute(), arg1);
	}

	public DataInstance instance(int index) {
		return new DataInstance(internalStore.instance(index));
	}

//	public double kthSmallestValue(Attribute att, int k) {
//		return internalStore.kthSmallestValue(att, k);
//	}
//
//	public double kthSmallestValue(int arg0, int arg1) {
//		return internalStore.kthSmallestValue(arg0, arg1);
//	}
//
//	public int lastIndexOf(Object o) {
//		return internalStore.lastIndexOf(o);
//	}
//
//	public Instance lastInstance() {
//		return internalStore.lastInstance();
//	}
//
//	public ListIterator<Instance> listIterator() {
//		return internalStore.listIterator();
//	}
//
//	public ListIterator<Instance> listIterator(int index) {
//		return internalStore.listIterator(index);
//	}
//
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
//
//	public int numDistinctValues(Attribute att) {
//		return internalStore.numDistinctValues(att);
//	}
//
//	public int numDistinctValues(int arg0) {
//		return internalStore.numDistinctValues(arg0);
//	}

	public int numInstances() {
		return internalStore.numInstances();
	}


//	public void randomize(Random arg0) {
//		internalStore.randomize(arg0);
//	}
//
	public String relationName() {
		return internalStore.relationName();
	}

	public DataInstance remove(int index) {
		return new DataInstance(internalStore.remove(index));
	}

//	public boolean remove(Object o) {
//		return internalStore.remove(o);
//	}

//	public boolean removeAll(Collection<?> c) {
//		return internalStore.removeAll(c);
//	}

//
//	public void renameAttribute(Attribute att, String name) {
//		internalStore.renameAttribute(att, name);
//	}

//	public void renameAttribute(int arg0, String arg1) {
//		internalStore.renameAttribute(arg0, arg1);
//	}

//	public void renameAttributeValue(Attribute att, String val, String name) {
//		internalStore.renameAttributeValue(att, val, name);
//	}
//
//	public void renameAttributeValue(int arg0, int arg1, String arg2) {
//		internalStore.renameAttributeValue(arg0, arg1, arg2);
//	}

//	public void replaceAttributeAt(Attribute arg0, int arg1) {
//		internalStore.replaceAttributeAt(arg0, arg1);
//	}

//	public Instances resample(Random random) {
//		return internalStore.resample(random);
//	}
//
//	public Instances resampleWithWeights(Random random,
//			boolean representUsingWeights) {
//		return internalStore.resampleWithWeights(random, representUsingWeights);
//	}
//
//	public Instances resampleWithWeights(Random arg0, boolean[] arg1,
//			boolean arg2) {
//		return internalStore.resampleWithWeights(arg0, arg1, arg2);
//	}
//
//	public Instances resampleWithWeights(Random random, boolean[] sampled) {
//		return internalStore.resampleWithWeights(random, sampled);
//	}
//
//	public Instances resampleWithWeights(Random arg0, double[] arg1,
//			boolean[] arg2, boolean arg3) {
//		return internalStore.resampleWithWeights(arg0, arg1, arg2, arg3);
//	}
//
//	public Instances resampleWithWeights(Random random, double[] weights,
//			boolean[] sampled) {
//		return internalStore.resampleWithWeights(random, weights, sampled);
//	}
//
//	public Instances resampleWithWeights(Random random, double[] weights) {
//		return internalStore.resampleWithWeights(random, weights);
//	}
//
//	public Instances resampleWithWeights(Random random) {
//		return internalStore.resampleWithWeights(random);
//	}

//	public boolean retainAll(Collection<?> c) {
//		return internalStore.retainAll(c);
//	}

//	public Instance set(int index, Instance instance) {
//		return internalStore.set(index, instance);
//	}

	public void setClass(DataAttribute att) {
		internalStore.setClass(att.getAttribute());
	}

	public void setClassIndex(int classIndex) {
		internalStore.setClassIndex(classIndex);
	}

//	public void setRelationName(String newName) {
//		internalStore.setRelationName(newName);
//	}

	public int size() {
		return internalStore.size();
	}

	public void sort(DataAttribute att) {
		internalStore.sort(att.getAttribute());
	}

	public void sort(int arg0) {
		internalStore.sort(arg0);
	}


//	public void stableSort(Attribute att) {
//		internalStore.stableSort(att);
//	}
//
//	public void stableSort(int arg0) {
//		internalStore.stableSort(arg0);
//	}
//
//	public void stratify(int arg0) {
//		internalStore.stratify(arg0);
//	}
//
//	public Instances stringFreeStructure() {
//		return internalStore.stringFreeStructure();
//	}
//
//	public List<Instance> subList(int fromIndex, int toIndex) {
//		return internalStore.subList(fromIndex, toIndex);
//	}
//
//	public double sumOfWeights() {
//		return internalStore.sumOfWeights();
//	}
//
//	public void swap(int i, int j) {
//		internalStore.swap(i, j);
//	}
//
//	public Instances testCV(int arg0, int arg1) {
//		return internalStore.testCV(arg0, arg1);
//	}
//
//	public Object[] toArray() {
//		return internalStore.toArray();
//	}
//
//	public <T> T[] toArray(T[] a) {
//		return internalStore.toArray(a);
//	}

	public String toString() {
		return internalStore.toString();
	}

	public String toSummaryString() {
		return internalStore.toSummaryString();
	}

//	public Instances trainCV(int numFolds, int numFold, Random random) {
//		return internalStore.trainCV(numFolds, numFold, random);
//	}
//
//	public Instances trainCV(int arg0, int arg1) {
//		return internalStore.trainCV(arg0, arg1);
//	}
//
//	public double variance(Attribute att) {
//		return internalStore.variance(att);
//	}
//
//	public double variance(int arg0) {
//		return internalStore.variance(arg0);
//	}
//
//	public double[] variances() {
//		return internalStore.variances();
//	}

	
}
