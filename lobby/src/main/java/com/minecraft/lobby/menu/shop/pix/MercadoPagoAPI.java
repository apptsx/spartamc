package com.minecraft.lobby.menu.shop.pix;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

public class MercadoPagoAPI {

    private static final String API_URL = "https://api.mercadopago.com/v1/payments";
    private static final String OAUTH_URL = "https://api.mercadopago.com/oauth/token";
    private static final String CLIENT_ID = System.getenv().getOrDefault("MERCADO_PAGO_CLIENT_ID", "");
    private static final String CLIENT_SECRET = System.getenv().getOrDefault("MERCADO_PAGO_CLIENT_SECRET", "");

    private static String accessToken = null;
    private static long tokenExpiry = 0;

    private static synchronized String getAccessToken() throws Exception {
        if (accessToken != null && System.currentTimeMillis() < tokenExpiry) {
            return accessToken;
        }

        String body = "grant_type=client_credentials"
                + "&client_id=" + CLIENT_ID
                + "&client_secret=" + CLIENT_SECRET;

        URL url = new URL(OAUTH_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
            out.write(body.getBytes(StandardCharsets.UTF_8));
            out.flush();
        }

        int code = conn.getResponseCode();
        BufferedReader reader;
        if (code >= 200 && code < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        if (code != 200) {
            throw new Exception("Erro ao obter token: " + code + " - " + response.toString());
        }

        String json = response.toString();

        accessToken = extractJsonValue(json, "\"access_token\":\"", "\"");
        int expiresIn = Integer.parseInt(extractJsonValue(json, "\"expires_in\":", ","));
        tokenExpiry = System.currentTimeMillis() + (expiresIn * 1000L) - 60000;

        return accessToken;
    }

    public static PixData createPixPayment(String playerName, String productName, double price, String orderId) throws Exception {
        String token = getAccessToken();

        String jsonBody = "{\n"
                + "  \"description\": \"" + productName + "\",\n"
                + "  \"payer\": {\n"
                + "    \"entity_type\": \"individual\",\n"
                + "    \"type\": \"customer\",\n"
                + "    \"email\": \"" + playerName + "@" + com.minecraft.core.Constant.SERVER_EMAIL_DOMAIN + "\"\n"
                + "  },\n"
                + "  \"external_reference\": \"" + orderId + "\",\n"
                + "  \"payment_method_id\": \"pix\",\n"
                + "  \"transaction_amount\": " + String.format(Locale.ROOT, "%.2f", price) + "\n"
                + "}";

        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("X-Idempotency-Key", UUID.randomUUID().toString());

        try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
            outputStream.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }

        int statusCode = connection.getResponseCode();

        BufferedReader reader;
        if (statusCode >= 200 && statusCode < 300) {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        if (statusCode != 201) {
            throw new Exception("Erro ao criar pagamento: " + statusCode + " - " + response.toString());
        }

        String jsonStr = response.toString();

        String paymentId = extractJsonValue(jsonStr, "\"id\":", ",");
        String qrCode = extractJsonValue(jsonStr, "\"qr_code\":\"", "\"");

        return new PixData(paymentId, qrCode);
    }

    public static boolean checkPayment(String paymentId) throws Exception {
        String token = getAccessToken();

        URL url = new URL(API_URL + "/" + paymentId);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + token);

        int statusCode = connection.getResponseCode();
        if (statusCode != 200) return false;

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        String status = extractJsonValue(response.toString(), "\"status\":\"", "\"");
        return status.equals("approved");
    }

    private static String extractJsonValue(String json, String key, String end) {
        int start = json.indexOf(key);
        if (start == -1) return "";
        start += key.length();
        int stop = json.indexOf(end, start);
        if (stop == -1) return json.substring(start);
        return json.substring(start, stop);
    }
}
