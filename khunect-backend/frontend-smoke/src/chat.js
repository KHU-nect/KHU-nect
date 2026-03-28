import { Client } from "@stomp/stompjs";
import { getAccessToken, getBaseUrl } from "./api";

function toWsUrl(httpBaseUrl) {
  const normalized = (httpBaseUrl || "").replace(/\/$/, "");
  if (normalized.startsWith("https://")) {
    return normalized.replace("https://", "wss://");
  }
  return normalized.replace("http://", "ws://");
}

export function createChatClient(type, roomId, onMessage, onStatus) {
  const base = toWsUrl(getBaseUrl());
  const topicPrefix = type === "course" ? "course-chat" : "direct-chat";
  const subscribeDest = `/sub/${topicPrefix}/rooms/${roomId}`;
  const publishDest = `/pub/${topicPrefix}/rooms/${roomId}/messages`;

  const client = new Client({
    brokerURL: `${base}/ws-stomp`,
    connectHeaders: getAccessToken() ? { Authorization: `Bearer ${getAccessToken()}` } : {},
    debug: () => {},
    reconnectDelay: 0
  });

  client.onConnect = () => {
    onStatus?.(`connected:${subscribeDest}`);
    client.subscribe(subscribeDest, (frame) => {
      try {
        onMessage?.(JSON.parse(frame.body));
      } catch {
        onMessage?.({ raw: frame.body });
      }
    });
  };

  client.onStompError = (frame) => onStatus?.(`stomp-error:${frame.headers.message || ""}`);
  client.onWebSocketError = (event) => onStatus?.(`ws-error:${event.type}`);
  client.onDisconnect = () => onStatus?.("disconnected");

  return {
    activate: () => client.activate(),
    deactivate: () => client.deactivate(),
    publish: (body) => {
      const token = getAccessToken();
      client.publish({
        destination: publishDest,
        headers: token ? { Authorization: `Bearer ${token}` } : {},
        body: JSON.stringify(body)
      });
    }
  };
}
