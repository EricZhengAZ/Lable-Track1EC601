/*
 * Copyright 2016 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.demo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;
import java.util.Vector;
import org.tensorflow.demo.OverlayView.DrawCallback;
import org.tensorflow.demo.env.BorderedText;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.env.Logger;
import org.tensorflow.demo.R; // Explicit import needed for internal Google builds.

public class ClassifierActivity extends CameraActivity implements OnImageAvailableListener, SwipeRefreshLayout.OnRefreshListener {
  private static final Logger LOGGER = new Logger();

  protected static final boolean SAVE_PREVIEW_BITMAP = false;

 // private ResultsView resultsView;
  private TextView best_item;

  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private long lastProcessingTimeMs;

  // These are the settings for the original v1 Inception model. If you want to
  // use a model that's been produced from the TensorFlow for Poets codelab,
  // you'll need to set IMAGE_SIZE = 299, IMAGE_MEAN = 128, IMAGE_STD = 128,
  // INPUT_NAME = "Mul", and OUTPUT_NAME = "final_result".
  // You'll also need to update the MODEL_FILE and LABEL_FILE paths to point to
  // the ones you produced.
  //
  // To use v3 Inception model, strip the DecodeJpeg Op from your retrained
  // model first:
  //
  // python strip_unused.py \
  // --input_graph=<retrained-pb-file> \
  // --output_graph=<your-stripped-pb-file> \
  // --input_node_names="Mul" \
  // --output_node_names="final_result" \
  // --input_binary=true
  private static final int INPUT_SIZE = 299;
  private static final int IMAGE_MEAN = 128;
  private static final float IMAGE_STD = 128;
  private static final String INPUT_NAME = "Mul";
  private static final String OUTPUT_NAME = "final_result";


  private static final String MODEL_FILE = "file:///android_asset/rounded_graph.pb";
  private static final String LABEL_FILE ="file:///android_asset/retrained_labels.txt";


  private static final boolean MAINTAIN_ASPECT = true;

  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);


  private Integer sensorOrientation;
  private Classifier classifier;
  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;


  private BorderedText borderedText;

  private String base_link = "http://www.amazon.com/s/ref=nb_sb_noss_2?url=search-alias%3Daps&field-keywords=";
  private String buy_link;

    private WebView webView = null;

    private SwipeRefreshLayout mySwipeRefreshLayout = null;

    @Override
    protected void onCreate(final Bundle onSavedInstanceState)
    {
        super.onCreate(onSavedInstanceState);



    }

  @Override
  protected int getLayoutId() {
    return R.layout.camera_connection_fragment;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

  private static final float TEXT_SIZE_DIP = 10;



    @Override
    public void onBackPressed() {
        // If the back button is pressed then exit the app
        back_pressed();
    }

    void back_pressed()
    {
        if (webView == null || webView.getVisibility() == View.INVISIBLE) {
            signOut();
        }
        else if (webView.canGoBack()) {
            webView.goBack();
        }
        else
        {
            webView.setVisibility(View.INVISIBLE);
            findViewById(R.id.swipe_container).setVisibility(View.INVISIBLE);
            webView.clearHistory();
            webView.clearCache(true);
            webView.removeAllViews();
            webView.destroyDrawingCache();
            webView.destroy();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                back_pressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    classifier =
        TensorFlowImageClassifier.create(
            getAssets(),
            MODEL_FILE,
            LABEL_FILE,
            INPUT_SIZE,
            IMAGE_MEAN,
            IMAGE_STD,
            INPUT_NAME,
            OUTPUT_NAME);

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    final Display display = getWindowManager().getDefaultDisplay();
    final int screenOrientation = display.getRotation();

    LOGGER.i("Sensor orientation: %d, Screen orientation: %d", rotation, screenOrientation);

    sensorOrientation = rotation + screenOrientation;

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    croppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Config.ARGB_8888);

    frameToCropTransform = ImageUtils.getTransformationMatrix(
        previewWidth, previewHeight,
        INPUT_SIZE, INPUT_SIZE,
        sensorOrientation, MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);

    addCallback(
        new DrawCallback() {
          @Override
          public void drawCallback(final Canvas canvas) {
            renderDebug(canvas);
          }
        });
  }

  @Override
  protected void processImage() {
    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);

    // For examining the actual TF input.
    if (SAVE_PREVIEW_BITMAP) {
      ImageUtils.saveBitmap(croppedBitmap);
    }
    runInBackground(
        new Runnable() {
          @Override
          public void run() {
            final long startTime = SystemClock.uptimeMillis();
            final List<Classifier.Recognition> results = classifier.recognizeImage(croppedBitmap);
            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
            LOGGER.i("Detect: %s", results);
            cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
            /*if (resultsView == null) {
              resultsView = (ResultsView) findViewById(R.id.results);
            }*/
            //resultsView.setResults(results);
            Float max_score = 0f;
            if (results.size() > 0) {
              Classifier.Recognition best = results.get(0);
              for (Classifier.Recognition r : results) {
                if (r.getConfidence() > max_score) {
                  max_score = r.getConfidence();
                  best = r;
                }
              }

              if (max_score > 0.30) {
                buy_link = base_link + best.getTitle();

                final Classifier.Recognition best_p = best;

                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {

                    //stuff that updates ui
                    // set the best item
                    best_item = (TextView) findViewById(R.id.best_item);
                    if (best_item != null && best_p != null)
                        best_item.setText(best_p.getTitle());

                  }
                });
              }
              else
              {
                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {

                    //stuff that updates ui
                    // set the best item
                    best_item = (TextView) findViewById(R.id.best_item);
                    if (best_item != null)
                        best_item.setText("");
                  }
                });
              }
            }

            requestRender();
            readyForNextImage();
          }
        });
  }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mySwipeRefreshLayout.setRefreshing(false);
            }
        }, 5000);
    }

  public void buy(View view) {
      if (buy_link == null || buy_link.compareTo("") == 0)
          return;

      //Intent i = new Intent(Intent.ACTION_WEB_SEARCH,Uri.parse(buy_link));
      //startActivityForResult(i,1000);

      if (webView == null)
          webView = (WebView) findViewById(R.id.webview);

      if (mySwipeRefreshLayout == null) {
          mySwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

          mySwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                  android.R.color.background_light);

          mySwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
              @Override
              public void onRefresh() {
                  System.out.println("refreshing");
                  webView.loadUrl(buy_link);
              }
          });
      }




      webView.setWebViewClient(new MyWebViewClient());
      webView.getSettings().setJavaScriptEnabled(true);
      webView.setVisibility(View.VISIBLE);
      findViewById(R.id.swipe_container).setVisibility(View.VISIBLE);
      webView.loadUrl(buy_link);

      mySwipeRefreshLayout.post(new Runnable() {
          @Override
          public void run() {
              mySwipeRefreshLayout.setRefreshing(true);
          }
      });

  }



    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            mySwipeRefreshLayout.setRefreshing(false);
            buy_link = url;
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon)
        {
            mySwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mySwipeRefreshLayout.setRefreshing(true);
                }
            });

            super.onPageStarted(view,url,favicon);
        }
    }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 1000) {
      //Intent i= new Intent(getBaseContext(),ClassifierActivity.class);
      //startActivity(i);
    }
  }

  @Override
  public void onSetDebug(boolean debug) {
    classifier.enableStatLogging(debug);
  }

  private void renderDebug(final Canvas canvas) {
    if (!isDebug()) {
      return;
    }
    final Bitmap copy = cropCopyBitmap;
    if (copy != null) {
      final Matrix matrix = new Matrix();
      final float scaleFactor = 2;
      matrix.postScale(scaleFactor, scaleFactor);
      matrix.postTranslate(
          canvas.getWidth() - copy.getWidth() * scaleFactor,
          canvas.getHeight() - copy.getHeight() * scaleFactor);
      canvas.drawBitmap(copy, matrix, new Paint());

      final Vector<String> lines = new Vector<String>();
      if (classifier != null) {
        String statString = classifier.getStatString();
        String[] statLines = statString.split("\n");
        for (String line : statLines) {
          lines.add(line);
        }
      }

      lines.add("Frame: " + previewWidth + "x" + previewHeight);
      lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
      lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
      lines.add("Rotation: " + sensorOrientation);
      lines.add("Inference time: " + lastProcessingTimeMs + "ms");

      borderedText.drawLines(canvas, 10, canvas.getHeight() - 10, lines);
    }
  }
}
