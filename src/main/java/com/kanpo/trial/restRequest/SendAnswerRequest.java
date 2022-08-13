package com.kanpo.trial.restRequest;

import com.kanpo.trial.log.MyLogger;

public class SendAnswerRequest {
	public long analysisId;
	public int answerNum;

	public void outputLog() {
		MyLogger.info("request body: answerId = {0}, answerNum = {1}", analysisId, answerNum);
		return;
	}
}
