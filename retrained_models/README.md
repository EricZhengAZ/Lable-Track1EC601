# Retrained Models
This folder contains the models we trained.

Celtech101&256_100000 is a combination of Celtech 101 and Celtech 256.

Celtech101&256_20000 is based on Celtech101&256_100000 but has few personalized categories, such as great white shark and tiger shark.

# Usage
To use these models, you need first copy the *.pb and *.txt files to iOS_app/camera/data and import them into the project. Then modify the CameraExampleViewController.mm file to fit the model's name. Here is an example:

![alt text](https://github.com/EricZhengAZ/Lable-Track1EC601/blob/master/old_apps/iOS_app/camera/data/image01.png)
