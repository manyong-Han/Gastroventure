package com.teamproject.gastroventure.menu.member;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamproject.gastroventure.MainActivity;
import com.teamproject.gastroventure.R;
import com.teamproject.gastroventure.util.DialogSampleUtil;
import com.teamproject.gastroventure.util.LoginSharedPreference;
import com.teamproject.gastroventure.vo.UserInfo;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MemberLoginFormFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemberLoginFormFragment extends Fragment {

    private FirebaseDatabase member_db;
    private DatabaseReference db_ref;

    public static final String AUTO_ID = "auto";             // 자동 로그인을 위한 상수
    public static final String SAVE_ID = "save";             // 아이디 저장을 위한 상수
    public static final String LOGIN_ID = "login";           // 로그인 정보를 저장하기 위한 상수. 이후 다른 페이지에서도 쓰임
    public static final String IS_CHECKED = "isChecked";    // 아이디 저장 체크박스의 체크여부를 저장하기 위한 상수

    public static final String USER_KEY = "user_key";

    public static MemberLoginFormFragment newInstance() {
        return new MemberLoginFormFragment();
    }

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View view;

    TextView tv_find_id_pwd;
    EditText et_id, et_pwd;
    CheckBox cb_save_id, cb_auto_login;
    Button btn_login;

    MainActivity main;

    LoginUserInfoFragment login_userFrag;
    MemberFindIdPwd find_id_pwd_Frag;

    String id, pwd;
    String user_key;

    boolean auto_isCheck = false;
    boolean save_id_check_ok;

    private ArrayList<UserInfo> memberInfo = new ArrayList<UserInfo>();

    public MemberLoginFormFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MemberLoginFormFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MemberLoginFormFragment newInstance(String param1, String param2) {
        MemberLoginFormFragment fragment = new MemberLoginFormFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_member_login_form, container, false);

        member_db = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
        db_ref = member_db.getReference(); // DB 테이블 연결

        login_userFrag = new LoginUserInfoFragment();
        find_id_pwd_Frag = new MemberFindIdPwd();

        main = (MainActivity) getActivity();

        // Inflate the layout for this fragment
        //아이디,비밀번호 찾기
        tv_find_id_pwd = (TextView) view.findViewById(R.id.tv_find_id_pwd);
        //아이디,비밀번호 입력
        et_id = view.findViewById(R.id.et_id);
        et_pwd = view.findViewById(R.id.et_pwd);
        //자동로그인, 아이디저장
        cb_save_id = view.findViewById(R.id.cb_save_id);
        cb_auto_login = view.findViewById(R.id.cb_auto_login);
        //로그인, 회원가입 버튼
        btn_login = view.findViewById(R.id.btn_login);

        // 아이디 저장을 위해 체크박스의 체크여부를 받아와서 설정.
        save_id_check_ok = LoginSharedPreference.getChecked(getContext(), IS_CHECKED);
        cb_save_id.setChecked(save_id_check_ok);

        // 아이디 저장 체크박스가 선택되어있으면 저장되어 있는 아이디 정보를 얻어온다.
        if(save_id_check_ok) {
           et_id.setText(LoginSharedPreference.getAttribute(getContext(), SAVE_ID));
        }


        // 체크박스가 체크되어있을시에.
        // 자동 로그인
        cb_auto_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auto_isCheck = ((CheckBox) v).isChecked();
                if(!auto_isCheck){
                    LoginSharedPreference.removeAttribute(getContext(), AUTO_ID);
                }
            }
        });

        cb_save_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save_id_check_ok = ((CheckBox) v).isChecked();
                if(save_id_check_ok) {
                    LoginSharedPreference.setChecked(getContext(), IS_CHECKED, save_id_check_ok);
                }else { // 체크가 해제되면 저장된 아이디를 해제하고 저장되어있던 체크박스의 상태여부도 초기화한다.
                    LoginSharedPreference.removeAttribute(getContext(), SAVE_ID);
                    LoginSharedPreference.removeChecked(getContext(), IS_CHECKED);
                }
            }
        });

        member_data();

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //로그인 정보값 읽어오기
                id = et_id.getText().toString().trim();
                pwd = et_pwd.getText().toString().trim();

                if (id.isEmpty()) {
                    DialogSampleUtil.showMessageDialog(getContext(), "", "아이디를 입력해주세요.");
                    return;
                }
                if (pwd.isEmpty()) {
                    DialogSampleUtil.showMessageDialog(getContext(), "", "비밀번호를 입력해주세요.");
                    return;
                }

                Log.d("LLLL", "에디트 텍스트 입력값:" + id + "/" + pwd);

                boolean checkB = false;
                for (UserInfo user : memberInfo) {
                    String check_id = user.getId();
                    if (check_id.equals(id)) {
                        String check_pwd = user.getPwd();
                        if (check_pwd.equals(pwd)) {
                            user_key = user.getUser_key();

                            Log.d("LLLL", "로그인 폼 user_key:"+user_key);

                            LoginSharedPreference.setAttribute(getContext(), USER_KEY, user_key);

                            Log.d("LLLL", "프리퍼런스 저장 확인:"+ LoginSharedPreference.getAttribute(getContext(),MemberLoginFormFragment.USER_KEY));

                            checkB = true;

                            break;
                        } else {
                            checkB = false;
                        }
                    } else {
                        checkB = false;
                    }
                }

                if(checkB) {
                    // 아이디 저장 체크
                    if(save_id_check_ok) {
                        //아이디 저장
                        LoginSharedPreference.setAttribute(getContext(), SAVE_ID ,et_id.getText().toString());
                    }

                    // 자동로그인 체크
                    if(auto_isCheck) {
                        // 자동로그인 저장
                        LoginSharedPreference.setAttribute(getContext(), AUTO_ID, id);
                    }

                    LoginSharedPreference.setAttribute(getContext(), LOGIN_ID, id);

                    et_id.setText("");
                    et_pwd.setText("");

                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.main_frame, login_userFrag.newInstance(user_key)).commit();

                    Log.d("LLLL", "키값 :" + user_key);
                } else {
                    DialogSampleUtil.showMessageDialog(getContext(), "", "아이디 또는 비밀번호가 일치하지 않습니다.");
                    et_id.setText("");
                    et_pwd.setText("");
                    return;
                }
            }
        });

        tv_find_id_pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.replaceFragment(find_id_pwd_Frag);
            }
        });

        return view;
    }

    public void member_data() {
        //DB에서 id, pwd 일치하는 경우 로그인 페이지로 이동, 일치하지 않을 경우 다이얼로그 이용해서 알려주기
        db_ref.child("Member").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    UserInfo vo = dataSnapshot1.getValue(UserInfo.class);
                    vo.setUser_key(dataSnapshot1.getKey());

                    memberInfo.add(vo);

                    Log.d("LLLL", "입력값:" + vo.getId() + "/" + vo.getPwd() + "/" + vo.getUser_key());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
