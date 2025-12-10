package com.payaza.nps.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Log Aggregation Service for log collection and search
 */
@Service
public class LogAggregationService {
    
    private static final Logger logger = LoggerFactory.getLogger(LogAggregationService.class);
    
    // In-memory log storage for demo purposes (in production, use Elasticsearch or similar)
    private final List<Map<String, Object>> logEntries = Collections.synchronizedList(new ArrayList<>());
    
    /**
     * Get system logs with filtering and pagination
     */
    public List<Map<String, Object>> getSystemLogs(int page, int size, String level, 
                                                  String source, LocalDateTime from, 
                                                  LocalDateTime to, String search) {
        
        // Generate mock log entries if empty
        if (logEntries.isEmpty()) {
            generateMockLogEntries();
        }
        
        List<Map<String, Object>> filteredLogs = logEntries.stream()
            .filter(log -> level == null || level.equals(log.get("level")))
            .filter(log -> source == null || source.equals(log.get("source")))
            .filter(log -> from == null || ((LocalDateTime) log.get("timestamp")).isAfter(from))
            .filter(log -> to == null || ((LocalDateTime) log.get("timestamp")).isBefore(to))
            .filter(log -> search == null || log.get("message").toString().toLowerCase().contains(search.toLowerCase()))
            .sorted((a, b) -> ((LocalDateTime) b.get("timestamp")).compareTo((LocalDateTime) a.get("timestamp")))
            .collect(Collectors.toList());
        
        // Apply pagination
        int start = page * size;
        int end = Math.min(start + size, filteredLogs.size());
        
        return filteredLogs.subList(start, end);
    }
    
    /**
     * Search logs with query
     */
    public List<Map<String, Object>> searchLogs(String query, int page, int size, 
                                               LocalDateTime from, LocalDateTime to) {
        
        // Generate mock log entries if empty
        if (logEntries.isEmpty()) {
            generateMockLogEntries();
        }
        
        List<Map<String, Object>> searchResults = logEntries.stream()
            .filter(log -> log.get("message").toString().toLowerCase().contains(query.toLowerCase()) ||
                          log.get("source").toString().toLowerCase().contains(query.toLowerCase()) ||
                          log.get("level").toString().toLowerCase().contains(query.toLowerCase()))
            .filter(log -> from == null || ((LocalDateTime) log.get("timestamp")).isAfter(from))
            .filter(log -> to == null || ((LocalDateTime) log.get("timestamp")).isBefore(to))
            .sorted((a, b) -> ((LocalDateTime) b.get("timestamp")).compareTo((LocalDateTime) a.get("timestamp")))
            .collect(Collectors.toList());
        
        // Apply pagination
        int start = page * size;
        int end = Math.min(start + size, searchResults.size());
        
        return searchResults.subList(start, end);
    }
    
    /**
     * Get log statistics
     */
    public Map<String, Object> getLogStatistics(LocalDateTime from, LocalDateTime to) {
        try {
            // Generate mock log entries if empty
            if (logEntries.isEmpty()) {
                generateMockLogEntries();
            }
            
            List<Map<String, Object>> filteredLogs = logEntries.stream()
                .filter(log -> from == null || ((LocalDateTime) log.get("timestamp")).isAfter(from))
                .filter(log -> to == null || ((LocalDateTime) log.get("timestamp")).isBefore(to))
                .collect(Collectors.toList());
            
            Map<String, Object> stats = new HashMap<>();
            
            // Total logs
            stats.put("totalLogs", filteredLogs.size());
            
            // Logs by level
            Map<String, Long> logsByLevel = filteredLogs.stream()
                .collect(Collectors.groupingBy(
                    log -> log.get("level").toString(),
                    Collectors.counting()
                ));
            stats.put("logsByLevel", logsByLevel);
            
            // Logs by source
            Map<String, Long> logsBySource = filteredLogs.stream()
                .collect(Collectors.groupingBy(
                    log -> log.get("source").toString(),
                    Collectors.counting()
                ));
            stats.put("logsBySource", logsBySource);
            
            // Error rate
            long errorCount = filteredLogs.stream()
                .filter(log -> "ERROR".equals(log.get("level")))
                .count();
            double errorRate = filteredLogs.isEmpty() ? 0.0 : (double) errorCount / filteredLogs.size() * 100;
            stats.put("errorRate", errorRate);
            
            // Time range
            Map<String, Object> timeRange = new HashMap<>();
            timeRange.put("from", from);
            timeRange.put("to", to);
            stats.put("timeRange", timeRange);
            stats.put("generatedAt", LocalDateTime.now());
            
            return stats;
        } catch (Exception e) {
            logger.error("Error getting log statistics: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get log statistics", e);
        }
    }
    
    /**
     * Export logs in various formats
     */
    public byte[] exportLogs(String format, LocalDateTime from, LocalDateTime to, 
                           String level, String source) {
        
        List<Map<String, Object>> filteredLogs = getSystemLogs(0, Integer.MAX_VALUE, 
                                                              level, source, from, to, null);
        
        switch (format.toLowerCase()) {
            case "csv":
                return exportToCsv(filteredLogs);
            case "json":
                return exportToJson(filteredLogs);
            case "txt":
                return exportToText(filteredLogs);
            default:
                throw new IllegalArgumentException("Unsupported export format: " + format);
        }
    }
    
    /**
     * Generate mock log entries for demonstration
     */
    private void generateMockLogEntries() {
        String[] levels = {"INFO", "WARN", "ERROR", "DEBUG"};
        String[] sources = {"AuthService", "PaymentService", "AlertService", "SystemController", "Database"};
        String[] messages = {
            "User authentication successful",
            "Payment processed successfully",
            "Alert triggered for high memory usage",
            "Database connection established",
            "API request received",
            "Response sent to client",
            "Error processing payment request",
            "System health check completed",
            "Webhook delivery failed",
            "Rate limit exceeded for client"
        };
        
        for (int i = 0; i < 100; i++) {
            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("id", "LOG" + String.format("%06d", i + 1));
            logEntry.put("timestamp", LocalDateTime.now().minusMinutes(i * 2));
            logEntry.put("level", levels[i % levels.length]);
            logEntry.put("source", sources[i % sources.length]);
            logEntry.put("message", messages[i % messages.length]);
            logEntry.put("thread", "http-nio-8080-exec-" + (i % 10 + 1));
            logEntry.put("logger", "com.payaza.nps." + sources[i % sources.length]);
            
            // Add some additional context
            if ("ERROR".equals(logEntry.get("level"))) {
                logEntry.put("exception", "java.lang.RuntimeException: Mock error for demonstration");
                logEntry.put("stackTrace", "at com.payaza.nps.service.MockService.method(MockService.java:123)");
            }
            
            logEntries.add(logEntry);
        }
    }
    
    /**
     * Export logs to CSV format
     */
    private byte[] exportToCsv(List<Map<String, Object>> logs) {
        StringBuilder csv = new StringBuilder();
        csv.append("Timestamp,Level,Source,Message,Thread,Logger\n");
        
        for (Map<String, Object> log : logs) {
            csv.append(log.get("timestamp")).append(",")
               .append(log.get("level")).append(",")
               .append(log.get("source")).append(",")
               .append("\"").append(log.get("message")).append("\",")
               .append(log.get("thread")).append(",")
               .append(log.get("logger")).append("\n");
        }
        
        return csv.toString().getBytes();
    }
    
    /**
     * Export logs to JSON format
     */
    private byte[] exportToJson(List<Map<String, Object>> logs) {
        StringBuilder json = new StringBuilder();
        json.append("{\n  \"logs\": [\n");
        
        for (int i = 0; i < logs.size(); i++) {
            Map<String, Object> log = logs.get(i);
            json.append("    {\n");
            json.append("      \"id\": \"").append(log.get("id")).append("\",\n");
            json.append("      \"timestamp\": \"").append(log.get("timestamp")).append("\",\n");
            json.append("      \"level\": \"").append(log.get("level")).append("\",\n");
            json.append("      \"source\": \"").append(log.get("source")).append("\",\n");
            json.append("      \"message\": \"").append(log.get("message")).append("\",\n");
            json.append("      \"thread\": \"").append(log.get("thread")).append("\",\n");
            json.append("      \"logger\": \"").append(log.get("logger")).append("\"\n");
            json.append("    }");
            
            if (i < logs.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("  ]\n}");
        
        return json.toString().getBytes();
    }
    
    /**
     * Export logs to text format
     */
    private byte[] exportToText(List<Map<String, Object>> logs) {
        StringBuilder text = new StringBuilder();
        
        for (Map<String, Object> log : logs) {
            text.append(log.get("timestamp"))
                .append(" [").append(log.get("level")).append("] ")
                .append(log.get("source")).append(" - ")
                .append(log.get("message")).append("\n");
        }
        
        return text.toString().getBytes();
    }
}
