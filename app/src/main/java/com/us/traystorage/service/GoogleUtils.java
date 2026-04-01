package com.us.traystorage.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

public class GoogleUtils {
    private static final int REQ_LOGIN_CODE = 9001;

    private LoginListener mLoginListner = null;
    private Context mContext = null;
    private GoogleSignInClient mGoogleSignInClient;
    private Activity mLoginActivity = null;

    public GoogleUtils(FragmentActivity context) {
        mContext = context;
        mLoginActivity = context;

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(context, signInOptions);
    }

    public void login(FragmentActivity activity, LoginListener listner) {
        mLoginListner = listner;

        Intent intent = mGoogleSignInClient.getSignInIntent();
        activity.startActivityForResult(intent, REQ_LOGIN_CODE);
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_LOGIN_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
            return true;
        }
        return false;
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            String nam = account.getDisplayName();
            Log.d("Name:::", nam != null ? nam : "null");
            String emai = account.getEmail();
            Log.d("Email:::", emai != null ? emai : "null");

            String imgUrl = null;
            if (account.getPhotoUrl() != null) {
                imgUrl = account.getPhotoUrl().toString();
                Log.d("IMageURL", imgUrl);
            }

            UserInfo userInfo = new UserInfo();
            userInfo.setId(account.getId());
            userInfo.setEmail(emai);
            userInfo.setName(nam);
            userInfo.setPhoto(imgUrl);

            if (mLoginListner != null) {
                mLoginListner.onSuccessed(userInfo);
            }

        } catch (ApiException e) {
            Log.e("GoogleUtils", "Sign-in failed. Status code: " + e.getStatusCode());
            if (mLoginListner != null) {
                mLoginListner.onFailed(e.getStatusCode());
            }
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class UserInfo implements Serializable {
        private String id;
        private String email;
        private String name;
        private String photo;
        private String birth;
        private String gender;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhoto() {
            return photo;
        }

        public void setPhoto(String photo) {
            this.photo = photo;
        }

        public String getBirth() {
            return birth;
        }

        public void setBirth(String birth) {
            this.birth = birth;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }
    }

    public interface LoginListener {
        void onSuccessed(UserInfo userInfo);

        void onFailed(int error);
    }
}
