package yueyueGo;

//这个文件是为不同环境下配置的常量（本地机器，百度云）
public class EnvConstants {

	public final static String AVG_LINE_ROOT_DIR="C:\\trend\\";
	public final static String FULL_MODEL_ROOT_DIR=AVG_LINE_ROOT_DIR+"fullModel\\";
	public final static int TRAINING_DATA_LIMIT=1000000; //用于训练单个模型的数据条数上限（这个和机器的内存有关）
	public final static int CPU_CORE_NUMBER=4;
}
