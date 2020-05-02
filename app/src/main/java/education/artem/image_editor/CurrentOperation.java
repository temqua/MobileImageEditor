package education.artem.image_editor;

public class CurrentOperation {
    private static final CurrentOperation ourInstance = new CurrentOperation();

    public static CurrentOperation getInstance() {
        return ourInstance;
    }

    private CurrentOperation() {
    }

    private static OperationName currentOperation;

    public static void setCurrentOperation(OperationName operation){
        currentOperation = operation;
    }

    public static OperationName getCurrentOperation(){
        return currentOperation;
    }
}
