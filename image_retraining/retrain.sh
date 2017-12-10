#Copyright 2017 Jiahao Zhao
bazel-bin/tensorflow/examples/image_retraining/retrain \
--bottleneck_dir=/tmp/bottlenecks/ \
--how_many_training_steps 50000 \
--model_dir=/tmp/inception2 \
--output_graph=/tmp/retrained_graph.pb \
--output_labels=/tmp/retrained_labels.txt \
--image_dir $HOME/data_set
# Here you need change the dir to your own datasets

bazel build tensorflow/python/tools:optimize_for_inference
bazel-bin/tensorflow/python/tools/optimize_for_inference \
--input=/tmp/retrained_graph.pb \
--output=/tmp/optimized_graph.pb \
--input_names=Mul \
--output_names=final_result

bazel build tensorflow/tools/quantization:quantize_graph
bazel-bin/tensorflow/tools/quantization/quantize_graph --input=/tmp/optimized_graph.pb \
--output=/tmp/rounded_graph.pb \
--output_node_names=final_result \
--mode=weights_rounded

# -----------------------------------------------------------------------------
# This is for iOS appliction (memory mapping)
# bazel build tensorflow/contrib/util:convert_graphdef_memmapped_format
# bazel-bin/tensorflow/contrib/util/convert_graphdef_memmapped_format \
# --in_graph=/tmp/rounded_graph.pb --out_graph=/tmp/mmapped_graph.pb
# -----------------------------------------------------------------------------

# -----------------------------------------------------------------------------
# This is for testing trained model
# Here you need change the dir to your own testing images
# bazel-bin/tensorflow/examples/image_retraining/label_image \
# --output_layer=final_result:0 \
# --labels=/tmp/output_labels.txt \
# --image=$HOME/flower_photos/daisy/5547758_eea9edfd54_n.jpg \
# --graph=/tmp/mmapped_graph.pb 
# -----------------------------------------------------------------------------
