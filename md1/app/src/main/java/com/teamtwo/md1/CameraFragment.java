package com.teamtwo.md1;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

public class CameraFragment extends Fragment {
    private static final String TAG = "CameraFragment";

    public ImageView thumbnailView;
    public MainActivity.ImageAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        thumbnailView = view.findViewById(R.id.thumbView);

        adapter = new MainActivity.ImageAdapter(this.getContext());
        GridView gridview = view.findViewById(R.id.gridview);
        gridview.setAdapter(adapter);

        return view;
    }


}
