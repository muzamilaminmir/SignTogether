import os
from PIL import Image

# Source image path
source_path = r"C:/Users/ACER/.gemini/antigravity/brain/61c9abf9-900f-47c8-aad5-8848f7490914/uploaded_image_1769174241684.png"
res_path = r"c:\Users\ACER\OneDrive\Desktop\sign lan\android-project\app\src\main\res"

# Icon sizes
sizes = {
    "mipmap-mdpi": (48, 48),
    "mipmap-hdpi": (72, 72),
    "mipmap-xhdpi": (96, 96),
    "mipmap-xxhdpi": (144, 144),
    "mipmap-xxxhdpi": (192, 192)
}

def create_icons():
    try:
        img = Image.open(source_path)
        
        # Convert to RGBA to ensure transparency support if we were doing masking (though input is jpg)
        img = img.convert("RGBA")

        for folder, size in sizes.items():
            folder_path = os.path.join(res_path, folder)
            os.makedirs(folder_path, exist_ok=True)
            
            # Resize
            icon = img.resize(size, Image.Resampling.LANCZOS)
            
            # Save as ic_launcher.png
            icon.save(os.path.join(folder_path, "ic_launcher.png"), "PNG")
            
            # Save as ic_launcher_round.png (using same image for simplicity)
            icon.save(os.path.join(folder_path, "ic_launcher_round.png"), "PNG")
            
            print(f"Created icons in {folder}")
            
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    create_icons()
