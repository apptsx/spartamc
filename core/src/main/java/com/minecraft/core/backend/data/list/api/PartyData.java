package com.minecraft.core.backend.data.list.api;

import com.minecraft.core.api.party.Party;
import com.minecraft.core.api.party.connection.PartyConnection;
import com.minecraft.core.api.party.request.PartyRequest;
import com.minecraft.core.backend.database.redis.RedisDatabase;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PartyData {

    private final RedisDatabase redis;

    private final String PARTY_KEY = "party:", // party:identifier
            PARTY_REQUEST_KEY = "party-request:", // party-request:sender:receiver
            PARTY_REQUEST_FORMAT = "party-request:%s:%s", // formato para criar chave específica
            PARTY_CONNECTION_KEY = "party-connection:"; // party-connection:userId

    public void save(Party party) {
        redis.save(PARTY_KEY + party.getIdentifier(), party);
    }

    public void delete(Party party) {
        redis.delete(PARTY_KEY + party.getIdentifier());
    }

    public void update(Party party) {
        redis.update(PARTY_KEY + party.getIdentifier(), party);
    }

    public Party of(UUID authorId) {
        return list(party -> party.getAuthorId().equals(authorId)).stream().findFirst().orElse(null);
    }

    public Party of(String identifier) {
        return redis.load(PARTY_KEY + identifier, Party.class);
    }

    public void clear() {
        redis.removeAll(PARTY_KEY);
        redis.removeAll(PARTY_REQUEST_KEY);
        redis.removeAll(PARTY_CONNECTION_KEY);
    }

    public void clearData(Party party) {
        listRequests(party).forEach(this::cancelRequest);
        listConnections(party).forEach(connection -> cancelConnection(connection.getUserId()));
    }

    public List<Party> list() {
        return redis.loadAll(PARTY_KEY, Party.class);
    }

    public List<Party> list(Predicate<Party> filter) {
        return list().stream().filter(filter).collect(Collectors.toList());
    }

    /* Party Request */
    public PartyRequest getRequest(UUID sender, UUID receiver) {
        return redis.load(String.format(PARTY_REQUEST_FORMAT, sender, receiver), PartyRequest.class);
    }

    public void request(PartyRequest request) {
        redis.save(String.format(PARTY_REQUEST_FORMAT, request.getSender(), request.getReceiver()), request, 60);
    }

    public void cancelRequest(PartyRequest request) {
        redis.delete(String.format(PARTY_REQUEST_FORMAT, request.getSender(), request.getReceiver()));
    }

    public boolean hasRequest(UUID sender, UUID receiver) {
        return getRequest(sender, receiver) != null;
    }

    public List<PartyRequest> listRequests(Party party) {
        return redis.loadAll(PARTY_REQUEST_KEY, PartyRequest.class).stream()
                .filter(request -> request != null && request.getPartyIdentifier() != null && 
                        request.getPartyIdentifier().equalsIgnoreCase(party.getIdentifier()))
                .collect(Collectors.toList());
    }

    public List<PartyRequest> listRequestsByReceiver(UUID receiver) {
        return redis.loadAll(PARTY_REQUEST_KEY, PartyRequest.class).stream()
                .filter(request -> request != null && request.getReceiver() != null && 
                        request.getReceiver().equals(receiver))
                .collect(Collectors.toList());
    }

    /* Party Reconnect */
    public PartyConnection getConnection(UUID userId) {
        return redis.load(PARTY_CONNECTION_KEY + userId, PartyConnection.class);
    }

    public void disconnect(PartyConnection connection) {
        redis.save(PARTY_CONNECTION_KEY + connection.getUserId(), connection, 60 * 3);
    }

    public void cancelConnection(UUID userId) {
        redis.delete(PARTY_CONNECTION_KEY + userId);
    }

    public boolean hasConnection(UUID userId) {
        return getConnection(userId) != null;
    }

    public List<PartyConnection> listConnections(Party party) {
        return redis.loadAll(PARTY_CONNECTION_KEY, PartyConnection.class).stream()
                .filter(connection -> connection.getPartyIdentifier().equalsIgnoreCase(party.getIdentifier()))
                .collect(Collectors.toList());
    }
}
