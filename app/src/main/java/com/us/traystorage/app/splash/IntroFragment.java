package com.us.traystorage.app.splash;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.us.traystorage.R;

import base.BaseFragment;

public class IntroFragment extends BaseFragment {

    int layoutRes;
    public IntroFragment(int layoutRes){
        super();
        this.layoutRes = layoutRes;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(layoutRes, container, false);
        
        // ?�로가�?버튼
        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> ((IntroActivity)getActivity()).prevPage());
        }
        
        // ?�기 버튼
        View btnClose = view.findViewById(R.id.btn_close);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> ((IntroActivity)getActivity()).goNext());
        }
        
        // ?�작?�기 버튼 (4번째 ?�면)
        View btnStart = view.findViewById(R.id.btn_start);
        if (btnStart != null) {
            btnStart.setOnClickListener(v -> ((IntroActivity)getActivity()).nextPage());
        }
        
        // ?�인 버튼 (5번째 권한 ?�면) - OS 권한 ?�청 ???�음 ?�면?�로
        View btnConfirm = view.findViewById(R.id.btn_confirm);
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> ((IntroActivity)getActivity()).requestAppPermissions());
        }
        
        return view;
    }
}
