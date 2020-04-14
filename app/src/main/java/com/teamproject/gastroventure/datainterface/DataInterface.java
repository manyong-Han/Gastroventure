package com.teamproject.gastroventure.datainterface;

/**
 * Created by hanman-yong on 2020/03/24.
 */
public interface DataInterface {
    // 데이터 삭제 후 목록 갱신을 위해 사용하는 콜백 메소드
    void dataRemove(String key, int pos);
    void dataDetail(String key);
}
