import tensorflow as tf

# Load the trained model
model = tf.keras.models.load_model('drug_classification_model.h5')

# Convert the model to TFLite
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

# Save the TFLite model
with open('drug_classification_model.tflite', 'wb') as f:
    f.write(tflite_model)
