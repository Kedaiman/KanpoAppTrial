package com.kanpo.trial.restRequest;

import com.kanpo.trial.log.MyLogger;

/**
 *  sendAnswer APIのリクエストクラス
 * @author keita
 */
public class SendAnswerRequest {

	/** 分析ID*/
	public long analysisId;
	/** 解答番号*/
	public int answerNum;

	/**
	 * リクエスト内容をログに出力するメソッド
	 */
	public void outputLog() {
		MyLogger.info("request body: answerId = {0}, answerNum = {1}", analysisId, answerNum);
		return;
	}
}
