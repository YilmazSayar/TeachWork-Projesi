package TeachWork.example.TeachWork.dto;

public class ReportDTO {
    private String title;
    private String content;
    private Long taskId;
    private Long uploadedById;

    // Getters ve Setters
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Long getTaskId() {
        return taskId;
    }
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
    public Long getUploadedById() {
        return uploadedById;
    }
    public void setUploadedById(Long uploadedById) {
        this.uploadedById = uploadedById;
    }
}
