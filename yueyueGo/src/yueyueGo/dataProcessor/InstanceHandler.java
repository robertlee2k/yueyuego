package yueyueGo.dataProcessor;


public class InstanceHandler {
	private static WekaInstanceProcessor weka_handler = null;
	
    public static BaseInstanceProcessor getHandler() {
       if(weka_handler == null) {
    	   Object obj=new Object();
    	   synchronized (obj) {
    		   if(weka_handler == null){
    			   weka_handler = new WekaInstanceProcessor();
    		   }
    	   }
		}
       return weka_handler;
    }

}
