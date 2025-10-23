package com.example.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 設備コードが重複している場合にスローされるカスタム例外。
 * HTTPステータスコード 409 ユニークカラムに対する重複エラーを返す
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateAssetCodeException extends RuntimeException {

    /**
     * 指定された詳細メッセージを持つ新しいDuplicateAssetCodeExceptionを構築します。
     *
     * @param message 例外の詳細メッセージ（ユーザーに表示されるメッセージ）
     */
    public DuplicateAssetCodeException(String message) {
        super(message);
    }

    /**
     * 指定された詳細メッセージと原因を持つ新しいDuplicateAssetCodeExceptionを構築します。
     *
     * @param message 例外の詳細メッセージ
     * @param cause   この例外の原因となるThrowable
     */
    public DuplicateAssetCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
