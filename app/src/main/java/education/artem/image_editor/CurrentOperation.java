package education.artem.image_editor;

import java.util.HashMap;
import java.util.Map;

public class CurrentOperation {
    private static final CurrentOperation ourInstance = new CurrentOperation();

    public static CurrentOperation getInstance() {
        return ourInstance;
    }

    private static Map<String, String> operationParams;
    private static OperationName currentOperationName;

    private CurrentOperation() {
        operationParams = new HashMap<>();
    }

    public static OperationName getCurrentOperationName() {
        return currentOperationName;
    }

    public static void setCurrentOperationName(OperationName operation) {
        currentOperationName = operation;
    }

    public static Map<String, String> getOperationParams() {
        return operationParams;
    }

    public static void setOperationParams(Map<String, String> operationParams) {
        CurrentOperation.operationParams = operationParams;
    }
}
