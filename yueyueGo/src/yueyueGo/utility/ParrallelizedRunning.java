package yueyueGo.utility;


/**
 * @author robert
 * 如果算法内部已经有线程并发运行实现，则继承这个接口。当外部有线程池并发时控制不要过度并发（算法内外都并发）
 *
 */
public interface ParrallelizedRunning {

	//将外部的并发线程根据算法内并发的计算强度折算出新的建议值
	public abstract int recommendRunningThreads(int runningThreads);
}
