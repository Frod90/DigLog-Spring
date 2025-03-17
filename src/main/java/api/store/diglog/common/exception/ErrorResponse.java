package api.store.diglog.common.exception;

import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import lombok.Getter;

@Getter
public class ErrorResponse {

	private final String code;
	private final String message;

	public ErrorResponse(String code, String message) {
		this.code = code;
		this.message = message;
	}

	// Validation 에러 -> ErrorResponse
	public ErrorResponse(MethodArgumentNotValidException e) {
		ObjectError objectError = e.getBindingResult().getAllErrors().getFirst();

		// VALIDATION_필드명 (EMAIL 등)
		String errorCode = "VALIDATION";
		if (objectError instanceof FieldError) {
			String fieldName = ((FieldError)objectError).getField().toUpperCase();
			errorCode = errorCode + "_" + fieldName;
		}

		this.code = errorCode;
		this.message = objectError.getDefaultMessage();
	}
}
