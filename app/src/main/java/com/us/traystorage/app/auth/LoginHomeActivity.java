package com.us.traystorage.app.auth;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import androidx.annotation.Nullable;

import com.us.traystorage.App;
import com.us.traystorage.R;
import com.us.traystorage.app.common.dialog.LoadingDialog;
import com.us.traystorage.app.common.util.Utils;
import com.us.traystorage.app.main.MainActivity;
import com.us.traystorage.data.DataManager;
import com.us.traystorage.data.LocalStorageManager;
import com.us.traystorage.data.model.ModelUser;
import com.us.traystorage.data.remote.ResponseSubscriber;
import com.us.traystorage.databinding.ActivityLoginHomeBinding;
import com.us.traystorage.service.GoogleUtils;

import base.BaseBindingActivity;
import base.BaseViewModel;
public class LoginHomeActivity extends BaseBindingActivity<ActivityLoginHomeBinding> {
    ViewModel viewModel;
    GoogleUtils googleUtils = null;

    @Override
    public int getLayout() {
        return R.layout.activity_login_home;
    }

    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        initViewModel();
        initView();

        googleUtils = new GoogleUtils(this);
    }

    private void initView() {
        binding.setActivity(this);
        // ?�스??모드 버튼 비활?�화 (XML 주석�??�께 ?�제?�여 ?�용)
        // binding.btnTestMode.setVisibility(
        //     BuildConfig.TEST_MODE_ENABLED ? View.VISIBLE : View.GONE
        // );
    }

    private void initViewModel() {
        viewModel = new ViewModel();
    }

    //click event
    public void goLogin() {
        Intent intent = new Intent(this, SigninActivity.class);
        startActivity(intent);
        finish();
    }

    public void goSignUp() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
        finish();
    }

    // ?�스??모드: 로그???�이 메인?�로 ?�동
    public void goTestMode() {
        // ?��? ?��? ?�이???�성
        ModelUser testUser = new ModelUser();
        testUser.id = 999;
        testUser.login_id = "test_user";
        testUser.name = "TestUser";
        testUser.access_token = "test_access_token_12345";
        testUser.phone_number = "01012345678";
        testUser.email = "test@test.com";
        testUser.isAutoLogin = false;
        testUser.is_agree = 1;
        testUser.birthday = "1990-01-01";
        testUser.profile_image = "";
        testUser.gender = 0;
        testUser.signup_type = 0;
        testUser.status = 0;
        
        // ?��? ?��? ?�??
        DataManager.get().setModel(testUser);
        
        // 로컬 ?�?�소 초기??�?기본 카테고리 ?�성
        LocalStorageManager.get().initDefaultCategory();
        
        // 메인?�로 ?�동
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void goSnsSignup(Integer type) {
        if (type == 1) {
            //google
            googleUtils.login(LoginHomeActivity.this, new GoogleUtils.LoginListener() {
                @Override
                public void onSuccessed(GoogleUtils.UserInfo userInfo) {
                    if (userInfo == null) {
                        Utils.showCustomToast(LoginHomeActivity.this, R.string.google_login_failed);
                        return;
                    }
                    String userid = String.valueOf(userInfo.getId());
                    viewModel.login(userid, userid, "1", true);
                }

                @Override
                public void onFailed(int error) {
                    Utils.showCustomToast(LoginHomeActivity.this, R.string.google_login_failed);
                }
            });
        }
    }


    private boolean bFinish = false;

    @Override
    public void onBackPressed() {
        if (!bFinish) {
            bFinish = true;
            Utils.showCustomToast(this, R.string.app_finish_message);
            new Handler().postDelayed(() -> bFinish = false, 2000);
        } else {
            finish();
        }
    }

    public class ViewModel extends BaseViewModel {
        public void login(String id, String pwd, String type, boolean isAutoLogin) {
            addDisposable(DataManager.get().login(id, pwd).subscribeWith(new ResponseSubscriber<ModelUser>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        ModelUser user = getResponse().data;
                        user.password = pwd;
                        user.isAutoLogin = isAutoLogin;
                        DataManager.get().setModel(user);

                        Intent intent = new Intent(LoginHomeActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (getResponse().result > 200) {
                        Intent intent = new Intent(LoginHomeActivity.this, SignupActivity.class);
                        intent.putExtra("id", id);
                        intent.putExtra("password", pwd);
                        intent.putExtra("type", type);
                        startActivity(intent);
                        finish();
                    } else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    } else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (googleUtils != null && googleUtils.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }
}
