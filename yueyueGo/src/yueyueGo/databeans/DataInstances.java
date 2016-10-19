package yueyueGo.databeans;

import java.util.ArrayList;

import weka.core.Instances;


/**
 * @author robert
 * 用于做框架内部数据传递用，目前暂时使用weka的实现
 */
public class DataInstances extends WekaInstances{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4782131732219257461L;

	public DataInstances(GeneralInstances data, int from, int to) {
		super(data, from, to);
	}

	public DataInstances(GeneralInstances instances, int size) {
		super(instances, size);
	}

	public DataInstances(GeneralInstances instances) {
		super(instances);
	}

	public DataInstances(String name, ArrayList<GeneralAttribute> fvAttributes,
			int size) {
		super(name, fvAttributes, size);
	}

	//利用weke的Instances构建DataInstances
	public DataInstances(Instances curve) {
		super(curve);
	}

}
