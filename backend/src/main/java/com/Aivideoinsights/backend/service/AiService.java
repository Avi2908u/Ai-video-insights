package com.Aivideoinsights.backend.service;

import com.Aivideoinsights.backend.client.OpenRouterClient;

import org.springframework.stereotype.Service;

@Service
public class AiService {
    private final OpenRouterClient openRouterClient;

    public AiService(OpenRouterClient openRouterClient) {
        this.openRouterClient = openRouterClient;
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
        return openRouterClient.generateResponse(prompt);
    }

    public String generateKeyTopics(String transcript) {
        String prompt = """
                You are an expert technical content analyzer.
                
                                Analyze the transcript and identify major topic transitions.
                                
                                 Return ONLY valid JSON.
                
                                Instructions:
                                - Extract between 4 to 10 topics.
                                - Topics must represent actual concepts taught in the video.
                                - Keep each topic short and concise.
                                - Avoid generic terms.
                                - Avoid duplicates.
                                - Do not return explanations.
                
                                Example:
                
                                [
                                           {
                                             "timestamp":"00:00",
                                             "topic":"Introduction to Spring Boot"
                                           },
                                           {
                                             "timestamp":"03:15",
                                             "topic":"Why Spring Boot Was Created"
                                           }
                                         ]
                
                
                                Transcript:
                
                                %s
                """.formatted(transcript);
        return openRouterClient.generateResponse(prompt);
    }
    public String generateTimestampedNotes(String topic, String transcriptSection) {
        String prompt = """
                You are an expert software engineering educator.
                
                                  Create concise study notes for the given topic.
                
                                  Topic:
                                  %s
                
                                  Transcript:
                                  %s
                
                                  Return ONLY valid JSON.
                
                                  Required format:
                
                                  {
                                  "title": "Short topic title",
                                  "notes": "A concise explanation of the topic."
                                  }
                
                                  Rules:
                
                                  Return ONLY a JSON object.
                                  Do NOT wrap the response in ```json blocks.
                                  Do NOT include explanations before or after the JSON.
                                  Do NOT use markdown headings (#).
                                  Do NOT return arrays.
                                  Do NOT return nested objects.
                                  The notes field must be a single plain text string.
                                  Explain:
                                  What the concept is.
                                  Why it exists.
                                  Any important example from the transcript.
                                  Keep notes between 100 and 200 words.
                                  Use only information from the transcript.
                                  The response must be parseable by Jackson ObjectMapper.readTree().
                
                                  Example:
                
                                  {
                                  "title": "Spring Boot",
                                  "notes": "Spring Boot simplifies the development of Spring applications by reducing configuration requirements and providing sensible defaults. It was introduced to solve the complexity of traditional Spring setup. For example, developers can avoid manually configuring large numbers of classes and dependencies, allowing them to focus on business logic instead."
                                  }
                """.formatted(topic, transcriptSection);
        return openRouterClient.generateResponse(prompt);

    }
}


