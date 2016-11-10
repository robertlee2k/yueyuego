package interfaces.jobs;

import org.springframework.stereotype.Service;

import yueyueGo.DailyPredict;

import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHander;
import com.xxl.job.core.model.ReturnT;
import com.xxl.job.core.util.JobClientUtil;

/**
 * 每日预测任务Handler（Bean模式）
 * 
 * 开发步骤：
 * 1、继承 “IJobHandler” ；
 * 2、装配到Spring，例如加 “@Service” 注解；
 * 3、加 “@JobHander” 注解，注解value值为新增任务生成的JobKey的值;多个JobKey用逗号分割;
 * 
 */
@Service
@JobHander(value="DailyPredictJob")
public class DailyPredictJobHandler extends IJobHandler {
	
	@Override
	public void execute(String... params) throws Exception {
		
		String wholeMarketFile=DailyPredict.callFullModelPredict();
		String maFile=DailyPredict.callDailyPredict();
				
		//均线
		ReturnT<String> maFileId=JobClientUtil.uploadJobRealFile(maFile);
		JobClientUtil.setJobExecutorParam(12,"201611091647330137",maFileId.getMsg());
		
		//短线
		ReturnT<String> wholeMarketFileId=JobClientUtil.uploadJobRealFile(wholeMarketFile);
		JobClientUtil.setJobExecutorParam(12,"201611101644540730",wholeMarketFileId.getMsg());
	}
}
