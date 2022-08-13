package com.kanpo.trial.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
* Questionクラス　(Entityクラス)
* @author　keita
*/
@Entity
@Table(name="question")
public class Question {

	/** IDフィールド(プライマリキー) */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column
	@NotNull
	private long id;

	/** 質問内容フィールド */
	@Column(nullable = false)
	private String questionContent;

	/** 質問選択オプションリストフィールド */
	@OneToMany
	private List<QuestionOption> optionList;

	/** 前の質問フィールド */
	@OneToOne
	private Question backNode = null;

	/**
	 * コンストラクタ (デフォルトコンストラクタ)
	 */
	public Question() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param questionContent 質問内容
	 * @param optionList 質問選択オプションリスト
	 */
	public Question(String questinContent, List<QuestionOption> optionList) {
		super();
		this.questionContent = questionContent;
		this.optionList = optionList;
	}

	/**
	 * ID取得メソッド
	 * @return IDを返却
	 */
	public long getId() {
		return id;
	}

	/**
	 * ID設定メソッド
	 * @param ID
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * 前の質問を取得するメソッド
	 * @return 前の質問を返却する
	 */
	public Question getBackNode() {
		return backNode;
	}

	/**
	 * 前の質問を設定するメソッド
	 * @param 前の質問オブジェクト
	 */
	public void setBackNode(Question backNode) {
		this.backNode = backNode;
	}

	/**
	 * 質問内容を取得するメソッド
	 * @return 質問内容を返却する
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
	 * 質問の選択オプションリストを取得するメソッド
	 * @return 選択オプションリストを返却する
	 */
	public List<QuestionOption> getOptionList() {
		return optionList;
	}

	/**
	 * 質問の選択オプションリストを設定するメソッド
	 * @param 選択オプションリスト
	 */
	public void setOptionList(List<QuestionOption> optionList) {
		this.optionList = optionList;
	}
}
