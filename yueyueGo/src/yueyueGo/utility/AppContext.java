package yueyueGo.utility;

import yueyueGo.EnvConstants;

//singleton 
public class AppContext {
	private static AppContext singleton = null;
	
	//在子类中可以另外定义根目录
	protected String C_ROOT_DIRECTORY =null;
	protected String NOMINAL_CLASSIFIER_DIR=null;
	protected String CONTINOUS_CLASSIFIER_DIR=null;
	protected String BACKTEST_RESULT_DIR=null;
	
	private AppContext(String rootPath) {
    	C_ROOT_DIRECTORY=rootPath;
		NOMINAL_CLASSIFIER_DIR = rootPath+"\\models\\01-二分类器\\";
		CONTINOUS_CLASSIFIER_DIR = rootPath+"\\models\\02-连续分类器\\";
		BACKTEST_RESULT_DIR=rootPath+"\\testResult\\";

    }

    public static AppContext createContext(String dataRootPath) {
       if(singleton == null) {
    	   Object obj=new Object();
    	   synchronized (obj) {
    		   if(singleton == null){
    			   singleton = new AppContext(EnvConstants.ROOT_DIR+dataRootPath);
    		   }
    	   }
		}
       return singleton;
    }
    
    //清除环境变量，重设context时调用
    public static void clearContext() {
    	singleton=null;
    }

	public static String getC_ROOT_DIRECTORY() {
		return singleton.C_ROOT_DIRECTORY;
	}

	public static String getNOMINAL_CLASSIFIER_DIR() {
		return singleton.NOMINAL_CLASSIFIER_DIR;
	}

	public static String getCONTINOUS_CLASSIFIER_DIR() {
		return singleton.CONTINOUS_CLASSIFIER_DIR;
	}

	public static String getBACKTEST_RESULT_DIR() {
		return singleton.BACKTEST_RESULT_DIR;
	}

    
    
}
