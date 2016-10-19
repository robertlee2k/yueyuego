package yueyueGo.datasource;


public class DataIOHandler {
	private static GeneralDataSupplier supplier = null;
	private static GeneralDataSaver saver = null;
	
    public static GeneralDataSupplier getSuppier() {
       if(supplier == null) {
    	   Object obj=new Object();
    	   synchronized (obj) {
    		   if(supplier == null){
    			   supplier = new WekaDataSupplier();
    		   }
    	   }
		}
       return supplier;
    }

	public static GeneralDataSaver getSaver(){
	      if(saver == null) {
	    	   Object obj=new Object();
	    	   synchronized (obj) {
	    		   if(saver == null){
	    			   saver = new WekaDataSaver();
	    		   }
	    	   }
			}
	       return saver;
	}

}
