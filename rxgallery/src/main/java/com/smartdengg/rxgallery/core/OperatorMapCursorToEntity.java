/*
 * Copyright 2016 SmartDengg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.smartdengg.rxgallery.core;

import android.database.Cursor;
import android.os.Build;
import com.smartdengg.rxgallery.entity.ImageEntity;
import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.exceptions.OnErrorThrowable;
import rx.plugins.RxJavaHooks;

/**
 * Created by SmartDengg on 2016/7/12.
 */
public class OperatorMapCursorToEntity implements Observable.Operator<ImageEntity, Cursor> {

  private String[] galleryProjection;

  public OperatorMapCursorToEntity(String[] galleryProjection) {
    this.galleryProjection = galleryProjection;
  }

  @Override public Subscriber<? super Cursor> call(Subscriber<? super ImageEntity> child) {
    MapCursorSubscriber parent = new MapCursorSubscriber(child, galleryProjection);
    child.add(parent);
    return parent;
  }

  private static final class MapCursorSubscriber extends Subscriber<Cursor> {

    private ImageEntity parent = new ImageEntity();
    private String[] galleryProjection;

    private Subscriber<? super ImageEntity> actual;
    boolean done;

    public MapCursorSubscriber(Subscriber<? super ImageEntity> child, String[] galleryProjection) {
      this.actual = child;
      this.galleryProjection = galleryProjection;
    }

    @Override public void onCompleted() {
      if (done) return;
      actual.onCompleted();
    }

    @Override public void onError(Throwable e) {

      if (done) {
        RxJavaHooks.onError(e);
        return;
      }
      done = true;
      actual.onError(e);
    }

    @Override public void onNext(Cursor cursor) {

      if (isUnsubscribed()) return;

      ImageEntity result;

      try {
        result = this.convertToImageEntity(cursor);
      } catch (Exception ex) {
        Exceptions.throwIfFatal(ex);
        unsubscribe();
        onError(OnErrorThrowable.addValueAsLastCause(ex, cursor));
        return;
      }

      actual.onNext(result);
    }

    @Override public void setProducer(Producer p) {
      actual.setProducer(p);
    }

    private ImageEntity convertToImageEntity(Cursor cursor) {
      String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(galleryProjection[0]));
      String imageName = cursor.getString(cursor.getColumnIndexOrThrow(galleryProjection[1]));
      long addDate = cursor.getLong(cursor.getColumnIndexOrThrow(galleryProjection[2]));
      long id = cursor.getLong(cursor.getColumnIndexOrThrow(galleryProjection[3]));
      String title = cursor.getString(cursor.getColumnIndexOrThrow(galleryProjection[4]));
      String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(galleryProjection[5]));

      long size = cursor.getLong(cursor.getColumnIndexOrThrow(galleryProjection[6]));
      long modifyDate = cursor.getLong(cursor.getColumnIndexOrThrow(galleryProjection[7]));

      ImageEntity clone = parent.newInstance();

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        String width = cursor.getString(cursor.getColumnIndexOrThrow(galleryProjection[8]));
        String Height = cursor.getString(cursor.getColumnIndexOrThrow(galleryProjection[9]));
        clone.setWidth(width);
        clone.setHeight(Height);
      } else {
        clone.setWidth("0");
        clone.setHeight("0");
      }

      clone.setImagePath(imagePath);
      clone.setImageName(imageName);
      clone.setAddDate(addDate);
      clone.setId(id);
      clone.setTitle(title);
      clone.setMimeType(mimeType);
      clone.setSize(size);
      clone.setModifyDate(modifyDate);
      return clone;
    }
  }
}
