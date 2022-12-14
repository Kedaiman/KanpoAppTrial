package com.kanpo.trial.cron;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kanpo.trial.configuration.MyProperties;
import com.kanpo.trial.log.MyLogger;
import com.kanpo.trial.model.Analysis;
import com.kanpo.trial.repository.AnalysisRepository;

/**
* スケジュールタスク実行クラス
* @author　keita
*/
@Component
public class ScheduledTasks {

	@Autowired
	AnalysisRepository analysisRepository;

	@Autowired
	MyProperties myproperties;

	/**
	 * analysisオブジェクト定期削除実行メソッド
	 * @return 削除したanalysisオブジェクト数
	 */
	@Scheduled(cron="${scheduledDeleteAnalysis.cron}")
	public int scheduledDeleteAnalysis() {
		int deleteCount = 0;
		try {
			// 現在時刻 - 1分のTimestampオブジェクトを作成する
			Date date = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.MINUTE, -1 * myproperties.getCronDeleteMinute());
			date = calendar.getTime();
			Timestamp timestamp = new Timestamp(date.getTime());

			//　定期的に分析オブジェクトを削除していく
			List<Analysis> deleteAnalysisList = analysisRepository.getByUpdateAtLessThan(timestamp);
			MyLogger.info("delete count of analysisObject = {0}", deleteAnalysisList.size());
			deleteCount = deleteAnalysisList.size();
			analysisRepository.deleteAll(deleteAnalysisList);
		} catch (Exception e) {
			MyLogger.error(e);
		}
		return deleteCount;
	}
}
