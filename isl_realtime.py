from flask import Flask, render_template, Response, jsonify
import cv2
import time
from ai_engine import SignLanguageSystem

app = Flask(__name__)

# Initialize the Judge-Ready AI Engine
sl_system = SignLanguageSystem()
is_running = True

def generate_frames():
    global is_running
    cap = cv2.VideoCapture(0)
    
    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break

        if is_running:
            # Process frame using the advanced Temporal AI Engine
            frame, _ = sl_system.process_frame(frame)

        ret, buffer = cv2.imencode('.jpg', frame)
        yield (b'--frame\r\nContent-Type: image/jpeg\r\n\r\n' + buffer.tobytes() + b'\r\n')
    
    cap.release()

@app.route('/')
def index():
    return render_template('model2.html') # Use the new UI with Robot

@app.route('/video_feed')
def video_feed():
    return Response(generate_frames(), mimetype='multipart/x-mixed-replace; boundary=frame')

@app.route('/get_status')
def get_status():
    return jsonify(sl_system.get_status())

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

if __name__ == '__main__':
    print("Starting Judge-Ready SignTogether (Standalone Mode)...")
    app.run(debug=True, port=5001)