import cv2
import mediapipe as mp
import numpy as np
import time
from collections import deque
from virtual_robot import VirtualRobot

class SignLanguageSystem:
    def __init__(self):
        # MediaPipe Initialization
        self.mp_hands = mp.solutions.hands
        self.hands = self.mp_hands.Hands(
            static_image_mode=False,
            max_num_hands=1,
            min_detection_confidence=0.5,
            min_tracking_confidence=0.5
        )
        self.mp_drawing = mp.solutions.drawing_utils

        # Temporal Processing Configuration
        # ---------------------------------------------------------
        # We use a temporal window to track gestures over time, not just single frames.
        # This allows us to detect motion (like "HELLO") and stabilize static signs.
        self.buffer_size = 30  # Temporal window size (30 frames)
        self.landmark_buffer = deque(maxlen=self.buffer_size)
        self.prediction_buffer = deque(maxlen=15) # Majority Vote Buffer for stability
        # ---------------------------------------------------------
        
        # Recognition State
        self.detected_text = ""
        self.full_sentence = []
        self.confidence = 0.0
        self.top_predictions = []
        self.last_detection_time = time.time()
        self.detection_delay = 1.2
        
        # Virtual Robot Instance
        self.robot = VirtualRobot()

    def process_frame(self, frame):
        """Processes a single frame and updates the system state."""
        frame = cv2.flip(frame, 1)
        rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        results = self.hands.process(rgb_frame)
        
        current_sign = None
        
        if results.multi_hand_landmarks:
            self.robot.set_state("LISTENING")
            for hand_landmarks in results.multi_hand_landmarks:
                # 1. Extract Landmarks
                landmarks = self._extract_landmarks(hand_landmarks)
                self.landmark_buffer.append(landmarks)
                
                # 2. Draw Landmarks
                self.mp_drawing.draw_landmarks(frame, hand_landmarks, self.mp_hands.HAND_CONNECTIONS)
                
                # 3. Detect Sign (Temporal first, then Static)
                raw_sign = self._detect_temporal_sign()
                if not raw_sign:
                    raw_sign = self._detect_static_sign(landmarks)
                
                # 4. Majority Vote Logic (Temporal Stability)
                if raw_sign:
                    self.prediction_buffer.append(raw_sign)
                
                if len(self.prediction_buffer) >= 10:
                    # Get most frequent prediction in buffer
                    counts = {}
                    for p in self.prediction_buffer:
                        counts[p] = counts.get(p, 0) + 1
                    
                    sorted_preds = sorted(counts.items(), key=lambda x: x[1], reverse=True)
                    current_sign = sorted_preds[0][0]
                    self.top_predictions = [p[0] for p in sorted_preds[:2]]
                    
                    # 5. Update Sentence
                    if current_sign:
                        self._update_sentence(current_sign)
                
                # 6. Visual Feedback on Frame
                color = (0, 255, 0) if self.confidence > 0.8 else (0, 255, 255)
                display_text = f"{current_sign} ({int(self.confidence*100)}%)" if current_sign else "Scanning..."
                cv2.putText(frame, display_text, 
                            (int(hand_landmarks.landmark[0].x * frame.shape[1]), 
                             int(hand_landmarks.landmark[0].y * frame.shape[0]) - 20),
                            cv2.FONT_HERSHEY_SIMPLEX, 1, color, 2)
        else:
            self.robot.set_state("IDLE")
            self.landmark_buffer.clear()
            self.prediction_buffer.clear()
            self.confidence = 0.0

        return frame, current_sign

    def _extract_landmarks(self, hand_landmarks):
        return [(lm.x, lm.y, lm.z) for lm in hand_landmarks.landmark]

    def _distance(self, p1, p2):
        return np.sqrt((p1[0] - p2[0])**2 + (p1[1] - p2[1])**2)

    def _is_finger_extended(self, landmarks, finger_index):
        """
        finger_index: 1=Index, 2=Middle, 3=Ring, 4=Pinky
        Returns True if the finger is extended.
        """
        tip_ids = [8, 12, 16, 20]
        pip_ids = [6, 10, 14, 18]
        
        tip = landmarks[tip_ids[finger_index-1]]
        pip = landmarks[pip_ids[finger_index-1]]
        wrist = landmarks[0]
        
        return self._distance(tip, wrist) > self._distance(pip, wrist)

    def _detect_static_sign(self, landmarks):
        """High-Precision Geometric rules for A-Z."""
        # Finger States
        index_open = self._is_finger_extended(landmarks, 1)
        middle_open = self._is_finger_extended(landmarks, 2)
        ring_open = self._is_finger_extended(landmarks, 3)
        pinky_open = self._is_finger_extended(landmarks, 4)
        
        # Landmark shortcuts
        wrist = landmarks[0]
        thumb_tip = landmarks[4]
        index_tip = landmarks[8]
        middle_tip = landmarks[12]
        ring_tip = landmarks[16]
        pinky_tip = landmarks[20]
        
        # Distances
        d_thumb_index = self._distance(thumb_tip, index_tip)
        d_thumb_middle = self._distance(thumb_tip, middle_tip)
        d_thumb_ring = self._distance(thumb_tip, ring_tip)
        d_thumb_pinky = self._distance(thumb_tip, pinky_tip)

        # --- ALPHABET RULES ---

        # A: Fist, thumb to the side
        if not index_open and not middle_open and not ring_open and not pinky_open:
            if thumb_tip[1] < index_tip[1] and thumb_tip[0] > index_tip[0]:
                self.confidence = 0.95
                return 'A'

        # B: All fingers open and together
        if index_open and middle_open and ring_open and pinky_open:
            if self._distance(index_tip, middle_tip) < 0.05:
                self.confidence = 0.95
                return 'B'

        # C: Curved hand
        if not index_open and not middle_open and not ring_open and not pinky_open:
             if d_thumb_index > 0.08 and d_thumb_index < 0.18:
                 self.confidence = 0.80
                 return 'C'

        # D: Index open, others closed
        if index_open and not middle_open and not ring_open and not pinky_open:
            if d_thumb_middle < 0.1:
                self.confidence = 0.95
                return 'D'

        # E: All closed, thumb tucked in front
        if not index_open and not middle_open and not ring_open and not pinky_open:
            if thumb_tip[1] > middle_tip[1] and thumb_tip[0] < ring_tip[0]:
                self.confidence = 0.88
                return 'E'

        # F: Index and thumb touching, others open
        if not index_open and middle_open and ring_open and pinky_open:
            if d_thumb_index < 0.06:
                self.confidence = 0.92
                return 'F'

        # G: Index pointing sideways
        if index_open and not middle_open and not ring_open and not pinky_open:
            if abs(index_tip[1] - thumb_tip[1]) < 0.08:
                self.confidence = 0.85
                return 'G'

        # H: Index and middle pointing sideways
        if index_open and middle_open and not ring_open and not pinky_open:
            if abs(index_tip[1] - middle_tip[1]) < 0.08:
                self.confidence = 0.88
                return 'H'

        # I: Pinky open
        if pinky_open and not index_open and not middle_open and not ring_open:
            self.confidence = 0.95
            return 'I'

        # K: Index and middle open, thumb between
        if index_open and middle_open and not ring_open and not pinky_open:
            if d_thumb_middle < 0.06:
                self.confidence = 0.90
                return 'K'

        # L: Index and thumb open
        if index_open and not middle_open and not ring_open and not pinky_open:
            if thumb_tip[0] < index_tip[0] and thumb_tip[1] > index_tip[1]:
                self.confidence = 0.95
                return 'L'

        # M: Thumb under 3 fingers
        if not index_open and not middle_open and not ring_open and not pinky_open:
            if thumb_tip[0] < pinky_tip[0]:
                self.confidence = 0.75
                return 'M'

        # N: Thumb under 2 fingers
        if not index_open and not middle_open and not ring_open and not pinky_open:
            if thumb_tip[0] < ring_tip[0] and thumb_tip[0] > pinky_tip[0]:
                self.confidence = 0.75
                return 'N'

        # O: All fingers touching thumb
        if not index_open and not middle_open and not ring_open and not pinky_open:
            if d_thumb_index < 0.06 and d_thumb_pinky < 0.06:
                self.confidence = 0.88
                return 'O'

        # P: K shape pointing down
        if index_open and middle_open and not ring_open and not pinky_open:
            if index_tip[1] > wrist[1]:
                self.confidence = 0.85
                return 'P'

        # Q: G shape pointing down
        if index_open and not middle_open and not ring_open and not pinky_open:
            if index_tip[1] > wrist[1]:
                self.confidence = 0.85
                return 'Q'

        # R: Crossed fingers
        if index_open and middle_open and not ring_open and not pinky_open:
            if index_tip[0] > middle_tip[0]:
                self.confidence = 0.88
                return 'R'

        # S: Fist, thumb over
        if not index_open and not middle_open and not ring_open and not pinky_open:
            if thumb_tip[1] < middle_tip[1] and thumb_tip[0] < index_tip[0]:
                self.confidence = 0.85
                return 'S'

        # T: Thumb under index
        if not index_open and not middle_open and not ring_open and not pinky_open:
            if thumb_tip[0] < middle_tip[0] and thumb_tip[0] > index_tip[0]:
                self.confidence = 0.82
                return 'T'

        # U: Index and middle together
        if index_open and middle_open and not ring_open and not pinky_open:
            if self._distance(index_tip, middle_tip) < 0.04:
                self.confidence = 0.92
                return 'U'

        # V: Index and middle apart
        if index_open and middle_open and not ring_open and not pinky_open:
            if self._distance(index_tip, middle_tip) > 0.06:
                self.confidence = 0.95
                return 'V'

        # W: 3 fingers open
        if index_open and middle_open and ring_open and not pinky_open:
            self.confidence = 0.92
            return 'W'

        # X: Hooked index
        if not index_open and not middle_open and not ring_open and not pinky_open:
            if index_tip[1] < thumb_tip[1] and d_thumb_index > 0.06:
                self.confidence = 0.78
                return 'X'

        # Y: Thumb and pinky open
        if pinky_open and not index_open and not middle_open and not ring_open:
            if d_thumb_pinky > 0.18:
                self.confidence = 0.95
                return 'Y'

        # Z: Index pointing (Static fallback)
        if index_open and not middle_open and not ring_open and not pinky_open:
            self.confidence = 0.75
            return 'Z'

        # Numbers 0-5
        if not index_open and not middle_open and not ring_open and not pinky_open: return '0'
        if index_open and not middle_open and not ring_open and not pinky_open: return '1'
        if index_open and middle_open and not ring_open and not pinky_open: return '2'
        if index_open and middle_open and ring_open and not pinky_open: return '3'
        
        # Check for 5 (Open Palm -> HELLO) BEFORE 4 to avoid shadowing
        # User requested "means hello not 4"
        if index_open and middle_open and ring_open and pinky_open and d_thumb_index > 0.12: return 'HELLO'
        
        if index_open and middle_open and ring_open and pinky_open: return '4'
        
        # Space
        if index_open and middle_open and ring_open and pinky_open and d_thumb_index > 0.12:
            if self._distance(index_tip, middle_tip) > 0.06:
                self.confidence = 0.95
                return ' '

        return None

    def _detect_temporal_sign(self):
        """Detects words and motion-based letters (J, Z) over time."""
        if len(self.landmark_buffer) < 15:
            return None
        
        # 1. Detect "HELLO" (Wave)
        x_coords = [lm[8][0] for lm in self.landmark_buffer]
        x_diff = np.diff(x_coords)
        if np.sum(np.abs(x_diff)) > 0.3 and np.std(x_coords) > 0.08:
            self.confidence = 0.92
            self.robot_state = "SIGNING"
            return "HELLO"
            
        # 2. Detect "J" (Pinky drawing a hook)
        # Check pinky tip movement: down then curve left
        y_pinky = [lm[20][1] for lm in self.landmark_buffer]
        x_pinky = [lm[20][0] for lm in self.landmark_buffer]
        if y_pinky[0] < y_pinky[-1] - 0.05 and x_pinky[-1] < x_pinky[0] - 0.05:
            self.confidence = 0.85
            self.robot_state = "SIGNING"
            return "J"

        # 3. Detect "Z" (Index drawing a Z)
        # Check index tip movement: horizontal, diagonal, horizontal
        x_index = [lm[8][0] for lm in self.landmark_buffer]
        if np.std(x_index) > 0.1: # Significant horizontal movement
            self.confidence = 0.80
            self.robot_state = "SIGNING"
            return "Z"

        # 4. Detect "THANKS"
        y_coords = [lm[8][1] for lm in self.landmark_buffer]
        if y_coords[0] < y_coords[-1] - 0.1:
             self.confidence = 0.85
             self.robot_state = "SIGNING"
             return "THANKS"

        # 5. Detect "HELP"
        last_landmarks = self.landmark_buffer[-1]
        is_fist = not self._is_finger_extended(last_landmarks, 1) and not self._is_finger_extended(last_landmarks, 2)
        y_coords_fist = [lm[0][1] for lm in self.landmark_buffer]
        if is_fist and y_coords_fist[0] > y_coords_fist[-1] + 0.1:
            self.confidence = 0.88
            self.robot_state = "SIGNING"
            return "HELP"

        return None

    def _process_conversation(self, sign):
        """Checks for conversational keywords and triggers robot replies."""
        replies = {
            "HELLO": "Hi there! I'm SignTogether. How can I help?",
            "THANKS": "You're very welcome! Happy to help.",
            "HELP": "I can help you translate signs or learn new ones!",
            "YES": "Great! I'm glad we agree.",
            "NO": "Oh, I see. Let me know what you need.",
            "GOOD": "That's wonderful to hear!",
            "BAD": "I'm sorry to hear that. Hope it gets better."
        }
        
        if sign in replies:
            self.robot.set_state("REPLYING")
            # We override the default text with the specific reply
            self.robot.STATES["REPLYING"]["text"] = replies[sign]
            return True
        return False

    def _update_sentence(self, sign):
        now = time.time()
        if now - self.last_detection_time >= self.detection_delay:
            if self.confidence < 0.6:
                self.robot.set_state("CONFUSED")
                return

            # Check for conversation triggers FIRST
            is_conversation = self._process_conversation(sign)

            if sign == " ":
                if self.full_sentence and self.full_sentence[-1] != " ":
                    self.full_sentence.append(" ")
            else:
                self.full_sentence.append(sign + " ")
            
            self.last_detection_time = now
            self.detected_text = sign
            
            # Only set to THINKING if we didn't just trigger a REPLY
            if not is_conversation:
                self.robot.set_state("THINKING")

    def get_status(self):
        robot_data = self.robot.get_ui_data()
        return {
            "detected_text": self.detected_text,
            "full_sentence": "".join(self.full_sentence),
            "confidence": round(self.confidence * 100, 1),
            "top_predictions": self.top_predictions,
            "robot_state": robot_data["state_name"],
            "robot_icon": robot_data["icon"],
            "robot_image": robot_data["image"],
            "robot_message": robot_data["text"],
            "robot_color": robot_data["color"]
        }
