package yueyueGo.databeans;

import java.util.ArrayList;

/**
 * @author robert
 * 用于做框架内部数据传递用，目前暂时使用weka的实现
 */
public class DataAttribute extends WekaAttribute {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7623686334030462898L;

	public DataAttribute(String string, ArrayList<String> values) {
		super(string, values);
	}

	public DataAttribute(String string) {
		super(string);
	}

}
