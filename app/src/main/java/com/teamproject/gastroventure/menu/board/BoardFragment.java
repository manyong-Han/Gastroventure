package com.teamproject.gastroventure.menu.board;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamproject.gastroventure.MainActivity;
import com.teamproject.gastroventure.R;
import com.teamproject.gastroventure.adapter.BoardAdapter;
import com.teamproject.gastroventure.datainterface.DataInterface;
import com.teamproject.gastroventure.menu.member.MemberLoginFormFragment;
import com.teamproject.gastroventure.util.DialogSampleUtil;
import com.teamproject.gastroventure.util.LoginSharedPreference;
import com.teamproject.gastroventure.vo.BoardVo;

import java.util.ArrayList;

public class BoardFragment extends Fragment implements DataInterface {
    private final String TAG = "BoardFrag LOG";
    private View view;
    private RecyclerView board_list;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<BoardVo> arrayList;
    private ArrayList<String> writeUserList = new ArrayList<String>();
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    FloatingActionButton board_fab;
    //    private Intent intent;
    private String title, content;
    private MainActivity mainActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_board, container, false);

        mainActivity = (MainActivity) getActivity();
        //게시판(리사이클러뷰)
        board_list = view.findViewById(R.id.board_list);
        board_list.setHasFixedSize(true);// 리사이클러뷰 기존 성능강화
        layoutManager = new LinearLayoutManager(getContext());
        board_list.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>();//BoardVo 객체를 담을 ArrayList (Adapter쪽으로)
        board_fab = view.findViewById(R.id.board_fab);

        database = FirebaseDatabase.getInstance();// firebase database 연결
        databaseReference = database.getReference(); // DB테이블

        board_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LoginSharedPreference.getAttribute(getContext(), MemberLoginFormFragment.USER_KEY).isEmpty()){
                    DialogSampleUtil.showMessageDialog(getContext(), "", "로그인을 해야 게시판 작성이 가능합니다.\n로그인 해 주시기 바랍니다.");
                }else {
                    mainActivity.replaceFragment(new BoardInsertFragment());
                }
            }
        });

        dataRead();

        return view;
    }

    public void dataRead(){
        databaseReference.child("Board").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            //FireBase Database의 데이터를 받아오는곳
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //기존 배열리스트 초기화
                arrayList.clear();
                //반복문으로 데이터 리스트를 추출함
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    BoardVo boardVo = snapshot.getValue(BoardVo.class);
                    boardVo.setBoard_key(snapshot.getKey());

                    writeUserList.add(boardVo.getWrite_user());
                    String writer = boardVo.getWrite_user().substring(0,3) + "****";
                    boardVo.setWrite_user(writer);

                    arrayList.add(boardVo);//담은 데이터들을 배열리스트에 추가하고 리사이클러뷰로 보낼준비
                }
                Log.d("1212121", "사이즈는 !! " + arrayList.size());
                adapter.notifyDataSetChanged();//리스트 저장 및 새로고침
            }
            //DB를 가져오던중 에러 발생할경우
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MainActivity", String.valueOf(databaseError.toException()));
            }
        });
        adapter = new BoardAdapter(arrayList,getContext(),this);
        board_list.setAdapter(adapter);//리사이클러뷰에 어댑터 연결

    }


    @Override
    public void dataRemove(final String key, final int pos) {
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {//Yes
                    try {
                        if(writeUserList.get(pos).equals(LoginSharedPreference.getAttribute(getContext(), MemberLoginFormFragment.LOGIN_ID))) {
                            Log.d("키 맞다고!!!!", key);
                            databaseReference.child("Board").child(key).removeValue();
                            dataRead();
                        }else{
                            DialogSampleUtil.showMessageDialog(getContext(),"","작성자만 삭제할 수 있습니다.");
                        }
                        //reviewAdapter.notifyDataSetChanged();
                    } catch (Exception e){
                        Log.d(TAG, e.getMessage());
                    }
                }
            }
        };

        DialogSampleUtil.showConfirmDialog(getContext(), "", "선택한 리뷰를 삭제 하시겠습니까?", handler);

    }


    @Override
    public void dataDetail(String key) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.main_frame, BoardDetailFragment.newInstance(key)).commit();
    }
}