package yueyueGo;

//这个文件是为不同环境下配置的常量（本地机器，百度云）
public class EnvConstants {

	
	
	// 本地机器
	public final static String ROOT_DIR="C:\\trend\\"; //应用系统的根目录
	public final static int TRAINING_DATA_LIMIT=1000000; //用于训练单个模型的数据条数上限（这个和机器的内存有关）
	public final static int CPU_CORE_NUMBER=4; //cpu个数，用于计算线程数
	public final static int HEAP_SIZE=6; //java可用heap，单位是G
	//百度云
//	public final static String ROOT_DIR="d:\\trend\\"; //应用系统的根目录
//	public final static int TRAINING_DATA_LIMIT=5000000; //用于训练单个模型的数据条数上限（这个和机器的内存有关）
//	public final static int CPU_CORE_NUMBER=16;//cpu个数，用于计算线程数
//	public final static int HEAP_SIZE=56; //java可用heap，单位是G


	//各处共用的环境变量
	public static String PREDICT_WORK_DIR=ROOT_DIR+"03-预测模型\\"; //这个是常量
	public final static String AVG_LINE_ROOT_DIR=ROOT_DIR; //均线模型的根目录
	public final static String FULL_MODEL_ROOT_DIR=ROOT_DIR+"fullModel\\"; //短线模型的根目录

}
