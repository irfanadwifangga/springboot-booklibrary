package com.booklibrary.booklibrary.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.booklibrary.booklibrary.dto.request.MemberRequest;
import com.booklibrary.booklibrary.dto.response.MemberResponse;
import com.booklibrary.booklibrary.service.MemberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Member Controller", description = "APIs for managing members in the library")
@RestController
@RequestMapping("/api/members")
public class MemberController {
  private final MemberService memberService;

  public MemberController(MemberService memberService) {
    this.memberService = memberService;
  }

  @Operation(summary = "Get all members", description = "Retrieve a list of all members in the library")
  @GetMapping
  public Page<MemberResponse> getAllMembers(@PageableDefault(size = 10, sort = "id") Pageable pageable) {
    return memberService.getAllMembers(pageable);
  }

  @Operation(summary = "Search members by name", description = "Search members by a keyword matched against their name")
  @GetMapping("/search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved the members"),
  })
  public List<MemberResponse> searchMembersByName(@RequestParam String name) {
    return memberService.searchMembersByName(name);
  }

  @Operation(summary = "Get member by ID", description = "Retrieve a member by its ID")
  @GetMapping("/{id}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved the member"),
      @ApiResponse(responseCode = "404", description = "Member not found")
  })
  public ResponseEntity<MemberResponse> getMemberById(@PathVariable Long id) {
    return ResponseEntity.ok(memberService.getMemberById(id));
  }

  @Operation(summary = "Create a new member", description = "Add a new member to the library")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Member created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid member data")
  })
  @PostMapping
  public ResponseEntity<MemberResponse> createMember(@Valid @RequestBody MemberRequest request) {
    MemberResponse savedMember = memberService.createMember(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedMember);
  }

  @Operation(summary = "Update an existing member", description = "Update the details of an existing member")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Member updated successfully"),
      @ApiResponse(responseCode = "404", description = "Member not found"),
      @ApiResponse(responseCode = "400", description = "Invalid member data")
  })
  @PutMapping("/{id}")
  public ResponseEntity<MemberResponse> updateMember(@PathVariable Long id, @Valid @RequestBody MemberRequest request) {
    return ResponseEntity.ok(memberService.updateMember(id, request));
  }

  @Operation(summary = "Delete a member", description = "Remove a member from the library by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Member deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Member not found")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
    memberService.deleteMember(id);
    return ResponseEntity.noContent().build();
  }
}
