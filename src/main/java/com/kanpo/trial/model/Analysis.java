package com.kanpo.trial.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;


/**
* Analysisクラス　(Entityクラス)
* @author　keita
*/
@Entity
@Table(name = "analysis")
public class Analysis {
	/** 定数設定 (分析継続) */
	public static final int CONTINUE = 0;
	/** 定数設定 (分析終了) */
	public static final int END = 1;

	/** IDフィールド(プライマリキー) */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column
	@NotNull
	private long id;

	/** 分析状態フィールド */
	@Column
	private long status;

	/** 現在の質問オブジェクト */
	@OneToOne
	private Question nowQuestion;

	/** 分析オブジェクト更新日時 */
	@Column
	private Timestamp updateAt;

	/** 回答オブジェクトID */
	@Column
	private long answerId = -1;

	/**
	 * コンストラクタ (デフォルトコンストラクタ)
	 */
	public Analysis() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param topQuestion TOPの質問オブジェクト
	 */
	public Analysis(Question topQuestion) {
		super();
		this.status = CONTINUE;
		this.nowQuestion = topQuestion;
	}

	/**
	 * 分析オブジェクト更新日時登録メソッド
	 */
	@PrePersist
	public void onPrePersist() {
		setUpdateAt(new Timestamp(System.currentTimeMillis()));
	}

	/**
	 * 分析オブジェクト更新日時更新メソッド
	 */
	@PreUpdate
	public void onPreUpdate() {
		setUpdateAt(new Timestamp(System.currentTimeMillis()));
	}

	/**
	 * 分析ID取得メソッド
	 * @return IDを返却
	 */
	public long getId() {
		return id;
	}

	/**
	 * 分析ID設定メソッド
	 * @param ID
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * 分析オブジェクトの分析状態取得メソッド
	 * @return 分析状態を返却
	 */
	public long getStatus() {
		return status;
	}

	/**
	 * 分析オブジェクトの分析状態設定メソッド
	 * @param 分析状態
	 */
	public void setStatus(long status) {
		this.status = status;
	}

	/**
	 * 分析オブジェクト更新日時取得メソッド
	 * @return 分析オブジェクト更新日時を返却
	 */
	public Timestamp getUpdateAt() {
		return updateAt;
	}

	/**
	 * 分析オブジェクト更新日時設定メソッド
	 * @param updateAt 更新日時
	 */
	public void setUpdateAt(Timestamp updateAt) {
		this.updateAt = updateAt;
	}

	/**
	 * 回答IDメソッド取得メソッド
	 * @return 回答IDを返却
	 */
	public long getAnswerId() {
		return answerId;
	}

	/**
	 * 回答IDメソッド設定メソッド
	 * @param answerId 回答ID
	 */
	public void setAnswerId(long answerId) {
		this.answerId = answerId;
	}

	/**
	 * 質問オブジェクトを取得メソッド
	 * @return 現在の質問オブジェクトを返却
	 */
	public Question getNowQuestion() {
		return nowQuestion;
	}

	/**
	 * 質問オブジェクト設定メソッド
	 * @param nowQuestion 質問オブジェクト
	 */
	public void setNowQuestion(Question nowQuestion) {
		this.nowQuestion = nowQuestion;
	}
}
