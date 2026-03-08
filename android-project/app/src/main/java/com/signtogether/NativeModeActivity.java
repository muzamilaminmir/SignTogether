package com.signtogether;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.solutions.hands.Hands;
import com.google.mediapipe.solutions.hands.HandsOptions;
import com.google.mediapipe.solutions.hands.HandsResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NativeModeActivity extends BaseActivity {

    private PreviewView viewFinder;
    private TextView detectedText, confidenceText, fullSentence, robotState, robotMessage;
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
    private ImageView robotAvatar;
    private WebView avatarWebView;
    private FloatingActionButton btnMic;

    private EditText inputField;
    private Button btnSend, btnSpeak, btnSwitchCamera;
    private LinearLayout inputContainer;

    // Video Dictionary UI
    private FrameLayout videoContainer;
    private VideoView videoPlayer;
    private ImageView avatarPlaceholder;
    private TextView videoStatusText;

    private ExecutorService cameraExecutor;
    private Hands hands;
    private SignLanguageProcessor processor;
    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private String lastSpokenSentence = "";

    private String currentMode = "SIGN_TO_SPEECH";

    // Playlist Manager
    private List<String> videoPlaylist = new ArrayList<>();
    private int currentVideoIndex = 0;
    private List<String> availableAssets = new ArrayList<>();

    // Translation Maps
    private java.util.HashMap<String, String> hiToEn = new java.util.HashMap<>();
    private java.util.HashMap<String, String> enToHi = new java.util.HashMap<>();

    // Phrase Map for common phrases to single/existing assets
    private java.util.HashMap<String, String> phraseMap = new java.util.HashMap<>();

    private void initTranslationMaps() {
        // ... (Keep existing En to Hi mappings) ...
        // En to Hi
        enToHi.put("after", "बाद में");
        enToHi.put("again", "फिर से");
        enToHi.put("against", "खिलाफ");
        enToHi.put("age", "उम्र");
        enToHi.put("all", "सब");
        enToHi.put("alone", "अकेला");
        enToHi.put("also", "भी");
        enToHi.put("and", "और");
        enToHi.put("ask", "पूछना");
        enToHi.put("at", "पर");
        enToHi.put("be", "होना");
        enToHi.put("beautiful", "सुंदर");
        enToHi.put("before", "पहले");
        enToHi.put("best", "श्रेष्ठ");
        enToHi.put("better", "बेहतर");
        enToHi.put("busy", "व्यस्त");
        enToHi.put("but", "लेकिन");
        enToHi.put("bye", "अलविदा");
        enToHi.put("can", "सकना");
        enToHi.put("cannot", "नहीं कर सकते");
        enToHi.put("change", "बदलाव");
        enToHi.put("college", "कॉलेज");
        enToHi.put("come", "आना");
        enToHi.put("computer", "कंप्यूटर");
        enToHi.put("day", "दिन");
        enToHi.put("distance", "दूरी");
        enToHi.put("do not", "मत करो");
        enToHi.put("do", "करना");
        enToHi.put("does not", "नहीं करता");
        enToHi.put("eat", "खाना"); // Verb
        enToHi.put("engineer", "इंजीनियर");
        enToHi.put("fight", "लड़ाई");
        enToHi.put("finish", "खत्म");
        enToHi.put("from", "से");
        enToHi.put("glitter", "चमक");
        enToHi.put("go", "जाना");
        enToHi.put("god", "भगवान");
        enToHi.put("gold", "सोना");
        enToHi.put("good", "अच्छा");
        enToHi.put("great", "महान");
        enToHi.put("hand", "हाथ");
        enToHi.put("hands", "हाथ");
        enToHi.put("happy", "खुश");
        enToHi.put("hello", "नमस्ते");
        enToHi.put("help", "मदद");
        enToHi.put("her", "उसकी");
        enToHi.put("here", "यहाँ");
        enToHi.put("his", "उसका");
        enToHi.put("home", "घर");
        enToHi.put("homepage", "होमपेज");
        enToHi.put("how", "कैसे");
        enToHi.put("i", "मैं");
        enToHi.put("invent", "आविष्कार");
        enToHi.put("it", "यह");
        enToHi.put("keep", "रखना");
        enToHi.put("language", "भाषा");
        enToHi.put("laugh", "हँसना");
        enToHi.put("learn", "सीखना");
        enToHi.put("me", "मुझे");
        enToHi.put("more", "अधिक");
        enToHi.put("my", "मेरा");
        enToHi.put("name", "नाम");
        enToHi.put("next", "अगला");
        enToHi.put("not", "नहीं");
        enToHi.put("now", "अब");
        enToHi.put("of", "का");
        enToHi.put("on", "पर");
        enToHi.put("our", "हमारा");
        enToHi.put("out", "बाहर");
        enToHi.put("pretty", "सुंदर");
        enToHi.put("right", "सही");
        enToHi.put("sad", "दुखी");
        enToHi.put("safe", "सुरक्षित");
        enToHi.put("see", "देखना");
        enToHi.put("self", "स्वयं");
        enToHi.put("sign", "संकेत");
        enToHi.put("sing", "गाना");
        enToHi.put("so", "तो");
        enToHi.put("sound", "ध्वनि");
        enToHi.put("stay", "रहना");
        enToHi.put("study", "अध्ययन");
        enToHi.put("talk", "बात करना");
        enToHi.put("television", "टेलीविज़न");
        enToHi.put("thank you", "धन्यवाद");
        enToHi.put("thank", "धन्यवाद");
        enToHi.put("that", "वह");
        enToHi.put("they", "वे");
        enToHi.put("this", "यह");
        enToHi.put("those", "वो");
        enToHi.put("time", "समय");
        enToHi.put("to", "को");
        enToHi.put("type", "प्रकार");
        enToHi.put("us", "हमे");
        enToHi.put("walk", "चलना");
        enToHi.put("wash", "धोना");
        enToHi.put("way", "रास्ता");
        enToHi.put("we", "हम");
        enToHi.put("welcome", "स्वागत");
        enToHi.put("what", "क्या");
        enToHi.put("when", "कब");
        enToHi.put("where", "कहाँ");
        enToHi.put("which", "कौनसा");
        enToHi.put("who", "कौन");
        enToHi.put("whole", "पूरा");
        enToHi.put("whose", "किसका");
        enToHi.put("why", "क्यों");
        enToHi.put("will", "होगा");
        enToHi.put("with", "साथ");
        enToHi.put("without", "बिना");
        enToHi.put("words", "शब्द");
        enToHi.put("work", "काम");
        enToHi.put("world", "दुनिया");
        enToHi.put("wrong", "गलत");
        enToHi.put("you", "तुम");
        enToHi.put("your", "तुम्हारा");
        enToHi.put("yourself", "खुद");

        // Manually added for basic needs if not present
        enToHi.put("please", "कृपया");
        enToHi.put("yes", "हाँ");
        enToHi.put("no", "नहीं");
        enToHi.put("food", "खाना"); // Noun
        enToHi.put("water", "पानी");
        enToHi.put("house", "घर");
        enToHi.put("family", "परिवार");
        enToHi.put("friend", "मित्र");
        enToHi.put("love", "प्यार");

        // Init Phrase Map (English Phrase -> English Asset Name or Single Word)
        phraseMap.put("right now", "now");
        phraseMap.put("thank you", "thank you"); // Explicitly map to asset to ensure it's picked
        phraseMap.put("thanks a lot", "thank you");
        phraseMap.put("see you", "bye");
        phraseMap.put("good morning", "hello"); // Approx
        phraseMap.put("what's up", "hello");

        // Hi to En (Reverse)
        for (Map.Entry<String, String> entry : enToHi.entrySet()) {
            hiToEn.put(entry.getValue(), entry.getKey());
        }
    }

    private boolean isHindi() {
        return Locale.getDefault().getLanguage().equals("hi");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_mode);

        initTranslationMaps();

        currentMode = getIntent().getStringExtra("MODE");
        if (currentMode == null)
            currentMode = "SIGN_TO_SPEECH";

        // Bind Views
        viewFinder = findViewById(R.id.viewFinder);
        detectedText = findViewById(R.id.detectedText);
        confidenceText = findViewById(R.id.confidenceText);
        fullSentence = findViewById(R.id.fullSentence);
        robotState = findViewById(R.id.robotState);
        robotMessage = findViewById(R.id.robotMessage);
        robotAvatar = findViewById(R.id.robotAvatar);
        btnMic = findViewById(R.id.btnMic);
        inputField = findViewById(R.id.inputField);
        btnSend = findViewById(R.id.btnSend);
        btnSpeak = findViewById(R.id.btnSpeak);
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera);

        videoContainer = findViewById(R.id.videoContainer);
        videoPlayer = findViewById(R.id.videoPlayer);
        avatarPlaceholder = findViewById(R.id.avatarPlaceholder);
        videoStatusText = findViewById(R.id.videoStatusText);
        avatarWebView = findViewById(R.id.avatarWebView);

        // Configure WebView
        WebSettings webSettings = avatarWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        avatarWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Inject CSS to hide cookie banner (vanilla-cookieconsent)
                String css = "#cc--main, #cc-main, .cc-window, .cc-banner { display: none !important; }";
                String js = "var style = document.createElement('style');" +
                        "style.innerHTML = '" + css + "';" +
                        "document.head.appendChild(style);";
                view.evaluateJavascript(js, null);
            }
        });
        // Load initial page (Sign Translate)
        // Using public URL for now. Can be replaced with
        // file:///android_asset/webapp/index.html
        avatarWebView.loadUrl("https://sign.mt/translate");

        // Load Asset List
        try {
            String[] assets = getAssets().list("signs");
            if (assets != null) {
                availableAssets = Arrays.asList(assets);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Apply Mode Logic
        if (currentMode.equals("SPEECH_TO_SIGN")) {
            // Hide Camera
            viewFinder.setVisibility(View.GONE);
            detectedText.setVisibility(View.GONE);
            confidenceText.setVisibility(View.GONE);
            robotAvatar.setVisibility(View.GONE); // Hide old robot
            btnSwitchCamera.setVisibility(View.GONE); // Hide switch camera

            // Show Inputs & Avatar Placeholder
            btnMic.setVisibility(View.VISIBLE);
            inputField.setVisibility(View.VISIBLE);
            btnSend.setVisibility(View.VISIBLE);
            btnSpeak.setVisibility(View.GONE); // No manual speak in this mode

            // Hide old video container, Show WebView
            videoContainer.setVisibility(View.GONE);
            avatarWebView.setVisibility(View.VISIBLE);

            videoStatusText.setText(getString(R.string.status_ready_to_sign));

        } else {
            // SIGN_TO_SPEECH
            // Show Camera
            viewFinder.setVisibility(View.VISIBLE);
            detectedText.setVisibility(View.VISIBLE);
            confidenceText.setVisibility(View.VISIBLE);
            btnSwitchCamera.setVisibility(View.VISIBLE); // Show switch camera
            // Hide Inputs & Video
            btnMic.setVisibility(View.GONE);
            inputField.setVisibility(View.GONE);
            btnSend.setVisibility(View.GONE);
            btnSpeak.setVisibility(View.VISIBLE); // Show Manual Speak Button
            videoContainer.setVisibility(View.GONE);
        }

        // Initialize Core Components
        processor = new SignLanguageProcessor();
        cameraExecutor = Executors.newSingleThreadExecutor();

        initTTS();
        initSpeechRecognition();
        initMediaPipe();

        // Only start camera if needed
        if (currentMode.equals("SIGN_TO_SPEECH")) {
            startCamera();
        }

        // Listeners
        btnMic.setOnClickListener(v -> startListening());
        btnSend.setOnClickListener(v -> handleTextInput());
        btnSpeak.setOnClickListener(v -> {
            String text = fullSentence.getText().toString();
            if (!text.isEmpty()) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        // Camera Switch Feature
        btnSwitchCamera.setOnClickListener(v -> {
            if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
            } else {
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
            }
            startCamera();
        });

        // Video Completion Listener
        videoPlayer.setOnCompletionListener(mp -> {
            currentVideoIndex++;
            playNextVideo();
        });

        // Close video on container click (only if not in speech mode)
        videoContainer.setOnClickListener(v -> {
            if (currentMode.equals("SIGN_TO_SPEECH")) {
                if (videoPlayer.isPlaying())
                    videoPlayer.stopPlayback();
                videoContainer.setVisibility(View.GONE);
            }
        });
    }

    private void initTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                if (isHindi()) {
                    tts.setLanguage(new Locale("hi", "IN"));
                } else {
                    tts.setLanguage(Locale.US);
                }
            }
        });
    }

    private void initSpeechRecognition() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Toast.makeText(NativeModeActivity.this, getString(R.string.status_listening), Toast.LENGTH_SHORT)
                        .show();
                if (currentMode.equals("SPEECH_TO_SIGN")) {
                    videoStatusText.setText(getString(R.string.status_listening));
                }
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int error) {
                Toast.makeText(NativeModeActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                if (currentMode.equals("SPEECH_TO_SIGN")) {
                    videoStatusText.setText(getString(R.string.status_ready_to_sign));
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String text = matches.get(0);
                    handleSpeechInput(text);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });
    }

    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        if (isHindi()) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN");
        } else {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        }
        speechRecognizer.startListening(intent);
    }

    private void handleSpeechInput(String text) {
        inputField.setText(text);

        if (currentMode.equals("SPEECH_TO_SIGN")) {
            // Use Avatar WebView instead of local playlist
            String url = "https://sign.mt/translate?text=" + android.net.Uri.encode(text);
            avatarWebView.loadUrl(url);
            // preparePlaylist(text); // Disable old logic
        } else {

            // Fallback for Sign to Speech mode (if needed)
            playDictionaryVideo(text);
        }
    }

    private void handleTextInput() {
        String text = inputField.getText().toString().trim();
        if (!text.isEmpty()) {
            handleSpeechInput(text);
            inputField.setText("");
        }
    }

    // --- SEQUENTIAL VIDEO LOGIC ---

    private void preparePlaylist(String sentence) {
        videoPlaylist.clear();
        currentVideoIndex = 0;

        String processedSentence = sentence.toLowerCase().trim();

        // 1. Check Phrase Map (Whole Sentence or Sub-phrases if complex logic)
        // Simple approach: Check if whole sentence is a phrase first
        if (phraseMap.containsKey(processedSentence)) {
            processedSentence = phraseMap.get(processedSentence);
        }

        String[] words = processedSentence.split("\\s+");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            // Check for translation if Hindi
            String targetWord = word;
            if (isHindi()) {
                if (hiToEn.containsKey(word)) {
                    targetWord = hiToEn.get(word);
                }
            }

            // CHECK FOR MULTI-WORD ASSET (Look ahead 1 word)
            if (i + 1 < words.length) {
                String nextWord = words[i + 1];
                // Check translation for next word too
                String nextTarget = nextWord;
                if (isHindi() && hiToEn.containsKey(nextWord)) {
                    nextTarget = hiToEn.get(nextWord);
                }

                String combined = targetWord + " " + nextTarget;
                String combinedFilename = combined + ".mp4";

                // Also check phrase map for 2-word phrase
                if (phraseMap.containsKey(combined)) {
                    String mappedCombined = phraseMap.get(combined);
                    if (availableAssets.contains(mappedCombined + ".mp4")) {
                        videoPlaylist.add(mappedCombined + ".mp4");
                        i++; // Skip next word
                        continue;
                    }
                }

                if (availableAssets.contains(combinedFilename)) {
                    videoPlaylist.add(combinedFilename);
                    i++; // Skip next word
                    continue;
                }
            }

            // Check Phrase Map for single word (maybe "right now" was passed as logic?)
            // Already handled whole sentence, but let's check current word if mapped
            if (phraseMap.containsKey(targetWord)) {
                targetWord = phraseMap.get(targetWord);
            }

            String filename = targetWord + ".mp4";
            if (availableAssets.contains(filename)) {
                videoPlaylist.add(filename);
            } else {
                // Spell it out
                // If Hindi, we skip spelling or maybe allow it if it's latin characters?
                // For now, only spell english characters
                for (char c : targetWord.toCharArray()) {
                    if (Character.isLetterOrDigit(c) && c <= 'z') { // Simple check for english
                        String charFile = c + ".mp4";
                        if (availableAssets.contains(charFile)) {
                            videoPlaylist.add(charFile);
                        }
                    }
                }
            }
        }

        if (!videoPlaylist.isEmpty()) {
            videoStatusText.setText(getString(R.string.status_signing, sentence));
            avatarPlaceholder.setVisibility(View.GONE);
            videoPlayer.setVisibility(View.VISIBLE);
            playNextVideo();
        } else {
            videoStatusText.setText(getString(R.string.status_no_signs));
        }
    }

    private void playNextVideo() {
        if (currentVideoIndex < videoPlaylist.size()) {
            String filename = videoPlaylist.get(currentVideoIndex);
            playAssetVideo(filename);
        } else {
            // Done
            videoPlayer.setVisibility(View.GONE);
            avatarPlaceholder.setVisibility(View.VISIBLE);
            videoStatusText.setText(getString(R.string.status_ready_to_sign));
        }
    }

    private void playAssetVideo(String filename) {
        try {
            File cacheFile = new File(getCacheDir(), filename);
            if (!cacheFile.exists()) {
                InputStream is = getAssets().open("signs/" + filename);
                FileOutputStream fos = new FileOutputStream(cacheFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                fos.close();
                is.close();
            }
            videoPlayer.setVideoPath(cacheFile.getAbsolutePath());
            videoPlayer.start();
        } catch (IOException e) {
            Log.e("Player", "Error playing " + filename, e);
            currentVideoIndex++;
            playNextVideo();
        }
    }

    // Legacy single video player
    private boolean playDictionaryVideo(String word) {
        String targetWord = word.toLowerCase().trim();
        if (isHindi() && hiToEn.containsKey(targetWord)) {
            targetWord = hiToEn.get(targetWord);
        }

        String filename = targetWord + ".mp4";
        if (availableAssets.contains(filename)) {
            videoContainer.setVisibility(View.VISIBLE);
            videoPlayer.setVisibility(View.VISIBLE);
            avatarPlaceholder.setVisibility(View.GONE);
            playAssetVideo(filename);
            return true;
        }
        return false;
    }

    private void initMediaPipe() {
        HandsOptions handsOptions = HandsOptions.builder()
                .setStaticImageMode(false)
                .setMaxNumHands(1)
                .setRunOnGpu(true)
                .build();

        hands = new Hands(this, handsOptions);
        hands.setErrorListener((message, e) -> Log.e("MediaPipe", "MediaPipe error: " + message));

        hands.setResultListener(this::handleResult);
    }

    private void handleResult(HandsResult result) {
        List<SignLanguageProcessor.Landmark> landmarks = null;

        if (result.multiHandLandmarks() != null && !result.multiHandLandmarks().isEmpty()) {
            landmarks = new ArrayList<>();
            List<com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark> mpLandmarks = result
                    .multiHandLandmarks().get(0).getLandmarkList();

            for (com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark lm : mpLandmarks) {
                landmarks.add(new SignLanguageProcessor.Landmark(lm.getX(), lm.getY(), lm.getZ()));
            }
        }

        SignLanguageProcessor.ProcessResult procResult = processor.process(landmarks);
        runOnUiThread(() -> updateUI(procResult));
    }

    private void updateUI(SignLanguageProcessor.ProcessResult result) {
        if (result.sign != null) {
            String displaySign = result.sign;
            // Translate if Hindi
            if (isHindi() && enToHi.containsKey(displaySign.toLowerCase())) {
                displaySign = enToHi.get(displaySign.toLowerCase());
            }

            detectedText.setText(displaySign);
            confidenceText.setText(
                    getString(R.string.status_confidence, String.valueOf((int) (result.confidence * 100)) + "%"));
        } else {
            detectedText.setText("...");
        }

        // Handle Sentence Completion
        if (result.isSentenceComplete && result.completedSentence != null) {
            String sentence = result.completedSentence;
            // Best effort translation for sentence logic (complex, might improve later)
            // For now, let's just speak English sentence in Hindi mode with English accent?
            // Or try to translate words?
            // Let's keep it simple: Use English sentence for TTS if no translation
            // available.
            // But if we can map individual signs, we can reconstruct?
            // "HELLO HELP" -> "नमस्ते मदद"

            if (isHindi()) {
                String[] words = sentence.split(" ");
                StringBuilder sb = new StringBuilder();
                for (String w : words) {
                    if (enToHi.containsKey(w.toLowerCase())) {
                        sb.append(enToHi.get(w.toLowerCase())).append(" ");
                    } else {
                        sb.append(w).append(" ");
                    }
                }
                sentence = sb.toString().trim();
            }

            Toast.makeText(this, getString(R.string.sentence_captured, sentence), Toast.LENGTH_LONG).show();
            // Optional: Speak the full sentence automatically
            if (currentMode.equals("SIGN_TO_SPEECH")) {
                tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, null);
            }
            fullSentence.setText(""); // Clear UI buffer
        } else {
            String currentSentence = processor.getFullSentence();
            // Real-time translation of sentence view?
            if (isHindi()) {
                String[] words = currentSentence.split(" ");
                StringBuilder sb = new StringBuilder();
                for (String w : words) {
                    if (enToHi.containsKey(w.toLowerCase())) {
                        sb.append(enToHi.get(w.toLowerCase())).append(" ");
                    } else {
                        sb.append(w).append(" ");
                    }
                }
                fullSentence.setText(sb.toString().trim());
            } else {
                fullSentence.setText(currentSentence);
            }
        }

        // Only update robot UI if in Sign to Speech mode (though robot is hidden in
        // Speech to Sign)
        if (currentMode.equals("SIGN_TO_SPEECH")) {
            updateRobotUI();
        }
    }

    private void updateRobotUI() {
        Map<String, String> robotData = processor.robot.getUiData();
        String state = robotData.get("state_name");
        String message = robotData.get("text"); // Translate this if needed?

        robotState.setText(state); // Localization of State names might be needed later
        robotMessage.setText(message);

        // Update Avatar Image
        int resId = R.drawable.idle; // Default

        if (state.equals("LISTENING"))
            resId = R.drawable.listening;
        else if (state.equals("THINKING"))
            resId = R.drawable.thinking;
        else if (state.equals("SIGNING") || state.equals("REPLYING"))
            resId = R.drawable.listening;
        else if (state.equals("CONFUSED"))
            resId = R.drawable.confused;

        robotAvatar.setImageResource(resId);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    Bitmap bitmap = toBitmap(image);
                    if (bitmap != null) {
                        long timestamp = System.currentTimeMillis();
                        hands.send(bitmap, timestamp);
                    }
                    image.close();
                });

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("Camera", "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private Bitmap toBitmap(ImageProxy image) {
        if (image.getFormat() != ImageFormat.YUV_420_888) {
            return null;
        }
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);
        byte[] imageBytes = out.toByteArray();

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
