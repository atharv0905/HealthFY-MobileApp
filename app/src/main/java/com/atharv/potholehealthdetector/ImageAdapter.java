package com.atharv.potholehealthdetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private Bitmap[] mImages; // Array of Bitmap objects

    public ImageAdapter(Context c, Bitmap[] images) {
        mContext = c;
        mImages = images;
    }

    public int getCount() {
        return mImages.length;
    }

    public Object getItem(int position) {
        return mImages[position];
    }

    public long getItemId(int position) {
        return position;
    }

    // Create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // If it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(430, 430)); // Adjust size as per your requirement
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setPadding(4, 8, 4, 8);
            imageView.setBackgroundResource(R.drawable.image_border);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageBitmap(mImages[position]);
        return imageView;
    }
}
