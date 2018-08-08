package task.enozom.gmailcomposerapp.gmail.composer;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import task.enozom.gmailcomposerapp.R;
import task.enozom.gmailcomposerapp.splash.screen.SplashScreen;

public class MainActivity extends Activity implements ComposerViewInterface{


    @BindView(R.id.messageSubject)
    EditText messageEnteredSubject;

    @BindView(R.id.messageContent)
    EditText messageEnteredContent;

    @BindView(R.id.bottom_progress_bar)
    ProgressBar bar;

    private ComposerPresenterInterface composerPresenterInterface;

    Dialog attachmentPopUp;

    private static final int PICK_IMAGE_REQUEST = 234;
    private static final int CAMERA_PIC_REQUEST = 1337;
    private static final int REQUEST_TAKE_GALLERY_VIDEO =3;

    private Uri filePath;
    private Boolean acceptedFile = false;
    private Boolean checkattachmentType = false;
    private UploadTask.TaskSnapshot myTaskSnapShot;
    //View  view;

   private ProgressDialog progressDialog = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        view = View.inflate(getApplicationContext(), R.layout.attachment_popup_dialog, null);

        composerPresenterInterface = new ComposerPresenter(this);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA},0);
        }

        attachmentPopUp = new Dialog(this);

    }


    //Check Internet Connectivity
    public boolean checkInternetConnectivity() {
        boolean status;
        ConnectivityManager connectivityManager = ((ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE));
        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {
            status = true;
        } else {
            status = false;
        }
        return status;
    }

    @OnClick(R.id.attachButton)
    public void attachFile(View view) {

        if (checkInternetConnectivity()) {
            attachmentPopUp.setContentView(R.layout.attachment_popup_dialog);

            ImageView cameraImageView = (ImageView) attachmentPopUp.findViewById(R.id.cameraImage);
            cameraImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    attachCameraImage();
                }
            });

            ImageView galleryImageView = (ImageView) attachmentPopUp.findViewById(R.id.galleryImage);
            galleryImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    attachGalleryImage();
                }
            });

            ImageView videoImageView = (ImageView) attachmentPopUp.findViewById(R.id.VideoImageView);
            videoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    attachVideo();
                }
            });

            attachmentPopUp.getWindow().setGravity(Gravity.TOP);
            attachmentPopUp.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            attachmentPopUp.show();
        }else {
            Toast.makeText(MainActivity.this,getApplicationContext().getResources().getString(R.string.internet_access),Toast.LENGTH_LONG).show();

        }
    }


    @OnClick(R.id.sendButton)
    public void sendfile(View view) {

        if (checkInternetConnectivity()){
        final String subjectToSave = messageEnteredSubject.getText().toString().trim();
        final String contentToSave = messageEnteredContent.getText().toString().trim();

        if (filePath != null && subjectToSave.length()>0 && contentToSave.length()>0 && acceptedFile ==true) {
            composerPresenterInterface.presenterSaveFileToDatabase(subjectToSave,contentToSave,myTaskSnapShot.getDownloadUrl().toString());
            Toast.makeText(MainActivity.this,"Sucessfully saved to Firebase database",Toast.LENGTH_SHORT).show();

        }else{
            Toast.makeText(MainActivity.this,getApplicationContext().getResources().getString(R.string.check_all_parameters),Toast.LENGTH_LONG).show();
        }
        }else{
            Toast.makeText(MainActivity.this,getApplicationContext().getResources().getString(R.string.internet_access),Toast.LENGTH_LONG).show();
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {

            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                checkattachmentType = true;

            } if (requestCode == CAMERA_PIC_REQUEST || requestCode == PICK_IMAGE_REQUEST || requestCode == REQUEST_TAKE_GALLERY_VIDEO) {

                filePath = data.getData();
                uploadFileToFirbaseStorage();
            }
        }
            else {
                Toast.makeText(this, "Error in attaching file", Toast.LENGTH_SHORT);
            }
        }


        private void uploadFileToFirbaseStorage() {
            if (filePath != null) {
                float fileSizeInMB= getFileSize(filePath);

                if (fileSizeInMB > 5.0) {
                    Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.unsupported_size), Toast.LENGTH_LONG).show();
                } else {
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setTitle("Uploading");
                    progressDialog.show();


                    composerPresenterInterface.presenterUploadFileToFirebaseStorage(filePath,checkattachmentType);
              }
                } else{
                    Toast.makeText(getApplicationContext(), "Error in selected file", Toast.LENGTH_LONG).show();
                }
        }


        private float getFileSize(Uri uploadedFile){
            Cursor returnCursor =
                    getContentResolver().query(uploadedFile, null, null, null, null);

            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            float fileSize = returnCursor.getFloat(sizeIndex);
            float fileSizeInMB = (fileSize / 1024) / 1024;
            return fileSizeInMB;
        }

    private void attachCameraImage() {

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    }

    private void attachGalleryImage(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

   private void attachVideo(){
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Video"),REQUEST_TAKE_GALLERY_VIDEO);
    }

    @Override
    public void initView() {
      //  ButterKnife.bind(this, view);
        ButterKnife.bind(this);
    }

    @Override
    public void viewResponseTosaveTofirebaseStorage(UploadTask.TaskSnapshot myTaskSnapShot, Boolean acceptedFile) {
        this.myTaskSnapShot=myTaskSnapShot;
        this.acceptedFile=acceptedFile;
        Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.uploaded_successfully), Toast.LENGTH_LONG).show();

    }

    @Override
    public void dismissProgressBar() {
        progressDialog.dismiss();
        attachmentPopUp.dismiss();
    }

    @Override
    public void showUploadingPercentage(UploadTask.TaskSnapshot taskSnapshot) {


        //displaying the upload progress
        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
        bar.setProgress((int)progress);
        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");

    }
}
