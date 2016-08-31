package yueyueGo;


//singleton 
public class RuntimeParams {
	private static RuntimeParams singleton = null;
	
	//在子类中可以另外定义根目录
	protected String C_ROOT_DIRECTORY =null;
	protected String NOMINAL_CLASSIFIER_DIR=null;
	protected String CONTINOUS_CLASSIFIER_DIR=null;
	protected String BACKTEST_RESULT_DIR=null;
	protected String PREDICT_WORK_DIR=null;

    private RuntimeParams(String rootPath) {
    	C_ROOT_DIRECTORY=rootPath;
		NOMINAL_CLASSIFIER_DIR = rootPath+"models\\01-二分类器\\";
		CONTINOUS_CLASSIFIER_DIR = rootPath+"models\\02-连续分类器\\";
		BACKTEST_RESULT_DIR=rootPath+"testResult\\";
		PREDICT_WORK_DIR=rootPath+"03-预测模型\\";	
    }

    public static RuntimeParams createInstance(String rootPath) {
       if(singleton == null) {
    	   Object obj=new Object();
    	   synchronized (obj) {
    		   if(singleton == null){
    			   singleton = new RuntimeParams(rootPath);
    		   }
    	   }
		}
       return singleton;
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

	public static String getPREDICT_WORK_DIR() {
		return singleton.PREDICT_WORK_DIR;
	}
    
    
}
