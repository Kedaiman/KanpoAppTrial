package com.kanpo.trial.exception;

/**
* InternalServerExceptionクラス
* @author　keita
*/
public class InternalServerException extends Exception {
	/** メッセージフィールド */
    private String message;

	/**
	 * コンストラクタ
	 *
	 * @param message エラーメッセージコンストラクタ
	 */
    public InternalServerException(String message){
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
