# RecBuy
## Team 11 - EC601 2017
Team members: Davide Lucchi, Honghao Zheng, Jiahao Zhao, Yanxing Zhang
## Introduction
Our Android App is able to Recognize objects in real time and it provides a custom link to Amazon.com to Buy the currently recognized object hence the name RecBuy! 
The App is based on the Tensorflow example [TF Classify](https://github.com/tensorflow/tensorflow/tree/master/tensorflow/examples/android) which uses the Google Inception  model to classify camera frames in real-time, displaying the top results in an overlay on the camera image.
To improve this model we are training our own dataset where we added more objects.
The script that we are using to train our own model can be found [here](https://github.com/tensorflow/tensorflow/blob/master/tensorflow/examples/image_retraining/retrain.py)
The models that we trained so far are available in the folder retrained_models.
The scripts used and a brief explanation about retraining can be found in the folder image_retraining.


# How to use
To build our project open the folder app/android with Android studio. All the dependacies should download automatically and the App will be ready to be installed.

If Android studio gives you an error message when it tries to launch the App then disable instant run in File -> Settings -> Build,Execution, Deployment -> Instant Run

# Content
### app
The android app, which is our final product.
### image retraining
This folder has the python code from Tensorflow on retraining models.
### retrained models
The models we trained for our product. We provide different versions if anyone wanna use them.
### use our model in iOS
The folder old_apps contains an iOS App that we are no longer using. It is a simple app for us to test our trained models at the beginning. It also gives a breif idea about how to use tensorflow trained models for iOS app.

# Reference: 

https://github.com/tensorflow/tensorflow/tree/master/tensorflow/examples/ios

https://github.com/tensorflow/tensorflow/tree/master/tensorflow/examples/android

https://www.tensorflow.org/tutorials/image_retraining

