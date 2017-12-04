### How retraining works

In order to be able to recognize a set of objects of our choice we used transfer learning applied to Tensorflow Inception model. The last layer of this model can be retrained on a new set of images which represent the new categories the model will be able to recognize. To do so tensorflow provides an already working script `image_retrain.py` which accepts as input the path to the folder with the new images and the name of the output files. The image folder must be organized such that all the images of an object are inside the same folder which name will be used for that object when recognized.
A more in depth explanation is given in the [Official Tensorflow page](https://www.tensorflow.org/tutorials/image_retraining)

### How to run the script

After having created a folder with the correct structure and images for the new classes, the script can be run with the command:

`python retrain.py   --output_labels output_labels.txt  --output_graph output_graph.pb  --image_dir images`

This program will generate the output files output_graph.pb  which is the new model and output_labels.txt which are the labels associated with it. 

This model though is big and slow and so it cannot be used effectively on a smarthphone which does not have great computational power. This problem is solved by running the script `strip_unused.py` which will generate different output models with different levels of optimization. This script analizes the model which is a graph and tries to built a minimum graph that can perform as the original.