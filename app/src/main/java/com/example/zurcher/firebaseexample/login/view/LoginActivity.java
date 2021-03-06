package com.example.zurcher.firebaseexample.login.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.zurcher.firebaseexample.R;
import com.example.zurcher.firebaseexample.chat.view.ChatActivity;
import com.example.zurcher.firebaseexample.login.LoginContract;
import com.example.zurcher.firebaseexample.login.presenter.LoginPresenter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity implements LoginContract.View, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;

    private int SIGN_IN_REQUEST_CODE = 888;

    private LoginPresenter presenter;

    @BindView(R.id.sign_in_progress_bar)
    ProgressBar sign_in_progress_bar;
    @BindView(R.id.sign_in_button)
    SignInButton sign_in_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        presenter = new LoginPresenter(this);

        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.setAuthListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.removeAuthListener();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConntectionFailed " + connectionResult.getErrorMessage());
    }

    @OnClick(R.id.sign_in_button)
    void signIn(View view) {
        showProgressBar(true);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, SIGN_IN_REQUEST_CODE);
    }

    private void showProgressBar(boolean show) {
        sign_in_progress_bar.setVisibility(show ? View.VISIBLE : View.GONE);
        sign_in_button.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                presenter.logInWithFirebase(result.getSignInAccount());
            }
        }
    }

    @Override
    public void startChatListActivity() {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);

        showProgressBar(false);
    }

    @Override
    public void showFirebaseAuthenticationFailedMessage() {
        Toast.makeText(LoginActivity.this, "Authentication failed.",
                Toast.LENGTH_SHORT).show();

        showProgressBar(false);
    }
}
