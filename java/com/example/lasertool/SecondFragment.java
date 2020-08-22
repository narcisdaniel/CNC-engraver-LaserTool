package com.example.lasertool;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.EditText;
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

public class SecondFragment extends Fragment {
    private static final int GALLERY_REQUEST_CODE = 123;
    ImageView imageView;
    Button btnPick,btnRsz;
    TextView textView;
    Drawable drawable;
    Bitmap bitmap=null;
    Button btnSave;
    Uri imageData=null;
    String ImagePath;
    Uri URI;
    Bitmap newBitmap;
    Bitmap bitmap1;
    TextView resolution;
    boolean ok;
    EditText fieldWidth,fieldHeight;
    int nWidth;
    int nHeight;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.logor);
        btnPick = (Button) view.findViewById(R.id.uploadButton);
        btnSave = (Button) view.findViewById(R.id.saveButton);
        btnRsz=(Button) view.findViewById(R.id.resizeButton);
        resolution=(TextView) view.findViewById(R.id.textView);
        btnRsz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                dialogResize();
            }
        });
        btnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                Intent intent= new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Pick an image"), GALLERY_REQUEST_CODE);

            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(newBitmap!=null){
                ImagePath = MediaStore.Images.Media.insertImage(getActivity().getApplicationContext().getContentResolver(), newBitmap, "LaserTool_" + System.currentTimeMillis(), "LaserTool_" + System.currentTimeMillis());

                URI = Uri.parse(ImagePath);
                    Toast.makeText(getContext(), "Saved", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getContext(), "You need to upload an image.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    public static Bitmap dithering(Bitmap original){
        Bitmap finalImage = Bitmap.createBitmap(original.getWidth(),original.getHeight(),original.getConfig());
        int[][] newimg=new int[original.getWidth()][original.getHeight()];
        int colorPixel;
        int width=original.getWidth();
        int height= original.getHeight();

        for (int y=0;y<height;y++) {
            for (int x=0;x<width;x++) {
                colorPixel= original.getPixel(x,y);

                int a= Color.alpha(colorPixel);
                int r = Color.red(colorPixel);
                int g = Color.green(colorPixel);
                int b = Color.blue(colorPixel);
                int gr = (r+g+b)/3;
                newimg[x][y]=gr;
            }
        }

        int newpix,err;

        for (int y=0;y<height-1;y++) {
            for (int x=1;x<width-1;x++) {
                if(newimg[x][y]>127)
                    newpix=255;
                else
                    newpix=0;
                err=newimg[x][y]-newpix;
                newimg[x][y]=newpix;
                newimg[x+1][y]+=(int)(err*7.0/16.0);
                newimg[x-1][y+1]+=(int)(err*3.0/16.0);
                newimg[x][y+1]+=(int)(err*5.0/16.0);
                newimg[x+1][y+1]+=(int)(err*1.0/16.0);
            }
        }
        for (int y=0;y<height;y++) {
            for (int x=0;x<width;x++) {
                if(newimg[x][y]>127)
                    newimg[x][y]=255;
                else
                    newimg[x][y]=0;
            }
        }
        for (int y=0;y<height;y++) {
            for (int x=0;x<width;x++) {
                if(newimg[x][y]>=0 && newimg[x][y]<256) {
                    finalImage.setPixel(x,y, Color.argb(255, newimg[x][y],newimg[x][y], newimg[x][y]));
                }
            }
        }

        return finalImage;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == GALLERY_REQUEST_CODE && resultCode== RESULT_OK && data!= null ){
            imageData = data.getData();


            try {
                bitmap1 = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageData);
            } catch (IOException e) {
                e.printStackTrace();
            }

            newBitmap = dithering(bitmap1);
            imageView.setImageBitmap(newBitmap);
            resolution.setText(newBitmap.getWidth()+"x"+newBitmap.getHeight());
            ok=false;


        }
    }
    public void dialogResize(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View v=inflater.inflate(R.layout.resize_dialog, null);



        builder.setView(v)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        nWidth=0;
                        nHeight=0;

                        fieldWidth=(EditText) v.findViewById(R.id.width);
                        fieldHeight=(EditText) v.findViewById(R.id.height);

                        String sW=fieldWidth.getText().toString();
                        String sH=fieldHeight.getText().toString();
                        try {

                            nHeight = Integer.parseInt(sH.replaceAll("[\\D]", ""));
                        }catch (NumberFormatException nfe) {
                            nHeight=0;

                        }
                        try {
                            nWidth = Integer.parseInt(sW.replaceAll("[\\D]", ""));

                        }catch (NumberFormatException nfe) {
                            nWidth=0;

                        }

                        if(bitmap1!=null) {
                            if (nWidth == 0) {
                                if (nHeight == 0) {
                                    Toast.makeText(getContext(), "Invalid input.", Toast.LENGTH_LONG).show();
                                } else {
                                    nWidth = (bitmap1.getWidth() * nHeight) / bitmap1.getHeight();
                                }

                            }else{
                                if(nHeight==0)
                                    nHeight=(bitmap1.getHeight()*nWidth)/bitmap1.getWidth();
                            }
                            if(nHeight!=9 && nWidth!=0) {
                                bitmap1 = Bitmap.createScaledBitmap(bitmap1, nWidth, nHeight, true);

                                newBitmap = dithering(bitmap1);
                                imageView.setImageBitmap(newBitmap);

                                resolution.setText(newBitmap.getWidth() + "x" + newBitmap.getHeight());
                            }
                        }
                        else{
                            Toast.makeText(getContext(), "You need to upload an image.", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // remove the dialog from the screen
                    }
                })
                .show();

    }
}
