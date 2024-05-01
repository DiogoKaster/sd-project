package records;

import enums.Operations;

public record Request<T>(Operations operation, String token, T data) {
    public Request(Operations operation, T data) {
        this(operation, null, data);
    }
}
