package com.example.verification.service;

import com.example.verification.model.dto.sign.CreateSignRecordRequest;
import com.example.verification.model.vo.SignRecordDetailResponse;
import com.example.verification.model.vo.SignRecordItemResponse;

import java.util.List;

public interface SignService {

    SignRecordDetailResponse createSignRecord(Long userId, CreateSignRecordRequest request);

    SignRecordDetailResponse getBySignNo(String signNo);

    List<SignRecordItemResponse> listUserSignRecords(Long userId);

    List<SignRecordItemResponse> listAllSignRecords();
}
