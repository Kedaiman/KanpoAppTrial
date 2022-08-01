package com.kanpo.trial.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="options")
public class QuestionOption {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column
	@NotNull
	private long id;

	@Column
	private String optionContent;

	@Column
	private long questionId = -1;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column
	private long answerId = -1;

	public QuestionOption() {
		super();
	}

	public QuestionOption(String optionContent, Question question) {
		super();
		this.optionContent = optionContent;
		this.questionId = question.getId();
	}

	public QuestionOption(String optionContent, Answer answer) {
		super();
		this.optionContent = optionContent;
		this.answerId = answer.getId();
	}

	public String getOptionContent() {
		return optionContent;
	}

	public void setOptionContent(String optionContent) {
		this.optionContent = optionContent;
	}

	public long getQuestionId() {
		return questionId;
	}

	public void setQuestionId(long questionId) {
		this.questionId = questionId;
	}

	public long getAnswerId() {
		return answerId;
	}

	public void setAnswerId(long answerId) {
		this.answerId = answerId;
	}
}