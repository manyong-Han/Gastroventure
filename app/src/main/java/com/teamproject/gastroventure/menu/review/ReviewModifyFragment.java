package com.teamproject.gastroventure.menu.review;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.otto.Subscribe;
import com.teamproject.gastroventure.MainActivity;
import com.teamproject.gastroventure.R;
import com.teamproject.gastroventure.adapter.ReviewDetailImgAdapter;
import com.teamproject.gastroventure.adapter.ReviewModifyImgAdapter;
import com.teamproject.gastroventure.datainterface.DataImgInterface;
import com.teamproject.gastroventure.event.ActivityResultEvent;
import com.teamproject.gastroventure.menu.member.MemberLoginFormFragment;
import com.teamproject.gastroventure.util.BusProvider;
import com.teamproject.gastroventure.util.DialogSampleUtil;
import com.teamproject.gastroventure.util.LoginSharedPreference;
import com.teamproject.gastroventure.vo.ReviewImgVo;
import com.teamproject.gastroventure.vo.ReviewVo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ReviewModifyFragment extends Fragment implements DataImgInterface {
    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA = 2;

    private static final String SELECT_KEY = "select_key";
    private final String CHILE_NAME_REVIEW = "Review";
    private final String CHILE_NAME_REVIEW_IMAGE = "Review_Image";
    private final String TAG = "ReviewInsertFrag";

    private View view;

    private MainActivity main;

    private FirebaseDatabase reviewDatabase;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference spaceRef;

    private EditText et_modify_store_name;
    private EditText et_modify_menu;
    private EditText et_modify_review_content;

    private RatingBar review_modify_rating;

    private RecyclerView modify_food_rcv_view;
    private RecyclerView.Adapter reviewModifyAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<ReviewVo> reviewList = new ArrayList<ReviewVo>();
    private ArrayList<ReviewImgVo> reviewImageList = new ArrayList<ReviewImgVo>();
    private ArrayList<Uri> imgUriList = new ArrayList<Uri>();
    private ArrayList<String> imgNameList = new ArrayList<String>();
    private ArrayList<String> reviewKeyList = new ArrayList<String>();

    private ReviewVo reviewVo;
    private ReviewImgVo reviewImgVo;

    private Button btn_modify_image_add;
    private Button btn_review_modify;
    private Button btn_review_modify_cancel;

    private Double rating_num = 0.0;
    private String mCurrentPhotoPath;
    private String file_name;
    private String select_key;

    private Uri imgUri, photoURI;
    private File tempFile;

    public static ReviewModifyFragment newInstance(String select_key) {
        ReviewModifyFragment fragment = new ReviewModifyFragment();
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_review_modify_form, container, false);

        main = (MainActivity) getActivity();
        Log.d(SELECT_KEY, "key = " + select_key);

        et_modify_store_name = view.findViewById(R.id.review_modify_name);
        et_modify_menu = view.findViewById(R.id.review_modify_menu);
        et_modify_review_content = view.findViewById(R.id.review_modify_content);

        review_modify_rating = view.findViewById(R.id.review_modify_rating);

        modify_food_rcv_view = view.findViewById(R.id.modify_rcv_image); // 아디 연결
        modify_food_rcv_view.setHasFixedSize(true); // 리사이클러뷰 기존성능 강화
        layoutManager = new GridLayoutManager(getContext(), 3);
        modify_food_rcv_view.setLayoutManager(layoutManager);

        btn_modify_image_add = view.findViewById(R.id.review_modify_image);
        btn_review_modify = view.findViewById(R.id.review_modify_btn);
        btn_review_modify_cancel = view.findViewById(R.id.review_modify_cancel_btn);

        reviewDatabase = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
        databaseReference = reviewDatabase.getReference(); // DB 테이블 연결

        // 가장 먼저, FirebaseStorage 인스턴스를 생성한다
        storage = FirebaseStorage.getInstance("gs://gastroventure-7f99f.appspot.com/");

        // 위에서 생성한 FirebaseStorage 를 참조하는 storage를 생성한다
        storageRef = storage.getReference();


        dataRead();
        imageDataRead();

        review_modify_rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rating_num = Double.parseDouble(String.valueOf(rating));
            }
        });

        btn_modify_image_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageSelect();
            }
        });

        btn_review_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String store_name = et_modify_store_name.getText().toString();
                String menu = et_modify_menu.getText().toString();
                String review_content = et_modify_review_content.getText().toString();

                if (store_name.isEmpty()) {
                    Toast.makeText(getContext(), "상호명을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (menu.isEmpty()) {
                    Toast.makeText(getContext(), "메뉴를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (review_content.isEmpty()) {
                    review_content = "";
                }

                if (rating_num == 0.0) {
                    Toast.makeText(getContext(), "별점은 1점 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                ReviewVo reviewVo = new ReviewVo();

                String user_id = LoginSharedPreference.getAttribute(getContext(), MemberLoginFormFragment.LOGIN_ID);

                reviewVo.setStore_name(store_name);
                reviewVo.setMenu(menu);
                reviewVo.setReview_content(review_content);
                reviewVo.setRating_num(rating_num);
                if(!imgUriList.isEmpty()) {
                    reviewVo.setMenu_image(imgUriList.get(0).toString());
                }
                reviewVo.setWrite_user(user_id);

                databaseReference.child(CHILE_NAME_REVIEW).child(select_key).setValue(reviewVo); // child 는 컬럼의 기본키?

                ReviewImgVo review_insertImgVo = new ReviewImgVo();

                if (!reviewImageList.isEmpty()) {
                    for(String key : reviewKeyList){
                        databaseReference.child(CHILE_NAME_REVIEW_IMAGE).child(key).removeValue();
                    }
                    for (int i = 0; i < imgUriList.size(); i++) {
                        review_insertImgVo.setMenu_image(imgUriList.get(i).toString());
                        review_insertImgVo.setMenu_image_name(imgNameList.get(i));
                        review_insertImgVo.setReview_key(select_key);
                        databaseReference.child(CHILE_NAME_REVIEW_IMAGE).push().setValue(review_insertImgVo);
                    }
                }

                main.replaceFragment(new ReviewFragment());
            }
        });

        btn_review_modify_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.main_frame, ReviewDetailFragment.newInstance(select_key)).commit();
            }
        });

        return view;
    }

    public void dataRead() {
        databaseReference.child(CHILE_NAME_REVIEW).child(select_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reviewVo = dataSnapshot.getValue(ReviewVo.class); // 만들어뒀던 User 객체에 데이터를 담는다.
                reviewVo.setReview_key(dataSnapshot.getKey());

                et_modify_store_name.setText(reviewVo.getStore_name());
                et_modify_menu.setText(reviewVo.getMenu());
                et_modify_review_content.setText(reviewVo.getReview_content());

                review_modify_rating.setRating((float) reviewVo.getRating_num());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 디비를 가져오던중 에러 발생 시
                Log.e("ReviewFragment", String.valueOf(databaseError.toException())); // 에러문 출력
            }
        });
    }

    public void imageDataRead() {
        databaseReference.child(CHILE_NAME_REVIEW_IMAGE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 파이어베이스 데이터베이스의 데이터를 받아오는 곳
                reviewImageList.clear(); // 기존 배열리스트가 존재하지않게 초기화
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) { // 반복문으로 데이터 List를 추출해냄
                    reviewImgVo = snapshot.getValue(ReviewImgVo.class); // 만들어뒀던 User 객체에 데이터를 담는다.
                    reviewImgVo.setReview_img_key(snapshot.getKey());

                    if (reviewImgVo.getReview_key().equals(reviewVo.getReview_key())) {
                        reviewImageList.add(reviewImgVo); // 담은 데이터들을 배열리스트에 넣고 리사이클러뷰로 보낼 준비
                        Log.d(TAG, " 리뷰 키 !! " + reviewImgVo.getReview_img_key());
                        Log.d(TAG, " 사진이름 !! " + reviewImgVo.getMenu_image());
                        imgUriList.add(Uri.parse(reviewImgVo.getMenu_image()));
                        imgNameList.add(reviewImgVo.getMenu_image_name());
                        reviewKeyList.add(reviewImgVo.getReview_img_key());
                    }
                }
                reviewModifyAdapter.notifyDataSetChanged(); // 리스트 저장 및 새로고침
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 디비를 가져오던중 에러 발생 시
                Log.e("ReviewDetailFragment", String.valueOf(databaseError.toException())); // 에러문 출력
            }
        });
        reviewModifyAdapter = new ReviewModifyImgAdapter(getContext(), reviewImageList, this);
        modify_food_rcv_view.setAdapter(reviewModifyAdapter); // 리사이클러뷰에 어댑터 연결
    }

    public void imageSelect() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
        alertBuilder.setTitle("실행할 메뉴를 선택하세요.");

        final ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.select_dialog_singlechoice);
        adapter.add("카메라");
        adapter.add("앨범");

        alertBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                switch (id) {
                    case 0:
                        takePhoto();
                        break;
                    case 1:
                        selectAlbum();
                        break;
                }
            }
        });

        alertBuilder.show();
    }

    public void takePhoto() {
        // 촬영 후 이미지 가져옴
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (photoFile != null) {
                    Uri providerURI = FileProvider.getUriForFile(getContext(), getActivity().getPackageName(), photoFile);
                    imgUri = providerURI;
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI);
                    startActivityForResult(intent, PICK_FROM_CAMERA);
                }
            }
        } else {
            Log.v("알림", "저장공간에 접근 불가능");
            return;
        }
    }

    public File createImageFile() throws IOException {
        String imgFileName = System.currentTimeMillis() + ".jpg";
        File imageFile = null;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "review");

        if (!storageDir.exists()) {
            //없으면 만들기
            Log.v("알림", "storageDir 존재 x " + storageDir.toString());
            storageDir.mkdirs();
        }
        Log.v("알림", "storageDir 존재함 " + storageDir.toString());
        imageFile = new File(storageDir, imgFileName);
        mCurrentPhotoPath = imageFile.getAbsolutePath();

        return imageFile;
    }

    public void selectAlbum() {
        //앨범에서 이미지 가져옴
        //앨범 열기
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    public void onUriListAdd() {
        DialogSampleUtil.showProgress(getContext(), "사진 업로드 중이니 잠시 기다려주세요.");

        ReviewImgVo reviewImgVo = new ReviewImgVo("", mCurrentPhotoPath);
        reviewImageList.add(reviewImgVo);

        uploadFireBase(mCurrentPhotoPath);

        Log.d(TAG, "" + reviewImageList.size());

        modify_food_rcv_view.setAdapter(new ReviewModifyImgAdapter(getContext(), reviewImageList, this));

        mCurrentPhotoPath = null;
    }

    public void uploadFireBase(String pathName) {
        Uri file = Uri.fromFile(new File(pathName));
        file_name = file.getLastPathSegment();
        imgNameList.add(file_name);

        // 위의 저장소를 참조하는 images 폴더를 연결한다.
        spaceRef = storageRef.child("images/" + file_name);
        UploadTask uploadTask = spaceRef.putFile(file);

        // 파일 업로드의 성공/실패에 대한 콜백 받아 핸들링 하기 위해 아래와 같이 작성한다
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "사진에러 : " + exception.getMessage());
                Toast.makeText(getContext(), "업로드에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                DialogSampleUtil.hideProgress();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (taskSnapshot.getMetadata() != null) {
                    if (taskSnapshot.getMetadata().getReference() != null) {
                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                imgUriList.add(uri);
                                Toast.makeText(getContext(), "업로드에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                                DialogSampleUtil.hideProgress();
                            }
                        });
                    }
                }
            }
        });
    }

    // 앨범에서 가져온 사진 path 얻어오기
    public String getPath(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};

        CursorLoader cursorLoader = new CursorLoader(getContext(), uri, proj, null, null, null);

        Cursor cursor = cursorLoader.loadInBackground();

        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(index);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDestroyView() {
        BusProvider.getInstance().unregister(this);
        super.onDestroyView();

    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onActivityResultEvent(@NonNull ActivityResultEvent event) {
        onActivityResult(event.getRequestCode(), event.getResultCode(), event.getData());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != getActivity().RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case PICK_FROM_ALBUM: {
                //앨범에서 가져오기
                if (data.getData() != null) {
                    try {
                        photoURI = data.getData();
                        mCurrentPhotoPath = getPath(photoURI);

                        onUriListAdd();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            case PICK_FROM_CAMERA: {
                //카메라 촬영
                try {
                    Log.v("알림", "FROM_CAMERA 처리");

                    onUriListAdd();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    // 파이어베이스 스토리지에서 해당 사진 삭제
    @Override
    public void dataImgRemove(final int position) {
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {//Yes
                    String fileName = imgNameList.get(position);
                    imgUriList.remove(position);
                    imgNameList.remove(position);
                    reviewImageList.remove(position);

                    storage.getReference().child("images").child(fileName).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getContext(), "삭제 완료", Toast.LENGTH_LONG).show();
                            modify_food_rcv_view.setAdapter(new ReviewModifyImgAdapter(getContext(), reviewImageList));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "삭제 실패", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };

        DialogSampleUtil.showConfirmDialog(getContext(), "", "이미지를 삭제하시겠습니까?", handler);
    }
}
