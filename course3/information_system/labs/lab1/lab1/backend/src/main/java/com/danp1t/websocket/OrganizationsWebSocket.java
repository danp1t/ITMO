package com.danp1t.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint("/websocket/organizations")
@ApplicationScoped
public class OrganizationsWebSocket {

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("WebSocket connected: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("WebSocket disconnected: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        sessions.remove(session);
        System.out.println("WebSocket error: " + throwable.getMessage());
    }


    public static void notifyTableUpdate() {
        synchronized (sessions) {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getAsyncRemote().sendText("refresh_table");
                    } catch (Exception e) {
                        System.err.println("Error sending refresh message: " + e.getMessage());
                    }
                }
            }
        }
    }
}