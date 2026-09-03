package com.booklibrary.booklibrary.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.booklibrary.booklibrary.dto.request.MemberRequest;
import com.booklibrary.booklibrary.dto.response.MemberResponse;
import com.booklibrary.booklibrary.entity.Member;
import com.booklibrary.booklibrary.exception.ResourceNotFoundException;
import com.booklibrary.booklibrary.repository.MemberRepository;

@Service
public class MemberService {
  private final MemberRepository memberRepository;

  public MemberService(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

  public Page<MemberResponse> getAllMembers(Pageable pageable) {
    return memberRepository.findAll(pageable).map(this::toResponse);
  }

  public MemberResponse getMemberById(Long id) {
    return memberRepository.findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Member not found with id " + id));
  }

  public MemberResponse createMember(MemberRequest request) {
    Member member = new Member();
    member.setName(request.getName());
    member.setEmail(request.getEmail());
    member.setPhoneNumber(request.getPhoneNumber());
    return toResponse(memberRepository.save(member));
  }

  public MemberResponse updateMember(Long id, MemberRequest updatedMember) {
    Member existingMember = memberRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Member not found with id " + id));
    existingMember.setName(updatedMember.getName());
    existingMember.setEmail(updatedMember.getEmail());
    existingMember.setPhoneNumber(updatedMember.getPhoneNumber());
    return toResponse(memberRepository.save(existingMember));
  }

  public void deleteMember(Long id) {
    memberRepository.deleteById(id);
  }

  public List<MemberResponse> searchMembersByName(String name) {
    return memberRepository.findByNameContainingIgnoreCase(name).stream().map(this::toResponse)
        .collect(Collectors.toList());
  }

  private MemberResponse toResponse(Member member) {
    MemberResponse response = new MemberResponse();
    response.setId(member.getId());
    response.setName(member.getName());
    response.setEmail(member.getEmail());
    response.setPhoneNumber(member.getPhoneNumber());
    return response;
  }
}
