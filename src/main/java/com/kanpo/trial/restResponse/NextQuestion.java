package com.kanpo.trial.restResponse;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kanpo.trial.model.QuestionOption;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NextQuestion {
	private long analysisId;
	private String questionContent;
	private List<String> optionList;
	private boolean isNextExist = true;

	public NextQuestion(long analysisId, boolean isNextExist) {
		super();
		this.analysisId = analysisId;
		this.isNextExist = isNextExist;
	}

	public NextQuestion(long analysisId, String questionContent, List<QuestionOption> optionList, boolean isNextExist) {
		super();
		this.analysisId = analysisId;
		this.questionContent = questionContent;
		this.optionList = new ArrayList<String>();
		for (QuestionOption option: optionList){
			this.optionList.add(option.getOptionContent());
		}
		this.isNextExist = isNextExist;
	}

	public boolean isNextExist() {
		return isNextExist;
	}

	public void setNextExist(boolean isNextExist) {
		this.isNextExist = isNextExist;
	}

	public long getAnalysisId() {
		return analysisId;
	}

	public void setAnalysisId(long analysisId) {
		this.analysisId = analysisId;
	}

	public String getQuestionContent() {
		return questionContent;
	}

	public List<String> getOptionList() {
		return optionList;
	}

	public void setOptionList(List<String> optionList) {
		this.optionList = optionList;
	}

	public void setQuestionContent(String questionContent) {
		this.questionContent = questionContent;
	}
}
