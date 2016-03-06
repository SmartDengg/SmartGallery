package com.smartdengg.smartgallery.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.smartdengg.smartgallery.R;

public class MainActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_layout);
    ButterKnife.bind(MainActivity.this);
  }

  @NonNull @OnClick(R.id.gallery_button) protected void onGalleryClick() {
    GalleryActivity.navigateToGallery(MainActivity.this);
    overridePendingTransition(0, 0);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    ButterKnife.unbind(MainActivity.this);
  }
}
