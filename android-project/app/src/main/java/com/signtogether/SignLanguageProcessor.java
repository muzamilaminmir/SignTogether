package com.signtogether;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SignLanguageProcessor {

    private static final int BUFFER_SIZE = 30;
    private LinkedList<List<Landmark>> landmarkBuffer = new LinkedList<>();
    private LinkedList<String> predictionBuffer = new LinkedList<>();

    private String detectedText = "";
    private StringBuilder fullSentence = new StringBuilder();

    public String getFullSentence() {
        return fullSentence.toString();
    }

    private float confidence = 0.0f;
    private List<String> topPredictions = new ArrayList<>();
    private long lastDetectionTime = 0;
    private static final long DETECTION_DELAY_MS = 1200;

    public VirtualRobot robot = new VirtualRobot();

    public static class Landmark {
        float x, y, z;

        public Landmark(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    // --- SMART DETECTION STATE ---
    private String lastStableSign = "";
    private long lastSignTimestamp = 0;
    private static final long SENTENCE_PAUSE_THRESHOLD = 1500; // 1.5 seconds pause -> New Sentence
    private List<Landmark> previousFrameLandmarks = null;

    public ProcessResult process(List<Landmark> landmarks) {
        long now = System.currentTimeMillis();

        // 1. Check for Sentence Completion (Pause or Neutral)
        if (landmarks == null || landmarks.isEmpty() || isNeutralPosition(landmarks)) {
            robot.setState("IDLE");
            landmarkBuffer.clear();
            predictionBuffer.clear();

            // If enough time passed since last sign, finalize sentence
            if (fullSentence.length() > 0 && (now - lastSignTimestamp > SENTENCE_PAUSE_THRESHOLD)) {
                String completed = fullSentence.toString().trim();
                fullSentence.setLength(0); // Clear buffer
                lastStableSign = ""; // Reset sign state
                return new ProcessResult(null, 0.0f, robot.getUiData(), true, completed);
            }

            // Reset last sign if neutral for a bit (allows repeating same sign after rest)
            if (now - lastSignTimestamp > 500) {
                lastStableSign = "";
            }

            return new ProcessResult(null, 0.0f, robot.getUiData(), false, null);
        }

        // 2. Calculate Velocity (to detect holding vs moving)
        float velocity = calculateVelocity(landmarks);
        previousFrameLandmarks = new ArrayList<>(landmarks);

        robot.setState("LISTENING");
        landmarkBuffer.add(landmarks);
        if (landmarkBuffer.size() > BUFFER_SIZE) {
            landmarkBuffer.removeFirst();
        }

        String rawSign = detectTemporalSign();
        if (rawSign == null) {
            rawSign = detectStaticSign(landmarks);
        }

        if (rawSign != null) {
            predictionBuffer.add(rawSign);
            if (predictionBuffer.size() > 15) {
                predictionBuffer.removeFirst();
            }
        }

        String currentSign = null;
        if (predictionBuffer.size() >= 5) { // Faster reaction
            String potentialSign = getMostFrequentPrediction();

            // Smart De-duplication Logic
            if (potentialSign != null) {
                boolean isNewSign = !potentialSign.equals(lastStableSign);
                boolean isSignificantChange = velocity > 0.15f; // Hand moved significantly
                boolean timeElapsed = (now - lastSignTimestamp > 1000); // Forced unlock after 1s if moving

                if (isNewSign || (isSignificantChange && timeElapsed)) {
                    // It's a valid new/repeated sign
                    currentSign = potentialSign;
                    updateSentence(currentSign);
                    lastStableSign = currentSign;
                    lastSignTimestamp = now;
                }
            }
        }

        return new ProcessResult(currentSign, confidence, robot.getUiData(), false, null);
    }

    private boolean isNeutralPosition(List<Landmark> landmarks) {
        if (landmarks == null || landmarks.isEmpty())
            return true;
        // Check if wrist is very low (y > 0.9) or hands are "dropped"
        Landmark wrist = landmarks.get(0);
        return wrist.y > 0.95;
    }

    private float calculateVelocity(List<Landmark> current) {
        if (previousFrameLandmarks == null || previousFrameLandmarks.size() != current.size())
            return 1.0f;

        float distSum = 0;
        // Check wrist and finger tips
        int[] points = { 0, 8, 12, 16, 20 };
        for (int i : points) {
            distSum += distance(current.get(i), previousFrameLandmarks.get(i));
        }
        return distSum / points.length;
    }

    private String getMostFrequentPrediction() {
        Map<String, Integer> counts = new HashMap<>();
        for (String s : predictionBuffer) {
            counts.put(s, counts.getOrDefault(s, 0) + 1);
        }

        if (counts.isEmpty())
            return null;

        List<Map.Entry<String, Integer>> list = new ArrayList<>(counts.entrySet());
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        // Threshold: Need at least 60% agreement in buffer
        if (list.get(0).getValue() < predictionBuffer.size() * 0.6)
            return null;

        return list.get(0).getKey();
    }

    private void updateSentence(String sign) {
        if (confidence < 0.6f) {
            robot.setState("CONFUSED");
            return;
        }

        boolean isConversation = processConversation(sign);

        if (sign.equals(" ")) {
            if (fullSentence.length() > 0 && fullSentence.charAt(fullSentence.length() - 1) != ' ') {
                fullSentence.append(" ");
            }
        } else {
            if (fullSentence.length() > 0 && fullSentence.charAt(fullSentence.length() - 1) != ' ') {
                fullSentence.append(" ");
            }
            fullSentence.append(sign);
        }

        detectedText = sign;

        if (!isConversation) {
            robot.setState("THINKING");
        }
    }

    private boolean processConversation(String sign) {
        Map<String, String> replies = new HashMap<>();
        replies.put("HELLO", "Hi there! I'm SignTogether. How can I help?");
        replies.put("THANKS", "You're very welcome! Happy to help.");
        replies.put("HELP", "I can help you translate signs or learn new ones!");
        replies.put("YES", "Great! I'm glad we agree.");
        replies.put("NO", "Oh, I see. Let me know what you need.");
        replies.put("GOOD", "That's wonderful to hear!");
        replies.put("BAD", "I'm sorry to hear that. Hope it gets better.");

        if (replies.containsKey(sign)) {
            robot.setState("REPLYING");
            robot.setReplyText(replies.get(sign));
            return true;
        }
        return false;
    }

    // --- GEOMETRIC HELPERS ---

    private float distance(Landmark p1, Landmark p2) {
        return (float) Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    private boolean isFingerExtended(List<Landmark> landmarks, int fingerIndex) {
        // 1=Index, 2=Middle, 3=Ring, 4=Pinky
        int[] tipIds = { 8, 12, 16, 20 };
        int[] pipIds = { 6, 10, 14, 18 };

        Landmark tip = landmarks.get(tipIds[fingerIndex - 1]);
        Landmark pip = landmarks.get(pipIds[fingerIndex - 1]);
        Landmark wrist = landmarks.get(0);

        return distance(tip, wrist) > distance(pip, wrist);
    }

    // --- DETECTION LOGIC ---

    private String detectStaticSign(List<Landmark> landmarks) {
        boolean indexOpen = isFingerExtended(landmarks, 1);
        boolean middleOpen = isFingerExtended(landmarks, 2);
        boolean ringOpen = isFingerExtended(landmarks, 3);
        boolean pinkyOpen = isFingerExtended(landmarks, 4);

        Landmark wrist = landmarks.get(0);
        Landmark thumbTip = landmarks.get(4);
        Landmark indexTip = landmarks.get(8);
        Landmark middleTip = landmarks.get(12);
        Landmark ringTip = landmarks.get(16);
        Landmark pinkyTip = landmarks.get(20);

        float dThumbIndex = distance(thumbTip, indexTip);
        float dThumbMiddle = distance(thumbTip, middleTip);
        float dThumbPinky = distance(thumbTip, pinkyTip);

        // A: Fist, thumb to the side
        if (!indexOpen && !middleOpen && !ringOpen && !pinkyOpen) {
            if (thumbTip.y < indexTip.y && thumbTip.x > indexTip.x) {
                confidence = 0.95f;
                return "A";
            }
        }

        // B: All fingers open and together
        if (indexOpen && middleOpen && ringOpen && pinkyOpen) {
            if (distance(indexTip, middleTip) < 0.05) {
                confidence = 0.95f;
                return "B";
            }
        }

        // C: Curved hand
        if (!indexOpen && !middleOpen && !ringOpen && !pinkyOpen) {
            if (dThumbIndex > 0.08 && dThumbIndex < 0.18) {
                confidence = 0.80f;
                return "C";
            }
        }

        // D: Index open, others closed
        if (indexOpen && !middleOpen && !ringOpen && !pinkyOpen) {
            if (dThumbMiddle < 0.1) {
                confidence = 0.95f;
                return "D";
            }
        }

        // E: All closed, thumb tucked in front
        if (!indexOpen && !middleOpen && !ringOpen && !pinkyOpen) {
            if (thumbTip.y > middleTip.y && thumbTip.x < ringTip.x) {
                confidence = 0.88f;
                return "E";
            }
        }

        // F: Index and thumb touching, others open
        if (!indexOpen && middleOpen && ringOpen && pinkyOpen) {
            if (dThumbIndex < 0.06) {
                confidence = 0.92f;
                return "F";
            }
        }

        // G: Index pointing sideways
        if (indexOpen && !middleOpen && !ringOpen && !pinkyOpen) {
            if (Math.abs(indexTip.y - thumbTip.y) < 0.08) {
                confidence = 0.85f;
                return "G";
            }
        }

        // H: Index and middle pointing sideways
        if (indexOpen && middleOpen && !ringOpen && !pinkyOpen) {
            if (Math.abs(indexTip.y - middleTip.y) < 0.08) {
                confidence = 0.88f;
                return "H";
            }
        }

        // I: Pinky open, others closed
        if (pinkyOpen && !indexOpen && !middleOpen && !ringOpen) {
            // Ensure thumb is crossing the fingers (not sticking out like Y)
            if (dThumbPinky < 0.15) { // Thumb shouldn't be too far from pinky (unlike Y)
                confidence = 0.90f;
                return "I";
            }
        }

        // K: Index and middle open, thumb between
        if (indexOpen && middleOpen && !ringOpen && !pinkyOpen) {
            // Relaxed threshold for K
            if (dThumbMiddle < 0.08 || (thumbTip.x > indexTip.x && thumbTip.x < middleTip.x)) {
                confidence = 0.90f;
                return "K";
            }
        }

        // L: Index and thumb open
        if (indexOpen && !middleOpen && !ringOpen && !pinkyOpen) {
            if (thumbTip.x < indexTip.x && thumbTip.y > indexTip.y) {
                confidence = 0.95f;
                return "L";
            }
        }

        // M: Thumb under 3 fingers
        if (!indexOpen && !middleOpen && !ringOpen && !pinkyOpen) {
            if (thumbTip.x < pinkyTip.x) {
                confidence = 0.75f;
                return "M";
            }
        }

        // N: Thumb under 2 fingers
        if (!indexOpen && !middleOpen && !ringOpen && !pinkyOpen) {
            if (thumbTip.x < ringTip.x && thumbTip.x > pinkyTip.x) {
                confidence = 0.75f;
                return "N";
            }
        }

        // O: All fingers touching thumb
        if (!indexOpen && !middleOpen && !ringOpen && !pinkyOpen) {
            if (dThumbIndex < 0.06 && dThumbPinky < 0.06) {
                confidence = 0.88f;
                return "O";
            }
        }

        // P: K shape pointing down
        if (indexOpen && middleOpen && !ringOpen && !pinkyOpen) {
            if (indexTip.y > wrist.y) {
                confidence = 0.85f;
                return "P";
            }
        }

        // Q: G shape pointing down
        if (indexOpen && !middleOpen && !ringOpen && !pinkyOpen) {
            if (indexTip.y > wrist.y) {
                confidence = 0.85f;
                return "Q";
            }
        }

        // R: Crossed fingers
        if (indexOpen && middleOpen && !ringOpen && !pinkyOpen) {
            if (indexTip.x > middleTip.x) {
                confidence = 0.88f;
                return "R";
            }
        }

        // S: Fist, thumb over
        if (!indexOpen && !middleOpen && !ringOpen && !pinkyOpen) {
            if (thumbTip.y < middleTip.y && thumbTip.x < indexTip.x) {
                confidence = 0.85f;
                return "S";
            }
        }

        // T: Thumb under index
        if (!indexOpen && !middleOpen && !ringOpen && !pinkyOpen) {
            if (thumbTip.x < middleTip.x && thumbTip.x > indexTip.x) {
                confidence = 0.82f;
                return "T";
            }
        }

        // U: Index and middle together
        if (indexOpen && middleOpen && !ringOpen && !pinkyOpen) {
            // Relaxed threshold for U
            if (distance(indexTip, middleTip) < 0.055) {
                confidence = 0.92f;
                return "U";
            }
        }

        // V: Index and middle apart
        if (indexOpen && middleOpen && !ringOpen && !pinkyOpen) {
            if (distance(indexTip, middleTip) > 0.06) {
                confidence = 0.95f;
                return "V";
            }
        }

        // W: 3 fingers open
        if (indexOpen && middleOpen && ringOpen && !pinkyOpen) {
            confidence = 0.92f;
            return "W";
        }

        // X: Hooked index
        if (!indexOpen && !middleOpen && !ringOpen && !pinkyOpen) {
            if (indexTip.y < thumbTip.y && dThumbIndex > 0.06) {
                confidence = 0.78f;
                return "X";
            }
        }

        // Y: Thumb and pinky open
        if (pinkyOpen && !indexOpen && !middleOpen && !ringOpen) {
            if (dThumbPinky > 0.18) {
                confidence = 0.95f;
                return "Y";
            }
        }

        // Z: Index pointing (Static fallback)
        if (indexOpen && !middleOpen && !ringOpen && !pinkyOpen) {
            confidence = 0.75f;
            return "Z";
        }

        // Numbers 0-5
        if (!indexOpen && !middleOpen && !ringOpen && !pinkyOpen)
            return "0";
        if (indexOpen && !middleOpen && !ringOpen && !pinkyOpen)
            return "1";
        if (indexOpen && middleOpen && !ringOpen && !pinkyOpen)
            return "2";
        if (indexOpen && middleOpen && ringOpen && !pinkyOpen)
            return "3";

        // Check for 5 (Open Palm -> HELLO) BEFORE 4 to avoid shadowing
        // Relaxed thumb distance for HELLO
        if (indexOpen && middleOpen && ringOpen && pinkyOpen && dThumbIndex > 0.10)
            return "HELLO";

        if (indexOpen && middleOpen && ringOpen && pinkyOpen)
            return "4";

        // Space
        if (indexOpen && middleOpen && ringOpen && pinkyOpen && dThumbIndex > 0.12) {
            if (distance(indexTip, middleTip) > 0.06) {
                confidence = 0.95f;
                return " ";
            }
        }

        return null;
    }

    private String detectTemporalSign() {
        if (landmarkBuffer.size() < 15)
            return null;

        // HELLO (Wave)
        float xDiffSum = 0;
        List<Float> xCoords = new ArrayList<>();
        for (List<Landmark> frame : landmarkBuffer) {
            xCoords.add(frame.get(8).x);
        }

        for (int i = 1; i < xCoords.size(); i++) {
            xDiffSum += Math.abs(xCoords.get(i) - xCoords.get(i - 1));
        }

        float xStd = calculateStdDev(xCoords);

        if (xDiffSum > 0.3 && xStd > 0.08) {
            confidence = 0.92f;
            robot.setState("SIGNING");
            return "HELLO";
        }

        // J: Pinky drawing a hook
        float yPinkyStart = landmarkBuffer.getFirst().get(20).y;
        float yPinkyEnd = landmarkBuffer.getLast().get(20).y;
        float xPinkyStart = landmarkBuffer.getFirst().get(20).x;
        float xPinkyEnd = landmarkBuffer.getLast().get(20).x;

        if (yPinkyStart < yPinkyEnd - 0.05 && xPinkyEnd < xPinkyStart - 0.05) {
            confidence = 0.85f;
            robot.setState("SIGNING");
            return "J";
        }

        // Z: Index drawing a Z
        List<Float> xIndex = new ArrayList<>();
        for (List<Landmark> frame : landmarkBuffer) {
            xIndex.add(frame.get(8).x);
        }
        if (calculateStdDev(xIndex) > 0.1) {
            confidence = 0.80f;
            robot.setState("SIGNING");
            return "Z";
        }

        // THANKS
        float yIndexStart = landmarkBuffer.getFirst().get(8).y;
        float yIndexEnd = landmarkBuffer.getLast().get(8).y;
        if (yIndexStart < yIndexEnd - 0.1) {
            confidence = 0.85f;
            robot.setState("SIGNING");
            return "THANKS";
        }

        // HELP
        List<Landmark> lastLandmarks = landmarkBuffer.getLast();
        boolean isFist = !isFingerExtended(lastLandmarks, 1) && !isFingerExtended(lastLandmarks, 2);
        float yWristStart = landmarkBuffer.getFirst().get(0).y;
        float yWristEnd = landmarkBuffer.getLast().get(0).y;

        if (isFist && yWristStart > yWristEnd + 0.1) {
            confidence = 0.88f;
            robot.setState("SIGNING");
            return "HELP";
        }

        return null;
    }

    private float calculateStdDev(List<Float> list) {
        float sum = 0.0f, standardDeviation = 0.0f;
        int length = list.size();
        for (float num : list) {
            sum += num;
        }
        float mean = sum / length;
        for (float num : list) {
            standardDeviation += (float) Math.pow(num - mean, 2);
        }
        return (float) Math.sqrt(standardDeviation / length);
    }

    public static class ProcessResult {
        public String sign;
        public float confidence;
        public Map<String, String> robotData;
        public boolean isSentenceComplete;
        public String completedSentence;

        public ProcessResult(String sign, float confidence, Map<String, String> robotData) {
            this(sign, confidence, robotData, false, null);
        }

        public ProcessResult(String sign, float confidence, Map<String, String> robotData, boolean isSentenceComplete,
                String completedSentence) {
            this.sign = sign;
            this.confidence = confidence;
            this.robotData = robotData;
            this.isSentenceComplete = isSentenceComplete;
            this.completedSentence = completedSentence;
        }
    }
}
