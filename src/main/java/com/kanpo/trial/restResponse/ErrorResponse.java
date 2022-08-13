package com.kanpo.trial.restResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.kanpo.trial.exception.BadRequestException;
import com.kanpo.trial.exception.InternalServerException;

/**
 * エラーレスポンス クラス
 * @author keita
 */
public class ErrorResponse {

	/** メッセージ */
    private String message;

	/**
	 * コンストラクタ
	 * @param message メッセージ
	 */
    public ErrorResponse(String  message){
        this.message = message;
    }

	/**
	 * レスポンスオブジェクト生成メソッド
	 * @param e BadRequestExceptionオブジェクト
	 * @return  エラーレスポンスオブジェクト
	 */
    public static ResponseEntity<ErrorResponse> createResponse(BadRequestException e){
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

	/**
	 * レスポンスオブジェクト生成メソッド
	 * @param e InternalServerExceptionオブジェクト
	 * @return  エラーレスポンスオブジェクト
	 */
    public static ResponseEntity<ErrorResponse> createResponse(InternalServerException e) {
    	return new ResponseEntity<ErrorResponse>(
    			new ErrorResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

	/**
	 * メッセージ取得メソッド
	 * @return メッセージを返却
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * メッセージ設定メソッド
	 * @param メッセージ
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}