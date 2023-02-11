package ch.micha.automation.room.errorhandling.exceptions;

import ch.micha.automation.room.errorhandling.ErrorMessageDTO;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;

public class UnexpectedSqlException extends AppException {
    private final SQLException exception;

    /**
     * can be used for all types of SQLExceptions. Will let the client know that there has been a server / db error.
     * response error: 500 and is marked as internal server error (for logging).
     * @param unexpectedException the thrown exception
     */
    public UnexpectedSqlException(SQLException unexpectedException) {
        super("sql statement failed: " + unexpectedException.getMessage(), unexpectedException, true);
        this.exception = unexpectedException;
    }

    @Override
    public Response getResponse() {
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorMessageDTO("internal database error", String.format("{code:%s} {state:%s} {message:%s}",
                        exception.getErrorCode(), exception.getSQLState(), exception.getMessage()),
                        super.formatStackTraceToHtml(exception.getStackTrace())))
                .build();
    }
}
