package yueyueGo.databeans;

import java.util.ArrayList;


/**
 * @author robert
 * 用于做框架内部数据传递用，目前暂时使用weka的实现
 */
public class DataInstances extends WekaInstances{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4782131732219257461L;

	public DataInstances(BaseInstances data, int from, int to) {
		super(data, from, to);
	}

	public DataInstances(BaseInstances instances, int size) {
		super(instances, size);
	}

	public DataInstances(BaseInstances instances) {
		super(instances);
	}

	public DataInstances(String name, ArrayList<BaseAttribute> fvAttributes,
			int size) {
		super(name, fvAttributes, size);
	}

}
