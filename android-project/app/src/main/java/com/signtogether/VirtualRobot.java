package com.signtogether;

import java.util.HashMap;
import java.util.Map;

public class VirtualRobot {
    private String currentState = "IDLE";
    private Map<String, Map<String, String>> states = new HashMap<>();
    private String replyText = "";

    public VirtualRobot() {
        // IDLE
        Map<String, String> idle = new HashMap<>();
        idle.put("text", "I'm ready to help! Start signing.");
        idle.put("color", "#94a3b8");
        idle.put("icon", "ri-robot-line");
        idle.put("image", "asset/robot_idle.gif"); // Path needs adjustment for Android assets
        states.put("IDLE", idle);

        // LISTENING
        Map<String, String> listening = new HashMap<>();
        listening.put("text", "Watching your hands...");
        listening.put("color", "#3b82f6");
        listening.put("icon", "ri-eye-line");
        listening.put("image", "asset/robot_listening.gif");
        states.put("LISTENING", listening);

        // THINKING
        Map<String, String> thinking = new HashMap<>();
        thinking.put("text", "Processing sign...");
        thinking.put("color", "#eab308");
        thinking.put("icon", "ri-brain-line");
        thinking.put("image", "asset/robot_thinking.gif");
        states.put("THINKING", thinking);

        // SIGNING
        Map<String, String> signing = new HashMap<>();
        signing.put("text", "I see movement!");
        signing.put("color", "#22c55e");
        signing.put("icon", "ri-hand-coin-line");
        signing.put("image", "asset/robot_signing.gif");
        states.put("SIGNING", signing);

        // CONFUSED
        Map<String, String> confused = new HashMap<>();
        confused.put("text", "I didn't catch that. Try again?");
        confused.put("color", "#ef4444");
        confused.put("icon", "ri-question-mark");
        confused.put("image", "asset/robot_confused.gif");
        states.put("CONFUSED", confused);

        // REPLYING
        Map<String, String> replying = new HashMap<>();
        replying.put("text", ""); // Dynamic
        replying.put("color", "#8b5cf6");
        replying.put("icon", "ri-chat-smile-line");
        replying.put("image", "asset/robot_happy.gif");
        states.put("REPLYING", replying);
    }

    public void setState(String newState) {
        if (states.containsKey(newState)) {
            this.currentState = newState;
        }
    }

    public void setReplyText(String text) {
        this.replyText = text;
    }

    public Map<String, String> getUiData() {
        Map<String, String> data = new HashMap<>(states.get(currentState));
        data.put("state_name", currentState);
        if (currentState.equals("REPLYING")) {
            data.put("text", replyText);
        }
        return data;
    }
}
