package com.example.firebasejournalapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import util.JournalUser;

public class SignupActivity extends AppCompatActivity {

    EditText password_create;
    Button Createbtn;
    EditText email_create;

    EditText username_create;

    //firebase auth
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //firebase connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();


        Createbtn = findViewById(R.id.Create_btn);
        email_create = findViewById(R.id.email_create);
        password_create = findViewById(R.id.password_create);
        username_create = findViewById(R.id.userName_create_ET);

        //authentication:
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if(currentUser!=null){
                    //already logged in user

                }else{
                    //no user has logged
                }
            }
        };

        Createbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(email_create.getText().toString()) && (!TextUtils.isEmpty(password_create.getText().toString()))){
                    String email = email_create.getText().toString().trim();
                    String password = password_create.getText().toString().trim();
                    String username = username_create.getText().toString().trim();


                    CreateUserEmailAccount(email, password, username);
                }
                else{
                    Toast.makeText(SignupActivity.this, "some fields might be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void CreateUserEmailAccount(String email, String password, final String username) {
        if(!TextUtils.isEmpty(email_create.getText().toString()) && (!TextUtils.isEmpty(password_create.getText().toString()))){
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        //take the user to next activity cause it has been registered
                        currentUser = firebaseAuth.getCurrentUser();
                        assert currentUser!=null;
                        final String currentUserId = currentUser.getUid();
                        Map<String, String> userObj = new HashMap<>();
                        userObj.put("userId", currentUserId);
                        userObj.put("username", username);

                        //adding users to firestore
                        collectionReference.add(userObj).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(Objects.requireNonNull(task.getResult()).exists()){
                                            String name = task.getResult().getString("username");

                                            JournalUser journalUser = JournalUser.getInstance();
                                            journalUser.setUserId(currentUserId);
                                            journalUser.setUsername(name);

                                            //if user is registered successfully then move user to journal activity
                                            Intent i = new Intent(SignupActivity.this, AddJournalActivity.class);
                                            i.putExtra("username", name);
                                            i.putExtra("userId", currentUserId);

                                            startActivity(i);
                                        }else{

                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //toast message informing the user about the failure
                                        Toast.makeText(SignupActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                }
            });

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}