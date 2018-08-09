package task.enozom.gmailcomposerapp.gmail.composer;

import android.net.Uri;

import com.google.firebase.storage.UploadTask;

/**
 * Created by Mayada on 8/8/2018.
 */

public class ComposerPresenter implements ComposerPresenterInterface {

    ComposerViewInterface composerViewInterface;
    ComposerInteractorInterface composerInteractorInterface;


    public ComposerPresenter(ComposerViewInterface composerViewInterface) {
        this.composerViewInterface = composerViewInterface;
        composerInteractorInterface = new ComposerInteractor(this);
        composerViewInterface.initView();
    }

    @Override
    public void presenterSaveFileToDatabase(String subjectToSave, String contentToSave, String attachmentURL) {
        composerInteractorInterface.interactorSaveFileToDatabase(subjectToSave, contentToSave, attachmentURL);
    }

    @Override
    public void presenterUploadFileToFirebaseStorage(Uri filePath, Boolean checkattachmentType) {
        composerInteractorInterface.interactorUploadFileToFirebaseStorage(filePath, checkattachmentType);
    }

    @Override
    public void presenterResponseTosaveTofirebaseStorage(UploadTask.TaskSnapshot myTaskSnapShot, Boolean acceptedFile) {
        composerViewInterface.viewResponseTosaveTofirebaseStorage(myTaskSnapShot, acceptedFile);
    }

    @Override
    public void presenterDismissDialog() {
        composerViewInterface.dismissProgressBar();
    }

    @Override
    public void presenterUploadingProgress(UploadTask.TaskSnapshot taskSnapshot) {
        composerViewInterface.showUploadingPercentage(taskSnapshot);
    }
}
