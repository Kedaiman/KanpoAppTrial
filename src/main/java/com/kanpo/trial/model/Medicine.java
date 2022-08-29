package com.kanpo.trial.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

/**
* Medicineクラス　(Entityクラス)
* @author　keita
*/
@Entity
@Table(name = "medicine")
public class Medicine {

	/** IDフィールド(プライマリキー) */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column
	@NotNull
	private long id;

	/** 漢方名 */
	@Column
	@NotNull
	@NotEmpty
	private String name;

	/** 漢方名 (ふりがな) */
	@Column
	@NotNull
	private String nameKana;

	/** 漢方説明 */
	@Column
	private String detailInfo;

	/** 漢方の画像のパス */
	@Column
	private String imagePath;

	/**
	 * コンストラクタ (デフォルトコンストラクタ)
	 */
	public Medicine() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param name 漢方名
	 * @param detailInfo 漢方説明
	 */
	public Medicine(String name, String detailInfo) {
		super();
		this.name = name;
		this.detailInfo = detailInfo;
	}

	/**
	 * コンストラクタ
	 * @param name 漢方名
	 * @param detailInfo 漢方説明
	 * @param imagePath 画像パス
	 */
	public Medicine(String name, String nameKana, String detailInfo, String imagePath) {
		super();
		this.name = name;
		this.nameKana = nameKana;
		this.detailInfo = detailInfo;
		this.imagePath = imagePath;
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
	 * 漢方名取得メソッド
	 * @return 漢方名を返却
	 */
	public String getName() {
		return name;
	}

	/**
	 * 漢方名設定メソッド
	 * @param 漢方名
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 漢方名(ふりがな)取得メソッド
	 * @return 漢方名(ふりがな)を返却
	 */

	public String getNameKana() {
		return nameKana;
	}

	/**
	 * 漢方名(ふりがな)設定メソッド
	 * @param 漢方名(ふりがな)
	 */
	public void setNameKana(String nameKana) {
		this.nameKana = nameKana;
	}

	/**
	 * 漢方説明取得メソッド
	 * @return 漢方説明を返却
	 */
	public String getDetailInfo() {
		return detailInfo;
	}

	/**
	 * 漢方説明設定メソッド
	 * @param 漢方説明
	 */
	public void setDetailInfo(String detailInfo) {
		this.detailInfo = detailInfo;
	}

	/**
	 * 漢方画像パス取得メソッド
	 * @return 漢方画像パスを返却
	 */
	public String getImagePath() {
		return imagePath;
	}

	/**
	 * 漢方画像パス設定メソッド
	 * @param 漢方画像パス
	 */
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
}
