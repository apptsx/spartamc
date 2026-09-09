package com.minecraft.core.account.validation;

import com.google.gson.JsonObject;
import com.minecraft.core.Core;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class ValidationWebServer {

    private HttpServer server;
    private final int port;

    public ValidationWebServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            
            // Endpoint para validação
            server.createContext("/api/validate", this::handleValidation);
            
            // Endpoint para verificar status
            server.createContext("/api/status", this::handleStatus);
            
            server.setExecutor(Executors.newFixedThreadPool(4));
            server.start();
            
            Core.getLogger().info("[ValidationServer] Servidor web iniciado na porta " + port);
        } catch (IOException e) {
            Core.getLogger().severe("[ValidationServer] Erro ao iniciar servidor web: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            Core.getLogger().info("[ValidationServer] Servidor web parado.");
        }
    }

    private void handleValidation(HttpExchange exchange) throws IOException {
        // Permitir CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, createErrorResponse("Método não permitido"));
            return;
        }

        try {
            // Ler corpo da requisição
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = parseFormData(body);

            String nickname = params.get("nickname");
            String code = params.get("code");

            if (nickname == null || nickname.isEmpty() || code == null || code.isEmpty()) {
                sendResponse(exchange, 400, createErrorResponse("Nickname e código são obrigatórios"));
                return;
            }

            // Validar código
            boolean success = ValidationManager.validateCode(nickname, code);

            if (success) {
                Core.getLogger().info("[ValidationServer] Código validado com sucesso para: " + nickname);
                sendResponse(exchange, 200, createSuccessResponse("Validação realizada com sucesso! Você já pode entrar no servidor."));
            } else {
                sendResponse(exchange, 400, createErrorResponse("Código inválido, expirado ou nickname incorreto"));
            }
        } catch (Exception e) {
            Core.getLogger().severe("[ValidationServer] Erro ao processar validação: " + e.getMessage());
            sendResponse(exchange, 500, createErrorResponse("Erro interno do servidor"));
        }
    }

    private void handleStatus(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        
        JsonObject response = new JsonObject();
        response.addProperty("status", "online");
        response.addProperty("pendingValidations", ValidationManager.getPendingCount());
        
        sendResponse(exchange, 200, response.toString());
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String createSuccessResponse(String message) {
        JsonObject json = new JsonObject();
        json.addProperty("success", true);
        json.addProperty("message", message);
        return json.toString();
    }

    private String createErrorResponse(String message) {
        JsonObject json = new JsonObject();
        json.addProperty("success", false);
        json.addProperty("error", message);
        return json.toString();
    }

    private Map<String, String> parseFormData(String formData) {
        Map<String, String> params = new HashMap<>();
        
        if (formData == null || formData.isEmpty()) {
            return params;
        }

        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                params.put(keyValue[0], java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
            }
        }
        
        return params;
    }
}
