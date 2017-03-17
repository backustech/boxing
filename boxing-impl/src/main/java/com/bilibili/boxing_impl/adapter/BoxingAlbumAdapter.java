/*
 *  Copyright (C) 2017 Bilibili
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.bilibili.boxing_impl.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bilibili.boxing.BoxingMediaLoader;
import com.bilibili.boxing.model.entity.AlbumEntity;
import com.bilibili.boxing.model.entity.impl.ImageMedia;
import com.bilibili.boxing_impl.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Album window adapter.
 *
 * @author ChenSL
 */
public class BoxingAlbumAdapter extends RecyclerView.Adapter implements View.OnClickListener {
    private static final String UNKNOW_ALBUM_NAME = "?";

    private int mCurrentAlbumPos;

    private List<AlbumEntity> mAlums;
    private LayoutInflater mInflater;
    private OnAlbumClickListener mAlbumOnClickListener;

    private int MAX_IMAGE_SIZE= (int) (Resources.getSystem().getDisplayMetrics().density*50);

    public BoxingAlbumAdapter(Context context) {
        this.mAlums = new ArrayList<>();
        this.mAlums.add(AlbumEntity.createDefaultAlbum());
        this.mInflater = LayoutInflater.from(context);
    }

    public void setAlbumOnClickListener(OnAlbumClickListener albumOnClickListener) {
        this.mAlbumOnClickListener = albumOnClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AlbumViewHolder(mInflater.inflate(R.layout.layout_album_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final AlbumViewHolder albumViewHolder = (AlbumViewHolder) holder;
        albumViewHolder.mCoverImg.setImageResource(R.drawable.ic_default_image);
        final int adapterPos = holder.getAdapterPosition();
        final AlbumEntity album = mAlums.get(adapterPos);

        if (album != null && album.hasImages()) {
            albumViewHolder.mNameTxt.setText(album.mBucketName);
            ImageMedia media = (ImageMedia) album.mImageList.get(0);
            if (media != null) {
                ImageRequest request = ImageRequestBuilder
                        .newBuilderWithSource(Uri.fromFile(new File(media.getPath())))
                        .setAutoRotateEnabled(true)
                        .setLocalThumbnailPreviewsEnabled(true)
                        .setResizeOptions(new ResizeOptions(MAX_IMAGE_SIZE, MAX_IMAGE_SIZE))//不设置宽高，会加载大图导致卡顿
                        .build();
                DraweeController controller= Fresco.newDraweeControllerBuilder().setImageRequest(request).build();
                albumViewHolder.mCoverImg.setController(controller);
            }
            albumViewHolder.mLayout.setTag(adapterPos);
            albumViewHolder.mLayout.setOnClickListener(this);
            albumViewHolder.mCheckedImg.setVisibility(album.mIsSelected ? View.VISIBLE : View.GONE);
            albumViewHolder.mSizeTxt.setText(albumViewHolder.mSizeTxt.
                    getResources().getString(R.string.album_images_fmt, album.mCount));
        } else {
            albumViewHolder.mNameTxt.setText(UNKNOW_ALBUM_NAME);
            albumViewHolder.mSizeTxt.setVisibility(View.GONE);
        }
    }

    public void addAllData(List<AlbumEntity> alums) {
        mAlums.clear();
        mAlums.addAll(alums);
        notifyDataSetChanged();
    }

    public List<AlbumEntity> getAlums() {
        return mAlums;
    }

    public int getCurrentAlbumPos() {
        return mCurrentAlbumPos;
    }

    public void setCurrentAlbumPos(int currentAlbumPos) {
        mCurrentAlbumPos = currentAlbumPos;
    }

    public AlbumEntity getCurrentAlbum() {
        if (mAlums == null || mAlums.size() <= 0) {
            return null;
        }
        return mAlums.get(mCurrentAlbumPos);
    }

    @Override
    public int getItemCount() {
        return mAlums != null ? mAlums.size() : 0;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.album_layout) {
            if (mAlbumOnClickListener != null) {
                mAlbumOnClickListener.onClick(v, (Integer) v.getTag());
            }
        }
    }

    private static class AlbumViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView mCoverImg;
        TextView mNameTxt;
        TextView mSizeTxt;
        View mLayout;
        ImageView mCheckedImg;

        AlbumViewHolder(final View itemView) {
            super(itemView);
            mCoverImg = (SimpleDraweeView) itemView.findViewById(R.id.album_thumbnail);
            mNameTxt = (TextView) itemView.findViewById(R.id.album_name);
            mSizeTxt = (TextView) itemView.findViewById(R.id.album_size);
            mLayout = itemView.findViewById(R.id.album_layout);
            mCheckedImg = (ImageView) itemView.findViewById(R.id.album_checked);
        }
    }

    public interface OnAlbumClickListener {
        void onClick(View view, int pos);
    }
}
