package task.enozom.gmailcomposerapp.gmail.composer;

import android.Manifest;
import android.app.ActionBar;
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
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.storage.UploadTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import task.enozom.gmailcomposerapp.R;
import task.enozom.gmailcomposerapp.gmail.composer.interfaces.ComposerPresenterInterface;
import task.enozom.gmailcomposerapp.gmail.composer.interfaces.ComposerViewInterface;

public class MainActivity extends AppCompatActivity implements ComposerViewInterface {


    @BindView(R.id.messageSubject)
    EditText messageEnteredSubject;

    @BindView(R.id.messageContent)
    EditText messageEnteredContent;

  //  @BindView(R.id.bottom_progress_bar)
    //ProgressBar bar;

    private ComposerPresenterInterface composerPresenterInterface;
    private UploadingAttachmentIntentService uploadingAttachmentIntentService;
    private Uri filePath;
    private Boolean acceptedFile = false;
    private Boolean checkAttachmentType = false;
    private UploadTask.TaskSnapshot myTaskSnapShot;
    private ProgressDialog progressDialog;

    private static final int PICK_IMAGE_REQUEST = 234;
    private static final int CAMERA_PIC_REQUEST = 1337;
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 3;

    private static final int REQ_CODE_EXTERNAL_STORAGE_PERMISSION = 77;
    private static final int REQ_CODE_CAMERA_PERMISSION = 88;


    Dialog dialog;
    Boolean checkCloseApp = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));
       // actionBar.setHomeButtonEnabled(true);

        //back button
       actionBar.setDisplayHomeAsUpEnabled(true);
        // action bar icon
        actionBar.setIcon(R.drawable.ic_action_name);
        actionBar.setDisplayShowHomeEnabled(true);

        actionBar.setTitle(R.string.compose);
        //actionBar.setDisplayUseLogoEnabled(true);


        // Status bar color
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));
        }

        composerPresenterInterface = new ComposerPresenter(this);
        uploadingAttachmentIntentService = new UploadingAttachmentIntentService(this);
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_composer_action, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.attachButton:
                attachFile();
                return true;

            case R.id.sendButton:
                sendfile();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void attachCameraImage() {
        // check CAMERA and WRITE EXTERNAL STORAGE PERMISSIONS
        if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA}, REQ_CODE_CAMERA_PERMISSION);
        }
    }

   private void attachVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
    }

   private void attachGalleryImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_CODE_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_CODE_EXTERNAL_STORAGE_PERMISSION);
        }

        if (requestCode == REQ_CODE_EXTERNAL_STORAGE_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
        }
    }

    private void attachFile() {

        if (checkInternetConnectivity()) {

            dialog = new Dialog(this);

            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);


            dialog.setContentView(R.layout.pop_up_dialog);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

            ImageView gallerySelected = dialog.findViewById(R.id.galleryImage);
            gallerySelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    attachGalleryImage();
                }
            });


            ImageView cameraSelected = dialog.findViewById(R.id.cameraImage);
            cameraSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    attachCameraImage();
                }
            });

            ImageView videoSelected = dialog.findViewById(R.id.VideoImageView);
            videoSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    attachVideo();
                }
            });

            dialog.getWindow().setGravity(Gravity.TOP);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();


        } else {
            Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.internet_access), Toast.LENGTH_LONG).show();
        }
    }


    private void sendfile() {

        if (checkInternetConnectivity()) {
            final String subjectToSave = messageEnteredSubject.getText().toString().trim();
            final String contentToSave = messageEnteredContent.getText().toString().trim();

            if (filePath != null && subjectToSave.length() != 0 && contentToSave.length() != 0 && acceptedFile == true) {
                composerPresenterInterface.presenterSaveFileToDatabase(subjectToSave, contentToSave, myTaskSnapShot.getDownloadUrl().toString());
                Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.saved_successfully_database), Toast.LENGTH_SHORT).show();
                finish();
                startActivity(getIntent());
            } else {
                Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.check_all_parameters), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.internet_access), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                checkAttachmentType = true;
            }
            if (requestCode == CAMERA_PIC_REQUEST || requestCode == PICK_IMAGE_REQUEST || requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                filePath = data.getData();
                uploadFileToFirbaseStorage();
            }
        } else {
            Toast.makeText(this, getApplicationContext().getResources().getString(R.string.attachedError), Toast.LENGTH_SHORT);
        }
    }

    private void uploadFileToFirbaseStorage() {
        if (filePath != null) {
            float fileSizeInMB = getFileSize(filePath);

            if (fileSizeInMB > 5.0) {
                Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.unsupported_size), Toast.LENGTH_LONG).show();
            } else {
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setTitle("Uploading");
                progressDialog.show();

                progressDialog.setCancelable(false);

                // Intent Service to upload attachement
                Intent intent = new Intent();
                intent.putExtra("filepath", filePath.toString());
                intent.putExtra("checkAttachmentType", checkAttachmentType);
                uploadingAttachmentIntentService.onHandleIntent(intent);


            }
        } else {
            Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.attachedError), Toast.LENGTH_LONG).show();
        }
    }

    private float getFileSize(Uri uploadedFile) {
        Cursor returnCursor =
                getContentResolver().query(uploadedFile, null, null, null, null);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        float fileSize = returnCursor.getFloat(sizeIndex);
        float fileSizeInMB = (fileSize / 1024) / 1024;
        return fileSizeInMB;
    }

    @Override
    public void viewResponseTosaveTofirebaseStorage(UploadTask.TaskSnapshot myTaskSnapShot, Boolean acceptedFile) {
        this.myTaskSnapShot = myTaskSnapShot;
        this.acceptedFile = acceptedFile;
    }

    @Override
    public void dismissProgressBar() {

        if (acceptedFile == true) {
            Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.uploaded_successfully), Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
            dialog.dismiss();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        checkCloseApp = true;
    }

    @Override
    public void showUploadingPercentage(UploadTask.TaskSnapshot taskSnapshot) {
        //displaying the upload progress
        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
       // bar.setProgress((int) progress);
        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
        progressDialog.setCancelable(false);
    }

    @Override
    public void viewResponseDeletionFromFirebaseStorage(Boolean checkDeletion) {
        if(checkDeletion == true) {
            acceptedFile = false;
            Toast.makeText(MainActivity.this,getApplicationContext().getResources().getString(R.string.successfully_deleted),Toast.LENGTH_SHORT).show();

        }else{
            Toast.makeText(MainActivity.this,getApplicationContext().getResources().getString(R.string.error_deletion),Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAttachmentFromFirebaseStorage(){
      composerPresenterInterface.presenterDeleteFileFromFirebaseStorage( myTaskSnapShot.getDownloadUrl().toString());
    }
}
