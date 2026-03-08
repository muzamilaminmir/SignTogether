import os
import shutil

SOURCE_DIR = r"c:\Users\ACER\OneDrive\Desktop\sign lan\static\signs"
DEST_DIR = r"c:\Users\ACER\OneDrive\Desktop\sign lan\android-project\app\src\main\assets\signs"

if not os.path.exists(DEST_DIR):
    os.makedirs(DEST_DIR)

count = 0
for filename in os.listdir(SOURCE_DIR):
    if filename.lower().endswith(".mp4"):
        src_path = os.path.join(SOURCE_DIR, filename)
        # Rename to lowercase for Android consistency
        dest_filename = filename.lower()
        dest_path = os.path.join(DEST_DIR, dest_filename)
        
        shutil.copy2(src_path, dest_path)
        count += 1

print(f"Copied and renamed {count} video files.")
