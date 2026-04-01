package com.us.traystorage.app.auth;

import android.app.Dialog;
import android.content.Intent;

import androidx.lifecycle.ViewModelProviders;

//import com.blankj.utilcode.util.KeyboardUtils;
//import com.blankj.utilcode.util.ToastUtils;
import com.us.traystorage.App;
import com.us.traystorage.R;
import com.us.traystorage.app.common.dialog.AlertDialog;
import com.us.traystorage.app.common.dialog.LoadingDialog;
import com.us.traystorage.app.common.util.Utils;
import com.us.traystorage.app.main.MainActivity;
import com.us.traystorage.data.ApiResponse;
import com.us.traystorage.data.DataManager;
import com.us.traystorage.data.model.ModelUser;
import com.us.traystorage.data.remote.ResponseSubscriber;
import com.us.traystorage.databinding.ActivitySigninBinding;

import java.time.LocalDateTime;
import java.util.Date;

import base.BaseBindingActivity;
import base.BaseViewModel;

public class SigninActivity extends BaseBindingActivity<ActivitySigninBinding> {
    ViewModel viewModel;
    Integer wrongPwdCount = 0;
    long wrongPwdTime;

    @Override
    public int getLayout() {
        return R.layout.activity_signin;
    }

    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        initViewModel();
        initView();
        setupKeyboard(binding.bg);
    }

    private void initViewModel() {
        viewModel = new ViewModel();
    }

    private void initView() {
        binding.setActivity(this);
        binding.autoCheck.setSelected(true);

    }

    //click event
    public void checkAutoLogin() {
        binding.autoCheck.setSelected(!binding.autoCheck.isSelected());
    }

    public void doLogin() {
        if (binding.textId.length() < 1) {
            Utils.showCustomToast(this, R.string.input_id);
            return;
        }
        if (binding.textPassword.length() < 1) {
            Utils.showCustomToast(this, R.string.input_password);
            return;
        }
        viewModel.login(binding.textId.getPlanText(), binding.textPassword.getText().toString(), binding.autoCheck.isSelected());
    }

    public void onChangedId() {
        wrongPwdCount = 0;
    }

    public class ViewModel extends BaseViewModel {
        public void login(String id, String pwd, boolean isAutoLogin) {
            addDisposable(DataManager.get().login(id, pwd).subscribeWith(new ResponseSubscriber<ModelUser>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        ModelUser user = getResponse().data;
                        user.password = pwd;
                        user.isAutoLogin = isAutoLogin;
                        DataManager.get().setModel(user);
                        if(user.is_agree == 1)
                            goMain();
                        else
                        {
                            Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
                            intent.putExtra("from_login", 1);
                            startActivity(intent);
                        }
                    } else if (getResponse().result > 200) {
                        onLoginFailure(getResponse().result);
                    } else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    } else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }
    }


    public void onLoginFailure(Integer status) {
        AlertDialog dlg = null;
        switch (status) {
            case ApiResponse.API_RESULT_ERROR_USER_PAUSED:
                AlertDialog.show(SigninActivity.this)
                        .setText(getString(R.string.login_suspended), "", getString(R.string.contactus), getString(R.string.confirm))
                        .setListener(() -> {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                            startActivity(intent);
                        });
                break;
            case ApiResponse.API_RESULT_ERROR_USER_EXIT:
                AlertDialog.show(SigninActivity.this)
                        .setText(getString(R.string.login_waiting), "", getString(R.string.confirm), getString(R.string.cancel))
                        .setListener(() -> {
                            goExitCancel();
                        });
                break;
            case ApiResponse.API_RESULT_ERROR_USER_NO_EXIST:
            case ApiResponse.API_RESULT_ERROR_WRONG_PWD: {
                boolean isShow = true;
                if (wrongPwdCount == 0)
                    wrongPwdTime = System.currentTimeMillis();
                wrongPwdCount++;
                if (wrongPwdCount >= 5) {
                    wrongPwdCount = 0;
                    long diffMils = System.currentTimeMillis() - wrongPwdTime;
                    if (diffMils < 5 * 60 * 1000/*5min*/) {
                        isShow = false;
                        AlertDialog.show(SigninActivity.this)
                                .setText("", getString(R.string.wrong_pwd), getString(R.string.find_password), getString(R.string.cancel))
                                .setListener(() -> {
                                    goFindPassword();
                                });
                    }
                    wrongPwdTime = System.currentTimeMillis();
                }

                if (isShow) {
                    AlertDialog.show(SigninActivity.this)
                            .setText(getString(R.string.login_not_exist), "", getString(R.string.confirm))
                            .setListener(() -> {
                            });
                }


            }
            break;
        }
    }

    void goMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    void goExitCancel() {
        Intent intent = new Intent(this, FindAuthActivity.class);
        intent.putExtra("type", "exit_cancel");
        intent.putExtra("user_id", binding.textId.getPlanText());
        if(binding.autoCheck.isSelected())
            intent.putExtra("pwd", binding.textPassword.getPlanText());
        startActivity(intent);
    }

    public void goFindPassword() {
        goPasswordReset();
    }

    public void goFindId() {
        Intent intent = new Intent(this, FindAuthActivity.class);
        intent.putExtra("type", "find_id");
        startActivity(intent);
    }

    public void goPasswordReset() {
        Intent intent = new Intent(this, FindAuthActivity.class);
        intent.putExtra("type", "password_reset");
        startActivity(intent);
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, LoginHomeActivity.class);
        startActivity(intent);
        finish();
    }
}
