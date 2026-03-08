import tensorflow as tf
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Conv2D, MaxPooling2D, Flatten, Dense, Dropout, BatchNormalization
import os

# Define image properties
img_size = (64, 64)
batch_size = 32
epochs = 15 # Reduced epochs for faster iteration, increase for better accuracy
dataset_path = "asl_dataset" # Relative path to the dataset folder

# Check if dataset exists
if not os.path.exists(dataset_path):
    print(f"Error: Dataset not found at {dataset_path}")
    exit()

# Image Data Generator (Preprocessing & Augmentation)
datagen = ImageDataGenerator(
    rescale=1./255,
    rotation_range=10,
    width_shift_range=0.1,
    height_shift_range=0.1,
    shear_range=0.1,
    zoom_range=0.1,
    validation_split=0.2
)

print("Loading Training Data...")
train_data = datagen.flow_from_directory(
    dataset_path,
    target_size=img_size,
    batch_size=batch_size,
    color_mode="rgb", # Changed to RGB as ASL dataset might be colored
    class_mode="categorical",
    subset="training",
    shuffle=True
)

print("Loading Validation Data...")
val_data = datagen.flow_from_directory(
    dataset_path,
    target_size=img_size,
    batch_size=batch_size,
    color_mode="rgb",
    class_mode="categorical",
    subset="validation",
    shuffle=False
)

num_classes = len(train_data.class_indices)
print(f"Detected {num_classes} classes: {list(train_data.class_indices.keys())}")

# CNN Architecture
model = Sequential([
    # First Convolutional Block
    Conv2D(32, (3, 3), activation='relu', input_shape=(64, 64, 3)),
    BatchNormalization(),
    MaxPooling2D(2, 2),

    # Second Convolutional Block
    Conv2D(64, (3, 3), activation='relu'),
    BatchNormalization(),
    MaxPooling2D(2, 2),

    # Third Convolutional Block
    Conv2D(128, (3, 3), activation='relu'),
    BatchNormalization(),
    MaxPooling2D(2, 2),

    # Dense Layers
    Flatten(),
    Dense(256, activation='relu'),
    Dropout(0.5),
    Dense(num_classes, activation='softmax')
])

# Compile Model
model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])

# Train Model
print("Starting Training...")
history = model.fit(train_data, validation_data=val_data, epochs=epochs)

# Save Model
model.save("cnn_model.h5")
print("Model saved successfully as 'cnn_model.h5'")

# Save Labels
import json
with open("class_labels.json", "w") as f:
    json.dump(train_data.class_indices, f)
print("Class labels saved to 'class_labels.json'")
