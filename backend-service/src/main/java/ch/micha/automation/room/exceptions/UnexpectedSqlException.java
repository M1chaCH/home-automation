package ch.micha.automation.room.exceptions;

import jakarta.ws.rs.core.Response;

import java.sql.SQLException;

public class UnexpectedSqlException extends AppException{
    private final SQLException exception;

    public UnexpectedSqlException(SQLException unexpectedException) {
        super("sql statement failed: " + unexpectedException.getMessage(), unexpectedException, true);
        this.exception = unexpectedException;
    }

    @Override
    protected Response getResponse() {
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorMessageDTO("internal database error", String.format("{code:%s} {state:%s} {message:%s}",
                        exception.getErrorCode(), exception.getSQLState(), exception.getMessage()),
                        super.formatStackTraceToHtml(exception.getStackTrace())))
                .build();
    }
}
