package com.us.traystorage.app.auth;

import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.ViewModelProviders;

import com.us.traystorage.App;
import com.us.traystorage.R;
import com.us.traystorage.app.common.dialog.AlertDialog;
import com.us.traystorage.app.common.dialog.LoadingDialog;
import com.us.traystorage.data.ApiResponse;
import com.us.traystorage.data.DataManager;
import com.us.traystorage.data.model.ModelBase;
import com.us.traystorage.data.model.ModelCode;
import com.us.traystorage.data.remote.ResponseSubscriber;
import com.us.traystorage.databinding.ActivityChangePasswordBinding;

import base.BaseBindingActivity;
import base.BaseViewModel;
import helper.Validation;

public class ChangePasswordActivity extends BaseBindingActivity<ActivityChangePasswordBinding> {
    ViewModel viewModel;
    String loginId;

    @Override
    public int getLayout() {
        return R.layout.activity_change_password;
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
        Intent i = getIntent();
        loginId = i.getStringExtra("login_id");

        binding.setActivity(this);
        binding.editPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                binding.linePassword.setBackgroundResource(b?R.color.black:R.color.light_gray);
            }
        });
        binding.editPassword2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                binding.linePassword2.setBackgroundResource(b?R.color.black:R.color.light_gray);
            }
        });
    }

    public void onConfirm(){
        viewModel.changePassword(loginId, binding.editPassword.getPlanText());
    }

    public ObservableBoolean isEnabled = new ObservableBoolean(false);
    public void onValidate() {
        isEnabled.set(Validation.isPassword(binding.editPassword.getPlanText())
                && binding.editPassword.getPlanText().equals(binding.editPassword2.getPlanText()));
    }


    public class ViewModel extends BaseViewModel{
        public void changePassword(String login_id, String password) {
            addDisposable(DataManager.get().changePassword(login_id, password).subscribeWith(new ResponseSubscriber<ModelBase>() {
                @Override
                public void onComplete() {
                    super.onComplete();

                    if (getResponse().result == 0) {
                        // ?�동로그?�을 ?�해 로컬???�?�된 비�?번호??갱신
                        com.us.traystorage.data.model.ModelUser user =
                                DataManager.get().getModel(com.us.traystorage.data.model.ModelUser.class);
                        if (user != null) {
                            user.password = password;
                            DataManager.get().setModel(user);
                        }
                        AlertDialog.show(ChangePasswordActivity.this)
                                .setText(getString(R.string.change_pwd_success), "", getString(R.string.confirm))
                                .setListener(() -> {onBackPressed();});
                    } else if (!getResponse().msg.isEmpty()) {
                        onApiError(getResponse().msg);
                    } else {
                        onApiError(App.get().getString(R.string.error_network_content));
                    }
                }
            }));
        }
    }

}
