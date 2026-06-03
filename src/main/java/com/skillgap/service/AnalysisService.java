package com.skillgap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillgap.dto.Dtos.*;
import com.skillgap.entity.AnalysisResult;
import com.skillgap.entity.User;
import com.skillgap.repository.AnalysisResultRepository;
import com.skillgap.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    private final OpenAiService openAiService;
    private final AnalysisResultRepository analysisResultRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalysisService(OpenAiService openAiService,
                           AnalysisResultRepository analysisResultRepository,
                           UserRepository userRepository) {
        this.openAiService = openAiService;
        this.analysisResultRepository = analysisResultRepository;
        this.userRepository = userRepository;
    }

    public AnalysisResponse analyze(AnalyzeRequest request, String userEmail) {
        User user = getUser(userEmail);

        String jobTitle = (request.getJobTitle() == null || request.getJobTitle().isBlank())
                ? "Software Engineer" : request.getJobTitle();

        // Run AI or rule-based analysis
        AnalysisResponse response = openAiService.analyzeSkillGap(
                request.getResumeText(),
                request.getJobDescription(),
                jobTitle
        );

        response.setAnalyzedAt(LocalDateTime.now());

        // Save to database
        try {
            AnalysisResult entity = new AnalysisResult();
            entity.setUser(user);
            entity.setJobTitle(jobTitle);
            entity.setJobDescription(request.getJobDescription());
            entity.setResumeText(request.getResumeText());
            entity.setMatchedSkills(String.join(",", response.getMatchedSkills()));
            entity.setMissingSkills(String.join(",", response.getMissingSkills()));
            entity.setMatchPercentage(response.getMatchPercentage());
            entity.setAiSummary(response.getAiSummary());
            entity.setLearningResources(objectMapper.writeValueAsString(response.getLearningResources()));

            AnalysisResult saved = analysisResultRepository.save(entity);
            response.setId(saved.getId());
        } catch (Exception e) {
            // Don't fail if saving fails
        }

        return response;
    }

    public List<HistoryItemResponse> getHistory(String userEmail) {
        User user = getUser(userEmail);
        return analysisResultRepository.findByUserOrderByAnalyzedAtDesc(user)
                .stream()
                .map(this::toHistoryItem)
                .collect(Collectors.toList());
    }

    public AnalysisResponse getById(Long id, String userEmail) {
        User user = getUser(userEmail);
        AnalysisResult result = analysisResultRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Analysis not found"));

        if (!result.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Not authorized");
        }

        return toFullResponse(result);
    }

    public void delete(Long id, String userEmail) {
        User user = getUser(userEmail);
        AnalysisResult result = analysisResultRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Analysis not found"));

        if (!result.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Not authorized");
        }

        analysisResultRepository.delete(result);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private HistoryItemResponse toHistoryItem(AnalysisResult r) {
        HistoryItemResponse item = new HistoryItemResponse();
        item.setId(r.getId());
        item.setJobTitle(r.getJobTitle());
        item.setMatchPercentage(r.getMatchPercentage());
        item.setAnalyzedAt(r.getAnalyzedAt());
        int missingCount = (r.getMissingSkills() == null || r.getMissingSkills().isBlank())
                ? 0 : r.getMissingSkills().split(",").length;
        item.setMissingSkillsCount(missingCount);
        return item;
    }

    private AnalysisResponse toFullResponse(AnalysisResult r) {
        AnalysisResponse response = new AnalysisResponse();
        response.setId(r.getId());
        response.setJobTitle(r.getJobTitle());
        response.setMatchPercentage(r.getMatchPercentage());
        response.setAiSummary(r.getAiSummary());
        response.setAnalyzedAt(r.getAnalyzedAt());

        response.setMatchedSkills(
            r.getMatchedSkills() == null || r.getMatchedSkills().isBlank()
                ? List.of() : Arrays.asList(r.getMatchedSkills().split(","))
        );
        response.setMissingSkills(
            r.getMissingSkills() == null || r.getMissingSkills().isBlank()
                ? List.of() : Arrays.asList(r.getMissingSkills().split(","))
        );

        try {
            List<LearningResource> resources = objectMapper.readValue(
                r.getLearningResources(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, LearningResource.class)
            );
            response.setLearningResources(resources);
        } catch (Exception e) {
            response.setLearningResources(List.of());
        }

        return response;
    }
}