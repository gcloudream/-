package com.silemore.service;

import com.silemore.dto.ContactCreateRequest;
import com.silemore.entity.EmergencyContact;
import com.silemore.entity.User;
import com.silemore.exception.AppException;
import com.silemore.repository.EmergencyContactRepository;
import com.silemore.util.TimeUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactService {
    private static final int CONTACT_LIMIT = 5;
    private static final int VERIFY_EXPIRE_DAYS = 7;

    private final EmergencyContactRepository contactRepository;
    private final EmailService emailService;

    public ContactService(EmergencyContactRepository contactRepository,
                          EmailService emailService) {
        this.contactRepository = contactRepository;
        this.emailService = emailService;
    }

    public List<EmergencyContact> listContacts(User user) {
        return contactRepository.findByUserId(user.getId());
    }

    @Transactional
    public EmergencyContact addContact(User user, ContactCreateRequest request) {
        long count = contactRepository.countByUserId(user.getId());
        if (count >= CONTACT_LIMIT) {
            throw new AppException(30001, HttpStatus.BAD_REQUEST, "Contact limit reached");
        }
        if (contactRepository.existsByUserIdAndEmailIgnoreCase(user.getId(), request.email())) {
            throw new AppException(30002, HttpStatus.CONFLICT, "Contact already exists");
        }

        EmergencyContact contact = new EmergencyContact();
        contact.setUser(user);
        contact.setName(request.name());
        contact.setEmail(request.email());
        contact.setRelationship(request.relationship());
        contact.setIsVerified(false);
        contact.setVerifyToken(UUID.randomUUID().toString().replace("-", ""));

        EmergencyContact saved;
        try {
            saved = contactRepository.save(contact);
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(30002, HttpStatus.CONFLICT, "Contact already exists");
        }

        emailService.sendVerificationEmail(saved, user, saved.getVerifyToken(), request.message());
        return saved;
    }

    @Transactional
    public void deleteContact(User user, Long contactId) {
        EmergencyContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new AppException(404, HttpStatus.NOT_FOUND, "Contact not found"));
        if (!contact.getUser().getId().equals(user.getId())) {
            throw new AppException(403, HttpStatus.FORBIDDEN, "Forbidden");
        }
        contactRepository.delete(contact);
        try {
            emailService.sendRemovedEmail(contact, user);
        } catch (Exception ignored) {
        }
    }

    @Transactional
    public EmergencyContact verifyContact(String token) {
        EmergencyContact contact = findValidContactByToken(token);
        contact.setIsVerified(true);
        contact.setVerifyToken(null);
        return contactRepository.save(contact);
    }

    @Transactional
    public void rejectContact(String token) {
        EmergencyContact contact = findValidContactByToken(token);
        contactRepository.delete(contact);
    }

    private EmergencyContact findValidContactByToken(String token) {
        EmergencyContact contact = contactRepository.findByVerifyToken(token)
                .orElseThrow(() -> new AppException(30003, HttpStatus.BAD_REQUEST, "Verification link expired"));

        LocalDateTime expiresAt = contact.getCreatedAt().plusDays(VERIFY_EXPIRE_DAYS);
        if (TimeUtil.now().isAfter(expiresAt)) {
            throw new AppException(30003, HttpStatus.BAD_REQUEST, "Verification link expired");
        }

        return contact;
    }
}
