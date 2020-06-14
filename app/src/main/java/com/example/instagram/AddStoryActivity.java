package com.example.instagram;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class AddStoryActivity extends AppCompatActivity {

    private Uri mImageUri;
    String myUrl ="";
    private StorageTask storageTask;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        storageReference = FirebaseStorage.getInstance().getReference("story");

        CropImage.activity()
                .setAspectRatio(9,16)
                .start(AddStoryActivity.this);
    }

    private String getFIleExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void publishStory(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Cargando...");
        pd.show();

        if(mImageUri != null){
           final StorageReference imageReference = storageReference.child(System.currentTimeMillis()+"."+getFIleExtension(mImageUri));
           storageTask = imageReference.putFile(mImageUri);
           storageTask.continueWithTask(new Continuation() {
               @Override
               public Task<Uri> then(@NonNull Task task) throws Exception {
                   if(!task.isSuccessful()){
                       throw task.getException();
                   }
                   return imageReference.getDownloadUrl();
               }
           }).addOnCompleteListener(new OnCompleteListener<Uri>() {
               @Override
               public void onComplete(@NonNull Task<Uri> task) {
                   if(task.isSuccessful()){
                       Uri downloadUri = task.getResult();
                       myUrl = downloadUri.toString();

                       String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                       DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                               .child(myId);

                       String storyId = reference.push().getKey();
                       long timeEnd = System.currentTimeMillis() + 86400000;//1 dia

                       HashMap<String, Object> hashMap = new HashMap<>();
                       hashMap.put("imageurl", myUrl);
                       hashMap.put("timestart", ServerValue.TIMESTAMP);
                       hashMap.put("timeend", timeEnd);
                       hashMap.put("storyid", storyId);
                       hashMap.put("userid", myId);

                       reference.child(storyId).setValue(hashMap);
                       pd.dismiss();
                       finish();
                   }else{
                       sendMessege("¡Fallo al subir la historia!");
                   }
               }
           }).addOnFailureListener(new OnFailureListener() {
               @Override
               public void onFailure(@NonNull Exception e) {
                   sendMessege(e.getMessage());
               }
           });
        }else {
            sendMessege("No ha seleccionado ninguna imagen");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();

            publishStory();
        }else {
            sendToMainActivity();
        }

    }

    private void sendToMainActivity() {
        Intent intentMain = new Intent(AddStoryActivity.this, MainActivity.class);
        startActivity(intentMain);
        finish();
    }

    private void sendMessege(String msg) {
        Toast.makeText(AddStoryActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
