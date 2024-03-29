package whu.edu.cn.ogedagboot.util;

public enum HttpStatusUtil {
    // 1XX
    CONTINUE(100), SWITCHING_PROTOCOLS(101),
    PROCESSING(102), EARLY_HINTS(103),
    // 2XX
    OK(200), CREATED(201), ACCEPTED(202),
    NON_AUTHORITATIVE_INFORMATION(203), NO_CONTENT(204),
    RESET_CONTENT(205), PARTIAL_CONTENT(206), MULTI_STATUS(207),
    ALREADY_REPORTED(208), IM_USED(226),
    // 3XX
    MULTIPLE_CHOICE(300),
    // 4XX
    BAD_REQUEST(400), UNAUTHORIZED(401), PROHIBITION(402),
    FORBIDDEN(403), NOT_FOUND(404), METHOD_NOT_ALLOWED(405),
    NOT_ACCEPTABLE(406),
    // 5XX
    INTERNAL_SERVER_ERROR(500), NOT_IMPLEMENTED(501), BAD_GATEWAY(502),
    SERVICE_UNAVAILABLE(503), GATEWAY_TIMEOUT(504);

    private final int code;

    HttpStatusUtil(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
