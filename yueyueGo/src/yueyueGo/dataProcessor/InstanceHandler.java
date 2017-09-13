package yueyueGo.dataProcessor;

import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaInstances;


public class InstanceHandler {
	private static WekaInstanceProcessor weka_handler = null;
	/*
	 * 这里的传入dataToProcess具体的列格式并不重要
	 * 我们只是通过它判断是否是WekaInstance来生成Handler
	 */
    public static BaseInstanceProcessor getHandler(GeneralInstances dataToProcess) {
    	if (dataToProcess instanceof WekaInstances) {
    		if(weka_handler == null) {
    			Object obj=new Object();
    			synchronized (obj) {
    				if(weka_handler == null){
    					weka_handler = new WekaInstanceProcessor();
    				}
    			}
    		}
    		return weka_handler;
    	}else{
    		throw new RuntimeException("cannot find predefined instancehandler for incoming data");
    	}
    }
}
