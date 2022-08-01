package com.kanpo.trial.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="question")
public class Question {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column
	@NotNull
	private long id;

	@Column(nullable = false)
	private String questionContent;

	@OneToMany
	private List<QuestionOption> optionList;

	//@ManyToMany
	//private List<QuestionOption> nodeList;

	public Question() {
		super();
	}

	public Question(String questinContent, List<QuestionOption> optionList) {
		super();
		this.questionContent = questionContent;
		this.optionList = optionList;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getQuestionContent() {
		return questionContent;
	}

	public void setQuestionContent(String questionContent) {
		this.questionContent = questionContent;
	}

	public List<QuestionOption> getOptionList() {
		return optionList;
	}

	public void setOptionList(List<QuestionOption> optionList) {
		this.optionList = optionList;
	}
}
