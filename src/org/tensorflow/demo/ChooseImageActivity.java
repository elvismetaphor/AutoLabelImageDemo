package org.tensorflow.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChooseImageActivity extends AppCompatActivity {

    private static final int PICK_FROM_FILE = 0;

    private static final int NUM_CLASSES = 1001;
    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input:0";
    private static final String OUTPUT_NAME = "output:0";

    private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/imagenet_comp_graph_label_strings.txt";

    private TensorFlowImageClassifier classifier;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.chosen_image)
    ImageView chosenImage;

    @BindView(R.id.chosen_image_label)
    TextView chosenImageLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        initializeClassifier();
    }

    private void initializeClassifier() {
        classifier = new TensorFlowImageClassifier();
        try {
            classifier.initializeTensorFlow(
                    getAssets(), MODEL_FILE, LABEL_FILE, NUM_CLASSES, INPUT_SIZE, IMAGE_MEAN, IMAGE_STD,
                    INPUT_NAME, OUTPUT_NAME);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.fab)
    void choosePictureFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Choose an image"), PICK_FROM_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        handleChosenImage(data);
    }

    private void handleChosenImage(Intent data) {
        Uri imageUri = data.getData();
        try (InputStream is = getContentResolver().openInputStream(imageUri)) {
            Bitmap image = BitmapFactory.decodeStream(is);
            List<Classifier.Recognition> results = classifier.recognizeImage(
                    Bitmap.createScaledBitmap(image, INPUT_SIZE, INPUT_SIZE, false));
            chosenImageLabel.setText(results.get(0).getTitle());
            chosenImage.setImageBitmap(image);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
