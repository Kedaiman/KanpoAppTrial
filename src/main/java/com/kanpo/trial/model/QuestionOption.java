package com.kanpo.trial.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
* QuestionOptionクラス　(Entityクラス)
* @author　keita
*/
@Entity
@Table(name="options")
public class QuestionOption {

	/** IDフィールド(プライマリキー) */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column
	@NotNull
	private long id;

	/** 選択オプション内容 */
	@Column
	private String optionContent;

	/** オプションを選択した時の次の質問ID */
	@Column
	private long questionId = -1;

	/** オプションを選択した時の次の回答ID */
	@Column
	private long answerId = -1;

	/**
	 * コンストラクタ (デフォルトコンストラクタ)
	 */
	public QuestionOption() {
		super();
	}

	/**
	 * コンストラクタ(オプションを選択したときに質問を設定したい場合)
	 * @param optionContent オプションの選択内容
	 * @param question 次の質問オブジェクト
	 */
	public QuestionOption(String optionContent, Question question) {
		super();
		this.optionContent = optionContent;
		this.questionId = question.getId();
	}

	/**
	 * コンストラクタ(オプションを選択したときに解答を設定したい場合)
	 * @param optionContent オプションの選択内容
	 * @param question 次の質問オブジェクト
	 */
	public QuestionOption(String optionContent, Answer answer) {
		super();
		this.optionContent = optionContent;
		this.answerId = answer.getId();
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
	 * 選択オプション内容取得メソッド
	 * @return 選択オプション内容を返却
	 */
	public String getOptionContent() {
		return optionContent;
	}

	/**
	 * 選択オプション内容設定メソッド
	 * @param 選択オプション内容
	 */
	public void setOptionContent(String optionContent) {
		this.optionContent = optionContent;
	}

	/**
	 * 次の質問ID取得メソッド
	 * @return 次の質問ID
	 */
	public long getQuestionId() {
		return questionId;
	}

	/**
	 * 次の質問ID設定メソッド
	 * @return 次の質問ID
	 */
	public void setQuestionId(long questionId) {
		this.questionId = questionId;
	}

	/**
	 * 解答ID取得メソッド
	 * @return 解答ID
	 */
	public long getAnswerId() {
		return answerId;
	}

	/**
	 * 解答ID設定メソッド
	 * @param 解答ID
	 */
	public void setAnswerId(long answerId) {
		this.answerId = answerId;
	}
}