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

import android.content.Context;
import com.smartdengg.rxgallery.entity.FolderEntity;
import com.smartdengg.rxgallery.entity.ImageEntity;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import rx.Observable;
import rx.functions.Func1;
import rx.observables.GroupedObservable;

/**
 * Created by SmartDengg on 2016/3/5.
 */
public class GalleryMapUseCase extends GalleryUseCase<Map<String, FolderEntity>> {

  private List<ImageEntity> allPictures = new ArrayList<>();

  private GalleryMapUseCase(Context context, String name) {
    super(context, name);
  }

  public static GalleryMapUseCase createdUseCase(Context context) {
    return createdUseCase(context, null);
  }

  public static GalleryMapUseCase createdUseCase(Context context, String name) {
    return new GalleryMapUseCase(context, name);
  }

  @Override public final Observable<Map<String, FolderEntity>> hunt(
      Observable<ImageEntity> entityObservable) {

    return entityObservable.groupBy(new GroupByFunc())
        .concatMap(new ContactMapFunc(this.folderEntity, this.allPictures))
        .last()
        .map(new Func1<Map<String, FolderEntity>, Map<String, FolderEntity>>() {
          @Override public Map<String, FolderEntity> call(Map<String, FolderEntity> entityMap) {

            FolderEntity allFolderEntity = GalleryMapUseCase.this.folderEntity.newInstance();
            allFolderEntity.setFolderName((name != null && !name.isEmpty()) ? name : DEFAULT_NAME);
            allFolderEntity.setFolderPath("");
            allFolderEntity.setThumbPath(allPictures.get(0).getImagePath());
            allFolderEntity.setImageEntities(allPictures);
            entityMap.put((name != null && !name.isEmpty()) ? name : DEFAULT_NAME, allFolderEntity);

            /*根据文件夹照片数量降序*/
            Map<String, FolderEntity> map = new TreeMap<>(new SortComparator(entityMap));
            for (Map.Entry<String, FolderEntity> entry : entityMap.entrySet()) {
              map.put(entry.getKey(), entry.getValue());
            }

            entityMap.clear();

            return Collections.unmodifiableMap(map);
          }
        })
        .compose(IoScheduler.<Map<String, FolderEntity>>apply());
  }

  private static final class GroupByFunc implements Func1<ImageEntity, String> {

    @Override public String call(ImageEntity imageEntity) {
      File parentFile = new File(imageEntity.getImagePath()).getParentFile();
      return parentFile.getAbsolutePath();
    }
  }

  private static final class ContactMapFunc implements
      Func1<GroupedObservable<String, ImageEntity>, Observable<Map<String, FolderEntity>>> {

    private FolderEntity instance;
    private List<ImageEntity> pictures;
    private Map<String, FolderEntity> folderEntityMap = new HashMap<>();

    public ContactMapFunc(FolderEntity instance, List<ImageEntity> pictures) {
      this.instance = instance;
      this.pictures = pictures;
    }

    @Override public Observable<Map<String, FolderEntity>> call(
        final GroupedObservable<String, ImageEntity> groupedObservable) {

      return groupedObservable.map(new Func1<ImageEntity, Map<String, FolderEntity>>() {
        @Override public Map<String, FolderEntity> call(ImageEntity imageEntity) {

          /*All pictures*/
          pictures.add(imageEntity);

          String key = groupedObservable.getKey();
          File folderFile = new File(imageEntity.getImagePath()).getParentFile();

          if (!folderEntityMap.containsKey(key)) {
            FolderEntity clone = instance.newInstance();
            clone.setFolderName(folderFile.getName());
            clone.setFolderPath(folderFile.getAbsolutePath());
            clone.setThumbPath(imageEntity.getImagePath());
            clone.addImage(imageEntity);
            folderEntityMap.put(key, clone);
          } else {
            folderEntityMap.get(key).addImage(imageEntity);
          }

          return folderEntityMap;
        }
      });
    }
  }

  private static final class SortComparator implements Comparator<String> {

    Map<String, FolderEntity> base;

    private SortComparator(Map<String, FolderEntity> base) {
      this.base = base;
    }

    @Override public int compare(String lhs, String rhs) {

      FolderEntity lhsEntity = base.get(lhs);
      FolderEntity rhsEntity = base.get(rhs);

      int lhsCount = lhsEntity.getImageCount();
      int rhsCount = rhsEntity.getImageCount();

      return (rhsCount < lhsCount) ? -1 : ((lhsCount == rhsCount) ? 0 : 1);
    }
  }
}
