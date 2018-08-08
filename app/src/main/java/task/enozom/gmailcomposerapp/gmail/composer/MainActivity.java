package task.enozom.gmailcomposerapp.gmail.composer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

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

        attachmentPopUp.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        attachmentPopUp.show();
    }





    private void attachGalleryImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
}
