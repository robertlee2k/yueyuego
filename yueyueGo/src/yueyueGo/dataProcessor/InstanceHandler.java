package yueyueGo.dataProcessor;

import yueyueGo.databeans.GeneralInstances;
import yueyueGo.databeans.WekaInstances;


public class InstanceHandler {
	private static WekaInstanceProcessor weka_handler = null;
	
    public static BaseInstanceProcessor getHandler(GeneralInstances dataToProcess) {
       if(weka_handler == null) {
    	   Object obj=new Object();
    	   synchronized (obj) {
    		   if(weka_handler == null){
    			   if (dataToProcess instanceof WekaInstances) {
    				   weka_handler = new WekaInstanceProcessor();
    			   }else{
    				   throw new RuntimeException("cannot find predefined instancehandler for incoming data");
    			   }
    		   }
    	   }
		}
       return weka_handler;
    }

}
