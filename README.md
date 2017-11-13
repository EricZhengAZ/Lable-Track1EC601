# Lable-Track1EC601
## Team 11 - EC601 2017

## Team members: Davide Lucchi, Honghao Zheng, Jiahao Zhao, Yanxing Zhang

Our Android App is able to recognize objects in real time and it provides a custom link to Amazon.com to buy the currently recognized object. 
The App is based on the Tensorflow example [TF Classify](https://github.com/tensorflow/tensorflow/tree/master/tensorflow/examples/android) which uses the Google Inception  model to classify camera frames in real-time, displaying the top results in an overlay on the camera image.
To improve this model we are training our own dataset where we added more objects.
The models that we trained so far are available in the folder retrained_models.

The folder old_apps contains an iOS App that we are no longer using. It is a simple app for us to test our trained models at the beginning.

# How to use
To build our project open the folder app/android with Android studio. All the dependacies should download automatically and the App will be ready to be installed.

# Reference: 

https://github.com/tensorflow/tensorflow/tree/master/tensorflow/examples/ios

https://github.com/tensorflow/tensorflow/tree/master/tensorflow/examples/android

https://www.tensorflow.org/tutorials/image_retraining

