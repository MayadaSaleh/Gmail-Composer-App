package task.enozom.gmailcomposerapp.gmail.composer;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.storage.UploadTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import task.enozom.gmailcomposerapp.R;

public class MainActivity extends Activity implements ComposerViewInterface {


    @BindView(R.id.messageSubject)
    EditText messageEnteredSubject;

    @BindView(R.id.messageContent)
    EditText messageEnteredContent;

    @BindView(R.id.bottom_progress_bar)
    ProgressBar bar;

    private ComposerPresenterInterface composerPresenterInterface;

    private Uri filePath;
    private Boolean acceptedFile = false;
    private Boolean checkattachmentType = false;
    private UploadTask.TaskSnapshot myTaskSnapShot;
    private ProgressDialog progressDialog;

    private static final int PICK_IMAGE_REQUEST = 234;
    private static final int CAMERA_PIC_REQUEST = 1337;
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 3;

    private static final int REQ_CODE_EXTERNAL_STORAGE_PERMISSION = 77;
    private static final int REQ_CODE_CAMERA_PERMISSION = 88;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        composerPresenterInterface = new ComposerPresenter(this);
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

    @OnClick(R.id.attachButton)
    public void attachFile(View view) {

        if (checkInternetConnectivity()) {

            PopUpFragment popUpFragment = new PopUpFragment();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.animator.pop_enter, R.animator.pop_exit, R.animator.pop_enter, R.animator.pop_exit);
            fragmentTransaction.replace(R.id.main_layout, popUpFragment, "pop_up fragment");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        } else {
            Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.internet_access), Toast.LENGTH_LONG).show();
        }
    }


    @OnClick(R.id.sendButton)
    public void sendfile(View view) {

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
                checkattachmentType = true;
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

                composerPresenterInterface.presenterUploadFileToFirebaseStorage(filePath, checkattachmentType);
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
        Toast.makeText(MainActivity.this, getApplicationContext().getResources().getString(R.string.uploaded_successfully), Toast.LENGTH_LONG).show();
    }

    @Override
    public void dismissProgressBar() {
//
//            FragmentManager fragmentManager = getFragmentManager();
//        while (fragmentManager.getBackStackEntryCount() > 0) {
//            fragmentManager.popBackStackImmediate();
//        }


        progressDialog.dismiss();


        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction =  fragmentManager.beginTransaction();
      PopUpFragment  popUpFragment = (PopUpFragment) fragmentManager.findFragmentByTag("pop_up fragment");
        fragmentTransaction.remove(popUpFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            while (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStackImmediate();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void showUploadingPercentage(UploadTask.TaskSnapshot taskSnapshot) {
        //displaying the upload progress
        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
        bar.setProgress((int) progress);
        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
    }
}
