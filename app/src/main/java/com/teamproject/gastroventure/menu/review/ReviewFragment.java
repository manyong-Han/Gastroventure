package com.teamproject.gastroventure.menu.review;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.teamproject.gastroventure.MainActivity;
import com.teamproject.gastroventure.R;
import com.teamproject.gastroventure.adapter.ReviewAdapter;
import com.teamproject.gastroventure.datainterface.DataInterface;
import com.teamproject.gastroventure.menu.member.MemberLoginFormFragment;
import com.teamproject.gastroventure.util.DialogSampleUtil;
import com.teamproject.gastroventure.util.LoginSharedPreference;
import com.teamproject.gastroventure.vo.ReviewImgVo;
import com.teamproject.gastroventure.vo.ReviewVo;

import java.util.ArrayList;

public class ReviewFragment extends Fragment implements DataInterface {
    private final String TAG = "ReviewFrag Log";
    private final String CHILE_NAME_REVIEW = "Review";
    private final String CHILE_NAME_REVIEW_IMAGE = "Review_Image";

    private View view;
    private FloatingActionButton insert_fbtn;
    private RecyclerView review_rcv_list;
    private RecyclerView.Adapter reviewAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<ReviewVo> reviewList = new ArrayList<ReviewVo>();
    private ArrayList<String> writeUserList = new ArrayList<String>();

    private FirebaseDatabase reviewDatabase;
    private DatabaseReference databaseReference;

    private FirebaseStorage storage;
    private StorageReference storageRef;

    private MainActivity main;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_review, container, false);

        main = (MainActivity)getActivity();

        review_rcv_list = view.findViewById(R.id.review_rcv_list); // 아디 연결
        review_rcv_list.setHasFixedSize(true); // 리사이클러뷰 기존성능 강화
        layoutManager = new LinearLayoutManager(getContext());
        review_rcv_list.setLayoutManager(layoutManager);

        reviewDatabase = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
        databaseReference = reviewDatabase.getReference(); // DB 테이블 연결

        // 가장 먼저, FirebaseStorage 인스턴스를 생성한다
        storage = FirebaseStorage.getInstance("gs://gastroventure-7f99f.appspot.com/");

        // 위에서 생성한 FirebaseStorage 를 참조하는 storage를 생성한다
        storageRef = storage.getReference();

        insert_fbtn = view.findViewById(R.id.review_fab);
        insert_fbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(LoginSharedPreference.getAttribute(getContext(), MemberLoginFormFragment.USER_KEY).isEmpty()){
                    DialogSampleUtil.showMessageDialog(getContext(), "", "로그인을 해야 리뷰 작성이 가능합니다.\n로그인 해 주시기 바랍니다.");
                } else {
                    main.replaceFragment(new ReviewInsertFragment());
                }
            }
        });

        dataRead();

        return view;
    }

    public void dataRead(){
        databaseReference.child(CHILE_NAME_REVIEW).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 파이어베이스 데이터베이스의 데이터를 받아오는 곳
                reviewList.clear(); // 기존 배열리스트가 존재하지않게 초기화
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) { // 반복문으로 데이터 List를 추출해냄
                    snapshot.getKey();
                    ReviewVo reviewVo = snapshot.getValue(ReviewVo.class); // 만들어뒀던 User 객체에 데이터를 담는다.
                    reviewVo.setReview_key(snapshot.getKey());

                    writeUserList.add(reviewVo.getWrite_user());

                    String user = reviewVo.getWrite_user().substring(0,3) + "****";
                    reviewVo.setWrite_user(user);

                    reviewList.add(reviewVo); // 담은 데이터들을 배열리스트에 넣고 리사이클러뷰로 보낼 준비

                    Log.d(TAG, " 메뉴이름! " + reviewVo.getMenu());
                    Log.d(TAG, " 리뷰 키! " + reviewVo.getReview_key());
                    Log.d(TAG, " 리뷰 작성자! " + reviewVo.getWrite_user());
                }
                reviewAdapter.notifyDataSetChanged(); // 리스트 저장 및 새로고침
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 디비를 가져오던중 에러 발생 시
                Log.e("ReviewFragment", String.valueOf(databaseError.toException())); // 에러문 출력
            }
        });

        reviewAdapter = new ReviewAdapter(reviewList, getContext(), this);
        review_rcv_list.setAdapter(reviewAdapter); // 리사이클러뷰에 어댑터 연결
    }

    @Override
    public void dataRemove(final String key, final int pos) {
        //final String review_key = key;
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {//Yes
                    try {
                        // 작성자의 아이디와 현재 로그인 한 유저의 아이디가 같은지 비교
                        if(writeUserList.get(pos).equals(LoginSharedPreference.getAttribute(getContext(), MemberLoginFormFragment.LOGIN_ID))) {
                            databaseReference.child(CHILE_NAME_REVIEW).child(key).removeValue();
                            // 해당 키에 대한 이미지도 다 삭제를 해야함.
                            imgDatabaseDelete(key);
                            dataRead();
                        } else {
                            DialogSampleUtil.showMessageDialog(getContext(),"","작성자만 삭제할 수 있습니다.");
                        }
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
        ft.replace(R.id.main_frame, ReviewDetailFragment.newInstance(key)).commit();
    }

    public void imgDatabaseDelete(final String key){
        databaseReference.child(CHILE_NAME_REVIEW_IMAGE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) { // 반복문으로 데이터 List를 추출해냄
                    ReviewImgVo reviewImgVo = snapshot.getValue(ReviewImgVo.class); // 만들어뒀던 User 객체에 데이터를 담는다.
                    reviewImgVo.setReview_img_key(snapshot.getKey());
                    String img_key = reviewImgVo.getReview_img_key();

                    if (reviewImgVo.getReview_key().equals(key)) {
                        databaseReference.child(CHILE_NAME_REVIEW_IMAGE).child(img_key).removeValue();
                        imgStorageDel(reviewImgVo.getMenu_image_name());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void imgStorageDel(String imgName){
        storage.getReference().child("images").child(imgName).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(), "삭제 완료", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "삭제 실패", Toast.LENGTH_LONG).show();
            }
        });
    }
}
