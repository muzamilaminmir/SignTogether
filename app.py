import sys
import ssl
import sys
import ssl
import importlib

# CRITICAL FIX: Reload SSL module to remove any monkeypatches from pip/truststore
# This restores ssl.create_default_context and ssl.SSLContext to their original states
try:
    importlib.reload(ssl)
    print("Successfully reloaded SSL module to clear patches.")
except Exception as e:
    print(f"Error reloading SSL: {e}")

from flask import Flask, render_template, send_from_directory, request, jsonify, Response
import os
import cv2
import mediapipe as mp
from spellchecker import SpellChecker
import numpy as np
import time
import pyttsx3
from threading import Lock
from ai_engine import SignLanguageSystem

app = Flask(__name__)

# Initialize the new Sign Language System
sl_system = SignLanguageSystem()

# Initialize thread-safe lock for TTS engine
tts_lock = Lock()

# MODEL 1 CODE
VIDEO_FOLDER = os.path.join("static", "signs")
app.config["VIDEO_FOLDER"] = VIDEO_FOLDER

# Alphabet & Number fallback (A-Z, 0-9)
CHAR_MAP = {chr(i): f"{chr(i).upper()}.mp4" for i in range(97, 123)}
CHAR_MAP.update({str(i): f"{i}.mp4" for i in range(10)})

# Comprehensive Word Mapping from static/signs
VIDEO_MAP = {
    "after": "After.mp4", "again": "Again.mp4", "against": "Against.mp4", "age": "Age.mp4",
    "all": "All.mp4", "alone": "Alone.mp4", "also": "Also.mp4", "and": "And.mp4",
    "ask": "Ask.mp4", "at": "At.mp4", "be": "Be.mp4", "beautiful": "Beautiful.mp4",
    "before": "Before.mp4", "best": "Best.mp4", "better": "Better.mp4", "busy": "Busy.mp4",
    "but": "But.mp4", "bye": "Bye.mp4", "can": "Can.mp4", "cannot": "Cannot.mp4",
    "change": "Change.mp4", "college": "College.mp4", "come": "Come.mp4", "computer": "Computer.mp4",
    "day": "Day.mp4", "distance": "Distance.mp4", "do not": "Do Not.mp4", "do": "Do.mp4",
    "does not": "Does Not.mp4", "eat": "Eat.mp4", "engineer": "Engineer.mp4", "fight": "Fight.mp4",
    "finish": "Finish.mp4", "from": "From.mp4", "glitter": "Glitter.mp4", "go": "Go.mp4",
    "god": "God.mp4", "gold": "Gold.mp4", "good": "Good.mp4", "great": "Great.mp4",
    "hand": "Hand.mp4", "hands": "Hands.mp4", "happy": "Happy.mp4", "hello": "Hello.mp4",
    "help": "Help.mp4", "her": "Her.mp4", "here": "Here.mp4", "his": "His.mp4",
    "home": "Home.mp4", "homepage": "Homepage.mp4", "how": "How.mp4", "i": "I.mp4",
    "invent": "Invent.mp4", "it": "It.mp4", "keep": "Keep.mp4", "language": "Language.mp4",
    "laugh": "Laugh.mp4", "learn": "Learn.mp4", "me": "ME.mp4", "more": "More.mp4",
    "my": "My.mp4", "name": "Name.mp4", "next": "Next.mp4", "not": "Not.mp4",
    "now": "Now.mp4", "of": "Of.mp4", "on": "On.mp4", "our": "Our.mp4",
    "out": "Out.mp4", "pretty": "Pretty.mp4", "right": "Right.mp4", "sad": "Sad.mp4",
    "safe": "Safe.mp4", "see": "See.mp4", "self": "Self.mp4", "sign": "Sign.mp4",
    "sing": "Sing.mp4", "so": "So.mp4", "sound": "Sound.mp4", "stay": "Stay.mp4",
    "study": "Study.mp4", "talk": "Talk.mp4", "television": "Television.mp4",
    "thank you": "Thank You.mp4", "thank": "Thank.mp4", "that": "That.mp4",
    "they": "They.mp4", "this": "This.mp4", "those": "Those.mp4", "time": "Time.mp4",
    "to": "To.mp4", "type": "Type.mp4", "us": "Us.mp4", "welcome": "Welcome.mp4",
    "what": "What.mp4", "when": "When.mp4", "where": "Where.mp4", "which": "Which.mp4",
    "who": "Who.mp4", "whole": "Whole.mp4", "whose": "Whose.mp4", "why": "Why.mp4",
    "will": "Will.mp4", "with": "With.mp4", "without": "Without.mp4", "words": "Words.mp4",
    "work": "Work.mp4", "world": "World.mp4", "wrong": "Wrong.mp4", "you": "You.mp4",
    "your": "Your.mp4", "yourself": "Yourself.mp4"
}

@app.route('/translate', methods=['POST'])
def translate_sentence():
    data = request.get_json()
    sentence = data.get("sentence", "").strip().lower()
    
    if not sentence:
        return jsonify({"videos": ["/static/signs/Talk.mp4"]})

    video_files = []
    
    # Simple greedy matching for multi-word phrases
    words = sentence.split()
    i = 0
    while i < len(words):
        # Check for 2-word phrase
        if i + 1 < len(words):
            phrase = f"{words[i]} {words[i+1]}"
            if phrase in VIDEO_MAP:
                video_files.append(f"/static/signs/{VIDEO_MAP[phrase]}")
                i += 2
                continue
        
        # Check for single word
        word = words[i]
        if word in VIDEO_MAP:
            video_files.append(f"/static/signs/{VIDEO_MAP[word]}")
        else:
            # Fallback to character/digit breakdown
            for char in word:
                if char in CHAR_MAP:
                    video_path = f"/static/signs/{CHAR_MAP[char]}"
                    if os.path.exists(os.path.join(VIDEO_FOLDER, CHAR_MAP[char])):
                        video_files.append(video_path)
        i += 1

    if not video_files:
        video_files.append("/static/signs/Talk.mp4")

    return jsonify({"videos": video_files})

@app.route('/static/signs/<path:filename>')
def serve_signs_video(filename):
    return send_from_directory(VIDEO_FOLDER, filename)

# MODEL 11 CODE
VIDEO_DIR = "static/signs2"

def get_video_files(word):
    videos = []
    for char in word.upper():
        filename = f"{char}.mp4"
        filepath = os.path.join(VIDEO_DIR, filename)
        if os.path.exists(filepath):
            videos.append(f"/static/signs2/{filename}")
    return videos

@app.route('/translate_word')
def translate_word():
    word = request.args.get('word', '').lower().strip()
    if not word:
        return jsonify({"videos": []})
    
    videos = get_video_files(word)
    return jsonify({"videos": videos})

@app.route('/static/signs2/<path:filename>')
def serve_signs2_video(filename):
    return send_from_directory(VIDEO_DIR, filename)


import base64
import io
from PIL import Image

# ... (existing imports)

# MODEL 2 CODE (UPGRADED)
is_running = True

@app.route('/process_frame', methods=['POST'])
def process_frame():
    global is_running
    if not is_running:
        return jsonify({"status": "stopped"})

    try:
        data = request.json
        image_data = data['image']
        
        # Decode base64 image
        header, encoded = image_data.split(",", 1)
        binary_data = base64.b64decode(encoded)
        
        # Convert to numpy array for OpenCV
        image = Image.open(io.BytesIO(binary_data))
        frame = cv2.cvtColor(np.array(image), cv2.COLOR_RGB2BGR)
        
        # Process frame
        processed_frame, current_sign = sl_system.process_frame(frame)
        
        # Encode back to base64
        _, buffer = cv2.imencode('.jpg', processed_frame)
        processed_image_data = base64.b64encode(buffer).decode('utf-8')
        
        return jsonify({
            "image": f"data:image/jpeg;base64,{processed_image_data}",
            "status": sl_system.get_status()
        })
    except Exception as e:
        print(f"Error processing frame: {e}")
        return jsonify({"error": str(e)}), 500

def generate_frames():
    global is_running
    cap = cv2.VideoCapture(0)
    
    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break

        if is_running:
            frame, _ = sl_system.process_frame(frame)

        ret, buffer = cv2.imencode('.jpg', frame)
        yield (b'--frame\r\nContent-Type: image/jpeg\r\n\r\n' + buffer.tobytes() + b'\r\n')
    
    cap.release()

@app.route('/team')
def team():
    return render_template('team.html')

@app.route('/video_feed')
def video_feed():
    return Response(generate_frames(), mimetype='multipart/x-mixed-replace; boundary=frame')

@app.route('/get_status')
def get_status():
    return jsonify(sl_system.get_status())

@app.route('/emergency', methods=['GET', 'POST'])
def emergency():
    if request.method == 'POST':
        data = request.get_json()
        message = data.get("message", "I need help")
        # Trigger TTS
        try:
            engine = pyttsx3.init()
            engine.say(message)
            engine.runAndWait()
        except:
            pass
        return jsonify({"status": "Emergency alert triggered", "message": message})
    return render_template('emergency.html')

@app.route('/learn')
def learn():
    return render_template('learn.html')

@app.route('/stop')
def stop():
    global is_running
    is_running = False
    return jsonify({"status": "stopped"})

@app.route('/restart')
def restart():
    global is_running
    is_running = True
    sl_system.full_sentence = []
    sl_system.detected_text = ""
    return jsonify({"status": "restarted"})

@app.route('/speak')
def speak():
    text = sl_system.get_status()["full_sentence"]
    if text:
        try:
            engine = pyttsx3.init()
            engine.say(text)
            engine.runAndWait()
        except:
            pass
    return jsonify({"status": "speaking"})
# HTML Routes (Unchanged)
@app.route('/')
def index():
    return render_template("index.html")

@app.route('/model1')
def model1():
    return render_template("model1.html")

@app.route('/model2')
def model2():
    return render_template("model2.html")

@app.route('/about')
def about():
    return render_template('about.html')

@app.route('/contact')
def contact():
    return render_template('contact.html')

@app.route('/register')
def register():
    return render_template('register.html')

@app.route('/converter')
def converter():
    return render_template("converter.html")

@app.route('/admin')
def admin():
    return render_template("admin.html")

if __name__ == '__main__':
    # Ensure we use standard SSL context
    context = None
    if os.path.exists('cert.pem') and os.path.exists('key.pem'):
        try:
            # Create a standard SSL Context manually to avoid truststore interference
            context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
            context.load_cert_chain('cert.pem', 'key.pem')
            print("Loaded SSL Certs successfully.")
        except Exception as e:
            print(f"Error loading certs: {e}")
            context = ('cert.pem', 'key.pem') # Fallback
    else:
        print("Warning: cert.pem or key.pem not found. Running without SSL (or adhoc if configured).")
        # context = 'adhoc' # Optional: Use adhoc if needed

    app.run(host='0.0.0.0', port=5000, debug=True, threaded=True, ssl_context=context)