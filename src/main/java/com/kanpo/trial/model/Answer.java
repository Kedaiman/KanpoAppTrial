package com.kanpo.trial.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
* Answerクラス　(Entityクラス)
* @author　keita
*/
@Entity
@Table(name = "answer")
public class Answer {

	/** IDフィールド(プライマリキー) */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column
	@NotNull
	private long id;

	/** 分析結果(漢方リスト) */
	@ManyToMany
	@Column
	private List<Medicine> medicineList;

	/**
	 * コンストラクタ (デフォルトコンストラクタ)
	 */
	public Answer() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param medicineList 回答結果（漢方リスト)
	 */
	public Answer(List<Medicine> medicineList) {
		super();
		this.medicineList = medicineList;
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
	 * 解析結果（漢方リスト）取得メソッド
	 * @return 漢方リストを返却
	 */
	public List<Medicine> getMedicineList() {
		return medicineList;
	}

	/**
	 * 解析結果（漢方リスト）設定メソッド
	 * @param 解析結果(漢方リスト)
	 */
	public void setMedicineList(List<Medicine> medicineList) {
		this.medicineList = medicineList;
	}
}
