package com.teamproject.gastroventure.menu.board;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamproject.gastroventure.MainActivity;
import com.teamproject.gastroventure.R;
import com.teamproject.gastroventure.menu.member.MemberLoginFormFragment;
import com.teamproject.gastroventure.util.DialogSampleUtil;
import com.teamproject.gastroventure.util.LoginSharedPreference;
import com.teamproject.gastroventure.vo.BoardVo;


public class BoardDetailFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    private TextView board_detail_title;
    private TextView board_detail_content;
    private View view;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private Button board_update_btn;
    private Button detail_cancle_btn;
    private BoardVo boardVo;
    private MainActivity mainActivity;
    private String user_id;


    private static final String SELECT_KEY = "select_key";


    // TODO: Rename and change types of parameters
    private String select_key;


    public BoardDetailFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static BoardDetailFragment newInstance(String select_key) {
        BoardDetailFragment fragment = new BoardDetailFragment();
        Bundle args = new Bundle();
        args.putString(SELECT_KEY, select_key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            select_key = getArguments().getString(SELECT_KEY);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_board_detail, container, false);
        mainActivity = (MainActivity) getActivity();
        board_detail_content = view.findViewById(R.id.board_detail_content);
        board_detail_title = view.findViewById(R.id.board_detail_title);
        board_update_btn = view.findViewById(R.id.board_update_btn);
        detail_cancle_btn = view.findViewById(R.id.detail_cancle_btn);

        database  = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
        databaseReference = database.getReference(); // DB 테이블 연결

        board_update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user_id.equals(LoginSharedPreference.getAttribute(getContext(), MemberLoginFormFragment.LOGIN_ID))) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.main_frame, BoardModifyFragment.newInstance(select_key)).commit();
                }else {
                    DialogSampleUtil.showMessageDialog(getContext(), "", "작성자만 수정 가능합니다.");
                }
            }
        });
        detail_cancle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.replaceFragment(new BoardFragment());
            }
        });


        dataRead();
        return view;
    }
    public void dataRead(){
        databaseReference.child("Board").child(select_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            //FireBase Database의 데이터를 받아오는곳
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    BoardVo boardVo = dataSnapshot.getValue(BoardVo.class);
                    boardVo.setBoard_key(dataSnapshot.getKey());

                    board_detail_title.setText(boardVo.getBoard_title());
                    board_detail_content.setText(boardVo.getBoard_content());
                    user_id = boardVo.getWrite_user();

            }
            //DB를 가져오던중 에러 발생할경우
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MainActivity", String.valueOf(databaseError.toException()));
            }
        });
    }
}