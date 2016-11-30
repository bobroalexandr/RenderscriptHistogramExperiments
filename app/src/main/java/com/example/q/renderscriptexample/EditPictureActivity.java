package com.example.q.renderscriptexample;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.example.q.renderscriptexample.utils.RenderScriptImageEdit;

import java.io.IOException;

public class EditPictureActivity extends AppCompatActivity {
    public final static String BITMAP_URI_EXTRA = "BITMAP_URI_EXTRA";
    private Bitmap image = null;
    private Bitmap editedImage = null;

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_picture);

        if (getIntent().hasExtra(BITMAP_URI_EXTRA)) {
            Uri imageUri = Uri.parse(getIntent().getStringExtra(BITMAP_URI_EXTRA));
            try {
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
                finish();
            }
        } else {
            finish();
        }
        final ImageView iv_original_image = (ImageView) findViewById(R.id.original_image);
        imageView = (ImageView) findViewById(R.id.computed_image);
        iv_original_image.setImageBitmap(image);
        new HistogramEqualizationTask().execute();

    }

    private class HistogramEqualizationTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            editedImage = RenderScriptImageEdit.histogramEqualization2(image, EditPictureActivity.this);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            imageView.setImageBitmap(editedImage);
        }
    }

}
