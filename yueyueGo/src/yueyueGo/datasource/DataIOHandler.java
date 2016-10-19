package yueyueGo.datasource;


public class DataIOHandler {
	private static WekaDataSupplier weka_supplier = null;
	private static WekaDataSaver weka_saver = null;
	
    public static GeneralDataSupplier getSuppier() {
       if(weka_supplier == null) {
    	   Object obj=new Object();
    	   synchronized (obj) {
    		   if(weka_supplier == null){
    			   weka_supplier = new WekaDataSupplier();
    		   }
    	   }
		}
       return weka_supplier;
    }

	public static GeneralDataSaver getSaver(){
	      if(weka_saver == null) {
	    	   Object obj=new Object();
	    	   synchronized (obj) {
	    		   if(weka_saver == null){
	    			   weka_saver = new WekaDataSaver();
	    		   }
	    	   }
			}
	       return weka_saver;
	}

}
