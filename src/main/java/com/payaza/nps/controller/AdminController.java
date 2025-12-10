package com.payaza.nps.controller;

import com.payaza.nps.annotation.Auditable;
import com.payaza.nps.dto.*;
import com.payaza.nps.model.AuditLog;
import com.payaza.nps.model.InternalClient;
import com.payaza.nps.repository.InternalClientRepository;
import com.payaza.nps.service.ApiKeyGenerationService;
import com.payaza.nps.service.AuditService;
import com.payaza.nps.service.PasswordService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Admin REST Controller for Internal Client Management
 * Provides CRUD operations for managing internal clients
 */
@RestController
@RequestMapping("/api/v1/admin/clients")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private InternalClientRepository clientRepository;

    @Autowired
    private ApiKeyGenerationService apiKeyGenerationService;

    @Autowired
    private AuditService auditService;
    
    @Autowired
    private PasswordService passwordService;

    /**
     * Create a new internal client
     */
    @PostMapping
    @Auditable(action = "CREATE_CLIENT", resource = "InternalClient", actionType = AuditLog.ActionType.CREATE, message = "Admin created new internal client")
    public ResponseEntity<?> createClient(@Valid @RequestBody CreateClientRequestDto request) {
        try {
            logger.info("Creating new client: {}", request.getClientId());

            // Check if client ID already exists
            if (clientRepository.existsByClientId(request.getClientId())) {
                auditService.logFailure("CREATE_CLIENT", "InternalClient", AuditLog.ActionType.CREATE, 
                                      "ADMIN", null, "Client ID already exists", "DUPLICATE_CLIENT_ID", request);
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Client ID '" + request.getClientId() + "' already exists"));
            }

            // Check if transaction prefix already exists
            if (clientRepository.existsByTransactionPrefix(request.getTransactionPrefix())) {
                auditService.logFailure("CREATE_CLIENT", "InternalClient", AuditLog.ActionType.CREATE, 
                                      "ADMIN", null, "Transaction prefix already exists", "DUPLICATE_PREFIX", request);
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Transaction prefix '" + request.getTransactionPrefix() + "' already exists"));
            }
            
            // Check if email already exists
            if (clientRepository.existsByEmail(request.getEmail())) {
                auditService.logFailure("CREATE_CLIENT", "InternalClient", AuditLog.ActionType.CREATE, 
                                      "ADMIN", null, "Email already exists", "DUPLICATE_EMAIL", request);
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Email '" + request.getEmail() + "' already exists"));
            }

            // Generate API key
            String apiKey = apiKeyGenerationService.generateClientApiKey(request.getClientId());

            // Check if generated API key already exists (very unlikely but safe)
            while (clientRepository.existsByApiKey(apiKey)) {
                apiKey = apiKeyGenerationService.generateClientApiKey(request.getClientId());
            }

            // Validate password strength
            if (!passwordService.isPasswordStrong(request.getPassword())) {
                auditService.logFailure("CREATE_CLIENT", "InternalClient", AuditLog.ActionType.CREATE, 
                                      "ADMIN", null, "Password does not meet strength requirements", "WEAK_PASSWORD", request);
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Password does not meet strength requirements"));
            }
            
            // Create new client
            InternalClient client = new InternalClient();
            client.setClientId(request.getClientId().toUpperCase());
            client.setClientName(request.getClientName());
            client.setEmail(request.getEmail().toLowerCase());
            client.setPassword(passwordService.encodePassword(request.getPassword()));
            client.setApiKey(apiKey);
            client.setTransactionPrefix(request.getTransactionPrefix().toUpperCase());
            client.setAllowedEndpoints(request.getAllowedEndpoints());
            client.setActive(request.getActive());
            client.setRateLimitPerMinute(request.getRateLimitPerMinute());
            client.setDescription(request.getDescription());
            client.setContactEmail(request.getContactEmail());
            client.setContactPhone(request.getContactPhone());
            client.setCreatedBy("ADMIN"); // TODO: Get from security context
            client.setUpdatedBy("ADMIN");

            // Save client
            InternalClient savedClient = clientRepository.save(client);
            logger.info("Client created successfully: {} with API key: {}", savedClient.getClientId(), savedClient.getApiKey());

            // Log successful creation
            auditService.logAdminAction("CREATE_CLIENT", "InternalClient", "ADMIN", 
                                      AuditLog.ActionType.CREATE, "Client created successfully", 
                                      Map.of("clientId", savedClient.getClientId(), "clientName", savedClient.getClientName()));

            // Return client info with API key (one-time display)
            ApiKeyResponseDto response = new ApiKeyResponseDto(
                savedClient.getClientId(),
                savedClient.getClientName(),
                savedClient.getApiKey()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error creating client: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Failed to create client: " + e.getMessage()));
        }
    }

    /**
     * Get all clients with pagination
     */
    @GetMapping
    public ResponseEntity<Page<ClientResponseDto>> getAllClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<InternalClient> clients = clientRepository.findAll(pageable);
            Page<ClientResponseDto> response = clients.map(ClientResponseDto::new);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching clients: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get client by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getClientById(@PathVariable Long id) {
        try {
            Optional<InternalClient> client = clientRepository.findById(id);
            
            if (client.isPresent()) {
                return ResponseEntity.ok(new ClientResponseDto(client.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error fetching client by ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Failed to fetch client: " + e.getMessage()));
        }
    }

    /**
     * Get client by client ID
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getClientByClientId(@PathVariable String clientId) {
        try {
            Optional<InternalClient> client = clientRepository.findByClientId(clientId.toUpperCase());
            
            if (client.isPresent()) {
                return ResponseEntity.ok(new ClientResponseDto(client.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error fetching client by client ID {}: {}", clientId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Failed to fetch client: " + e.getMessage()));
        }
    }

    /**
     * Update client
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateClient(@PathVariable Long id, @Valid @RequestBody CreateClientRequestDto request) {
        try {
            Optional<InternalClient> existingClient = clientRepository.findById(id);
            
            if (!existingClient.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            InternalClient client = existingClient.get();

            // Check if client ID is being changed and if new one exists
            if (!client.getClientId().equals(request.getClientId().toUpperCase()) && 
                clientRepository.existsByClientId(request.getClientId())) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Client ID '" + request.getClientId() + "' already exists"));
            }

            // Check if transaction prefix is being changed and if new one exists
            if (!client.getTransactionPrefix().equals(request.getTransactionPrefix().toUpperCase()) && 
                clientRepository.existsByTransactionPrefix(request.getTransactionPrefix())) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Transaction prefix '" + request.getTransactionPrefix() + "' already exists"));
            }

            // Update client fields
            client.setClientId(request.getClientId().toUpperCase());
            client.setClientName(request.getClientName());
            client.setTransactionPrefix(request.getTransactionPrefix().toUpperCase());
            client.setAllowedEndpoints(request.getAllowedEndpoints());
            client.setActive(request.getActive());
            client.setRateLimitPerMinute(request.getRateLimitPerMinute());
            client.setDescription(request.getDescription());
            client.setContactEmail(request.getContactEmail());
            client.setContactPhone(request.getContactPhone());
            client.setUpdatedBy("ADMIN"); // TODO: Get from security context

            InternalClient updatedClient = clientRepository.save(client);
            logger.info("Client updated successfully: {}", updatedClient.getClientId());

            return ResponseEntity.ok(new ClientResponseDto(updatedClient));

        } catch (Exception e) {
            logger.error("Error updating client: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Failed to update client: " + e.getMessage()));
        }
    }

    /**
     * Delete client
     */
    @DeleteMapping("/{id}")
    @Auditable(action = "DELETE_CLIENT", resource = "InternalClient", actionType = AuditLog.ActionType.DELETE, message = "Admin deleted client")
    public ResponseEntity<?> deleteClient(@PathVariable Long id) {
        try {
            Optional<InternalClient> client = clientRepository.findById(id);
            
            if (!client.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            clientRepository.deleteById(id);
            logger.info("Client deleted successfully: {}", client.get().getClientId());

            return ResponseEntity.ok(new SuccessResponse("Client deleted successfully"));

        } catch (Exception e) {
            logger.error("Error deleting client: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Failed to delete client: " + e.getMessage()));
        }
    }

    /**
     * Regenerate API key for client
     */
    @PostMapping("/{id}/regenerate-api-key")
    @Auditable(action = "REGENERATE_API_KEY", resource = "InternalClient", actionType = AuditLog.ActionType.UPDATE, message = "Admin regenerated API key for client")
    public ResponseEntity<?> regenerateApiKey(@PathVariable Long id) {
        try {
            Optional<InternalClient> client = clientRepository.findById(id);
            
            if (!client.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            // Generate new API key
            String newApiKey = apiKeyGenerationService.generateClientApiKey(client.get().getClientId());
            
            // Ensure uniqueness
            while (clientRepository.existsByApiKey(newApiKey)) {
                newApiKey = apiKeyGenerationService.generateClientApiKey(client.get().getClientId());
            }

            // Update client with new API key
            client.get().setApiKey(newApiKey);
            client.get().setUpdatedBy("ADMIN"); // TODO: Get from security context
            clientRepository.save(client.get());

            logger.info("API key regenerated for client: {}", client.get().getClientId());

            // Return new API key (one-time display)
            ApiKeyResponseDto response = new ApiKeyResponseDto(
                client.get().getClientId(),
                client.get().getClientName(),
                newApiKey
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error regenerating API key: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Failed to regenerate API key: " + e.getMessage()));
        }
    }

    /**
     * Toggle client active status
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleClientStatus(@PathVariable Long id) {
        try {
            Optional<InternalClient> client = clientRepository.findById(id);
            
            if (!client.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            client.get().setActive(!client.get().isActive());
            client.get().setUpdatedBy("ADMIN"); // TODO: Get from security context
            InternalClient updatedClient = clientRepository.save(client.get());

            logger.info("Client status toggled for: {} to {}", updatedClient.getClientId(), updatedClient.isActive());

            return ResponseEntity.ok(new ClientResponseDto(updatedClient));

        } catch (Exception e) {
            logger.error("Error toggling client status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Failed to toggle client status: " + e.getMessage()));
        }
    }

    /**
     * Get client statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getClientStatistics() {
        try {
            long totalClients = clientRepository.count();
            long activeClients = clientRepository.countActiveClients();
            long inactiveClients = totalClients - activeClients;

            ClientStatisticsDto stats = new ClientStatisticsDto(totalClients, activeClients, inactiveClients);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.error("Error fetching client statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Failed to fetch statistics: " + e.getMessage()));
        }
    }

    /**
     * Error response DTO
     */
    public static class ErrorResponse {
        private String error;
        private String message;
        private String timestamp;

        public ErrorResponse(String message) {
            this.error = "CLIENT_ERROR";
            this.message = message;
            this.timestamp = LocalDateTime.now().toString();
        }

        // Getters
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getTimestamp() { return timestamp; }
    }

    /**
     * Success response DTO
     */
    public static class SuccessResponse {
        private String message;
        private String timestamp;

        public SuccessResponse(String message) {
            this.message = message;
            this.timestamp = LocalDateTime.now().toString();
        }

        // Getters
        public String getMessage() { return message; }
        public String getTimestamp() { return timestamp; }
    }

    /**
     * Client statistics DTO
     */
    public static class ClientStatisticsDto {
        private long totalClients;
        private long activeClients;
        private long inactiveClients;

        public ClientStatisticsDto(long totalClients, long activeClients, long inactiveClients) {
            this.totalClients = totalClients;
            this.activeClients = activeClients;
            this.inactiveClients = inactiveClients;
        }

        // Getters
        public long getTotalClients() { return totalClients; }
        public long getActiveClients() { return activeClients; }
        public long getInactiveClients() { return inactiveClients; }
    }
}
