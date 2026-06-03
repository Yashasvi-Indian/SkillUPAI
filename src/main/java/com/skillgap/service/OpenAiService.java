package com.skillgap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillgap.dto.Dtos.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public AnalysisResponse analyzeSkillGap(String resumeText, String jobDescription, String jobTitle) {
        if (apiKey.equals("your-openai-api-key-here") || apiKey.isBlank()) {
            return ruleBasedAnalysis(resumeText, jobDescription, jobTitle);
        }
        try {
            return callOpenAi(resumeText, jobDescription, jobTitle);
        } catch (Exception e) {
            return ruleBasedAnalysis(resumeText, jobDescription, jobTitle);
        }
    }

    private AnalysisResponse callOpenAi(String resumeText, String jobDescription, String jobTitle) throws Exception {
        String prompt = buildPrompt(resumeText, jobDescription, jobTitle);

        String requestBody = objectMapper.writeValueAsString(new java.util.HashMap<>() {{
            put("model", model);
            put("messages", List.of(
                new java.util.HashMap<>() {{
                    put("role", "system");
                    put("content", "You are an expert career coach. Analyze skill gaps. Respond in valid JSON only.");
                }},
                new java.util.HashMap<>() {{
                    put("role", "user");
                    put("content", prompt);
                }}
            ));
            put("temperature", 0.3);
        }});

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode root = objectMapper.readTree(response.body());
        String content = root.path("choices").get(0).path("message").path("content").asText();
        content = content.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
        return parseAiResponse(content, jobTitle);
    }

    private String buildPrompt(String resumeText, String jobDescription, String jobTitle) {
        return String.format("""
            Analyze the skill gap between this resume and job description.
            JOB TITLE: %s
            JOB DESCRIPTION: %s
            RESUME: %s
            Respond ONLY with valid JSON:
            {
              "matchPercentage": 72,
              "matchedSkills": ["Java", "Spring Boot"],
              "missingSkills": ["Kubernetes", "AWS"],
              "aiSummary": "2-3 sentence summary",
              "learningResources": [
                {
                  "skill": "Kubernetes",
                  "platform": "KodeKloud",
                  "url": "https://kodekloud.com",
                  "type": "FREE"
                }
              ]
            }
            """, jobTitle, jobDescription, resumeText);
    }

    private AnalysisResponse parseAiResponse(String json, String jobTitle) throws Exception {
        JsonNode node = objectMapper.readTree(json);
        AnalysisResponse response = new AnalysisResponse();
        response.setJobTitle(jobTitle);
        response.setMatchPercentage(node.path("matchPercentage").asInt(50));
        response.setAiSummary(node.path("aiSummary").asText("Analysis complete."));

        List<String> matched = new ArrayList<>();
        node.path("matchedSkills").forEach(n -> matched.add(n.asText()));
        response.setMatchedSkills(matched);

        List<String> missing = new ArrayList<>();
        node.path("missingSkills").forEach(n -> missing.add(n.asText()));
        response.setMissingSkills(missing);

        List<LearningResource> resources = new ArrayList<>();
        node.path("learningResources").forEach(r -> {
            LearningResource lr = new LearningResource();
            lr.setSkill(r.path("skill").asText());
            lr.setPlatform(r.path("platform").asText());
            lr.setUrl(r.path("url").asText());
            lr.setType(r.path("type").asText("FREE"));
            resources.add(lr);
        });
        response.setLearningResources(resources);
        return response;
    }

    private AnalysisResponse ruleBasedAnalysis(String resumeText, String jobDescription, String jobTitle) {
        String resumeLower = resumeText.toLowerCase();
        String jdLower = jobDescription.toLowerCase();

        List<String> allSkills = Arrays.asList(
            "java", "spring boot", "spring", "maven", "hibernate", "jpa",
            "rest api", "microservices", "docker", "kubernetes", "aws", "gcp", "azure",
            "mysql", "postgresql", "mongodb", "redis", "kafka",
            "javascript", "typescript", "react", "angular", "node.js",
            "python", "git", "github", "ci/cd", "jenkins",
            "junit", "testing", "jwt", "security", "html", "css",
            "agile", "scrum", "linux", "bash", "graphql"
        );

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String skill : allSkills) {
            boolean inJd = jdLower.contains(skill);
            boolean inResume = resumeLower.contains(skill);
            if (inJd && inResume) matched.add(formatSkill(skill));
            else if (inJd && !inResume) missing.add(formatSkill(skill));
        }

        int matchPct = matched.isEmpty() && missing.isEmpty() ? 50 :
            (int)((matched.size() / (double)(matched.size() + missing.size())) * 100);

        List<LearningResource> resources = missing.stream().limit(6).map(skill -> {
            LearningResource lr = new LearningResource();
            lr.setSkill(skill);
            lr.setPlatform(getPlatform(skill));
            lr.setUrl(getUrl(skill));
            lr.setType("FREE");
            return lr;
        }).collect(Collectors.toList());

        String summary = String.format(
            "You match %d%% of the required skills for this %s role. " +
            "You have strong skills in %s. Focus on learning %s to become a strong candidate.",
            matchPct, jobTitle,
            matched.isEmpty() ? "several areas" : String.join(", ", matched.subList(0, Math.min(3, matched.size()))),
            missing.isEmpty() ? "nothing — you are well prepared!" : String.join(", ", missing.subList(0, Math.min(3, missing.size())))
        );

        AnalysisResponse response = new AnalysisResponse();
        response.setJobTitle(jobTitle);
        response.setMatchPercentage(matchPct);
        response.setMatchedSkills(matched);
        response.setMissingSkills(missing);
        response.setLearningResources(resources);
        response.setAiSummary(summary);
        return response;
    }

    private String formatSkill(String skill) {
        return Arrays.stream(skill.split(" "))
            .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1))
            .collect(Collectors.joining(" "));
    }

    private String getPlatform(String skill) {
        String s = skill.toLowerCase();
        if (s.contains("docker") || s.contains("kubernetes")) return "KodeKloud";
        if (s.contains("aws") || s.contains("azure")) return "AWS/Azure Free Tier";
        if (s.contains("java") || s.contains("spring")) return "Baeldung";
        if (s.contains("python")) return "freeCodeCamp";
        return "roadmap.sh";
    }

    private String getUrl(String skill) {
        String s = skill.toLowerCase();
        if (s.contains("docker")) return "https://kodekloud.com/courses/docker-for-the-absolute-beginner/";
        if (s.contains("kubernetes")) return "https://kodekloud.com/courses/kubernetes-for-the-absolute-beginners/";
        if (s.contains("aws")) return "https://aws.amazon.com/free/";
        if (s.contains("spring")) return "https://www.baeldung.com/spring-boot";
        if (s.contains("react")) return "https://react.dev/learn";
        if (s.contains("python")) return "https://www.freecodecamp.org/";
        if (s.contains("git")) return "https://learngitbranching.js.org/";
        return "https://roadmap.sh/";
    }
}