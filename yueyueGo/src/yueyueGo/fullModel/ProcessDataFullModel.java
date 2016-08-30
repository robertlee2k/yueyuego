package yueyueGo.fullModel;

import yueyueGo.ProcessData;

public class ProcessDataFullModel extends ProcessData {
	public static String C_ROOT_DIRECTORY = "C:\\trend\\fullModel\\";
	
	public static void main(String[] args) {
		try {
			
			UpdateHistoryArffFullModel.createFullModelInstances();
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
}
