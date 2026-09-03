package com.booklibrary.booklibrary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.booklibrary.booklibrary.dto.request.MemberRequest;
import com.booklibrary.booklibrary.dto.response.MemberResponse;
import com.booklibrary.booklibrary.entity.Member;
import com.booklibrary.booklibrary.exception.ResourceNotFoundException;
import com.booklibrary.booklibrary.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

  @Mock
  private MemberRepository memberRepository;

  @InjectMocks
  private MemberService memberService;

  private Member sampleMember() {
    Member member = new Member();
    member.setId(1L);
    member.setName("John Doe");
    member.setEmail("john.doe@example.com");
    member.setPhoneNumber("+62834567890");
    return member;
  }

  @Test
  void getMemberById_whenExists_returnsMemberResponse() {
    when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember()));

    MemberResponse result = memberService.getMemberById(1L);

    assertEquals(1L, result.getId());
    assertEquals("John Doe", result.getName());
  }

  @Test
  void getMemberById_whenNotFound_throwsResourceNotFoundException() {
    when(memberRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> memberService.getMemberById(99L));
  }

  @Test
  void createMember_savesAndReturnsResponse() {
    MemberRequest request = new MemberRequest();
    request.setName("Jane Doe");
    request.setEmail("jane.doe@example.com");
    request.setPhoneNumber("+62811111111");

    when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
      Member saved = invocation.getArgument(0);
      saved.setId(2L);
      return saved;
    });

    MemberResponse result = memberService.createMember(request);

    assertEquals(2L, result.getId());
    assertEquals("Jane Doe", result.getName());
  }

  @Test
  void updateMember_whenNotFound_throwsResourceNotFoundException() {
    when(memberRepository.findById(5L)).thenReturn(Optional.empty());

    MemberRequest request = new MemberRequest();
    request.setName("Updated");
    request.setEmail("updated@example.com");
    request.setPhoneNumber("+62800000000");

    assertThrows(ResourceNotFoundException.class, () -> memberService.updateMember(5L, request));
  }

  @Test
  void deleteMember_callsRepositoryDeleteById() {
    memberService.deleteMember(1L);

    verify(memberRepository).deleteById(1L);
  }

  @Test
  void searchMembersByName_returnsMappedList() {
    when(memberRepository.findByNameContainingIgnoreCase("john")).thenReturn(List.of(sampleMember()));

    List<MemberResponse> result = memberService.searchMembersByName("john");

    assertEquals(1, result.size());
    assertEquals("John Doe", result.get(0).getName());
  }

  @Test
  void getAllMembers_returnsPagedResponse() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Member> memberPage = new PageImpl<>(List.of(sampleMember()), pageable, 1);

    when(memberRepository.findAll(pageable)).thenReturn(memberPage);

    Page<MemberResponse> result = memberService.getAllMembers(pageable);

    assertEquals(1, result.getTotalElements());
  }
}
