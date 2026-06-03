package com.skillgap.dto;

import java.time.LocalDateTime;
import java.util.List;

public class Dtos {

    // ---- Auth DTOs ----
    public static class RegisterRequest {
        private String name;
        private String email;
        private String password;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class AuthResponse {
        private String token;
        private String email;
        private String name;
        private String role;

        public AuthResponse(String token, String email, String name, String role) {
            this.token = token;
            this.email = email;
            this.name = name;
            this.role = role;
        }

        public String getToken() { return token; }
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getRole() { return role; }
    }

    // ---- Analysis DTOs ----
    public static class AnalyzeRequest {
        private String jobDescription;
        private String resumeText;
        private String jobTitle;

        public String getJobDescription() { return jobDescription; }
        public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }
        public String getResumeText() { return resumeText; }
        public void setResumeText(String resumeText) { this.resumeText = resumeText; }
        public String getJobTitle() { return jobTitle; }
        public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    }

    public static class AnalysisResponse {
        private Long id;
        private String jobTitle;
        private List<String> matchedSkills;
        private List<String> missingSkills;
        private List<LearningResource> learningResources;
        private int matchPercentage;
        private String aiSummary;
        private LocalDateTime analyzedAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getJobTitle() { return jobTitle; }
        public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
        public List<String> getMatchedSkills() { return matchedSkills; }
        public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }
        public List<String> getMissingSkills() { return missingSkills; }
        public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }
        public List<LearningResource> getLearningResources() { return learningResources; }
        public void setLearningResources(List<LearningResource> learningResources) { this.learningResources = learningResources; }
        public int getMatchPercentage() { return matchPercentage; }
        public void setMatchPercentage(int matchPercentage) { this.matchPercentage = matchPercentage; }
        public String getAiSummary() { return aiSummary; }
        public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
        public LocalDateTime getAnalyzedAt() { return analyzedAt; }
        public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
    }

    public static class LearningResource {
        private String skill;
        private String platform;
        private String url;
        private String type;

        public String getSkill() { return skill; }
        public void setSkill(String skill) { this.skill = skill; }
        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class HistoryItemResponse {
        private Long id;
        private String jobTitle;
        private int matchPercentage;
        private LocalDateTime analyzedAt;
        private int missingSkillsCount;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getJobTitle() { return jobTitle; }
        public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
        public int getMatchPercentage() { return matchPercentage; }
        public void setMatchPercentage(int matchPercentage) { this.matchPercentage = matchPercentage; }
        public LocalDateTime getAnalyzedAt() { return analyzedAt; }
        public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
        public int getMissingSkillsCount() { return missingSkillsCount; }
        public void setMissingSkillsCount(int missingSkillsCount) { this.missingSkillsCount = missingSkillsCount; }
    }

    public static class ApiError {
        private String message;
        private int status;
        private LocalDateTime timestamp;

        public ApiError(String message, int status) {
            this.message = message;
            this.status = status;
            this.timestamp = LocalDateTime.now();
        }

        public String getMessage() { return message; }
        public int getStatus() { return status; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}