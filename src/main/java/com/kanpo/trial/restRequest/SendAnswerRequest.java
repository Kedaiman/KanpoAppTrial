package com.kanpo.trial.restRequest;

import com.kanpo.trial.log.MyLogger;

import javax.persistence.criteria.CriteriaBuilder;

/**
 *  sendAnswer APIのリクエストクラス
 * @author keita
 */
public class SendAnswerRequest {

	/** 分析ID*/
	public long analysisId;
	/** 解答番号*/
	public int answerNum;

	public SendAnswerRequest(long analysisId, int answerNum) {
		this.analysisId = analysisId;
		this.answerNum = answerNum;
	}

	public SendAnswerRequest() {}

	/**
	 * リクエスト内容をログに出力するメソッド
	 */
	public void outputLog() {
		MyLogger.info("request body: answerId = {0}, answerNum = {1}", analysisId, answerNum);
		return;
	}
}
