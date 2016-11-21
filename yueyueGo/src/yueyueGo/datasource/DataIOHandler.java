package yueyueGo.datasource;


public class DataIOHandler {
	
	//用于存放原始数据的公共数据仓库定义
	public final static String URL = "jdbc:mysql://uts.simu800.com/develop?characterEncoding=utf8&autoReconnect=true";
	public final static String USER = "root";
	public final static String PASSWORD = "data@2014";
	
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
