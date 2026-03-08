import time

class VirtualRobot:
    """
    The Virtual Interpreter Character.
    Manages states, emotions, and communication with the user.
    """
    STATES = {
        "IDLE": {"icon": "ri-robot-line", "image": "/static/asset/robot/idle.png", "text": "I'm ready when you are!", "color": "#94a3b8"},
        "LISTENING": {"icon": "ri-pulse-line", "image": "/static/asset/robot/listening.png", "text": "I see you! Keep signing...", "color": "#22c55e"},
        "THINKING": {"icon": "ri-brain-line", "image": "/static/asset/robot/thinking.png", "text": "Let me translate that...", "color": "#f59e0b"},
        "SIGNING": {"icon": "ri-hand-coin-line", "image": "/static/asset/robot/signing.png", "text": "I'm signing now!", "color": "#6366f1"},
        "CONFUSED": {"icon": "ri-question-line", "image": "/static/asset/robot/confused.png", "text": "I didn't quite get that. Could you repeat?", "color": "#f97316"},
        "EMERGENCY": {"icon": "ri-alarm-warning-line", "image": "/static/asset/robot/emergency.png", "text": "EMERGENCY MODE ACTIVE", "color": "#ef4444"},
        "REPLYING": {"icon": "ri-chat-smile-3-line", "image": "/static/asset/robot/listening.png", "text": "I'm replying...", "color": "#8b5cf6"}
    }

    def __init__(self):
        self.current_state = "IDLE"
        self.last_state_change = time.time()
        self.message_history = []

    def set_state(self, state):
        if state in self.STATES and state != self.current_state:
            self.current_state = state
            self.last_state_change = time.time()
            return True
        return False

    def get_ui_data(self):
        data = self.STATES[self.current_state].copy()
        data["state_name"] = self.current_state
        return data

    def get_response(self, confidence, detected_text):
        """Logic for smart responses based on confidence."""
        if confidence < 60:
            self.set_state("CONFUSED")
            return "Could you repeat that more clearly?"
        
        if detected_text:
            self.set_state("THINKING")
            return f"I think you signed: {detected_text}"
        
        return self.STATES[self.current_state]["text"]
