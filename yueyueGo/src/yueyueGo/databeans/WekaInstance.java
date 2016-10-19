
package yueyueGo.databeans;

import java.io.Serializable;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;


public class WekaInstance implements Serializable, GeneralInstance{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7442832870650058195L;
	DenseInstance instance;

	public WekaInstance(DenseInstance denseInstance) {
		this.instance = denseInstance;
	}
	
	// only for wekaDataInstances use
	protected WekaInstance(Instance instance) {
		this.instance = (DenseInstance)instance;
	}
	
	protected DenseInstance getInternalDenseInstance() {
		return instance;
	}

	public WekaInstance(int numAttributes) {
		this.instance=new DenseInstance(numAttributes);
	}

	public WekaInstance(WekaInstance instanceToCopy) {
		this.instance=(DenseInstance)(instanceToCopy.getInternalDenseInstance().copy());
	}
	
	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstance#attribute(int)
	 */
	@Override
	public WekaAttribute attribute(int index) {
		Attribute findAttribute=instance.attribute(index);
		if (findAttribute!=null){
			return new WekaAttribute(findAttribute);
		}else{
			return null;
		}
	}


	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstance#classIndex()
	 */
	@Override
	public int classIndex() {
		return instance.classIndex();
	}


	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstance#classValue()
	 */
	@Override
	public double classValue() {
		return instance.classValue();
	}


	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstance#dataset()
	 */
	@Override
	public GeneralInstances dataset() {
		return new WekaInstances(instance.dataset());
	}


	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstance#index(int)
	 */
	@Override
	public int index(int position) {
		return instance.index(position);
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstance#insertAttributeAt(int)
	 */
	@Override
	public void insertAttributeAt(int position) {
		instance.insertAttributeAt(position);
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstance#numAttributes()
	 */
	@Override
	public int numAttributes() {
		return instance.numAttributes();
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstance#setClassValue(double)
	 */
	@Override
	public void setClassValue(double value) {
		instance.setClassValue(value);
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstance#setClassValue(java.lang.String)
	 */
	@Override
	public final void setClassValue(String value) {
		instance.setClassValue(value);
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstance#setDataset(yueyueGo.databeans.BaseInstances)
	 */
	@Override
	public final void setDataset(GeneralInstances instances) {
		instance.setDataset(((WekaInstances)instances).getInternalWekaInstances());
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstance#setValue(yueyueGo.databeans.DataAttribute, double)
	 */
	@Override
	public final void setValue(GeneralAttribute att, double value) {
		instance.setValue(((WekaAttribute)att).getInteranalWekaAttribute(), value);
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstance#setValue(yueyueGo.databeans.DataAttribute, java.lang.String)
	 */
	@Override
	public final void setValue(GeneralAttribute att, String value) {
		instance.setValue(((WekaAttribute)att).getInteranalWekaAttribute(), value);
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstance#setValue(int, double)
	 */
	@Override
	public void setValue(int attIndex, double value) {
		instance.setValue(attIndex, value);
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstance#setValue(int, java.lang.String)
	 */
	@Override
	public final void setValue(int attIndex, String value) {
		instance.setValue(attIndex, value);
	}


	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstance#stringValue(yueyueGo.databeans.DataAttribute)
	 */
	@Override
	public final String stringValue(GeneralAttribute att) {
		return instance.stringValue(((WekaAttribute)att).getInteranalWekaAttribute());
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstance#stringValue(int)
	 */
	@Override
	public final String stringValue(int attIndex) {
		return instance.stringValue(attIndex);
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstance#toString()
	 */
	@Override
	public String toString() {
		return instance.toString();
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstance#value(yueyueGo.databeans.DataAttribute)
	 */
	@Override
	public double value(GeneralAttribute att) {
		return instance.value(((WekaAttribute)att).getInteranalWekaAttribute());
	}

	/* (non-Javadoc)
	 * @see yueyueGo.databeans.BaseInstance#value(int)
	 */
	@Override
	public double value(int attIndex) {
		return instance.value(attIndex);
	}
	/**
	 * @param data
	 * @throws RuntimeException
	 */
	public static DenseInstance convertToWekaInstance(GeneralInstance data)
			throws RuntimeException {
		if (data instanceof WekaInstance){
			DenseInstance wekaData=	((WekaInstance)data).getInternalDenseInstance();
	    	return wekaData;
	    }else{
	    	throw new RuntimeException("expected WEKA data in WEKA related argorithm but not found");
	    }
	}
 }
