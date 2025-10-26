package TeachWork.example.TeachWork.model;

public enum TaskStatus {
    PENDING("Beklemede"),
    IN_PROGRESS("Devam Ediyor"),
    COMPLETED("Tamamlandı");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
