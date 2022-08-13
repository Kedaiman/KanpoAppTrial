package com.kanpo.trial.exception;

/**
* BadRequestExceptionクラス
* @author　keita
*/
public class BadRequestException extends Exception {
	/** エラーメッセージフィールド */
    private String message;

	/**
	 * コンストラクタ
	 *
	 * @param message エラーメッセージコンストラクタ
	 */
    public BadRequestException(String message){
        this.message = message;
    }

	/**
	 * エラーメッセージを取得するメソッド
	 *
	 * @return エラーメッセージ
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * エラーメッセージを設定するメソッド
	 *
	 * @param message エラーメッセージ
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
