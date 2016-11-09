package yueyueGo.jobs;

import org.springframework.stereotype.Service;

import yueyueGo.DailyPredict;

import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHander;

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
//		JobClientUtil.setJobExecutorParam(1,"201611071501120800","JXKJobHadler");
		DailyPredict.main(null);
	}
}
