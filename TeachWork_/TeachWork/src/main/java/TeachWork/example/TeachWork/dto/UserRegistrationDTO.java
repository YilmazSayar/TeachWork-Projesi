package TeachWork.example.TeachWork.dto;

public class UserRegistrationDTO {
    private String email;
    private String password;
    private String fullName; // İstersen başka alanlar da ekleyebilirsin

    // Getters & Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
