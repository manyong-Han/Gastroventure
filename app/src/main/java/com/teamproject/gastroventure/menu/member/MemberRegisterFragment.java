package com.teamproject.gastroventure.menu.member;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamproject.gastroventure.MainActivity;
import com.teamproject.gastroventure.R;
import com.teamproject.gastroventure.util.DialogSampleUtil;
import com.teamproject.gastroventure.vo.UserInfo;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MemberRegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemberRegisterFragment extends Fragment {

    final String TAG = "LLLL";

    private FirebaseDatabase member_db;
    private DatabaseReference db_ref;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View view;

    //fragment_member_register 항목 변수 선언
    EditText et_id, et_pwd, et_pwd_check, et_name, et_tel, et_nickname;
    Button btn_submit, btn_cancel;

    MainActivity main;

    LogoutUserInfoFragment logoutFrag;
    MemberLoginFormFragment loginFrag;

    String id;
    String pwd;
    String pwd_check;
    String name;
    String nickname;
    String tel;

    private ArrayList<UserInfo> member_list = new ArrayList<UserInfo>();
    private ArrayList<UserInfo> tel_list = new ArrayList<UserInfo>();

    public MemberRegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MemberRegisterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MemberRegisterFragment newInstance(String param1, String param2) {
        MemberRegisterFragment fragment = new MemberRegisterFragment();
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
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_member_register, container, false);

        member_db = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
        db_ref = member_db.getReference(); // DB 테이블 연결
        Log.d(TAG, "db연결");


        loginFrag = new MemberLoginFormFragment();
        logoutFrag = new LogoutUserInfoFragment();
        main = (MainActivity) getActivity();

        et_id = view.findViewById(R.id.et_id_email);
        et_pwd = view.findViewById(R.id.et_pwd);
        et_pwd_check = view.findViewById(R.id.et_pwd_check);
        et_name = view.findViewById(R.id.et_name);
        et_nickname = view.findViewById(R.id.et_nickname);
        et_tel = view.findViewById(R.id.et_tel);

        btn_submit = view.findViewById(R.id.btn_submit);
        btn_cancel = view.findViewById(R.id.btn_cancel);

        memberData();

        //프래그먼트 이동
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "입력 버튼 눌렀음");

                //입력된 항목 값 읽어오기
                id = et_id.getText().toString().trim();
                Log.d(TAG, "id :" + id);
                pwd = et_pwd.getText().toString().trim();
                pwd_check = et_pwd_check.getText().toString().trim();
                name = et_name.getText().toString().trim();
                nickname = et_nickname.getText().toString().trim();
                tel = et_tel.getText().toString().trim();

                if (id.isEmpty()) {
                    Toast.makeText(getContext(), "이메일 형식의 아이디를 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pwd.isEmpty()) {
                    Toast.makeText(getContext(), "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pwd_check.isEmpty()) {
                    Toast.makeText(getContext(), "비밀번호를 한번 더 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (name.isEmpty()) {
                    Toast.makeText(getContext(), "이름을 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (nickname.isEmpty()) {
                    Toast.makeText(getContext(), "닉네임을 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (tel.isEmpty()) {
                    Toast.makeText(getContext(), "연락처를 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(isValidEmail(id) == false){
                    Toast.makeText(getContext(), "유효하지 않은 Email형식의 ID입니다.", Toast.LENGTH_SHORT).show();
                    et_id.requestFocus();
                    return;
                }
                if(isValidtel(tel) ==false){
                    Toast.makeText(getContext(), "유효하지 않은 형식의 전화번호입니다.", Toast.LENGTH_SHORT).show();
                    et_tel.requestFocus();
                    return;
                }

                //비밀번호 재확인 정상작동
                //비밀번호 확인
                if (!pwd_check.equals(pwd)) {
                    //비밀번호 확인했을 때 같지않을 경우
                    //DialogSampleUtil.showMessageDialog(getContext(),"","비밀번호가 일치하지 않습니다. 다시 확인해주세요");
                    Toast.makeText(getContext(), "비밀번호가 일치하지 않습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    et_pwd_check.setText("");
                    et_pwd_check.requestFocus();
                    return;
                }

                if(!member_list.isEmpty()) {

                    for (UserInfo check_id : member_list) {
                        Log.d(TAG, "if전 사용중 아이디 비번 확인 :" + check_id.getId());
                        if ((check_id.getId()).equals(id)) {
                            //여기서 에러
                            DialogSampleUtil.showMessageDialog(getContext(), "", "이미 사용중인 아이디 입니다.");
                            et_id.setText("");
                            et_id.requestFocus();
                            return;
                        }
                    }

                    for (UserInfo check_tel : tel_list) {
                        Log.d(TAG, "if전 사용중 전화번호 확인 :" + check_tel.getTel());
                        if ((check_tel.getTel()).equals(tel)) {
                            DialogSampleUtil.showMessageDialog(getContext(), "", "이미 사용중인 전화번호 입니다.");
                            et_tel.setText("");
                            et_tel.requestFocus();
                            return;
                        }
                    }
                }

                //DB로 인서트
                UserInfo userInfo = new UserInfo();
                userInfo.setId(id);
                userInfo.setPwd(pwd);
                userInfo.setName(name);
                userInfo.setNickname(nickname);
                userInfo.setTel(tel);

                Log.d(TAG, "정보확인 :" + id + "/" + pwd + "/" + name + "/" + nickname + "/" + tel);
                String user_key = db_ref.push().getKey();
                userInfo.setUser_key(user_key);

                db_ref.child("Member").child(user_key).setValue(userInfo);

                Log.d(TAG, "member 테이블에 입력 끝");
                Toast.makeText(getContext(), "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();

                main.replaceFragment(loginFrag);
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.replaceFragment(logoutFrag);
            }
        });

        return view;
    }

    public void memberData() {
        db_ref.child("Member").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    UserInfo vo = dataSnapshot1.getValue(UserInfo.class);
                    vo.setUser_key(dataSnapshot1.getKey());

                    member_list.add(vo);
                    Log.d("LLLL", vo.getName() + " / " + vo.getTel());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("LLLL", "데이터 로딩 오류!!!!!!");
            }
        });
    }

    //ID - email 유효성 정규식
    public static boolean isValidEmail(String email) {
        boolean err = false;
        String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        if(m.matches()) {
            err = true;
        }
        return err;
    }
    //전화번호 유효성 정규식
    public boolean isValidtel(String tel){
        return tel.matches("(01[016789])(\\d{3,4})(\\d{4})");
    }
}
