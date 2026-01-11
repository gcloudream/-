package com.silemore.controller;

import com.silemore.config.UserPrincipal;
import com.silemore.dto.ApiResponse;
import com.silemore.dto.ContactCreateRequest;
import com.silemore.dto.ContactCreateResponse;
import com.silemore.dto.ContactListResponse;
import com.silemore.dto.ContactVerifyRequest;
import com.silemore.dto.ContactVerifyResponse;
import com.silemore.entity.EmergencyContact;
import com.silemore.entity.User;
import com.silemore.service.ContactService;
import com.silemore.service.UserService;
import com.silemore.util.MaskingUtil;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contacts")
public class ContactController {
    private final ContactService contactService;
    private final UserService userService;

    public ContactController(ContactService contactService, UserService userService) {
        this.contactService = contactService;
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<ContactListResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getUser(principal.getUserId());
        List<EmergencyContact> contacts = contactService.listContacts(user);
        List<ContactListResponse.ContactItem> items = contacts.stream()
                .map(contact -> new ContactListResponse.ContactItem(
                        contact.getId(),
                        contact.getName(),
                        MaskingUtil.maskEmail(contact.getEmail()),
                        contact.getIsVerified(),
                        contact.getCreatedAt()
                ))
                .collect(Collectors.toList());

        int total = items.size();
        int limit = 5;
        int remaining = Math.max(0, limit - total);
        return ApiResponse.success(new ContactListResponse(items, total, limit, remaining));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContactCreateResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ContactCreateRequest request) {
        User user = userService.getUser(principal.getUserId());
        EmergencyContact contact = contactService.addContact(user, request);
        ContactCreateResponse response = new ContactCreateResponse(
                contact.getId(),
                contact.getName(),
                MaskingUtil.maskEmail(contact.getEmail()),
                contact.getRelationship(),
                contact.getIsVerified(),
                contact.getCreatedAt(),
                contact.getCreatedAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "Contact created", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable Long id) {
        User user = userService.getUser(principal.getUserId());
        contactService.deleteContact(user, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify")
    public ApiResponse<ContactVerifyResponse> verify(@Valid @RequestBody ContactVerifyRequest request) {
        EmergencyContact contact = contactService.verifyContact(request.token());
        ContactVerifyResponse response = new ContactVerifyResponse(
                contact.getUser().getNickname(),
                contact.getName()
        );
        return ApiResponse.success(response);
    }

    @GetMapping("/verify")
    public ApiResponse<ContactVerifyResponse> verifyLink(@RequestParam("token") String token,
                                                         @RequestParam(value = "action", defaultValue = "verify")
                                                                 String action) {
        if ("reject".equalsIgnoreCase(action)) {
            contactService.rejectContact(token);
            return ApiResponse.message("Contact request declined");
        }

        EmergencyContact contact = contactService.verifyContact(token);
        ContactVerifyResponse response = new ContactVerifyResponse(
                contact.getUser().getNickname(),
                contact.getName()
        );
        return ApiResponse.success(response);
    }
}
