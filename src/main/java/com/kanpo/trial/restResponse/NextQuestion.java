package com.kanpo.trial.restResponse;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kanpo.trial.log.MyLogger;
import com.kanpo.trial.model.QuestionOption;

/**
 * 次の質問を返却するレスポンス クラス
 * @author keita
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NextQuestion {
	/** 分析ID */
	private long analysisId;
	/** 質問内容 */
	private String questionContent;
	/** 質問選択オプションリスト */
	private List<String> optionList;
	/** 次の質問が存在するかフラグ */
	private boolean isNextExist = true;

	/**
	 * コンストラクタ
	 * @param analysisId 分析ID
	 * @param isNextExist 次の質問の存在フラグ
	 */
	public NextQuestion(long analysisId, boolean isNextExist) {
		super();
		this.analysisId = analysisId;
		this.isNextExist = isNextExist;
	}

	/**
	 * コンストラクタ
	 * @param analysisId 分析ID
	 * @param questionContent 次の質問内容
	 * @param optionList 質問の選択オプションリスト
	 * @param isNextExist 次の質問の存在フラグ
	 */
	public NextQuestion(long analysisId, String questionContent, List<QuestionOption> optionList, boolean isNextExist) {
		super();
		this.analysisId = analysisId;
		this.questionContent = questionContent;
		this.optionList = new ArrayList<String>();
		for (QuestionOption option: optionList){
			this.optionList.add(option.getOptionContent());
		}
		this.isNextExist = isNextExist;

		this.outputLog();
	}

	/**
	 * レスポンス内容をログ出力するメソッド
	 */
	public void outputLog() {
		MyLogger.info("response: analysisId = {0}, isNextExist = {1}", analysisId, isNextExist);
		return;
	}

	/**
	 * 次の質問の存在フラグを取得するメソッド
	 * @return 次の質問の存在フラグ
	 */
	public boolean getIsNextExist() {
		return isNextExist;
	}

	/**
	 * 次の質問の存在フラグを設定するメソッド
	 * @param 次の質問の存在フラグ
	 */
	public void setIsNextExist(boolean isNextExist) {
		this.isNextExist = isNextExist;
	}

	/**
	 * 分析IDを取得するメソッド
	 * @return 分析ID
	 */
	public long getAnalysisId() {
		return analysisId;
	}

	/**
	 * 分析IDを設定するメソッド
	 * @param 分析ID
	 */
	public void setAnalysisId(long analysisId) {
		this.analysisId = analysisId;
	}

	/**
	 * 質問内容を取得するメソッド
	 * @return 質問内容
	 */
	public String getQuestionContent() {
		return questionContent;
	}

	/**
	 * 質問内容を設定するメソッド
	 * @param 質問内容
	 */
	public void setQuestionContent(String questionContent) {
		this.questionContent = questionContent;
	}

	/**
	 * 質問選択オプションリストを取得するメソッド
	 * @return 質問選択オプションリスト
	 */
	public List<String> getOptionList() {
		return optionList;
	}

	/**
	 * 質問選択オプションリストを設定するメソッド
	 * @param 質問選択オプションリスト
	 */
	public void setOptionList(List<String> optionList) {
		this.optionList = optionList;
	}
}
