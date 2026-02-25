package com.kyad.traystorage.app.splash;

import com.kyad.traystorage.App;
import com.kyad.traystorage.R;
import com.kyad.traystorage.data.ApiResponse;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.model.ModelUser;
import com.kyad.traystorage.data.model.ModelVersion;
import com.kyad.traystorage.data.remote.ResponseSubscriber;

import base.BaseViewModel;
import lombok.Getter;
import lombok.Setter;

public class LoadingViewModel extends BaseViewModel {
    @Getter
    @Setter
    public LoadingApiListener apiListener;

    public void autoLogin(String id, String password) {
        addDisposable(DataManager.get().login(id, password).subscribeWith(new ResponseSubscriber<ModelUser>() {
            @Override
            public void onComplete() {
                super.onComplete();

                if (getResponse().result == 0) {
                    ModelUser user = getResponse().data;

                    ModelUser locUser = DataManager.get().getModel(ModelUser.class);
                    user.password = locUser.password;
                    user.isAutoLogin = locUser.isAutoLogin;

                    DataManager.get().setModel(user);

                    apiListener.onLoginSuccess();
                }
                else if(getResponse().result > 200 && getResponse().result < 300){
                    apiListener.onLoginFailure(0);
                }
                else {
                    apiListener.onError(App.get().getString(R.string.error_network_content));
                }
            }
        }));
    }

    public void getVersionInfo() {
        // 테스트 모드일 경우 테스트 버전 정보 사용
        if (DataManager.get().isTestMode()) {
            ModelVersion version = DataManager.get().getTestVersionInfo();
            apiListener.onGetVersionSuccess(version);
            return;
        }
        
        addDisposable(DataManager.get().getVersionInfo().subscribeWith(new ResponseSubscriber<ModelVersion>() {
            @Override
            public void onComplete() {
                super.onComplete();

                if (getResponse().result == 0) {
                    ModelVersion version = getResponse().data;
                    apiListener.onGetVersionSuccess(version);
                } else {
                    apiListener.onError(App.get().getString(R.string.error_network_content));
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                apiListener.onError(App.get().getString(R.string.error_network_content));
            }
        }));
    }
}
