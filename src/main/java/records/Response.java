package records;

import enums.Operations;
import enums.Statuses;

public record Response<T>(Operations operation, Statuses status, T data) {
    public Response(Operations operation, Statuses status) {
        this(operation, status, (T) new Object());
    }
}
