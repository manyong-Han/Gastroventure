package com.teamproject.gastroventure.menu.member;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.teamproject.gastroventure.MainActivity;
import com.teamproject.gastroventure.R;

public class LogoutUserInfoFragment extends Fragment {

    //Instance를 변환해줄 메소드 생성
    public static  LogoutUserInfoFragment newInstance(){
        return new LogoutUserInfoFragment();
    }

    private View view;

    Button btn_login, btn_register;
    //버튼 눌렀을때 이동할 프래그먼트
    MemberLoginFormFragment loginFrag;
    MemberRegisterFragment registerFrag;

    MainActivity main;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_logout_user_info, container, false);

        //replaceFragment 메소드를 쓰기 위해 MainActivity 가져옴
        main = (MainActivity)getActivity();

        loginFrag =new MemberLoginFormFragment();
        registerFrag = new MemberRegisterFragment();

        btn_login = (Button)view.findViewById(R.id.btn_login);
        btn_register =(Button)view.findViewById(R.id.btn_register);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.replaceFragment(loginFrag);
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.replaceFragment(registerFrag);
            }
        });

        return view;
    }
}
