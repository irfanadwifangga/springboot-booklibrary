package com.booklibrary.booklibrary.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.booklibrary.booklibrary.entity.Member;
import com.booklibrary.booklibrary.repository.MemberRepository;

@Service
public class MemberService {
  private final MemberRepository memberRepository;

  public MemberService(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

  public List<Member> getAllMembers() {
    return memberRepository.findAll();
  }

  public Member getMemberById(Long id) {
    return memberRepository.findById(id).orElseThrow(() -> new RuntimeException("Member not found with id " + id));
  }

  public Member createMember(Member member) {
    return memberRepository.save(member);
  }

  public Member updateMember(Long id, Member updatedMember) {
    Member existingMember = getMemberById(id);
    existingMember.setName(updatedMember.getName());
    existingMember.setEmail(updatedMember.getEmail());
    existingMember.setPhoneNumber(updatedMember.getPhoneNumber());
    return memberRepository.save(existingMember);
  }

  public void deleteMember(Long id) {
    memberRepository.deleteById(id);
  }

  public List<Member> searchMembersByName(String name) {
    return memberRepository.findByNameContainingIgnoreCase(name);
  }
}
