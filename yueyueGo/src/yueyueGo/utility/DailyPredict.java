package yueyueGo.utility;

import yueyueGo.ProcessData;
import yueyueGo.fullModel.ProcessDataFullModel;

public class DailyPredict {
	public static void main(String[] args) {
		try {
			//短线模型的每日预测
			System.out.println("==================================================");
			System.out.println("===============starting 短线模型预测===============");
			System.out.println("==================================================");
			ProcessDataFullModel fullModelWorker=new ProcessDataFullModel();
			fullModelWorker.init();
			fullModelWorker.callFullModelPredict();

			//用均线模型预测每日增量数据
			System.out.println("==================================================");
			System.out.println("===============starting 均线模型预测===============");
			System.out.println("==================================================");			
			ProcessData worker=new ProcessData();
			worker.init();
			worker.callDailyPredict();
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
}
