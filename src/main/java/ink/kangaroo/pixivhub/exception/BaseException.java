package ink.kangaroo.pixivhub.exception;


/**
 * 基础异常
 * 
 * @author Administrator
 *
 */
public class BaseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 所属模块
	 */
	private String module;

	/**
	 * 错误码
	 */
	private String code;

	/**
	 * 错误码对应的参数
	 */
	private Object[] args;

	/**
	 * 错误消息
	 */
	private String defaultMessage;

	public BaseException(String module, String code, Object[] args, String defaultMessage) {
		this.module = module;
		this.code = code;
		this.args = args;
		this.defaultMessage = defaultMessage;
	}

	public BaseException(String module, String code, Object[] args) {
		this(module, code, args, null);
	}

	public BaseException(String module, String defaultMessage) {
		this(module, null, null, defaultMessage);
	}

	public BaseException(String code, Object[] args) {
		this(null, code, args, null);
	}

	public BaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public BaseException(String defaultMessage) {
		this(null, null, null, defaultMessage);
	}

	public BaseException(ResultEnums resultEnums) {
		this(null,resultEnums.getCode(),null, resultEnums.getMessage());
	}
	public BaseException(ResultEnums resultEnums,Object[] args) {
		this(null,resultEnums.getCode(),args, resultEnums.getMessage());
	}

	public String getModule() {
		return module;
	}

	public String getCode() {
		return code;
	}

	public Object[] getArgs() {
		return args;
	}

	public String getDefaultMessage() {
		return defaultMessage;
	}
}
