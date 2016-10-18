package yueyueGo.databeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;

import weka.core.Attribute;
import weka.core.Instances;

public class WekaInstances implements Serializable, BaseInstances{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3704902897369927844L;
	Instances internalStore;

	//根据输入Instance格式创建instance的wrapper
	public WekaInstances(Instances instances) {
		internalStore=instances;
	}
	
	//根据输入Instance格式创建空的内部instance
	public WekaInstances(BaseInstances instances,int size) {
		internalStore=new Instances(((WekaInstances)instances).getInternalWekaInstances(),size);
	}
	
	public WekaInstances(BaseInstances data,int from, int to){
		internalStore=new Instances(((WekaInstances)data).getInternalWekaInstances(),from,to);    
	}
	
	//clone一个新的DataInstances
	public WekaInstances(BaseInstances instances) {
		internalStore=new Instances(((WekaInstances)instances).getInternalWekaInstances());
	}
	
	public WekaInstances(String name,ArrayList<BaseAttribute> fvAttributes,int size){
		int length=fvAttributes.size();
		ArrayList<Attribute> fvWekaAttributes=new ArrayList<Attribute> (length);
		for (int i=0;i<length;i++){
			Attribute oneAttribute=((WekaAttribute)(fvAttributes.get(i))).getInteranalWekaAttribute();
			fvWekaAttributes.add(oneAttribute);
		}
		internalStore=new Instances(name,fvWekaAttributes,size);
	}
	

	protected Instances getInternalWekaInstances() {
		return internalStore;
	}


	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#add(yueyueGo.databeans.DataInstance)
	 */
	@Override
	public boolean add(BaseInstance instance) {
		return internalStore.add(((WekaInstance)instance).getInternalDenseInstance());
	}


	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#attribute(int)
	 */
	@Override
	public WekaAttribute attribute(int index) {
		Attribute findAttribute=internalStore.attribute(index);
		if (findAttribute!=null){
			return new WekaAttribute(findAttribute);
		}else{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#attribute(java.lang.String)
	 */
	@Override
	public WekaAttribute attribute(String name) {
		Attribute findAttribute=internalStore.attribute(name);
		if (findAttribute!=null){
			return new WekaAttribute(findAttribute);
		}else{
			return null;
		}
	}


	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#attributeToDoubleArray(int)
	 */
	@Override
	public double[] attributeToDoubleArray(int arg0) {
		return internalStore.attributeToDoubleArray(arg0);
	}


	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#classAttribute()
	 */
	@Override
	public BaseAttribute classAttribute() {
		return new WekaAttribute(internalStore.classAttribute());
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#classIndex()
	 */
	@Override
	public int classIndex() {
		return internalStore.classIndex();
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#enumerateAttributes()
	 */
	@Override
	public ArrayList<BaseAttribute> getAttributeList() {
		
		Enumeration<Attribute> enu = internalStore.enumerateAttributes();
		ArrayList<BaseAttribute> baseAttributes=new ArrayList<BaseAttribute>();
		while (enu.hasMoreElements()) {
			baseAttributes.add(new WekaAttribute((Attribute)enu.nextElement()));
		}
		return baseAttributes;
	}



	public boolean equalHeaders(BaseInstances dataset) {
		return internalStore.equalHeaders(((WekaInstances)dataset).getInternalWekaInstances());
	}


	public String equalHeadersMsg(BaseInstances arg0) {
		return internalStore.equalHeadersMsg(((WekaInstances)arg0).getInternalWekaInstances());
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#get(int)
	 */
	@Override
	public BaseInstance get(int index) {
		return new WekaInstance(internalStore.get(index));
	}


	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#insertAttributeAt(yueyueGo.databeans.DataAttribute, int)
	 */
	@Override
	public void insertAttributeAt(BaseAttribute att, int arg1) {
		internalStore.insertAttributeAt(((WekaAttribute)att).getInteranalWekaAttribute(), arg1);
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#instance(int)
	 */
	@Override
	public WekaInstance instance(int index) {
		return new WekaInstance(internalStore.instance(index));
	}


	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#meanOrMode(yueyueGo.databeans.DataAttribute)
	 */
	@Override
	public double meanOrMode(BaseAttribute att) {
		return internalStore.meanOrMode(((WekaAttribute)att).getInteranalWekaAttribute());
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#meanOrMode(int)
	 */
	@Override
	public double meanOrMode(int arg0) {
		return internalStore.meanOrMode(arg0);
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#numAttributes()
	 */
	@Override
	public int numAttributes() {
		return internalStore.numAttributes();
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#numClasses()
	 */
	@Override
	public int numClasses() {
		return internalStore.numClasses();
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#numInstances()
	 */
	@Override
	public int numInstances() {
		return internalStore.numInstances();
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#relationName()
	 */
	@Override
	public String relationName() {
		return internalStore.relationName();
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#remove(int)
	 */
	@Override
	public BaseInstance remove(int index) {
		return new WekaInstance(internalStore.remove(index));
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#setClass(yueyueGo.databeans.DataAttribute)
	 */
	@Override
	public void setClass(BaseAttribute att) {
		internalStore.setClass(((WekaAttribute)att).getInteranalWekaAttribute());
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#setClassIndex(int)
	 */
	@Override
	public void setClassIndex(int classIndex) {
		internalStore.setClassIndex(classIndex);
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#size()
	 */
	@Override
	public int size() {
		return internalStore.size();
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#sort(yueyueGo.databeans.DataAttribute)
	 */
	@Override
	public void sort(BaseAttribute att) {
		internalStore.sort(((WekaAttribute)att).getInteranalWekaAttribute());
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstances#sort(int)
	 */
	@Override
	public void sort(int arg0) {
		internalStore.sort(arg0);
	}

	/**
	 * @param data
	 * @throws RuntimeException
	 */
	public static Instances convertToWekaInstances(BaseInstances data)
			throws RuntimeException {
		if (data instanceof WekaInstances){
	    	Instances wekaData=	((WekaInstances)data).getInternalWekaInstances();
	    	return wekaData;
	    }else{
	    	throw new RuntimeException("expected WEKA data in WEKA related argorithm but not found");
	    }
	}

	
}
