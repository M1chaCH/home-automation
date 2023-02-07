package ch.micha.automation.room.exceptions;

import jakarta.ws.rs.core.Response;

public abstract class AppException extends RuntimeException{
    protected final boolean serverError;

    protected abstract Response getResponse();

    protected AppException(String message, Throwable throwable, boolean serverError) {
        super(message, throwable);
        this.serverError = serverError;
    }

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
        htmlBuilder.append("</div");
        return htmlBuilder.toString();
    }

    public boolean isServerError() {
        return serverError;
    }
}
