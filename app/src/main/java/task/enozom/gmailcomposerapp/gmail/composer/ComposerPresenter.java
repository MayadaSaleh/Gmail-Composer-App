package task.enozom.gmailcomposerapp.gmail.composer;

import android.net.Uri;

import com.google.firebase.storage.UploadTask;

import task.enozom.gmailcomposerapp.gmail.composer.interfaces.ComposerInteractorInterface;
import task.enozom.gmailcomposerapp.gmail.composer.interfaces.ComposerPresenterInterface;
import task.enozom.gmailcomposerapp.gmail.composer.interfaces.ComposerViewInterface;

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

}
