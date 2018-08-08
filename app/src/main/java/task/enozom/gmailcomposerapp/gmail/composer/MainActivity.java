package task.enozom.gmailcomposerapp.gmail.composer;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import task.enozom.gmailcomposerapp.R;

public class MainActivity extends Activity {


    @BindView(R.id.messageSubject)
    EditText messageEnteredSubject;

    @BindView(R.id.messageContent)
    EditText messageEnteredContent;


    @Nullable
    @BindView(R.id.galleryImage)
    ImageView GalleryImageView;

    @Nullable
    @BindView(R.id.VideoImageView)
    ImageView VideoImageView;

    Dialog attachmentPopUp;
    private static final int PICK_IMAGE_REQUEST = 234;
    private Uri filePath;


    private StorageReference storageReference;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        storageReference = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference("messages");
        attachmentPopUp = new Dialog(this);

    }



    @OnClick(R.id.attachButton)
    public void attachFile(View view){

        attachmentPopUp.setContentView(R.layout.attachment_popup_dialog);

      ImageView  cameraImageView = (ImageView) attachmentPopUp.findViewById(R.id.cameraImage);
        cameraImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attachGalleryImage();
            }
        });

        attachmentPopUp.getWindow().setGravity(Gravity.TOP);
        attachmentPopUp.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        attachmentPopUp.show();
    }



    @OnClick(R.id.sendButton)
    public void sendfile(View view){

        final String subjectToSave = messageEnteredSubject.getText().toString().trim();
        final String contentToSave = messageEnteredContent.getText().toString().trim();

        if (filePath != null &&  subjectToSave!= null && contentToSave!=null ) {
            Cursor returnCursor =
                    getContentResolver().query(filePath, null, null, null, null);

            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            float fileSize = returnCursor.getFloat(sizeIndex);
            float fileSizeInKB = fileSize / 1024;
            float  fileSizeInMB = fileSizeInKB / 1024;


            if(fileSizeInMB > 5.0){
                Toast.makeText(MainActivity.this,"Maximum image size is 5MB, please choose another one",Toast.LENGTH_LONG).show();
            }else {
                Log.i("image size", "image Sizeyuy   " + fileSizeInMB);

                //displaying progress dialog while image is uploading
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Uploading");
                progressDialog.show();

                //getting the storage reference
                StorageReference sRef = storageReference.child("messages/image.jpg");

                //adding the file to reference
                sRef.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //dismissing the progress dialog
                                progressDialog.dismiss();

                                //displaying success toast
                                Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();

                                //creating the upload object to store uploaded image details
                                MessagePojo uploadedMessage = new MessagePojo(subjectToSave, contentToSave, taskSnapshot.getDownloadUrl().toString());

                                //adding an upload to firebase database
                                String uploadId = mDatabase.push().getKey();
                                mDatabase.child(uploadId).setValue(uploadedMessage);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                //displaying the upload progress
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                            }
                        });
            }
        } else {
            Toast.makeText(getApplicationContext(), "Error in selected file  ", Toast.LENGTH_LONG).show();
        }

    }








    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

        }
    }







    private void attachGalleryImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
}
