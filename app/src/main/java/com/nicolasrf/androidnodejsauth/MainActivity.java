package com.nicolasrf.androidnodejsauth;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.material.button.MaterialButton;
import com.nicolasrf.androidnodejsauth.Retrofit.INodeJS;
import com.nicolasrf.androidnodejsauth.Retrofit.RetrofitClient;
import com.rengwuxian.materialedittext.MaterialEditText;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    INodeJS myAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    MaterialEditText emailEditText, passwordEditText;
    MaterialButton registerButton, loginButton;

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init API
        Retrofit retrofit = RetrofitClient.getInstance();
        myAPI = retrofit.create(INodeJS.class);

        //View
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);

        //Event
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser(emailEditText.getText().toString(),passwordEditText.getText().toString());
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser(emailEditText.getText().toString(),passwordEditText.getText().toString());
            }
        });
    }

    private void registerUser(final String email, final String password) {
        //With register, we need one more step to enter name because register need 3 params:
        //Email, Name, Password
        //So we will create new Dialog with enter name value after fill email+password

        final View enter_name_view = LayoutInflater.from(this).inflate(R.layout.enter_name_layout,null);

        new MaterialStyledDialog.Builder(this)
                .setTitle("Register")
                .setDescription("One more step!")
                .setCustomView(enter_name_view)
                .setIcon(R.drawable.ic_user)
                .setNegativeText("Cancel")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //get value from name_edit_text
                        MaterialEditText nameEditText = enter_name_view.findViewById(R.id.name_edit_text);
                        compositeDisposable.add(myAPI.registerUser(email,nameEditText.getText().toString(),password)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String s) throws Exception {
                                Toast.makeText(MainActivity.this, "Error: " +s, Toast.LENGTH_SHORT).show(); //Just show error From API
                            }
                        }));
                    }
                }).show();
    }

    private void loginUser(String email, String password) {
        compositeDisposable.add(myAPI.loginUser(email,password)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                if(s.contains("encrypted_password"))
                    Toast.makeText(MainActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MainActivity.this, "Error: " +s, Toast.LENGTH_SHORT).show(); //Just show error From API
            }
        }));

    }
}
