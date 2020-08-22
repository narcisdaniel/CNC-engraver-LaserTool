package com.example.lasertool;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.io.File;
import java.io.IOException;


import static android.app.Activity.RESULT_OK;
import static androidx.core.content.PermissionChecker.checkSelfPermission;

public class FirstFragment extends Fragment {


    ImageView imageView;
    Button btnPick;
    TextView textView;
    Button btnSave;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageView = (ImageView) view.findViewById(R.id.imageView);
        btnPick = (Button) view.findViewById(R.id.uploadButton);
        textView = (TextView) view.findViewById(R.id.textView);
        btnSave = (Button) view.findViewById(R.id.saveButton);
        imageView.setImageResource(R.drawable.logor);
        btnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                Intent i = new Intent(getActivity(), DeviceListActivity.class);
                startActivity(i);
            }
        });

    }

}
