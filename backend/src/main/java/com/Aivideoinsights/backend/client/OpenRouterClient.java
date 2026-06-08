package com.Aivideoinsights.backend.client;

import com.Aivideoinsights.backend.dto.openrouter.Message;
import com.Aivideoinsights.backend.dto.openrouter.OpenRouterRequest;
import com.Aivideoinsights.backend.dto.openrouter.OpenRouterResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class OpenRouterClient {
    private final RestTemplate restTemplate;

    @Value("${openrouter.api.key}")
    private String apiKey;
    @Value("${openrouter.url}")
    private String url;
    @Value("${openrouter.model}")
    private String model;

    public OpenRouterClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateSummary(String transcript) {
        String prompt = """
                        You are an expert content summarizer.
                
                                        Your task is to create a comprehensive summary of the following video transcript.
                
                                        Instructions:
                                        - Preserve the flow of the lecture.
                                        - Focus on the concepts being taught.
                                        - Explain the important ideas and their relationships.
                                        - Write in clear, professional language.
                                        - Do not use bullet points.
                                        - Do not mention phrases such as:
                                          "The video explains"
                                          "The speaker discusses"
                                          "This video talks about"
                                        - Write 200-400 words.
                                        - Only use information present in the transcript.
                                        - Do not invent facts.
                
                                        Transcript:
                
                                        %s        
                """.formatted(transcript);
        return callOpenRouter(prompt);
    }
    public String generateKeyTopics(String transcript) {
        String prompt = """
                You are an expert technical content analyzer.
                
                                Extract the most important learning concepts from the transcript.
                
                                Instructions:
                                - Return only a JSON array.
                                - Extract between 5 and 10 topics.
                                - Topics must represent actual concepts taught in the video.
                                - Keep each topic short and concise.
                                - Avoid generic terms.
                                - Avoid duplicates.
                                - Do not return explanations.
                
                                Example:
                
                                [
                                  "Spring Boot",
                                  "Embedded Tomcat",
                                  "Dependency Management"
                                ]
                
                                Transcript:
                
                                %s
                """.formatted(transcript);
        return callOpenRouter(prompt);
    }
    public String generateNotes(String transcript) {
        String prompt = """
                You are an expert software engineering instructor.
                
                                Create detailed study notes from the transcript.
                
                                Instructions:
                                - Use markdown formatting.
                                - Organize notes using headings and subheadings.
                                - Explain each important concept.
                                - Include definitions where appropriate.
                                - Include examples mentioned in the transcript.
                                - Include important revision points.
                                - Structure the notes for quick learning.
                                - Minimum 300 words.
                                - Do not mention the speaker.
                                - Use only information available in the transcript.
                
                                Transcript:
                
                                %s
                """.formatted(transcript);
        return callOpenRouter(prompt);
    }
    private String callOpenRouter(String prompt){

        OpenRouterRequest request = new OpenRouterRequest(model, List.of(new Message("user", prompt)));
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("HTTP-Referer", "http://localhost:8080");
        headers.set("X-Title", "AI Video Insights");
        HttpEntity<OpenRouterRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<OpenRouterResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, OpenRouterResponse.class);
        OpenRouterResponse body = response.getBody();
        if (body == null
                || body.choices() == null
                || body.choices().isEmpty()) {

            throw new RuntimeException(
                    "No response received from OpenRouter"
            );
        }
        return body.choices().get(0).message().content();
    }

}
