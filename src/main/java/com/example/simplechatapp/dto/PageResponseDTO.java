package com.example.simplechatapp.dto;


import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
public class PageResponseDTO <E>{

    private List<E> dtoList;
    private List<Integer> pageNumList;
    private PageRequestDTO pageRequestDTO; // 검색 조건, 카테고리 등의 자세한 정보

    private boolean prev, next;

    private int totalCount, prevPage, nextPage, totalPage, current;

    @Builder(builderMethodName = "withAll")
    public PageResponseDTO(List<E> dtoList, PageRequestDTO pageRequestDTO, long total) {

        this.dtoList = dtoList;
        this.pageRequestDTO = pageRequestDTO;
        this.totalCount = (int) total;

        int end = (int)(Math.ceil(pageRequestDTO.getPage() / 10.0)) * 10;
        // 10단위의 마지막 페이지 수

        int start= end- 9;
        // 현재 마지막 페이지를 기준으로 페이지 번호 목록의 끝 번호와 시작 번호를 계산

        int last = (int)(Math.ceil(totalCount / (double)pageRequestDTO.getSize()));
        //전체 데이터 페이지 사이즈로 나누어 총 페이지 수를 계산
        // totalCount는 전체 페이지 데이터수  // pageRequestDTO는 한페에지에 나타낼 개수
        // last 는 전체 페이지 수

        end = Math.min(end, last); // 페이지 번호 목록의 끝 번호가 실제 마지막 페이지 번호보다 크지 않게 조정

        this.prev = start > 1; // 시작 페이지 번호가 1보다 크면 이전 페이지가 존재한다고 판단

        this.next = totalCount> end * pageRequestDTO.getSize();
        // 전체 데이터 수가 페이지 번호 목록의 끝 번호 * 페이지 사이즈보다 크면 다음 페이지가 존재한다고 판단

        this.pageNumList= IntStream.rangeClosed(start,end).boxed().collect(Collectors.toList());
        //시작 번호 부터 끝 번호 까지의 정수 스트림을 새성하고, 이를 리스트로 변환하여 페이지 번호 목록을 초기화

        if (prev) {
            this.prevPage = prev?start-1:1; // 이전 페이지가 존재 하지 않으면 prev는 1 존재하면 start-1
        }

        if (next) {
            this.nextPage = next?end+1:0; // 다음 페이지가 존재하지 않으면 next는 0 존재하면 end+1
        }

        this.totalPage = this.pageNumList.size();
        this.current = pageRequestDTO.getPage();

    }


}
