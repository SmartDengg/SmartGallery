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
import rx.Observer;
import rx.exceptions.Exceptions;
import rx.functions.Func2;

/**
 * Created by Joker on 2016/6/27.
 */
class CursorFactory implements Func2<Cursor, Observer<? super Cursor>, Cursor> {

  private CursorFactory() {
  }

  public static CursorFactory created() {
    return new CursorFactory();
  }

 /* @Override public Observable<Cursor> call(final Cursor cursor) {
    return Observable.create(new Observable.OnSubscribe<Cursor>() {
      @Override public void call(Subscriber<? super Cursor> subscriber) {

        try {
          while (cursor.moveToNext() && !subscriber.isUnsubscribed()) {
            /** exclude .gif *//*
            if (cursor.getString(cursor.getColumnIndexOrThrow(GalleryUseCase.GALLERY_PROJECTION[0])).endsWith(".gif")) {
              continue;
            }

            subscriber.onNext(cursor);
          }
        } catch (Exception e) {
          Exceptions.throwIfFatal(e);
          if (!subscriber.isUnsubscribed()) subscriber.onError(e);
        } finally {
          if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
        }
      }
    });
  }*/

  @Override public Cursor call(Cursor cursor, Observer<? super Cursor> observer) {
    if (!cursor.isClosed() && cursor.moveToNext()) {
      try {
        /** exclude .gif */
        if (!cursor.getString(cursor.getColumnIndexOrThrow(GalleryUseCase.GALLERY_PROJECTION[0]))
            .endsWith(".gif")) {
          observer.onNext(cursor);
        }
      } catch (Exception e) {
        Exceptions.throwIfFatal(e);
        observer.onError(e);
      }
    } else {
      observer.onCompleted();
    }

    return cursor;
  }
}
