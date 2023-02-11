package ch.micha.automation.room.errorhandling.exceptions;

import jakarta.ws.rs.core.Response;

/**
 * This is a global exception that can be used throughout the app.
 * It provides an easy way for the api to handle errors automatically.
 */
public abstract class AppException extends RuntimeException{
    /**
     * used to determine how the error should be logged. true: error will be logged with level SEVER and the stack
     * trace will be included, else Level.INFO
     */
    protected final boolean serverError;

    /**
     * @return the response that will be sent to the client if the exception is thrown during an HTTP request.
     */
    public abstract Response getResponse();

    protected AppException(String internalLogMessage, Throwable throwable, boolean serverError) {
        super(internalLogMessage, throwable);
        this.serverError = serverError;
    }

    /**
     * can be used to format the content for the "htmlStack" field in the ErrorMessageDTO
     * @param stackTrace the stack trace of the current exception
     * @return an HTML formatted string that can be displayed in the UI.
     */
    protected String formatStackTraceToHtml(StackTraceElement[] stackTrace) {
        StringBuilder htmlBuilder = new StringBuilder("<div>");
        for (StackTraceElement element : stackTrace) {
            htmlBuilder
                    .append("<p>")
                    .append(element.getLineNumber())
                    .append(": ")
                    .append(element.getClassName())
                    .append(" -> ")
                    .append(element.getMethodName())
                    .append("</p>");
        }
        htmlBuilder.append("</div>");
        return htmlBuilder.toString();
    }

    public boolean isServerError() {
        return serverError;
    }
}
